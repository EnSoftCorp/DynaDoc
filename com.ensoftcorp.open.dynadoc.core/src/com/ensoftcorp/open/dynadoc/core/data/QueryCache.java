package com.ensoftcorp.open.dynadoc.core.data;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class QueryCache {

	public static final Q containsEdges = Query.resolve(null, Query.universe().edges(XCSG.Contains));
	
	public static final Q callEdges = Query.resolve(null, Query.universe().edges(XCSG.Call));
	
	public static final Q typeOfEdges = Query.resolve(null, Query.universe().edges(XCSG.TypeOf));
	
	public static final Q dataFlowEdges = Query.resolve(null, Query.universe().edges(XCSG.DataFlow_Edge));
	
	public static final Q returnsEdges = Query.resolve(null, Query.universe().edges(XCSG.Returns));
	
	public static Q extend(Q base){
		return base.union(containsEdges.reverse(base));
	}

}
