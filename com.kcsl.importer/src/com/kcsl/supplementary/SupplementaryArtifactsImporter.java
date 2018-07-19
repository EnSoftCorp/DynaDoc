package com.kcsl.supplementary;

import com.kcsl.supplementary.importer.BugzillaIssuesImporter;
import com.kcsl.supplementary.importer.CommitHistoryImporter;
import com.kcsl.supplementary.importer.IssuesCommitRelationImporter;

public class SupplementaryArtifactsImporter {
	
	public static void importArtifacts() {
		BugzillaIssuesImporter issuesImporter = new BugzillaIssuesImporter();
		issuesImporter.importArtifacts();
		
		CommitHistoryImporter commitsImporter = new CommitHistoryImporter();
		commitsImporter.importArtifacts();
		
		IssuesCommitRelationImporter relationImporter = new IssuesCommitRelationImporter();
		relationImporter.importArtifacts();
	}
	
}
