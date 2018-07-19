package com.kcsl.dynadoc.generator;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

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
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.highlighter.CFGHighlighter;
import com.ensoftcorp.open.slice.analysis.DataDependenceGraph;
import com.ensoftcorp.open.slice.analysis.DependenceGraph;
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
import com.kcsl.dynadoc.Configurations;
import com.kcsl.dynadoc.doc.JavaDocAttributes;
import com.kcsl.dynadoc.doc.JavaDocAttributes.CodeMap;
import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitIssueRelation;
import com.kcsl.supplementary.SupplementaryArtifactConstants.BugzillaIssues;
import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory;

public class ClassDocumentationGenerator {

	private static final String CONSTRUCTORS_SECTION_HEADER = "Constructor Summary";
	
	private static final String METHODS_SECTION_HEADER = "Method Summary";
	
	private static final String FIELDS_SECTION_HEADER = "Field Summary";
	
	private static final String ISSUES_SECTION_HEADER = "Issue Summary";
	
	private static final String COMMIT_SECTION_HEADER = "Revision Control Summary";
	
	private static final String CALL_GRAPH_FILE_NAME_TEMPLATE = "%s-call.svg";
	
	private static final String CFG_GRAPH_FILE_NAME_TEMPLATE = "%s-cfg.svg";
	
	private static final String DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE = "%s-ddg.svg";
	
	private static final String [] CONSTRUCTORS_TABLE_HEADERS = { "Visibility", "Name", "Parameters", "Deprecated", "External Use", "CFG", "Call", "Intra DDG"};

	private static final String [] METHODS_TABLE_HEADERS = { "Visibility", "Return", "Name", "Parameters", "Static", "Instance", "Concrete", "Deprecated", "External Use", "CFG", "Call", "Inra DDG"};

	private static final String [] FIELDS_TABLE_HEADERS = { "Visibility", "Type", "Name", "Static", "Instance", "Final", "Deprecated", "External Use", "Inter DDG"};
	
	private static final String [] ISSUES_TABLE_HEADERS = { "Issue Id", "Last Changed", "Summary", "Status", "Severity", "Priority", "Related Commits", "View Report" };
	
	private static final String [] COMMITS_TABLE_HEADERS = { "Commit Id", "Commiter", "Date/Time", "Summary", "Related Issues", "View Commit" };
		
	private Node classNode;
	
	public ClassDocumentationGenerator(Node classNode) {
		this.classNode = classNode;
	}
	
