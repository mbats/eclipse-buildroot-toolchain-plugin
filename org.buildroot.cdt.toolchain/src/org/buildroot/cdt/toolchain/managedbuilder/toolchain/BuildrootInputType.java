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
 * For each tool the followed inputType is generated dynamically : <inputType
 * dependencyCalculator=
 * "org.eclipse.cdt.managedbuilder.makegen.gnu.DefaultGCCDependencyCalculator2"
 * dependencyContentType="org.eclipse.cdt.core.cHeader" dependencyExtensions="h"
 * id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.assembler.input"
 * languageId="org.eclipse.cdt.core.assembly"
 * sourceContentType="org.eclipse.cdt.core.asmSource" sources="s,S,asm"
 * superClass="cdt.managedbuild.tool.gnu.assembler.input"> </inputType>
 * 
 * @author Melanie Bats <melanie.bats@obeo.fr>
 */
public class BuildrootInputType extends BuildrootConfigElement {
	private static final Object SCANNER_CONFIG_DISCOVERY_PROFILE_ID = "scannerConfigDiscoveryProfileId";
	private String id;
	private String scannerConfigProfileId;
	private String superClass;
	private String path;
	private String architecture;
	private BuildrootToolType toolType;
	private String command;

	/**
	 * Buildroot input type constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param architecture
	 *            Toolchain architecture
	 * @param toolType
	 *            Tool type
	 * @param command
	 *            Input type command
	 */
	public BuildrootInputType(String path, String architecture,
			BuildrootToolType toolType, String command) {
		this.command = command;
		this.path = path;
		this.architecture = architecture;
		this.toolType = toolType;
		switch (this.toolType) {
		case C_COMPILER:
			id = getIdentifier(path, "c.input");
			scannerConfigProfileId = getScannerConfigProfileId();
			superClass = "cdt.managedbuild.tool.gnu.c.compiler.input";
			break;

		case CC_COMPILER:
			id = getIdentifier(path, "cpp.input");
			scannerConfigProfileId = getScannerConfigProfileId();
			superClass = "cdt.managedbuild.tool.gnu.cpp.compiler.input";
			break;
		default:
			break;
		}

		// Get the scanner configuration discovery profile
		StringBuffer buffer = createScannerConfigurationDiscoveryProfile();
		// Register this extension dynamically
		BuildrootUtils.registerExtensionPoint(buffer);
	}

	public String getScannerConfigProfileId() {
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

	@Override
	public String getName() {
		return "inputType";
	}

	@Override
	public String getAttribute(String attribute) {
		if (SUPER_CLASS.equals(attribute)) {
			return superClass;
		} else if (ID.equals(attribute))
			return id;
		else if (SCANNER_CONFIG_DISCOVERY_PROFILE_ID.equals(attribute))
			return scannerConfigProfileId;
		return null;
	}

	/**
	 * Get the scanner configuration discovery profile for the current input
	 * type.
	 * 
	 * @return Scanner configuration discovery profile extension point in string
	 *         format.
	 */
	private StringBuffer createScannerConfigurationDiscoveryProfile() {
		StringBuffer buffer = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<?eclipse version=\"3.4\"?>");
		buffer.append("<plugin>");
		buffer.append("	<extension");
		buffer.append("		id=\"" + getScannerConfigProfileId() + "\"");
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
				+ getSpecFileName() + "\"");
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
	 * @return Spec file name
	 */
	private String getSpecFileName() {
		switch (toolType) {
		case C_COMPILER:
			return "specs.c";
		case CC_COMPILER:
			return "specs.cpp";
		default:
			return null;
		}
	}
}
