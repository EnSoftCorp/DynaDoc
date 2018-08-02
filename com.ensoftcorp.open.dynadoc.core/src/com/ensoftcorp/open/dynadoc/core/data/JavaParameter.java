package com.ensoftcorp.open.dynadoc.core.data;

import static com.ensoftcorp.open.dynadoc.core.data.QueryCache.typeOfEdges;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.dynadoc.supplementary.SupplementaryArtifactConstants.JavaDoc;

public class JavaParameter {
	
	private Node parameterNode;
	
	private int index;
	
	private String name;

	public JavaParameter(Node parameterNode) {
		this.parameterNode = parameterNode;
		this.index = (int) parameterNode.getAttr(XCSG.parameterIndex);
		this.name = this.parameterNode.getAttr(XCSG.name).toString();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSignature() {
		return this.getName() + JavaDoc.METHOD_PARAMETER_TYPE_SEPARATOR + this.getSimpleTypeName();
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public Node getParameterNode() {
		return this.parameterNode;
	}
	
	public Q getParameterQ() {
		return Common.toQ(this.getParameterNode());
	}
	
	public Node getTypeNode() {
		return typeOfEdges.successors(this.getParameterQ()).nodes(XCSG.Type).eval().nodes().one();
	}
	
	public String getTypeName() {
		return this.getTypeNode().getAttr(XCSG.name).toString();
	}
	
	public String getSimpleTypeName() {
		String typeName = this.getTypeName();
		
		int indextOfArrayBracket = typeName.indexOf("[");
		if(indextOfArrayBracket >= 0) {
			typeName = typeName.substring(0, indextOfArrayBracket);
		}
		
		int indexOfMapCarrent = typeName.indexOf("<");
		if(indexOfMapCarrent >= 0) {
			typeName = typeName.substring(0, indexOfMapCarrent);
		}
		
		return typeName.trim();
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
		JavaParameter other = (JavaParameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
