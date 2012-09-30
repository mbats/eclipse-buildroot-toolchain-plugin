/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Melanie Bats <melanie.bats@obeo.fr> - Initial contribution
 *******************************************************************************/
package org.buildroot.cdt.toolchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.buildroot.cdt.toolchain.launch.BuildrootLaunchConfiguration;
import org.buildroot.cdt.toolchain.managedbuilder.projecttype.BuildrootProjectType;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElementProvider;

/**
 * The Eclipse plugin reads the buildroot generated file
 * $HOME/.buildroot-eclipse.toolchains, and then creates the necessary objects
 * in Eclipse to make these toolchains usable for C/C++ projects.
 * 
 * Implements the dynamicElementProvider of extension point
 * org.eclipse.cdt.managedbuilder.core.buildProperties.
 * 
 * @author Melanie Bats <melanie.bats@obeo.fr>
 */
public class DynamicToolchainProvider implements IManagedConfigElementProvider {

	@Override
	public IManagedConfigElement[] getConfigElements() {
		// When a Buildroot project is built with BR2_ECLIPSE_REGISTER, it adds
		// a few information describing the generated toolchain into
		// $HOME/.buildroot-eclipse.toolchains.
		String buildrootConfigFilePath = System.getProperty("user.home")
				+ File.separator + ".buildroot-eclipse.toolchains";

		// Parse the build configuration and provide dynamically the
		// configuration information to CDT
		List<IManagedConfigElement> configElements = parseBuildrootConfiguration(buildrootConfigFilePath);

		return (IManagedConfigElement[]) configElements
				.toArray(new IManagedConfigElement[configElements.size()]);
	}

	/**
	 * Parse the buildroot configuration file.
	 * 
	 * Below the content of a .buildroot-eclipse.toolchains file is provided as
	 * example:
	 * 
	 * /home/opt/project-arm:arm-none-linux-gnueabi-:arm
	 * /home/opt/project-mips:mips-linux-gnu-:mipsel
	 * 
	 * Each line of the file defines a toolchain. The line is composed by :
	 * {toolchain_output_path}:{toolchain_prefix}:{architecture_name}
	 * 
	 * For each toolchain the followed configuration is generated dynamically :
	 * <toolChain archList="all"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base"
	 * isAbstract="false" name="Buildroot ARM Linux GCC" osList="linux"
	 * targetTool=
	 * "cdt.managedbuild.tool.gnu.c.linker;cdt.managedbuild.tool.gnu.cpp.linker;cdt.managedbuild.tool.gnu.archiver"
	 * > <targetPlatform archList="all"
	 * binaryParser="org.eclipse.cdt.core.GNU_ELF"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.platform.base"
	 * isAbstract="false" name="Linux Platform" osList="linux">
	 * </targetPlatform> <tool command="arm-linux-gnueabi-as"
	 * commandLineGenerator=
	 * "org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
	 * errorParsers=
	 * "org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GASErrorParser"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.assembler"
	 * isAbstract="false" name="Buildroot ARM Linux GCC Assembler"
	 * natureFilter="both" outputFlag="-o"> <supportedProperties> <property
	 * id="org.eclipse.cdt.build.core.buildType"> <value
	 * id="org.eclipse.cdt.build.core.buildType.debug"> </value> <value
	 * id="org.eclipse.cdt.build.core.buildType.release"> </value> </property>
	 * </supportedProperties> <envVarBuildPath pathType="buildpathInclude"
	 * variableList="CPATH,C_INCLUDE_PATH"> </envVarBuildPath> <inputType
	 * dependencyCalculator=
	 * "org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2"
	 * dependencyContentType="org.eclipse.cdt.core.cHeader"
	 * dependencyExtensions="h"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.assembler.input"
	 * languageId="org.eclipse.cdt.core.assembly"
	 * sourceContentType="org.eclipse.cdt.core.asmSource" sources="s,S,asm"
	 * superClass="cdt.managedbuild.tool.gnu.assembler.input"> </inputType>
	 * <outputType buildVariable="OBJS"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.output" outputs="o"
	 * superClass="cdt.managedbuild.tool.gnu.assembler.output"> </outputType>
	 * </tool> <tool command="arm-linux-gnueabi-gcc" commandLineGenerator=
	 * "org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
	 * errorParsers="org.eclipse.cdt.core.GCCErrorParser"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.c.compiler"
	 * isAbstract="false" name="Buildroot ARM Linux GCC C Compiler"
	 * natureFilter="both" superClass="cdt.managedbuild.tool.gnu.c.compiler">
	 * <inputType id="org.buildroot.cdt.toolchain.input"
	 * scannerConfigDiscoveryProfileId
	 * ="org.buildroot.cdt.toolchain.ARM_ManagedMakePerProjectProfileC"
	 * superClass="cdt.managedbuild.tool.gnu.c.compiler.input"> </inputType>
	 * </tool> <builder command="make"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.builder"
	 * isAbstract="false" isVariableCaseSensitive="false"
	 * name="Buildroot ARM GNU Make builder"
	 * superClass="cdt.managedbuild.target.gnu.builder"> </builder> <tool
	 * advancedInputCategory="false" command="arm-linux-gnueabi-gcc"
	 * commandLineGenerator=
	 * "org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
	 * errorParsers="org.eclipse.cdt.core.GLDErrorParser"
	 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.c.linker"
	 * isAbstract="false" name="Buildroot ARM GCC C Linker"
	 * natureFilter="cnature" outputFlag="-o"
	 * superClass="cdt.managedbuild.tool.gnu.c.linker"> </tool> </toolChain>
	 * <projectType
	 * buildArtefactType="org.eclipse.cdt.build.core.buildArtefactType.exe"
	 * id="com.analog.gnu.toolchain.blackfin.target.bfin.elf.exe"
	 * isAbstract="false" isTest="false"> <configuration buildProperties=
	 * "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.debug"
	 * cleanCommand="rm -rf"
	 * id="com.analog.gnu.toolchain.blackfin.config.bfin.elf.exe.debug"
	 * name="%ConfigName.Dbg" parent="cdt.managedbuild.config.gnu.base">
	 * <toolChain
	 * id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.debug"
	 * superClass
	 * ="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
	 * </toolChain> </configuration> <configuration buildProperties=
	 * "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.release"
	 * cleanCommand="rm -rf"
	 * id="com.analog.gnu.toolchain.blackfin.config.bfin.elf.exe.release"
	 * name="%ConfigName.Rel" parent="cdt.managedbuild.config.gnu.base">
	 * <toolChain
	 * id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.release"
	 * superClass
	 * ="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
	 * </toolChain> </configuration> </projectType>
	 * 
	 * @param buildrootConfigFilePath
	 *            Path to the buildroot configuration file
	 * @return List of CDT configuration elements
	 */
	private List<IManagedConfigElement> parseBuildrootConfiguration(
			final String buildrootConfigFilePath) {
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

				// Create toolchain
				BuildrootToolchain toolchain = new BuildrootToolchain(path,
						prefix, architecture);
				configElements.add(toolchain);

				// Create projectType
				BuildrootProjectType projectType = new BuildrootProjectType(
						path, toolchain);
				configElements.add(projectType);

				// Create launch configuration
				BuildrootLaunchConfiguration launchConfiguration = new BuildrootLaunchConfiguration(
						path, prefix, architecture);
				launchConfiguration.createLaunchConfiguration();
			}

			input.close();
		} catch (FileNotFoundException e) {
			BuildrootActivator.getDefault().error(
					"Buildroot configuration file doe not exist : "
							+ buildrootConfigFilePath, e);
		}
		return configElements;
	}
}
