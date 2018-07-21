package com.kcsl.dynadoc.data;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.kcsl.supplementary.SupplementaryArtifactConstants.BugzillaIssues;
import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitHistory;
import com.kcsl.supplementary.SupplementaryArtifactConstants.CommitIssueRelation;

public class Issue {

	private Node issueNode;
	
	private String id;
	
	public Issue(Node issueNode) {
		this.issueNode = issueNode;
		this.setId();
	}
	
	private void setId() {
		this.id = this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_ID).toString();
	}
	
	public String id() {
		return this.id;
	}
	
	public String lastChanged() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_LAST_CHANGED).toString();
	}
	
	public String summary() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_SUMMARY).toString();
	}
	
	public String status() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_STATUS).toString();
	}
	
	public String severity() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_SEVERITY).toString();
	}
	
	public String priority() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_PRIORITY).toString();
	}
	
	private AtlasSet<Node> associatedCommits() {
		Q commitsIssuesRelationEdges = Query.universe().edges(CommitIssueRelation.COMMIT_LINKED_TO_ISSUE_EDGE_TAG);
		AtlasSet<Node> commits = commitsIssuesRelationEdges.predecessors(this.getIssueQ()).eval().nodes();
		return commits;
	}
	
	public String associatedCommitsString() {
		StringBuilder sb = new StringBuilder();
		AtlasSet<Node> commits = this.associatedCommits();
		int index = 0;
		for(Node commit: commits) {
			sb.append(commit.getAttr(CommitHistory.Attributes.COMMIT_ID));
			if(index < commits.size() - 1) {
				sb.append(", ");
			}
			index++;
		}
		return sb.toString();
	}
	
	public String url() {
		return this.getIssueNode().getAttr(BugzillaIssues.Attributes.ISSUE_URL).toString();
	}
	
	public Node getIssueNode() {
		return this.issueNode;
	}
	
	public Q getIssueQ() {
		return Common.toQ(this.getIssueNode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Issue other = (Issue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
