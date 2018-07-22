package com.kcsl.dynadoc.wrapper;

import java.nio.file.Path;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.hp.gagawa.java.elements.A;
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
import com.kcsl.dynadoc.data.JavaClassField;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.path.WorkingDirectoryCache;
import com.kcsl.dynadoc.utils.HTMLUtils;
import com.kcsl.dynadoc.utils.PathUtils;
import com.kcsl.dynadoc.utils.SaveUtils;

public class ClassFieldsWrapper {
	
	private static final String FIELDS_TABLE_JAVASCRIPT_FILE_NAME = "jquery-fields-table-script.js";
	
	private static final String FIELDS_SECTION_HEADER = "Field Summary";
	
	private static final String FIELDS_TABLE_NAME = "fields-table";

	private static final String [] FIELDS_TABLE_HEADERS = { "Visibility", "Type", "Name", "Static", "Instance", "Final", "Deprecated", "External Use", "Inter DDG"};


	private List<JavaClassField> fields;
	
	private WorkingDirectory workingDirectory;
	
	public ClassFieldsWrapper(JavaClass javaClass) {
		this.fields = javaClass.getFields();
		this.workingDirectory = WorkingDirectoryCache.getWorkingDirectory(javaClass.getClassNode());
	}
	
	private List<JavaClassField> getFields() {
		return this.fields;
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
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-secondary mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(FIELDS_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId(FIELDS_TABLE_NAME);
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: FIELDS_TABLE_HEADERS) {
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
						List<JavaClassField> fields = this.getFields();
						for(JavaClassField field: fields) {
							Tr fieldRow = this.wrapField(field);
							tBody.appendChild(fieldRow);
						}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < FIELDS_TABLE_HEADERS.length; i++) {
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
			
			fieldsTableDiv.appendChild(cardHeader);
		return fieldsTableDiv;
	}

	private Tr wrapField(JavaClassField field) {
		Tr row = new Tr();
		
		Td showHideColumn = this.wrapShowHideIcon();
		row.appendChild(showHideColumn);
		
		Td visibilityColumn = this.wrapVisibility(field);
		row.appendChild(visibilityColumn);
		
		Td typeColumn = this.wrapType(field);
		row.appendChild(typeColumn);
		
		Td nameColumn = this.wrapName(field);
		row.appendChild(nameColumn);
		
		Td staticColumn = this.wrapStatic(field);
		row.appendChild(staticColumn);
		
		Td instanceColumn = this.wrapInstance(field);
		row.appendChild(instanceColumn);
		
		Td finalColumn = this.wrapFinal(field);
		row.appendChild(finalColumn);
		
		Td deprecatedColumn = this.wrapDeprecated(field);
		row.appendChild(deprecatedColumn);
		
		Td usedOutsideClassColumn = this.wrapCalledOutsideContainingClass(field);
		row.appendChild(usedOutsideClassColumn);

		Td ddgColumn = this.wrapDDG(field);
		row.appendChild(ddgColumn);		

		Td commentColumn = this.wrapComments(field);
		row.appendChild(commentColumn);
		
		return row;
	}
	
	private Td wrapShowHideIcon() {
		Td td = new Td();
		td.setCSSClass("details-control");
		return td;
	}
	
	private Td wrapVisibility(JavaClassField field) {
		Td td = new Td();
		Code code = new Code();
		code.appendText(field.getVisibility());
		td.appendChild(code);
		return td;
	}
	
	private Td wrapType(JavaClassField field) {
		Td td = new Td();
		Code code = new Code();
		Node typeNode = field.getTypeNode();
		String typeName = typeNode.getAttr(XCSG.name).toString();
		if(typeNode.taggedWith(XCSG.Primitive)) {
			code.appendText(typeName);
		}else {
			A link = new A();
			link.setHref("#");
			link.appendText(typeName);
			code.appendChild(link);
		}
		td.appendChild(code);
		return td;
	}
	
	private Td wrapName(JavaClassField field) {
		Td td = new Td();
		Code code = new Code();
		code.appendText(field.getName());
		td.appendChild(code);
		return td;
	}
	
	private Td wrapDeprecated(JavaClassField field) {
		Td td = new Td();
		if(field.deprecatedField()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapStatic(JavaClassField field) {
		Td td = new Td();
		if(field.staticField()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapInstance(JavaClassField field) {
		Td td = new Td();
		if(field.instanceField()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapFinal(JavaClassField field) {
		Td td = new Td();
		if(field.finalField()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapCalledOutsideContainingClass(JavaClassField field) {
		Td td = new Td();
		if(field.calledOutsideContainingClass()) {
			td.appendChild(this.checkImg());
		}
		return td;
	}
	
	private Td wrapDDG(JavaClassField field) {
		Td td = new Td();
		A link = new A();
		String ddgFileName = String.format(FileNameTemplateConstants.DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, field.getName());
		Path ddgFilePath = this.getPathToGraphFile(ddgFileName);
		SaveUtils.saveGraph(ddgFilePath, field.getDataDependenceGraph(), field.getDataDependencyGraphMarkup());
		String relativePathToDDG = this.getRelativePathStringToGraphFile(ddgFileName);
		link.setHref(relativePathToDDG);
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		td.appendChild(link);
		return td;
	}
	
	private Td wrapComments(JavaClassField field) {
		Td td = new Td();
		td.setAttribute("style", "display:none;");
			Div div = new Div();
			div.appendText(field.getComments());
		td.appendChild(div);
		return td;
	}
	
	private Img checkImg() {
		return HTMLUtils.checkImg(this.getWorkingDirectory());
	}
}
