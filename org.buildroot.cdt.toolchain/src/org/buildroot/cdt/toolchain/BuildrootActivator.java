package org.buildroot.cdt.toolchain;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class BuildrootActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.buildroot.cdt.toolchain"; //$NON-NLS-1$

	// The shared instance
	private static BuildrootActivator plugin;

	/**
	 * The log of the plug-in.
	 */
	private static ILog logger;

	/**
	 * The constructor
	 */
	public BuildrootActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static BuildrootActivator getDefault() {
		return plugin;
	}

	public static ILog getLogger() {
		return logger;
	}

	/**
	 * Logs an error in the error log.
	 * 
	 * @param message
	 *            the message to log (optional)
	 * @param e
	 *            the exception (optional)
	 */
	public void error(String message, final Throwable e) {
		String error = message;
		if (message == null && e != null) {
			error = e.getMessage();
		}
		if (e instanceof CoreException) {
			this.getLog().log(((CoreException) e).getStatus());
		} else {
			final IStatus status = new Status(IStatus.ERROR, this.getBundle()
					.getSymbolicName(), error, e);
			this.getLog().log(status);
		}
	}

	/**
	 * Logs a warning in the error log.
	 * 
	 * @param message
	 *            the message to log (optional)
	 * @param e
	 *            the exception (optional)
	 */
	public void warning(String message, final Exception e) {
		String warning = message;
		if (message == null && e != null) {
			warning = e.getMessage();
		}
		if (e instanceof CoreException) {
			this.getLog().log(((CoreException) e).getStatus());
		} else {
			final IStatus status = new Status(IStatus.WARNING, this.getBundle()
					.getSymbolicName(), warning, e);
			this.getLog().log(status);
		}
	}
}
