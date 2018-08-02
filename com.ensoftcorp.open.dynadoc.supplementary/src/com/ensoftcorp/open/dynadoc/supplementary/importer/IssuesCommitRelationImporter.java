package com.ensoftcorp.open.dynadoc.supplementary.importer;

import static com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitIssueRelation.COMMIT_LINKED_TO_ISSUE_EDGE_TAG;
import static com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitIssueRelation.PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH;
import static com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitIssueRelation.PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.supplementary.Activator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsCache;

public class IssuesCommitRelationImporter implements ISupplementaryArtifactsImporter{
	
	private AtlasSet<Node> javaClassNodes;
	
	public IssuesCommitRelationImporter() {
		this.javaClassNodes = Query.universe().nodes(XCSG.Java.Class).eval().nodes();
	}
	
	@Override
	public void importArtifacts() {
		Reader dataReader = this.getDataReader();
		if(dataReader == null) {
			return;
		}
		
		try {
			CSVParser csvParser = new CSVParser(dataReader, CSVFormat.DEFAULT.withIgnoreHeaderCase().withTrim());
	        for (CSVRecord csvRecord : csvParser) {
	        	
	        	String bugId = csvRecord.get(0);
	        	Node bugNode = SupplementaryArtifactsCache.getCachedBugzillaIssue(bugId);
	        	boolean validBug = bugNode != null;
	        	
	        	String commitId = csvRecord.get(1);
	        	Node commitNode = SupplementaryArtifactsCache.getCachedCommitRecord(commitId);
	        	boolean validCommit = commitNode != null;
	        	
	        	String javaFileName = csvRecord.get(2);
	        	Node classNode = getClassNodeForJavaFileName(javaFileName);
	        	boolean validClassNode = classNode != null;
	        	
	        	if(validBug && validClassNode) {
	        		this.populateRelation(classNode, bugNode, PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
	        	}
	        	
	        	if(validCommit && validClassNode) {
	        		this.populateRelation(classNode, commitNode, PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
	        	}
	        	
	        	if(validBug && validCommit) {
	        		this.populateRelation(commitNode, bugNode, COMMIT_LINKED_TO_ISSUE_EDGE_TAG);
	        	}
	        	
	        }
	        csvParser.close();
		} catch (IOException e) {
			Log.error("Error while importing/populating the commit record and bugzilla issues relation data from the data file: " + PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH, e);
		}
		
	}
	
	private Node getClassNodeForJavaFileName(String javaFileName) {
		String javaFileNameWithoutExtension = javaFileName;
		if(javaFileName.endsWith(".java")) {
			javaFileNameWithoutExtension = javaFileName.replace(".java", "");
		}
		return this.getUniverseJavaClassNodes().one(XCSG.name, javaFileNameWithoutExtension);
	}
	
	private void populateRelation(Node fromNode, Node toNode, String tag) {
		Edge newRelationEdge = Graph.U.createEdge(fromNode, toNode);
		newRelationEdge.tag(XCSG.Edge);
		newRelationEdge.tag(tag);
	}
	
	private Reader getDataReader() {
		try {
			InputStream dataFileInputStream = Activator.getFileURL(PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH).openStream();
			return new InputStreamReader(dataFileInputStream);
		} catch (IOException e) {
			Log.error("Error in openning a stream to the plugin entry: " + PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH, e);
			return null;
		}
	}
	
	private AtlasSet<Node> getUniverseJavaClassNodes() {
		return this.javaClassNodes;
	}

}
