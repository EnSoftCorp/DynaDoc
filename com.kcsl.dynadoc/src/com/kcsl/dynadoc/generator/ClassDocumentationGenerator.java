package com.kcsl.dynadoc.generator;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.PropertySet;
import com.ensoftcorp.atlas.core.markup.UnionMarkup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Code;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Li;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.Ol;
import com.hp.gagawa.java.elements.P;
import com.hp.gagawa.java.elements.Script;
import com.hp.gagawa.java.elements.Strong;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Tbody;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tfoot;
import com.hp.gagawa.java.elements.Th;
import com.hp.gagawa.java.elements.Thead;
import com.hp.gagawa.java.elements.Title;
import com.hp.gagawa.java.elements.Tr;
import com.hp.gagawa.java.elements.Ul;
import com.kcsl.dynadoc.Activator;
import com.kcsl.dynadoc.html.Nav;

public class ClassDocumentationGenerator {

	private static final String [] CONSTRUCTORS_TABLE_HEADERS = { "Visibility", "Static", "Return", "Name", "Parameters", "Abstract", "Override", "External Use", "CFG", "Call", "Usage"};

	private static final String [] METHODS_TABLE_HEADERS = { "Visibility", "Static", "Return", "Name", "Parameters", "Abstract", "Override", "External Use", "CFG", "Call", "Usage"};

	private static final String [] FIELDS_TABLE_HEADERS = { "Visibility", "Static", "Final", "Type", "Name", "External Use", "Usage"};

	private static final String [] RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED = { "check.svg", "details_close.png", "details_open.png", "styles.css"};

	private static final String GRAPHS_DIRECTORY_NAME = "graphs";
	
	private static final String RESOURCES_DIRECTORY_NAME = "resources";
	
	private Q classQ;
	
	private Node classNode;
	
	private Path storeDirectoryPath; 
	
	public ClassDocumentationGenerator(Q classQ, Path storeDirectoryPath) {
		this.classQ = classQ;
		this.storeDirectoryPath = storeDirectoryPath;
		this.classNode = classQ.eval().nodes().one();
	}
	
	public ClassDocumentationGenerator(Node classNode, Path storeDirectoryPath) {
		this.classNode = classNode;
		this.storeDirectoryPath = storeDirectoryPath;
		this.classQ = Common.toQ(classNode);
	}
	
	public void generate() {
		this.createProperOutputDirectoriesStructure();
		
		Html htmlDocument = new Html();
		htmlDocument.setLang("en");
		
		Head head = this.generateHead();
		htmlDocument.appendChild(head);
		
		Body body = this.generateBody();
		htmlDocument.appendChild(body);
		
		try {
			Path classDocHTMLFilePath = this.getStoreDirectoryPath().resolve(this.getQualifiedName() + ".html");
			PrintWriter out = new PrintWriter(new FileOutputStream(classDocHTMLFilePath.toFile()));
			out.println(htmlDocument.write());
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error while writing the HTML document for class [" + this.getQualifiedName() +"]");
		}	
	}
	
	private void createProperOutputDirectoriesStructure() {
		// Main Output Directory/
		// ... graphs/
		// ... resources/
		File mainOutputDirectory = this.getStoreDirectoryPath().toFile();
		if(mainOutputDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(mainOutputDirectory);
			} catch (IOException e) {
				System.err.println("Error while cleaning the main output directory: " + mainOutputDirectory.getAbsolutePath());
			}
		}else {
			mainOutputDirectory.mkdirs();
		}
		File graphsDirectoryFile = this.getGraphsDirectoryPath().toFile();
		graphsDirectoryFile.mkdirs();
		
		File resourcesDirectoryFile = this.getResourcesDirectoryPath().toFile();
		resourcesDirectoryFile.mkdirs();
		
