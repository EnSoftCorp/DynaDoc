package com.kcsl.dynadoc;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.dynado.doc.JavaDocGeneratorTask;
import com.kcsl.dynadoc.generator.ClassDocumentationGenerator;

import static com.kcsl.dynadoc.Configurations.OUTPUT_JAVADOC_DIRECTORY_NAME;

public class DynaDocDriver { 
	

	
	public static void testClass() {
		String projectName = "ApachePOI";
		String packageName = "org.apache.poi.xssf.usermodel";
		String className = "XSSFWorkbook";
		String docletProjectAbsolutePathToOutputDirectory = "/Users/ahmedtamrawi/git/dyna-doc/com.kcsl.jsondoclet/bin/";
		
		Path outputDirectoryPath = Paths.get("/Users/ahmedtamrawi/Desktop", "test"); 
		Configurations.createProperOutputDirectoriesStructure(outputDirectoryPath);
		
		Node classNode = Common.typeSelect(packageName, className).eval().nodes().one();
		generateClassDocumentation(classNode, outputDirectoryPath);
		generateClassJavaDoc(classNode, docletProjectAbsolutePathToOutputDirectory, outputDirectoryPath);
	}
	
	private static void generateClassDocumentation(Node classNode, Path outputDirectoryPath) {
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classNode, outputDirectoryPath);
		classDocumentationGenerator.generate();
	}
	
	private static void generateClassJavaDoc(Node classNode, String docletProjectAbsolutePathToOutputDirectory, Path outputDirectoryPath) {
		SourceCorrespondence classSourceCorrespondence = (SourceCorrespondence) classNode.getAttr(XCSG.sourceCorrespondence);
		IFile classSourceFile = classSourceCorrespondence.sourceFile;
		String classSourceFileAbsolutePath = classSourceFile.getLocation().toString();
		String outputJavadocDirectoryAbsolutePath = outputDirectoryPath.resolve(OUTPUT_JAVADOC_DIRECTORY_NAME).toFile().getAbsolutePath();
		JavaDocGeneratorTask.runOnClass(classSourceFileAbsolutePath, docletProjectAbsolutePathToOutputDirectory, outputJavadocDirectoryAbsolutePath);
	}
		
}
