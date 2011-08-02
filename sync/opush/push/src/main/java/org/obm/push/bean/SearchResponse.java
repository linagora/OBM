package org.obm.push.bean;

import java.util.List;

import org.obm.push.search.SearchResult;

public class SearchResponse {

	private final List<SearchResult> results;
	private final int rangeLower;
	private final int rangeUpper;
	
	public SearchResponse(List<SearchResult> results, int rangeLower, int rangeUpper) {
		this.results = results;
		this.rangeLower = rangeLower;
		this.rangeUpper = rangeUpper;
	}

	public List<SearchResult> getResults() {
		return results;
	}
	
	public int getRangeLower() {
		return rangeLower;
	}
	
	public int getRangeUpper() {
		return rangeUpper;
	}
}
