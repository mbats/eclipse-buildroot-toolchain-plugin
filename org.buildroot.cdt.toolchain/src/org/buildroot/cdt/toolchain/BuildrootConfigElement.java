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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IEnvironmentVariableSupplierDynamicConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;

/**
 * Buildroot configuration elements for loading the managed build model objects.
 */
public class BuildrootConfigElement implements IManagedConfigElement, IEnvironmentVariableSupplierDynamicConfiguration {
	public static final String LINUX = "linux";
	public static final String CDT_MANAGEDBUILD_C_COMPILER_INPUT = "cdt.managedbuild.tool.gnu.c.compiler.input";
	public static final String CDT_MANAGEDBUILD_CPP_COMPILER_INPUT = "cdt.managedbuild.tool.gnu.cpp.compiler.input";

	public static final String CDT_MANAGEDBUILDER_COMMAND_LINE_GENERATOR = "org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator";

	public enum ManagedConfigElementAttribute {
		archList, configurationEnvironmentSupplier, osList, name, isAbstract, id, superClass, binaryParser, command, natureFilter, scannerConfigDiscoveryProfileId, isVariableCaseSensitive, category, resourceFilter, value, valueType, defaultValue, buildArtefactType, buildProperties, parent, isTest, projectEnvironmentSupplier, cleanCommand, commandLineGenerator, languageSettingsProviders;
	}

	public enum ManagedConfigElement {
		TOOL("tool"), TOOLCHAIN("toolChain"), INPUT_TYPE("inputType"), OPTION_CATEGORY(
				"optionCategory"), OPTION("option"), BUILDER("builder"), TARGET_PLATFORM(
				"targetPlatform"), PROJECT_TYPE("projectType"), CONFIGURATION(
				"configuration");

		private String name;

		private ManagedConfigElement(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private List<IManagedConfigElement> children = new ArrayList<IManagedConfigElement>();
	private String name;
	private Map<String, String> attributes = new HashMap<String, String>();

	public BuildrootConfigElement(ManagedConfigElement name) {
		this.name = name.getName();
	}

	public String getIdentifier(String path, String suffix) {
		path = path.replaceAll("/", ".");
		if (path.endsWith("."))
			path = path.substring(0, path.length() - 1);
		if (path.startsWith("."))
			path = path.substring(1, path.length());

		return "org.buildroot." + path + "." + suffix;
	}

	@Override
	public IManagedConfigElement[] getChildren() {
		return (IManagedConfigElement[]) children
				.toArray(new IManagedConfigElement[children.size()]);
	}

	@Override
	public IManagedConfigElement[] getChildren(String elementName) {
		List<IManagedConfigElement> filteredChildren = new ArrayList<IManagedConfigElement>();
		for (IManagedConfigElement element : children) {
			if (element.getName().equals(elementName))
				filteredChildren.add(element);
		}
		return (IManagedConfigElement[]) filteredChildren
				.toArray(new IManagedConfigElement[filteredChildren.size()]);
	}

	public void addChild(IManagedConfigElement element) {
		children.add(element);
	}

	public void addChildren(List<IManagedConfigElement> element) {
		children.addAll(element);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAttribute(String attribute) {
		return attributes.get(attribute);
	}

	public void setAttribute(ManagedConfigElementAttribute name, String value) {
		attributes.put(name.name(), value);
	}

	@Override
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier() {
		return new BuildrootEnvironmentVariableSupplier();
	}
}
