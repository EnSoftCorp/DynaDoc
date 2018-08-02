package com.ensoftcorp.open.dynadoc.supplementary;

import java.nio.file.Path;

import com.ensoftcorp.open.dynadoc.supplementary.importer.BugzillaIssuesImporter;
import com.ensoftcorp.open.dynadoc.supplementary.importer.CommitHistoryImporter;
import com.ensoftcorp.open.dynadoc.supplementary.importer.IssuesCommitRelationImporter;
import com.ensoftcorp.open.dynadoc.supplementary.importer.JavaDocImporter;

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
