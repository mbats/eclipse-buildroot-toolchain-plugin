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


public class BuildrootLaunchConfiguration {

	private String path;
	private String prefix;
	private String architecture;

	public BuildrootLaunchConfiguration(String path, String prefix,
			String architecture) {
		this.path = path;
		this.prefix = prefix;
		this.architecture = architecture;
	}

	public void createLaunchConfiguration() {
		StringBuffer buffer = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buffer.append("<?eclipse version=\"3.4\"?>");
		buffer.append("<plugin>");
		buffer.append(" <extension");
		buffer.append("  point=\"org.eclipse.debug.core.launchConfigurationTypes\">");
		buffer.append("  <launchConfigurationType");
		buffer.append("    delegate=\"org.eclipse.cdt.launch.remote.launching.RemoteRunLaunchDelegate\"");
		buffer.append("    id=\"" + getLaunchConfigTypeId() + "\"");
		buffer.append("    modes=\"run,debug\"");
		buffer.append("    name=\""
				+ BuildrootUtils.getToolName(architecture, path, null) + "\"");
		buffer.append("    public=\"true\"");
		buffer.append("    sourceLocatorId=\"org.eclipse.cdt.debug.core.sourceLocator\"");
		buffer.append("    sourcePathComputerId=\"org.eclipse.cdt.debug.core.sourcePathComputer\">");
		buffer.append("  </launchConfigurationType>");
		buffer.append(" </extension>");
		buffer.append(" <extension");
		buffer.append("  point=\"org.eclipse.debug.ui.launchConfigurationTabGroups\">");
		buffer.append("  <launchConfigurationTabGroup");
		buffer.append("   class=\"org.buildroot.cdt.toolchain.BuildrootLaunchConfigurationTabGroup\"");
		buffer.append("   id=\"" + getLaunchConfigTabGroupId() + "\"");
		buffer.append("   type=\"" + getLaunchConfigTypeId() + "\">");
		buffer.append("  </launchConfigurationTabGroup>");
		buffer.append(" </extension>");
		buffer.append(" <extension");
		buffer.append("  point=\"org.eclipse.debug.ui.launchConfigurationTypeImages\">");
		buffer.append("  <launchConfigurationTypeImage");
		buffer.append("   configTypeID=\"" + getLaunchConfigTypeId() + "\"");
		buffer.append("   icon=\"icons/br.png\"");
		buffer.append("   id=\"org.buildroot.cdt.toolchain.launchConfigurationTypeImage." + prefix + "." + architecture + "\">");
		buffer.append("  </launchConfigurationTypeImage>");
		buffer.append(" </extension>");
		buffer.append("</plugin>");

		// Register this extension dynamically
		BuildrootUtils.registerExtensionPoint(buffer);

		// Register the debugger configuration
		BuildrootActivator.registerDebuggerConfiguration(architecture, prefix, path);
	}

	public String getLaunchConfigTypeId() {
		return "org.buildroot.cdt.toolchain.launchConfigurationType." + prefix
				+ "." + architecture;
	}

	public String getLaunchConfigTabGroupId() {
		return getLaunchConfigTypeId() + "TabGroup";
	}
}
