package com.ensoftcorp.open.dynadoc.core.utils;

import com.ensoftcorp.open.dynadoc.core.path.WorkingDirectory;
import com.hp.gagawa.java.elements.Img;

public class HTMLUtils {

	public static Img checkImg(WorkingDirectory workingDirectory) {
		String pathRelativeToResouceCheckImageFile = PathUtils.getRelativePathStringToCheckImage(workingDirectory);
		Img tickImage = new Img("", pathRelativeToResouceCheckImageFile);
		tickImage.setWidth("25");
		return tickImage;
	}
}
