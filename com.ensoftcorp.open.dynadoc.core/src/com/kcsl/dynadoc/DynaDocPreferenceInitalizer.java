package com.kcsl.dynadoc;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class DynaDocPreferenceInitalizer extends AbstractPreferenceInitializer {

	private static final String INITIAL_ROOT_WORKING_DIRECTORY_PATH = ".";
	
	private static final String ROOT_WORKING_DIRECTORY_PATH_PROPERTY_NAME = "root-working-directory-path";
	
	@Override
	public void initializeDefaultPreferences() {
	    IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	    node.put(ROOT_WORKING_DIRECTORY_PATH_PROPERTY_NAME, INITIAL_ROOT_WORKING_DIRECTORY_PATH);
	}
	
	public static void setUserRootWorkingDirectoryPreference(String userPreferedRootWorkingDirectoryPath) {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		node.put(ROOT_WORKING_DIRECTORY_PATH_PROPERTY_NAME, userPreferedRootWorkingDirectoryPath);
	}
	
	public static String getRootWorkingDirectoryUserPreference() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		if(node == null) {
			return INITIAL_ROOT_WORKING_DIRECTORY_PATH;
		}
		return node.get(ROOT_WORKING_DIRECTORY_PATH_PROPERTY_NAME, INITIAL_ROOT_WORKING_DIRECTORY_PATH);
	}

}
