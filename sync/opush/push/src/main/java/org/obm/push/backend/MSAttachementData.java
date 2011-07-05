package org.obm.push.backend;

import java.io.InputStream;

/**
 * 
 * @author adrienp
 *
 */
public class MSAttachementData {
	
	private InputStream file;
	private String contentType;
	
	public MSAttachementData(String contentType, InputStream file){
		this.contentType = contentType;
		this.file = file;
	}

	public InputStream getFile() {
		return file;
	}

	public String getContentType() {
		return contentType;
	}
}
