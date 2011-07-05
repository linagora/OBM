package org.obm.push.search;

import java.util.List;

import org.obm.push.backend.BackendSession;

public interface ISearchSource {
	
	StoreName getStoreName(); 
	
	public List<SearchResult> search(BackendSession bs,
			String query, Integer limit);

}
