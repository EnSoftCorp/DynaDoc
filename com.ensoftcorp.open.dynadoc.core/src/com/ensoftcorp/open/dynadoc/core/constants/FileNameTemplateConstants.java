package com.ensoftcorp.open.dynadoc.core.constants;

public interface FileNameTemplateConstants {
	
	public static final String IMAGE_FILE_EXTENSION = ".svg";

	public static final String HTML_FILE_NAME_TEMPLATE = "%s.html";
	
	public static final String HIERARCHY_GRAPH_FILE_NAME_TEMPLATE = "%s-hierarchy" + IMAGE_FILE_EXTENSION;
	
	public static final String NESTING_CLASSES_GRAPH_FILE_NAME_TEMPLATE = "%s-nesting" + IMAGE_FILE_EXTENSION;
	
	public static final String CALL_GRAPH_FILE_NAME_TEMPLATE = "%s-call" + IMAGE_FILE_EXTENSION;
	
	public static final String CFG_GRAPH_FILE_NAME_TEMPLATE = "%s-cfg" + IMAGE_FILE_EXTENSION;
	
	public static final String DATA_DEPENDENCY_GRAPH_FILE_NAME_TEMPLATE = "%s-dfg" + IMAGE_FILE_EXTENSION;
}
