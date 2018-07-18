package com.kcsl.importer.internal;

import static com.kcsl.importer.Configurations.PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.importer.Activator;
import com.kcsl.importer.NonProgramArtifacts;
import com.kcsl.importer.NonProgramArtifacts.Commits;
import com.kcsl.importer.NonProgramArtifacts.Issues;

public class IssuesCommitRelationImporter {
	
	public static void importData() {
		clean();
		Bundle pluginBundle = Activator.getDefault().getBundle();
		InputStream issuesCSVFileInputStream;
		try {
			issuesCSVFileInputStream = pluginBundle.getEntry(PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH).openStream();
		} catch (IOException e) {
			Log.error("Error reading [" + PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH + "] File.", e);
			return;
		}
		
		Q typesQ = Query.resolve(null, Query.universe().nodes(XCSG.Type));
		Q issuesQ = Query.resolve(null, Query.universe().nodes(Issues.Tags.ISSUE_NODE_TAG));
		Q commitsQ = Query.resolve(null, Query.universe().nodes(Commits.Tags.COMMIT_NODE_TAG));
		
		Reader reader = new InputStreamReader(issuesCSVFileInputStream);
        CSVParser csvParser;
		try {
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withIgnoreHeaderCase().withTrim());
	        for (CSVRecord csvRecord : csvParser) {
	        	
	        	String bugId = csvRecord.get(0);
	        	Node bugNode = null;
	        	if(!bugId.isEmpty()) {
	        		bugNode= issuesQ.selectNode(Issues.Attributes.ISSUE_ID, bugId).eval().nodes().one();
	        	}
	        	boolean validBug = bugNode != null;
	        	
	        	String commitId = csvRecord.get(1);
	        	Node commitNode = null;
	        	if(!commitId.equals("*") && !commitId.isEmpty()) {
	        		commitNode = commitsQ.selectNode(Commits.Attributes.COMMIT_ID, commitId).eval().nodes().one();
	        	}
	        	boolean validCommit = commitNode != null;
	        	
	        	String className = csvRecord.get(2);
	        	Node classNode = getClassNodeForJavaFileName(className, typesQ);
	        	boolean validClassNode = classNode != null;
	        	
	        	if(validBug && validClassNode) {
	        		Edge newRelationEdge = Graph.U.createEdge(classNode, bugNode);
	        		newRelationEdge.tag(XCSG.Edge);
	        		newRelationEdge.tag(NonProgramArtifacts.LINKED_TO_EDGE_TAG);
	        	}
	        	
	        	if(validCommit && validClassNode) {
	        		Edge newRelationEdge = Graph.U.createEdge(classNode, commitNode);
	        		newRelationEdge.tag(XCSG.Edge);
	        		newRelationEdge.tag(NonProgramArtifacts.LINKED_TO_EDGE_TAG);
	        	}
	        	
	        	if(validBug && validCommit) {
	        		Edge newRelationEdge = Graph.U.createEdge(commitNode, bugNode);
	        		newRelationEdge.tag(XCSG.Edge);
	        		newRelationEdge.tag(NonProgramArtifacts.LINKED_TO_EDGE_TAG);
	        	}
	        	
	        }
		} catch (IOException e) {
			Log.error("Error while reading the CSV file: " + PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH, e);
		}
	}
	
	private static Node getClassNodeForJavaFileName(String javaFileName, Q typesQ) {
		if(javaFileName.endsWith(".java")) {
			String javaFileNameWithoutExtension = javaFileName.replace(".java", "");
			return typesQ.selectNode(XCSG.name, javaFileNameWithoutExtension).eval().nodes().one();
		}
		return null;
	}

	private static void clean() {
		Q linkedToEdgesQ = Query.universe().edges(NonProgramArtifacts.LINKED_TO_EDGE_TAG);
		AtlasSet<Edge> linkedToEdges = linkedToEdgesQ.eval().edges();
		List<Edge> edges = new ArrayList<Edge>();
		for(Edge linkedToEdge: linkedToEdges) {
			edges.add(linkedToEdge);
		}
		for(Edge edge: edges) {
			Graph.U.delete(edge);
		}
	}
}
