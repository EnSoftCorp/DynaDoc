package com.kcsl.dynadoc.wrapper;

import java.nio.file.Path;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Code;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Tbody;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tfoot;
import com.hp.gagawa.java.elements.Th;
import com.hp.gagawa.java.elements.Thead;
import com.hp.gagawa.java.elements.Tr;
import com.kcsl.dynadoc.data.JavaClass;
import com.kcsl.dynadoc.data.JavaMethod;
import com.kcsl.dynadoc.data.JavaMethodParameter;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.path.WorkingDirectoryCache;
import com.kcsl.dynadoc.utils.HTMLUtils;
import com.kcsl.dynadoc.utils.PathUtils;
import com.kcsl.dynadoc.utils.SaveUtils;
import com.kcsl.dynadoc.conf.FileNameTemplates;

public class ClassMethodsWrapper {
	
	private static final String METHODS_TABLE_JAVASCRIPT_FILE_NAME = "jquery-methods-table-script.js";
	
	private static final String METHODS_SECTION_HEADER = "Method Summary";
	
	private static final String METHODS_TABLE_NAME = "methods-table";
	
	private static final String [] METHODS_TABLE_HEADERS = { "Visibility", "Return", "Name", "Parameters", "Static", "Instance", "Concrete", "Deprecated", "External Use", "CFG", "Call", "Inra DDG"};

	private List<JavaMethod> methods;
	
	private WorkingDirectory workingDirectory;
	
	public ClassMethodsWrapper(JavaClass javaClass) {
		this.methods = javaClass.getMethods();
		this.methods.removeAll(javaClass.getConstructors());
		this.workingDirectory = WorkingDirectoryCache.getWorkingDirectory(javaClass.getClassNode());
	}
	
	private List<JavaMethod> getMethods() {
		return this.methods;
	}
	
	public WorkingDirectory getWorkingDirectory() {
		return this.workingDirectory;
	}
	
	private Path getPathToGraphFile(String fileName) {
		Path graphsDirectoryPath = PathUtils.getGraphsWorkingDirectory(this.getWorkingDirectory()).getPath();
		return graphsDirectoryPath.resolve(fileName);
	}
	
	private String getRelativePathStringToGraphFile(String fileName) {
		return PathUtils.getRelativePathStringToGraphsDirectory(this.getWorkingDirectory()) + fileName;
	}
	
	public Div wrap() {
		Div methodsTableDiv = new Div();
		methodsTableDiv.setCSSClass("card text-white bg-info mb-3");
		methodsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(METHODS_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId(METHODS_TABLE_NAME);
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: METHODS_TABLE_HEADERS) {
									Th column = new Th();
									column.appendText(headerText);
									tr.appendChild(column);
								}

								Th lastColumn = new Th();
								lastColumn.setStyle("display:none;");
								tr.appendChild(lastColumn);
							tHead.appendChild(tr);
						table.appendChild(tHead);
							Tbody tBody = new Tbody();
							List<JavaMethod> methods = this.getMethods();
							for(JavaMethod method: methods) {
								Tr methodRow = this.wrapMethod(method);
								tBody.appendChild(methodRow);
							}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < METHODS_TABLE_HEADERS.length; i++) {
									Th column = new Th();
									tr.appendChild(column);
								}
	
								lastColumn = new Th();
								lastColumn.setStyle("display:none;");
								tr.appendChild(lastColumn);							
							tFoot.appendChild(tr);
						table.appendChild(tFoot);
						
					cardContent.appendChild(table);
				
				cardHeader.appendChild(cardContent);
			
