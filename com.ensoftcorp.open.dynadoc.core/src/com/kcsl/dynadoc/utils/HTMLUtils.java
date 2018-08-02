package com.kcsl.dynadoc.utils;

import com.hp.gagawa.java.elements.Img;
import com.kcsl.dynadoc.path.WorkingDirectory;

public class HTMLUtils {

	public static Img checkImg(WorkingDirectory workingDirectory) {
		String pathRelativeToResouceCheckImageFile = PathUtils.getRelativePathStringToCheckImage(workingDirectory);
		Img tickImage = new Img("", pathRelativeToResouceCheckImageFile);
		tickImage.setWidth("25");
		return tickImage;
	}
}
