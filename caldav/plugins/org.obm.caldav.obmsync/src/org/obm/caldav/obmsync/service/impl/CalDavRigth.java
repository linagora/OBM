package org.obm.caldav.obmsync.service.impl;

public class CalDavRigth {
	private Boolean isReadable;
	private Boolean isWritable;
	
	public CalDavRigth() {
		this.isReadable = false;
		this.isWritable = false;
	}
	
	public CalDavRigth(Boolean isReadable, Boolean isWritable) {
		super();
		this.isReadable = isReadable;
		this.isWritable = isWritable;
	}
	
	public Boolean isReadable() {
		return isReadable;
	}

	public Boolean isWritable() {
		return isWritable;
	}
}
