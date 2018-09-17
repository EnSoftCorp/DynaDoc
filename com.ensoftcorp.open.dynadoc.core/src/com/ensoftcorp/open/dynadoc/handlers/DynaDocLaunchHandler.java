package com.ensoftcorp.open.dynadoc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.open.dynadoc.core.DynaDocDriver;
import com.ensoftcorp.open.dynadoc.support.DialogUtils;

public class DynaDocLaunchHandler extends AbstractHandler {
	
	private static final String DYNADOC_JAVA_CLASS_SELECTOR_PANEL_TITLE = "DynaDoc: Java Class Selector";
	
	private static final String DYNADOC_JAVA_CLASS_SELECTOR_PANEL_MESSAGE = "Please provide the fully qualified name for the Java class you want to generate documentation for:";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String javaClassFullyQualifiedName = DialogUtils.promptUserForInput(DYNADOC_JAVA_CLASS_SELECTOR_PANEL_TITLE, DYNADOC_JAVA_CLASS_SELECTOR_PANEL_MESSAGE);
		DynaDocDriver.generateClassDocumentation(javaClassFullyQualifiedName);
		return null;
	}

}
