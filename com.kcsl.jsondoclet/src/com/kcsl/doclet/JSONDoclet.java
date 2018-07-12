package com.kcsl.doclet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.sun.javadoc.*;

public final class JSONDoclet {
	
	private static final String OUTPUT_DIRECTORY_LOCATION = "/Users/ahmedtamrawi/Desktop/test/java-docs/";
	
	private static Path OUTPUT_DIRECTORY_PATH;
	
	private static FileWriter writer;
	
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    public static boolean start(RootDoc rootDoc) {
    	try {
			writer = new FileWriter("/Users/ahmedtamrawi/Desktop/test/log.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	write("1");
    	prepareOutputDirectoryStructure();
    	write("2");
    	generateDocumentationForJavaClasses(rootDoc);
    	write("3");
    	try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return true;
    }
    
    private static void write(String message) {
    	try {
			writer.write(message + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void prepareOutputDirectoryStructure() {
    	File outputDirectoryFile = new File(OUTPUT_DIRECTORY_LOCATION);
    	if(outputDirectoryFile.exists()) {
    		try {
				FileUtils.cleanDirectory(outputDirectoryFile);
			} catch (IOException e) {
				write("error3");
				System.err.println("Error cleaning the output directory: " + OUTPUT_DIRECTORY_LOCATION);
			}
    	}else {
    		outputDirectoryFile.mkdirs();
    	}
    	OUTPUT_DIRECTORY_PATH = outputDirectoryFile.toPath();
    }
    
    private static void generateDocumentationForJavaClasses(RootDoc rootDoc) {
    	ClassDoc[] classDocs = rootDoc.classes();
    	for(ClassDoc classDoc: classDocs) {
    		generateDocumentationForJavaClass(classDoc);
    	}
    }
    
    private static void generateDocumentationForJavaClass(ClassDoc classDoc) {
    	String qualifiedClassName = classDoc.qualifiedName();
    	JSONObject classJSONObject = new JSONObject();
    		JSONObject classProperties = new JSONObject();
    		classProperties.put("comment1", classDoc.commentText());
    		classProperties.put("comment2", classDoc.getRawCommentText());
    	
    			JSONObject fieldsJSONObject = new JSONObject();
	    		FieldDoc[] fieldDocs = classDoc.fields();
	    		for(FieldDoc fieldDoc: fieldDocs) {
	    			fieldsJSONObject.put(fieldDoc.name(), fieldDoc.getRawCommentText());
	    		}
	    		classProperties.put("fields", fieldsJSONObject);
    		
	    		JSONObject methodsJSONObject = new JSONObject();
	    		MethodDoc[] methodDocs = classDoc.methods();
	    		for(MethodDoc methodDoc: methodDocs) {
	    			methodsJSONObject.put(methodDoc.signature(), methodDoc.getRawCommentText());
	    		}
	    		classProperties.put("methods", methodsJSONObject);
    	
    		classJSONObject.put(qualifiedClassName, classProperties);
    	saveJSONClassDocumentation(qualifiedClassName, classJSONObject);
    }
    
    private static void saveJSONClassDocumentation(String qualifiedClassName, JSONObject jsonObject) {
    	File outputFile = OUTPUT_DIRECTORY_PATH.resolve(qualifiedClassName + ".json").toFile();
        try {
        	FileWriter file = new FileWriter(outputFile);
            file.write(jsonObject.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			System.err.println("Error writing the JSON file for class: " + qualifiedClassName);
		}
    }

}