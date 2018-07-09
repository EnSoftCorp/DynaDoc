package com.kcsl.dynadoc.data;

import org.apache.commons.lang3.StringUtils;

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
