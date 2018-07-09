package com.kcsl.dynadoc.data;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ensoftcorp.atlas.core.db.graph.Graph;

public class JavaClassData {
	
	private String fullyQualifiedName;

	public JavaClassData(String fullyQualifiedClassName) {
		this.fullyQualifiedName = fullyQualifiedClassName;
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
	
	public boolean isAbstract() {
		// TODO
		return false;
	}
	
	public boolean isInterface() {
		// TODO
		return false;
	}
	
	public boolean isEnum() {
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
	
	public Graph getClassHierarchyGraph() {
		// TODO: return the type hierarchy for this graph
		return null;
	}
	
	public Graph getClassUsageGraph() {
		// TODO: return a graph of classes that uses this class
		return null;
	}
	
	public List<JavaMethodData> getConstructors() {
		// TODO
		return Collections.emptyList();
	}
	
	public List<JavaFieldData> getClassFields() {
		// TODO
		return Collections.emptyList();
	}
	
	public List<JavaMethodData> getClassMethods() {
		// TODO
		return Collections.emptyList();
	}
	
	public List<Graph> getUsageExamples() {
		// TODO: return a list of graph where each shows a simple usage for this class. A usage can show how the class is constructed (for example).
		return Collections.emptyList();
	}
	
	public String getContainingPackageFullyQualifiedName() {
		String packagefullyQualifiedName = this.getFullyQualifiedName();
		int lastDotIndex = packagefullyQualifiedName.lastIndexOf(".");
		if(lastDotIndex < 0) {
			throw new AssertionError("Improper fully qualified class name: " + this.getFullyQualifiedName());
		}
		return packagefullyQualifiedName.substring(0, lastDotIndex);
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
		JavaClassData other = (JavaClassData) obj;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		return true;
	}
}
