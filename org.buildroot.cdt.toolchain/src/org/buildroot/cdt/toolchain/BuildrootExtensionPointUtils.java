package org.buildroot.cdt.toolchain;

import java.io.ByteArrayInputStream;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

public class BuildrootExtensionPointUtils {
	public static void registerExtensionPoint(StringBuffer buffer) {
		ByteArrayInputStream is = new ByteArrayInputStream(buffer.toString()
				.getBytes());
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		Object key = ((ExtensionRegistry) registry).getTemporaryUserToken();
		Bundle bundle = Activator.getDefault().getBundle();
		IContributor contributor = ContributorFactoryOSGi
				.createContributor(bundle);
		if (!registry.addContribution(is, contributor, false, null, null, key)) {
			// TODO Log an error or something
		}
	}
}
