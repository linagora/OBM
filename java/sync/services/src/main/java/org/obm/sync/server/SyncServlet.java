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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.sync.GuiceServletContextListener;
import org.obm.sync.server.handler.ISyncHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.ObmSyncVersion;

@Singleton
public class SyncServlet extends HttpServlet {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Injector injector;

	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.info("Init obm-sync servlet ...");
		super.init(config);
		injector = (Injector)config.getServletContext().getAttribute(GuiceServletContextListener.ATTRIBUTE_NAME);
		logger.info("Starting obm-sync " + ObmSyncVersion.current());
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Request request;
		XmlResponder responder = new XmlResponder(resp);

		try {
			request = new Request(req);
			if (request.getHandlerName() == null) {
				showSyncStatus(resp);
			} else {
				String handlerName = request.getHandlerName();
				ISyncHandler handler = injector.getInstance(SyncHandlers.class).getHandlers().
						get(handlerName);
				if (handler == null) {
					logger.error("no handler for {}", handlerName);
					responder.sendError(new Exception("no handler for " + handlerName));
				}
				else {
					long t = System.nanoTime();
					handler.handle(request, responder);
					final long elapsedTime = (System.nanoTime() - t) / 1000000;
					logger.info("handler responded to {}/{} in {}ms.", new Object[]{handlerName,
							request.getMethod(), elapsedTime});
				}
			}

		}
		catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			responder.sendError(ex);
		}

	}

	private void showSyncStatus(HttpServletResponse resp) {
		logger.info("show obm sync status");
		SyncStatus ss = injector.getInstance(SyncStatus.class);
		ss.show(resp);
	}
	
}
