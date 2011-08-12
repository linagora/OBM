package org.obm.sync.services;

import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;

public interface IMailingList {

	List<MailingList> listAllMailingList(AccessToken token) throws ServerFault;

	MailingList createMailingList(AccessToken token, 
			MailingList mailingList) throws ServerFault;

	MailingList modifyMailingList(AccessToken token, 
			MailingList mailingList) throws ServerFault;

	void removeMailingList(AccessToken token, Integer id)
			throws ServerFault;

	MailingList getMailingListFromId(AccessToken token, Integer id)
			throws ServerFault;
	
	List<MLEmail> addEmails(AccessToken token, 
			Integer mailingListId, List<MLEmail> email) throws ServerFault;
	
	void removeEmail(AccessToken token, 
			Integer mailingListId, Integer emailId) throws ServerFault;
}
