package org.obm.push.bean;

import java.io.InputStream;

import com.google.common.base.Objects;

public class MSAttachementData {
	
	private final InputStream file;
	private final String contentType;
	
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

	@Override
	public final int hashCode(){
		return Objects.hashCode(file, contentType);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAttachementData) {
			MSAttachementData that = (MSAttachementData) object;
			return Objects.equal(this.file, that.file)
				&& Objects.equal(this.contentType, that.contentType);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("file", file)
			.add("contentType", contentType)
			.toString();
	}
	
}
