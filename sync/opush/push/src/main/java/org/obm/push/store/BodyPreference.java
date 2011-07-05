package org.obm.push.store;


public class BodyPreference {

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
