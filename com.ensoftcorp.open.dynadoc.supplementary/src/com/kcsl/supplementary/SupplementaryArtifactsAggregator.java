package com.kcsl.supplementary;

import java.nio.file.Path;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.kcsl.supplementary.aggregator.JavaDocAggregatorTask;

public class SupplementaryArtifactsAggregator {

	public static void aggregateArtifacts(Node projectNode, Node classNode, Path rootWorkingDirectoryPath) {
		JavaDocAggregatorTask.runOnClass(projectNode, classNode, rootWorkingDirectoryPath);
	}
}
