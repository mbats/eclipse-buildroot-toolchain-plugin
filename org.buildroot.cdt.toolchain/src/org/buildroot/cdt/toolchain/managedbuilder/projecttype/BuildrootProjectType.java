package org.buildroot.cdt.toolchain.managedbuilder.projecttype;

import org.buildroot.cdt.toolchain.managedbuilder.projecttype.BuildrootConfiguration.ConfigurationType;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootConfigElement;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;

public class BuildrootProjectType extends BuildrootConfigElement {
	private String id;

	public BuildrootProjectType(String path, BuildrootToolchain toolchain) {
		id = getIdentifier(path, ".elf.exe");

		BuildrootConfiguration debugConfig = new BuildrootConfiguration(path,
				ConfigurationType.DEBUG, toolchain);
		addChildren(debugConfig);
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
