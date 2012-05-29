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

/**
 * For each toolchain the followed builder is generated dynamically :
 * <builder
 *	command="make"
 * 	id="org.buildroot.cdt.toolchain.arm.linux.gnueabi.builder"
 * 	isAbstract="false"
 * 	isVariableCaseSensitive="false"
 * 	name="Buildroot ARM GNU Make builder"
 * 	superClass="cdt.managedbuild.target.gnu.builder">
 * </builder>
*/
public class BuildrootBuilder extends BuildrootConfigElement {
	/**
	 * Case sensitive variable constant.
	 */
	private static final String IS_VARIABLE_CASE_SENSITIVE = "isVariableCaseSensitive";
	/**
	 * Command constant.
	 */
	private static final String COMMAND = "command";
	private String id;
	private String name;

	/**
	 * Buildroot builder constructor.
	 * 
	 * @param path
	 *            Toolchain path
	 * @param architecture
	 *            Toolchain architecture
	 */
	public BuildrootBuilder(String path, String architecture) {
		this.id = getIdentifier(path, "builder");
		this.name = getName(architecture, path, "builder");
	}

	@Override
	public String getName() {
		return "builder";
	}

	@Override
	public String getAttribute(String attribute) {
		if (COMMAND.equals(attribute))
			return "make";
		else if (ID.equals(attribute))
			return id;
		else if (IS_ABSTRACT.equals(attribute))
			return "false";
		else if (NAME.equals(attribute))
			return name;
		else if (IS_VARIABLE_CASE_SENSITIVE.equals(attribute))
			return "false";
		else if (SUPER_CLASS.equals(attribute))
			return "cdt.managedbuild.target.gnu.builder";
		return null;
	}
}
