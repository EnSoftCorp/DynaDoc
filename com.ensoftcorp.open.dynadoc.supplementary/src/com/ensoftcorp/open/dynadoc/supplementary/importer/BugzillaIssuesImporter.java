package com.ensoftcorp.open.dynadoc.supplementary.importer;

import static com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues.ISSUE_URL_TEMPLATE;
import static com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues.PLUGIN_ISSUES_CSV_FILE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.notification.NotificationHashMap;
import com.ensoftcorp.atlas.core.db.notification.NotificationMap;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.supplementary.Activator;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactsCache;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues.Attributes;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues.Tags;

public class BugzillaIssuesImporter implements ISupplementaryArtifactsImporter{
	
	public BugzillaIssuesImporter() {
		
	}
	
	@Override
	public void importArtifacts() {
		Reader dataReader = this.getDataReader();
		if(dataReader == null) {
			return;
		}
		
		this.clearPreviouslyPopulatedArtifacts();
		
		try {
			CSVParser csvParser = new CSVParser(dataReader, CSVFormat.DEFAULT.withIgnoreHeaderCase().withTrim());
	        for (CSVRecord csvRecord : csvParser) {
	        	NotificationMap<String, Object> attributesMap = new NotificationHashMap<String, Object>();
	        	List<String> tags = new ArrayList<String>();
	        	tags.add(Tags.ISSUE_NODE_TAG);
	        	
	        	String bugId = csvRecord.get(0);
	        	attributesMap.put(Attributes.ISSUE_ID, bugId);
	        	attributesMap.put(XCSG.name, bugId);
	        	
	        	String status = csvRecord.get(4);
	        	attributesMap.put(Attributes.ISSUE_STATUS, status);
	        	
	        	String severity = csvRecord.get(5);
	        	attributesMap.put(Attributes.ISSUE_SEVERITY, severity);
	        	
	        	String summary = csvRecord.get(7);
	        	attributesMap.put(Attributes.ISSUE_SUMMARY, summary);
	        	
	        	String lastChanged = csvRecord.get(8);
	        	attributesMap.put(Attributes.ISSUE_LAST_CHANGED, lastChanged);
	        	
	        	String priority = csvRecord.get(20);
	        	attributesMap.put(Attributes.ISSUE_PRIORITY, priority);
	        	
	        	String bugReportUrl = String.format(ISSUE_URL_TEMPLATE, bugId);
	        	attributesMap.put(Attributes.ISSUE_URL, bugReportUrl);
	        	
	        	this.populateArtifact(attributesMap, tags);
	        }
	        csvParser.close();
		} catch (IOException e) {
			Log.error("Error while importing/populating Bugzilla issue data from the data file: " + PLUGIN_ISSUES_CSV_FILE_PATH, e);
		}
	}
	
	private void populateArtifact(NotificationMap<String, Object> attributesMap, List<String> tags) {
		Node newNode = Graph.U.createNode();
		newNode.putAllAttr(attributesMap);
		for(String tag: tags) {
			newNode.tag(tag);
		}
		
		SupplementaryArtifactsCache.cacheBugzillaIssue(attributesMap.get(Attributes.ISSUE_ID).toString(), newNode);
	}
	
	private Reader getDataReader() {
		try {
			InputStream dataFileInputStream = Activator.getFileURL(PLUGIN_ISSUES_CSV_FILE_PATH).openStream();
			return new InputStreamReader(dataFileInputStream);
		} catch (IOException e) {
			Log.error("Error in openning a stream to the plugin entry: " + PLUGIN_ISSUES_CSV_FILE_PATH, e);
			return null;
		}
	}

	private void clearPreviouslyPopulatedArtifacts() {
		Q issuesQ = Query.universe().nodes(Tags.ISSUE_NODE_TAG);
		AtlasSet<Node> issueNodes = issuesQ.eval().nodes();
		List<Node> nodes = new ArrayList<Node>();
		for(Node issueNode: issueNodes) {
			nodes.add(issueNode);
		}
		for(Node node: nodes) {
			Graph.U.delete(node);
		}
		
		SupplementaryArtifactsCache.clearBugzillaIssuesCache();
	}
	
}
