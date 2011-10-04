package org.obm.push.search;

import java.util.List;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SearchResult;
import org.obm.push.bean.StoreName;

public interface ISearchSource {

	StoreName getStoreName();

	List<SearchResult> search(BackendSession bs, String query, Integer limit);

}
