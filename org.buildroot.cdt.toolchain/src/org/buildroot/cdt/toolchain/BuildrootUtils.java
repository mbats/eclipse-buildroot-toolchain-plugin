package org.buildroot.cdt.toolchain;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

public class BuildrootUtils {
	public static void registerExtensionPoint(StringBuffer buffer) {
		ByteArrayInputStream is = new ByteArrayInputStream(buffer.toString()
				.getBytes());
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		Object key = ((ExtensionRegistry) registry).getTemporaryUserToken();
		Bundle bundle = BuildrootActivator.getDefault().getBundle();
		IContributor contributor = ContributorFactoryOSGi
				.createContributor(bundle);
		if (!registry.addContribution(is, contributor, false, null, null, key)) {
			BuildrootActivator.getDefault().warning(
					"Contribution is not registered : " + buffer.toString(),
					null);
		}
	}

	public static boolean isCompilerAvailable(String path, String prefix,
			String compilerName) {
		File file = new File(getToolPath(prefix, path, compilerName));
		return file.exists();
	}

	public static String getToolPath(String prefix, String pathStr,
			String toolName) {
		Path path = new Path(pathStr);
		return ((Path) path.append("host/usr/bin/" + prefix + toolName))
				.toString();
	}
}
