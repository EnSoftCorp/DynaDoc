package com.kcsl.dynadoc.data;

public class JavaClassData {
	
	private String fullyQualifiedClassName;

	/**
	 * Constructs a new instance of {@link JavaClassData) for the given <code>fullyQualifiedClassName</code>.
	 * 
	 * @param fullyQualifiedClassName
	 */
	public JavaClassData(String fullyQualifiedClassName) {
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedClassName == null) ? 0 : fullyQualifiedClassName.hashCode());
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
		if (fullyQualifiedClassName == null) {
			if (other.fullyQualifiedClassName != null)
				return false;
		} else if (!fullyQualifiedClassName.equals(other.fullyQualifiedClassName))
			return false;
		return true;
	}
}
