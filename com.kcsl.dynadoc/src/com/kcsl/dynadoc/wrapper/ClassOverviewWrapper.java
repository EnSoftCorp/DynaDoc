package com.kcsl.dynadoc.wrapper;

import java.nio.file.Path;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Code;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Li;
import com.hp.gagawa.java.elements.Ol;
import com.hp.gagawa.java.elements.Strong;
import com.hp.gagawa.java.elements.Ul;
import com.kcsl.dynadoc.constants.FileNameTemplateConstants;
import com.kcsl.dynadoc.data.JavaClass;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.path.WorkingDirectoryCache;
import com.kcsl.dynadoc.utils.PathUtils;
import com.kcsl.dynadoc.utils.SaveUtils;

public class ClassOverviewWrapper {
	
	private JavaClass javaClass;
	
	private WorkingDirectory workingDirectory;
	
	public ClassOverviewWrapper(JavaClass javaClass) {
		this.javaClass = javaClass;
		this.workingDirectory = WorkingDirectoryCache.getWorkingDirectory(javaClass.getClassNode());
	}
	
	public JavaClass getJavaClass() {
		return this.javaClass;
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
		Div div = new Div();
		Div breadcrumbSection = this.wrapBreadcrumbSection();
		div.appendChild(breadcrumbSection);
		
		Div classNameSection = this.wrapClassNameSection();
		div.appendChild(classNameSection);
		
		Div classInfoSection = this.wrapClassInfoSection();
		div.appendChild(classInfoSection);
		return div;
	}
	
	private Div wrapBreadcrumbSection() {
		Div breadcrumb = new Div();
		breadcrumb.setCSSClass("nav-container");
		breadcrumb.setStyle("position:relative;");
		
			Ol ol = new Ol();
			ol.setCSSClass("breadcrumb");
			
				Li packageLi = new Li();
				packageLi.setCSSClass("breadcrumb-item");
				packageLi.appendText(this.getJavaClass().getPackageName());
				ol.appendChild(packageLi);
				
				Li classLi = new Li();
				classLi.setCSSClass("breadcrumb-item active");
				classLi.appendText(this.getJavaClass().getName());
				
				ol.appendChild(classLi);
			breadcrumb.appendChild(ol);
		
			A guideLink = new A();
			guideLink.setHref(PathUtils.getRelativePathStringToGuidePageFile(this.getWorkingDirectory()));
			guideLink.setTarget("_blank");
			guideLink.setCSSClass("btn btn-bg btn-danger");
			guideLink.setStyle("position:absolute; right:1rem; top:50%; transform:translateY(-50%);");
			guideLink.setAttribute("role", "button");
			guideLink.appendText("DynaDoc Guide");
			
			breadcrumb.appendChild(guideLink);
		
		return breadcrumb;
	}
	
	private Div wrapClassNameSection() {
		Div section = new Div();
		section.setCSSClass("card border-dark mb-3");
		section.setStyle("max-width: 98%; margin: 10pt");
		
			Div classNameDiv = new Div();
			classNameDiv.setCSSClass("card-header");
			classNameDiv.appendText(this.getJavaClass().getName());
			section.appendChild(classNameDiv);
			
			Div contentDiv = new Div();
			contentDiv.setCSSClass("card-body text-dark small");
			contentDiv.appendText(this.getJavaClass().getComments());
			section.appendChild(contentDiv);
		
		return section;
	}
	
	private Div wrapClassInfoSection() {
		Div cardGroupDiv = new Div();
		cardGroupDiv.setCSSClass("card-group");
		cardGroupDiv.setStyle("max-width: 98%; margin: 10pt");
		
			// Class Properties Card
			Div classPropertiesCard = new Div();
			classPropertiesCard.setCSSClass("card text-white bg-info mb-3");
			classPropertiesCard.setStyle("max-width: 18rem; margin: 10pt");
				Div cardHeader = new Div();
				cardHeader.setCSSClass("card-header");
				cardHeader.appendText("Class Properties");
				classPropertiesCard.appendChild(cardHeader);
				
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark scrollClass");
				cardContent.appendChild(this.wrapProperties());
				classPropertiesCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classPropertiesCard);
			
