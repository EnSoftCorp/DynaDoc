package com.kcsl.dynadoc;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.dynado.doc.CodeMapJavaDocInjector;
import com.kcsl.dynado.doc.JavaDocGeneratorTask;
import com.kcsl.dynadoc.generator.ClassDocumentationGenerator;

public class DynaDocDriver { 
	
	public static void testClass() {
		String projectName = "ApachePOI";
		String packageName = "org.apache.poi.xssf.usermodel";
		String className = "XSSFWorkbook";
		String docletProjectOutputDirectoryAbsolutePath = "/Users/ahmedtamrawi/git/dyna-doc/com.kcsl.jsondoclet/bin/";
		String outputDirectory = "/Users/ahmedtamrawi/Desktop/test"; 
		generateClassDocumentation(projectName, packageName, className, docletProjectOutputDirectoryAbsolutePath, outputDirectory);
	}
	
	public static void generateClassDocumentation(String projectName, String packageName, String className, String docletProjectOutputDirectory, String outputDirectory) {
		Node projectNode = Common.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName).eval().nodes().one();
		Node classNode = Common.typeSelect(packageName, className).eval().nodes().one();
		
		Configurations.createProperOutputDirectoriesStructure(outputDirectory, docletProjectOutputDirectory);
		
		generateClassJavaDoc(projectNode, classNode);
		populateProjectCodeMapWithJavaDocs(projectNode);
		generateClassDocumentation(classNode);
	}
	
	private static void generateClassJavaDoc(Node projectNode, Node classNode) {
		JavaDocGeneratorTask.runOnClass(projectNode, classNode);
	}
	
	private static void populateProjectCodeMapWithJavaDocs(Node projectNode) {
		CodeMapJavaDocInjector codeMapJavaDocInjector = new CodeMapJavaDocInjector(projectNode);
		codeMapJavaDocInjector.populate();
	}
	
	private static void generateClassDocumentation(Node classNode) {
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classNode);
		classDocumentationGenerator.generate();
	}
	
}
