package org.minig.imap;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.MimeType;

import com.google.common.collect.Sets;

public class MimeMessageFactory {

	private static <T extends MimePart> T fillSimpleMimePart(T mimePart, String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		mimePart.setMimeType(new MimeType(mimeType, mimeSubtype));
		HashSet<BodyParam> params = Sets.newHashSet();
		for (Entry<String, String> entry: bodyParams.entrySet()) {
			params.add(new BodyParam(entry.getKey(), entry.getValue()));
		}
		mimePart.setBodyParams(params);
		for (MimePart part: parts) {
			mimePart.addPart(part);
		}
		mimePart.setContentId(contentId);
		mimePart.setContentTransfertEncoding(encoding);
		return mimePart;
	}
	
	public static MimePart createSimpleMimePart(String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		MimePart tree = new MimePart();
		fillSimpleMimePart(tree, mimeType, mimeSubtype, contentId, encoding, bodyParams, parts);
		return tree;
	}
	
	public static MimeMessage createSimpleMimeTree(String mimeType, String mimeSubtype, String contentId, String encoding, Map<String, String> bodyParams, MimePart... parts) {
		MimeMessage tree = new MimeMessage(createSimpleMimePart(mimeType, mimeSubtype, contentId, encoding, bodyParams, parts));
		return tree;
	}

	
}
