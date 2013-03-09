package org.buildroot.cdt.toolchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.ui.IStartup;

public class BuildrootToolchainsProvider implements IStartup {
	private enum BuildArtefactType {
		CDT_EXE("exe"), CDT_SHARED_LIBRARY("sharedLib"), CDT_STATIC_LIBRARY(
				"staticLib");

		String value;

		private BuildArtefactType(String val) {
			value = val;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * Two types of configuration exist : debug or release.
	 */
	public enum ConfigurationType {
		DEBUG, RELEASE
	}

	/**
	 * It exists 6 types of tool : assembler, compilers for C and C++, linkers
	 * for C and C++, pkg-config.
	 * */
	private enum BuildrootToolType {
		ASSEMBLER, C_COMPILER, CC_COMPILER, C_LINKER, CC_LINKER, PKG_CONFIG
	}

	@Override
	public void earlyStartup() {
		// When a Buildroot project is built with BR2_ECLIPSE_REGISTER, it adds
		// a few information describing the generated toolchain into
		// $HOME/.buildroot-eclipse.toolchains.
		String buildrootConfigFilePath = System.getProperty("user.home")
				+ File.separator + ".buildroot-eclipse.toolchains";

		// Parse the build configuration and provide dynamically the
		// configuration information to CDT
		parseBuildrootConfiguration(buildrootConfigFilePath);
	}

	private void parseBuildrootConfiguration(String buildrootConfigFilePath) {
		File file = new File(buildrootConfigFilePath);
		Scanner input;
		List<IManagedConfigElement> configElements = new ArrayList<IManagedConfigElement>();
		try {
			input = new Scanner(file);

			while (input.hasNext()) {
				String nextLine = input.nextLine();
				String[] config = nextLine.split(":");
				String path = config[0];
				String prefix = config[1];
				String architecture = config[2].toUpperCase();

				// If gcc compiler is not defined for the current toolchain
				// ignore it
				if (!BuildrootUtils.isCompilerAvailable(path, prefix, "gcc"))
					continue;

				registerBuildrootToolchains(path, prefix, architecture);

				// Create launch configuration
				BuildrootLaunchConfiguration launchConfiguration = new BuildrootLaunchConfiguration(
						path, prefix, architecture);
				launchConfiguration.createLaunchConfiguration();
			}

			input.close();
		} catch (FileNotFoundException e) {
			BuildrootActivator.getDefault().error(
					"Buildroot configuration file does not exist : "
							+ buildrootConfigFilePath, e);
		}
	}

	private void registerBuildrootToolchains(String path, String prefix,
			String architecture) {
		StringBuffer buffer = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<?eclipse version=\"3.4\"?>");
		buffer.append("<plugin>");
		buffer.append("<extension");
		buffer.append(" point=\"org.eclipse.cdt.managedbuilder.core.buildDefinitions\">");

		// Create toolchain
		buffer.append(createToolchain(path, prefix, architecture));

		// Create project types
		for (BuildArtefactType buildArtefactType : BuildArtefactType.values()) {
			buffer.append(createProjectType(path, prefix, architecture,
					buildArtefactType));
		}
		buffer.append("</extension>");

		buffer.append("</plugin>");

		// Register this extension dynamically
		BuildrootUtils.registerExtensionPoint(buffer);
	}

	private StringBuffer createProjectType(String path, String prefix,
			String architecture, BuildArtefactType artefactType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<projectType");
		buffer.append(" buildArtefactType=\""
				+ "org.eclipse.cdt.build.core.buildArtefactType."
				+ artefactType.getValue() + "\"");
		buffer.append(" id=\"" + getIdentifier(path, artefactType.getValue())
				+ "\"");
		buffer.append(" isAbstract=\"false\"");
		buffer.append(" isTest=\"false\"");
		buffer.append(" projectEnvironmentSupplier=\"org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootCrossEnvironmentVariableSupplier\">");

		// Create debug configuration
		buffer.append(createConfiguration(path, ConfigurationType.DEBUG,
				artefactType));

		// Create release configuration
		buffer.append(createConfiguration(path, ConfigurationType.RELEASE,
				artefactType));
		buffer.append("</projectType>");
		return buffer;
	}

