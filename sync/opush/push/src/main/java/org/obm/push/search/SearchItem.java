package org.obm.push.search;


/**
 * 
 * @author adrienp
 *
 */
public class SearchItem {
	
	private StoreName storeName;
	private String query;
	private Integer rangeLower;
	private Integer rangeUpper;
	
	public StoreName getStoreName() {
		return storeName;
	}

	public void setStoreName(StoreName storeName) {
		this.storeName = storeName;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Integer getRangeLower() {
		return rangeLower;
	}

	public void setRangeLower(Integer rangeLower) {
		this.rangeLower = rangeLower;
	}

	public Integer getRangeUpper() {
		return rangeUpper;
	}

	public void setRangeUpper(Integer rangeUpper) {
		this.rangeUpper = rangeUpper;
	}

}
