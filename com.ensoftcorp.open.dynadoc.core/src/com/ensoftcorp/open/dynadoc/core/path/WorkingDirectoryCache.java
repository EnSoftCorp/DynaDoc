package com.ensoftcorp.open.dynadoc.core.path;

import java.nio.file.Path;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.map.AtlasGraphKeyHashMap;
import com.ensoftcorp.atlas.core.db.map.AtlasMap;

public class WorkingDirectoryCache {

	private static final AtlasMap<Node, WorkingDirectory> CLASS_NODE_TO_WORKING_DIRECTORY_MAP = new AtlasGraphKeyHashMap<Node, WorkingDirectory>();
	
	public static WorkingDirectory createWorkingDirectory(Node classNode, String subDirectoryName, WorkingDirectory currentWorkingDirectory) {
		WorkingDirectory classWorkingDirectory = currentWorkingDirectory.push(subDirectoryName);
		CLASS_NODE_TO_WORKING_DIRECTORY_MAP.put(classNode, classWorkingDirectory);
		return classWorkingDirectory;
	}
	
	public static WorkingDirectory getWorkingDirectory(Node classNode) {
		assert CLASS_NODE_TO_WORKING_DIRECTORY_MAP.containsKey(classNode);
		WorkingDirectory classWorkingDirectory = CLASS_NODE_TO_WORKING_DIRECTORY_MAP.get(classNode);
		return classWorkingDirectory;
	}
	
	public static Path getWorkingDirectoryPath(Node classNode) {
		return getWorkingDirectory(classNode).getPath();
	}
	
	public static String getRelativePathStringToRootWorkingDirectory(Node classNode) {
		return getWorkingDirectory(classNode).getRelativePathStringToRootWorkingDirectory();
	}

}
