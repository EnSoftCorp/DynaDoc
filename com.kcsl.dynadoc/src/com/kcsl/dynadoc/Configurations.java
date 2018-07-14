package com.kcsl.dynadoc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;

public class Configurations {
	
	public static final String DOCLET_QULALIFIED_NAME = "com.kcsl.doclet.JSONDoclet";
	
	public static final String OUTPUT_GRAPHS_DIRECTORY_NAME = "graphs";
	
	public static final String OUTPUT_JAVADOC_DIRECTORY_NAME = "javadoc";
	
	public static final String OUTPUT_RESOURCES_DIRECTORY_NAME = "resources";
	
	public static final String PLUGIN_SCRIPTS_DIRECTORY_PATH = "./templates/scripts/";
	
	private static final String PLUGIN_RESOURCES_DIRECTORY_PATH = "./templates/resources/";
	
	private static final String [] RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED = { "check.svg", "details_close.png", "details_open.png", "styles.css"};
	
	private static Path DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH;
	
	private static Path OUTPUT_DIRECTORY_PATH;
	
	private static Path OUTPUT_JAVADOC_DIRECTORY_PATH;
	
	public static void createProperOutputDirectoriesStructure(String outputDirectory, String docletProjectOutputDirectory) {
		// Main Output Directory/
		// ... graphs/
		// ... resources/
		OUTPUT_DIRECTORY_PATH = Paths.get(outputDirectory);
		DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH = Paths.get(docletProjectOutputDirectory);
		
		File mainOutputDirectory = OUTPUT_DIRECTORY_PATH.toFile();
		if(mainOutputDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(mainOutputDirectory);
			} catch (IOException e) {
				System.err.println("Error while cleaning the main output directory: " + mainOutputDirectory.getAbsolutePath());
			}
		}else {
			mainOutputDirectory.mkdirs();
		}
		File graphsDirectoryFile = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_GRAPHS_DIRECTORY_NAME).toFile();
		graphsDirectoryFile.mkdirs();
		
		File resourcesDirectoryFile = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_RESOURCES_DIRECTORY_NAME).toFile();
		resourcesDirectoryFile.mkdirs();
		
		OUTPUT_JAVADOC_DIRECTORY_PATH = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_JAVADOC_DIRECTORY_NAME);
		File javadocDirectoryFile = OUTPUT_JAVADOC_DIRECTORY_PATH.toFile();
		javadocDirectoryFile.mkdirs();
		
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
	
	public static Path getDocletProjectOutputDirectoryPath() {
		return DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH;
	}
	
	public static Path getOutputDirectoryPath() {
		return OUTPUT_DIRECTORY_PATH;
	}
	
	public static Path getOutputJavaDocDirectoryPath() {
		return OUTPUT_JAVADOC_DIRECTORY_PATH;
	}
}
