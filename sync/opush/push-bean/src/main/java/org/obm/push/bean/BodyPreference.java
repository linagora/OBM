package org.obm.push.bean;

import java.io.Serializable;



public class BodyPreference implements Serializable {

	private Integer truncationSize;
	private MSEmailBodyType type;
	
	public BodyPreference(){
		
	}

	public Integer getTruncationSize() {
		return this.truncationSize;
	}

	public void setTruncationSize(Integer truncationSize) {
		this.truncationSize = truncationSize;
	}
	
	public MSEmailBodyType getType() {
		return type;
	}

	public void setType(MSEmailBodyType type) {
		this.type = type;
	}

}