	public void generate() {
		Html htmlDocument = new Html();
		htmlDocument.setLang("en");
		
		Head head = this.generateHead();
		htmlDocument.appendChild(head);
		
		Body body = this.generateBody();
		htmlDocument.appendChild(body);
		
		try {
			Path classDocHTMLFilePath =  Configurations.getOutputDirectoryPath().resolve(this.getQualifiedName() + ".html");
			PrintWriter out = new PrintWriter(new FileOutputStream(classDocHTMLFilePath.toFile()));
			out.println(htmlDocument.write());
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error while writing the HTML document for class [" + this.getQualifiedName() +"]");
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
		boostrapCSS.setHref("https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css");
		boostrapCSS.setAttribute("integrity", "sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B");
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
		bootstrapScript.setSrc("https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/js/bootstrap.min.js");
		bootstrapScript.setAttribute("integrity", "sha384-o+RDsa0aLu++PJvFqy8fFScvbHFLtbvScb8AjopnFD+iEQ7wo/CG0xlczd+2O/em");
		bootstrapScript.setAttribute("crossorigin", "anonymous");
		scripts.add(bootstrapScript);
		
		// table scripts
		for(String tableScriptFileName: Configurations.getClassDocumentationScriptFileNames()) {
			Script tableScript = new Script("text/javascript");
			tableScript.setSrc(this.getRelativeFilePathString(this.getScriptsDirectoryPath(), tableScriptFileName));
			scripts.add(tableScript);
		}
		
		return scripts;
	}
	
	private Body generateBody() {
		Body body = new Body();
		
		Div breadcrumbSection = this.generateBreadcrumbSection();
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
		
		Div issuesSummarySection = this.generateIssueSummarySection();
		body.appendChild(issuesSummarySection);
		
		Div revisionControlSummarySection = this.generateRevisionControlSummarySection();
		body.appendChild(revisionControlSummarySection);
		
		return body;
	}
	
	private Div generateBreadcrumbSection() {
		Div breadcrumb = new Div();
		breadcrumb.setCSSClass("nav-container");
		breadcrumb.setStyle("position:relative;");
		
			Ol ol = new Ol();
			ol.setCSSClass("breadcrumb");
			
				Li packageLi = new Li();
				packageLi.setCSSClass("breadcrumb-item");
				packageLi.appendText(this.getContainingPackageQualifiedName());
				ol.appendChild(packageLi);
				
				Li classLi = new Li();
				classLi.setCSSClass("breadcrumb-item active");
				classLi.appendText(this.getName());
				
				ol.appendChild(classLi);
			breadcrumb.appendChild(ol);
		
			A guideLink = new A();
			guideLink.setHref(Configurations.getHTMLGuidePagePath());
			guideLink.setTarget("_blank");
			guideLink.setCSSClass("btn btn-bg btn-danger");
			guideLink.setStyle("position:absolute; right:1rem; top:50%; transform:translateY(-50%);");
			guideLink.setAttribute("role", "button");
			guideLink.appendText("DynaDoc Guide");
			
			breadcrumb.appendChild(guideLink);
		
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
			contentDiv.setCSSClass("card-body text-dark small");
			contentDiv.appendChild(this.getClassUserAnnotations());
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
				cardContent.setCSSClass("card-body bg-white text-dark scrollClass");
				cardContent.appendChild(this.getClassPropertiesElement());
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
		
		return cardGroupDiv;
	}
	
	private void generateTypeHierarchyImage(String filePath) {
		Node containingProjectNode = CommonQueries.getContainingNode(this.getClassNode(), XCSG.Project);
		Q containingProjectQ = Common.toQ(containingProjectNode);
		Q containedTypes = containingProjectQ.forwardOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Type);
		Q superTypeEdges = Query.universe().edges(XCSG.Supertype);
		Q forwardTypeHierarchyQ = superTypeEdges.forward(this.getClassQ()).intersection(containedTypes).induce(superTypeEdges);
		Q reverseTypeHierarchyQ = superTypeEdges.reverse(this.getClassQ()).intersection(containedTypes).induce(superTypeEdges);
		Q typeHierarchyQ = forwardTypeHierarchyQ.union(reverseTypeHierarchyQ);
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
		Q containsEdges = Query.universe().edges(XCSG.Contains);
		Q containmentGraphQ = containsEdges.forward(this.getClassQ()).nodes(XCSG.Type).induce(containsEdges);
		containmentGraphQ = Common.extend(containmentGraphQ, XCSG.Contains);
		Markup markup = new Markup();
		markup.set(this.getClassQ(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		this.saveGraph(filePath, containmentGraphQ, markup);
	}
	
	private AtlasSet<Node> getInnerClasses() {
		return this.getClassQ().successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Java.InnerClass).eval().nodes();
	}
	
	private AtlasSet<Node> getConstructors() {
		return this.getClassQ().successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Constructor).eval().nodes();
	}
	
	private AtlasSet<Node> getMethods() {
		Q classMethods = Query.universe().edges(XCSG.Contains).successors(this.getClassQ()).nodes(XCSG.Method);
		Q initMethods = classMethods.selectNode(XCSG.name, "<init>").union(classMethods.selectNode(XCSG.name, "<clinit>"));
		return classMethods.difference(initMethods).eval().nodes();
	}
	
	private AtlasSet<Node> getFields() {
		return this.getClassQ().successorsOn(Query.universe().edges(XCSG.Contains)).nodes(XCSG.Field).eval().nodes();
	}   
	
