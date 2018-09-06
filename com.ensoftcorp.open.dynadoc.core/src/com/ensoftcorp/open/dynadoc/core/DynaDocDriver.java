package com.ensoftcorp.open.dynadoc.core;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.dynadoc.core.generator.ClassDocumentationGenerator;
import com.ensoftcorp.open.dynadoc.core.utils.PathUtils;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsAggregator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsImporter;

public class DynaDocDriver { 
	
	private static final String ERROR_MESSAGE_NOT_FOUND_CLASS_IN_CODE_MAP_TEMPLATE = "Class [%s] cannot be found in the current code map for the project: %s";
	
	public static void testClass() {
		String projectName = "ApachePOI";
		String packageName = "org.apache.poi.xssf.usermodel";
		String className = "XSSFWorkbook";
		generateClassDocumentation(projectName, packageName, className);
	}
	
	public static void generateClassDocumentation(String projectName, String packageName, String className) {
		Node projectNode = Query.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		Node classNode = Common.typeSelect(packageName, className).eval().nodes().one();
		
		if(classNode == null) {
			String errorMessage = String.format(ERROR_MESSAGE_NOT_FOUND_CLASS_IN_CODE_MAP_TEMPLATE, CommonQueries.getQualifiedName(classNode), projectName);
			Log.error(errorMessage, null);
			return;
		}
		
		if(!Configurations.configureWorkingDirectory()) {
			return;
		}
		
		aggregateAndImportSupplementaryArtifacts(projectNode, classNode);
		generateClassDocumentation(classNode);
	}
	
	public static void testClasses() {
		String projectName = "ApachePOI";
		Node projectNode = Query.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		Q classesQ = Common.toQ(projectNode).contained().nodes(XCSG.Java.Class).difference(Common.toQ(projectNode).contained().nodes(XCSG.Java.InnerClass));
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
