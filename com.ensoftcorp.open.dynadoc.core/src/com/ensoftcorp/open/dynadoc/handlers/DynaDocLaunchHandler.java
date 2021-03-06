package com.ensoftcorp.open.dynadoc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import com.ensoftcorp.open.dynadoc.core.DynaDocDriver;

public class DynaDocLaunchHandler extends AbstractHandler {
	
	private static final String DYNADOC_JAVA_CLASS_SELECTOR_PANEL_TITLE = "DynaDoc: Class Selector";
	
	private static final String DYNADOC_JAVA_CLASS_SELECTOR_PANEL_MESSAGE = "Enter a comma-separated list of fully qualified Java class names (or '*' for all classes in codemap) ";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String fullyQualifiedJavaClassNames = DisplayUtils.promptString(DYNADOC_JAVA_CLASS_SELECTOR_PANEL_TITLE, DYNADOC_JAVA_CLASS_SELECTOR_PANEL_MESSAGE);
		if(fullyQualifiedJavaClassNames != null) {
			if(fullyQualifiedJavaClassNames.equals("*")) {
				DynaDocDriver.run();
			}else {
				DynaDocDriver.run(fullyQualifiedJavaClassNames);
			}
		}
		return null;
	}

}
