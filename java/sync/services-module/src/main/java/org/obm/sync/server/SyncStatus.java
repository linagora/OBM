/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.obm.sync.server.handler.ISyncHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.ObmSyncVersion;
import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;

@Singleton
public class SyncStatus {

	private static final String TITLE = "OBM Sync Status";

	private final Logger logger =  LoggerFactory.getLogger(getClass());

	private final Map<String, ISyncHandler> handlers;

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
		w.println("<p><em>Version :</em> " + getObmSyncVersion() + "</p> ");
	}
	
	private String getObmSyncVersion() {
		try {
			return ObmSyncVersion.current().toString();
		} catch (ObmSyncVersionNotFoundException e) {
			return "Invalid obm-sync server version";
		}
	}

}
