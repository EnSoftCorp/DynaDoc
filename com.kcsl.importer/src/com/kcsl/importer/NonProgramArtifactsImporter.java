package com.kcsl.importer;

import com.kcsl.importer.internal.CommitsImporter;
import com.kcsl.importer.internal.IssuesImporter;

public class NonProgramArtifactsImporter {

	public static void importData() {
		IssuesImporter.importData();
		CommitsImporter.importData();
	}
}
