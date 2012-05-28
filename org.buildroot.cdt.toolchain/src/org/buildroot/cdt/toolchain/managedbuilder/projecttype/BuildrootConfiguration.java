package org.buildroot.cdt.toolchain.managedbuilder.projecttype;

import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

public class BuildrootConfiguration extends BuildrootConfigElement {

	public enum ConfigurationType {
		DEBUG, RELEASE
	}

	private String id;
	private String name;

	public BuildrootConfiguration(String path, ConfigurationType configType,
			BuildrootToolchain toolchain) {
		id = getIdentifier(path, ".elf.exe."
				+ configType.toString().toLowerCase());
		name = configType.toString().toLowerCase();
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
			return "org.eclipse.cdt.build.core.buildType=org.eclipse.cdt.build.core.buildType.debug";
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