		// Copy stuff into resources directory
		Bundle pluginBundle = Activator.getDefault().getBundle();
		for(String pluginResourceFileName: RESOURCES_DIRECTORY_CONTENTS_TO_BE_COPIED) {
			try {
				InputStream pluginResourceInputStream = pluginBundle.getEntry("./templates/resources/" + pluginResourceFileName).openStream();
				File destinationFile = new File(resourcesDirectoryFile, pluginResourceFileName);
				FileUtils.copyInputStreamToFile(pluginResourceInputStream, destinationFile);
			} catch (IOException e) {
				System.err.println("Error while copying contents of plugin resources file");
			}
		}		
	}
	
	private Head generateHead() {
		Head head = new Head();
		
		Title title = this.generateTitle();
		head.appendChild(title);
		
		Meta meta = this.generateMeta();
		head.appendChild(meta);
		
		List<Link> styleSheets = this.generateStyleSheetLinks();
		for(Link styleSheet: styleSheets) {
			head.appendChild(styleSheet);
		}
		
		List<Script> scripts = this.generateScripts();
		for(Script script: scripts) {
			head.appendChild(script);
		}
		return head;
	}
	
	private Title generateTitle() {
		Title title = new Title();
		title.appendText(this.getQualifiedName());
		return title;
	}
	
	private Meta generateMeta() {
		Meta meta = new Meta("width=device-width, initial-scale=1");
		meta.setName("viewport");
		return meta;
	}
	
	private List<Link> generateStyleSheetLinks() {
		List<Link> styleSheets = new ArrayList<Link>();
		
		// Bootstrap CSS
		Link boostrapCSS = new Link();
		boostrapCSS.setRel("stylesheet");
		boostrapCSS.setType("text/css");
		boostrapCSS.setHref("https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css");
		boostrapCSS.setAttribute("integrity", "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm");
		boostrapCSS.setAttribute("crossorigin", "anonymous");
		styleSheets.add(boostrapCSS);
		
		// JQuery data table CSS
		Link jqueryDataTableCSS = new Link();
		jqueryDataTableCSS.setRel("stylesheet");
		jqueryDataTableCSS.setType("text/css");
		jqueryDataTableCSS.setHref("https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css");
		styleSheets.add(jqueryDataTableCSS);
		
		// Our own CSS
		Link ourOwnCSS = new Link();
		ourOwnCSS.setRel("stylesheet");
		ourOwnCSS.setType("text/css");
		String ourStyleCSSRelativeFilePathString = this.getRelativeFilePathString(this.getResourcesDirectoryPath(), "styles.css");
		ourOwnCSS.setHref(ourStyleCSSRelativeFilePathString);
		styleSheets.add(ourOwnCSS);
		
		return styleSheets;
	}
	
	private List<Script> generateScripts() {
		List<Script> scripts = new ArrayList<Script>();
		
		// JQuery script
		Script jqueryScript = new Script("text/javascript");
		jqueryScript.setSrc("https://code.jquery.com/jquery-3.3.1.js");
		scripts.add(jqueryScript);
		
		// JQuery Data Table script
		Script jQueryDataTableScript = new Script("text/javascript");
		jQueryDataTableScript.setSrc("https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js");
		scripts.add(jQueryDataTableScript);
		
		// Bootstrap script
		Script bootstrapScript = new Script("text/javascript");
		bootstrapScript.setSrc("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js");
		scripts.add(bootstrapScript);
		
		scripts.addAll(this.generateJQueryDataTableScripts());
		
		return scripts;
	}
	
	private List<Script> generateJQueryDataTableScripts() {
		List<Script> scripts = new ArrayList<Script>();
		
		Bundle pluginBundle = Activator.getDefault().getBundle();
		
		// Constructors Table
		try {
			InputStream constructorsTableScriptIS = pluginBundle.getEntry("./templates/jquery-constructors-table-script.js").openStream();
			List<String> constructorsTableScriptLines = IOUtils.readLines(constructorsTableScriptIS);
			Script constructorsTableScript = new Script("text/javascript");
			constructorsTableScript.appendText(collapseListOfStrings(constructorsTableScriptLines));
			scripts.add(constructorsTableScript);
		} catch (IOException e) {
			System.err.println("Error reading [./templates/jquery-constructors-table-script.js] File.");
		}
		
		// Methods Table
		try {
			InputStream methodsTableScriptIS = pluginBundle.getEntry("./templates/jquery-methods-table-script.js").openStream();
			List<String> methodsTableScriptLines = IOUtils.readLines(methodsTableScriptIS);
			Script methodsTableScript = new Script("text/javascript");
			methodsTableScript.appendText(collapseListOfStrings(methodsTableScriptLines));
			scripts.add(methodsTableScript);
		} catch (IOException e) {
			System.err.println("Error reading [./templates/jquery-methods-table-script.js] File.");
		}
		
		
		// Fields Table
		try {
			InputStream fieldsTableScriptIS = pluginBundle.getEntry("./templates/jquery-fields-table-script.js").openStream();
			List<String> fieldsTableScriptLines = IOUtils.readLines(fieldsTableScriptIS);
			Script fieldsTableScript = new Script("text/javascript");
			fieldsTableScript.appendText(collapseListOfStrings(fieldsTableScriptLines));
			scripts.add(fieldsTableScript);
		} catch (IOException e) {
			System.err.println("Error reading [./templates/jquery-fields-table-script.js] File.");
		}
		
		return scripts;
	}
	
	private Body generateBody() {
		Body body = new Body();
		
		Nav breadcrumbSection = this.generateBreadcrumbSection();
		body.appendChild(breadcrumbSection);
		
		Div classNameSection = this.generateClassNameSection();
		body.appendChild(classNameSection);
		
		Div classInfoSection = this.generateClassInfoSection();
		body.appendChild(classInfoSection);
		
		Div constructorTableSection = this.generateConstructorsTableSection();
		body.appendChild(constructorTableSection);
		
		Div fieldsTableSection = this.generateFieldsTableSection();
		body.appendChild(fieldsTableSection);
		
		Div methodsTableSection = this.generateMethodsTableSection();
		body.appendChild(methodsTableSection);
		
		Div usageExamplesSection = this.generateUsageExamplesSection();
		body.appendChild(usageExamplesSection);
		
		Div issuesSummarySection = this.generateIssueSummarySection();
		body.appendChild(issuesSummarySection);
		
		Div revisionControlSummarySection = this.generateRevisionControlSummarySection();
		body.appendChild(revisionControlSummarySection);
		
		Div discussionSection = this.generateDiscussionSection();
		body.appendChild(discussionSection);
		
		return body;
	}
	
	private Nav generateBreadcrumbSection() {
		Nav breadcrumb = new Nav();
		breadcrumb.setAttribute("aria-label", "breadcrumb");
		
			Ol ol = new Ol();
			ol.setCSSClass("breadcrumb");
			
				Li packageLi = new Li();
				packageLi.setCSSClass("breadcrumb-item");
				packageLi.appendText(this.getContainingPackageQualifiedName());
				ol.appendChild(packageLi);
				
				Li classLi = new Li();
				classLi.setCSSClass("breadcrumb-item active");
				classLi.setAttribute("aria-current", "page");
				classLi.appendText(this.getName());
				
				ol.appendChild(classLi);
			
			breadcrumb.appendChild(ol);
		return breadcrumb;
	}
	
	private Div generateClassNameSection() {
		Div section = new Div();
		section.setCSSClass("card border-dark mb-3");
		section.setStyle("max-width: 98%; margin: 10pt");
		
			Div classNameDiv = new Div();
			classNameDiv.setCSSClass("card-header");
			classNameDiv.appendText(this.getName());
			section.appendChild(classNameDiv);
			
			Div contentDiv = new Div();
			contentDiv.setCSSClass("card-body text-dark");
				P comments = new P();
				comments.setCSSClass("card-text");
				comments.appendText(this.getClassUserAnnotations());
				contentDiv.appendChild(comments);
			section.appendChild(contentDiv);
		
		return section;
	}
	
	private Div generateClassInfoSection() {
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
				cardContent.setCSSClass("card-body bg-white text-dark");
					P content = new P();
					content.setCSSClass("card-text");
					content.appendChild(this.getClassPropertiesElement());
					cardContent.appendChild(content);
				classPropertiesCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classPropertiesCard);
			
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
							label.appendText("Inner Classes: ");
							innerClassesLi.appendChild(label);
							innerClassesLi.appendText(this.getInnerClasses().size() + "");
						statsList.appendChild(innerClassesLi);
					
						// Constructors Count
						Li constructorsLi = new Li();
							label = new Strong();
							label.appendText("Constructors: ");
							constructorsLi.appendChild(label);
							constructorsLi.appendText(this.getConstructors().size() + "");
						statsList.appendChild(constructorsLi);
					
						// Fields Count
						Li fieldsLi = new Li();
							label = new Strong();
							label.appendText("Fields: ");
							fieldsLi.appendChild(label);
							fieldsLi.appendText(this.getFields().size() + "");
						statsList.appendChild(fieldsLi);
					
						// Methods Count
						Li methodsLi = new Li();
							label = new Strong();
							label.appendText("Methods: ");
							methodsLi.appendChild(label);
							long methodsMinusConstructors = this.getMethods().size() - this.getConstructors().size();
							methodsLi.appendText(methodsMinusConstructors + "");
						statsList.appendChild(methodsLi);
						
					cardContent.appendChild(statsList);
				classStatisticsCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classStatisticsCard);
			
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
					String classHierarchyImageFileName = this.getQualifiedName() + "-hierarchy.svg";
					String classHierarchyImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), classHierarchyImageFileName);
					String classHierarchyImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), classHierarchyImageFileName);
					this.generateTypeHierarchyImage(classHierarchyImageAbsolutePath);
					imageLink.setHref(classHierarchyImageRelativePath);
					imageLink.setTarget("_blank");
						Img image = new Img("Click to view full-sized image", classHierarchyImageRelativePath);
						image.setCSSClass("img-thumbnail");
						imageLink.appendChild(image);
						cardContent.appendChild(imageLink);
				classHierarchyCard.appendChild(cardContent);
			cardGroupDiv.appendChild(classHierarchyCard);
			
			// Class Interaction Card
			Div classUsageCard = new Div();
			classUsageCard.setCSSClass("card bg-warning mb-3");
			classUsageCard.setStyle("max-width: 27rem; margin: 10pt");
				cardHeader = new Div();
				cardHeader.setCSSClass("card-header");
				cardHeader.appendText("Class Interaction");
				classUsageCard.appendChild(cardHeader);
				
				cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
					imageLink = new A();

					String classInteractionImageFileName = this.getQualifiedName() + "-interaction.svg";
					String classInteractionImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), classInteractionImageFileName);
					String classInteractionImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), classInteractionImageFileName);
					
					this.generateInteractionImage(classInteractionImageAbsolutePath);
					imageLink.setHref(classInteractionImageRelativePath);
					imageLink.setTarget("_blank");
						image = new Img("Click to view full-sized image", classInteractionImageRelativePath);
						image.setCSSClass("img-thumbnail");
						imageLink.appendChild(image);
						cardContent.appendChild(imageLink);
						classUsageCard.appendChild(cardContent);			
			cardGroupDiv.appendChild(classUsageCard);
		
		return cardGroupDiv;
	}
	
	private void generateTypeHierarchyImage(String filePath) {
		Node containingProjectNode = CommonQueries.getContainingNode(this.getClassNode(), XCSG.Project);
		Q containingProjectQ = Common.toQ(containingProjectNode);
		Q containedTypes = containingProjectQ.forwardOn(Common.universe().edges(XCSG.Contains).nodes(XCSG.Type));
		Q superTypeEdges = Common.universe().edges(XCSG.Supertype);
		Q typeHierarchyQ = superTypeEdges.forward(this.getClassQ()).intersection(containedTypes).induce(superTypeEdges);
		typeHierarchyQ = Common.extend(typeHierarchyQ, XCSG.Contains);
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge && element.taggedWith(XCSG.Supertype)) {
					if(element.taggedWith(XCSG.Java.Extends)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "extends");
					}else if(element.taggedWith(XCSG.Java.Implements)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "implements");
					}
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(this.getClassNode(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		UnionMarkup markup = new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
		this.saveGraph(filePath, typeHierarchyQ, markup);
	}
	
	private void saveGraph(String filePath, Q graphQ, IMarkup markup) {
		try {
			SaveUtil.saveGraph(new File(filePath), graphQ.eval(), markup);
		} catch(Exception e) {
			System.err.println("Cannot save graph due to: " + e.getMessage());
		}
	}
	
	private void generateInteractionImage(String filePath) {
		// TODO
	}
	
	private AtlasSet<Node> getInnerClasses() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Contains)).nodes(XCSG.Java.InnerClass).eval().nodes();
	}
	
	private AtlasSet<Node> getConstructors() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Contains)).nodes(XCSG.Constructor).eval().nodes();
	}
	
	private AtlasSet<Node> getMethods() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Contains)).nodes(XCSG.Method).eval().nodes();
	}
	
	private AtlasSet<Node> getFields() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Contains)).nodes(XCSG.Field).eval().nodes();
	}   
	
	private Code getClassPropertiesElement() {
		Code code = new Code();
		
		code.appendText(this.getVisibility());
		code.appendText(" ");
		
		if(this.isStatic()) {
			code.appendText("static");
			code.appendText(" ");
		}
		
		if(this.isFinal()) {
			code.appendText("final");
			code.appendText(" ");
		}	
		
		if(this.isAbstract()) {
			code.appendText("abstract");
			code.appendText(" ");
		}
		
		if(this.isInterface()) {
			code.appendText("interface");
			code.appendText(" ");
		}
		
		if(this.isEnum()) {
			code.appendText("enum");
			code.appendText(" ");
		}
		
		if(!this.isEnum() && !this.isInterface()) {
			code.appendText("class");
			code.appendText(" ");
		}
		
			A classLink = new A();
			classLink.setHref("#");
			classLink.appendText(this.getName());
			code.appendChild(classLink);
			code.appendText(" ");
			
		Node extendedClassNode = this.getExtendedClassNode();
		if(extendedClassNode != null) {
			code.appendText("extends");
			code.appendText(" ");
				A extendedClassLink = new A();
				extendedClassLink.setHref("#");
				extendedClassLink.appendText(extendedClassNode.getAttr(XCSG.name).toString());
				code.appendChild(extendedClassLink);
				code.appendText(" ");
		}
		
		AtlasSet<Node> implementedClassNodes = this.getImplementedClassNodes();
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
		
		return code;
	}
	
	private Node getExtendedClassNode() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private AtlasSet<Node> getImplementedClassNodes() {
		return this.getClassQ().successorsOn(Common.universe().edges(XCSG.Java.Implements)).nodes(XCSG.Type).eval().nodes();
	}
	
	private String getVisibility() {
		if(this.getClassNode().taggedWith(XCSG.privateVisibility)) {
			return Visibility.PRIVATE.toString();
		}
		if(this.getClassNode().taggedWith(XCSG.protectedPackageVisibility)) {
			return Visibility.PROTECTED.toString();
		}
		if(this.getClassNode().taggedWith(XCSG.publicVisibility)) {
			return Visibility.PUBLIC.toString();
		}
		return Visibility.PUBLIC.toString();
	}
	
	private boolean isAbstract() {
		return this.getClassNode().taggedWith(XCSG.Java.AbstractClass);
	}
	
	private boolean isFinal() {
		return this.getClassNode().taggedWith(XCSG.Java.finalClass);
	}
	
	private boolean isStatic() {
		return this.getClassNode().taggedWith("static");
	}
	
	private boolean isInterface() {
		return this.getClassNode().taggedWith(XCSG.Java.Interface);
	}
	
	private boolean isEnum() {
		return this.getClassNode().taggedWith(XCSG.Java.Enum);
	}
	
	private boolean isInnerClass() {
		return this.getClassNode().taggedWith(XCSG.Java.InnerClass);
	}
	
	private Div generateConstructorsTableSection() {
		Div constructorsTableDiv = new Div();
		constructorsTableDiv.setCSSClass("card text-white bg-primary mb-3");
		constructorsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText("Constrcutors Summary");
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId("constructor-table");
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
						AtlasSet<Node> constructors = this.getConstructors();
							for(Node constructor: constructors) {
								Tr constructorRow = this.constructMethodRow(constructor);
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
	
	private String getFieldVisibility(Node fieldNode) {
		if(fieldNode.taggedWith(XCSG.privateVisibility)) {
			return Visibility.PRIVATE.toString();
		}
		if(fieldNode.taggedWith(XCSG.protectedPackageVisibility)) {
			return Visibility.PROTECTED.toString();
		}
		if(fieldNode.taggedWith(XCSG.publicVisibility)) {
			return Visibility.PUBLIC.toString();
		}
		return Visibility.PUBLIC.toString();
	}
	
	private String getMethodVisibility(Node methodNode) {
		if(methodNode.taggedWith(XCSG.privateVisibility)) {
			return Visibility.PRIVATE.toString();
		}
		if(methodNode.taggedWith(XCSG.protectedPackageVisibility)) {
			return Visibility.PROTECTED.toString();
		}
		if(methodNode.taggedWith(XCSG.publicVisibility)) {
			return Visibility.PUBLIC.toString();
		}
		return Visibility.PUBLIC.toString();
	}
	
	private Img getTickImage() {
		String usageImageRelativePath = this.getRelativeFilePathString(this.getResourcesDirectoryPath(), "check.svg");
		Img tickImage = new Img("", usageImageRelativePath);
		tickImage.setWidth("25");
		return tickImage;
	}
	
	private Node getReturnTypeForMethod(Node methodNode) {
		return Common.universe().edges(XCSG.Returns).successors(Common.toQ(methodNode)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private AtlasSet<Node> getParametersForMethod(Node methodNode) {
		return CommonQueries.functionParameter(Common.toQ(methodNode)).eval().nodes();
	}
	
	private boolean isAbstractMethod(Node methodNode) {
		return methodNode.taggedWith(XCSG.abstractMethod);
	}
	
	private boolean isOverridingMethod(Node methodNode) {
		// TODO:
		return false;
	}
	
	private boolean isExternallyUsedMethod(Node methodNode) {
		Q containsEdges = Common.universe().edges(XCSG.Contains);
		Q callEdges = Common.universe().edges(XCSG.Call);
		Q currentContainingClass = containsEdges.predecessors(Common.toQ(methodNode)).nodes(XCSG.Java.Class);
		Q callers = callEdges.predecessors(Common.toQ(methodNode));
		Q callerClasses = containsEdges.reverse(callers).nodes(XCSG.Java.Class);
		callerClasses = callerClasses.difference(currentContainingClass);
		return !callerClasses.eval().nodes().isEmpty();
	}
	
	private void generateCFGImageForMethod(String filePath, Node methodNode) {
		Q cfgQ = CommonQueries.cfg(methodNode);
		cfgQ = Common.extend(cfgQ, XCSG.Contains);
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge) {
					if (element.taggedWith(XCSG.ControlFlow_Edge) && element.hasAttr(XCSG.conditionValue)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, element.getAttr(XCSG.conditionValue).toString());
					}
				}
				return new PropertySet();
			}
		};
		UnionMarkup markup = new UnionMarkup(Arrays.asList(labelEdgesMarkup));
		this.saveGraph(filePath, cfgQ, markup);
	}
	
	private void generateCallHierarchyImageForMethod(String filePath, Node methodNode) {
		Q callEdges = Common.universe().edges(XCSG.Call);
		Q callGraphQ = callEdges.forwardStep(Common.toQ(methodNode)).union(callEdges.reverseStep(Common.toQ(methodNode)));
		callGraphQ = Common.extend(callGraphQ, XCSG.Contains);
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(methodNode, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		UnionMarkup markup = new UnionMarkup(Arrays.asList(nodeMarkup));
		this.saveGraph(filePath, callGraphQ, markup);
	}
	
	private void generateUsageImageForMethod(String filePath, Node methodNode) {
		// TODO
	}
	
	private void generateUsageImageForField(String filePath, Node fieldNode) {
		// TODO
	}
	
	private String getUniqueMethodName(Node methodNode) {
		return CommonQueries.getQualifiedFunctionName(methodNode) + "-" + methodNode.getAttr("##signature");
	}
	
	private String getUniqueFiledName(Node fieldNode) {
		return this.getQualifiedName() + "-" + fieldNode.getAttr(XCSG.name);
	}
	
	private Tr constructMethodRow(Node methodNode) {
		List<Td> columns = new ArrayList<Td>();
		
		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setCSSClass("details-control");
		columns.add(expandColumn);
		
		// Visibility Column
		Td accessorColumn = new Td();
		Code code = new Code();
		code.appendText(this.getMethodVisibility(methodNode));
		accessorColumn.appendChild(code);
		columns.add(accessorColumn);
		
		// Static Column
		Td staticColumn = new Td();
		if(methodNode.taggedWith(XCSG.ClassMethod) || methodNode.taggedWith("static")) {
			staticColumn.appendChild(this.getTickImage());
		}
		columns.add(staticColumn);
		
		// Return Type Column
		Td returnTypeColumn = new Td();
		code = new Code();
		Node type = this.getReturnTypeForMethod(methodNode);
		if(type == null) {
			code.appendText("void");
		}else {
			if(type.taggedWith(XCSG.Primitive)) {
				code.appendText(type.getAttr(XCSG.name).toString());
			}else {
				A link = new A();
				link.setHref("#");
				link.appendText(type.getAttr(XCSG.name).toString());
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
		AtlasSet<Node> parameterNodes = this.getParametersForMethod(methodNode);
		for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
			Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
			Node typeNode = Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
			String fullyQualifiedClassName = typeNode.getAttr(XCSG.name).toString();
			
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
			if(parameterIndex < parameterNodes.size() - 1) {
				parametersColumn.appendChild(new Br());
			}
		}
		columns.add(parametersColumn);

		
		// Abstract
		Td abstractColumn = new Td();
		if(this.isAbstractMethod(methodNode)) {
			abstractColumn.appendChild(this.getTickImage());
		}
		columns.add(abstractColumn);
		
		
		// Override
		Td overrideColumn = new Td();
		if(this.isOverridingMethod(methodNode)) {
			overrideColumn.appendChild(this.getTickImage());
		}
		columns.add(overrideColumn);
		
		// Externally Used Column
		Td externalColumn = new Td();
		if(this.isExternallyUsedMethod(methodNode)) {
			externalColumn.appendChild(this.getTickImage());
		}
		columns.add(externalColumn);
		
		// CFG Column
		Td cfgColumn = new Td();
		A CFGlink = new A();
		
		String cfgImageFileName = this.getUniqueMethodName(methodNode) + "-cfg.svg";
		String cfgImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), cfgImageFileName);
		String cfgImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), cfgImageFileName);
		
		
		this.generateCFGImageForMethod(cfgImageAbsolutePath, methodNode);
		CFGlink.setHref(cfgImageRelativePath);
		CFGlink.setTarget("_blank");
		CFGlink.setAttribute("role", "button");
		CFGlink.setAttribute("class", "btn btn-success");
		CFGlink.appendText("Show");
		cfgColumn.appendChild(CFGlink);
		columns.add(cfgColumn);
		
		
		// Call Hierarchy Column
		Td callColumn = new Td();
		A link = new A();
		
		String callImageFileName = this.getUniqueMethodName(methodNode) + "-call.svg";
		String callImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), callImageFileName);
		String callImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), callImageFileName);
		
		this.generateCallHierarchyImageForMethod(callImageAbsolutePath, methodNode);
		link.setHref(callImageRelativePath);
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-success");
		link.appendText("Show");
		callColumn.appendChild(link);
		columns.add(callColumn);
		
		// Usage Example Column
		Td usageExampleColumn = new Td();
		link = new A();
		
		String usageImageFileName = this.getUniqueMethodName(methodNode) + "-usage.svg";
		String usageImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		String usageImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		
		this.generateUsageImageForMethod(usageImageAbsolutePath, methodNode);
		link.setHref(usageImageRelativePath);
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		usageExampleColumn.appendChild(link);
		columns.add(usageExampleColumn);
		
		// User Comments Column
		Td userCommentsColumn = new Td();
		userCommentsColumn.setAttribute("style", "display:none;");
		Div container = this.generateMethodAnnotations(methodNode);
		userCommentsColumn.appendChild(container);
		columns.add(userCommentsColumn);
		
		Tr methodRow = new Tr();
		for(Td column : columns) {
			methodRow.appendChild(column);
		}
		return methodRow;
	}
	
	private Div generateMethodAnnotations(Node methodNode) {
		// TODO
		return new Div();
	}
	
	private Div generateFieldAnnotations(Node fieldNode) {
		// TODO
		return new Div();
	}
	
	private Div generateFieldsTableSection() {
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-primary mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText("Fields Summary");
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId("fields-table");
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
						AtlasSet<Node> fields = this.getFields();
							for(Node field: fields) {
								Tr fieldRow = this.constructFieldRow(field);
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
	
	private boolean isStaticField(Node fieldNode) {
		return fieldNode.taggedWith(XCSG.ClassVariable) || fieldNode.taggedWith("static");
	}
	
	private boolean isFinalField(Node fieldNode) {
		return fieldNode.taggedWith(XCSG.immutable) || fieldNode.taggedWith("final");
	}
	
	private Node getTypeForField(Node fieldNode) {
		return Common.universe().edges(XCSG.TypeOf).successors(Common.toQ(fieldNode)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private boolean isExternallyUsedField(Node fieldNode) {
		Q dataFlowEdges = Common.universe().edges(XCSG.DataFlow_Edge);
		Q currentContainingClass = Common.edges(XCSG.Contains).predecessors(Common.toQ(fieldNode)).nodes(XCSG.Java.Class);
		Q forwardStepDataFlow = dataFlowEdges.forwardStep(Common.toQ(fieldNode));
		Q reverseStepDataFlow = dataFlowEdges.reverseStep(Common.toQ(fieldNode));
		Q surroundingDataFlowOneStep = forwardStepDataFlow.union(reverseStepDataFlow);
		Q extendedOnContains = Common.extend(surroundingDataFlowOneStep, XCSG.Contains);
		Q containingClassesQ = extendedOnContains.nodes(XCSG.Java.Class);
		containingClassesQ = containingClassesQ.difference(currentContainingClass);
		return !containingClassesQ.eval().nodes().isEmpty();
	}
	
	private Tr constructFieldRow(Node fieldNode) {
		List<Td> columns = new ArrayList<Td>();
		
		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setCSSClass("details-control");
		columns.add(expandColumn);
		
		// Visibility Column
		Td accessorColumn = new Td();
		Code code = new Code();
		code.appendText(this.getFieldVisibility(fieldNode));
		accessorColumn.appendChild(code);
		columns.add(accessorColumn);
		
		// Static Column
		Td staticColumn = new Td();
		if(this.isStaticField(fieldNode)) {
			staticColumn.appendChild(this.getTickImage());
		}
		columns.add(staticColumn);
		
		// Final Column
		Td finalColumn = new Td();
		if(this.isFinalField(fieldNode)) {
			finalColumn.appendChild(this.getTickImage());
		}
		columns.add(finalColumn);
		
		// Type Column
		Td typeColumn = new Td();
		code = new Code();
		Node type = this.getTypeForField(fieldNode);
		if(type.taggedWith(XCSG.Primitive)) {
			code.appendText(type.getAttr(XCSG.name).toString());
		}else {
			String qualifiedName = type.getAttr(XCSG.name).toString();
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
		if(this.isExternallyUsedField(fieldNode)) {
			externalColumn.appendChild(this.getTickImage());
		}
		columns.add(externalColumn);
		
		// Usage Example Column
		Td usageExampleColumn = new Td();
		A link = new A();
		
		String usageImageFileName = this.getUniqueFiledName(fieldNode) + "-usage.svg";
		String usageImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		String usageImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		
		this.generateUsageImageForField(usageImageAbsolutePath, fieldNode);
		link.setHref(usageImageRelativePath);
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		usageExampleColumn.appendChild(link);
		columns.add(usageExampleColumn);
		
		// User Comments Column
		Td userCommentsColumn = new Td();
		userCommentsColumn.setAttribute("style", "display:none;");
		Div container = this.generateFieldAnnotations(fieldNode);
		userCommentsColumn.appendChild(container);
		columns.add(userCommentsColumn);
		
		Tr fieldRow = new Tr();
		for(Td column : columns) {
			fieldRow.appendChild(column);
		}
		return fieldRow;
	}
	
	private Div generateMethodsTableSection() {
		Div methodsTableDiv = new Div();
		methodsTableDiv.setCSSClass("card text-white bg-primary mb-3");
		methodsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText("Methods Summary");
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId("methods-table");
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
						AtlasSet<Node> methodsMinusConstructors = Common.toQ(this.getMethods()).difference(Common.toQ(this.getConstructors())).eval().nodes();
							for(Node method: methodsMinusConstructors) {
								Tr methodRow = this.constructMethodRow(method);
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
	
	private Div generateUsageExamplesSection() {
		// TODO
		return new Div();
	}
	
	private Div generateIssueSummarySection() {
		// TODO
		return new Div();
	}
	
	private Div generateRevisionControlSummarySection() {
		// TODO
		return new Div();
	}
	
	private Div generateDiscussionSection() {
		// TODO
		return new Div();
	}
	
	private String getClassUserAnnotations() {
		// TODO
		return StringUtils.EMPTY;
	}
	
	private String getQualifiedName() {
		return CommonQueries.getQualifiedTypeName(this.getClassNode());
	}
	
	private String getName() {
		return this.getClassNode().getAttr(XCSG.name).toString();
	}
	
	private Node getContainingPackage() {
		return CommonQueries.getContainingNode(this.getClassNode(), XCSG.Package);
	}
	
	private String getContainingPackageQualifiedName() {
		return this.getContainingPackage().getAttr(XCSG.name).toString();
	}
	
	private static String collapseListOfStrings(List<String> strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for(String string: strings) {
			stringBuilder.append(string);
		}
		return stringBuilder.toString();
	}
	
	private Q getClassQ() {
		return this.classQ;
	}
	
	private Node getClassNode() {
		return this.classNode;
	}
	
	private Path getStoreDirectoryPath() {
		return this.storeDirectoryPath;
	}
	
	private Path getGraphsDirectoryPath() {
		return this.getStoreDirectoryPath().resolve(GRAPHS_DIRECTORY_NAME);
	}
	
	private Path getResourcesDirectoryPath() {
		return this.getStoreDirectoryPath().resolve(RESOURCES_DIRECTORY_NAME);
	}
	
	private String getAbsoluteFilePathString(Path containingDirectory, String fileName) {
		return containingDirectory.toFile().getAbsolutePath() + IOUtils.DIR_SEPARATOR + fileName;
	}
	
	private String getRelativeFilePathString(Path containingDirectory, String fileName) {
		return containingDirectory.toFile().getName() + IOUtils.DIR_SEPARATOR + fileName;
	}
}