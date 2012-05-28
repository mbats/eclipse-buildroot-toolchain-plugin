package org.buildroot.cdt.toolchain.managedbuilder.toolchain;

public class BuildrootTargetPlatform extends BuildrootConfigElement {
	private static final String BINARY_PARSER = "binaryParser";
	private String id;
	private String name;

	public BuildrootTargetPlatform(String path, String architecture) {
		id = getIdentifier(path, "platform.base");
		name = getName(architecture, path, "Platform");
	}

	@Override
	public String getName() {
		return "targetPlatform";
	}

	@Override
	public String getAttribute(String attribute) {
		if (ARCH_LIST.equals(attribute))
			return "all";
		else if (BINARY_PARSER.equals(attribute))
			return "org.eclipse.cdt.core.GNU_ELF";
		else if (ID.equals(attribute))
			return id;
		else if (IS_ABSTRACT.equals(attribute))
			return "false";
		else if (NAME.equals(attribute))
			return name;
		else if (OS_LIST.equals(attribute))
			return "linux";
		return null;
	}
}
