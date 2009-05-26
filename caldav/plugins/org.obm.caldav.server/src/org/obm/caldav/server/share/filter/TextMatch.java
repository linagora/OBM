package org.obm.caldav.server.share.filter;


/**
 * 
 * @author adrienp
 *
 */
public class TextMatch {

	private boolean negateCondition;
	private String collation;
	private String text;

	public TextMatch() {
		this.negateCondition = false;
		this.collation = "";
		this.text = "";
	}

	public boolean isNegateCondition() {
		return negateCondition;
	}

	public String getCollation() {
		return collation;
	}

	public String getText() {
		return text;
	}

	public void setNegateCondition(boolean negateCondition) {
		this.negateCondition = negateCondition;
	}

	public void setCollation(String collation) {
		this.collation = collation;
	}

	public void setText(String text) {
		this.text = text;
	}

}
