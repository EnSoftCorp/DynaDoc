package com.kcsl.dynadoc.data;

public class MethodParameter {
	
	int index;
	private String name;
	private String fullyQualifiedClassName;
	
	public MethodParameter(String name, String fullyQualifiedClassName, int index) {
		this.name = name;
		this.fullyQualifiedClassName = fullyQualifiedClassName;
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getFullyQualifiedClassName() {
		return this.fullyQualifiedClassName;
	}

}
