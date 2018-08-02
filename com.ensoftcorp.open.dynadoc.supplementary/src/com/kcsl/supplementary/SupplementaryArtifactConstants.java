package com.kcsl.supplementary;

import com.ensoftcorp.open.dynadoc.doclet.JavaDocConstants;

public class SupplementaryArtifactConstants {
	
	public static final String PLUGIN_RESOURCES_DIRECTORY = "./resources/";
	
	public static interface JavaDoc {
		
		public static final String JAVADOC_OUTPUT_DIRECTORY_NAME = "javadoc";
		
		public static final String METHOD_SIGNATURE_SEPARATOR = JavaDocConstants.JAVA_DOC_METHOD_SIGNATURE_SEPARATOR;
		
		public static final String METHOD_PARAMETER_TYPE_SEPARATOR = JavaDocConstants.JAVA_DOC_METHOD_PARAMETER_TYPE_SEPARATOR;

		public static interface Attributes {
			public static final String Commnets = "Comments";
		}
		
		public static interface Tags {
			public static final String Deprecated = "deprecated";
		}
	}
	
	public static interface CommitHistory {
		
		public static final String COMMIT_URL_TEMPLATE = "https://github.com/apache/poi/commit/%s";
		
		public static final String PLUGIN_COMMITS_CSV_FILE_PATH = PLUGIN_RESOURCES_DIRECTORY + "commits-history.csv";
		
		public static interface Attributes {
			public static final String COMMIT_ID = "CommitId";
			public static final String COMMIT_MESSAGE = "CommitMessage";
			public static final String COMMIT_COMMITER = "CommitCommiter";
			public static final String COMMIT_DATA_TIME = "CommitDataTime";
			public static final String COMMIT_URL = "CommitUrl";
		}
		
		public static interface Tags {
			public static final String COMMIT_NODE_TAG = "Commit";
		}
	}
	
	public static interface BugzillaIssues {
	
		public static final String ISSUE_URL_TEMPLATE = "https://bz.apache.org/bugzilla/show_bug.cgi?id=%s";
		
		public static final String PLUGIN_ISSUES_CSV_FILE_PATH = PLUGIN_RESOURCES_DIRECTORY + "All _Apache_POI-3.17_Bugzilla_Bugs.csv";
		
		public static interface Attributes {
			public static final String ISSUE_ID = "IssueId";
			public static final String ISSUE_LAST_CHANGED = "IssueLastChanged";
			public static final String ISSUE_SUMMARY = "IssueSummary";
			public static final String ISSUE_STATUS = "IssueStatus";
			public static final String ISSUE_SEVERITY = "IssueSeverity";
			public static final String ISSUE_PRIORITY = "IssuePriority";
			public static final String ISSUE_URL = "IssueUrl";
		}
		
		public static interface Tags {
			public static final String ISSUE_NODE_TAG = "Issue";
		}
	}
	
	public static interface CommitIssueRelation {
		
		public static final String PLUGIN_COMMITS_ISSUES_RELATION_FILE_PATH = PLUGIN_RESOURCES_DIRECTORY + "commits-bug-relation.csv";
		
		public static final String COMMIT_LINKED_TO_ISSUE_EDGE_TAG = "ConnetsTo";
		
		public static final String PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG = "LinksTo";
		
	}
	
}
