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
import com.kcsl.dynadoc.constants.FileNameTemplateConstants;
import com.kcsl.dynadoc.data.JavaClass;
import com.kcsl.dynadoc.data.JavaMethod;
import com.kcsl.dynadoc.data.JavaMethodParameter;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.path.WorkingDirectoryCache;
import com.kcsl.dynadoc.utils.HTMLUtils;
import com.kcsl.dynadoc.utils.PathUtils;
import com.kcsl.dynadoc.utils.SaveUtils;

public class ClassConstructorsWrapper {
	
	private static final String CONSTRUCTORS_TABLE_JAVASCRIPT_FILE_NAME = "jquery-constructors-table-script.js";
	
	private static final String CONSTRUCTORS_SECTION_HEADER = "Constructor Summary";
	
	private static final String CONSTRCUTORS_TABLE_NAME = "constructor-table";
	
	private static final String [] CONSTRUCTORS_TABLE_HEADERS = { "Visibility", "Name", "Parameters", "Deprecated", "External Use", "CFG", "Call", "Intra DDG"};


	private List<JavaMethod> constrcutors;
	
	private WorkingDirectory workingDirectory;
	
	public ClassConstructorsWrapper(JavaClass javaClass) {
		this.constrcutors = javaClass.getConstructors();
		this.workingDirectory = WorkingDirectoryCache.getWorkingDirectory(javaClass.getClassNode());
	}
	
	private List<JavaMethod> getConstructors() {
		return this.constrcutors;
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
		Div constructorsTableDiv = new Div();
		constructorsTableDiv.setCSSClass("card text-white bg-primary mb-3");
		constructorsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(CONSTRUCTORS_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId(CONSTRCUTORS_TABLE_NAME);
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: CONSTRUCTORS_TABLE_HEADERS) {
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
							for(JavaMethod constructor: this.getConstructors()) {
								Tr constructorRow = this.wrapConstructor(constructor);
								tBody.appendChild(constructorRow);
							}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < CONSTRUCTORS_TABLE_HEADERS.length; i++) {
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
			
			constructorsTableDiv.appendChild(cardHeader);
		return constructorsTableDiv;
	}

	private Tr wrapConstructor(JavaMethod constructor) {
		Tr row = new Tr();
		
		Td showHideColumn = this.wrapShowHideIcon();
		row.appendChild(showHideColumn);
		
		Td visibilityColumn = this.wrapVisibility(constructor);
		row.appendChild(visibilityColumn);
		
		Td nameColumn = this.wrapName(constructor);
		row.appendChild(nameColumn);
		
		Td parametersColumn = this.wrapParameters(constructor);
		row.appendChild(parametersColumn);
		
		Td deprecatedColumn = this.wrapDeprecated(constructor);
		row.appendChild(deprecatedColumn);
		
		Td usedOutsideClassColumn = this.wrapCalledOutsideContainingClass(constructor);
		row.appendChild(usedOutsideClassColumn);
		
		Td cfgColumn = this.wrapCFG(constructor);
		row.appendChild(cfgColumn);
		
		Td callGraphColumn = this.wrapCallGraph(constructor);
		row.appendChild(callGraphColumn);

		Td ddgColumn = this.wrapDDG(constructor);
		row.appendChild(ddgColumn);		

		Td commentColumn = this.wrapComments(constructor);
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
		String cfgFileName = String.format(FileNameTemplateConstants.CFG_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
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
		String callGraphFileName = String.format(FileNameTemplateConstants.CALL_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
		Path callGraphPath = this.getPathToGraphFile(callGraphFileName);
		SaveUtils.saveGraph(callGraphPath, method.getCallGraph(), method.getCallGraphMarkup());
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
		String ddgFileName = String.format(FileNameTemplateConstants.DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, method.getSignature());
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
