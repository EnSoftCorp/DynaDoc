package com.ensoftcorp.open.dynadoc.core.data;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitHistory;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitIssueRelation;

public class Commit {
	
	private Node commitNode;
	
	private String id;
	
	public Commit(Node commitNode) {
		this.commitNode = commitNode;
		this.setId();
	}
	
	private void setId() {
		this.id = this.getCommitNode().getAttr(CommitHistory.Attributes.COMMIT_ID).toString();
	}
	
	public String id() {
		return this.id;
	}
	
	public String commiter() {
		return this.getCommitNode().getAttr(CommitHistory.Attributes.COMMIT_COMMITER).toString();
	}
	
	public String dateTime() {
		return this.getCommitNode().getAttr(CommitHistory.Attributes.COMMIT_DATA_TIME).toString();
	}
	
	public String summary() {
		return this.getCommitNode().getAttr(CommitHistory.Attributes.COMMIT_MESSAGE).toString();
	}
	
	public AtlasSet<Node> associatedIssues() {
		Q commitsIssuesRelationEdges = Query.universe().edges(CommitIssueRelation.COMMIT_LINKED_TO_ISSUE_EDGE_TAG);
		AtlasSet<Node> issues = commitsIssuesRelationEdges.successors(this.getCommitQ()).eval().nodes();
		return issues;
	}
	
	public String associatedIssuesString() {
		StringBuilder sb = new StringBuilder();
		AtlasSet<Node> issues = this.associatedIssues();
		int index = 0;
		for(Node issue: issues) {
			sb.append(issue.getAttr(BugzillaIssues.Attributes.ISSUE_ID));
			if(index < issues.size() - 1) {
				sb.append(", ");
			}
			index++;
		}
		return sb.toString();
	}
	
	public String url() {
		return this.getCommitNode().getAttr(CommitHistory.Attributes.COMMIT_URL).toString();
	}
	
	public Node getCommitNode() {
		return this.commitNode;
	}
	
	public Q getCommitQ() {
		return Common.toQ(this.getCommitNode());
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
		Commit other = (Commit) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
