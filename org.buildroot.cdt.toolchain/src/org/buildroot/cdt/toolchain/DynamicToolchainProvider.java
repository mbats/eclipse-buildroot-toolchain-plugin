package org.buildroot.cdt.toolchain;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElementProvider;

public class DynamicToolchainProvider implements IManagedConfigElementProvider {

	@Override
	public IManagedConfigElement[] getConfigElements() {
		String path = "/home/melanie/buildroot/output";
		String prefix = "arm-none-linux-gnueabi";
		String architecture = "ARM";
		BuildrootToolchain toolchain = new BuildrootToolchain(path, prefix,
				architecture);

		List<IManagedConfigElement> configElements = new ArrayList<IManagedConfigElement>();
		configElements.add(toolchain);

		return (IManagedConfigElement[]) configElements
				.toArray(new IManagedConfigElement[configElements.size()]);
	}
}
