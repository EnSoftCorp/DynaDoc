package com.ensoftcorp.open.dynadoc.supplementary.aggregator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.doclet.Activator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants;

public class JavaDocAggregatorTask implements Runnable {
	
	private static String DOCLET_QULALIFIED_CLASS_NAME;
	
	private static String DOCLET_PROJECT_CLASSES_DIRECTORY_PATH;
	
	/**
	 * The following is the list of arguments:
	 * <p>1. The absolute/relative path to the project for which the generation will occur.</p>
	 * <p>2. The doclet qualified class name</p>
	 * <p>3. The absolute/relative path to the output directory of the Doclet project.</p>
	 * <p>4. The absolute/relative path to the output directory where the results will be stored.</p>
	 */
	@SuppressWarnings(value = { "unused" })
	private static final String JAVA_DOC_RUN_ON_PROJECT_COMMAND_TEMPLATE = "find %s -type f -name \"*.java\" | xargs javadoc -quiet -private -doclet %s -docletpath %s -output %s";
	
	/**
	 * The following is the list of arguments:
	 * <p>1. The doclet qualified class name</p>
	 * <p>2. The absolute/relative path to the output directory of the Doclet project.</p>
	 * <p>3. The "-sourcepath" option placeholder (if any).</p>
	 * <p>4. The "-classpath" option placeholder (if any).</p>
	 * <p>5. The absolute/relative path to the class for which the generation will occur.</p>
	 * <p>6. The absolute/relative path to the output directory where the results will be stored.</p>
	 */
	private static final String JAVA_DOC_RUN_ON_CLASS_COMMAND_TEMPLATE = "javadoc -quiet -private -doclet %s -docletpath %s %s %s %s -output %s"; 	
	
	
	private String command;
	
	private String workingDirectory;
	
	public JavaDocAggregatorTask(String workingDirectory, String command) {
		this.workingDirectory = workingDirectory;
		this.command = command;
	}
	
	private String getCommand() {
		return this.command;
	}
	
