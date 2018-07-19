package com.kcsl.supplementary.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.notification.NotificationHashMap;
import com.ensoftcorp.atlas.core.db.notification.NotificationMap;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.supplementary.Activator;

import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory.Attributes;
import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory.Tags;
import com.kcsl.supplementary.SupplementaryArtifactsCache;

import static com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory.PLUGIN_COMMITS_CSV_FILE_PATH;
import static com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory.COMMIT_URL_TEMPLATE;

public class CommitHistoryImporter implements ISupplementaryArtifactsImporter{
	
	private static final String DATA_FILE_LINE_DELIMETER = "###@@@###";
	
	public CommitHistoryImporter() {
		
	}
	
	@Override
	public void importArtifacts() {
		Reader dataReader = this.getDataReader();
		if(dataReader == null) {
			return;
		}
		
		this.clearPreviouslyPopulatedArtifacts();
		
		try {
			List<String> commitHistoryLines = IOUtils.readLines(dataReader);
			for(String line: commitHistoryLines) {
				String [] properties = line.split(DATA_FILE_LINE_DELIMETER);
	        	NotificationMap<String, Object> attributesMap = new NotificationHashMap<String, Object>();
	        	
	        	List<String> tags = new ArrayList<String>();
	        	tags.add(Tags.COMMIT_NODE_TAG);
	        	
	        	String commitId = properties[0];
	        	attributesMap.put(Attributes.COMMIT_ID, commitId);
	        	attributesMap.put(XCSG.name, commitId);
	        	
	        	String comiter = properties[1];
	        	attributesMap.put(Attributes.COMMIT_COMMITER, comiter);
	        	
	        	String commitDataTime = properties[2];
	        	attributesMap.put(Attributes.COMMIT_DATA_TIME, commitDataTime);
	        	
	        	String commitMessage = properties[3];
	        	attributesMap.put(Attributes.COMMIT_MESSAGE, commitMessage);
	        	
	        	String commitUrl = String.format(COMMIT_URL_TEMPLATE, commitId);
	        	attributesMap.put(Attributes.COMMIT_URL, commitUrl);
	        	
	        	this.populateArtifact(attributesMap, tags);
			}
		} catch (IOException e) {
			Log.error("Error while importing/populating Commit hisotry record data from the data file: " + PLUGIN_COMMITS_CSV_FILE_PATH, e);
		}
	}
	
	private void populateArtifact(NotificationMap<String, Object> attributesMap, List<String> tags) {
		Node newNode = Graph.U.createNode();
		newNode.putAllAttr(attributesMap);
		for(String tag: tags) {
			newNode.tag(tag);
		}
		
		SupplementaryArtifactsCache.cacheCommitRecord(attributesMap.get(Attributes.COMMIT_ID).toString(), newNode);
	}
	
	private Reader getDataReader() {
		try {
			InputStream dataFileInputStream = Activator.getFileURL(PLUGIN_COMMITS_CSV_FILE_PATH).openStream();
			return new InputStreamReader(dataFileInputStream);
		} catch (IOException e) {
			Log.error("Error in openning a stream to the plugin entry: " + PLUGIN_COMMITS_CSV_FILE_PATH, e);
			return null;
		}
	}
	
	private void clearPreviouslyPopulatedArtifacts() {
		Q commitsQ = Query.universe().nodes(Tags.COMMIT_NODE_TAG);
		AtlasSet<Node> commitNodes = commitsQ.eval().nodes();
		List<Node> nodes = new ArrayList<Node>();
		for(Node commitNode: commitNodes) {
			nodes.add(commitNode);
		}
		for(Node node: nodes) {
			Graph.U.delete(node);
		}
		
		SupplementaryArtifactsCache.clearCommitHistoryCache();
	}
	
}
