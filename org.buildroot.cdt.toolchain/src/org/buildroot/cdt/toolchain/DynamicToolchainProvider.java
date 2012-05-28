package org.buildroot.cdt.toolchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.buildroot.cdt.toolchain.managedbuilder.projecttype.BuildrootProjectType;
import org.buildroot.cdt.toolchain.managedbuilder.toolchain.BuildrootToolchain;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElementProvider;

public class DynamicToolchainProvider implements IManagedConfigElementProvider {

	@Override
	public IManagedConfigElement[] getConfigElements() {
		String buildrootConfigFilePath = System.getProperty("user.home")
				+ File.separator + ".buildroot-eclipse.toolchains";

		List<IManagedConfigElement> configElements = parseBuildrootConfiguration(buildrootConfigFilePath);

		return (IManagedConfigElement[]) configElements
				.toArray(new IManagedConfigElement[configElements.size()]);
	}

	private List<IManagedConfigElement> parseBuildrootConfiguration(
			String buildrootConfigFilePath) {
		File file = new File(buildrootConfigFilePath);
		Scanner input;
		List<IManagedConfigElement> configElements = new ArrayList<IManagedConfigElement>();
		try {
			input = new Scanner(file);

			while (input.hasNext()) {
				String nextLine = input.nextLine();
				String[] config = nextLine.split(":");
				String path = config[0];
				String prefix = config[1];
				String architecture = config[2].toUpperCase();
				if (!BuildrootUtils.isCompilerAvailable(path, prefix, "gcc"))
					continue;
				// Create toolchain
				BuildrootToolchain toolchain = new BuildrootToolchain(path,
						prefix, architecture);
				configElements.add(toolchain);

				// Create projectType
				BuildrootProjectType projectType = new BuildrootProjectType(
						path, toolchain);
				configElements.add(projectType);
			}

			input.close();
		} catch (FileNotFoundException e) {
			BuildrootActivator.getDefault().error(
					"Buildroot configuration file doe not exist : "
							+ buildrootConfigFilePath, e);
		}
		return configElements;
	}
}
