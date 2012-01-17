/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.mail;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;

public interface MailboxService {

	List<MSEmail> fetchMails(BackendSession bs, Integer collectionId, String collectionName, 
			Collection<Long> uids) throws MailException;

	void updateReadFlag(BackendSession bs, String collectionName, long uid, boolean read) throws MailException, ImapMessageNotFoundException;

	String parseMailBoxName(BackendSession bs, String collectionName) throws MailException;

	void delete(BackendSession bs, Integer devId, String collectionPath, Integer collectionId, Long uid) throws MailException, DaoException;

	Long moveItem(BackendSession bs, Integer devId, String srcFolder, Integer srcFolderId, String dstFolder, Integer dstFolderId, 
			Long uid) throws MailException, DaoException;

	InputStream fetchMailStream(BackendSession bs, String collectionName, long uid) throws MailException;

	void setAnsweredFlag(BackendSession bs, String collectionName, long uid) throws MailException, ImapMessageNotFoundException;

	void sendEmail(BackendSession bs, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException, StoreEmailException;

	InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid, String mimePartAddress) throws MailException;

	Collection<Long> purgeFolder(BackendSession bs, Integer devId, String collectionPath, Integer collectionId) throws MailException, DaoException;

	boolean storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) throws StoreEmailException;

	boolean getLoginWithDomain();

	boolean getActivateTLS();
	
	Collection<Email> fetchEmails(BackendSession bs, Collection<Long> uids) throws MailException;

	Set<Email> fetchEmails(BackendSession bs, String collectionName, Date windows) throws MailException;

}
