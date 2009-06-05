package org.obm.caldav.server.resultBuilder;

import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * 
 * RFC4918
 * 		9.8.5.  Status Codes
 * 
 * @author adrienp
 *
 */
public class ErreurBuiler extends ResultBuilder {

	// <?xml version="1.0" encoding="utf-8" ?>
	// <d:multistatus xmlns:d="DAV:">
	// <d:response>
	// <d:href>http://www.example.com/container/resource3</d:href>
	// <d:status>HTTP/1.1 423 Locked</d:status>
	// <d:error><d:lock-token-submitted/></d:error>
	// </d:response>
	// </d:multistatus>

	public Document build(Token token, DavRequest req, int code) {
		Document doc = null;
		try {
			doc = createDocument();
			Element root = doc.getDocumentElement();
			Element response = DOMUtils.createElement(root, "D:response");
			DOMUtils.createElementAndText(response, "D:href", getHref(req));
			DOMUtils.createElementAndText(response, "D:status", getStatus(code));
			//DOMUtils.createElementAndText(response, "D:error", href);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}
	
	private String getStatus(int code){
		return "HTTP/1.1 "+ code + " " + StatusCodeConstant.getStatusMessage(code);
	}
	
	private String getHref(DavRequest req){
		return req.getHref()+req.getURI();
	}

}
