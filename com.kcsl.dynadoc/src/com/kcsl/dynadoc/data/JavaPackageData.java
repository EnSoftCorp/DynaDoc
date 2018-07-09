package com.kcsl.dynadoc.data;

public class JavaPackageData {
	
	private String fullyQualifiedPackageName;
	
	public JavaPackageData(String fullyQualifiedPackageName) {
		this.fullyQualifiedPackageName = fullyQualifiedPackageName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedPackageName == null) ? 0 : fullyQualifiedPackageName.hashCode());
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
		if (fullyQualifiedPackageName == null) {
			if (other.fullyQualifiedPackageName != null)
				return false;
		} else if (!fullyQualifiedPackageName.equals(other.fullyQualifiedPackageName))
			return false;
		return true;
	}

}
