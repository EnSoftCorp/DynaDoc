package com.kcsl.dynadoc.data;

import java.util.Collections;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Graph;

public class JavaPackageData {
	
	private String fullyQualifiedName;
	
	public JavaPackageData(String fullyQualifiedPackageName) {
		this.fullyQualifiedName = fullyQualifiedPackageName;
	}
	
	public String getName() {
		return this.fullyQualifiedName;
	}
	
	public List<JavaClassData> getContainedJavaClasses() {
		// TODO
		return Collections.emptyList();
	}
	
	public Graph getUsageExample() {
		// TODO: returns a graph displaying how this package interacts with other.
		return null;
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
		JavaPackageData other = (JavaPackageData) obj;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		return true;
	}

}
