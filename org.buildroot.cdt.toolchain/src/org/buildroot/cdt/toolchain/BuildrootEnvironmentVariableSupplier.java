package org.buildroot.cdt.toolchain;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

public class BuildrootEnvironmentVariableSupplier implements
		IConfigurationEnvironmentVariableSupplier {
	private enum ToolName {
		CC("gcc"), CXX("g++"), LD("ld");

		private String toolName;

		private ToolName(String toolName) {
			this.toolName = toolName;
		}

		public String getToolName() {
			return toolName;
		}
	}

	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (PathEnvironmentVariable.name.equals(variableName)
				&& PathEnvironmentVariable.isVar(variableName))
			return PathEnvironmentVariable.create(configuration);
		else if (ToolName.CC.name().equals(variableName))
			return ToolEnvironmentVariable.create(configuration, ToolName.CC);
		else if (ToolName.CXX.name().equals(variableName))
			return ToolEnvironmentVariable.create(configuration, ToolName.CXX);
		else if (ToolName.LD.name().equals(variableName))
			return ToolEnvironmentVariable.create(configuration, ToolName.LD);
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = PathEnvironmentVariable
				.create(configuration);
		IBuildEnvironmentVariable toolCC = ToolEnvironmentVariable.create(
				configuration, ToolName.CC);
		IBuildEnvironmentVariable toolCXX = ToolEnvironmentVariable.create(
				configuration, ToolName.CXX);
		IBuildEnvironmentVariable toolLD = ToolEnvironmentVariable.create(
				configuration, ToolName.LD);
		return path != null ? new IBuildEnvironmentVariable[] { path, toolCC,
				toolCXX, toolLD } : new IBuildEnvironmentVariable[0];
	}

	private static class PathEnvironmentVariable implements
			IBuildEnvironmentVariable {

		public static String name = "PATH";

		private File path;

		private PathEnvironmentVariable(File path) {
			this.path = path;
		}

		public static PathEnvironmentVariable create(
				IConfiguration configuration) {

			IToolChain toolchain = configuration.getToolChain();
			while (toolchain.getOptionById(toolchain.getBaseId()
					+ ".option.path") == null) {
				toolchain = toolchain.getSuperClass();
			}

			IOption option = toolchain.getOptionById(toolchain.getBaseId()
					+ ".option.path");
			String path = (String) option.getValue();
			File sysroot = new File(path);
			File bin = new File(sysroot, "bin");
			if (bin.isDirectory())
				sysroot = bin;
			return new PathEnvironmentVariable(sysroot);
		}

		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return Platform.getOS().equals(Platform.OS_WIN32) ? name
					.equalsIgnoreCase(PathEnvironmentVariable.name) : name
					.equals(PathEnvironmentVariable.name);
		}

		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":";
		}

		public String getName() {
			return name;
		}

		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}

		public String getValue() {
			return path.getAbsolutePath();
		}

	}

	private static class ToolEnvironmentVariable implements
			IBuildEnvironmentVariable {

		public String toolName;
		public String varName;
		public String prefix;

		private ToolEnvironmentVariable(String toolName, String varName,
				String prefix) {
			this.toolName = toolName;
			this.varName = varName;
			this.prefix = prefix;
		}

		public static IBuildEnvironmentVariable create(
				IConfiguration configuration, ToolName toolName) {
			IToolChain toolchain = configuration.getToolChain();
			while (toolchain.getOptionById(toolchain.getBaseId()
					+ ".option.prefix") == null) {
				toolchain = toolchain.getSuperClass();
			}

			IOption option = toolchain.getOptionById(toolchain.getBaseId()
					+ ".option.prefix");

			return new ToolEnvironmentVariable(toolName.name(),
					toolName.getToolName(), (String) option.getValue());
		}

		@Override
		public String getName() {
			return toolName;
		}

		@Override
		public String getValue() {
			return prefix + varName;
		}

		@Override
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_REPLACE;
		}

		@Override
		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":";
		}

	}

}
