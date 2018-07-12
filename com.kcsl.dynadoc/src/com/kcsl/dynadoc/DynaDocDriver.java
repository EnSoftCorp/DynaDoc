package com.kcsl.dynadoc;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Code;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tr;
import com.kcsl.dynadoc.data.JavaMethodParameter;
import com.kcsl.dynadoc.factory.JavaDataFactory;
import com.kcsl.dynadoc.generator.ClassDocumentationGenerator;

public class DynaDocDriver { 
	
	public static void test() {
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Path storeDirectoryPath = Paths.get("/Users/ahmedtamrawi/Desktop", "test"); 
		ClassDocumentationGenerator classDocumentationGenerator = new ClassDocumentationGenerator(classQ, storeDirectoryPath);
		classDocumentationGenerator.generate();
	}
	
	public static void generateDocumentation(String projectName) {
		Log.info("Finding software artificats in project: " + projectName);
		Q projectQ = Common.universe().nodes(XCSG.Project).selectNode(XCSG.name, projectName);
		Q containsEdges = Common.universe().edges(XCSG.Contains);
		Q containedNodes = containsEdges.forward(projectQ).nodes(XCSG.Node);
		
		// Iterate through project packages.
		Q packagesQ = containedNodes.nodes(XCSG.Package);
		AtlasSet<Node> packageNodes = packagesQ.eval().nodes();
		Log.info("Numer of packages in project: " + packageNodes.size());
		for(Node packageNode : packageNodes) {
			JavaDataFactory.createJavaPackageData(packageNode.getAttr(XCSG.name).toString());
		}
		
		// Iterate through project classes.
		Q classesQ = containedNodes.nodes(XCSG.Java.Class);
		AtlasSet<Node> classNodes = classesQ.eval().nodes();
		Log.info("Number of classes in project: " + classNodes.size());
		for(Node classNode : classNodes) {
			Node containingPackageNode = CommonQueries.getContainingNode(classNode, XCSG.Package);
			assert containingPackageNode != null && !containingPackageNode.getAttr(XCSG.name).toString().isEmpty();
			String fullyQualifiedClassName = containingPackageNode.getAttr(XCSG.name) + "." + classNode.getAttr(XCSG.name);
			JavaDataFactory.createJavaClassData(fullyQualifiedClassName);
		}
		
		// Iterate through project methods.
//		Q methodsQ = containedNodes.nodes(XCSG.Method);
//		AtlasSet<Node> methodNodes = methodsQ.eval().nodes();
//		Log.info("Number of methods in project: " + methodNodes.size());
//		for(Node methodNode : methodNodes) {
//			Node containingClassNode = CommonQueries.getContainingNode(methodNode, XCSG.Java.Class);
//			Node containingPackageNode = CommonQueries.getContainingNode(methodNode, XCSG.Package);
//			String fullyQualifiedMethodName = containingPackageNode.getAttr(XCSG.name) + "." + containingClassNode.getAttr(XCSG.name) + "." + methodNode.getAttr(XCSG.name);
//			Q parametersQ = CommonQueries.functionParameter(Common.toQ(methodNode));
//			AtlasSet<Node> parameterNodes = parametersQ.eval().nodes();
//			JavaMethodParameter[] javaMethodParameters = new JavaMethodParameter[(int)parameterNodes.size()];
//			for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
//				Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
//				Node typeNode = Common.universe().edges(XCSG.TypeOf).predecessors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
//				String fullyQualifiedClassName = "";
//				if(typeNode.taggedWith(XCSG.Primitive)) {
//					fullyQualifiedClassName = typeNode.getAttr(XCSG.name).toString();
//				}else {
//					containingPackageNode = CommonQueries.getContainingNode(typeNode, XCSG.Package);
//					fullyQualifiedClassName = containingPackageNode.getAttr(XCSG.name) + "." + typeNode.getAttr(XCSG.name);
//				}
//				javaMethodParameters[parameterIndex] = JavaDataFactory.createJavaMethodParameter(parameterNode.getAttr(XCSG.name).toString(), fullyQualifiedClassName, parameterIndex);
//			}
//			JavaDataFactory.createJavaMethodData(fullyQualifiedMethodName, javaMethodParameters);
//		}
		
		// Iterate through project Fields.
	}
	
