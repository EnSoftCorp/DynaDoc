package com.ensoftcorp.open.dynadoc.core.data;

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
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.slice.analysis.DataDependenceGraph;
import com.ensoftcorp.open.slice.analysis.DependenceGraph;

import com.kcsl.supplementary.SupplementaryArtifactConstants.JavaDoc;

import static com.ensoftcorp.open.dynadoc.core.data.QueryCache.callEdges;
import static com.ensoftcorp.open.dynadoc.core.data.QueryCache.containsEdges;
import static com.ensoftcorp.open.dynadoc.core.data.QueryCache.returnsEdges;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class JavaMethod {
	
	private Node methodNode;
	
	private Node containingClassNode;
	
	private String qualifiedName;
	
	private List<JavaParameter> parameters;
	
	private String name;
	
	public JavaMethod(Node containingClassNode, Node methodNode) {
		this.methodNode = methodNode;
		this.name = methodNode.getAttr(XCSG.name).toString();
		this.containingClassNode = containingClassNode;
		// TODO: the qualified name is not unique, I do not see that we use the hashCode or equals anywhere, so we could remove these functions.
		this.qualifiedName = CommonQueries.getQualifiedFunctionName(methodNode);
		this.setParameters();
	}
	
	private void setParameters() {
		this.parameters = new ArrayList<JavaParameter>();
		AtlasSet<Node> parameters = this.getParametersQ().eval().nodes();
		for(Node parameter: parameters) {
			JavaParameter methodParameter = new JavaParameter(parameter);
			this.parameters.add(methodParameter);
		}
		Collections.sort(this.parameters, new Comparator<JavaParameter>() {

			@Override
			public int compare(JavaParameter o1, JavaParameter o2) {
				return o1.getIndex() - o2.getIndex();
			}
		});
	}
	
	private Q getParametersQ() {
		return containsEdges.successors(this.getMethodQ()).nodes(XCSG.Parameter);
	}
	
	public List<JavaParameter> getParameters() {
		return this.parameters;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Node getContainingClassNode() {
		return this.containingClassNode;
	}
	
	public Node getReturnType() {
		return returnsEdges.successors(this.getMethodQ()).nodes(XCSG.Type).eval().nodes().one();
	}
	
	public boolean calledOutsideContainingClass() {
		Q callers = callEdges.predecessors(this.getMethodQ());
		Q callerClasses = containsEdges.reverse(callers).nodes(XCSG.Java.Class);
		callerClasses = callerClasses.difference(Common.toQ(this.getContainingClassNode()));
		return !callerClasses.eval().nodes().isEmpty();
	}
	
	public Q getCFG() {
		Q cfgQ = CommonQueries.excfg(this.getMethodNode());
		cfgQ = QueryCache.extend(cfgQ);
		return cfgQ;
	}
	
	public Q getCallGraph() {
		Q callGraphQ = callEdges.forwardStep(this.getMethodQ()).union(callEdges.reverseStep(this.getMethodQ()));
		callGraphQ = QueryCache.extend(callGraphQ);
		return callGraphQ;
	}
	
	public Q getDataDependenceGraph() {
		DataDependenceGraph dataDependenceGraph = DependenceGraph.Factory.buildDDG(methodNode);
		Q dataDependenceGraphQ = dataDependenceGraph.getGraph();
		if(!this.getParameters().isEmpty()) {
			dataDependenceGraphQ = dataDependenceGraphQ.forward(this.getParametersQ());
		}
		dataDependenceGraphQ = QueryCache.extend(dataDependenceGraphQ);
		return dataDependenceGraphQ;
	}
	
	public String getSignature() {
		StringBuilder methodSignature = new StringBuilder();
		methodSignature.append(this.getName());
		List<JavaParameter> parameters = this.getParameters();
		for(JavaParameter parameter: parameters) {
			methodSignature.append(JavaDoc.METHOD_SIGNATURE_SEPARATOR);
			methodSignature.append(parameter.getSignature());
		}
		return methodSignature.toString();
	}
	
	public String getComments() {
		if(this.getMethodNode().hasAttr(JavaDoc.Attributes.Commnets)) {
			return this.getMethodNode().getAttr(JavaDoc.Attributes.Commnets).toString();
		}
		return StringUtils.EMPTY;
	}
	
	public String getVisibility() {
		if(privateMethod()) {
			return "private";
		}
		if(protectedMethod()) {
			return "protected";
		}
		return "public";
	}
	
	public boolean contructorMethod() {
		return this.getMethodNode().taggedWith(XCSG.Constructor);
	}
	
	public boolean staticMethod() {
		return this.getMethodNode().taggedWith(XCSG.ClassMethod) || this.getMethodNode().taggedWith("static");
	}
	
	public boolean instanceMethod() {
		return this.getMethodNode().taggedWith(XCSG.InstanceMethod) || !this.staticMethod();
	}
	
	private boolean abstractMethod() {
		return this.getMethodNode().taggedWith(XCSG.abstractMethod);
	}
	
	public boolean concreteMethod() {
		return !this.abstractMethod();
	}
	
	public boolean deprecatedMethod() {
		return this.getMethodNode().taggedWith(JavaDoc.Tags.Deprecated);
	}
	
	public boolean publicMethod() {
		return this.getMethodNode().taggedWith(XCSG.publicVisibility) || !(privateMethod() || protectedMethod());
	}
	
	public boolean protectedMethod() {
		return this.getMethodNode().taggedWith(XCSG.protectedPackageVisibility);
	}
	
	public boolean privateMethod() {
		return this.getMethodNode().taggedWith(XCSG.privateVisibility);
	}
	
	public Node getMethodNode() {
		return this.methodNode;
	}
	
	public Q getMethodQ() {
		return Common.toQ(this.getMethodNode());
	}
	
	public String getQualifiedName() {
		return this.qualifiedName;
	}
	
	public IMarkup getCFGMarkup() {
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge && element.taggedWith(XCSG.ControlFlow_Edge)) {
					
					PropertySet propertySet = new PropertySet();
					if(element.taggedWith(XCSG.ExceptionalControlFlow_Edge)) {
						propertySet.set(MarkupProperty.EDGE_COLOR, Color.BLUE);
						return propertySet;
					}
					
					boolean conditionalEdge = element.hasAttr(XCSG.conditionValue);
					if(conditionalEdge) {
						Object conditionValue = element.getAttr(XCSG.conditionValue);
						if(conditionValue.equals(true) || conditionValue.equals(Boolean.TRUE) || conditionValue.equals("true")) {
							propertySet.set(MarkupProperty.EDGE_COLOR, Color.WHITE);
							propertySet.set(MarkupProperty.LABEL_TEXT, "true");
							return propertySet;
						}
						if(conditionValue.equals(false) || conditionValue.equals(Boolean.FALSE) || conditionValue.equals("false")) {
							propertySet.set(MarkupProperty.EDGE_COLOR, Color.BLACK);
							propertySet.set(MarkupProperty.LABEL_TEXT, "false");
							return propertySet;
						}
					}
					
					propertySet.set(MarkupProperty.EDGE_COLOR, Color.GRAY);
					return propertySet;
				}
				return new PropertySet();
			}
		};
		return labelEdgesMarkup;
	}
	
	public IMarkup getCallGraphMarkup() {
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(this.getMethodNode(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		return new UnionMarkup(Arrays.asList(nodeMarkup));
	}
	
	public IMarkup getDataDependencyGraphMarkup() {
		Markup labelEdgesMarkup = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge && element.taggedWith(DataDependenceGraph.DATA_DEPENDENCE_EDGE)) {
					return new PropertySet().set(MarkupProperty.LABEL_TEXT, DataDependenceGraph.DATA_DEPENDENCE_EDGE);
				}
				return new PropertySet();
			}
		};
		Markup nodeMarkup = new Markup();
		nodeMarkup.set(this.getParametersQ(), MarkupProperty.NODE_BACKGROUND_COLOR, Color.YELLOW);
		return new UnionMarkup(Arrays.asList(nodeMarkup, labelEdgesMarkup));
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
		JavaMethod other = (JavaMethod) obj;
		if (qualifiedName == null) {
			if (other.qualifiedName != null)
				return false;
		} else if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}
}
