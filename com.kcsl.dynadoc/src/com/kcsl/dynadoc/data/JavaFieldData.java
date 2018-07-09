package com.kcsl.dynadoc.data;

public class JavaFieldData {
	
	private String fullyQualifiedName;
	
	public JavaFieldData(String fullyQualifiedFieldName) {
		this.fullyQualifiedName = fullyQualifiedFieldName;
	}
	
	public String getName() {
		int lastDotIndex = this.getFullyQualifiedName().lastIndexOf(".");
		return this.getFullyQualifiedName().substring(lastDotIndex + 1);
	}
	
	public String getContainingClassFullyQualifiedName() {
		String classfullyQualifiedName = this.getFullyQualifiedName();
		int lastDotIndex = classfullyQualifiedName.lastIndexOf(".");
		if(lastDotIndex < 0) {
			throw new AssertionError("Improper fully qualified field name: " + this.getFullyQualifiedName());
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
		JavaFieldData other = (JavaFieldData) obj;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null)
				return false;
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
			return false;
		return true;
	}

}
