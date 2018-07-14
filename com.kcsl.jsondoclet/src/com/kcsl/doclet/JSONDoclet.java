package com.kcsl.doclet;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.sun.javadoc.*;

public final class JSONDoclet {
	
	private static final String OUTPUT_DIRECTORY_COMMAND_LINE_OPTION_NAME = "-output";
	
	private static Path OUTPUT_DIRECTORY_PATH;
	
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
    
	/**
	 * Check for doclet-added options. Returns the number of arguments you must
	 * specify on the command line for the given option. For example, "-d docs"
	 * would return 2.
	 * <P>
	 * This method is required if the doclet contains any options. If this
	 * method is missing, Javadoc will print an invalid flag error for every
	 * option.
	 * 
	 * @see com.sun.javadoc.Doclet#optionLength(String)
	 * 
	 * @param optionName
	 *            The name of the option.
	 * @return number of arguments on the command line for an option including
	 *         the option name itself. Zero return means option not known.
	 *         Negative value means error occurred.
	 */
	public static int optionLength(String optionName) {
		if(OUTPUT_DIRECTORY_COMMAND_LINE_OPTION_NAME.equals(optionName)) {
			return 2;
		}
		return 0;
	}

    public static boolean start(RootDoc rootDoc) {
    	if(setOutputDirectory(rootDoc.options())) {
    		generateDocumentationForJavaClasses(rootDoc);
    		return true;
    	}
    	System.err.println("Please provide an output directory in options '-output'");
        return false;
    }
    
    private static boolean setOutputDirectory(String[][] options) {
    	for(String[] option: options) {
    		if(option.length == 2 && OUTPUT_DIRECTORY_COMMAND_LINE_OPTION_NAME.equals(option[0])) {
    			OUTPUT_DIRECTORY_PATH = Paths.get(option[1].trim());
    			return true;
    		}
    	}
    	return false;
    }
    
    private static void generateDocumentationForJavaClasses(RootDoc rootDoc) {
    	ClassDoc[] classDocs = rootDoc.classes();
    	for(ClassDoc classDoc: classDocs) {
    		generateDocumentationForJavaClass(classDoc);
    	}
    }
    
    private static void generateDocumentationForJavaClass(ClassDoc classDoc) {
    	JSONObject classJSONObject = new JSONObject();
    	classJSONObject.put("class_package", classDoc.containingPackage().name());
    	classJSONObject.put("class_name", classDoc.name());
    	classJSONObject.put("class_qualified_name", classDoc.qualifiedName());
    	classJSONObject.put("class_comments", classDoc.commentText());
    	
		JSONObject fieldsJSONObject = new JSONObject();
		FieldDoc[] fieldDocs = classDoc.fields();
		for(FieldDoc fieldDoc: fieldDocs) {
			fieldsJSONObject.put(fieldDoc.name(), fieldDoc.commentText());
		}
    	classJSONObject.put("class_fields", fieldsJSONObject);
    		
		JSONObject methodsJSONObject = new JSONObject();
		ConstructorDoc[] constructorDocs = classDoc.constructors();
		for(ConstructorDoc constructorDoc: constructorDocs) {
			String methodSignature = formMethodSignatureWithParameterNames(constructorDoc);
			methodsJSONObject.put(methodSignature.toString(), constructorDoc.commentText());
		}
		
		MethodDoc[] methodDocs = classDoc.methods();
		for(MethodDoc methodDoc: methodDocs) {
			String methodSignature = formMethodSignatureWithParameterNames(methodDoc);
			methodsJSONObject.put(methodSignature.toString(), methodDoc.commentText());
		}
    	classJSONObject.put("class_methods", methodsJSONObject);
    	
    	saveJSONClassDocumentation(classDoc.qualifiedName(), classJSONObject);
    }
    
    private static String formMethodSignatureWithParameterNames(ExecutableMemberDoc methodDoc) {
		StringBuilder methodSignature = new StringBuilder();
		String methodName = methodDoc.name();
		methodSignature.append(methodName);
		Parameter[] parameters = methodDoc.parameters();
		for(int index = 0; index < parameters.length; index++) {
			methodSignature.append("###");
			String parameterName = parameters[index].name();
			methodSignature.append(parameterName);
		}
		return methodSignature.toString();
    }
    
    private static String formMethodSignatureWithParameterTypes(ExecutableMemberDoc methodDoc) {
		StringBuilder methodSignature = new StringBuilder();
		String methodName = methodDoc.name();
		methodSignature.append(methodName);
		Parameter[] parameters = methodDoc.parameters();
		for(int index = 0; index < parameters.length; index++) {
			methodSignature.append("###");
			String parameterTypeName = parameters[index].type().simpleTypeName();
			methodSignature.append(parameterTypeName);
		}
		return methodSignature.toString();
    }
    
    private static String formMethodSignatureWithLineNumbers(ExecutableMemberDoc methodDoc) {
		return methodDoc.name() + "###" + methodDoc.position().line();
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