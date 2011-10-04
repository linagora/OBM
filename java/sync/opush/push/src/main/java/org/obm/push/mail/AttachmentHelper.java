package org.obm.push.mail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author adrienp
 * 
 */
public class AttachmentHelper {

	public final static String COLLECTION_ID = "collectionId";
	public final static String MESSAGE_ID = "messageId";
	public final static String MIME_PART_ADDRESS = "mimePartAddress";
	public final static String CONTENT_TYPE = "contentType";
	public final static String CONTENT_TRANSFERE_ENCODING = "contentTransferEncoding";

	public static String getAttachmentId(String collectionId, String messageId,
			String mimePartAddress, String contentType,
			String contentTransferEncoding) {
		String ct = Base64.encodeBase64String(contentType.getBytes());
		String ret = collectionId + "_" + messageId + "_" + mimePartAddress
				+ "_" + ct;
		if (contentTransferEncoding != null
				&& !contentTransferEncoding.isEmpty()) {
			String cte = Base64.encodeBase64String(contentTransferEncoding.getBytes());
			ret += "_" + cte;
		}
		return ret;
	}

	public static Map<String, String> parseAttachmentId(String attachmentId) {
		String[] tab = attachmentId.split("_");
		if (tab.length < 4) {
			return null;
		}
		Map<String, String> data = new HashMap<String, String>();
		data.put(COLLECTION_ID, tab[0]);
		data.put(MESSAGE_ID, tab[1]);
		data.put(MIME_PART_ADDRESS, tab[2]);
		data.put(CONTENT_TYPE, new String(Base64.decodeBase64(tab[3])));
		if(tab.length >=5){
			data.put(CONTENT_TRANSFERE_ENCODING, new String(Base64.decodeBase64(tab[4])));
		}
		return data;
	}
}
