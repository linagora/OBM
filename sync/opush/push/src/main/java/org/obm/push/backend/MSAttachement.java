package org.obm.push.backend;

/**
 * 
 * @author adrienp
 *
 */
public class MSAttachement {
	
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
}