	private String getWorkingDirectory() {
		return this.workingDirectory;
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			Process process = null;
			if(this.getWorkingDirectory().trim().isEmpty()) {
				process = rt.exec(this.getCommand());
			}else {
				File workingDirectoryFile = new File(this.getWorkingDirectory());
				process = rt.exec(this.getCommand(), null, workingDirectoryFile);
			}
			process.waitFor();
		} catch (IOException e) {
			Log.error("Error while executing the JavaDoc command: " + command, e);
		} catch (InterruptedException e) {
			Log.error("Error while waiting for executing the JavaDoc command: " + command, e);
		}
	}
	
	public static boolean runOnClasses(Node projectNode, Q classesQ, Path rootWorkingDirectory) {
		if(!figureDocletProjectParameters()) {
			Log.error("Missing the proper configuration for the doclet project: " + Activator.PLUGIN_ID, null);
			return false;
		}
		
		if(!configureJavaDocDirectory(rootWorkingDirectory)) {
			Log.error("Could not properly configure the directory to hold the JavaDoc generated contents", null);
			return false;
		}
		
		Path javaDocDirectoryPath = rootWorkingDirectory.resolve(SupplementaryArtifactConstants.JavaDoc.JAVADOC_OUTPUT_DIRECTORY_NAME);
		String javaDocDirectoryAbsolutePath = javaDocDirectoryPath.toFile().getAbsolutePath();
		
		String projectName = projectNode.getAttr(XCSG.name).toString();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(projectName);
		String projectAbsolutePathString = project.getRawLocation().toOSString();
		
		Map<Integer, List<String>> classPathEntryKindToFileList = findSourceAndLibClassPathEntries(project);
		
		List<String> sourceClassPathEntries = classPathEntryKindToFileList.get(IClasspathEntry.CPE_SOURCE);
		String sourcePathOptionCommandString = formCommandString("-sourcepath", sourceClassPathEntries);
		
		List<String> libraryClassPathEntries = classPathEntryKindToFileList.get(IClasspathEntry.CPE_LIBRARY);
		String classPathOptionCommandString = formCommandString("-classpath", libraryClassPathEntries);
		
		AtlasSet<Node> classNodes = classesQ.eval().nodes();
		for(Node classNode: classNodes) {
			SourceCorrespondence classSourceCorrespondence = (SourceCorrespondence) classNode.getAttr(XCSG.sourceCorrespondence);
			IFile classSourceFile = classSourceCorrespondence.sourceFile;
			String classSourceFileAbsolutePath = classSourceFile.getLocation().toString();
			
			String command = String.format(JAVA_DOC_RUN_ON_CLASS_COMMAND_TEMPLATE, 
												DOCLET_QULALIFIED_CLASS_NAME,
												DOCLET_PROJECT_CLASSES_DIRECTORY_PATH, 
												sourcePathOptionCommandString, 
												classPathOptionCommandString, 
												classSourceFileAbsolutePath, 
												javaDocDirectoryAbsolutePath);
			runCommand(projectAbsolutePathString, command);
		}
		return true;
	}
	
	private static boolean figureDocletProjectParameters() {
		DOCLET_QULALIFIED_CLASS_NAME = Activator.getDocletClassQualifiedName();
		if(StringUtils.isEmpty(DOCLET_QULALIFIED_CLASS_NAME)) {
			return false;
		}
		
		DOCLET_PROJECT_CLASSES_DIRECTORY_PATH = Activator.getDocletOutputDirectoryPath();
		if(StringUtils.isEmpty(DOCLET_PROJECT_CLASSES_DIRECTORY_PATH)) {
			return false;
		}
		return true;
	}
	
	private static boolean configureJavaDocDirectory(Path rootWorkingDirectory) {
		Path javaDocDirectoryPath = rootWorkingDirectory.resolve(SupplementaryArtifactConstants.JavaDoc.JAVADOC_OUTPUT_DIRECTORY_NAME);
		File javaDocDirectory = javaDocDirectoryPath.toFile();
		if(javaDocDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(javaDocDirectory);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return javaDocDirectoryPath.toFile().mkdirs();	
	}
	
	private static String formCommandString(String option, List<String> valueList) {
		String optionValue = StringUtils.join(valueList, File.pathSeparatorChar);
		return optionValue.isEmpty() ? StringUtils.EMPTY : option + " " + optionValue;
	}
	
	private static Map<Integer, List<String>> findSourceAndLibClassPathEntries(IProject project) {
		Map<Integer, List<String>> classPathEntryKindToFileList = new HashMap<Integer, List<String>>();
		classPathEntryKindToFileList.put(IClasspathEntry.CPE_LIBRARY, new ArrayList<String>());
		classPathEntryKindToFileList.put(IClasspathEntry.CPE_SOURCE, new ArrayList<String>());
		
		try {
			if(project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject javaProject = JavaCore.create(project);
				IClasspathEntry[] resolvedClasspathEntries = javaProject.getResolvedClasspath(true);
				for (IClasspathEntry classpathEntry : resolvedClasspathEntries) {
					IPath classPathEntryPath = classpathEntry.getPath();
					String firstSegment = classPathEntryPath.segment(0);
					if(project.getName().equals(firstSegment)) {
						classPathEntryPath = classPathEntryPath.removeFirstSegments(1);
					}
					if(IClasspathEntry.CPE_LIBRARY == classpathEntry.getEntryKind()) {
						classPathEntryKindToFileList.get(IClasspathEntry.CPE_LIBRARY).add(classPathEntryPath.toOSString());
					}else if(IClasspathEntry.CPE_SOURCE == classpathEntry.getEntryKind()) {
						classPathEntryKindToFileList.get(IClasspathEntry.CPE_LIBRARY).add(classPathEntryPath.toOSString());
					}
				}
			}
		} catch (CoreException e) {
			Log.error("Project [" + project.getName() + "] is not a Java project therefore the JavaDoc program cannot generate proper documentations", e);
		}
		return classPathEntryKindToFileList;
	}
	
	private static boolean runCommand(String workingDirectory, String command) {
		JavaDocAggregatorTask javaDocGeneratorTask = new JavaDocAggregatorTask(workingDirectory, command);
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
