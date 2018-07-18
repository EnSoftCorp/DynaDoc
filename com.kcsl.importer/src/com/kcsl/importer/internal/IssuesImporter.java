package com.kcsl.importer.internal;

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

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.importer.Activator;

import static com.kcsl.importer.NonProgramArtifacts.Issues;
import static com.kcsl.importer.Configurations.PLUGIN_ISSUES_CSV_FILE_PATH;
import static com.kcsl.importer.Configurations.ISSUE_URL_TEMPLATE;

public class IssuesImporter {

	public static void importData() {
		clean();
		Bundle pluginBundle = Activator.getDefault().getBundle();
		InputStream issuesCSVFileInputStream;
		try {
			issuesCSVFileInputStream = pluginBundle.getEntry(PLUGIN_ISSUES_CSV_FILE_PATH).openStream();
		} catch (IOException e) {
			Log.error("Error reading [" + PLUGIN_ISSUES_CSV_FILE_PATH + "] File.", e);
			return;
		}
		Reader reader = new InputStreamReader(issuesCSVFileInputStream);
        CSVParser csvParser;
		try {
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withIgnoreHeaderCase().withTrim());
	        for (CSVRecord csvRecord : csvParser) {
	        	Node newIssueNode = Graph.U.createNode();
	        	newIssueNode.tag(Issues.Tags.ISSUE_NODE_TAG);
	        	
	        	String bugId = csvRecord.get(0);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_ID, bugId);
	        	
	        	String lastChanges = csvRecord.get(8);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_LAST_CHANGED, lastChanges);
	        	
	        	String summary = csvRecord.get(7);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_SUMMARY, summary);
	        	
	        	String status = csvRecord.get(4);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_STATUS, status);
	        	
	        	String severity = csvRecord.get(5);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_SEVERITY, severity);
	        	
	        	String priority = csvRecord.get(20);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_PRIORITY, priority);
	        	
	        	String bugReportUrl = String.format(ISSUE_URL_TEMPLATE, bugId);
	        	newIssueNode.putAttr(Issues.Attributes.ISSUE_URL, bugReportUrl);
	        	newIssueNode.putAttr(XCSG.name, bugId);
	        }
		} catch (IOException e) {
			Log.error("Error while reading the CSV file: " + PLUGIN_ISSUES_CSV_FILE_PATH, e);
		}
	}
	
	private static void clean() {
		Q issuesQ = Query.universe().nodes(Issues.Tags.ISSUE_NODE_TAG);
		AtlasSet<Node> issueNodes = issuesQ.eval().nodes();
		List<Node> nodes = new ArrayList<Node>();
		for(Node issueNode: issueNodes) {
			nodes.add(issueNode);
		}
		for(Node node: nodes) {
			Graph.U.delete(node);
		}
	}
	
}