	public static Q getClassContext(Q classQ) {
		Q containsEdges = Common.universe().edges(XCSG.Contains);
		Q innerClassesQ = containsEdges.forward(classQ).nodes(XCSG.Java.InnerClass);
		//DisplayUtil.displayGraph(innerClassesQ.eval());
		Q innerClassesContainerQ = containsEdges.forward(innerClassesQ);
		Q context = containsEdges.difference(innerClassesContainerQ);
		return context;
	}
	
	public static void stat() {
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Q containsEdges = getClassContext(classQ);
		
		Q methodsQ = containsEdges.forward(classQ).nodes(XCSG.Method);
		AtlasSet<Node> methodNodes = methodsQ.eval().nodes();
		Log.info("Method Count: " + methodNodes.size());
		
		Log.info("Constructors Count: " + methodNodes.tagged(XCSG.Constructor).size());
		
		Q fieldsQ = containsEdges.forward(classQ).nodes(XCSG.Field);
		AtlasSet<Node> fieldNodes = fieldsQ.eval().nodes();
		Log.info("Field Count: " + fieldNodes.size());
		
		DisplayUtil.displayGraph(CommonQueries.interactions(methodsQ, methodsQ, XCSG.Call).eval());
	}
	
	public static void classInteraction(Q selectedClass) {
		Q containsEdges = Common.universe().edges(XCSG.Contains);
		Q callEdges = Common.universe().edges(XCSG.Call);
		
		Q otherClasses = Common.universe().nodes(XCSG.Java.Class).difference(selectedClass);
		
		DisplayUtil.displayGraph(CommonQueries.interactions(selectedClass, otherClasses, XCSG.Call).eval());
		
//		Q methodsQ = containsEdges.forward(selectedClass).nodes(XCSG.Method);
//		callEdges.predecessors(methodsQ)
//		
//		Q fieldsQ = containsEdges.forward(selectedClass).nodes(XCSG.Field);
	}
	
