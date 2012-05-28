package org.buildroot.cdt.toolchain.managedbuilder.projecttype;

import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

public class BuildrootToolchainRef extends BuildrootConfigElement {
	private String id;
	private String superClass;

	public BuildrootToolchainRef(final String path, String suffix,
			BuildrootToolchain toolchain) {
		id = getIdentifier(path, ".elf.exe." + suffix);
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
