package org.buildroot.cdt.toolchain.managedbuilder.toolchain;

public class BuildrootBuilder extends BuildrootConfigElement {

	private static final String IS_VARIABLE_CASE_SENSITIVE = "isVariableCaseSensitive";
	private static final String COMMAND = "command";
	private String id;
	private String name;

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
