package org.obm.sync.services;

import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;

public interface IMailingList {

	public List<MailingList> listAllMailingList(AccessToken token) throws AuthFault, ServerFault;

	public MailingList createMailingList(AccessToken token, 
			MailingList mailingList) throws AuthFault, ServerFault;

	public MailingList modifyMailingList(AccessToken token, 
			MailingList mailingList) throws AuthFault, ServerFault;

	public void removeMailingList(AccessToken token, Integer id)
			throws AuthFault, ServerFault;

	public MailingList getMailingListFromId(AccessToken token, Integer id)
			throws AuthFault, ServerFault;
	
	public List<MLEmail> addEmails(AccessToken token, 
			Integer mailingListId, List<MLEmail> email) throws AuthFault, ServerFault;
	
	public void removeEmail(AccessToken token, 
			Integer mailingListId, Integer emailId) throws AuthFault, ServerFault;
}
