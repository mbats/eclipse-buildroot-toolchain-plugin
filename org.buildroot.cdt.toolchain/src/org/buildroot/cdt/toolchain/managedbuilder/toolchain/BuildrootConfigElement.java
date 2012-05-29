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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;

/**
 * Buildroot configuration elements for loading the managed build model objects.
 */
public abstract class BuildrootConfigElement implements IManagedConfigElement {
	protected static final String OS_LIST = "osList";
	protected static final String NAME = "name";
	protected static final String IS_ABSTRACT = "isAbstract";
	protected static final String ID = "id";
	protected static final String ARCH_LIST = "archList";
	protected static final String SUPER_CLASS = "superClass";

	private List<IManagedConfigElement> children = new ArrayList<IManagedConfigElement>();

	public String getIdentifier(String path, String suffix) {
		path = path.replaceAll("/", ".");
		if (path.endsWith("."))
			path = path.substring(0, path.length() - 1);
		if (path.startsWith("."))
			path = path.substring(1, path.length());

		return "org.buildroot." + path + "." + suffix;
	}

	public String getName(String architecture, String path,
			String toolDescription) {
		if (toolDescription != null)
			return "Buildroot " + architecture + " " + toolDescription + " ("
					+ path + ")";
		return "Buildroot " + architecture + " (" + path + ")";
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

	public void addChildren(IManagedConfigElement element) {
		children.add(element);
	}
}
