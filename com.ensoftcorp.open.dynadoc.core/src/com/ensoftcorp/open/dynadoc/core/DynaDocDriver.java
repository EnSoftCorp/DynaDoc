package com.ensoftcorp.open.dynadoc.core;

import org.apache.commons.lang3.StringUtils;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.core.generator.ClassDocumentationGenerator;
import com.ensoftcorp.open.dynadoc.core.utils.PathUtils;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsAggregator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsImporter;
import com.ensoftcorp.open.dynadoc.support.DialogUtils;

public class DynaDocDriver { 
		
	private static final String ERROR_MESSAGE_EMPTY_CLASS_NAME_PROVIDED_BY_USER = "Empty class name provided by user";
	
	private static final String ERROR_MESSAGE_NOT_FOUND_CLASS_IN_CODE_MAP_TEMPLATE = "Class [%s] cannot be found in the current code map";
	
	private static final String ERROR_MESSAGE_CANNOT_CONFIGURE_WORKING_DIRECTORY = "Cannot properly configure the working directory to generate documentation";
	
	public static void testClass() {
		String javaClassFullyQualifiedName = "org.apache.poi.xssf.usermodel.XSSFWorkbook";
		generateClassDocumentation(javaClassFullyQualifiedName);
	}
	
	public static void generateClassDocumentation(String javaClassFullyQualifiedName) {
		if(StringUtils.isBlank(javaClassFullyQualifiedName)) {
			DialogUtils.showError(ERROR_MESSAGE_EMPTY_CLASS_NAME_PROVIDED_BY_USER);
			return;
		}
		Node classNode = null;
		int lastDotIndex = javaClassFullyQualifiedName.lastIndexOf('.');
		if(lastDotIndex >= 0) {
			// Java class fully qualified name is prefixed with package fully qualified name.
			String packageName = javaClassFullyQualifiedName.substring(0, lastDotIndex);
			String className = javaClassFullyQualifiedName.substring(lastDotIndex + 1);
			classNode = Common.typeSelect(packageName, className).eval().nodes().one();
		} else {
			// Java class fully qualified name is the same as the Java class name.
			classNode = Common.types(javaClassFullyQualifiedName).eval().nodes().one();
		}
		
		if(classNode == null) {
			String errorMessage = String.format(ERROR_MESSAGE_NOT_FOUND_CLASS_IN_CODE_MAP_TEMPLATE, javaClassFullyQualifiedName);
			DialogUtils.showError(errorMessage);
			return;
		}
		
		if(!Configurations.configureWorkingDirectory()) {
			DialogUtils.showError(ERROR_MESSAGE_CANNOT_CONFIGURE_WORKING_DIRECTORY);
			return;
		}
		
		Node projectNode = Common.toQ(classNode).containers().nodes(XCSG.Project).eval().nodes().one();
		aggregateAndImportSupplementaryArtifacts(projectNode, classNode);
		generateClassDocumentation(classNode);
	}
	
	public static void testClasses() {
		String projectName = "ApachePOI";
		Node projectNode = Query.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		Q projectQ = Common.toQ(projectNode);
		Q projectContainedNodesQ = projectQ.contained();
		Q projectClasses = projectContainedNodesQ.nodes(XCSG.Java.Class);
		Q projectInterfaces = projectContainedNodesQ.nodes(XCSG.Java.Interface);
		Q projectLocalClassesQ = projectContainedNodesQ.nodes(XCSG.Java.LocalClass);
		Q classesQ = projectClasses.union(projectInterfaces);
		classesQ = classesQ.difference(projectLocalClassesQ);
		generateClassesDocumentation(projectName, classesQ);
	}
	
	public static void generateClassesDocumentation(String projectName, Q classesQ) {
		Node projectNode = Query.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		AtlasSet<Node> classNodes = classesQ.eval().nodes();
		if(classNodes.isEmpty()) {
			Log.error("Cannot find nodes corresponding to XCSG.Java.Class in the given code map for the project: " + projectName, null);
			return;
		}
		
		if(!Configurations.configureWorkingDirectory()) {
			return;
		}
		
		int progress = 0;
		double estimatedTimeToCompleteGenerationInMinutes = (classNodes.size() * 30.0) / 60.0;
		Log.info("There are [" + classNodes.size() + "] classes, estimated time is [" + estimatedTimeToCompleteGenerationInMinutes + "] minutes." );
		for(Node classNode: classNodes) {
			Log.info("Progress: " + (++progress) + "/" + classNodes.size());
			aggregateAndImportSupplementaryArtifacts(projectNode, classNode);
			generateClassDocumentation(classNode);
		}
	}
	
	private static void aggregateAndImportSupplementaryArtifacts(Node projectNode, Node classNode) {
		long start = System.currentTimeMillis();
		Log.info("Started aggregating and importing Ssupplementary artifacts.");
		SupplementaryArtifactsAggregator.aggregateArtifacts(projectNode, classNode, Configurations.rootWorkingDirectory().getPath());
		SupplementaryArtifactsImporter.importArtifacts(Configurations.rootWorkingDirectory().getPath());
		
		double duration = (System.currentTimeMillis() - start) / 60000.0;
		Log.info("Done aggregating and importing of supplementary artifacts in: " + duration + "m");
	}
	
	private static void generateClassDocumentation(Node classNode) {
		long start = System.currentTimeMillis();
		Log.info("Started generating documentation for class: " + classNode.getAttr(XCSG.name));
		
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classNode, PathUtils.getDocumentationWorkingDirectory());
		classDocumentationGenerator.generate();
		
		double duration = (System.currentTimeMillis() - start) / 60000.0;
		Log.info("Done generating documentation for class (" + classNode.getAttr(XCSG.name) + ") in: " + duration + "m");
	}
	
}
