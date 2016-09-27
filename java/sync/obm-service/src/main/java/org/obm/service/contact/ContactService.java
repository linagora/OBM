/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service.contact;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.obm.domain.dao.CommitedOperationDao;
import org.obm.domain.dao.ContactDao;
import org.obm.locator.LocatorClientException;
import org.obm.provisioning.dao.exceptions.FindException;
import org.obm.service.solr.SolrHelper;
import org.obm.service.solr.SolrHelper.Factory;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.CommitedOperation;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.dao.EntityId;
import org.obm.sync.dao.Tracking;
import org.obm.sync.services.ImportVCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContactService {

	private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

	private final Factory solrHelperFactory;
	private final ContactDao contactDao;
	private final CommitedOperationDao commitedOperationDao;
	private final VCFtoContactConverter contactConverter;

	@Inject
	public ContactService(SolrHelper.Factory solrHelperFactory, 
			ContactDao contactDao, CommitedOperationDao commitedOperationDao,
			VCFtoContactConverter contactConverter) {
		this.solrHelperFactory = solrHelperFactory;
		this.contactDao = contactDao;
		this.contactConverter = contactConverter;
		this.commitedOperationDao = commitedOperationDao;
	}

	public void importVCF(AccessToken token, String vcf, Optional<Tracking> tracking) throws ImportVCardException {
		try {
			Optional<CommitedOperation<Contact>> previousImport = getPreviousImport(token, tracking);
			List<Contact> contactsToImport = contactConverter.convert(vcf);
			
			if (contactsToImport.isEmpty()) {
				logger.warn("No contact found in the VCF");
			} else if (!previousImport.isPresent()) {
				importVCFAsNew(token, contactsToImport, tracking);
			} else if (previousImportDateIsBelow(previousImport.get(), tracking.get())) {
				importVCFAsUpdatingContact(token, contactsToImport, tracking.get(), previousImport.get());
			} else {
				logger.warn("Import is skipped as the tracking reference {} is already known with date {}",
					tracking.get().getReferenceSha1(), previousImport.get().getClientDate());
			}
		} catch (Exception e) {
			throw new ImportVCardException(e);
		}
	}

	private Optional<CommitedOperation<Contact>> getPreviousImport(AccessToken token, Optional<Tracking> tracking) throws SQLException {
		if (tracking.isPresent()) {
			return Optional.fromNullable(commitedOperationDao.findAsContact(token, tracking.get().getReferenceSha1().toString()));
		}
		return Optional.absent();
	}

	private boolean previousImportDateIsBelow(CommitedOperation<Contact> commitedOperation, Tracking tracking) {
		if (!commitedOperation.getClientDate().isPresent()) {
			throw new ImportVCardException("Illegal state, a previous import matching this one has no date");
		}
		return commitedOperation.getClientDate().get().compareTo(tracking.getDate()) < 0;
	}

	private void importVCFAsNew(AccessToken token, List<Contact> contacts, Optional<Tracking> tracking) throws SQLException, ServerFault {
		AddressBook.Id addressBookId = contactDao.findDefaultAddressBookId(token.getObmId(), false);

		if (tracking.isPresent() && contacts.size() > 1) {
			throw new ImportVCardException("Illegal state, the tracking cannot be used when the VCF contains multiple vCards");
		} else if (tracking.isPresent()) {
			importContact(token, addressBookId, Iterables.getOnlyElement(contacts), tracking.get());
		} else {
			importContacts(token, addressBookId, contacts);
		}
	}

	private void importVCFAsUpdatingContact(AccessToken token, List<Contact> contacts, Tracking tracking, CommitedOperation<Contact> previousImport)
			throws SQLException, ServerFault {
		
		if (contacts.size() > 1) {
			throw new ImportVCardException("Illegal state, cannot do any update when the VCF contains multiple vCards");
		}
		
		Contact updatedContact = updateContact(token, previousImport.getEntity(), Iterables.getOnlyElement(contacts));
		trackOperation(token, tracking.getReferenceSha1(), tracking.getDate(), updatedContact);
	}

	private void importContact(AccessToken token, AddressBook.Id addressBookId, Contact contact, Tracking tracking) throws ServerFault {
		logger.info("Creating the contact with tracking {} {}, {}", contact.getFirstname(), contact.getLastname(), tracking);
		createContact(token, addressBookId.getId(), contact, tracking.getReferenceSha1(), tracking.getDate());
	}

	private void importContacts(AccessToken token, AddressBook.Id addressBookId, List<Contact> contactsToImport) throws ServerFault {
		for (Contact contact : contactsToImport) {
			logger.info("Creating the contact without tracking {} {}", contact.getFirstname(), contact.getLastname());
			createContact(token, addressBookId.getId(), contact, null);
		}
	}

	public Set<Integer> searchContactIds(AccessToken token, String query, Collection<AddressBook> addrBooks, Integer limit, Integer offset)
			throws LocatorClientException {
		Set<Integer> contactIds = new HashSet<Integer>();

		if (addrBooks.size() > 0) {
			SolrHelper solrHelper = solrHelperFactory.createClient(token);
			CommonsHttpSolrServer solrServer = solrHelper.getSolrContact();
			StringBuilder sb = new StringBuilder();
			sb.append("-is:archive ");
			sb.append("+addressbookId:(");
			int idx = 0;
			for (AddressBook book : addrBooks) {
				if (idx > 0) {
					sb.append(" OR ");
				}
				sb.append(book.getUid());
				idx++;
			}
			sb.append(")");
			if (query != null && !"".equals(query)) {
				sb.append(" +(displayname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR firstname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR lastname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR email:(");
				sb.append(query.toLowerCase());
				sb.append("*))");
			}
			SolrQuery params = new SolrQuery();
			params.setQuery(sb.toString());
			params.setIncludeScore(true);
			params.setRows(limit);
			params.setStart(offset);

			try {
				QueryResponse resp = solrServer.query(params);

				SolrDocumentList results = resp.getResults();
				if (logger.isDebugEnabled()) {
					logger.debug("SOLR query time for " + results.size()
							+ " results: " + resp.getElapsedTime() + "ms.");
				}

				for (int i = 0; i < limit && i < results.size(); i++) {
					SolrDocument doc = results.get(i);
					Map<String, Object> payload = doc.getFieldValueMap();
					contactIds.add((Integer) payload.get("id"));
				}
			} catch (SolrServerException e) {
				logger.error("Error querying server for '" + sb.toString()
						+ " url: "
						+ ClientUtils.toQueryString(params, false), e);
			}
		}
		
		return contactIds;
	}

	public Contact createContact(AccessToken token, Integer addressBookId, Contact contact, String clientId) throws ServerFault {
		return createContact(token, addressBookId, contact, clientId, null);
	}
	
	private Contact createContact(AccessToken token, Integer addressBookId, Contact contact, String clientId, Date clientDate) throws ServerFault {
		try {
			CommitedOperation<Contact> commitedContact = commitedOperationDao.findAsContact(token, clientId);
			if (commitedContact != null) {
				return commitedContact.getEntity();
			}
			
			Contact createdContact = contactDao.createContactInAddressBook(token, contact, addressBookId);
			trackOperation(token, clientId, clientDate, createdContact);
			return createdContact;
		} catch (SQLException e) {
			throw new ServerFault(e.getMessage());
		}
	}

	private void trackOperation(AccessToken token, String clientId, Date clientDate, Contact createdContact) throws SQLException, ServerFault {
		EntityId entityId = createdContact.getEntityId();
		if (clientId != null && entityId != null) {
			commitedOperationDao.store(token, 
					CommitedElement.builder()
						.clientId(clientId)
						.clientDate(clientDate)
						.entityId(entityId)
						.kind(Kind.VCONTACT)
						.build());
		}
	}

	public Contact updateContact(AccessToken token, Contact previousContact, Contact newContact) throws ServerFault {
		try {
			newContact.setEntityId(previousContact.getEntityId());
			newContact.setFolderId(previousContact.getFolderId());
			
			if (newContact.getUid() == null) {
				newContact.setUid(previousContact.getUid());
			}

			return contactDao.updateContact(token, newContact);
		} catch (SQLException|FindException|EventNotFoundException ex) {
			throw new ServerFault(ex.getMessage());
		}
	}
}
