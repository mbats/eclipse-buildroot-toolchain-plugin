/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Melanie Bats <melanie.bats@obeo.fr> - Initial contribution
 *******************************************************************************/
package org.buildroot.cdt.toolchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class BuildrootDebuggerConfig {
	private String gdbInitPath;

	public String getGdbInitPath() {
		return gdbInitPath;
	}

	public String getDebugName() {
		return debugName;
	}

	private String debugName;

	public BuildrootDebuggerConfig(String prefix, String path) {
		debugName = BuildrootUtils.getPrefixedToolPath(prefix, path, "gdb");
		String buildrootDirPath = path + "/staging/usr/share/buildroot/";
		gdbInitPath = buildrootDirPath + "gdbinit";

		File buildrootDir = new File(buildrootDirPath);
		buildrootDir.mkdirs();
		File gdbInitFile = new File(gdbInitPath);
		if (!gdbInitFile.exists()) {
			PrintWriter writer;
			try {
				writer = new PrintWriter(gdbInitFile, "UTF-8");
				writer.println("set sysroot " + path + "/staging");
				writer.close();
			} catch (FileNotFoundException e) {
				BuildrootActivator.getDefault().error(
						"gdbinit file can not be created", e);
			} catch (UnsupportedEncodingException e) {
				BuildrootActivator.getDefault().error(
						"gdbinit file can not be created", e);
			}
		}
	}
}
