package com.kcsl.dynadoc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.log.Log;

public class Configurations {
	
	private static final String OUTPUT_GRAPHS_DIRECTORY_NAME = "graphs";
	
	private static final String OUTPUT_JAVADOC_DIRECTORY_NAME = "javadoc";
	
	private static final String OUTPUT_RESOURCES_DIRECTORY_NAME = "resources";
	
	private static final String PLUGIN_RESOURCES_ROOT_DIRECTORY = "./templates/";
	
	public static final String PLUGIN_SCRIPTS_DIRECTORY_PATH = PLUGIN_RESOURCES_ROOT_DIRECTORY + "scripts/";
	
	private static final String PLUGIN_RESOURCES_DIRECTORY_PATH = PLUGIN_RESOURCES_ROOT_DIRECTORY + "resources/";
	
	private static final String [] RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED = { "check.svg", "details_close.png", "details_open.png", "styles.css"};
	
	private static String DOCLET_QULALIFIED_CLASS_NAME = null;
	
	private static String OUTPUT_DIRECTORY_ABSOLUTE_PATH = null;
	
	private static Path DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH = null;
	
	private static Path OUTPUT_DIRECTORY_PATH = null;
	
	private static Path OUTPUT_JAVADOC_DIRECTORY_PATH = null;
	
	private static Path OUTPUT_GRAPHS_DIRECTORY_PATH = null;
	
	private static Path OUTPUT_RESOURCES_DIRECTORY_PATH = null;
	
	public static void createProperOutputDirectoriesStructure() {		
		// Doclet configurations
		DOCLET_QULALIFIED_CLASS_NAME = com.kcsl.docdoclet.Activator.getDocletClassQualifiedName();
		String docletProjectOutputDirectoryAbsolutePath = com.kcsl.docdoclet.Activator.getDocletOutputDirectoryPath();
		if(docletProjectOutputDirectoryAbsolutePath == null) {
			Log.warning("Missing the output directory location for the doclet project: " + Activator.PLUGIN_ID);
			return;
		}
		Log.info(docletProjectOutputDirectoryAbsolutePath);
		DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH = Paths.get(docletProjectOutputDirectoryAbsolutePath);
		
		// Output directory for generated documentation
		boolean outputDirectorySet = setOutputDirectoryFromUser();
		if(!outputDirectorySet) {
			Log.warning("Missing output directory location to store generated documentation");
			return;
		}
		OUTPUT_DIRECTORY_PATH = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE_PATH);
		
		// Creating the structure
		// Output Directory/
		// ... graphs/
		// ... resources/
		// ... javadoc/
		File mainOutputDirectory = OUTPUT_DIRECTORY_PATH.toFile();
		if(mainOutputDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(mainOutputDirectory);
			} catch (IOException e) {
				Log.error("Error while cleaning the main output directory: " + mainOutputDirectory.getAbsolutePath(), e);
			}
		}else {
			mainOutputDirectory.mkdirs();
		}
		
		// "graphs" directory
		OUTPUT_GRAPHS_DIRECTORY_PATH = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_GRAPHS_DIRECTORY_NAME);
		File graphsDirectoryFile = OUTPUT_GRAPHS_DIRECTORY_PATH.toFile();
		graphsDirectoryFile.mkdirs();
		
		// "resources" directory
		OUTPUT_RESOURCES_DIRECTORY_PATH = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_RESOURCES_DIRECTORY_NAME);
		File resourcesDirectoryFile = OUTPUT_RESOURCES_DIRECTORY_PATH.toFile();
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
		
		// "javadoc" directory
		OUTPUT_JAVADOC_DIRECTORY_PATH = OUTPUT_DIRECTORY_PATH.resolve(OUTPUT_JAVADOC_DIRECTORY_NAME);
		File javadocDirectoryFile = OUTPUT_JAVADOC_DIRECTORY_PATH.toFile();
		javadocDirectoryFile.mkdirs();		
	}
	
	private static boolean setOutputDirectoryFromUser() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
		    @Override
		    public void run() {
		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				DirectoryDialog dialog = new DirectoryDialog(shell);
				if(OUTPUT_DIRECTORY_ABSOLUTE_PATH == null) {
					dialog.setFilterPath(".");
				} else {
					dialog.setFilterPath(OUTPUT_DIRECTORY_ABSOLUTE_PATH);
				}
				OUTPUT_DIRECTORY_ABSOLUTE_PATH = dialog.open();
		    }
		});
		return OUTPUT_DIRECTORY_ABSOLUTE_PATH != null;
	}
	
	public static Path getDocletProjectOutputDirectoryPath() {
		return DOCLET_PROJECT_OUTPUT_DIRECTORY_PATH;
	}
	
	public static String getDocletQualifiedClassName() {
		return DOCLET_QULALIFIED_CLASS_NAME;
	}
	
	public static Path getOutputDirectoryPath() {
		return OUTPUT_DIRECTORY_PATH;
	}
	
	public static Path getOutputJavaDocDirectoryPath() {
		return OUTPUT_JAVADOC_DIRECTORY_PATH;
	}
	
	public static Path getOutputGraphsDirectoryPath() {
		return OUTPUT_GRAPHS_DIRECTORY_PATH;
	}
	
	public static Path getOutputResourcesDirectoryPath() {
		return  OUTPUT_RESOURCES_DIRECTORY_PATH;
	}

}
