package com.kcsl.dynadoc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;

public class Configurations {

	public static final String OUTPUT_GRAPHS_DIRECTORY_NAME = "graphs";
	
	public static final String OUTPUT_RESOURCES_DIRECTORY_NAME = "resources";
	
	public static final String PLUGIN_SCRIPTS_DIRECTORY_PATH = "./templates/scripts/";
	
	private static final String PLUGIN_RESOURCES_DIRECTORY_PATH = "./templates/resources/";
	
	private static final String [] RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED = { "check.svg", "details_close.png", "details_open.png", "styles.css"};
	
	public static void createProperOutputDirectoriesStructure(Path outputDirectoryPath) {
		// Main Output Directory/
		// ... graphs/
		// ... resources/
		File mainOutputDirectory = outputDirectoryPath.toFile();
		if(mainOutputDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(mainOutputDirectory);
			} catch (IOException e) {
				System.err.println("Error while cleaning the main output directory: " + mainOutputDirectory.getAbsolutePath());
			}
		}else {
			mainOutputDirectory.mkdirs();
		}
		File graphsDirectoryFile = outputDirectoryPath.resolve(OUTPUT_GRAPHS_DIRECTORY_NAME).toFile();
		graphsDirectoryFile.mkdirs();
		
		File resourcesDirectoryFile = outputDirectoryPath.resolve(OUTPUT_RESOURCES_DIRECTORY_NAME).toFile();
		resourcesDirectoryFile.mkdirs();
		
		// Copy stuff into resources directory
		Bundle pluginBundle = Activator.getDefault().getBundle();
		for(String pluginResourceFileName: RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED) {
			try {
				InputStream pluginResourceInputStream = pluginBundle.getEntry(PLUGIN_RESOURCES_DIRECTORY_PATH + pluginResourceFileName).openStream();
				File destinationFile = new File(resourcesDirectoryFile, pluginResourceFileName);
				FileUtils.copyInputStreamToFile(pluginResourceInputStream, destinationFile);
			} catch (IOException e) {
				System.err.println("Error while copying contents of plugin resources file");
			}
		}		
	}
}
