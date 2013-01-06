/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Melanie Bats <melanie.bats@obeo.fr> - Initial contribution
 *******************************************************************************/
package org.buildroot.cdt.toolchain.launch;

import org.buildroot.cdt.toolchain.BuildrootUtils;

public class BuildrootDebuggerConfig {
	private String solibPath;

	public String getSolibPath() {
		return solibPath;
	}

	public String getDebugName() {
		return debugName;
	}

	private String debugName;

	public BuildrootDebuggerConfig(String prefix, String path) {
		debugName = BuildrootUtils.getPrefixedToolPath(prefix, path, "gdb");
		solibPath = path + "/staging";
	}
}
