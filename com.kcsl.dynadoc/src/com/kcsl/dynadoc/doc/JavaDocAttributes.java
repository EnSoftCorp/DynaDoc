package com.kcsl.dynadoc.doc;

public final class JavaDocAttributes {

	public static interface CodeMap {

		public static interface Attributes {
			public static final String Commnets = "Comments";
		}
		
		public static interface Tags {
			public static final String Deprecated = "deprecated";
		}
	}
	
	public static interface JSONData {

		public static final String ClassPackage = "class_package";
		public static final String ClassName = "class_name";
		public static final String ClassQualifiedName = "class_qualified_name";
		public static final String ClassComments = "class_comments";
		public static final String ClassMethods = "class_methods";
		public static final String ClassFields = "class_fields";
	}

}
