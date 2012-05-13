package org.buildroot.cdt.toolchain;

import org.buildroot.cdt.toolchain.BuildrootTool.BuildrootToolType;

public class BuildrootToolchain extends BuildrootConfigElement {

	private String id;
	private String name;

	public BuildrootToolchain(String path, String prefix, String architecture) {
		id = getIdentifier(path, ".toolchain.base");
		name = getName(architecture, path, null);

		BuildrootTargetPlatform targetPlatform = new BuildrootTargetPlatform(
				path, architecture);
		BuildrootTool assembler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.ASSEMBLER);
		BuildrootTool cCompiler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER);

		BuildrootTool cLinker = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_LINKER);
		BuildrootTool ccCompiler = new BuildrootTool(path, prefix,
				architecture, BuildrootToolType.CC_COMPILER);

		BuildrootTool ccLinker = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.CC_LINKER);
		BuildrootBuilder builder = new BuildrootBuilder(path, architecture);

		addChildren(targetPlatform);
		addChildren(assembler);
		addChildren(cCompiler);
		addChildren(cLinker);
		addChildren(ccCompiler);
		addChildren(ccLinker);
		addChildren(builder);
	}

	@Override
	public String getName() {
		return "toolChain";
	}

	@Override
	public String getAttribute(String attribute) {
		if (ARCH_LIST.equals(attribute))
			return "all";
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