			// Class Hierarchy Card
			Div classHierarchyCard = new Div();
			classHierarchyCard.setCSSClass("card text-white bg-dark mb-3");
			classHierarchyCard.setStyle("max-width: 27rem; margin: 10pt");
				cardHeader = new Div();
				cardHeader.setCSSClass("card-header");
				cardHeader.appendText("Class Hierarchy");
				classHierarchyCard.appendChild(cardHeader);
				
				cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
					A imageLink = new A();
					String hierarchyGraphFileName = String.format(FileNameTemplateConstants.HIERARCHY_GRAPH_FILE_NAME_TEMPLATE, this.getJavaClass().getQualifiedName());
					Path hierarchyGraphFilePath = this.getPathToGraphFile(hierarchyGraphFileName);
					SaveUtils.saveGraph(hierarchyGraphFilePath, this.getJavaClass().getTypeHierarchyGraph(), this.getJavaClass().getTypeHierarchyGraphMarkup());
					String relativePathToHierarchyGraph = this.getRelativePathStringToGraphFile(hierarchyGraphFileName);
					imageLink.setHref(relativePathToHierarchyGraph);
					imageLink.setTarget("_blank");
						Img image = new Img("Click to view full-sized image", relativePathToHierarchyGraph);
						image.setCSSClass("img-thumbnail");
						imageLink.appendChild(image);
						cardContent.appendChild(imageLink);
				classHierarchyCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classHierarchyCard);
			
			// Inner Classes Overview Card
			Div classUsageCard = new Div();
			classUsageCard.setCSSClass("card bg-warning mb-3");
			classUsageCard.setStyle("max-width: 27rem; margin: 10pt");
				cardHeader = new Div();
				cardHeader.setCSSClass("card-header");
				cardHeader.appendText("Nested Classes");
				classUsageCard.appendChild(cardHeader);
				
				cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
					imageLink = new A();
					String nestingClassesGraphFileName = String.format(FileNameTemplateConstants.NESTING_CLASSES_GRAPH_FILE_NAME_TEMPLATE, this.getJavaClass().getQualifiedName());
					Path nestingClassesGraphFilePath = this.getPathToGraphFile(nestingClassesGraphFileName);
					SaveUtils.saveGraph(nestingClassesGraphFilePath, this.getJavaClass().getClassNestingGraph(), this.getJavaClass().getClassNestingGraphMarkup());
					String relativePathToNestingClassesGraph = this.getRelativePathStringToGraphFile(nestingClassesGraphFileName);
					imageLink.setHref(relativePathToNestingClassesGraph);
					imageLink.setTarget("_blank");
						image = new Img("Click to view full-sized image", relativePathToNestingClassesGraph);
						image.setCSSClass("img-thumbnail");
						imageLink.appendChild(image);
						cardContent.appendChild(imageLink);
						classUsageCard.appendChild(cardContent);			
			cardGroupDiv.appendChild(classUsageCard);
			
			// Class Statistics Card
			Div classStatisticsCard = new Div();
			classStatisticsCard.setCSSClass("card bg-light mb-3");
			classStatisticsCard.setStyle("max-width: 18rem; margin: 10pt");
				cardHeader = new Div();
				cardHeader.setCSSClass("card-header");
				cardHeader.appendText("Class Statistics");
				classStatisticsCard.appendChild(cardHeader);
				
				cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
					Ul statsList = new Ul();
						// Inner Classes Count
						Li innerClassesLi = new Li();
							Strong label = new Strong();
							label.appendText("Nested Classes: ");
							innerClassesLi.appendChild(label);
							innerClassesLi.appendText(this.getJavaClass().getInnerClasses().size() + "");
						statsList.appendChild(innerClassesLi);
					
						// Constructors Count
						Li constructorsLi = new Li();
							label = new Strong();
							label.appendText("Constructors: ");
							constructorsLi.appendChild(label);
							constructorsLi.appendText(this.getJavaClass().getConstructors().size() + "");
						statsList.appendChild(constructorsLi);
					
						// Fields Count
						Li fieldsLi = new Li();
							label = new Strong();
							label.appendText("Fields: ");
							fieldsLi.appendChild(label);
							fieldsLi.appendText(this.getJavaClass().getFields().size() + "");
						statsList.appendChild(fieldsLi);
					
						// Methods Count
						Li methodsLi = new Li();
							label = new Strong();
							label.appendText("Methods: ");
							methodsLi.appendChild(label);
							long methodsMinusConstructors = this.getJavaClass().getMethods().size() - this.getJavaClass().getConstructors().size();
							methodsLi.appendText(methodsMinusConstructors + "");
						statsList.appendChild(methodsLi);
						
					cardContent.appendChild(statsList);
				classStatisticsCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classStatisticsCard);
		
		return cardGroupDiv;
	}
	
	private Ul wrapProperties() {
		
		Ul list = new Ul();
		list.setStyle("margin: 5; padding: 0");
		
			Li hierarchyLi = new Li();
				Strong hierarchyTag = new Strong();
				hierarchyTag.appendText("Hierarchy:");
			hierarchyLi.appendChild(hierarchyTag);
		list.appendChild(hierarchyLi);
		
			Code code = new Code();
			
			code.appendText(this.getJavaClass().getVisibility());
			code.appendText(" ");
			
			if(this.getJavaClass().staticClass()) {
				code.appendText("static");
				code.appendText(" ");
			}
			
			if(this.getJavaClass().finalClass()) {
				code.appendText("final");
				code.appendText(" ");
			}	
			
			if(this.getJavaClass().abstractClass()) {
				code.appendText("abstract");
				code.appendText(" ");
			}
			
			if(this.getJavaClass().interfaceClass()) {
				code.appendText("interface");
				code.appendText(" ");
			}
			
			if(this.getJavaClass().enumClass()) {
				code.appendText("enum");
				code.appendText(" ");
			}
			
			if(!this.getJavaClass().enumClass() && !this.getJavaClass().interfaceClass()) {
				code.appendText("class");
				code.appendText(" ");
			}
			
				A classLink = new A();
				classLink.setHref("#");
				classLink.appendText(this.getJavaClass().getName());
				code.appendChild(classLink);
				code.appendText(" ");
				
			Node extendedClassNode = this.getJavaClass().getExtendedClass();
			if(extendedClassNode != null) {
				code.appendText("extends");
				code.appendText(" ");
					A extendedClassLink = new A();
					extendedClassLink.setHref("#");
					extendedClassLink.appendText(extendedClassNode.getAttr(XCSG.name).toString());
					code.appendChild(extendedClassLink);
					code.appendText(" ");
			}
			
			AtlasSet<Node> implementedClassNodes = this.getJavaClass().getImplementedClasses();
			if(!implementedClassNodes.isEmpty()) {
				code.appendText("implements");
				code.appendText(" ");
				for(Node implementedClassNode: implementedClassNodes) {
					A implementedClassLink = new A();
					implementedClassLink.setHref("#");
					implementedClassLink.appendText(implementedClassNode.getAttr(XCSG.name).toString());
					code.appendChild(implementedClassLink);
					code.appendText(" ");
				}
			}
		list.appendChild(code);
		
			Li nestedClassesLi = new Li();
				Strong nestedClassesTag = new Strong();
				nestedClassesTag.appendText("Nested Classes:");
			nestedClassesLi.appendChild(nestedClassesTag);
		list.appendChild(nestedClassesLi);
		
		AtlasSet<Node> nestedClasses = this.getJavaClass().getInnerClasses();
			Ul nestedClassesList = new Ul();
			list.setStyle("margin: 5; padding: 0");
			for(Node nestedClass: nestedClasses) {
				Li nestedClassLi = new Li();
					Code classCode = new Code();
					classCode.appendText(nestedClass.getAttr(XCSG.name).toString());
				nestedClassLi.appendChild(classCode);
				nestedClassesList.appendChild(nestedClassLi);
			}
		
		list.appendChild(nestedClassesList);
		return list;
	}

}
