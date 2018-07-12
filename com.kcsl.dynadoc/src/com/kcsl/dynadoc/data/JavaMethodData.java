package com.kcsl.dynadoc.data;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ensoftcorp.atlas.core.db.graph.Graph;

public class JavaMethodData {
	
	private String fullyQualifiedName;
	
	public JavaMethodData(String fullyQualifiedMethodName) {
		this.fullyQualifiedName = fullyQualifiedMethodName;
	}
	
	public String getName() {
		int lastDotIndex = this.getFullyQualifiedName().lastIndexOf(".");
		return this.getFullyQualifiedName().substring(lastDotIndex + 1);
	}
	
	public String getUserComments() {
		// TODO: retrieve the user comments on this elements (if any), otherwise return an empty string.
		return StringUtils.EMPTY;
	}
	
	public boolean isPublic() {
		// TODO
		return false;
	}
	
	public boolean isPrivate() {
		// TODO
		return false;
	}
	
	public boolean isProtected() {
		// TODO
		return false;
	}
	
	public boolean isStatic() {
		// TODO
		return false;
	}
	
	public boolean isDeprecated() {
		// TODO
		return false;
	}
	
	public boolean isInherited() {
		// TODO
		return false;
	}
	
	public boolean isAbstract() {
		// TODO
		return false;
	}
	
	public boolean isUsedOutsideContainingClass() {
		// TODO
		return false;
	}
	
	public boolean isOverriding() {
		// TODO
		return false;
	}
	
	public Graph getClallHierarchy() {
		// TODO
		return null;
	}
	
	public List<JavaMethodParameter> getParameters() {
		// TODO
		return Collections.emptyList();
	}
	
	public List<Graph> getUsageExamples() {
		// TODO: return a list of graph, each graph corresponds to a usage of function
		// A usage can be simply a data flow from the variables in the function calling this function.
		return Collections.emptyList();
	}
	
	
	public String getContainingClassFullyQualifiedName() {
		String classfullyQualifiedName = this.getFullyQualifiedName();
		int lastDotIndex = classfullyQualifiedName.lastIndexOf(".");
		if(lastDotIndex < 0) {
			throw new AssertionError("Improper fully qualified method name: " + this.getFullyQualifiedName());
		}
		return classfullyQualifiedName.substring(0, lastDotIndex);
	}
	
	public String getFullyQualifiedName() {
		return this.fullyQualifiedName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
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
		JavaMethodData other = (JavaMethodData) obj;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		return true;
	}

}
