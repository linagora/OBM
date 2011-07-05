package org.obm.push.backend;

/**
 * 
 * @author adrienp
 *
 */
public enum Importance {
	LOW, NORMAL, HIGH;
	
	public String asIntString() {
		switch (this) {
		case HIGH:
			return "2";
		case LOW:
			return "0";
		case NORMAL:
		default:
			return "1";
		}
	}

}
