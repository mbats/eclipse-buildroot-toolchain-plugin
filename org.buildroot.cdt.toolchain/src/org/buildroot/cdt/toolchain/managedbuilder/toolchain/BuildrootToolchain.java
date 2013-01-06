/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Melanie Bats <melanie.bats@obeo.fr> - Initial contribution
 *******************************************************************************/
package org.buildroot.cdt.toolchain.managedbuilder.toolchain;

import org.buildroot.cdt.toolchain.BuildrootUtils;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootTool.BuildrootToolType;

/**
 * For each toolchain the followed configuration is generated dynamically :
 * <toolChain
 *       archList="all"
 *       id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base"
 *       isAbstract="false"
 *       name="Buildroot ARM Linux GCC"
 *       osList="linux"
 *       targetTool="cdt.managedbuild.tool.gnu.c.linker;cdt.managedbuild.tool.gnu.cpp.linker;cdt.managedbuild.tool.gnu.archiver">
 *    <targetPlatform
 *          archList="all"
 *          binaryParser="org.eclipse.cdt.core.GNU_ELF"
 *          id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.platform.base"
 *          isAbstract="false"
 *          name="Linux Platform"
 *          osList="linux">
 *    </targetPlatform>
 *    <tool
 *          command="arm-linux-gnueabi-as"
 *          commandLineGenerator="org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
 *          errorParsers="org.eclipse.cdt.core.GCCErrorParser;org.eclipse.cdt.core.GASErrorParser"
 *          id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.assembler"
 *          isAbstract="false"
 *          name="Buildroot ARM Linux GCC Assembler"
 *          natureFilter="both"
 *          outputFlag="-o">
 *       <supportedProperties>
 *          <property
 *                id="org.eclipse.cdt.build.core.buildType">
 *             <value
 *                   id="org.eclipse.cdt.build.core.buildType.debug">
 *             </value>
 *             <value
 *                   id="org.eclipse.cdt.build.core.buildType.release">
 *             </value>
 *          </property>
 *       </supportedProperties>
 *       <envVarBuildPath
 *             pathType="buildpathInclude"
 *             variableList="CPATH,C_INCLUDE_PATH">
 *       </envVarBuildPath>
 *       <inputType
 *             dependencyCalculator="org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2"
 *             dependencyContentType="org.eclipse.cdt.core.cHeader"
 *             dependencyExtensions="h"
 *             id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.assembler.input"
 *             languageId="org.eclipse.cdt.core.assembly"
 *             sourceContentType="org.eclipse.cdt.core.asmSource"
 *             sources="s,S,asm"
 *             superClass="cdt.managedbuild.tool.gnu.assembler.input">
 *      </inputType>
 *       <outputType
 *             buildVariable="OBJS"
 *             id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.output"
 *             outputs="o"
 *             superClass="cdt.managedbuild.tool.gnu.assembler.output">
 *       </outputType>
 *    </tool>
 *    <tool
 *          command="arm-linux-gnueabi-gcc"
 *          commandLineGenerator="org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
 *          errorParsers="org.eclipse.cdt.core.GCCErrorParser"
 *          id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.c.compiler"
 *          isAbstract="false"
 *          name="Buildroot ARM Linux GCC C Compiler"
 *          natureFilter="both"
 *          superClass="cdt.managedbuild.tool.gnu.c.compiler">
 *       <inputType
 *             id="org.buildroot.cdt.toolchain.input"
 *             scannerConfigDiscoveryProfileId="org.buildroot.cdt.toolchain.ARM_ManagedMakePerProjectProfileC"
 *             superClass="cdt.managedbuild.tool.gnu.c.compiler.input">
 *       </inputType>
 *    </tool>
 *    <builder
 *          command="make"
 *          id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.builder"
 *          isAbstract="false"
 *          isVariableCaseSensitive="false"
 *          name="Buildroot ARM GNU Make builder"
 *          superClass="cdt.managedbuild.target.gnu.builder">
 *    </builder>
 *    <tool
 *          advancedInputCategory="false"
 *          command="arm-linux-gnueabi-gcc"
 *          commandLineGenerator="org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator"
 *          errorParsers="org.eclipse.cdt.core.GLDErrorParser"
 *          id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.c.linker"
 *          isAbstract="false"
 *          name="Buildroot ARM GCC C Linker"
 *          natureFilter="cnature"
 *          outputFlag="-o"
 *          superClass="cdt.managedbuild.tool.gnu.c.linker">
 *    </tool>
 * </toolChain>
 * 
 * author Melanie Bats <melanie.bats@obeo.fr>
 */
public class BuildrootToolchain extends BuildrootConfigElement {

	/**
	 * Toolchain idenfitifer.
	 */
	private String id;
	
	/**
	 * Toolchain name.
	 */
	private String name;

	/**
	 * Buildroot toolchain constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param prefix
	 *            Toolchain prefix
	 * @param architecture
	 *            Toolchain architecture
	 */
	public BuildrootToolchain(String path, String prefix, String architecture) {
		id = getIdentifier(path, ".toolchain.base");
		name = BuildrootUtils.getToolName(architecture, path, null);

		// Create target platform
		BuildrootTargetPlatform targetPlatform = new BuildrootTargetPlatform(
				path, architecture);
		addChildren(targetPlatform);

		// Create assembler
		BuildrootTool assembler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.ASSEMBLER);
		addChildren(assembler);

		// Create C compiler. We ignore all the toolchain that does not define a
		// C compiler.
		BuildrootTool cCompiler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER);
		addChildren(cCompiler);

		// Create C Linker
		BuildrootTool cLinker = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_LINKER);
		addChildren(cLinker);

		// Creat C++ compiler if necessary
		if (BuildrootUtils.isCompilerAvailable(path, prefix, "g++")) {
			BuildrootTool ccCompiler = new BuildrootTool(path, prefix,
					architecture, BuildrootToolType.CC_COMPILER);
			BuildrootTool ccLinker = new BuildrootTool(path, prefix,
					architecture, BuildrootToolType.CC_LINKER);
			addChildren(ccCompiler);
			addChildren(ccLinker);
		}
		
		// Create pkg-config
		BuildrootTool pkgConfig = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.PKG_CONFIG);
		addChildren(pkgConfig);


		// Create builder
		BuildrootBuilder builder = new BuildrootBuilder(path, architecture);
		addChildren(builder);
	}

	@Override
	public String getName() {
		return "toolChain";
	}

	@Override
	public String getAttribute(String attribute) {
		if (ARCH_LIST.equals(attribute))
			return "all";
		else if (ID.equals(attribute))
			return id;
		else if (IS_ABSTRACT.equals(attribute))
			return "false";
		else if (NAME.equals(attribute))
			return name;
		else if (OS_LIST.equals(attribute))
			return "linux";
		return null;
	}

	public String getIdentifier() {
		return id;
	}
}
