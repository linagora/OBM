package org.obm.push.impl;

import java.io.InputStream;

import org.w3c.dom.Document;

public interface Responder {

	void sendResponse(String defaultNamespace, Document doc);

	void sendResponseFile(String contentType, InputStream file);

	void sendError(int statusCode);

	void sendNoChangeResponse();

}