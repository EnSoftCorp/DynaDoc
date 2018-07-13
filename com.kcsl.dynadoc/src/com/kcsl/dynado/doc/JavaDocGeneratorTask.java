package com.kcsl.dynado.doc;

import java.io.IOException;

import com.ensoftcorp.atlas.core.log.Log;

public class JavaDocGeneratorTask implements Runnable {
	
	private static final String DOCLET_QULALIFIED_NAME = "com.kcsl.doclet.JSONDoclet";
	/**
	 * The following is the list of arguments:
	 * <p>1. The absolute/relative path to the project for which the generation will occur.</p>
	 * <p>2. The absolute/relative path to the output directory of the Doclet project.</p>
	 * <p>3. The absolute/relative path to the output directory where the results will be stored.</p>
	 */
	private static final String JAVA_DOC_RUN_ON_PROJECT_COMMAND_TEMPLATE = "find %s -type f -name \"*.java\" | xargs javadoc -quiet -private -doclet " + DOCLET_QULALIFIED_NAME + " -docletpath %s -output %s";
	
	/**
	 * The following is the list of arguments:
	 * <p>1. The absolute/relative path to the output directory of the Doclet project.</p>
	 * <p>2. The absolute/relative path to the class for which the generation will occur.</p>
	 * <p>3. The absolute/relative path to the output directory where the results will be stored.</p>
	 */
	private static final String JAVA_DOC_RUN_ON_CLASS_COMMAND_TEMPLATE = "javadoc -quiet -private -doclet " + DOCLET_QULALIFIED_NAME + " -docletpath %s %s -output %s"; 	
	
	
	private String command;
	
	public JavaDocGeneratorTask(String command) {
		this.command = command;
	}
	
	private String getCommand() {
		return this.command;
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			Process process = rt.exec(this.getCommand());
			process.waitFor();
		} catch (IOException e) {
			Log.error("Error while executing the JavaDoc command: " + command, e);
		} catch (InterruptedException e) {
			Log.error("Error while waiting for executing the JavaDoc command: " + command, e);
		}
	}
	
	public static boolean runOnClass(String pathToClass, String docletClassPath, String outputDirectory) {
		String command = String.format(JAVA_DOC_RUN_ON_CLASS_COMMAND_TEMPLATE, docletClassPath, pathToClass, outputDirectory);
		return runCommand(command);
	}
	
	public static boolean runOnProject(String pathToProject, String docletClassPath, String outputDirectory) {
		String command = String.format(JAVA_DOC_RUN_ON_PROJECT_COMMAND_TEMPLATE, pathToProject, docletClassPath, outputDirectory);
		return runCommand(command);
	}
	
	public static boolean runCommand(String command) {
        JavaDocGeneratorTask javaDocGeneratorTask = new JavaDocGeneratorTask(command);
        Thread executorThread = new Thread(javaDocGeneratorTask);
        executorThread.start();
        try {
			executorThread.join();
		} catch (InterruptedException e) {
			Log.error("Error while waiting for the JavaDoc generation to finish: " + command, e);
			return false;
		}
        return true;
	}

}
