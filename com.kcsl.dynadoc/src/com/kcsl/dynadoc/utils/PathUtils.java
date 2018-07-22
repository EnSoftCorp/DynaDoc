package com.kcsl.dynadoc.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.kcsl.dynadoc.constants.OutputDirectoryConstants;
import com.kcsl.dynadoc.constants.PluginResourceConstants;
import com.kcsl.dynadoc.path.WorkingDirectory;

public class PathUtils {
	
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
	
	public static Path promptUserForRootWorkingDirectory(String userPreferedRootWorkingDirectoryPath) {
		final AtomicReference<Path> selectedWorkingDirectoryPath = new AtomicReference<Path>(null);
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
		    @Override
		    public void run() {
		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setText("Select an output directory");
				dialog.setMessage("The output directory will be used to store all the generated documentation files");
				dialog.setFilterPath(userPreferedRootWorkingDirectoryPath);
				selectedWorkingDirectoryPath.set(Paths.get(dialog.open()));
		    }
		});
		return selectedWorkingDirectoryPath.get();
	}
	
}
