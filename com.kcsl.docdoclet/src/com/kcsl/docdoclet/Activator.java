package com.kcsl.docdoclet;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.ensoftcorp.atlas.core.log.Log;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.kcsl.docdoclet"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static Activator getDefault() {
		return plugin;
	}
	
	public static String getDocletOutputDirectoryPath() {
		Bundle pluginBundle = getDefault().getBundle();
		URL bundleEntry = pluginBundle.getEntry("bin");
		try {
			return FileLocator.toFileURL(bundleEntry).getPath();
		} catch (IOException e) {
			Log.error("Error in locating the output directory for the plugin: " + PLUGIN_ID, e);
			return null;
		}
	}
	
	public static String getDocletClassQualifiedName() {
		return JSONDoclet.class.getName();
	}

}
