package com.kcsl.supplementary;

import java.nio.file.Path;

import com.kcsl.supplementary.importer.BugzillaIssuesImporter;
import com.kcsl.supplementary.importer.CommitHistoryImporter;
import com.kcsl.supplementary.importer.IssuesCommitRelationImporter;
import com.kcsl.supplementary.importer.JavaDocImporter;

public class SupplementaryArtifactsImporter {
	
	public static void importArtifacts(Path rootWorkingDirectory) {
		
		JavaDocImporter javaDocImporter = new JavaDocImporter(rootWorkingDirectory);
		javaDocImporter.importArtifacts();
		
		BugzillaIssuesImporter issuesImporter = new BugzillaIssuesImporter();
		issuesImporter.importArtifacts();
		
		CommitHistoryImporter commitsImporter = new CommitHistoryImporter();
		commitsImporter.importArtifacts();
		
		IssuesCommitRelationImporter relationImporter = new IssuesCommitRelationImporter();
		relationImporter.importArtifacts();
	}
	
}