	private Ul getClassPropertiesElement() {
		
		Ul list = new Ul();
		list.setStyle("margin: 5; padding: 0");
		
			Li hierarchyLi = new Li();
				Strong hierarchyTag = new Strong();
				hierarchyTag.appendText("Hierarchy:");
			hierarchyLi.appendChild(hierarchyTag);
		list.appendChild(hierarchyLi);
		
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
		list.appendChild(code);
		
			Li nestedClassesLi = new Li();
				Strong nestedClassesTag = new Strong();
				nestedClassesTag.appendText("Nested Classes:");
			nestedClassesLi.appendChild(nestedClassesTag);
		list.appendChild(nestedClassesLi);
		
		AtlasSet<Node> nestedClasses = this.getInnerClasses();
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
	
	private Node getExtendedClassNode() {
		return this.getClassQ().successorsOn(Query.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private AtlasSet<Node> getImplementedClassNodes() {
		return this.getClassQ().successorsOn(Query.universe().edges(XCSG.Java.Implements)).nodes(XCSG.Type).eval().nodes();
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
	
	 @SuppressWarnings(value = { "unused" })
	private boolean isInnerClass() {
		return this.getClassNode().taggedWith(XCSG.Java.InnerClass);
	}
	
	private Div generateConstructorsTableSection() {
		Div constructorsTableDiv = new Div();
		constructorsTableDiv.setCSSClass("card text-white bg-primary mb-3");
		constructorsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(CONSTRUCTORS_SECTION_HEADER);
			
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
								Tr constructorRow = this.constructConstructorRow(constructor);
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
		return Query.universe().edges(XCSG.Returns).successors(Common.toQ(methodNode)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private AtlasSet<Node> getParametersForMethod(Node methodNode) {
		return CommonQueries.functionParameter(Common.toQ(methodNode)).eval().nodes();
	}
	
	private boolean isConcreteMethod(Node methodNode) {
		return !methodNode.taggedWith(XCSG.abstractMethod);
	}
	
	private boolean isMethodDeprecated(Node methodNode) {
		return methodNode.taggedWith(JavaDocAttributes.CodeMap.Tags.Deprecated);
	}
	
	private boolean isExternallyUsedMethod(Node methodNode) {
		Q containsEdges = Query.universe().edges(XCSG.Contains);
		Q callEdges = Query.universe().edges(XCSG.Call);
		Q currentContainingClass = containsEdges.predecessors(Common.toQ(methodNode)).nodes(XCSG.Java.Class);
		Q callers = callEdges.predecessors(Common.toQ(methodNode));
		Q callerClasses = containsEdges.reverse(callers).nodes(XCSG.Java.Class);
		callerClasses = callerClasses.difference(currentContainingClass);
		return !callerClasses.eval().nodes().isEmpty();
	}
	
	private void generateCFGImageForMethod(String filePath, Node methodNode) {
		Q cfgQ = CommonQueries.excfg(methodNode);
		cfgQ = Common.extend(cfgQ, XCSG.Contains);
		Markup m = new Markup();
		CFGHighlighter.applyHighlightsForCFG(m);
		this.saveGraph(filePath, cfgQ, m );
	}
	
	private void generateCallHierarchyImageForMethod(String filePath, Node methodNode) {
		Q callEdges = Query.universe().edges(XCSG.Call);
		Q callGraphQ = callEdges.forwardStep(Common.toQ(methodNode)).union(callEdges.reverseStep(Common.toQ(methodNode)));
		callGraphQ = Common.extend(callGraphQ, XCSG.Contains);
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(methodNode, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		UnionMarkup markup = new UnionMarkup(Arrays.asList(nodeMarkup));
		this.saveGraph(filePath, callGraphQ, markup);
	}
	
	private void generateUsageImageForMethod(String filePath, Node methodNode) {
		DataDependenceGraph dataDependenceGraph = DependenceGraph.Factory.buildDDG(methodNode);
		Q dataDependenceGraphQ = dataDependenceGraph.getGraph();
		Q methodParameters = Common.toQ(this.getParametersForMethod(methodNode));
		if(!methodParameters.eval().nodes().isEmpty()) {
			dataDependenceGraphQ = dataDependenceGraphQ.forward(methodParameters);
		}
		dataDependenceGraphQ = Common.extend(dataDependenceGraphQ, XCSG.Contains);
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge && element.taggedWith(DataDependenceGraph.DATA_DEPENDENCE_EDGE)) {
					return new PropertySet().set(MarkupProperty.LABEL_TEXT, DataDependenceGraph.DATA_DEPENDENCE_EDGE);
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(methodParameters, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		UnionMarkup markup = new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
		this.saveGraph(filePath, dataDependenceGraphQ, markup);
	}
	
	private void generateUsageImageForField(String filePath, Node fieldNode) {
		Q dataFlowEdges = Query.universe().edges(XCSG.DataFlow_Edge);
		Q fieldQ = Common.toQ(fieldNode);
		Q dataFlowGraphQ = dataFlowEdges.forwardStep(fieldQ).union(dataFlowEdges.reverseStep(fieldQ));
		dataFlowGraphQ = Common.extend(dataFlowGraphQ, XCSG.Contains);
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge) {
					if(element.taggedWith(XCSG.LocalDataFlow)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "local-dataflow");
					}else if(element.taggedWith(XCSG.InterproceduralDataFlow)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "inter-dataflow");
					}
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(fieldNode, MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		UnionMarkup markup = new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
		this.saveGraph(filePath, dataFlowGraphQ, markup);
	}
	
	private String getUniqueMethodName(Node methodNode) {
		return CommonQueries.getQualifiedFunctionName(methodNode) + "-" + methodNode.getAttr("##signature");
	}
	
	private String getUniqueFiledName(Node fieldNode) {
		return this.getQualifiedName() + "-" + fieldNode.getAttr(XCSG.name);
	}
	
	private Tr constructConstructorRow(Node methodNode) {
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
		
		
		// Name Column
		Td nameColumn = new Td();
		code = new Code();
		code.appendText(methodNode.getAttr(XCSG.name).toString());
		nameColumn.appendChild(code);
		columns.add(nameColumn);
		
		// Parameters Column
		Td parametersColumn = new Td();
		parametersColumn.setStyle("white-space:nowrap");
		AtlasSet<Node> parameterNodes = this.getParametersForMethod(methodNode);
		for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
			Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
			Node typeNode = Query.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
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
		
		// Deprecated Column
		Td deprecatedColumn = new Td();
		if(this.isMethodDeprecated(methodNode)) {
			deprecatedColumn.appendChild(this.getTickImage());
		}
		columns.add(deprecatedColumn);
		
		// Externally Used Column
		Td externalColumn = new Td();
		if(this.isExternallyUsedMethod(methodNode)) {
			externalColumn.appendChild(this.getTickImage());
		}
		columns.add(externalColumn);
		
		// CFG Column
		Td cfgColumn = new Td();
		A CFGlink = new A();
		
		String cfgImageFileName = String.format(CFG_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));
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
		
		String callImageFileName = String.format(CALL_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));
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
		
		String usageImageFileName = String.format(DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));
		String usageImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		String usageImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		
		this.generateUsageImageForMethod(usageImageAbsolutePath, methodNode);
		link.setHref(usageImageRelativePath);
		link.setTarget("_blank");
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
		parametersColumn.setStyle("white-space:nowrap");
		AtlasSet<Node> parameterNodes = this.getParametersForMethod(methodNode);
		for(int parameterIndex = 0; parameterIndex < parameterNodes.size(); parameterIndex++) {
			Node parameterNode = parameterNodes.filter(XCSG.parameterIndex, parameterIndex).one();
			Node typeNode = Query.universe().edges(XCSG.TypeOf).successors(Common.toQ(parameterNode)).nodes(XCSG.Type).eval().nodes().one();
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

		// Static Column
		Td staticColumn = new Td();
		boolean staticMethod = methodNode.taggedWith(XCSG.ClassMethod) || methodNode.taggedWith("static");
		if(staticMethod) {
			staticColumn.appendChild(this.getTickImage());
		}
		columns.add(staticColumn);
		
		// Instance Column
		Td instanceColumn = new Td();
		boolean instanceMethod = methodNode.taggedWith(XCSG.InstanceMethod) || !staticMethod;
		if(instanceMethod) {
			instanceColumn.appendChild(this.getTickImage());
		}
		columns.add(instanceColumn);
				
		// Concrete
		Td concreteMethod = new Td();
		if(this.isConcreteMethod(methodNode)) {
			concreteMethod.appendChild(this.getTickImage());
		}
		columns.add(concreteMethod);
		
		// Deprecated Column
		Td deprecatedColumn = new Td();
		if(this.isMethodDeprecated(methodNode)) {
			deprecatedColumn.appendChild(this.getTickImage());
		}
		columns.add(deprecatedColumn);
		
		
		// Externally Used Column
		Td externalColumn = new Td();
		if(this.isExternallyUsedMethod(methodNode)) {
			externalColumn.appendChild(this.getTickImage());
		}
		columns.add(externalColumn);
		
		// CFG Column
		Td cfgColumn = new Td();
		A CFGlink = new A();
		
		String cfgImageFileName = String.format(CFG_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));;
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
		
		String callImageFileName = String.format(CALL_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));
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
		
		String usageImageFileName = String.format(DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueMethodName(methodNode));
		String usageImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		String usageImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		
		this.generateUsageImageForMethod(usageImageAbsolutePath, methodNode);
		link.setHref(usageImageRelativePath);
		link.setTarget("_blank");
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
		Div commentDiv = new Div();
		if(methodNode.hasAttr(CodeMap.Attributes.Commnets)) {
			String comment = methodNode.getAttr(CodeMap.Attributes.Commnets).toString();
			commentDiv.appendText(comment);
		}
		return commentDiv;
	}
	
	private Div generateFieldAnnotations(Node fieldNode) {
		Div commentDiv = new Div();
		if(fieldNode.hasAttr(CodeMap.Attributes.Commnets)) {
			String comment = fieldNode.getAttr(CodeMap.Attributes.Commnets).toString();
			commentDiv.appendText(comment);
		}
		return commentDiv;
	}
	
	private Div generateFieldsTableSection() {
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-secondary mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(FIELDS_SECTION_HEADER);
			
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
	
	private boolean isDeprecatedField(Node fieldNode) {
		return fieldNode.taggedWith(CodeMap.Tags.Deprecated);
	}
	
	private Node getTypeForField(Node fieldNode) {
		return Query.universe().edges(XCSG.TypeOf).successors(Common.toQ(fieldNode)).nodes(XCSG.Type).eval().nodes().one();
	}
	
	private boolean isExternallyUsedField(Node fieldNode) {
		Q dataFlowEdges = Query.universe().edges(XCSG.DataFlow_Edge);
		Q currentContainingClass = this.getClassQ();
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
		
		// Static Column
		Td staticColumn = new Td();
		boolean staticField = this.isStaticField(fieldNode);
		if(staticField) {
			staticColumn.appendChild(this.getTickImage());
		}
		columns.add(staticColumn);
		
		// Instance Column
		Td instanceColumn = new Td();
		if(!staticField) {
			instanceColumn.appendChild(this.getTickImage());
		}
		columns.add(instanceColumn);
		
		// Final Column
		Td finalColumn = new Td();
		if(this.isFinalField(fieldNode)) {
			finalColumn.appendChild(this.getTickImage());
		}
		columns.add(finalColumn);
		
		// Deprecated Column
		Td deprecatedColumn = new Td();
		if(this.isDeprecatedField(fieldNode)) {
			deprecatedColumn.appendChild(this.getTickImage());
		}
		columns.add(deprecatedColumn);
		
		// Externally Used Column
		Td externalColumn = new Td();
		if(this.isExternallyUsedField(fieldNode)) {
			externalColumn.appendChild(this.getTickImage());
		}
		columns.add(externalColumn);
		
		// Usage Example Column
		Td usageExampleColumn = new Td();
		A link = new A();
		
		String usageImageFileName = String.format(DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE, this.getUniqueFiledName(fieldNode));
		String usageImageAbsolutePath = this.getAbsoluteFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		String usageImageRelativePath = this.getRelativeFilePathString(this.getGraphsDirectoryPath(), usageImageFileName);
		
		this.generateUsageImageForField(usageImageAbsolutePath, fieldNode);
		link.setHref(usageImageRelativePath);
		link.setTarget("_blank");
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
		methodsTableDiv.setCSSClass("card text-white bg-info mb-3");
		methodsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(METHODS_SECTION_HEADER);
			
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

	private Div generateIssueSummarySection() {
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-secondary mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(ISSUES_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId("issues-table");
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: ISSUES_TABLE_HEADERS) {
									Th column = new Th();
									column.appendText(headerText);
									tr.appendChild(column);
								}
							tHead.appendChild(tr);
						table.appendChild(tHead);
						
						Tbody tBody = new Tbody();
						
						AtlasSet<Node> issues = this.getClassIssues();
							for(Node issue: issues) {
								Tr issueRow = this.constructIssueRow(issue);
								tBody.appendChild(issueRow);
							}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < ISSUES_TABLE_HEADERS.length; i++) {
									Th column = new Th();
									tr.appendChild(column);
								}						
							tFoot.appendChild(tr);
						table.appendChild(tFoot);
						
					cardContent.appendChild(table);
				
				cardHeader.appendChild(cardContent);
			
			fieldsTableDiv.appendChild(cardHeader);
		return fieldsTableDiv;
	}
	
	private AtlasSet<Node> getClassIssues() {
		Q linkedToEdges = Query.universe().edges(CommitIssueRelation.PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
		return linkedToEdges.successors(this.getClassQ()).nodes(BugzillaIssues.Tags.ISSUE_NODE_TAG).eval().nodes();
	}
	
	private AtlasSet<Node> getClassCommits() {
		Q linkedToEdges = Query.universe().edges(CommitIssueRelation.PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
		return linkedToEdges.successors(this.getClassQ()).nodes(CommitHistory.Tags.COMMIT_NODE_TAG).eval().nodes();
	}
	
	private Tr constructIssueRow(Node issueNode) {
		List<Td> columns = new ArrayList<Td>();

		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setCSSClass("details-control");
		columns.add(expandColumn);
		
		// Issue Id Column
		Td issueIdColumn = new Td();
		issueIdColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_ID).toString());
		columns.add(issueIdColumn);
		
		// Last Changed Column
		Td lastChangedColumn = new Td();
		lastChangedColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_LAST_CHANGED).toString());
		columns.add(lastChangedColumn);
		
		// Summary Column
		Td summaryColumn = new Td();
		summaryColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_SUMMARY).toString());
		columns.add(summaryColumn);
		
		// Status Column
		Td statusColumn = new Td();
		statusColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_STATUS).toString());
		columns.add(statusColumn);
		
		// Severity Column
		Td severityColumn = new Td();
		severityColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_SEVERITY).toString());
		columns.add(severityColumn);
		
		// Priority Column
		Td priorityColumn = new Td();
		priorityColumn.appendText(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_PRIORITY).toString());
		columns.add(priorityColumn);
		
		// Associated Commits Column
		Td associatedCommitsColumn = new Td();
		associatedCommitsColumn.appendText(this.getCommitsForIssue(issueNode));
		columns.add(associatedCommitsColumn);
		
		// Issue Report Column
		Td issueUrlColumn = new Td();
		A link = new A();
		link.setHref(issueNode.getAttr(BugzillaIssues.Attributes.ISSUE_URL).toString());
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		issueUrlColumn.appendChild(link);
		columns.add(issueUrlColumn);
		
		Tr fieldRow = new Tr();
		for(Td column : columns) {
			fieldRow.appendChild(column);
		}
		return fieldRow;
	}
	
	private Div generateRevisionControlSummarySection() {
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-warning mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(COMMIT_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId("commits-table");
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: COMMITS_TABLE_HEADERS) {
									Th column = new Th();
									column.appendText(headerText);
									tr.appendChild(column);
								}

							tHead.appendChild(tr);
						table.appendChild(tHead);
						
						Tbody tBody = new Tbody();
						
						AtlasSet<Node> commits = this.getClassCommits();
							for(Node commit: commits) {
								Tr commitRow = this.constructCommitRow(commit);
								tBody.appendChild(commitRow);
							}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < COMMITS_TABLE_HEADERS.length; i++) {
									Th column = new Th();
									tr.appendChild(column);
								}						
							tFoot.appendChild(tr);
						table.appendChild(tFoot);
						
					cardContent.appendChild(table);
				
				cardHeader.appendChild(cardContent);
			
			fieldsTableDiv.appendChild(cardHeader);
		return fieldsTableDiv;
	}
	
	private Tr constructCommitRow(Node commitNode) {
		List<Td> columns = new ArrayList<Td>();
		
		// Expand Column
		Td expandColumn = new Td();
		expandColumn.setCSSClass("details-control");
		columns.add(expandColumn);
		
		// Commit Id Column
		Td commitIdColumn = new Td();
		commitIdColumn.appendText(commitNode.getAttr(CommitHistory.Attributes.COMMIT_ID).toString());
		columns.add(commitIdColumn);
		
		// Commiter Column
		Td commiterColumn = new Td();
		commiterColumn.appendText(commitNode.getAttr(CommitHistory.Attributes.COMMIT_COMMITER).toString());
		columns.add(commiterColumn);
		
		// DataTime Column
		Td dataTimeColumn = new Td();
		dataTimeColumn.appendText(commitNode.getAttr(CommitHistory.Attributes.COMMIT_DATA_TIME).toString());
		columns.add(dataTimeColumn);
		
		// Summary Column
		Td summaryColumn = new Td();
		summaryColumn.appendText(commitNode.getAttr(CommitHistory.Attributes.COMMIT_MESSAGE).toString());
		columns.add(summaryColumn);
		
		// Associated Issues Column
		Td associatedIssuesColumn = new Td();
		associatedIssuesColumn.appendText(this.getIssuesForCommit(commitNode));
		columns.add(associatedIssuesColumn);
		
		// Commit URL Column
		Td commitUrlColumn = new Td();
		A link = new A();
		link.setHref(commitNode.getAttr(CommitHistory.Attributes.COMMIT_URL).toString());
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		commitUrlColumn.appendChild(link);
		columns.add(commitUrlColumn);
		
		Tr commitRow = new Tr();
		for(Td column : columns) {
			commitRow.appendChild(column);
		}
		return commitRow;
	}
	
	private String getCommitsForIssue(Node issueNode) {
		StringBuilder sb = new StringBuilder();
		AtlasSet<Node> commits = Query.universe().edges(CommitIssueRelation.COMMIT_LINKED_TO_ISSUE_EDGE_TAG).predecessors(Common.toQ(issueNode)).eval().nodes();
		int index = 0;
		for(Node commit: commits) {
			sb.append(commit.getAttr(CommitHistory.Attributes.COMMIT_ID));
			if(index < commits.size() - 1) {
				sb.append(", ");
			}
			index++;
		}
		return sb.toString();
	}
	
	private String getIssuesForCommit(Node commitNode) {
		StringBuilder sb = new StringBuilder();
		AtlasSet<Node> issues = Query.universe().edges(CommitIssueRelation.COMMIT_LINKED_TO_ISSUE_EDGE_TAG).successors(Common.toQ(commitNode)).eval().nodes();
		int index = 0;
		for(Node issue: issues) {
			sb.append(issue.getAttr(BugzillaIssues.Attributes.ISSUE_ID));
			if(index < issues.size() - 1) {
				sb.append(", ");
			}
			index++;
		}
		return sb.toString();
	}
	
	private Div getClassUserAnnotations() {
		Div commentDiv = new Div();
		if(this.getClassNode().hasAttr(CodeMap.Attributes.Commnets)) {
			String comment = this.getClassNode().getAttr(CodeMap.Attributes.Commnets).toString();
			commentDiv.appendText(comment);
		}
		return commentDiv;
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
	
	private Q getClassQ() {
		return Common.toQ(this.getClassNode());
	}
	
	private Node getClassNode() {
		return this.classNode;
	}
	
	private Path getGraphsDirectoryPath() {
		return Configurations.getOutputGraphsDirectoryPath();
	}
	
	private Path getResourcesDirectoryPath() {
		return  Configurations.getOutputResourcesDirectoryPath();
	}
	
	private Path getScriptsDirectoryPath() {
		return  Configurations.getOutputScriptsDirectoryPath();
	}
	
	private String getAbsoluteFilePathString(Path containingDirectory, String fileName) {
		return containingDirectory.toFile().getAbsolutePath() + IOUtils.DIR_SEPARATOR + fileName;
	}
	
	private String getRelativeFilePathString(Path containingDirectory, String fileName) {
		return containingDirectory.toFile().getName() + IOUtils.DIR_SEPARATOR + fileName;
	}
}
