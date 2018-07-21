package com.kcsl.dynadoc.conf;

import org.apache.commons.io.IOUtils;

public interface PluginResourceConstants {
	
	public static final String ROOT_DIRECTORY_NAME = "resources";
	
	public static final String ROOT_DIRECTORY_PATH = ROOT_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;;
	
	// "resources/scripts" Directory
	public static interface Scripts {
		
		public static final String SCRIPTS_DIRECTORY_NAME = "scripts";
		
		public static final String SCRIPTS_DIRECTORY_PATH = ROOT_DIRECTORY_PATH + SCRIPTS_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
		
		public static final String[] SCRIPTS_DIRECTORY_CONTENTS_FILE_NAMES = { "jquery-constructors-table-script.js", "jquery-methods-table-script.js", "jquery-fields-table-script.js", "jquery-issues-table-script.js", "jquery-commits-table-script.js" };
	}
	
	// "resources/guide" Directory
	public static interface Guide {
		
		public static final String GUIDE_DIRECTORY_NAME = "guide";
		
		public static final String GUIDE_DIRECTORY_PATH = ROOT_DIRECTORY_PATH + GUIDE_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
		
		public static final String DYANDOC_GUIDE_HTML_PAGE_NAME = "dynadoc-guide.html";
		
		public static final String GUIDE_IMAGES_DIRECTORY_NAME = "slides";
		
		public static final String GUIDE_IMAGES_DIRECTORY_PATH = GUIDE_DIRECTORY_PATH + GUIDE_IMAGES_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
		
		public static final String[] GUIDE_IMAGE_FILE_NAMES = { "slide1.png", "slide2.png", "slide3.png", "slide4.png", "slide5.png", "slide6.png", "slide7.png", "slide8.png", "slide9.png" };
	}
	
	// "resources/resources" Directory
	public static interface Resources {
		
		public static final String RESOURCES_DIRECTORY_NAME = "resources";
		
		public static final String RESOURCES_DIRECTORY_PATH = ROOT_DIRECTORY_PATH + RESOURCES_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
		
		public static final String RESOURCES_CHECK_IMAGE_FILE_NAME = "check.svg";
		
		public static final String RESOURCES_CSS_STYLE_FILE_NAME = "styles.css";
		
		public static final String [] RESOURCES_DIRECTORY_CONTENTS_FILE_NAMES = { RESOURCES_CHECK_IMAGE_FILE_NAME, "details_close.png", "details_open.png", RESOURCES_CSS_STYLE_FILE_NAME };
	}
	
}
