package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class MSAttachement implements Serializable {
	
	private String displayName;
	private String fileReference;
	private MethodAttachment method;
	private Integer estimatedDataSize;
	private String contentId;
	private String contentLocation;
	private String isInline;
	
	public MSAttachement(){
		method = MethodAttachment.NormalAttachment;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFileReference() {
		return fileReference;
	}

	public void setFileReference(String fileReference) {
		this.fileReference = fileReference;
	}

	public MethodAttachment getMethod() {
		return method;
	}

	public void setMethod(MethodAttachment method) {
		this.method = method;
	}

	public Integer getEstimatedDataSize() {
		return estimatedDataSize;
	}

	public void setEstimatedDataSize(Integer estimatedDataSize) {
		this.estimatedDataSize = estimatedDataSize;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentLocation() {
		return contentLocation;
	}

	public void setContentLocation(String contentLocation) {
		this.contentLocation = contentLocation;
	}

	public String getIsInline() {
		return isInline;
	}

	public void setIsInline(String isInline) {
		this.isInline = isInline;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(displayName, fileReference, method, estimatedDataSize, 
				contentId, contentLocation, isInline);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAttachement) {
			MSAttachement that = (MSAttachement) object;
			return Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.fileReference, that.fileReference)
				&& Objects.equal(this.method, that.method)
				&& Objects.equal(this.estimatedDataSize, that.estimatedDataSize)
				&& Objects.equal(this.contentId, that.contentId)
				&& Objects.equal(this.contentLocation, that.contentLocation)
				&& Objects.equal(this.isInline, that.isInline);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("displayName", displayName)
			.add("fileReference", fileReference)
			.add("method", method)
			.add("estimatedDataSize", estimatedDataSize)
			.add("contentId", contentId)
			.add("contentLocation", contentLocation)
			.add("isInline", isInline)
			.toString();
	}
	
}
