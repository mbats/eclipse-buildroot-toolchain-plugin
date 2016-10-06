package org.buildroot.cdt.toolchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.buildroot.cdt.toolchain.BuildrootConfigElement.ManagedConfigElement;
import org.buildroot.cdt.toolchain.BuildrootConfigElement.ManagedConfigElementAttribute;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElementProvider;

public class BuildrootToolchainsProvider implements
		IManagedConfigElementProvider {

	private static final String STRING = "string";
	private static final String ALL = "all";
	private static final String FALSE = "false";

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
		ASSEMBLER, C_COMPILER, CC_COMPILER, ARCHIVER, C_LINKER, CC_LINKER, PKG_CONFIG
	}

	@Override
	public IManagedConfigElement[] getConfigElements() {
		// When a Buildroot project is built with BR2_ECLIPSE_REGISTER, it adds
		// a few information describing the generated toolchain into
		// $HOME/.buildroot-eclipse.toolchains.
		String buildrootConfigFilePath = System.getProperty("user.home")
				+ File.separator + ".buildroot-eclipse.toolchains";

		// Parse the build configuration and provide dynamically the
		// configuration information to CDT
		return parseBuildrootConfiguration(buildrootConfigFilePath);
	}

	/**
	 * Parse the buildroot configuration file to create project types.
	 * 
	 * @param buildrootConfigFilePath
	 *            Path to the buildroot configuration file
	 * @return List of CDT configuration elements
	 */
	private IManagedConfigElement[] parseBuildrootConfiguration(
			String buildrootConfigFilePath) {
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

				configElements.addAll(registerBuildrootToolchains(path, prefix,
						architecture));

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
		return (IManagedConfigElement[]) configElements
				.toArray(new IManagedConfigElement[configElements.size()]);
	}

	private List<IManagedConfigElement> registerBuildrootToolchains(
			String path, String prefix, String architecture) {
		List<IManagedConfigElement> configElements = new ArrayList<IManagedConfigElement>();

		// Create toolchain
		configElements.add(createToolchain(path, prefix, architecture));

		// Create executable, static lib and shared lib project types
		for (BuildArtefactType buildArtefactType : BuildArtefactType.values()) {
			configElements.add(createProjectType(path, prefix, architecture,
					buildArtefactType));
		}

		// Create Autotools toolchain
		configElements
				.add(createAutotoolsToolchain(path, prefix, architecture));

		// Create Autotools project type
		configElements.add(createAutotoolsProjectType(path, prefix,
				architecture));

		return configElements;
	}

	private IManagedConfigElement createAutotoolsToolchain(String path,
			String prefix, String architecture) {
		BuildrootConfigElement toolchain = new BuildrootConfigElement(
				ManagedConfigElement.TOOLCHAIN);
		toolchain.setAttribute(ManagedConfigElementAttribute.archList, ALL);
		toolchain
				.setAttribute(
						ManagedConfigElementAttribute.configurationEnvironmentSupplier,
						"org.buildroot.cdt.toolchain.BuildrootEnvironmentVariableSupplier");
		toolchain.setAttribute(ManagedConfigElementAttribute.id,
				getAutotoolsToolchainIdentifier(path));
		toolchain.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		toolchain.setAttribute(ManagedConfigElementAttribute.name, "Autotools "
				+ BuildrootUtils.getToolName(architecture, path, null));
		toolchain.setAttribute(ManagedConfigElementAttribute.osList,
				BuildrootConfigElement.LINUX);
		toolchain.setAttribute(ManagedConfigElementAttribute.superClass,
				"org.eclipse.linuxtools.cdt.autotools.core.toolChain");

		// Create options and option category
		toolchain.addChildren(createOptions(path, prefix,
				getAutotoolsToolchainIdentifier(path)));

		// Create configure
		toolchain.addChild(createConfigureTool(path, prefix, architecture));

		// Create tools
		toolchain.addChild(createAutotoolsTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER));
		toolchain.addChild(createAutotoolsTool(path, prefix, architecture,
				BuildrootToolType.CC_COMPILER));

		return toolchain;
	}

	private IManagedConfigElement createAutotoolsTool(String path,
			String prefix, String architecture, BuildrootToolType toolType) {

		String toolName = null;
		String toolchainSuffix = null;
		String idSuffix = null;
		String toolDescription = null;
		String toolPath = null;
		String natureFilter = null;
		switch (toolType) {
		case C_COMPILER:
			natureFilter = "both";
			toolName = "gcc";
			toolchainSuffix = "gcc";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "autotools.c.compiler";
			toolDescription = "C Compiler";
			break;

		case CC_COMPILER:
			natureFilter = "ccnature";
			toolName = "g++";
			toolchainSuffix = "gpp";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "autotools.cc.compiler";
			toolDescription = "C++ Compiler";
			break;
		default:
			break;
		}
		BuildrootConfigElement tool = new BuildrootConfigElement(
				ManagedConfigElement.TOOL);
		tool.setAttribute(ManagedConfigElementAttribute.command, toolPath);
		tool.setAttribute(
				ManagedConfigElementAttribute.commandLineGenerator,
				BuildrootConfigElement.CDT_MANAGEDBUILDER_COMMAND_LINE_GENERATOR);
		tool.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, idSuffix));
		tool.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		tool.setAttribute(
				ManagedConfigElementAttribute.name,
				"Autotools "
						+ BuildrootUtils.getToolName(architecture, path,
								toolDescription));
		tool.setAttribute(ManagedConfigElementAttribute.natureFilter,
				natureFilter);
		tool.setAttribute(ManagedConfigElementAttribute.superClass,
				"org.eclipse.linuxtools.cdt.autotools.core.toolchain.tool."
						+ toolchainSuffix);

		BuildrootConfigElement inputType = new BuildrootConfigElement(
				ManagedConfigElement.INPUT_TYPE);
		inputType.setAttribute(ManagedConfigElementAttribute.superClass,
				BuildrootConfigElement.CDT_MANAGEDBUILD_C_COMPILER_INPUT);
		inputType.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, toolType.name().toLowerCase() + ".input"));
		inputType.setAttribute(
				ManagedConfigElementAttribute.scannerConfigDiscoveryProfileId,
				getScannerConfigProfileId(path, architecture, toolType));

		tool.addChild(inputType);

		return tool;
	}

	private IManagedConfigElement createConfigureTool(String path,
			String prefix, String architecture) {
		BuildrootConfigElement tool = new BuildrootConfigElement(
				ManagedConfigElement.TOOL);
		tool.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "autotools.tool.configure"));
		tool.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		tool.setAttribute(ManagedConfigElementAttribute.superClass,
				"org.eclipse.linuxtools.cdt.autotools.core.tool.configure");

		BuildrootConfigElement option = new BuildrootConfigElement(
				ManagedConfigElement.OPTION);
		option.setAttribute(ManagedConfigElementAttribute.defaultValue,
				prefix.substring(0, prefix.length() - 1));
		option.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "autotools.toolChain.option.host"));
		option.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		option.setAttribute(ManagedConfigElementAttribute.name, "Host");
		option.setAttribute(ManagedConfigElementAttribute.resourceFilter, ALL);
		option.setAttribute(ManagedConfigElementAttribute.superClass,
				"org.eclipse.linuxtools.cdt.autotools.core.option.configure.host");
		option.setAttribute(ManagedConfigElementAttribute.valueType, STRING);
		tool.addChild(option);

		return tool;
	}

	private IManagedConfigElement createAutotoolsProjectType(String path,
			String prefix, String architecture) {
		BuildrootConfigElement projectType = new BuildrootConfigElement(
				ManagedConfigElement.PROJECT_TYPE);
		projectType
				.setAttribute(ManagedConfigElementAttribute.buildArtefactType,
						"org.eclipse.linuxtools.cdt.autotools.core.buildArtefactType.autotools");
		projectType.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "autotools"));
		projectType.setAttribute(ManagedConfigElementAttribute.isAbstract,
				FALSE);

		// Create default configuration
		projectType.addChild(createAutotoolsConfiguration(path));
		return projectType;
	}

	private IManagedConfigElement createAutotoolsConfiguration(String path) {
		BuildrootConfigElement config = new BuildrootConfigElement(
				ManagedConfigElement.CONFIGURATION);
		config.setAttribute(ManagedConfigElementAttribute.buildProperties,
				"org.eclipse.linuxtools.cdt.autotools.core.buildType.default");
		config.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "autotools.default"));
		config.setAttribute(ManagedConfigElementAttribute.name, "Configuration");
		config.setAttribute(ManagedConfigElementAttribute.parent,
				"org.eclipse.linuxtools.cdt.autotools.core.configuration.build");
		config.addChild(createAutotoolsToolchainRef(path));
		return config;
	}

	private IManagedConfigElement createAutotoolsToolchainRef(String path) {
		BuildrootConfigElement toolchain = new BuildrootConfigElement(
				ManagedConfigElement.TOOLCHAIN);
		toolchain.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "autotools.default"));
		toolchain.setAttribute(ManagedConfigElementAttribute.superClass,
				getAutotoolsToolchainIdentifier(path));
		return toolchain;
	}

	private IManagedConfigElement createProjectType(String path, String prefix,
			String architecture, BuildArtefactType artefactType) {
		BuildrootConfigElement projectType = new BuildrootConfigElement(
				ManagedConfigElement.PROJECT_TYPE);
		projectType.setAttribute(
				ManagedConfigElementAttribute.buildArtefactType,
				"org.eclipse.cdt.build.core.buildArtefactType."
						+ artefactType.getValue());
		projectType.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, artefactType.getValue()));
		projectType.setAttribute(ManagedConfigElementAttribute.isAbstract,
				FALSE);
		projectType.setAttribute(ManagedConfigElementAttribute.isTest, FALSE);
		projectType
				.setAttribute(
						ManagedConfigElementAttribute.projectEnvironmentSupplier,
						"org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootEnvironmentVariableSupplier");
		StringBuffer buffer = new StringBuffer();

		// Create debug configuration
		projectType.addChild(createConfiguration(path, ConfigurationType.DEBUG,
				artefactType));

		// Create release configuration
		projectType.addChild(createConfiguration(path,
				ConfigurationType.RELEASE, artefactType));

		return projectType;
	}

	private IManagedConfigElement createConfiguration(String path,
			ConfigurationType configType, BuildArtefactType artefactType) {
		BuildrootConfigElement config = new BuildrootConfigElement(
				ManagedConfigElement.CONFIGURATION);
		config.setAttribute(ManagedConfigElementAttribute.buildProperties,
				"org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType."
						+ configType.name().toLowerCase());
		config.setAttribute(ManagedConfigElementAttribute.cleanCommand,
				"rm -rf");
		config.setAttribute(
				ManagedConfigElementAttribute.id,
				getIdentifier(path, artefactType.getValue() + "."
						+ configType.toString().toLowerCase()));
		config.setAttribute(ManagedConfigElementAttribute.name, configType
				.toString().toLowerCase());
		config.setAttribute(ManagedConfigElementAttribute.parent,
				"cdt.managedbuild.config.gnu.base");

		config.addChild(createToolchainRef(path, artefactType, configType));

		return config;
	}

	private IManagedConfigElement createToolchainRef(String path,
			BuildArtefactType artefactType, ConfigurationType configType) {
		BuildrootConfigElement toolchain = new BuildrootConfigElement(
				ManagedConfigElement.TOOLCHAIN);
		toolchain.setAttribute(
				ManagedConfigElementAttribute.id,
				getIdentifier(path, artefactType.getValue() + "."
						+ configType.name().toLowerCase()));
		toolchain.setAttribute(ManagedConfigElementAttribute.superClass,
				getToolchainIdentifier(path));
		return toolchain;
	}

	private IManagedConfigElement createToolchain(String path, String prefix,
			String architecture) {

		BuildrootConfigElement toolchain = new BuildrootConfigElement(
				ManagedConfigElement.TOOLCHAIN);
		toolchain.setAttribute(ManagedConfigElementAttribute.archList, ALL);
		toolchain
				.setAttribute(
						ManagedConfigElementAttribute.configurationEnvironmentSupplier,
						"org.buildroot.cdt.toolchain.BuildrootEnvironmentVariableSupplier");
		toolchain.setAttribute(ManagedConfigElementAttribute.id,
				getToolchainIdentifier(path));
		toolchain.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		toolchain.setAttribute(ManagedConfigElementAttribute.name,
				BuildrootUtils.getToolName(architecture, path, null));
		toolchain.setAttribute(ManagedConfigElementAttribute.osList,
				BuildrootConfigElement.LINUX);

		// Create options and option category
		toolchain.addChildren(createOptions(path, prefix,
				getToolchainIdentifier(path)));

		// Create target platform
		toolchain.addChild(createTargetPlatform(path, architecture));

		// Create assembler
		toolchain.addChild(createTool(path, prefix, architecture,
				BuildrootToolType.ASSEMBLER));

		// Create C compiler. We ignore all the toolchain that does not define a
		// C compiler.
		toolchain.addChild(createTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER));

		// Create C Linker
		toolchain.addChild(createTool(path, prefix, architecture,
				BuildrootToolType.C_LINKER));

		// Create C++ compiler if necessary
		if (BuildrootUtils.isCompilerAvailable(path, prefix, "g++")) {
			toolchain.addChild(createTool(path, prefix, architecture,
					BuildrootToolType.CC_COMPILER));
			toolchain.addChild(createTool(path, prefix, architecture,
					BuildrootToolType.CC_LINKER));
		}

		// Create Archiver
		toolchain.addChild(createTool(path, prefix, architecture,
				BuildrootToolType.ARCHIVER));

		// Create pkg-config
		toolchain.addChild(createTool(path, prefix, architecture,
				BuildrootToolType.PKG_CONFIG));

		// Create builder
		toolchain.addChild(createBuilder(path, architecture));
		return toolchain;
	}

	private List<IManagedConfigElement> createOptions(String path,
			String prefix, String toolchainId) {
		List<IManagedConfigElement> options = new ArrayList<IManagedConfigElement>();
		BuildrootConfigElement optionCategory = new BuildrootConfigElement(
				ManagedConfigElement.OPTION_CATEGORY);
		String optionCategoryId = toolchainId + ".optionCategory";
		optionCategory.setAttribute(ManagedConfigElementAttribute.id,
				optionCategoryId);
		optionCategory.setAttribute(ManagedConfigElementAttribute.name,
				"Generic Buildroot Settings");
		options.add(optionCategory);

		options.add(createPathOption(path, toolchainId, optionCategoryId));

		options.add((createPrefixOption(prefix, toolchainId, optionCategoryId)));
		return options;
	}

	private IManagedConfigElement createPathOption(String path,
			String toolchainId, String optionCategoryId) {
		BuildrootConfigElement option = new BuildrootConfigElement(
				ManagedConfigElement.OPTION);
		option.setAttribute(ManagedConfigElementAttribute.category,
				optionCategoryId);
		option.setAttribute(ManagedConfigElementAttribute.id, toolchainId
				+ ".option.path");
		option.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		option.setAttribute(ManagedConfigElementAttribute.name, "Path");
		option.setAttribute(ManagedConfigElementAttribute.resourceFilter, ALL);
		option.setAttribute(ManagedConfigElementAttribute.value, path
				+ "/host/usr/bin");
		option.setAttribute(ManagedConfigElementAttribute.valueType, STRING);
		return option;
	}

	private IManagedConfigElement createPrefixOption(String prefix,
			String toolchainId, String optionCategoryId) {
		BuildrootConfigElement option = new BuildrootConfigElement(
				ManagedConfigElement.OPTION);
		option.setAttribute(ManagedConfigElementAttribute.category,
				optionCategoryId);
		option.setAttribute(ManagedConfigElementAttribute.id, toolchainId
				+ ".option.prefix");
		option.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		option.setAttribute(ManagedConfigElementAttribute.name, "Prefix");
		option.setAttribute(ManagedConfigElementAttribute.resourceFilter, ALL);
		option.setAttribute(ManagedConfigElementAttribute.value, prefix);
		option.setAttribute(ManagedConfigElementAttribute.valueType, STRING);
		return option;
	}

	private String getToolchainIdentifier(String path) {
		return getIdentifier(path, "toolchain.base");
	}

	private String getAutotoolsToolchainIdentifier(String path) {
		return getIdentifier(path, "autotools.toolchain.base");
	}

	private IManagedConfigElement createBuilder(String path, String architecture) {

		BuildrootConfigElement builder = new BuildrootConfigElement(
				ManagedConfigElement.BUILDER);

		builder.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "builder"));
		builder.setAttribute(ManagedConfigElementAttribute.superClass,
				"cdt.managedbuild.target.gnu.builder");
		return builder;
	}

	private IManagedConfigElement createTool(String path, String prefix,
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

		case ARCHIVER:
			superClass = "cdt.managedbuild.tool.gnu.archiver";
			natureFilter = "both";
			toolName = "ar";
			toolPath = BuildrootUtils.getPrefixedToolPath(prefix, path,
					toolName);
			idSuffix = "archiver";
			toolDescription = "Archiver";
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
			toolDescription = "C++ Linker";
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

		BuildrootConfigElement tool = new BuildrootConfigElement(
				ManagedConfigElement.TOOL);
		tool.setAttribute(ManagedConfigElementAttribute.command, toolPath);
		tool.setAttribute(
				ManagedConfigElementAttribute.commandLineGenerator,
				BuildrootConfigElement.CDT_MANAGEDBUILDER_COMMAND_LINE_GENERATOR);
		tool.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, idSuffix));
		tool.setAttribute(ManagedConfigElementAttribute.isAbstract, FALSE);
		tool.setAttribute(ManagedConfigElementAttribute.name,
				BuildrootUtils.getToolName(architecture, path, toolDescription));
		tool.setAttribute(ManagedConfigElementAttribute.natureFilter,
				natureFilter);
		tool.setAttribute(ManagedConfigElementAttribute.superClass, superClass);

		if (toolType == BuildrootToolType.C_COMPILER
				|| toolType == BuildrootToolType.CC_COMPILER) {
			tool.addChild((createInputType(path, architecture, toolType,
					toolPath)));
		}

		return tool;
	}

	private IManagedConfigElement createInputType(String path,
			String architecture, BuildrootToolType toolType, String toolPath) {
		String scannerConfigProfileId = null;
		String superClass = null;
		String id = null;

		switch (toolType) {
		case C_COMPILER:
			id = getIdentifier(path, "c.input");
			scannerConfigProfileId = getScannerConfigProfileId(path,
					architecture, toolType);
			superClass = BuildrootConfigElement.CDT_MANAGEDBUILD_C_COMPILER_INPUT;
			break;

		case CC_COMPILER:
			id = getIdentifier(path, "cpp.input");
			scannerConfigProfileId = getScannerConfigProfileId(path,
					architecture, toolType);
			superClass = BuildrootConfigElement.CDT_MANAGEDBUILD_CPP_COMPILER_INPUT;
			break;
		default:
			break;
		}

		BuildrootConfigElement inputType = new BuildrootConfigElement(
				ManagedConfigElement.INPUT_TYPE);
		inputType.setAttribute(ManagedConfigElementAttribute.superClass,
				superClass);
		inputType.setAttribute(ManagedConfigElementAttribute.id, id);
		inputType.setAttribute(
				ManagedConfigElementAttribute.scannerConfigDiscoveryProfileId,
				scannerConfigProfileId);

		// Get the scanner configuration discovery profile
		StringBuffer buffer = createScannerConfigurationDiscoveryProfile(path,
				architecture, toolType, toolPath);

		// Register this extension dynamically
		BuildrootUtils.registerExtensionPoint(buffer);

		return inputType;
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

	private IManagedConfigElement createTargetPlatform(String path,
			String architecture) {
		BuildrootConfigElement targetPlatform = new BuildrootConfigElement(
				ManagedConfigElement.TARGET_PLATFORM);
		targetPlatform
				.setAttribute(ManagedConfigElementAttribute.archList, ALL);
		targetPlatform.setAttribute(ManagedConfigElementAttribute.binaryParser,
				"org.eclipse.cdt.core.GNU_ELF");
		targetPlatform.setAttribute(ManagedConfigElementAttribute.id,
				getIdentifier(path, "platform.base"));
		targetPlatform.setAttribute(ManagedConfigElementAttribute.isAbstract,
				FALSE);
		targetPlatform.setAttribute(ManagedConfigElementAttribute.name,
				BuildrootUtils.getToolName(architecture, path, "Platform"));
		targetPlatform.setAttribute(ManagedConfigElementAttribute.osList,
				BuildrootConfigElement.LINUX);
		return targetPlatform;
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
