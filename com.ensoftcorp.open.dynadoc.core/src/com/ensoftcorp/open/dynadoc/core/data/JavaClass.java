package com.ensoftcorp.open.dynadoc.core.data;

import static com.ensoftcorp.open.dynadoc.core.data.QueryCache.containsEdges;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.PropertySet;
import com.ensoftcorp.atlas.core.markup.UnionMarkup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.BugzillaIssues;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitHistory;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.CommitIssueRelation;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.JavaDoc;


public class JavaClass {
	
	private Node classNode;
	
	private String qualifiedName;
	
	private String name;
	
	private List<JavaMethod> methods;
	
	private List<JavaField> fields;
	
	private List<Issue> issues;
	
	private List<Commit> commits;

	public JavaClass(Node classNode) {
		this.classNode = classNode;
		this.name = this.classNode.getAttr(XCSG.name).toString();
		this.qualifiedName = CommonQueries.getQualifiedTypeName(classNode);
		this.setMethods();
		this.setFields();
		this.setIssues();
		this.setCommits();
	}
	
	public String getName() {
		return this.name;
	}
	
	private void setMethods() {
		this.methods = new ArrayList<JavaMethod>();
		AtlasSet<Node> methodNodes = this.getMethodsQ().eval().nodes();
		for(Node methodNode: methodNodes) {
			JavaMethod method = new JavaMethod(this.getClassNode(), methodNode);
			this.methods.add(method);
		}
	}
	
	private void setFields() {
		this.fields = new ArrayList<JavaField>();
		AtlasSet<Node> fieldNodes = this.getFieldsQ().eval().nodes();
		for(Node fieldNode: fieldNodes) {
			JavaField field = new JavaField(this.getClassNode(), fieldNode);
			this.fields.add(field);
		}
	}
	
	private void setIssues() {
		this.issues = new ArrayList<Issue>();
		AtlasSet<Node> issueNodes = this.getIssuesQ().eval().nodes();
		for(Node issueNode: issueNodes) {
			Issue issue = new Issue(issueNode);
			this.issues.add(issue);
		}
	}
	
	private void setCommits() {
		this.commits = new ArrayList<Commit>();
		AtlasSet<Node> commitNodes = this.getCommitsQ().eval().nodes();
		for(Node commitNode: commitNodes) {
			Commit commit = new Commit(commitNode);
			this.commits.add(commit);
		}
	}
	
	public List<JavaMethod> getMethods() {
		return this.methods;
	}
	
	public List<JavaField> getFields() {
		return this.fields;
	}
	
	public List<Issue> getIssues() {
		return this.issues;
	}
	
	public List<Commit> getCommits() {
		return this.commits;
	}
	
	private Q getFieldsQ() {
		return containsEdges.successors(this.getClassQ()).nodes(XCSG.Field);
	}
	
	public List<JavaMethod> getConstructors() {
		List<JavaMethod> constructors = new ArrayList<JavaMethod>();
		for(JavaMethod method: this.getMethods()) {
			if(method.contructorMethod()) {
				constructors.add(method);
			}
		}
		return constructors;
	}
	
	private Q getMethodsQ() {
		AtlasSet<Node> methodNodes = containsEdges.successors(this.getClassQ()).nodes(XCSG.Method).eval().nodes();
		AtlasSet<Node> excludeNodes = methodNodes.filter(XCSG.name, "<init>", "<clinit>");
		return Common.toQ(methodNodes).difference(Common.toQ(excludeNodes));
	}
	
