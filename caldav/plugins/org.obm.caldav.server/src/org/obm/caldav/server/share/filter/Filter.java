package org.obm.caldav.server.share.filter;


/**
 * http://www.webdav.org/specs/rfc4791.html#rfc.section.9.7
 * 
 * @author adrienp
 *
 */
public class Filter {

	public final static String NAMESPACE = "urn:ietf:params:xml:ns:caldav";
	private CompFilter vCalendar;
	
	public Filter(){
	}
	
	public CompFilter getCompFilter(){
		return vCalendar;
	}
	
	public void setCompFilter(CompFilter compFilter){
		this.vCalendar = compFilter;
	}
	
}
