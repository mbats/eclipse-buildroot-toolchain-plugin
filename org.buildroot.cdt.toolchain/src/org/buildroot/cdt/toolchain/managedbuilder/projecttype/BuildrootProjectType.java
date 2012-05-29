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

import org.buildroot.cdt.toolchain.managedbuilder.projecttype.BuildrootConfiguration.ConfigurationType;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

/**
 * For each toolchain the followed project type is generated dynamically :
 * <projectType
 *       buildArtefactType="org.eclipse.cdt.build.core.buildArtefactType.exe"
 *       id="com.analog.gnu.toolchain.blackfin.target.bfin.elf.exe"
 *       isAbstract="false"
 *       isTest="false">
 *    <configuration
 *          buildProperties="org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.debug"
 *          cleanCommand="rm -rf"
 *          id="com.analog.gnu.toolchain.blackfin.config.bfin.elf.exe.debug"
 *          name="%ConfigName.Dbg"
 *          parent="cdt.managedbuild.config.gnu.base">
 *       <toolChain
 *             id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.debug"
 *             superClass="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
 *       </toolChain>
 *    </configuration>
 *    <configuration
 *          buildProperties="org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.release"
 *          cleanCommand="rm -rf"
 *          id="com.analog.gnu.toolchain.blackfin.config.bfin.elf.exe.release"
 *          name="%ConfigName.Rel"
 *          parent="cdt.managedbuild.config.gnu.base">
 *       <toolChain
 *             id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.release"
 *             superClass="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
 *       </toolChain>
 *    </configuration>
 * </projectType>
 */
public class BuildrootProjectType extends BuildrootConfigElement {
	/**
	 * Project type identifier.
	 */
	private String id;

	/**
	 * Buildroot project type constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param toolchain
	 *            Referenced toolchain
	 */
	public BuildrootProjectType(String path, BuildrootToolchain toolchain) {
		// Get project type identifier
		id = getIdentifier(path, ".elf.exe");

		// Create debug configuration
		BuildrootConfiguration debugConfig = new BuildrootConfiguration(path,
				ConfigurationType.DEBUG, toolchain);
		addChildren(debugConfig);
		
		// Create release configuration
		BuildrootConfiguration releaseConfig = new BuildrootConfiguration(path,
				ConfigurationType.RELEASE, toolchain);
		addChildren(releaseConfig);
	}

	@Override
	public String getName() {
		return "projectType";
	}

	@Override
	public String getAttribute(String attribute) {
		if ("buildArtefactType".equals(attribute)) {
			return "org.eclipse.cdt.build.core.buildArtefactType.exe";
		} else if (ID.equals(attribute)) {
			return id;
		} else if (IS_ABSTRACT.equals(attribute)) {
			return "false";
		} else if ("isTest".equals(attribute)) {
			return "false";
		}
		return null;
	}
}
