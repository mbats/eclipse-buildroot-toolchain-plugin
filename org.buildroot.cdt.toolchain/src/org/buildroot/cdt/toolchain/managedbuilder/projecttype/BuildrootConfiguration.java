/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Melanie Bats <melanie.bats@obeo.fr> - Initial contribution
 *******************************************************************************/
package org.buildroot.cdt.toolchain.managedbuilder.projecttype;

import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

/**
 * For each toolchain the followed configuration is generated dynamically :
 * <configuration buildProperties=
 * "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.debug"
 * cleanCommand="rm -rf"
 * id="com.analog.gnu.toolchain.blackfin.config.bfin.elf.exe.debug"
 * name="%ConfigName.Dbg" parent="cdt.managedbuild.config.gnu.base"> <toolChain
 * id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.debug"
 * superClass="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
 * </toolChain> </configuration>
 * 
 * @author Melanie Bats <melanie.bats@obeo.fr>
 */
public class BuildrootConfiguration extends BuildrootConfigElement {

	/**
	 * Two types of configuration exist : debug or release.
	 */
	public enum ConfigurationType {
		DEBUG, RELEASE
	}

	/**
	 * Configuration identifier.
	 */
	private String id;

	/**
	 * Configuration name.
	 */
	private String name;

	/**
	 * Configuration type.
	 */
	private ConfigurationType configurationType;

	/**
	 * Buildroot configuration constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param configType
	 *            Configuration type
	 * @param toolchain
	 *            Referenced toolchain
	 */
	public BuildrootConfiguration(String path, ConfigurationType configType,
			BuildrootToolchain toolchain) {
		id = getIdentifier(path, ".elf.exe."
				+ configType.toString().toLowerCase());
		name = configType.toString().toLowerCase();
		configurationType = configType;
		BuildrootToolchainRef toolchainRef = new BuildrootToolchainRef(path,
				configType.toString().toLowerCase(), toolchain);
		addChildren(toolchainRef);
	}

	@Override
	public String getName() {
		return "configuration";
	}

	@Override
	public String getAttribute(String attribute) {
		if ("buildProperties".equals(attribute)) {
			return "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType."
					+ configurationType.name().toLowerCase();
		} else if ("cleanCommand".equals(attribute)) {
			return "rm -rf";
		} else if (ID.equals(attribute)) {
			return id;
		} else if (NAME.equals(attribute)) {
			return name;
		} else if ("parent".equals(attribute)) {
			return "cdt.managedbuild.config.gnu.base";
		}
		return null;
	}
}
