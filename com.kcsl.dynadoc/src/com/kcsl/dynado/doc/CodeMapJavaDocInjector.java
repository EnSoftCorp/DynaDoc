package com.kcsl.dynado.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.kcsl.dynadoc.Configurations;

import static com.kcsl.dynado.doc.JavaDocAttributes.CodeMap;
import static com.kcsl.dynado.doc.JavaDocAttributes.JSONData;

public class CodeMapJavaDocInjector {
	
	private static final String JAVA_DOC_METHOD_SIGNATURE_SEPARATOR = "###";
	
	private Node projectNode;
	
	public CodeMapJavaDocInjector(Node projectNode) {
		this.projectNode = projectNode;
	}
	
	public void populate() {
		Collection<File> javaDocFiles = this.fetchJavaDocFiles();
		for(File javaDocFile: javaDocFiles) {
			this.parseAndPopulateJavaDoc(javaDocFile);
		}
	}
	
	private Collection<File> fetchJavaDocFiles() {
		File javaDocDirectoryPathFile = Configurations.getOutputJavaDocDirectoryPath().toFile();
		return FileUtils.listFiles(javaDocDirectoryPathFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	}
	
	private void parseAndPopulateJavaDoc(File javaDocFile) {
		JSONParser jsonParser = new JSONParser();
		try {
			Object obj = jsonParser.parse(new FileReader(javaDocFile));
			JSONObject jsonObject = (JSONObject) obj;
			String packageName = (String) jsonObject.get(JSONData.ClassPackage);
			String className = (String) jsonObject.get(JSONData.ClassName);
			
			Q classQ = this.findClassQ(packageName, className);
			
			String classComment = (String) jsonObject.get(JSONData.ClassComments);
			this.populateForClass(classComment, classQ);
			
			JSONObject methodsObject = (JSONObject) jsonObject.get(JSONData.ClassMethods);
			this.populateForMethods(methodsObject, classQ);
			
			JSONObject fieldsObject = (JSONObject) jsonObject.get(JSONData.ClassFields);
			this.populateForFields(fieldsObject, classQ);
			
		} catch (FileNotFoundException e) {
			Log.error("The Java Doc JSON file in not found: " + javaDocFile.getAbsolutePath(), e);
		} catch (IOException e) {
			Log.error("I/O Error while parsing the Java Doc JSON file: " + javaDocFile.getAbsolutePath(), e);
		} catch (ParseException e) {
			Log.error("Error while parsing the Java Doc JSON file: " + javaDocFile.getAbsolutePath(), e);
		}
		
	}
	
	private Q findClassQ(String packageName, String className) {
		String[] classNameParts = StringUtils.split(className, ".");
		if(classNameParts.length == 1) {
			return Common.typeSelect(packageName, className);
		}
		Q classQ = Common.typeSelect(packageName, classNameParts[0]);
		for(int i = 1; i < classNameParts.length; i++) {
			classQ = classQ.successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Type).selectNode(XCSG.name, classNameParts[1]);
		}
		return classQ;
	}
	
	private void populateForClass(String comment, Q classQ) {
		Node classNode = classQ.eval().nodes().one();
		classNode.putAttr(CodeMap.Commnets, comment);
	}
	
	private void populateForMethods(JSONObject methodsObject, Q classQ) {
		Q classMethodsQ = classQ.successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Method);
		AtlasSet<Node> methodNdoes = classMethodsQ.eval().nodes();
		for(Node methodNode: methodNdoes) {
			String methodSignature = this.formJavaDocMethodSignatureFromMethodNode(methodNode);
			String comment = (String) methodsObject.get(methodSignature);
			methodNode.putAttr(CodeMap.Commnets, comment);
		}
	}
	
	private String formJavaDocMethodSignatureFromMethodNode(Node methodNode) {
		StringBuilder methodSignature = new StringBuilder();
		String methodName = methodNode.getAttr(XCSG.name).toString();
		methodSignature.append(methodName);
		
		List<Node> parameters = this.getSortedParameterListForMethodNode(methodNode);
		for(Node parameter: parameters) {
			methodSignature.append(JAVA_DOC_METHOD_SIGNATURE_SEPARATOR);
			methodSignature.append(parameter.getAttr(XCSG.name));
		}
		
		return methodSignature.toString();
	}
	
	private List<Node> getSortedParameterListForMethodNode(Node methodNode) {
		AtlasSet<Node> parameters = CommonQueries.functionParameter(Common.toQ(methodNode)).eval().nodes();
		List<Node> parameterList = new ArrayList<Node>();
		for(Node parameter: parameters) {
			parameterList.add(parameter);
		}
		Collections.sort(parameterList, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				int indexO1 = (int) o1.getAttr(XCSG.parameterIndex);
				int indexO2 = (int) o2.getAttr(XCSG.parameterIndex);
				return indexO1 - indexO2;
			}
		});
		return parameterList;
	}
	
	private void populateForFields(JSONObject fieldsObject, Q classQ) {
		Q classFieldsQ = classQ.successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Field);
		AtlasSet<Node> fields = classFieldsQ.eval().nodes();
		for(Node field: fields) {
			String name = field.getAttr(XCSG.name).toString();
			String comment = (String) fieldsObject.get(name);
			field.putAttr(CodeMap.Commnets, comment);
		}
	}
	
	public Q getProjectQ() {
		return Common.toQ(this.getProjectNode());
	}
	
	public Node getProjectNode() {
		return this.projectNode;
	}
	
}
