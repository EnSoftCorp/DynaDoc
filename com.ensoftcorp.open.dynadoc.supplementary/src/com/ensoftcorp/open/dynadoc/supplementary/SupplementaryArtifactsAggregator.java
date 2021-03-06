package com.ensoftcorp.open.dynadoc.supplementary;

import java.nio.file.Path;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.dynadoc.supplementary.aggregator.JavaDocAggregatorTask;

public class SupplementaryArtifactsAggregator {

	public static void aggregateArtifacts(Node projectNode, Q classesQ, Path rootWorkingDirectoryPath) {
		JavaDocAggregatorTask.runOnClasses(projectNode, classesQ, rootWorkingDirectoryPath);
	}
}