	public static void w() throws IOException {
		FileWriter writer = new FileWriter(new File("/Users/ahmedtamrawi/Desktop/log.txt"));
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		
		Q containsEdges = getClassContext(classQ);
		
		Q methodsQ = containsEdges.forward(classQ).nodes(XCSG.Method);
		AtlasSet<Node> methodNodes = methodsQ.eval().nodes().tagged(XCSG.Constructor);
		for(Node cons : methodNodes) {
			writer.write("********************\n");
			writer.write(cons.getAttr(XCSG.name).toString() + "\n");
			Q parametersQ = CommonQueries.functionParameter(Common.toQ(cons));
			AtlasSet<Node> parameterNodes = parametersQ.eval().nodes();
			DisplayUtil.displayGraph(Common.toGraph(parameterNodes));
			for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
				Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
				Node typeNode = Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
				String fullyQualifiedClassName = "";
				if(typeNode.taggedWith(XCSG.Primitive)) {
					fullyQualifiedClassName = typeNode.getAttr(XCSG.name).toString();
				}else {
					Node containingPackageNode = CommonQueries.getContainingNode(typeNode, XCSG.Package);
					fullyQualifiedClassName = containingPackageNode.getAttr(XCSG.name) + "." + typeNode.getAttr(XCSG.name);
				}
				writer.write(fullyQualifiedClassName + "\t" + parameterNode.getAttr(XCSG.name) + "\n");
			}
			writer.write("********************\n");
		}
		writer.flush();
		writer.close();
		
	}
	
	/**
	 * Constructs the Class Hierarchy
	 */
	public static void a() {
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Q projectQ = Common.universe().nodes(XCSG.Project).selectNode(XCSG.name, "ApachePOI");
		Q containedNodesQ = Common.universe().edges(XCSG.Contains).forward(projectQ);
		Q hierarchy = Common.universe().edges(XCSG.Supertype).forward(classQ).intersection(containedNodesQ).induce(Common.universe().edges(XCSG.Supertype));
		DisplayUtil.displayGraph(Common.extend(hierarchy, XCSG.Contains).eval());
	}
	
	/**
	 * Logs Java methods in File
	 *          <th>Accessor</th>
                <th>Static</th>
                <th>Return Type</th>
                <th>Name</th>
                <th>Parameters</th>
                <th>Inherited</th>
                <th>Abstract</th>
                <th>Override</th>
                <th>Externally Used</th>
                <th>Call Hierarcy</th>
                <th>Usage Examples</th>
                <th style="display:none;"></th>
	 * @throws IOException
	 */
	public static void b() throws IOException {
		FileWriter writer = new FileWriter(new File("/Users/ahmedtamrawi/Desktop/log.txt"));
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		
		Q containsEdges = getClassContext(classQ);
		
		Q methodsQ = containsEdges.forward(classQ).nodes(XCSG.Method);
		methodsQ = methodsQ.difference(methodsQ.nodes(XCSG.Constructor));
		AtlasSet<Node> methodNodes = methodsQ.eval().nodes();
		for(Node cons : methodNodes) {
			writer.write("********************\n");
			writer.write(cons.getAttr(XCSG.name).toString() + "\n");
			Q parametersQ = CommonQueries.functionParameter(Common.toQ(cons));
			AtlasSet<Node> parameterNodes = parametersQ.eval().nodes();
			DisplayUtil.displayGraph(Common.toGraph(parameterNodes));
			for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
				Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
				Node typeNode = Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
				String fullyQualifiedClassName = "";
				if(typeNode.taggedWith(XCSG.Primitive)) {
					fullyQualifiedClassName = typeNode.getAttr(XCSG.name).toString();
				}else {
					Node containingPackageNode = CommonQueries.getContainingNode(typeNode, XCSG.Package);
					fullyQualifiedClassName = containingPackageNode.getAttr(XCSG.name) + "." + typeNode.getAttr(XCSG.name);
				}
				writer.write(fullyQualifiedClassName + "\t" + parameterNode.getAttr(XCSG.name) + "\n");
			}
			writer.write("********************\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static void constructFieldsTable() {
		Html html = new Html();
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Q containsEdges = getClassContext(classQ);
		Q fieldsQ = containsEdges.forward(classQ).nodes(XCSG.Field);
		AtlasSet<Node> fieldNodes = fieldsQ.eval().nodes();
		for(Node fieldNode : fieldNodes) {
			Tr fieldRow = constructFieldRow(fieldNode);
			html.appendChild(fieldRow);
		}
		try {
			File output = new File("/Users/ahmedtamrawi/Desktop/log.txt");
			PrintWriter out = new PrintWriter(new FileOutputStream(output));
			out.println(html.write());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static Tr constructFieldRow(Node fieldNode) {
		List<Td> columns = new ArrayList<Td>();
		
		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setAttribute("class", "details-control");
		columns.add(expandColumn);
		
		// Accessor Column
		Td accessorColumn = new Td();
		Code code = new Code();
		if(fieldNode.taggedWith(XCSG.publicVisibility)) {
			code.appendText("public");
		}else if(fieldNode.taggedWith(XCSG.privateVisibility)) {
			code.appendText("private");
		}else if(fieldNode.taggedWith(XCSG.protectedPackageVisibility)) {
			code.appendText("protected");
		}else {
			code.appendText("public");
		}
		accessorColumn.appendChild(code);
		columns.add(accessorColumn);
		
		// Static Column
		Td staticColumn = new Td();
		if(fieldNode.taggedWith(XCSG.ClassVariable) || fieldNode.taggedWith("static")) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			staticColumn.appendChild(tickImage);
		}
		columns.add(staticColumn);
		
		// Final Column
		Td finalColumn = new Td();
		if(fieldNode.taggedWith(XCSG.immutable) || fieldNode.taggedWith("final")) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			finalColumn.appendChild(tickImage);
		}
		columns.add(finalColumn);
		
		// Type Column
		Td typeColumn = new Td();
		code = new Code();
		Node type = Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(fieldNode)).nodes(XCSG.Type).eval().nodes().one();
		if(type.taggedWith(XCSG.Primitive)) {
			code.appendText(CommonQueries.getQualifiedTypeName(type));
		}else {
			String qualifiedName = CommonQueries.getQualifiedTypeName(type);
			A link = new A();
			link.setHref("#");
			link.appendText(qualifiedName);
			code.appendChild(link);
		}
		typeColumn.appendChild(code);
		columns.add(typeColumn);
		
		// Name Column
		Td nameColumn = new Td();
		code = new Code();
		code.appendText(fieldNode.getAttr(XCSG.name).toString());
		nameColumn.appendChild(code);
		columns.add(nameColumn);
		
		// Externally Used Column
		Td externalColumn = new Td();
		Q currentContainingClass = Common.edges(XCSG.Contains).predecessors(Common.toQ(fieldNode)).nodes(XCSG.Java.Class);
		Q forwardStepDataFlow = Common.universe().edges(XCSG.DataFlow_Edge).forwardStep(Common.toQ(fieldNode));
		Q reverseStepDataFlow = Common.universe().edges(XCSG.DataFlow_Edge).reverseStep(Common.toQ(fieldNode));
		Q surroundingDataFlowOneStep = forwardStepDataFlow.union(reverseStepDataFlow);
		Q extendedOnContains = Common.extend(surroundingDataFlowOneStep, XCSG.Contains);
		Q containingClassesQ = extendedOnContains.nodes(XCSG.Java.Class);
		containingClassesQ = containingClassesQ.difference(currentContainingClass);
		if(!containingClassesQ.eval().nodes().isEmpty()) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			externalColumn.appendChild(tickImage);
		}
		columns.add(externalColumn);
		
		// Usage Example Column
		Td usageExampleColumn = new Td();
		A link = new A();
		link.setHref("#");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		usageExampleColumn.appendChild(link);
		columns.add(usageExampleColumn);
		
		// User Comments Column
		Td userCommentsColumn = new Td();
		userCommentsColumn.setAttribute("style", "display:none;");
		// TODO
		Div container = new Div();
		userCommentsColumn.appendChild(container);
		columns.add(userCommentsColumn);
		
		Tr methodRow = new Tr();
		for(Td column : columns) {
			methodRow.appendChild(column);
		}
		return methodRow;
	}
	
	public static void constructConstructorMethodsTable() {
		Html html = new Html();
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Q containsEdges = getClassContext(classQ);
		Q methodsQ = containsEdges.forward(classQ).nodes(XCSG.Constructor);
		AtlasSet<Node> methodNodes = methodsQ.eval().nodes();
		for(Node methodNode : methodNodes) {
			Tr methodRow = constructMethodRow(methodNode);
			html.appendChild(methodRow);
		}
		try {
			File output = new File("/Users/ahmedtamrawi/Desktop/log.txt");
			PrintWriter out = new PrintWriter(new FileOutputStream(output));
			out.println(html.write());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void constructMethodsTable() {
		Html html = new Html();
		Q classQ  = Common.universe().nodes(XCSG.Java.Class).selectNode(XCSG.name, "XSSFWorkbook");
		Q containsEdges = getClassContext(classQ);
		Q methodsQ = containsEdges.forward(classQ).nodes(XCSG.Method);
		methodsQ = methodsQ.difference(methodsQ.nodes(XCSG.Constructor));
		AtlasSet<Node> methodNodes = methodsQ.eval().nodes();
		for(Node methodNode : methodNodes) {
			Tr methodRow = constructMethodRow(methodNode);
			html.appendChild(methodRow);
		}
		try {
			File output = new File("/Users/ahmedtamrawi/Desktop/log.txt");
			PrintWriter out = new PrintWriter(new FileOutputStream(output));
			out.println(html.write());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Tr constructMethodRow(Node methodNode) {
		List<Td> columns = new ArrayList<Td>();
		
		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setAttribute("class", "details-control");
		columns.add(expandColumn);
		
		// Accessor Column
		Td accessorColumn = new Td();
		Code code = new Code();
		if(methodNode.taggedWith(XCSG.publicVisibility)) {
			code.appendText("public");
		}else if(methodNode.taggedWith(XCSG.privateVisibility)) {
			code.appendText("private");
		}else if(methodNode.taggedWith(XCSG.protectedPackageVisibility)) {
			code.appendText("protected");
		}else {
			code.appendText("public");
		}
		accessorColumn.appendChild(code);
		columns.add(accessorColumn);
		
		// Static Column
		Td staticColumn = new Td();
		if(methodNode.taggedWith(XCSG.ClassMethod) || methodNode.taggedWith("static")) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			staticColumn.appendChild(tickImage);
		}
		columns.add(staticColumn);
		
		// Return Type Column
		Td returnTypeColumn = new Td();
		code = new Code();
		Node type = Common.universe().edges(XCSG.Returns).successors(Common.toQ(methodNode)).nodes(XCSG.Type).eval().nodes().one();
		if(type == null) {
			code.appendText("void");
		}else {
			if(type.taggedWith(XCSG.Primitive)) {
				code.appendText(CommonQueries.getQualifiedTypeName(type));
			}else {
				String qualifiedName = CommonQueries.getQualifiedTypeName(type);
				A link = new A();
				link.setHref("#");
				link.appendText(qualifiedName);
				code.appendChild(link);
			}
		}
		returnTypeColumn.appendChild(code);
		columns.add(returnTypeColumn);
		
		// Name Column
		Td nameColumn = new Td();
		code = new Code();
		code.appendText(methodNode.getAttr(XCSG.name).toString());
		nameColumn.appendChild(code);
		columns.add(nameColumn);
		
		// Parameters Column
		Td parametersColumn = new Td();
		
		Q parametersQ = CommonQueries.functionParameter(Common.toQ(methodNode));
		AtlasSet<Node> parameterNodes = parametersQ.eval().nodes();
		for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
			Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
			Node typeNode = Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
			String fullyQualifiedClassName = CommonQueries.getQualifiedTypeName(typeNode);
			
			// Parameter (parameterIndex)
			Code parameter1 = new Code();
			if(typeNode.taggedWith(XCSG.Primitive)) {
				parameter1.appendText(fullyQualifiedClassName);
			}else {
				A link1 = new A();
				link1.setHref("#");
				link1.appendText(fullyQualifiedClassName);
				parameter1.appendChild(link1);
			}
			parameter1.appendText(" " + parameterNode.getAttr(XCSG.name));
			parametersColumn.appendChild(parameter1);
			parametersColumn.appendChild(new Br());
		}
		columns.add(parametersColumn);
		
		// Inherited
		Td inheritedColumn = new Td();
		//TODO
//		Img tickImage = new Img("", "./resources/check.svg");
//		tickImage.setWidth("25");
//		inheritedColumn.appendChild(tickImage);
		columns.add(inheritedColumn);
		
		// Abstract
		Td abstractColumn = new Td();
		if(methodNode.taggedWith(XCSG.abstractMethod)) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			abstractColumn.appendChild(tickImage);
		}
		columns.add(abstractColumn);
		
		// Override
		Td overrideColumn = new Td();
		// TODO
//		Img tickImage = new Img("", "./resources/check.svg");
//		tickImage.setWidth("25");
//		overrideColumn.appendChild(tickImage);
		columns.add(overrideColumn);
		
		// Externally Used Column
		Td externalColumn = new Td();
		Q currentContainingClass = Common.universe().edges(XCSG.Contains).predecessors(Common.toQ(methodNode)).nodes(XCSG.Java.Class);
		Q callEdges = Common.universe().edges(XCSG.Call);
		Q callers = callEdges.predecessors(Common.toQ(methodNode));
		Q containsEdges = Common.universe().edges(XCSG.Contains);
		Q callerClasses = containsEdges.reverse(callers).nodes(XCSG.Java.Class);
		callerClasses = callerClasses.difference(currentContainingClass);
		if(!callerClasses.eval().nodes().isEmpty()) {
			Img tickImage = new Img("", "./resources/check.svg");
			tickImage.setWidth("25");
			externalColumn.appendChild(tickImage);
		}
		columns.add(externalColumn);
		
		// Call Hierarchy Column
		Td callColumn = new Td();
		A link = new A();
		Q callGraphQ = callEdges.forwardStep(Common.toQ(methodNode)).union(callEdges.reverseStep(Common.toQ(methodNode)));
		callGraphQ = Common.extend(callGraphQ, XCSG.Contains);
		String callGraphFileName = CommonQueries.getQualifiedFunctionName(methodNode) + "-" + methodNode.getAttr("##signature") + "-call.svg";
		File callGraphFile = new File("/Users/ahmedtamrawi/Dropbox/DySDoc-workshop/notes/AT/DynaDoc-Example/images/" + callGraphFileName);
		Markup markup = new Markup();
		markup.set(methodNode, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		SaveUtil.saveGraph(callGraphFile, callGraphQ.eval(), markup);
		link.setHref("./images/" + callGraphFileName);
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-success");
		link.appendText("Show");
		callColumn.appendChild(link);
		columns.add(callColumn);
		
		// Usage Example Column
		Td usageExampleColumn = new Td();
		link = new A();
		link.setHref("#");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		usageExampleColumn.appendChild(link);
		columns.add(usageExampleColumn);
		
		// User Comments Column
		Td userCommentsColumn = new Td();
		userCommentsColumn.setAttribute("style", "display:none;");
		// TODO
		Div container = new Div();
		userCommentsColumn.appendChild(container);
		columns.add(userCommentsColumn);
		
		Tr methodRow = new Tr();
		for(Td column : columns) {
			methodRow.appendChild(column);
		}
		return methodRow;
	}
}
