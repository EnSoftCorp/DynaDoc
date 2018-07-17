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
import org.json.simple.JSONArray;
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
	
	private static final String JAVA_DOC_METHOD_PARAMETER_TYPE_SEPARATOR = "@@@";
	
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
		classNode.putAttr(CodeMap.Attributes.Commnets, comment);
	}
	
	private void populateForMethods(JSONObject methodsObject, Q classQ) {
		Q classMethodsQ = Query.universe().edges(XCSG.Contains).successors(classQ).nodes(XCSG.Method);
		Q initMethods = classMethodsQ.selectNode(XCSG.name, "<init>").union(classMethodsQ.selectNode(XCSG.name, "<clinit>"));
		classMethodsQ = classMethodsQ.difference(initMethods);
		AtlasSet<Node> methodNdoes = classMethodsQ.eval().nodes();
		for(Node methodNode: methodNdoes) {
			String methodSignature = this.formJavaDocMethodSignatureFromMethodNode(methodNode);
			JSONArray jsonArray = (JSONArray) methodsObject.get(methodSignature);
			if(jsonArray == null) {
				Log.warning("Missing documentation for method with signature: " + methodSignature);
				continue;
			}
			
			boolean isDeprecated = (boolean) jsonArray.get(0);
			if(isDeprecated) {
				methodNode.tag(CodeMap.Tags.Deprecated);
			}
			
			String comment = (String) jsonArray.get(1);
			methodNode.putAttr(CodeMap.Attributes.Commnets, comment);
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
			methodSignature.append(JAVA_DOC_METHOD_PARAMETER_TYPE_SEPARATOR);
			methodSignature.append(getParamterSimplyTypeName(parameter));
		}
		
		return methodSignature.toString();
	}
	
	private String getParamterSimplyTypeName(Node parameter) {
		Node typeNode = Query.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameter)).nodes(XCSG.Type).eval().nodes().one();
		
		// Remove the array or bracket notations from the signature. That occurs whenever we have primitvie type (e.g., byte[], int[])
		String typeName = typeNode.getAttr(XCSG.name).toString();
		int indextOfArrayBracket = typeName.indexOf("[");
		int indexOfMapCarrent = typeName.indexOf("<");
		if(indextOfArrayBracket >= 0) {
			typeName = typeName.substring(0, indextOfArrayBracket);
		}
		
		if(indexOfMapCarrent >= 0) {
			typeName = typeName.substring(0, indexOfMapCarrent);
		}
		
		return typeName.trim();
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
			JSONArray jsonArray = (JSONArray) fieldsObject.get(name);
			if(jsonArray == null) {
				Log.warning("Missing documentation for field with signature: " + name);
				continue;
			}
			
			boolean isDeprecated = (boolean) jsonArray.get(0);
			if(isDeprecated) {
				field.tag(CodeMap.Tags.Deprecated);
			}
			
			String comment = (String) jsonArray.get(1);
			field.putAttr(CodeMap.Attributes.Commnets, comment);
		}
	}
	
	public Q getProjectQ() {
		return Common.toQ(this.getProjectNode());
	}
	
	public Node getProjectNode() {
		return this.projectNode;
	}
	
}
