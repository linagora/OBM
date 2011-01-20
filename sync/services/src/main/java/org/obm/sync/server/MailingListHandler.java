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

import java.util.List;

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

	private MailingListBindingImpl binding;
	private MailingListItemsParser parser;

	@Inject
	private MailingListHandler(SessionManagement sessionManagement, MailingListBindingImpl mailingListBindingImpl, MailingListItemsParser mailingListItemsParser) {
		super(sessionManagement);
		binding = mailingListBindingImpl;
		parser = mailingListItemsParser;
	}

	@Override
	public void handle(String method, ParametersSource params,
			XmlResponder responder) throws Exception {
		if ("getMailingListFromId".equals(method)) {
			getMailingListFromId(params, responder);
		} else if ("listAllMailingList".equals(method)) {
			listAllMailingList(params, responder);
		} else if ("createMailingList".equals(method)) {
			createMailingList(params, responder);
		} else if ("modifyMailingList".equals(method)) {
			modifyMailingList(params, responder);
		} else if ("removeMailingList".equals(method)) {
			removeMailingList(params, responder);
		} else if ("addEmails".equals(method)) {
			addEmails(params, responder);
		} else if ("removeEmail".equals(method)) {
			removeEmail(params, responder);
		} else {
			responder.sendError("Cannot handle method '" + method + "'");
		}
	}

	private void removeEmail(ParametersSource params, XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			String mailingListId = p(params, "mailingListId");
			String mailingListEmailId = p(params, "mailingListEmailId");
			binding.removeEmail(at, Integer.parseInt(mailingListId),
					Integer.parseInt(mailingListEmailId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void addEmails(ParametersSource params, XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			String mailingListId = p(params, "mailingListId");
			String mailingListEmails = p(params, "mailingListEmails");
			List<MLEmail> ret = binding.addEmails(at, Integer.parseInt(mailingListId),
					parser.parseMailingListEmails(mailingListEmails));
			responder.sendListMailingListEmails(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void getMailingListFromId(ParametersSource params,
			XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			String id = p(params, "id");

			MailingList ret = binding.getMailingListFromId(at,
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

	private void listAllMailingList(ParametersSource params,
			XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			List<MailingList> ret = binding.listAllMailingList(at);
			responder.sendListMailingLists(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void createMailingList(ParametersSource params,
			XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			MailingList ret = binding.createMailingList(at,
					parser.parseMailingList(p(params, "mailingList")));
			responder.sendMailingList(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void modifyMailingList(ParametersSource params,
			XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			String ct = p(params, "mailingList");
			MailingList ret = binding.modifyMailingList(at,
					parser.parseMailingList(ct));
			responder.sendMailingList(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

	private void removeMailingList(ParametersSource params,
			XmlResponder responder) {
		AccessToken at = getToken(params);
		try {
			String id = p(params, "id");
			binding.removeMailingList(at, Integer.parseInt(id));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e);
		}
	}

}