			methodsTableDiv.appendChild(cardHeader);
		return methodsTableDiv;
	}

	private Tr wrapMethod(JavaMethod method) {
		Tr row = new Tr();
		
		Td showHideColumn = this.wrapShowHideIcon();
		row.appendChild(showHideColumn);
		
		Td visibilityColumn = this.wrapVisibility(method);
		row.appendChild(visibilityColumn);
		
		Td returnTypeColumn = this.wrapReturnType(method);
		row.appendChild(returnTypeColumn);
		
		Td nameColumn = this.wrapName(method);
		row.appendChild(nameColumn);
		
		Td parametersColumn = this.wrapParameters(method);
		row.appendChild(parametersColumn);
		
		Td staticColumn = this.wrapStatic(method);
		row.appendChild(staticColumn);
		
		Td instanceColumn = this.wrapInstance(method);
		row.appendChild(instanceColumn);
				
		Td concreteMethod = this.wrapConcrete(method);
		row.appendChild(concreteMethod);
		
		Td deprecatedColumn = this.wrapDeprecated(method);
		row.appendChild(deprecatedColumn);
		
		Td usedOutsideClassColumn = this.wrapCalledOutsideContainingClass(method);
		row.appendChild(usedOutsideClassColumn);
		
		Td cfgColumn = this.wrapCFG(method);
		row.appendChild(cfgColumn);
		
		Td callGraphColumn = this.wrapCallGraph(method);
		row.appendChild(callGraphColumn);

		Td ddgColumn = this.wrapDDG(method);
		row.appendChild(ddgColumn);		

		Td commentColumn = this.wrapComments(method);
		row.appendChild(commentColumn);
		
		return row;
	}
	
	private Td wrapShowHideIcon() {
		Td td = new Td();
		td.setCSSClass("details-control");
		return td;
	}
	
	private Td wrapVisibility(JavaMethod method) {
		Td td = new Td();
		Code code = new Code();
		code.appendText(method.getVisibility());
		td.appendChild(code);
		return td;
	}
	
	private Td wrapReturnType(JavaMethod method) {
		Td td = new Td();
		Code code = new Code();
		Node typeNode = method.getReturnType();
		if(typeNode == null) {
			code.appendText("void");
		}else {
			String typeName = typeNode.getAttr(XCSG.name).toString();
			if(typeNode.taggedWith(XCSG.Primitive)) {
				code.appendText(typeName);
			}else {
				A link = new A();
				link.setHref("#");
				link.appendText(typeName);
				code.appendChild(link);
			}
		}
		td.appendChild(code);
		return td;
	}
	
	private Td wrapName(JavaMethod method) {
		Td td = new Td();
		Code code = new Code();
		code.appendText(method.getName());
		td.appendChild(code);
		return td;
	}
	
	private Td wrapParameters(JavaMethod method) {
		Td td = new Td();
		td.setStyle("white-space:nowrap");
		List<JavaMethodParameter> parameters = method.getParameters();
		for(int index = 0; index < parameters.size(); index++) {
			JavaMethodParameter parameter = parameters.get(index);
			String name = parameter.getName();
			Node typeNode = parameter.getTypeNode();
			String typeName = parameter.getTypeName();
			
			Code p = new Code();
			if(typeNode.taggedWith(XCSG.Primitive)) {
				p.appendText(typeName);
			}else {
				A link = new A();
				link.setHref("#");
				link.appendText(typeName);
				p.appendChild(link);
			}
			p.appendText(" " + name);
			td.appendChild(p);
			if(index < parameters.size() - 1) {
				td.appendChild(new Br());
			}
		}
		return td;
	}
	
	private Td wrapStatic(JavaMethod method) {
		Td td = new Td();
		if(method.staticMethod()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapInstance(JavaMethod method) {
		Td td = new Td();
		if(method.instanceMethod()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapConcrete(JavaMethod method) {
		Td td = new Td();
		if(method.concreteMethod()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapDeprecated(JavaMethod method) {
		Td td = new Td();
		if(method.deprecatedMethod()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapCalledOutsideContainingClass(JavaMethod method) {
		Td td = new Td();
		if(method.calledOutsideContainingClass()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapCFG(JavaMethod method) {
		Td td = new Td();
		A CFGlink = new A();
		String cfgFileName = String.format(FileNameTemplates.CFG_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
		Path cfgFilePath = this.getPathToGraphFile(cfgFileName);
		SaveUtils.saveGraph(cfgFilePath, method.getCFG(), method.getCFGMarkup());
		String relativePathToCFGGraph = this.getRelativePathStringToGraphFile(cfgFileName);
		CFGlink.setHref(relativePathToCFGGraph);
		CFGlink.setTarget("_blank");
		CFGlink.setAttribute("role", "button");
		CFGlink.setAttribute("class", "btn btn-success");
		CFGlink.appendText("Show");
		td.appendChild(CFGlink);
		return td;
	}
	
	private Td wrapCallGraph(JavaMethod method) {
		Td td = new Td();
		A link = new A();
		String callGraphFileName = String.format(FileNameTemplates.CALL_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
		Path callGraphFilePath = this.getPathToGraphFile(callGraphFileName);
		SaveUtils.saveGraph(callGraphFilePath, method.getCallGraph(), method.getCallGraphMarkup());
		String relativePathToCallGraph = this.getRelativePathStringToGraphFile(callGraphFileName);
		link.setHref(relativePathToCallGraph);
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-success");
		link.appendText("Show");
		td.appendChild(link);
		return td;
	}
	
	private Td wrapDDG(JavaMethod method) {
		Td td = new Td();
		A link = new A();
		String ddgFileName = String.format(FileNameTemplates.DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
		Path ddgFilePath = this.getPathToGraphFile(ddgFileName);
		SaveUtils.saveGraph(ddgFilePath, method.getDataDependenceGraph(), method.getDataDependencyGraphMarkup());
		String relativePathToDDG = this.getRelativePathStringToGraphFile(ddgFileName);
		link.setHref(relativePathToDDG);
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		td.appendChild(link);
		return td;
	}
	
	private Td wrapComments(JavaMethod method) {
		Td td = new Td();
		td.setAttribute("style", "display:none;");
			Div div = new Div();
			div.appendText(method.getComments());
		td.appendChild(div);
		return td;
	}
	
	private Img checkImg() {
		return HTMLUtils.checkImg(this.getWorkingDirectory());
	}
}
