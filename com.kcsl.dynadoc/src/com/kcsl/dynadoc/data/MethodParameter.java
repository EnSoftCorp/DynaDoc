package com.kcsl.dynadoc.data;

public class MethodParameter {
	
	int index;
	private String name;
	private JavaClassData type;
	
	public MethodParameter(String name, JavaClassData type, int index) {
		this.name = name;
		this.type = type;
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public String getName() {
		return this.name;
	}
	
	public JavaClassData getType() {
		return this.type;
	}

}
