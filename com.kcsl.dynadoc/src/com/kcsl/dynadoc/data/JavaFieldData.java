package com.kcsl.dynadoc.data;

public class JavaFieldData {
	
	private String fullyQualifiedFieldName;
	
	public JavaFieldData(String fullyQualifiedFieldName) {
		this.fullyQualifiedFieldName = fullyQualifiedFieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedFieldName == null) ? 0 : fullyQualifiedFieldName.hashCode());
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
		if (fullyQualifiedFieldName == null) {
			if (other.fullyQualifiedFieldName != null)
				return false;
		} else if (!fullyQualifiedFieldName.equals(other.fullyQualifiedFieldName))
			return false;
		return true;
	}

}
