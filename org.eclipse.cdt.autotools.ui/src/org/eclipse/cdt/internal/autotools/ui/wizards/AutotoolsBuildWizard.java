/*******************************************************************************
 * Copyright (c) 2007, 2008, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Red Hat Inc - modification for Autotools project
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ProjectType;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;

/**
 *
 */
@SuppressWarnings("restriction")
public class AutotoolsBuildWizard extends AbstractCWizard {
	public static final String OTHERS_LABEL = AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.1"); //$NON-NLS-1$
	public static final String AUTOTOOLS_PROJECTTYPE_ID = "org.eclipse.linuxtools.cdt.autotools.core.projectType"; //$NON-NLS-1$
	
	/**
	 * @since 5.1
	 */
	public static final String EMPTY_PROJECT = AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.2"); //$NON-NLS-1$
	public static final String AUTOTOOLS_TOOLCHAIN_ID = "org.eclipse.linuxtools.cdt.autotools.core.toolChain"; //$NON-NLS-1$
	
	/**
	 * Get all the project types that extends the autotools project type.
	 */
	private Map<IProjectType, AutotoolsBuildWizardHandler> autotoolsProjectTypes = new HashMap<IProjectType, AutotoolsBuildWizardHandler>();
	/**
	 * The autotools build wizard handler.
	 */
	private AutotoolsBuildWizardHandler autotoolsBuildWizardHandler;
	/**
	 * The autotools project type.
	 */
	private IProjectType autotoolsProjectType;
	 
	/**
	 * Creates and returns an array of items to be displayed 
	 */
	@SuppressWarnings("restriction")
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());
		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();

		// look for project types that have a toolchain based on the Autotools toolchain
		// and if so, add an entry for the project type.
		// Fix for bug#374026
		EntryDescriptor oldsRoot = null;
		SortedMap<String, IProjectType> sm = ManagedBuildManager.getExtensionProjectTypeMap();
		for (Map.Entry<String, IProjectType> e : sm.entrySet()) {
			IProjectType pt = e.getValue();
			AutotoolsBuildWizardHandler h = new AutotoolsBuildWizardHandler(pt, parent, wizard);
			IToolChain[] tcs = ManagedBuildManager.getExtensionToolChains(pt);
			for(int i = 0; i < tcs.length; i++){
				IToolChain t = tcs[i];

				IToolChain parent = t;
				while (parent.getSuperClass() != null) {
					parent = parent.getSuperClass();
				}

				if (!parent.getId().equals(AUTOTOOLS_TOOLCHAIN_ID))
					continue;

				if(t.isSystemObject()) 
					continue;
				if (!isValid(t, supportedOnly, wizard))
					continue;

				h.addTc(t);
			}

			// Get the autotools project type and build wizard handler
			if (AUTOTOOLS_PROJECTTYPE_ID.equals(pt.getId())) {
				autotoolsBuildWizardHandler = h;
				autotoolsProjectType = pt;
			} else {
				// Keep the other autotools toolchains in order to register them
				// into the default autotools project type later
				autotoolsProjectTypes.put(pt, h);
			}
		}
		
		// Register all the autotools toolchains in the default autotools project type
		for (IProjectType projectType : autotoolsProjectTypes.keySet()) {
			AutotoolsBuildWizardHandler h = autotoolsProjectTypes
					.get(projectType);
			for (IToolChain tc : h.getToolChains().values()) {
				IConfiguration tcConfig = null;
				for (IConfiguration config : projectType.getConfigurations()) {
					if (tc.equals(config.getToolChain()))
						tcConfig = config;
				}
				// Add the new configurations
				if (autotoolsProjectType instanceof ProjectType
						&& tcConfig instanceof Configuration)
					((ProjectType) autotoolsProjectType)
							.addConfiguration((Configuration) tcConfig);
				// Add the new toolchain
				autotoolsBuildWizardHandler.addTc(tc);
			}
		}
		
		String pId = null;
		if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
			if (oldsRoot == null) {
				oldsRoot = new EntryDescriptor(OTHERS_LABEL, null,
						OTHERS_LABEL, true, null, null);
				items.add(oldsRoot);
			}
			pId = oldsRoot.getId();
		} else { // do not group to <Others>
			pId = null;
		}

		// Add the autotools project type
		items.add(new EntryDescriptor(autotoolsProjectType.getId(), pId,
				autotoolsProjectType.getName(), true,
				autotoolsBuildWizardHandler, null));

		return (EntryDescriptor[])items.toArray(new EntryDescriptor[items.size()]);
	}
}
