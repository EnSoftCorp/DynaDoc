package com.ensoftcorp.open.dynadoc.support;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.ensoftcorp.open.commons.ui.components.InputDialog;

/**
 * A set of helper utilities to prompt user with different dialogs.
 */
public class DialogUtils {

	/**
	 * Opens a display prompt alerting the user of the error 
	 * 
	 * @param message the message to display
	 */
	public static void showError(final String message) {
		final Display display = getDisplay();
		display.syncExec(new Runnable(){
			@Override
			public void run() {
				MessageBox mb = new MessageBox(getShell(display), SWT.ICON_ERROR | SWT.OK);
				mb.setText("Alert");
				mb.setMessage(message);
				mb.open();
			}
		});
	}
	
	/**
	 * Opens a display prompt to acquire user input.
	 * 
	 * @param title the title to display
	 * @param message the message to display
	 * @return the user input
	 */
	public static String promptUserForInput(final String title, final String message) {
		final AtomicReference<String> userInput = new AtomicReference<String>(null);
		final Display display = getDisplay();
		display.syncExec(new Runnable(){
			@Override
			public void run() {
		        InputDialog inputDialog = new InputDialog(getShell(display), title, message);
		        userInput.set(inputDialog.open());
			}
		});
		return userInput.get();
	}
	
	public static Path promptUserForRootWorkingDirectory(String userPreferedRootWorkingDirectoryPath) {
		final AtomicReference<Path> selectedWorkingDirectoryPath = new AtomicReference<Path>(null);
		final Display display = getDisplay();
		display.syncExec(new Runnable(){
		    @Override
		    public void run() {
				DirectoryDialog dialog = new DirectoryDialog(getShell(display));
				dialog.setText("Select an output directory");
				dialog.setMessage("The output directory will be used to store all the generated documentation files");
				dialog.setFilterPath(userPreferedRootWorkingDirectoryPath);
				selectedWorkingDirectoryPath.set(Paths.get(dialog.open()));
		    }
		});
		return selectedWorkingDirectoryPath.get();
	}
	
	/**
	 * Returns a shell for the given display
	 * @param display
	 * @return
	 */
	private static Shell getShell(Display display){
		Shell shell = display.getActiveShell();
		if (shell == null) {
			shell = new Shell(display);
		}
		return shell;
	}
	
	/**
	 * Returns the active display
	 * @return
	 */
	private static Display getDisplay(){
		Display display = Display.getCurrent();
		if(display == null){
			display = Display.getDefault();
		}
		return display;
	}
	
}