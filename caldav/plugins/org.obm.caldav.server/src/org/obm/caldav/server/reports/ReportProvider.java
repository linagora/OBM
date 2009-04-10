package org.obm.caldav.server.reports;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.impl.Token;

public abstract class ReportProvider {

	protected Log logger = LogFactory.getLog(getClass());
	
	protected ReportProvider() {
		
	}
	
	public abstract void process(Token token, DavRequest req, HttpServletResponse resp);

}