	private StringBuffer createConfiguration(String path,
			ConfigurationType configType, BuildArtefactType artefactType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<configuration");
		buffer.append(" buildProperties=\""
				+ "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType."
				+ configType.name().toLowerCase() + "\"");
		buffer.append(" cleanCommand=\"rm -rf\"");
		buffer.append(" id=\""
				+ getIdentifier(path, artefactType.getValue() + "."
						+ configType.toString().toLowerCase()) + "\"");
		buffer.append(" name=\"" + configType.toString().toLowerCase() + "\"");
		buffer.append(" parent=\"cdt.managedbuild.config.gnu.base\">");
		buffer.append(createToolchainRef(path, artefactType, configType));
		buffer.append("</configuration>");
		return buffer;
	}

	private StringBuffer createToolchainRef(String path,
			BuildArtefactType artefactType, ConfigurationType configType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<toolChain");
		buffer.append(" id=\""
				+ getIdentifier(path, artefactType.getValue() + "."
						+ configType.name().toLowerCase()) + "\"");
		buffer.append(" superClass=\"" + getToolchainIdentifier(path) + "\">");
		buffer.append("</toolChain>");
		return buffer;
	}

	private StringBuffer createToolchain(String path, String prefix,
			String architecture) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<toolChain");
		buffer.append(" archList=\"all\"");
		buffer.append(" id=\"" + getToolchainIdentifier(path) + "\"");
		buffer.append(" isAbstract=\"false\"");
		buffer.append(" name=\""
				+ BuildrootUtils.getToolName(architecture, path, null) + "\"");
		buffer.append(" osList=\"linux\">");

		// Create target platform
		buffer.append(createTargetPlatform(path, architecture));

		// Create assembler
		buffer.append(createTool(path, prefix, architecture,
				BuildrootToolType.ASSEMBLER));

		// Create C compiler. We ignore all the toolchain that does not define a
		// C compiler.
		buffer.append(createTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER));

		// Create C Linker
		buffer.append(createTool(path, prefix, architecture,
				BuildrootToolType.C_LINKER));

		// Create C++ compiler if necessary
		if (BuildrootUtils.isCompilerAvailable(path, prefix, "g++")) {
			buffer.append(createTool(path, prefix, architecture,
					BuildrootToolType.CC_COMPILER));
			buffer.append(createTool(path, prefix, architecture,
					BuildrootToolType.CC_LINKER));
		}

		// Create pkg-config
		buffer.append(createTool(path, prefix, architecture,
				BuildrootToolType.PKG_CONFIG));

