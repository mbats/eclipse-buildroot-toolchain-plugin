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

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

/**
 * Utility to manage extension point and buildroot configuration properties.
 * 
 * @author Melanie Bats <melanie.bats@obeo.fr>
 */
public class BuildrootUtils {
	/**
	 * Register dynamically an extension point.
	 * 
	 * @param buffer
	 *            StringBuffer defining the extension point content
	 */
	public static void registerExtensionPoint(StringBuffer buffer) {
		ByteArrayInputStream is = new ByteArrayInputStream(buffer.toString()
				.getBytes());
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		Object key = ((ExtensionRegistry) registry).getTemporaryUserToken();
		Bundle bundle = BuildrootActivator.getDefault().getBundle();
		IContributor contributor = ContributorFactoryOSGi
				.createContributor(bundle);
		if (!registry.addContribution(is, contributor, false, null, null, key)) {
			BuildrootActivator.getDefault().warning(
					"Contribution is not registered : " + buffer.toString(),
					null);
		}
	}

	/**
	 * Check if a compiler exist for the toolchain.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param prefix
	 *            Toolchain prefix
	 * @param compilerName
	 *            Compiler name
	 * @return True if compiler exists in toolchain otherwise false
	 */
	public static boolean isCompilerAvailable(String path, String prefix,
			String compilerName) {
		File file = new File(getToolPath(prefix, path, compilerName));
		return file.exists();
	}

	/**
	 * Get path of a buildroot tool.
	 * 
	 * @param prefix
	 *            Toolchain prefix
	 * @param pathStr
	 *            Toolchain path
	 * @param toolName
	 *            Tool name
	 * @return Path of tool
	 */
	public static String getToolPath(String prefix, String pathStr,
			String toolName) {
		Path path = new Path(pathStr);
		return ((Path) path.append("host/usr/bin/" + prefix + toolName))
				.toString();
	}
}
