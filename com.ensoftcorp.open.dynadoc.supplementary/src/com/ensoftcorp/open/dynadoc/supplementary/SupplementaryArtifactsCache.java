package com.ensoftcorp.open.dynadoc.supplementary;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.map.AtlasGraphValueHashMap;
import com.ensoftcorp.atlas.core.db.map.AtlasMap;

public final class SupplementaryArtifactsCache {

	private static AtlasMap<String, Node> BUGZILLA_ISSUES_CACHE;
	
	private static AtlasMap<String, Node> COMMIT_HISTORY_RECORDS_CACHE;
	
	static {
		BUGZILLA_ISSUES_CACHE = new AtlasGraphValueHashMap<String, Node>();
		COMMIT_HISTORY_RECORDS_CACHE = new AtlasGraphValueHashMap<String, Node>();
	}
	
	public static void clearBugzillaIssuesCache() {
		BUGZILLA_ISSUES_CACHE.clear();
	}
	
	public static void cacheBugzillaIssue(String id, Node node) {
		BUGZILLA_ISSUES_CACHE.put(id, node);
	}
	
	public static Node getCachedBugzillaIssue(String id) {
		return BUGZILLA_ISSUES_CACHE.get(id);
	}
	
	public static void clearCommitHistoryCache() {
		COMMIT_HISTORY_RECORDS_CACHE.clear();
	}
	
	public static void cacheCommitRecord(String id, Node node) {
		COMMIT_HISTORY_RECORDS_CACHE.put(id, node);
	}
	
	public static Node getCachedCommitRecord(String id) {
		return COMMIT_HISTORY_RECORDS_CACHE.get(id);
	}
}
