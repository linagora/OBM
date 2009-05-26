package org.obm.caldav.server.share.filter;

import java.util.ArrayList;
import java.util.List;



/**
 * http://www.webdav.org/specs/rfc4791.html#rfc.section.9.7.1
 * 
 * @author adrienp
 *
 */
public class CompFilter {
	
	public final static String VCALENDAR = "vcalendar";
	public final static String VEVENT = "vevent";
	public final static String VTODO = "vtodo";
	public final static String NAMESPACE = "urn:ietf:params:xml:ns:caldav";
	
	private String name;
	private boolean isNotDefined;
	private TimeRange timeRange;
	private List<PropFilter> propFilters;
	private List<CompFilter> compFilters;
	
	public CompFilter() {
		this.name = "";
		this.isNotDefined = false;
		this.timeRange = null;
		this.propFilters = new ArrayList<PropFilter>();
		this.compFilters = new ArrayList<CompFilter>();
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


	public List<PropFilter> getPropFilters() {
		return propFilters;
	}


	public List<CompFilter> getCompFilters() {
		return compFilters;
	}



	public void setName(String name) {
		this.name = name;
	}



	public void setIsNotDefined(boolean isNotDefined) {
		this.isNotDefined = isNotDefined;
	}



	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	public void addPropFilter(PropFilter propFilters) {
		this.propFilters.add(propFilters);
	}

	public void addCompFilter(CompFilter compFilter) {
		this.compFilters.add(compFilter);
	}
	
	public boolean isEmpty(){
		boolean empty = false;
		if(name == null || "".equals(name)){
			empty = true;
		}
		if(isNotDefined){
			empty = true;
		}
		if(timeRange == null || timeRange.isEmpty()){
			empty = true;
		}
		
		if(propFilters.size() == 0){
			empty = true;
		}

		if(compFilters.size() == 0){
			empty = true;
		}
		return empty;
	}
	

}
