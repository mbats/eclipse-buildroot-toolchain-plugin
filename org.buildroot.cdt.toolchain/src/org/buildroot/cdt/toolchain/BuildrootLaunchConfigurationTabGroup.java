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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.launch.remote.tabs.RemoteCDebuggerTab;
import org.eclipse.cdt.launch.remote.tabs.RemoteCMainTab;
import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Launch configuration tab group.
 */
public class BuildrootLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	/**
	 * Create the tabs in the launch configuration dialog.
	 * 
	 * @param dialog
	 *            the launch configuration dialog
	 * @param mode
	 *            the launch configuration mode
	 */
	@SuppressWarnings("restriction")
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		if ("run".equals(mode)) {
			ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
					new RemoteCMainTab(), new CArgumentsTab(), new CommonTab() };
			setTabs(tabs);
		} else if ("debug".equals(mode)) {
			ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
					new RemoteCMainTab(), new CArgumentsTab(),
					new RemoteCDebuggerTab(), new CommonTab() };
			setTabs(tabs);
		}
	}

	/**
	 * setDefaults is called when the user creates a new launch configuration.
	 * This function sets up the default settings for the tabs in the debugger
	 * plugin, based on the launch configuration type selected by the user.
	 * 
	 * @param configuration
	 *            the launch configuration
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);

		try {
			String debugName = BuildrootActivator.getDebugName(configuration
					.getType().getName());
			configuration.setAttribute(
					IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, debugName);

			String solibPath = BuildrootActivator.getSolibPath(configuration
					.getType().getName());
			List<String> solibPaths = new ArrayList<String>();
			solibPaths.add(solibPath);
			configuration.setAttribute(
					IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH,
					solibPaths);
		} catch (CoreException e) {
			BuildrootActivator.getDefault().error(
					"Launch configuration is not valid", e);
		}
	}
}
