package com.kcsl.dynadoc.data;

public class JavaMethodData {
	
	private String fullyQualifiedMethodName;
	
	public JavaMethodData(String fullyQualifiedMethodName) {
		this.fullyQualifiedMethodName = fullyQualifiedMethodName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedMethodName == null) ? 0 : fullyQualifiedMethodName.hashCode());
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
		if (fullyQualifiedMethodName == null) {
			if (other.fullyQualifiedMethodName != null)
				return false;
		} else if (!fullyQualifiedMethodName.equals(other.fullyQualifiedMethodName))
			return false;
		return true;
	}

}
