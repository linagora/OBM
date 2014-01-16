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
package fr.aliasource.obm.autoconf;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.inject.Injector;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.utils.ConstantService;
import fr.aliasource.obm.utils.DOMUtils;

public class AutoconfService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3346961272290678959L;
	private static final Logger logger = LoggerFactory.getLogger(AutoconfService.class);
	private Injector injector;

	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.info("Init obm-autoconf servlet ...");
		super.init(config);
		injector = (Injector) config.getServletContext().getAttribute(
				GuiceServletContextListener.ATTRIBUTE_NAME);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String reqString = req.getRequestURI();
		logger.info("AutoconfService : reqString: '" + reqString);
		logger.info("Guice injector: " + injector);

		// get user from reqString : format ".../autoconfiguration/login"
		String login = reqString.substring(reqString.lastIndexOf("/") + 1);
		String domain = null;
		logger.info("login : '" + login + "'");
		if (login.indexOf("@") > 0) {
			String[] splitted = login.split("@");
			login = splitted[0];
			domain = splitted[1];
		}
		DBQueryTool dbqt = injector.getInstance(DBQueryTool.class);

		HashMap<String, String> servicesHostNames = new HashMap<String, String>();
		try {
			servicesHostNames.putAll(dbqt.getDBInformation(login, loadDomain(dbqt, login,
					domain)));
		} catch (Exception e) {
			logger.error("Cannot contact DB:" + e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		DirectoryConfig dc = new DirectoryConfig(login , domain, ConstantService
				.getInstance());
		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet attributeSet;
		try {
			attributeSet = lqt.getLDAPInformations();
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.LOCAL_ERROR) {
				logger
						.warn("NULL informations obtained from LDAPQueryTool for "
								+ login);
			} else {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		}

		TemplateLoader tl = new TemplateLoader(dc.getConfigXml(),
				ConstantService.getInstance());

		Document doc = tl.applyTemplate(attributeSet, servicesHostNames);

		if (doc != null && tl.isValidTemplate(doc.getDocumentElement())) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z");
			resp.setHeader("Expires", formatter.format(new Date()));
			resp.setContentType("application/xml");

			try {
				DOMUtils.serialize(doc, resp.getOutputStream());
			} catch (Exception e) {
				logger.error("error sending xml document", e);
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
		} else {
			logger.warn("null doc or resulting config.xml is invalid.");
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}

	}

	private String loadDomain(DBQueryTool dbqt, String login, String domain) {
		if (domain != null) {
			return domain;
		} else {
			return dbqt.getDomain(login);
		}
	}

}
