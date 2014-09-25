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
package fr.aliacom.obm.common.mailingList;

import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.services.IMailingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailingListBindingImpl implements IMailingList {

	private static final Logger logger = LoggerFactory
			.getLogger(MailingListBindingImpl.class);

	private final MailingListHome mailingListHome;
	
	@Inject
	protected MailingListBindingImpl(MailingListHome mailingListHome) {
		this.mailingListHome = mailingListHome;
	}

	@Override
	@Transactional(readOnly=true)
	public List<MailingList> listAllMailingList(AccessToken token)
			throws ServerFault {
		try {
			return mailingListHome.findMailingLists(token);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault("error finding addressbooks ");
		}
	}

	@Override
	@Transactional
	public MailingList createMailingList(AccessToken token,
			MailingList mailingList) throws ServerFault {
		try {
			MailingList ml = mailingListHome.createMailingList(
					token, mailingList);

			logger.info("Mailing list[" + ml.getId()
					+ "] : " + ml.getName() + " created");
			return ml;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public MailingList modifyMailingList(AccessToken token,
			MailingList mailingList) throws ServerFault {
		try {
			MailingList ml = mailingListHome.modifyMailingList(
					token, mailingList);

			logger.info("Mailing list : "
					+ ml.getName() + " modified");
			return ml;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void removeMailingList(AccessToken token, Integer id)
			throws ServerFault {
		try {
			mailingListHome.removeMailingList(token, id);
			logger.info("Mailing list : " + id
					+ " removed");
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public MailingList getMailingListFromId(AccessToken token, Integer id)
			throws ServerFault {
		try {
			return mailingListHome.getMailingListFromId(token, id);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<MLEmail> addEmails(AccessToken at, Integer mailingListId,
			List<MLEmail> email) throws ServerFault {
		try {
			List<MLEmail> ret = mailingListHome.addEmails(at, mailingListId, email);
			logger.info(ret.size() +" emails were added in mailingList: " + mailingListId );
			return ret;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void removeEmail(AccessToken at, Integer mailingListId,
			Integer emailId) throws ServerFault {
		try {
			mailingListHome.removeEmail(at, mailingListId,
					emailId);
			logger.info("Email[" + emailId
					+ "] in MailingList[" + mailingListId + "]  : "
					+ " removed");
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

}
