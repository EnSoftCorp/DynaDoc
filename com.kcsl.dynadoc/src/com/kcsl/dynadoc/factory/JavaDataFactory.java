package com.kcsl.dynadoc.factory;

import java.util.HashMap;
import java.util.Map;

import com.kcsl.dynadoc.data.JavaClassData;
import com.kcsl.dynadoc.data.JavaFieldData;
import com.kcsl.dynadoc.data.JavaMethodData;
import com.kcsl.dynadoc.data.JavaPackageData;

public class JavaDataFactory {
	
	private static Map<String, JavaPackageData> fullyQualifiedPackageNameToJavaPackageDataMap = new HashMap<String, JavaPackageData>();
	private static Map<String, JavaClassData> fullyQualifiedClassNameToJavaClassDataMap = new HashMap<String, JavaClassData>();
	private static Map<String, JavaMethodData> fullyQualifiedMethodNameToJavaMethodDataMap = new HashMap<String, JavaMethodData>();
	private static Map<String, JavaFieldData> fullyQualifiedFieldNameToJavaFieldDataMap = new HashMap<String, JavaFieldData>();
	
	
	public static JavaPackageData createJavaPackageData(String fullyQualifiedPackageName) {
		if(fullyQualifiedPackageNameToJavaPackageDataMap.containsKey(fullyQualifiedPackageName)) {
			return fullyQualifiedPackageNameToJavaPackageDataMap.get(fullyQualifiedPackageName);
		}
		JavaPackageData javaPackageDataInstance = new JavaPackageData(fullyQualifiedPackageName);
		fullyQualifiedPackageNameToJavaPackageDataMap.put(fullyQualifiedPackageName, javaPackageDataInstance);
		return javaPackageDataInstance;
	}
	
	public static JavaClassData createJavaClassData(String fullyQualifiedClassName) {
		if(fullyQualifiedClassNameToJavaClassDataMap.containsKey(fullyQualifiedClassName)) {
			return fullyQualifiedClassNameToJavaClassDataMap.get(fullyQualifiedClassName);
		}
		JavaClassData javaClassDataInstance = new JavaClassData(fullyQualifiedClassName);
		fullyQualifiedClassNameToJavaClassDataMap.put(fullyQualifiedClassName, javaClassDataInstance);
		return javaClassDataInstance;
	}
	
	public static JavaMethodData createJavaMethodData(String fullyQualifiedMethodName) {
		if(fullyQualifiedMethodNameToJavaMethodDataMap.containsKey(fullyQualifiedMethodName)) {
			return fullyQualifiedMethodNameToJavaMethodDataMap.get(fullyQualifiedMethodName);
		}
		JavaMethodData javaMethodDataInstance = new JavaMethodData(fullyQualifiedMethodName);
		fullyQualifiedMethodNameToJavaMethodDataMap.put(fullyQualifiedMethodName, javaMethodDataInstance);
		return javaMethodDataInstance;
	}
	
	public static JavaFieldData createJavaFieldData(String fullyQualifiedFieldName) {
		if(fullyQualifiedFieldNameToJavaFieldDataMap.containsKey(fullyQualifiedFieldName)) {
			return fullyQualifiedFieldNameToJavaFieldDataMap.get(fullyQualifiedFieldName);
		}
		JavaFieldData javaFieldDataInstance = new JavaFieldData(fullyQualifiedFieldName);
		fullyQualifiedFieldNameToJavaFieldDataMap.put(fullyQualifiedFieldName, javaFieldDataInstance);
		return javaFieldDataInstance;
	}
}
