package com.kcsl.dynadoc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.dynado.doc.JavaDocGeneratorTask;
import com.kcsl.dynadoc.generator.ClassDocumentationGenerator;

public class DynaDocDriver { 
	

	
	public static void testClass() {
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Path outputDirectoryPath = Paths.get("/Users/ahmedtamrawi/Desktop", "test"); 
		Configurations.createProperOutputDirectoriesStructure(outputDirectoryPath);
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classQ, outputDirectoryPath);
		classDocumentationGenerator.generate();
		
//		JavaDocGeneratorTask.runOnClass();
//		JavaDocGeneratorTask.runOnProject();
	}
		
}
