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
package fr.aliacom.obm.common.mailingList;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.services.IMailingList;

import com.google.inject.Inject;

import fr.aliacom.obm.utils.LogUtils;

public class MailingListBindingImpl implements IMailingList {

	private static final Log logger = LogFactory.getLog(MailingListBindingImpl.class);

	private MailingListHome mailingListHome;
	
	@Inject
	private MailingListBindingImpl(MailingListHome mailingListHome) {
		this.mailingListHome = mailingListHome;
	}

	@Override
	public List<MailingList> listAllMailingList(AccessToken token)
			throws AuthFault, ServerFault {
		try {
			return mailingListHome.findMailingLists(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault("error finding addressbooks ");
		}
	}

	@Override
	public MailingList createMailingList(AccessToken token,
			MailingList mailingList) throws AuthFault, ServerFault {
		try {
			MailingList ml = mailingListHome.createMailingList(
					token, mailingList);

			logger.info(LogUtils.prefix(token) + "Mailing list[" + ml.getId()
					+ "] : " + ml.getName() + " created");
			return ml;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public MailingList modifyMailingList(AccessToken token,
			MailingList mailingList) throws AuthFault, ServerFault {
		try {
			MailingList ml = mailingListHome.modifyMailingList(
					token, mailingList);

			logger.info(LogUtils.prefix(token) + "Mailing list : "
					+ ml.getName() + " modified");
			return ml;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public void removeMailingList(AccessToken token, Integer id)
			throws AuthFault, ServerFault {
		try {
			mailingListHome.removeMailingList(token, id);
			logger.info(LogUtils.prefix(token) + "Mailing list : " + id
					+ " removed");
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public MailingList getMailingListFromId(AccessToken token, Integer id)
			throws AuthFault, ServerFault {
		try {
			return mailingListHome.getMailingListFromId(token, id);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public List<MLEmail> addEmails(AccessToken at, Integer mailingListId,
			List<MLEmail> email) throws AuthFault, ServerFault {
		try {
			List<MLEmail> ret = mailingListHome.addEmails(at, mailingListId, email);
			logger.info(LogUtils.prefix(at) + ret.size() +" emails were added in mailingList: " + mailingListId );
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(at) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	public void removeEmail(AccessToken at, Integer mailingListId,
			Integer emailId) throws AuthFault, ServerFault {
		try {
			mailingListHome.removeEmail(at, mailingListId,
					emailId);
			logger.info(LogUtils.prefix(at) + "Email[" + emailId
					+ "] in MailingList[" + mailingListId + "]  : "
					+ " removed");
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(at) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

}
