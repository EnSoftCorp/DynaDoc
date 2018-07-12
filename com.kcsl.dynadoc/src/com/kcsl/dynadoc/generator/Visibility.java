package com.kcsl.dynadoc.generator;

public enum Visibility {

	PUBLIC("public"),
	PRIVATE("private"),
	PROTECTED("protected");
	
	private String name;
	
	private Visibility(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
}
