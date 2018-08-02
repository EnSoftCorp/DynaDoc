package com.ensoftcorp.open.dynadoc.core.utils;

import java.nio.file.Path;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;

public class SaveUtils {

	public static void saveGraph(Path filePath, Q graphQ, IMarkup markup) {
		try {
			SaveUtil.saveGraph(filePath.toFile(), graphQ.eval(), markup);
		} catch(Exception e) {
			Log.error("Cannot save graph", e);
		}
	}

}
