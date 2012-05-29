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


public class BuildrootTool extends BuildrootConfigElement {
	private static final String SUPER_CLASS = "superClass";
	private static final String NATURE_FILTER = "natureFilter";
	private static final String COMMAND_LINE_GENERATOR = "commandLineGenerator";
	private static final String COMMAND = "command";
	private String id;
	private String name;
	private String command;
	private String natureFilter;
	private String superClass;

	/**
	 * It exists 5 types of tool : assembler, compilers for C and C++, linkers
	 * for C and C++.
	 * */
	public enum BuildrootToolType {
		ASSEMBLER, C_COMPILER, CC_COMPILER, C_LINKER, CC_LINKER
	}

	/**
	 * Buildroot tool constructor.
	 * @param path Toolchain path
	 * @param prefix Tool path prefix
	 * @param architecture Toolchain architecture
	 * @param toolType Tool type
	 */
	public BuildrootTool(String path, String prefix, String architecture,
			BuildrootToolType toolType) {
		String toolName = null;
		String idSuffix = null;
		String toolDescription = null;
		switch (toolType) {
		case ASSEMBLER:
			this.superClass = "cdt.managedbuild.tool.gnu.assembler";
			this.natureFilter = "both";
			toolName = "as";
			idSuffix = "assembler";
			toolDescription = "Assembler";
			break;

		case C_COMPILER:
			this.superClass = "cdt.managedbuild.tool.gnu.c.compiler";
			this.natureFilter = "both";
			toolName = "gcc";
			idSuffix = "c.compiler";
			toolDescription = "C Compiler";
			break;

		case CC_COMPILER:
			this.superClass = "cdt.managedbuild.tool.gnu.cpp.compiler";
			this.natureFilter = "ccnature";
			toolName = "g++";
			idSuffix = "cc.compiler";
			toolDescription = "C++ Compiler";
			break;

		case C_LINKER:
			this.superClass = "cdt.managedbuild.tool.gnu.c.linker";
			this.natureFilter = "cnature";
			toolName = "gcc";
			idSuffix = "c.linker";
			toolDescription = "C Linker";
			break;

		case CC_LINKER:
			this.superClass = "cdt.managedbuild.tool.gnu.cpp.linker";
			this.natureFilter = "ccnature";
			toolName = "g++";
			idSuffix = "cc.linker";
			toolDescription = "CC Linker";
			break;

		default:
			break;
		}
		String toolPath = BuildrootUtils.getToolPath(prefix, path, toolName);
		this.command = toolPath;
		this.id = getIdentifier(path, idSuffix);
		this.name = getName(architecture, path, toolDescription);

		if (toolType == BuildrootToolType.C_COMPILER
				|| toolType == BuildrootToolType.CC_COMPILER) {
			BuildrootInputType input = new BuildrootInputType(path,
					architecture, toolType, command);
			addChildren(input);
		}

	}

	@Override
	public String getName() {
		return "tool";
	}

	@Override
	public String getAttribute(String attribute) {
		if (COMMAND.equals(attribute))
			return command;
		else if (COMMAND_LINE_GENERATOR.equals(attribute))
			return "org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator";
		else if (ID.equals(attribute))
			return id;
		else if (IS_ABSTRACT.equals(attribute))
			return "false";
		else if (NAME.equals(attribute))
			return name;
		else if (NATURE_FILTER.equals(attribute))
			return natureFilter;
		else if (SUPER_CLASS.equals(attribute))
			return superClass;
		return null;
	}
}
