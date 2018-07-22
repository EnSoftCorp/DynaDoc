package com.kcsl.dynadoc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.log.Log;
import com.kcsl.dynadoc.conf.OutputDirectoryConstants;
import com.kcsl.dynadoc.conf.PluginResourceConstants;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.utils.PathUtils;

public class Configurations {
	
	private static WorkingDirectory ROOT_WORKING_DIRECTORY = null;
	
	public static boolean configureWorkingDirectory() {
		if(!configureRootWorkingDirectory()) {
			return false;
		}
		
		if(!configureResourcesDirectory()) {
			return false;
		}
		
		if(!configureScriptsDirectory()) {
			return false;
		}
		
		if(!configureGuideDocuments()) {
			return false;
		}
		
		return true;
	}
	
	private static boolean configureRootWorkingDirectory() {
		String userPreferedRootWorkingDirectoryPath = DynaDocPreferenceInitalizer.getRootWorkingDirectoryUserPreference();
		Path userRecommendedRootWorkingDirectoryPath = PathUtils.promptUserForRootWorkingDirectory(userPreferedRootWorkingDirectoryPath);
		if(userRecommendedRootWorkingDirectoryPath == null) {
			Log.warning("User did not select an output directory to store the generated documentation. Exiting DynaDoc!");
			return false;
		}
		DynaDocPreferenceInitalizer.setUserRootWorkingDirectoryPreference(userRecommendedRootWorkingDirectoryPath.toFile().getAbsolutePath());
		ROOT_WORKING_DIRECTORY = WorkingDirectory.createRootWorkingDirectory(userRecommendedRootWorkingDirectoryPath);
		File rootWorkingDirectoryFile = ROOT_WORKING_DIRECTORY.getPath().toFile();
		if(rootWorkingDirectoryFile.exists()) {
			try {
				FileUtils.cleanDirectory(rootWorkingDirectoryFile);
			} catch (IOException e) {
				Log.error("Error while cleaning the root working directory: " + rootWorkingDirectoryFile.getAbsolutePath(), e);
				return false;
			}
		}else {
			rootWorkingDirectoryFile.mkdirs();
		}
		return true;
	}
	
	private static boolean configureResourcesDirectory() {
		Path resourcesDirectoryPath = ROOT_WORKING_DIRECTORY.getPath().resolve(OutputDirectoryConstants.RESOURCES_DIRECTORY_NAME);
		File resourcesDirectoryFile = resourcesDirectoryPath.toFile();
		resourcesDirectoryFile.mkdirs();
		
		// Copy stuff into resources directory
		Bundle bundle = Activator.getDefault().getBundle();
		for(String resourceFileName: PluginResourceConstants.Resources.RESOURCES_DIRECTORY_CONTENTS_FILE_NAMES) {
			try {
				InputStream resourceInputStream = bundle.getEntry(PluginResourceConstants.Resources.RESOURCES_DIRECTORY_PATH + resourceFileName).openStream();
				File destinationFile = new File(resourcesDirectoryFile, resourceFileName);
				FileUtils.copyInputStreamToFile(resourceInputStream, destinationFile);
			} catch (IOException e) {
				Log.error("Error while copying contents from plugin resources directory", e);
				return false;
			}
		}
		return true;
	}
	
	private static boolean configureScriptsDirectory() {
		Path scriptsDirectoryPath = ROOT_WORKING_DIRECTORY.getPath().resolve(OutputDirectoryConstants.SCRIPTS_DIRECTORY_NAME);
		File scriptsDirectoryFile = scriptsDirectoryPath.toFile();
		scriptsDirectoryFile.mkdirs();
		
		// Copy stuff into scripts directory
		Bundle bundle = Activator.getDefault().getBundle();
		for(String scriptFileName: PluginResourceConstants.Scripts.SCRIPTS_DIRECTORY_CONTENTS_FILE_NAMES) {
			try {
				InputStream scriptInputStream = bundle.getEntry(PluginResourceConstants.Scripts.SCRIPTS_DIRECTORY_PATH + scriptFileName).openStream();
				File destinationFile = new File(scriptsDirectoryFile, scriptFileName);
				FileUtils.copyInputStreamToFile(scriptInputStream, destinationFile);
			} catch (IOException e) {
				Log.error("Error while copying contents from plugin scripts directory", e);
				return false;
			}
		}
		return true;
	}
	
	private static boolean configureGuideDocuments() {
		File rootWorkingDirectoryFile = ROOT_WORKING_DIRECTORY.getPath().toFile();
		Bundle bundle = Activator.getDefault().getBundle();
		
		String guidePageFileName = PluginResourceConstants.Guide.DYANDOC_GUIDE_HTML_PAGE_NAME;
		try {
			InputStream guidePageInputStream = bundle.getEntry(PluginResourceConstants.Guide.GUIDE_DIRECTORY_PATH + guidePageFileName).openStream();
			File destinationFile = new File(rootWorkingDirectoryFile, guidePageFileName);
			FileUtils.copyInputStreamToFile(guidePageInputStream, destinationFile);
		} catch (IOException e) {
			Log.error("Error while copying the DynaDoc guide HTML page", e);
			return false;
		}
		
		File resourcesDirectoryFile = ROOT_WORKING_DIRECTORY.getPath().resolve(OutputDirectoryConstants.RESOURCES_DIRECTORY_NAME).toFile();
		for(String imageFileName: PluginResourceConstants.Guide.GUIDE_IMAGE_FILE_NAMES) {
			try {
				InputStream imageInputStream = bundle.getEntry(PluginResourceConstants.Guide.GUIDE_IMAGES_DIRECTORY_PATH + imageFileName).openStream();
				File destinationFile = new File(resourcesDirectoryFile, imageFileName);
				FileUtils.copyInputStreamToFile(imageInputStream, destinationFile);
			} catch (IOException e) {
				Log.error("Error while copying contents guide images directory", e);
				return false;
			}
		}
		return true;
	}
	
	public static boolean configureGraphsDirectory(WorkingDirectory workingDirectory) {
		Path graphsDirectoryPath = workingDirectory.getPath().resolve(OutputDirectoryConstants.GRAPHS_DIRECTORY_NAME);
		return graphsDirectoryPath.toFile().mkdirs();
	}
	
	public static WorkingDirectory rootWorkingDirectory() {
		return ROOT_WORKING_DIRECTORY;
	}

}
