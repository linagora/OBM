package org.obm.caldav.server.share.filter;


/**
 * 
 * @author adrienp
 *
 */
public class ParamFilter {
	
	private String name;
	private boolean isNotDefined;
	private TextMatch textMatch;
	
	public ParamFilter() {
		super();
		this.name = "";
		this.isNotDefined = false;
		this.textMatch = null;
	}
	
	public String getName() {
		return name;
	}
	public boolean isNotDefined() {
		return isNotDefined;
	}
	public TextMatch getTextMatch() {
		return textMatch;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNotDefined(boolean isNotDefined) {
		this.isNotDefined = isNotDefined;
	}
	public void setTextMatch(TextMatch textMatch) {
		this.textMatch = textMatch;
	}
	
	

}
