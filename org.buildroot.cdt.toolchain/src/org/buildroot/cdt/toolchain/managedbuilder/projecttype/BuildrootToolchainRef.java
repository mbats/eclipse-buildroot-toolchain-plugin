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

import org.buildroot.cdt.toolchain.DynamicToolchainProvider.BuildArtefactType;
import org.buildroot.cdt.toolchain.managedbuilder.projecttype.BuildrootConfiguration.ConfigurationType;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

/**
 * For each toolchain the followed toolchain reference is generated dynamically
 * : <toolChain
 * id="com.analog.gnu.toolchain.blackfin.toolchain.bfin.elf.exe.debug"
 * superClass="org.buildroot.cdt.toolchain.arm.linux.gnueabi.toolchain.base">
 * </toolChain>
 * 
 * @author Melanie Bats <melanie.bats@obeo.fr>
 */
public class BuildrootToolchainRef extends BuildrootConfigElement {
	/**
	 * Toolchain reference identifier.
	 */
	private String id;
	/**
	 * Referenced superclass.
	 */
	private String superClass;

	/**
	 * Buildroot toolchain reference constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param configType
	 *            Configuration type
	 * @param toolchain
	 *            Referenced toolchain
	 * @param buildArtefactType
	 *            Build artefact type
	 */
	public BuildrootToolchainRef(final String path,
			ConfigurationType configType, BuildrootToolchain toolchain,
			BuildArtefactType buildArtefactType) {
		id = getIdentifier(path, buildArtefactType.getValue() + "."
				+ configType.name().toLowerCase());
		superClass = toolchain.getIdentifier();
	}

	@Override
	public String getName() {
		return "toolChain";
	}

	@Override
	public String getAttribute(String attribute) {
		if (ID.equals(attribute)) {
			return id;
		} else if (SUPER_CLASS.equals(attribute)) {
			return superClass;
		}
		return null;
	}
}
