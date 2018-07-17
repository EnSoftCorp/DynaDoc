package com.kcsl.importer;

public class Configurations {
	
	public static final String ISSUE_URL_TEMPLATE = "https://bz.apache.org/bugzilla/show_bug.cgi?id=%s";
	
	public static final String COMMIT_URL_TEMPLATE = "https://github.com/apache/poi/commit/%s";

	public static final String PLUGIN_ISSUES_CSV_FILE_PATH = "./resources/All _Apache_POI-3.17_Bugzilla_Bugs.csv";
	
	public static final String PLUGIN_COMMITS_CSV_FILE_PATH = "./resources/commits-history.csv";
}
