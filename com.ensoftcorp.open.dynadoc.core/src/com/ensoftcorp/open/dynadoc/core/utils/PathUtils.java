package com.ensoftcorp.open.dynadoc.core.utils;

import org.apache.commons.io.IOUtils;

import com.ensoftcorp.open.dynadoc.core.Configurations;
import com.ensoftcorp.open.dynadoc.core.constants.OutputDirectoryConstants;
import com.ensoftcorp.open.dynadoc.core.constants.PluginResourceConstants;
import com.ensoftcorp.open.dynadoc.core.path.WorkingDirectory;

public class PathUtils {
	
	public static WorkingDirectory getDocumentationWorkingDirectory() {
		return Configurations.rootWorkingDirectory().push(OutputDirectoryConstants.DOCUMENTATION_DIRECTORY_NAME);
	}
	
	public static WorkingDirectory getGraphsWorkingDirectory(WorkingDirectory workingDirectory) {
		return workingDirectory.push(OutputDirectoryConstants.GRAPHS_DIRECTORY_NAME);
	}
	
	public static String getRelativePathStringToGraphsDirectory(WorkingDirectory workingDirectory) {
		return OutputDirectoryConstants.GRAPHS_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
	}
	
	public static String getRelativePathStringToResourcesDirectory(WorkingDirectory workingDirectory) {
		return workingDirectory.getRelativePathStringToRootWorkingDirectory() + OutputDirectoryConstants.RESOURCES_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
	}
	
	public static String getRelativePathStringToScriptsDirectory(WorkingDirectory workingDirectory) {
		return workingDirectory.getRelativePathStringToRootWorkingDirectory() + OutputDirectoryConstants.SCRIPTS_DIRECTORY_NAME + IOUtils.DIR_SEPARATOR;
	}

	public static String getRelativePathStringToCheckImage(WorkingDirectory workingDirectory) {
		return getRelativePathStringToResourcesDirectory(workingDirectory) + PluginResourceConstants.Resources.RESOURCES_CHECK_IMAGE_FILE_NAME;
	}
	
	public static String getRelativePathStringToCSSFile(WorkingDirectory workingDirectory) {
		return getRelativePathStringToResourcesDirectory(workingDirectory) + PluginResourceConstants.Resources.RESOURCES_CSS_STYLE_FILE_NAME;
	}
	
	public static String getRelativePathStringToGuidePageFile(WorkingDirectory workingDirectory) {
		return workingDirectory.getRelativePathStringToRootWorkingDirectory() + PluginResourceConstants.Guide.DYANDOC_GUIDE_HTML_PAGE_NAME;
	}
	
}
