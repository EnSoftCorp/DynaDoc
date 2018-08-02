package com.ensoftcorp.open.dynadoc.core;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.core.generator.ClassDocumentationGenerator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsAggregator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsImporter;

public class DynaDocDriver { 
	
	public static void testClass() {
		String projectName = "ApachePOI";
		String packageName = "org.apache.poi.xssf.usermodel";
		String className = "XSSFWorkbook";
		generateClassDocumentation(projectName, packageName, className);
	}
	
	public static void generateClassDocumentation(String projectName, String packageName, String className) {
		Node projectNode = Common.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		Node classNode = Common.typeSelect(packageName, className).eval().nodes().one();
		
		if(!Configurations.configureWorkingDirectory()) {
			return;
		}
		
		aggregateAndImportSupplementaryArtifacts(projectNode, classNode);
		generateClassDocumentation(classNode);
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
		
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classNode, Configurations.rootWorkingDirectory());
		classDocumentationGenerator.generate();
		
		double duration = (System.currentTimeMillis() - start) / 60000.0;
		Log.info("Done generating documentation for class (" + classNode.getAttr(XCSG.name) + ") in: " + duration + "m");
	}
	
}
