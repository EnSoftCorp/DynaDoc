package com.kcsl.importer;

public class NonProgramArtifacts {
	
	public static final String LINKED_TO_EDGE_TAG = "LinkedTo";
	
	public static interface Issues {
		
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
	
	public static interface Commits {
		
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

}
