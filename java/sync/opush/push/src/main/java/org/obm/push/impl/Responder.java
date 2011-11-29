package org.obm.push.impl;

import java.io.InputStream;
import java.util.Collection;

import org.w3c.dom.Document;

public interface Responder {

	void sendResponse(String defaultNamespace, Document doc);

	void sendResponseFile(String contentType, InputStream file);

	public void sendMSSyncMultipartResponse(String defaultNamespace,
			Document doc, Collection<byte[]> files, boolean gzip);
	
	void sendError(int statusCode);

	void sendNoChangeResponse();

}