	private Q getIssuesQ() {
		Q programToSupplementaryEdges = Query.universe().edges(CommitIssueRelation.PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
		return programToSupplementaryEdges.successors(this.getClassQ()).nodes(BugzillaIssues.Tags.ISSUE_NODE_TAG);
	}
	
	private Q getCommitsQ() {
		Q programToSupplementaryEdges = Query.universe().edges(CommitIssueRelation.PROGRAM_ARTIFACT_LINKED_TO_SUPPLEMENTARY_ARTIFACT_EDGE_TAG);
		return programToSupplementaryEdges.successors(this.getClassQ()).nodes(CommitHistory.Tags.COMMIT_NODE_TAG);
	}
	
	public Q getTypeHierarchyGraph() {
		Q superTypeEdges = Query.resolve(null, Query.universe().edges(XCSG.Supertype));
		Q containingProjectQ = Common.toQ(this.getProject());
		Q containedTypes = containsEdges.forward(containingProjectQ);
		Q forwardTypeHierarchyQ = superTypeEdges.forward(this.getClassQ());
		Q reverseTypeHierarchyQ = superTypeEdges.reverse(this.getClassQ());
		Q typeHierarchyQ = forwardTypeHierarchyQ.union(reverseTypeHierarchyQ);
		typeHierarchyQ = typeHierarchyQ.intersection(containedTypes).induce(superTypeEdges);
		typeHierarchyQ = QueryCache.extend(typeHierarchyQ);
		return typeHierarchyQ;
	}
	
	public Q getClassNestingGraph() {
		Q containmentGraphQ = containsEdges.forward(this.getClassQ()).nodes(XCSG.Type);
		containmentGraphQ = QueryCache.extend(containmentGraphQ);
		return containmentGraphQ;
	}
	
	public Node getExtendedClass() {
		Q extendsEdges = Query.universe().edges(XCSG.Java.Extends);
		return extendsEdges.successors(this.getClassQ()).eval().nodes().one(XCSG.Type);
	}
	
	public AtlasSet<Node> getImplementedClasses() {
		Q implementsEdges = Query.universe().edges(XCSG.Java.Implements);
		return implementsEdges.successors(this.getClassQ()).eval().nodes().tagged(XCSG.Type);
	}
	
	public AtlasSet<Node> getInnerClasses() {
		return containsEdges.successors(this.getClassQ()).nodes(XCSG.Java.Class).eval().nodes();
	}
	
	public String getComments() {
		if(this.getClassNode().hasAttr(JavaDoc.Attributes.Commnets)) {
			return this.getClassNode().getAttr(JavaDoc.Attributes.Commnets).toString();
		}
		return StringUtils.EMPTY;
	}
	
	public String getVisibility() {
		if(this.privateClass()) {
			return "private";
		}
		if(this.protectedClass()) {
			return "protected";
		}
		return "public";
	}
	
	private Node getPackage() {
		return CommonQueries.getContainingNode(this.getClassNode(), XCSG.Package);
	}
	
	public String getPackageName() {
		return this.getPackage().getAttr(XCSG.name).toString();
	}
	
	private Node getProject() {
		return CommonQueries.getContainingNode(this.getClassNode(), XCSG.Project);
	}
	
	public String getQualifiedName() {
		return this.qualifiedName;
	}
	
	private boolean privateClass() {
		return this.getClassNode().taggedWith(XCSG.privateVisibility);
	}
	
	private boolean protectedClass() {
		return this.getClassNode().taggedWith(XCSG.protectedPackageVisibility);
	}
	
	public boolean publicClass() {
		return this.getClassNode().taggedWith(XCSG.publicVisibility) || !(privateClass() || protectedClass());
	}

	public boolean abstractClass() {
		return this.getClassNode().taggedWith(XCSG.Java.AbstractClass);
	}

	public boolean finalClass() {
		return this.getClassNode().taggedWith(XCSG.Java.finalClass);
	}

	public boolean staticClass() {
		return this.getClassNode().taggedWith("static");
	}

	public boolean interfaceClass() {
		return this.getClassNode().taggedWith(XCSG.Java.Interface);
	}

	public boolean enumClass() {
		return this.getClassNode().taggedWith(XCSG.Java.Enum);
	}

	private boolean isInnerClass() {
		return this.getClassNode().taggedWith(XCSG.Java.InnerClass);
	}
	
	public IMarkup getTypeHierarchyGraphMarkup() {
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge && element.taggedWith(XCSG.Supertype)) {
					if(element.taggedWith(XCSG.Java.Extends)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "extends");
					}else if(element.taggedWith(XCSG.Java.Implements)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "implements");
					}
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(this.getClassNode(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		return new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
	}
	
	public IMarkup getClassNestingGraphMarkup() {
		Markup markup = new Markup();
		markup.set(this.getClassQ(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		return markup;
	}
	
	public Node getClassNode() {
		return this.classNode;
	}
	
	private Q getClassQ() {
		return Common.toQ(this.getClassNode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
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
		JavaClass other = (JavaClass) obj;
		if (qualifiedName == null) {
			if (other.qualifiedName != null)
				return false;
		} else if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}
	
}
