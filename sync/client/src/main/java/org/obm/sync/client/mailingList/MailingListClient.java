package org.obm.sync.client.mailingList;

import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.mailingList.MailingListItemsParser;
import org.obm.sync.mailingList.MailingListItemsWriter;
import org.obm.sync.services.IMailingList;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;

public class MailingListClient extends AbstractClientImpl implements IMailingList {

	private MailingListItemsParser mlParser;
	private MailingListItemsWriter mlWriter;

	public MailingListClient(String obmSyncServicesUrl) {
		super(obmSyncServicesUrl);
		mlParser = new MailingListItemsParser();
		mlWriter = new MailingListItemsWriter();
	}

	@Override
	public MailingList createMailingList(AccessToken token, MailingList mailingList) throws ServerFault {
		if(mailingList == null){
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingList", mlWriter.getMailingListsAsString(mailingList));
		Document doc = execute("/mailingList/createMailingList", params);
		checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public MailingList getMailingListFromId(AccessToken token,  Integer id) throws ServerFault {
		if (id == null) {
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("id", id.toString());
		Document doc = execute("/mailingList/getMailingListFromId", params);
		checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public List<MailingList> listAllMailingList(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute("/mailingList/listAllMailingList", params);
		checkServerFaultException(doc);
		List<MailingList> addressBooks = mlParser.parseListMailingList(doc);
		return addressBooks;
	}
	
	@Override
	public MailingList modifyMailingList(AccessToken token, 
			MailingList mailingList) throws ServerFault {
		if(mailingList == null){
			return null;
		}
		Multimap<String, String> params = initParams(token);
		String ml = mlWriter.getMailingListsAsString(mailingList);
		params.put("mailingList", ml);
		Document doc = execute("/mailingList/modifyMailingList", params);
		checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public void removeMailingList(AccessToken token, Integer id) throws ServerFault {
		if(id == null){
			return;
		}
		Multimap<String, String> params = initParams(token);
		params.put("id", id.toString());
		executeVoid("/mailingList/removeMailingList", params);
	}

	@Override
	public List<MLEmail> addEmails(AccessToken token, Integer mailingListId,
			List<MLEmail> email) throws ServerFault {
		if(mailingListId == null || email == null){
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingListId", mailingListId.toString());
		params.put("mailingListEmails", mlWriter.getMailingListEmailsAsString(email));
		Document doc = execute("/mailingList/addEmails", params);
		checkServerFaultException(doc);
		return mlParser.parseMailingListEmails(doc);
	}
	
	@Override
	public void removeEmail(AccessToken token, Integer mailingListId,
			Integer emailId) throws ServerFault {
		if(mailingListId == null || emailId == null){
			return;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingListId", mailingListId.toString());
		params.put("mailingListEmailId", emailId.toString());
		executeVoid("/mailingList/removeEmail", params);
	}
	
}
