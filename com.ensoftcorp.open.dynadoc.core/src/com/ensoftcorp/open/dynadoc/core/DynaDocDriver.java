package com.ensoftcorp.open.dynadoc.core;

import java.awt.Desktop;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import com.ensoftcorp.open.dynadoc.core.data.QueryCache;
import com.ensoftcorp.open.dynadoc.core.generator.ClassDocumentationGenerator;
import com.ensoftcorp.open.dynadoc.core.utils.PathUtils;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsAggregator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsImporter;

public class DynaDocDriver { 

	private static final String ERROR_MESSAGE_EMPTY_CLASS_NAMES_PROVIDED_BY_USER = "User did not provide any valid fully qualified Java class name in this code map";
	
	private static final String ERROR_MESSAGE_PROJECT_NOT_FOUND_IN_CODE_MAP_TEMPLATE = "Project [%s] cannot be found in the current code map";
	
	private static final String ERROR_MESSAGE_CANNOT_CONFIGURE_WORKING_DIRECTORY = "Cannot properly configure the working directory to generate documentation";
	
	/**
	 * Runs DynaDoc on every {@link XCSG.Java.Class} and {@link XCSG.Java.Interface} in the {@link Query#universe()}.
	 */
	public static void run() {
		runOnClassesWithinContext(Query.universe());
	}
	
	/**
	 * Runs DynaDoc on every {@link XCSG.Java.Class} and {@link XCSG.Java.Interface} within the given project.
	 * 
	 * @param projectName The project name to be used to mine the classes within.
	 */
	public static void runOnProject(String projectName) {
		Node projectNode = Query.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		if(projectNode == null) {
			String errorMessage = String.format(ERROR_MESSAGE_PROJECT_NOT_FOUND_IN_CODE_MAP_TEMPLATE, projectName);
			DisplayUtils.showError(errorMessage);
			return;
		}
		Q projectQ = Common.toQ(projectNode);
		runOnClassesWithinContext(projectQ);
	}
	
	/**
	 * Runs DynaDoc on the given comma-separated list of fully qualified Java class names.
	 * 
	 * @param fullyQualifiedJavaClassNames A comma-separated list of fully qualified Java class names.
	 */
	public static void run(String fullyQualifiedJavaClassNames) {
		if(StringUtils.isBlank(fullyQualifiedJavaClassNames)) {
			DisplayUtils.showError(ERROR_MESSAGE_EMPTY_CLASS_NAMES_PROVIDED_BY_USER);
			return;
		}
		String[] fullyQualifiedJavaClassNamesArray = StringUtils.split(fullyQualifiedJavaClassNames, Configurations.USER_INPUT_CLASS_NAMES_SEPARATOR);
		Q classesQ = Common.empty();
		for(String fullyQualifiedJavaClassName: fullyQualifiedJavaClassNamesArray) {
			Q classQ = queryClassByFullyQualifiedName(fullyQualifiedJavaClassName);
			classesQ = classesQ.union(classQ);
		}
		run(classesQ);
	}
	
	/**
	 * Runs DynaDoc on the given set of classes.
	 * 
	 * @param classesQ A instance of {@link Q} containing a set of Java classes.
	 */
	public static void run(Q classesQ) {
		AtlasSet<Node> classNodes = classesQ.eval().nodes();
		if(classNodes.isEmpty()) {
			DisplayUtils.showError(ERROR_MESSAGE_EMPTY_CLASS_NAMES_PROVIDED_BY_USER);
			return;
		}
		
		if(!Configurations.configureWorkingDirectory()) {
			DisplayUtils.showError(ERROR_MESSAGE_CANNOT_CONFIGURE_WORKING_DIRECTORY);
			return;
		}
		
		aggregateAndImportSupplementaryArtifacts(classesQ);
		generateClassesDocumentation(classesQ);
		
		try {
			Desktop.getDesktop().open(Configurations.rootWorkingDirectory().getPath().toFile());
		} catch (IOException e) {
			Log.warning("Error while trying to open the current working directory.");
		}
	}
	
	private static Q queryClassByFullyQualifiedName(String fullyQualifiedJavaClassName) {
		int lastDotIndex = fullyQualifiedJavaClassName.lastIndexOf('.');
		if(lastDotIndex >= 0) {
			// Java class fully qualified name is prefixed with package fully qualified name.
			String packageName = fullyQualifiedJavaClassName.substring(0, lastDotIndex);
			String className = fullyQualifiedJavaClassName.substring(lastDotIndex + 1);
			return Common.typeSelect(packageName, className);
		} else {
			// Java class fully qualified name is the same as the Java class name.
			return Common.types(fullyQualifiedJavaClassName);
		}
	}
	
	private static void runOnClassesWithinContext(Q context) {
		Q contextContainedNodesQ = QueryCache.containsEdges.forward(context);
		Q contextClasses = contextContainedNodesQ.nodes(XCSG.Java.Class);
		Q contextInterfaces = contextContainedNodesQ.nodes(XCSG.Java.Interface);
		Q contextLocalClassesQ = contextContainedNodesQ.nodes(XCSG.Java.LocalClass);
		Q classesQ = contextClasses.union(contextInterfaces);
		classesQ = classesQ.difference(contextLocalClassesQ);
		run(classesQ);
	}
	
	private static void aggregateAndImportSupplementaryArtifacts(Q classesQ) {
		long start = System.currentTimeMillis();
		Log.info("Started aggregating and importing supplementary artifacts.");
		Q projectsQ = QueryCache.containsEdges.reverse(classesQ).nodes(XCSG.Project);
		AtlasSet<Node> projectNodes = projectsQ.eval().nodes();
		for(Node projectNode: projectNodes) {
			Q classesForProject = QueryCache.containsEdges.forward(Common.toQ(projectNode)).intersection(classesQ);
			SupplementaryArtifactsAggregator.aggregateArtifacts(projectNode, classesForProject, Configurations.rootWorkingDirectory().getPath());
			SupplementaryArtifactsImporter.importArtifacts(Configurations.rootWorkingDirectory().getPath());
		}
		
		double duration = (System.currentTimeMillis() - start) / 60000.0;
		Log.info("Done aggregating and importing of supplementary artifacts in: " + duration + "m");
	}
	
	private static void generateClassesDocumentation(Q classesQ) {
		AtlasSet<Node> classNodes = classesQ.eval().nodes();
		int progress = 0;
		double estimatedTimeToCompleteGenerationInMinutes = (classNodes.size() * 30.0) / 60.0;
		Log.info("There are [" + classNodes.size() + "] classes, estimated time is [" + estimatedTimeToCompleteGenerationInMinutes + "] minutes." );
		for(Node classNode: classNodes) {
			Log.info("Progress: " + (++progress) + "/" + classNodes.size());
			long start = System.currentTimeMillis();
			Log.info("Started generating documentation for class: " + classNode.getAttr(XCSG.name));
			
			ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classNode, PathUtils.getDocumentationWorkingDirectory());
			classDocumentationGenerator.generate();
			
			double duration = (System.currentTimeMillis() - start) / 60000.0;
			Log.info("Done generating documentation for class (" + classNode.getAttr(XCSG.name) + ") in: " + duration + "m");
		}
	}
	
}
