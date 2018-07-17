package com.kcsl.importer.internal;

import static com.kcsl.importer.Configurations.COMMIT_URL_TEMPLATE;
import static com.kcsl.importer.Configurations.PLUGIN_COMMITS_CSV_FILE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.kcsl.importer.Activator;
import com.kcsl.importer.NonProgramArtifacts.Commits;

public class CommitsImporter {

	public static void importData() {
		Bundle pluginBundle = Activator.getDefault().getBundle();
		InputStream commitsCSVFileInputStream;
		try {
			commitsCSVFileInputStream = pluginBundle.getEntry(PLUGIN_COMMITS_CSV_FILE_PATH).openStream();
		} catch (IOException e) {
			Log.error("Error reading [" + PLUGIN_COMMITS_CSV_FILE_PATH + "] File.", e);
			return;
		}
		Reader reader = new InputStreamReader(commitsCSVFileInputStream);
        CSVParser csvParser;
		try {
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withIgnoreHeaderCase().withTrim());
	        for (CSVRecord csvRecord : csvParser) {
	        	Node newCommitNode = Graph.U.createNode();
	        	newCommitNode.tag(Commits.Tags.COMMIT_NODE_TAG);
	        	
	        	String commitId = csvRecord.get(0);
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_ID, commitId);
	        	
	        	String commitDataTime = csvRecord.get(8);
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_DATA_TIME, commitDataTime);
	        	
	        	String commitMessage = csvRecord.get(7);
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_MESSAGE, commitMessage);
	        	
	        	String commitUrl = String.format(COMMIT_URL_TEMPLATE, commitId);
	        	newCommitNode.putAttr(Commits.Attributes.COMMIT_URL, commitUrl);
	        }
		} catch (IOException e) {
			Log.error("Error while reading the CSV file: " + PLUGIN_COMMITS_CSV_FILE_PATH, e);
		}
	}
}
