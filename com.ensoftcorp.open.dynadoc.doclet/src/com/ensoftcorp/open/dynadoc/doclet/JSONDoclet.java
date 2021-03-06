package com.ensoftcorp.open.dynadoc.doclet;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.javadoc.*;

public final class JSONDoclet {
	
	private static final String OUTPUT_DIRECTORY_COMMAND_LINE_OPTION_NAME = "-output";
	
	private static final String DEPRECATED_TAG = "@deprecated";
	
	private static final String DEPRECATED_TAG_TYPE = "java.lang.Deprecated";
	
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
    	ClassDoc[] classDocs = rootDoc.specifiedClasses();
    	for(ClassDoc classDoc: classDocs) {
    		generateDocumentationForJavaClass(classDoc);
    	}
    }
    
    @SuppressWarnings("unchecked")
	private static void generateDocumentationForJavaClass(ClassDoc classDoc) {
    	JSONObject classJSONObject = new JSONObject();
    	classJSONObject.put(JavaDocConstants.CLASS_PACKAGE_NAME_ATTRIBUTE, classDoc.containingPackage().name());
    	classJSONObject.put(JavaDocConstants.CLASS_NAME_ATTRIBUTE, classDoc.name());
    	classJSONObject.put(JavaDocConstants.CLASS_QUALIFIED_NAME_ATTRIBUTE, classDoc.qualifiedName());
    	classJSONObject.put(JavaDocConstants.CLASS_COMMENTS_ATTRIBUTE, classDoc.commentText());
    	
		JSONObject fieldsJSONObject = new JSONObject();
		parseFields(classDoc.fields(), fieldsJSONObject);
    	classJSONObject.put(JavaDocConstants.CLASS_FIELDS_ATTRIBUTE, fieldsJSONObject);
    		
		JSONObject methodsJSONObject = new JSONObject();
		parseMethods(classDoc.constructors(), methodsJSONObject);
		parseMethods(classDoc.methods(), methodsJSONObject);
    	classJSONObject.put(JavaDocConstants.CLASS_METHODS_ATTRIBUTE, methodsJSONObject);
    	
    	saveJSONClassDocumentation(classDoc.qualifiedName(), classJSONObject);
    }
    
    @SuppressWarnings("unchecked")
    private static void parseFields(FieldDoc[] fieldDocs, JSONObject fieldsJSONObject) {
		for(FieldDoc fieldDoc: fieldDocs) {
			JSONArray fieldProperties = new JSONArray();
			boolean isDeprecated = isDeprecated(fieldDoc);
			fieldProperties.add(isDeprecated);
			fieldProperties.add(fieldDoc.commentText());
			fieldsJSONObject.put(fieldDoc.name(), fieldProperties);
		}
    }
    
    @SuppressWarnings("unchecked")
    private static void parseMethods(ExecutableMemberDoc[] methodDocs, JSONObject methodsJSONObject) {
		for(ExecutableMemberDoc methodDoc: methodDocs) {
			JSONArray methodsProperties = new JSONArray();
			String methodSignature = formMethodSignature(methodDoc);
			boolean isDeprecated = isDeprecated(methodDoc);
			methodsProperties.add(isDeprecated);
			methodsProperties.add(methodDoc.commentText());
			methodsJSONObject.put(methodSignature, methodsProperties);
		}
    }
    
    private static String formMethodSignature(ExecutableMemberDoc methodDoc) {
		StringBuilder methodSignature = new StringBuilder();
		String methodName = methodDoc.name();
		methodSignature.append(methodName);
		Parameter[] parameters = methodDoc.parameters();
		for(int index = 0; index < parameters.length; index++) {
			methodSignature.append(JavaDocConstants.JAVA_DOC_METHOD_SIGNATURE_SEPARATOR);
			String parameterName = parameters[index].name();
			methodSignature.append(parameterName);
			methodSignature.append(JavaDocConstants.JAVA_DOC_METHOD_PARAMETER_TYPE_SEPARATOR);
			methodSignature.append(parameters[index].type().simpleTypeName());
		}
		return methodSignature.toString();
    }
    
    private static boolean isDeprecated(ProgramElementDoc programElementDoc) {
    	AnnotationDesc[] annotations = programElementDoc.annotations();
		for (AnnotationDesc annotation : annotations) {
			try {
				if (annotation.annotationType().qualifiedTypeName().equals(DEPRECATED_TAG_TYPE)) {
					return true;
				}
			} catch (RuntimeException e) {
				System.err.println(annotation + " has invalid javadoc: " + e.getClass() + ": " + e.getMessage());
			}
		}
		Tag[] tags = programElementDoc.tags();
		for(Tag tag: tags) {
			if(DEPRECATED_TAG.equals(tag.name().toLowerCase())) {
				return true;
			}
		}
		return false;
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