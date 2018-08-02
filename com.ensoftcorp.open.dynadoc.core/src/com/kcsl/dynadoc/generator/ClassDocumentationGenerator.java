package com.kcsl.dynadoc.generator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.Script;
import com.hp.gagawa.java.elements.Title;
import com.kcsl.dynadoc.Configurations;
import com.kcsl.dynadoc.constants.FileNameTemplateConstants;
import com.kcsl.dynadoc.constants.PluginResourceConstants;
import com.kcsl.dynadoc.data.JavaClass;
import com.kcsl.dynadoc.path.WorkingDirectory;
import com.kcsl.dynadoc.path.WorkingDirectoryCache;
import com.kcsl.dynadoc.utils.PathUtils;
import com.kcsl.dynadoc.wrapper.ClassCommitsWrapper;
import com.kcsl.dynadoc.wrapper.ClassConstructorsWrapper;
import com.kcsl.dynadoc.wrapper.ClassFieldsWrapper;
import com.kcsl.dynadoc.wrapper.ClassIssuesWrapper;
import com.kcsl.dynadoc.wrapper.ClassMethodsWrapper;
import com.kcsl.dynadoc.wrapper.ClassOverviewWrapper;

public class ClassDocumentationGenerator {

	private JavaClass javaClass;
	
	private Node classNode;
	
	private WorkingDirectory workingDirectory;
	
	public ClassDocumentationGenerator(Node classNode, WorkingDirectory parentWorkingDirectory) {
		this.classNode = classNode;
		this.javaClass = new JavaClass(classNode);
		this.workingDirectory = WorkingDirectoryCache.createWorkingDirectory(classNode, parentWorkingDirectory);
	}
	
	public Node getClassNode() {
		return this.classNode;
	}
	
	public JavaClass getJavaClass() {
		return this.javaClass;
	}
	
	public WorkingDirectory getWorkingDirectory() {
		return this.workingDirectory;
	}
	
	public void generate() {
		
		if(!Configurations.configureGraphsDirectory(this.workingDirectory)) {
			Log.error("Graph directory not configured correctly for the class: " + this.getJavaClass().getQualifiedName(), new IOException());
			return;
		}
		
		Html htmlDocument = new Html();
		htmlDocument.setLang("en");
		
		Head head = this.generateHead();
		htmlDocument.appendChild(head);
		
		Body body = this.generateBody();
		htmlDocument.appendChild(body);
		
		try {
			String htmlFileName = String.format(FileNameTemplateConstants.HTML_FILE_NAME_TEMPLATE, this.getJavaClass().getQualifiedName());
			Path classDocHTMLFilePath =  this.getWorkingDirectory().getPath().resolve(htmlFileName);
			PrintWriter out = new PrintWriter(new FileOutputStream(classDocHTMLFilePath.toFile()));
			out.println(htmlDocument.write());
			out.close();
		} catch (FileNotFoundException e) {
			Log.error("Error while writing the HTML document for class [" + this.getJavaClass().getQualifiedName() +"]", e);
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
		title.appendText(this.getJavaClass().getQualifiedName());
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
		String ourStyleCSSRelativeFilePathString = PathUtils.getRelativePathStringToCSSFile(this.getWorkingDirectory());
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
		String relativePathStringToScriptsDirectory = PathUtils.getRelativePathStringToScriptsDirectory(this.getWorkingDirectory());
		for(String tableScriptFileName: PluginResourceConstants.Scripts.SCRIPTS_DIRECTORY_CONTENTS_FILE_NAMES) {
			Script tableScript = new Script("text/javascript");
			String relativePathToScriptFile = relativePathStringToScriptsDirectory + tableScriptFileName;
			tableScript.setSrc(relativePathToScriptFile);
			scripts.add(tableScript);
		}
		
		return scripts;
	}
	
	private Body generateBody() {
		Body body = new Body();
		
		long start = System.currentTimeMillis();
		Log.info("classOverviewWrapper");
		ClassOverviewWrapper classOverviewWrapper = new ClassOverviewWrapper(this.getJavaClass());
		body.appendChild(classOverviewWrapper.wrap());
		double duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classOverviewWrapper: " + duration + "s");
		
		start = System.currentTimeMillis();
		Log.info("classConstructorsWrapper");
		ClassConstructorsWrapper classConstructorsWrapper = new ClassConstructorsWrapper(this.getJavaClass());
		body.appendChild(classConstructorsWrapper.wrap());
		duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classConstructorsWrapper: " + duration + "s");
		
		start = System.currentTimeMillis();
		Log.info("classFieldsWrapper");
		ClassFieldsWrapper classFieldsWrapper = new ClassFieldsWrapper(this.getJavaClass());
		body.appendChild(classFieldsWrapper.wrap());
		duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classFieldsWrapper: " + duration + "s");
		
		start = System.currentTimeMillis();
		Log.info("classMethodsWrapper");
		ClassMethodsWrapper classMethodsWrapper = new ClassMethodsWrapper(this.getJavaClass());
		body.appendChild(classMethodsWrapper.wrap());
		duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classMethodsWrapper: " + duration + "s");
		
		start = System.currentTimeMillis();
		Log.info("classIssuesWrapper");
		ClassIssuesWrapper classIssuesWrapper = new ClassIssuesWrapper(this.getJavaClass());
		body.appendChild(classIssuesWrapper.wrap());
		duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classIssuesWrapper: " + duration + "s");
		
		start = System.currentTimeMillis();
		Log.info("classCommitsWrapper");
		ClassCommitsWrapper classCommitsWrapper = new ClassCommitsWrapper(this.getJavaClass());
		body.appendChild(classCommitsWrapper.wrap());
		duration = (System.currentTimeMillis() - start) / 1000.0;
		Log.info("classCommitsWrapper: " + duration + "s");
		
		return body;
	}
}