		// Create builder
		buffer.append(createBuilder(path, architecture));
		buffer.append("</toolChain>");
		return buffer;
	}

	private String getToolchainIdentifier(String path) {
		return getIdentifier(path, ".toolchain.base");
	}

	private StringBuffer createBuilder(String path, String architecture) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<builder");
		buffer.append(" command=\"make\"");
		buffer.append(" id=\"" + getIdentifier(path, "builder") + "\"");
		buffer.append(" isAbstract=\"false\"");
		buffer.append(" name=\""
				+ BuildrootUtils.getToolName(architecture, path, "builder")
				+ "\"");
		buffer.append(" isVariableCaseSensitive=\"false\"");
		buffer.append(" superClass=\"cdt.managedbuild.target.gnu.builder\">");
		buffer.append("</builder>");
		return buffer;
	}

	private StringBuffer createTool(String path, String prefix,
			String architecture, BuildrootToolType toolType) {
		String toolName = null;
		String idSuffix = null;
		String toolDescription = null;
		String toolPath = null;
		String superClass = null;
		String natureFilter = null;

		switch (toolType) {
		case ASSEMBLER:
			superClass = "cdt.managedbuild.tool.gnu.assembler";
			natureFilter = "both";
			toolName = "as";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "assembler";
			toolDescription = "Assembler";
			break;

		case C_COMPILER:
			superClass = "cdt.managedbuild.tool.gnu.c.compiler";
			natureFilter = "both";
			toolName = "gcc";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "c.compiler";
			toolDescription = "C Compiler";
			break;

		case CC_COMPILER:
			superClass = "cdt.managedbuild.tool.gnu.cpp.compiler";
			natureFilter = "ccnature";
			toolName = "g++";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "cc.compiler";
			toolDescription = "C++ Compiler";
			break;

		case C_LINKER:
			superClass = "cdt.managedbuild.tool.gnu.c.linker";
			natureFilter = "cnature";
			toolName = "gcc";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "c.linker";
			toolDescription = "C Linker";
			break;

		case CC_LINKER:
			superClass = "cdt.managedbuild.tool.gnu.cpp.linker";
			natureFilter = "ccnature";
			toolName = "g++";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "cc.linker";
			toolDescription = "CC Linker";
			break;

		case PKG_CONFIG:
			superClass = "org.eclipse.cdt.managedbuilder.pkgconfig.tool";
			natureFilter = "both";
			toolName = "pkg-config";
			toolPath = BuildrootUtils.getToolPath(path, toolName);
			idSuffix = "pkgconfig";
			toolDescription = "Pkg config";
			break;

		default:
			break;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("<tool");
		buffer.append(" command=\"" + toolPath + "\"");
		buffer.append(" commandLineGenerator=\"org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator\"");
		buffer.append(" id=\"" + getIdentifier(path, idSuffix) + "\"");
		buffer.append(" isAbstract=\"false\"");
		buffer.append(" name=\""
				+ BuildrootUtils.getToolName(architecture, path,
						toolDescription) + "\"");
		buffer.append(" natureFilter=\"" + natureFilter + "\"");
		buffer.append(" superClass=\"" + superClass + "\">");
		if (toolType == BuildrootToolType.C_COMPILER
				|| toolType == BuildrootToolType.CC_COMPILER) {
			buffer.append(createInputType(path, architecture, toolType,
					toolPath));
		}
		buffer.append("</tool>");
		return buffer;
	}

	private StringBuffer createInputType(String path, String architecture,
			BuildrootToolType toolType, String toolPath) {
		String scannerConfigProfileId = null;
		String superClass = null;
		String id = null;

		switch (toolType) {
		case C_COMPILER:
			id = getIdentifier(path, "c.input");
			scannerConfigProfileId = getScannerConfigProfileId(path,
					architecture, toolType);
			superClass = "cdt.managedbuild.tool.gnu.c.compiler.input";
			break;

		case CC_COMPILER:
			id = getIdentifier(path, "cpp.input");
			scannerConfigProfileId = getScannerConfigProfileId(path,
					architecture, toolType);
			superClass = "cdt.managedbuild.tool.gnu.cpp.compiler.input";
			break;
		default:
			break;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<inputType");
		buffer.append(" superClass=\"" + superClass + "\"");
		buffer.append(" id=\"" + id + "\"");
		buffer.append(" scannerConfigDiscoveryProfileId=\""
				+ scannerConfigProfileId + "\">");
		buffer.append("</inputType>");

		// Get the scanner configuration discovery profile
		StringBuffer buffer2 = createScannerConfigurationDiscoveryProfile(path,
				architecture, toolType, toolPath);

		// Register this extension dynamically
		BuildrootUtils.registerExtensionPoint(buffer2);

		return buffer;
	}

	/**
	 * Get the scanner configuration discovery profile for the current input
	 * type.
	 * 
	 * @param path
	 * @param architecture
	 * @param toolType
	 * @param command
	 * 
	 * @return Scanner configuration discovery profile extension point in string
	 *         format.
	 */
	private StringBuffer createScannerConfigurationDiscoveryProfile(
			String path, String architecture, BuildrootToolType toolType,
			String command) {
		StringBuffer buffer = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<?eclipse version=\"3.4\"?>");
		buffer.append("<plugin>");
		buffer.append("	<extension");
		buffer.append("		id=\""
				+ getScannerConfigProfileId(path, architecture, toolType)
				+ "\"");
		buffer.append("		name=\"Buildroot ManagedMakePerProjectProfileC\"");
		buffer.append("		point=\"org.eclipse.cdt.make.core.ScannerConfigurationDiscoveryProfile\">");
		buffer.append("		<scannerInfoCollector");
		buffer.append("			class=\"org.buildroot.cdt.toolchain.DefaultGCCScannerInfoCollector\"");
		buffer.append("			scope=\"project\">");
		buffer.append("		</scannerInfoCollector>");
		buffer.append("		<buildOutputProvider>");
		buffer.append("			<open></open>");
		buffer.append("			<scannerInfoConsoleParser");
		buffer.append("				class=\"org.buildroot.cdt.toolchain.ManagedGCCScannerInfoConsoleParser\">");
		buffer.append("			</scannerInfoConsoleParser>");
		buffer.append("		</buildOutputProvider>");
		buffer.append("		<scannerInfoProvider");
		buffer.append("			providerId=\"specsFile\">");
		buffer.append(" 		<run");
		buffer.append("				arguments=\"-E -P -v -dD ${plugin_state_location}/"
				+ getSpecFileName(toolType) + "\"");
		buffer.append("				class=\"org.eclipse.cdt.make.internal.core.scannerconfig2.GCCSpecsRunSIProvider\"");
		buffer.append("				command=\"" + command + "\">");
		buffer.append("			</run>");
		buffer.append("			<scannerInfoConsoleParser");
		buffer.append("				class=\"org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCSpecsConsoleParser\">");
		buffer.append("			</scannerInfoConsoleParser>");
		buffer.append("		</scannerInfoProvider>");
		buffer.append("	</extension>");
		buffer.append("</plugin>");
		return buffer;
	}

	/**
	 * Get Spec file name according for current input type.
	 * 
	 * @param toolType
	 * 
	 * @return Spec file name
	 */
	private String getSpecFileName(BuildrootToolType toolType) {
		switch (toolType) {
		case C_COMPILER:
			return "specs.c";
		case CC_COMPILER:
			return "specs.cpp";
		default:
			return null;
		}
	}

	private String getScannerConfigProfileId(String path, String architecture,
			BuildrootToolType toolType) {
		switch (toolType) {
		case C_COMPILER:
			return getIdentifier(path, architecture
					+ "_ManagedMakePerProjectProfileC");

		case CC_COMPILER:
			return getIdentifier(path, architecture
					+ "_ManagedMakePerProjectProfileCPP");
		default:
			break;
		}
		return null;
	}

	private StringBuffer createTargetPlatform(String path, String architecture) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<targetPlatform");
		buffer.append(" archList=\"all\"");
		buffer.append(" binaryParser=\"org.eclipse.cdt.core.GNU_ELF\"");
		buffer.append(" id=\"" + getIdentifier(path, "platform.base") + "\"");
		buffer.append(" isAbstract=\"false\"");
		buffer.append(" name=\""
				+ BuildrootUtils.getToolName(architecture, path, "Platform")
				+ "\"");
		buffer.append(" osList=\"linux\">");
		buffer.append("</targetPlatform>");
		return buffer;
	}

	private String getIdentifier(String path, String suffix) {
		path = path.replaceAll("/", ".");
		if (path.endsWith("."))
			path = path.substring(0, path.length() - 1);
		if (path.startsWith("."))
			path = path.substring(1, path.length());

		return "org.buildroot." + path + "." + suffix;
	}
}
