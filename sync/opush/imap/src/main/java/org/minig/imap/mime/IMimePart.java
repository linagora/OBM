package org.minig.imap.mime;

import java.util.Collection;
import java.util.List;

public interface IMimePart {

	void addPart(IMimePart child);

	String getMimeType();

	String getMimeSubtype();

	List<IMimePart> getChildren();

	List<IMimePart> getSibling();
	
	MimeAddress getAddress();
	
	MimeAddress getAddressInternal();

	Collection<BodyParam> getBodyParams();

	BodyParam getBodyParam(final String param);

	IMimePart getParent();

	Collection<IMimePart> listLeaves(boolean depthFirst, boolean filterNested);

	void defineParent(IMimePart parent, int index);

	String getFullMimeType();

	boolean isInvitation();

	String getContentTransfertEncoding();
	
	String getCharset();

	String getContentId();

	boolean isCancelInvitation();

	void setBodyParams(Collection<BodyParam> newParams);

	void setMimeType(MimeType mimetype);

	String getName();

	boolean isMultipart();

	String getMultipartSubtype();

	void setMultipartSubtype(String subtype);

	boolean isAttachment();

	boolean isNested();
	
}