package com.kcsl.dynadoc;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.dynadoc.generator.ClassDocumentationGenerator;

public class DynaDocDriver { 
	
	public static void test() {
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Path storeDirectoryPath = Paths.get("/Users/ahmedtamrawi/Desktop", "test"); 
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classQ, storeDirectoryPath);
		classDocumentationGenerator.generate();
	}
		
}
