package org.obm.caldav.server.share.filter;

import java.util.ArrayList;
import java.util.List;



/**
 * http://www.webdav.org/specs/rfc4791.html#rfc.section.9.7.2
 * 
 * @author adrienp
 *
 */
public class PropFilter {

	private String name;
	private boolean isNotDefined;
	private TimeRange timeRange;
	private TextMatch textMatch;
	private List<ParamFilter> paramFilters;
	
	public PropFilter() {
		this.name = "";
		this.isNotDefined = false;
		this.timeRange = null;
		this.textMatch = null;
		this.paramFilters = new ArrayList<ParamFilter>();
	}

	public String getName() {
		return name;
	}

	public boolean isNotDefined() {
		return isNotDefined;
	}

	public TimeRange getTimeRange() {
		return timeRange;
	}

	public TextMatch getTextMatch() {
		return textMatch;
	}

	public List<ParamFilter> getParamFilters() {
		return paramFilters;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNotDefined(boolean isNotDefined) {
		this.isNotDefined = isNotDefined;
	}

	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	public void setTextMatch(TextMatch textMatch) {
		this.textMatch = textMatch;
	}

	public void addParamFilter(ParamFilter paramFilter) {
		this.paramFilters.add(paramFilter);
	}
	
}
