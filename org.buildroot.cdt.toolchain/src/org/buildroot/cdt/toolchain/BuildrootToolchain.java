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
		addChildren(targetPlatform);

		BuildrootTool assembler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.ASSEMBLER);
		addChildren(assembler);

		BuildrootTool cCompiler = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_COMPILER);
		addChildren(cCompiler);

		BuildrootTool cLinker = new BuildrootTool(path, prefix, architecture,
				BuildrootToolType.C_LINKER);
		addChildren(cLinker);

		if (BuildrootUtils.isCompilerAvailable(path, prefix, "g++")) {
			BuildrootTool ccCompiler = new BuildrootTool(path, prefix,
					architecture, BuildrootToolType.CC_COMPILER);
			BuildrootTool ccLinker = new BuildrootTool(path, prefix,
					architecture, BuildrootToolType.CC_LINKER);
			addChildren(ccCompiler);
			addChildren(ccLinker);
		}

		BuildrootBuilder builder = new BuildrootBuilder(path, architecture);
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
