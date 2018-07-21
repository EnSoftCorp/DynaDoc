package com.kcsl.dynadoc.path;

import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public final class WorkingDirectory {
	
	private static final String PARENT_DIRECTORY_SEPARATOR = ".." + IOUtils.DIR_SEPARATOR;

	private Path path;
	
	private String relativePathStringToRootWorkingDirectory;
	
	private WorkingDirectory(Path path) {
		this(path, StringUtils.EMPTY);
	}
	
	private WorkingDirectory(Path path, String relativePathToRootDirectoryString) {
		this.path = path;
		this.relativePathStringToRootWorkingDirectory = relativePathToRootDirectoryString;
	}
	
	public Path getPath() {
		return this.path;
	}
	
	public String getRelativePathStringToRootWorkingDirectory() {
		return this.relativePathStringToRootWorkingDirectory;
	}
	
	public WorkingDirectory push(String subDirectoryName) {
		Path newPath = this.path.resolve(subDirectoryName);
		String newRelativePathToRootDirectoryString = this.relativePathStringToRootWorkingDirectory + PARENT_DIRECTORY_SEPARATOR;
		return new WorkingDirectory(newPath, newRelativePathToRootDirectoryString);
	}
	
	public static WorkingDirectory createRootWorkingDirectory(Path rootWorkingDirectoryPath) {
		return new WorkingDirectory(rootWorkingDirectoryPath);
	}
}
