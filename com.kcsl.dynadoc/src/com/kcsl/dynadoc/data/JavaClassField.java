package com.kcsl.dynadoc.data;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.PropertySet;
import com.ensoftcorp.atlas.core.markup.UnionMarkup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.supplementary.SupplementaryArtifactConstants.JavaDoc;

import static com.kcsl.dynadoc.data.QueryCache.typeOfEdges;
import static com.kcsl.dynadoc.data.QueryCache.dataFlowEdges;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class JavaClassField {

	private Node fieldNode;
	
	private Node containingClassNode;
	
	private String name;
	
	public JavaClassField(Node containingClassNode, Node fieldNode) {
		this.fieldNode = fieldNode;
		this.containingClassNode = containingClassNode;
		this.name = fieldNode.getAttr(XCSG.name).toString();
	}
	
	public boolean calledOutsideContainingClass() {
		Q dataDependencyGraph = this.getDataDependenceGraph();
		Q containingClassesQ = dataDependencyGraph.nodes(XCSG.Java.Class);
		containingClassesQ = containingClassesQ.difference(Common.toQ(this.getContainingClassNode()));
		return !containingClassesQ.eval().nodes().isEmpty();
	}
	
	public Q getDataDependenceGraph() {
		Q dataFlowGraphQ = dataFlowEdges.forwardStep(this.getFieldQ()).union(dataFlowEdges.reverseStep(this.getFieldQ()));
		dataFlowGraphQ = QueryCache.extend(dataFlowGraphQ);
		return dataFlowGraphQ;
	}
	
	public Node getTypeNode() {
		return typeOfEdges.successors(this.getFieldQ()).nodes(XCSG.Type).eval().nodes().one();
	}
	
	public String getComments() {
		if(this.getFieldNode().hasAttr(JavaDoc.Attributes.Commnets)) {
			return this.getFieldNode().getAttr(JavaDoc.Attributes.Commnets).toString();
		}
		return StringUtils.EMPTY;
	}
	
	public String getVisibility() {
		if(this.privateFiled()) {
			return "private";
		}
		if(this.protectedField()) {
			return "protected";
		}
		return "public";
	}
	
	public boolean staticField() {
		return this.getFieldNode().taggedWith(XCSG.ClassVariable) || this.getFieldNode().taggedWith("static");
	}
	
	public boolean instanceField() {
		return !this.staticField();
	}
	
	public boolean finalField() {
		return this.getFieldNode().taggedWith(XCSG.immutable) || this.getFieldNode().taggedWith("final");
	}
	
	public boolean deprecatedField() {
		return this.getFieldNode().taggedWith(JavaDoc.Tags.Deprecated);
	}
	
	public boolean publicField() {
		return this.getFieldNode().taggedWith(XCSG.publicVisibility) || !(this.privateFiled() || this.protectedField());
	}
	
	public boolean protectedField() {
		return this.getFieldNode().taggedWith(XCSG.protectedPackageVisibility);
	}
	
	public boolean privateFiled() {
		return this.getFieldNode().taggedWith(XCSG.privateVisibility);
	}
	
	public String getName() {
		return this.name;
	}
	
	public Node getFieldNode() {
		return this.fieldNode;
	}
	
	public Node getContainingClassNode() {
		return this.containingClassNode;
	}
	
	public Q getFieldQ() {
		return Common.toQ(this.getFieldNode());
	}
	
	public IMarkup getDataDependencyGraphMarkup() {
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge) {
					if(element.taggedWith(XCSG.LocalDataFlow)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "local-dataflow");
					}else if(element.taggedWith(XCSG.InterproceduralDataFlow)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, "inter-dataflow");
					}
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(this.getFieldQ(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		return new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		JavaClassField other = (JavaClassField) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
