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

import java.util.List;

import org.obm.sync.LoggerService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.mailingList.MailingListItemsParser;
import org.obm.sync.server.handler.SecureSyncHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.mailingList.MailingListBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;

@Singleton
public class MailingListHandler extends SecureSyncHandler {

	private final MailingListBindingImpl binding;
	private final MailingListItemsParser parser;

	@Inject
	private MailingListHandler(SessionManagement sessionManagement,
			LoggerService loggerService,
			MailingListBindingImpl mailingListBindingImpl,
			MailingListItemsParser mailingListItemsParser) {

		super(sessionManagement, loggerService);
		binding = mailingListBindingImpl;
		parser = mailingListItemsParser;
	}

	@Override
	public void handle(Request request, XmlResponder responder) throws Exception {
		AccessToken at = getCheckedToken(request);

		String method = request.getMethod();
		if ("getMailingListFromId".equals(method)) {
			getMailingListFromId(at, request, responder);
		} else if ("listAllMailingList".equals(method)) {
			listAllMailingList(at, responder);
		} else if ("createMailingList".equals(method)) {
			createMailingList(at, request, responder);
		} else if ("modifyMailingList".equals(method)) {
			modifyMailingList(at, request, responder);
		} else if ("removeMailingList".equals(method)) {
			removeMailingList(at, request, responder);
		} else if ("addEmails".equals(method)) {
			addEmails(at, request, responder);
		} else if ("removeEmail".equals(method)) {
			removeEmail(at, request, responder);
		} else {
			responder.sendError("Cannot handle method '" + method + "'");
		}
	}

	private void removeEmail(AccessToken token, Request request, XmlResponder responder) {
		try {
			String mailingListId = p(request, "mailingListId");
			String mailingListEmailId = p(request, "mailingListEmailId");
			binding.removeEmail(token, Integer.parseInt(mailingListId),
					Integer.parseInt(mailingListEmailId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void addEmails(AccessToken token, Request request, XmlResponder responder) {
		try {
			String mailingListId = p(request, "mailingListId");
			String mailingListEmails = p(request, "mailingListEmails");
			List<MLEmail> ret = binding.addEmails(token, Integer.parseInt(mailingListId),
					parser.parseMailingListEmails(mailingListEmails));
			responder.sendListMailingListEmails(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void getMailingListFromId(AccessToken token, Request request,
			XmlResponder responder) {
		try {
			String id = p(request, "id");

			MailingList ret = binding.getMailingListFromId(token,
					Integer.parseInt(id));
			if (ret != null) {
				responder.sendMailingList(ret);
			} else {
				responder.sendError("mailing list with id " + id
						+ " not found.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}

	}

	private void listAllMailingList(AccessToken token,
			XmlResponder responder) {
		try {
			List<MailingList> ret = binding.listAllMailingList(token);
			responder.sendListMailingLists(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void createMailingList(AccessToken token, Request request,
			XmlResponder responder) {
		try {
			MailingList ret = binding.createMailingList(token,
					parser.parseMailingList(p(request, "mailingList")));
			responder.sendMailingList(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void modifyMailingList(AccessToken token, Request request,
			XmlResponder responder) {
		try {
			String ct = p(request, "mailingList");
			MailingList ret = binding.modifyMailingList(token,
					parser.parseMailingList(ct));
			responder.sendMailingList(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void removeMailingList(AccessToken token, Request request,
			XmlResponder responder) {
		try {
			String id = p(request, "id");
			binding.removeMailingList(token, Integer.parseInt(id));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

}
