/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.server.handler.ISyncHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.ObmSyncVersion;

@Singleton
public class SyncStatus {

	private static final String TITLE = "OBM Sync Status";

	private Log logger = LogFactory.getLog(getClass());

	private Map<String, ISyncHandler> handlers;

	@Inject
	public SyncStatus(SyncHandlers handlers) {
		this.handlers = handlers.getHandlers();
	}

	public void show(HttpServletResponse resp) {
		resp.setContentType("text/html");
		try {

			PrintWriter w = resp.getWriter();

			w.println("<html><head><title>" + TITLE + "</title></head><body>");

			printVMInfo(w);

			printHandlers(w);

			printSyncInfo(w);

			w.print("</body></html>");

		} catch (Exception e) {
			logger.error("error showing status", e);
		}

	}

	private void printHandlers(PrintWriter w) {
		section("OBM Sync endpoints", w);
		for (String s : handlers.keySet()) {
			w.println("<p>handler configured for <em>" + s + "</em></p>");
		}
	}

	private void section(String string, PrintWriter w) {
		w.println("<h1>" + string + "</h1>");
	}

	private void printVMInfo(PrintWriter w) {
		section("Java Virtual Machine informations", w);
		w.println("<p><em>Vendor :</em> " + System.getProperty("java.vendor")
				+ "</p> ");
		w.println("<p><em>Version :</em> " + System.getProperty("java.version")
				+ "</p> ");
		w.println("<p><em>OS :</em> " + System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + "</p> ");
		w.println("<p><em>Timezone :</em> "
				+ System.getProperty("user.timezone") + "</p> ");
		w.println("<p><em>Username :</em> " + System.getProperty("user.name")
				+ "</p> ");
	}

	private void printSyncInfo(PrintWriter w) {
		section("OBM Sync informations", w);
		w.println("<p><em>Version :</em> " + 
				ObmSyncVersion.current().toString()
				+ "</p> ");
	}

}
