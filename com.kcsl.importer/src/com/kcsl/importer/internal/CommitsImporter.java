package com.kcsl.importer.internal;

import static com.kcsl.importer.Configurations.COMMIT_URL_TEMPLATE;
import static com.kcsl.importer.Configurations.PLUGIN_COMMITS_CSV_FILE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.importer.Activator;
import com.kcsl.importer.NonProgramArtifacts.Commits;

public class CommitsImporter {

	public static void importData() {
		clean();
		
		Bundle pluginBundle = Activator.getDefault().getBundle();
		InputStream commitsCSVFileInputStream;
		try {
			commitsCSVFileInputStream = pluginBundle.getEntry(PLUGIN_COMMITS_CSV_FILE_PATH).openStream();
		} catch (IOException e) {
			Log.error("Error reading [" + PLUGIN_COMMITS_CSV_FILE_PATH + "] File.", e);
			return;
		}
		
		try {
			List<String> commitHistoryLines = IOUtils.readLines(commitsCSVFileInputStream);
			for(String line: commitHistoryLines) {
				String [] properties = line.split("###@@@###");
	        	Node newCommitNode = Graph.U.createNode();
	        	newCommitNode.tag(Commits.Tags.COMMIT_NODE_TAG);
	        	
	        	String commitId = properties[0];
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_ID, commitId);
	        	
	        	String comiter = properties[1];
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_COMMITER, comiter);
	        	
	        	String commitDataTime = properties[2];
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_DATA_TIME, commitDataTime);
	        	
	        	String commitMessage = properties[3];
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_MESSAGE, commitMessage);
	        	
	        	String commitUrl = String.format(COMMIT_URL_TEMPLATE, commitId);
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_URL, commitUrl);
	        	
	        	newCommitNode.putAttr(XCSG.name, commitId);
			}
		} catch (IOException e) {
			Log.error("Error reading [" + PLUGIN_COMMITS_CSV_FILE_PATH + "] File.", e);
		}
	}
	
	private static void clean() {
		Q commitsQ = Query.universe().nodes(Commits.Tags.COMMIT_NODE_TAG);
		AtlasSet<Node> commitNodes = commitsQ.eval().nodes();
		List<Node> nodes = new ArrayList<Node>();
		for(Node commitNode: commitNodes) {
			nodes.add(commitNode);
		}
		for(Node node: nodes) {
			Graph.U.delete(node);
		}
	}
	
}
