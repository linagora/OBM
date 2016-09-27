/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016 Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.processing.impl.addressboook;

import java.sql.SQLException;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.obm.annotations.transactional.Transactional;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.ContactDao;
import org.obm.provisioning.ProvisioningService;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.json.AddressBookCreationJsonDeserializer;
import org.obm.provisioning.processing.impl.AbstractOperationProcessor;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.AddressBook.Id;
import org.obm.sync.book.AddressBookCreation;
import org.obm.sync.book.AddressBookRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class AddressBookOperationProcessor extends AbstractOperationProcessor {

	private static final String PAPI_ORIGIN = "provisioning";

	private static final Logger logger = LoggerFactory.getLogger(AddressBookOperationProcessor.class);

	@Inject
	private AddressBookDao addressBookDao;

	@Inject
	private ContactDao contactDao;
	
	@Inject
	AddressBookOperationProcessor() {
		super(BatchEntityType.ADDRESS_BOOK, HttpVerb.POST);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		try {
			String userEmail = operation.getRequest().getParams().get(Request.USERS_EMAIL_KEY);
			ObmUser owner = getUserFromDao(userEmail, batch.getDomain());
			
			AddressBookCreation creation = getAddressBookCreationFromRequestBody(operation);
			
			if (creation.getReference().isPresent()) {
				createAndTrackReference(creation, owner);
			} else if (AddressBookRole.CUSTOM.equals(creation.getRole())) {
				createCustomAddressBook(creation, owner);
			} else {
				logger.warn("Processing of a non-custom addressbook without reference is a noop");
			}
			
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	private void createAndTrackReference(AddressBookCreation creation, ObmUser owner) throws DaoException, SQLException {
		switch (creation.getRole()) {
		case PRIMARY:
			trackReference(creation, contactDao.findDefaultAddressBookId(owner.getUid(), false));
			break;
		case COLLECTED:
			trackReference(creation, contactDao.findDefaultAddressBookId(owner.getUid(), true));
			break;
		case CUSTOM:
			Optional<Id> existingAddressBook = findAddressBookIdFromReference(creation);
			
			if (existingAddressBook.isPresent()) {
				logger.info("Renaming the addressbook {} to {}", existingAddressBook.get(), creation.getName());
				addressBookDao.rename(existingAddressBook.get(), creation.getName());
			} else {
				trackReference(creation, createCustomAddressBook(creation, owner).getUid());
			}
			break;
		}
	}

	private void trackReference(AddressBookCreation creation, Id addressBookId) throws DaoException {
		logger.info("Creating a new addressbook reference for id {}, role {}, reference {}",
				addressBookId.getId(), creation.getRole().name(), creation.getReference().get());
		addressBookDao.createReference(creation.getReference().get(), addressBookId);
	}

	private AddressBookCreation getAddressBookCreationFromRequestBody(Operation operation) {
		String requestBody = operation.getRequest().getBody();
		try {
			return getDefaultObjectMapper().readValue(requestBody, AddressBookCreation.class);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot parse AddressBookCreation object from request body %s.", requestBody), e);
		}
	}
	
	private AddressBook createCustomAddressBook(AddressBookCreation creation, ObmUser owner) throws DaoException {
		logger.info("Creating a new custom addressbook named {} for user {}", creation.getName(), owner.getLoginAtDomain());
		AddressBook created = addressBookDao.create(AddressBook.builder()
					.name(creation.getName())
					.syncable(true)
					.defaultBook(false)
					.origin(PAPI_ORIGIN)
					.build(), owner);
		addressBookDao.enableAddressBookSynchronization(created.getUid(), owner);
		return created;
	}

	private Optional<Id> findAddressBookIdFromReference(AddressBookCreation creation) throws DaoException {
		if (creation.getReference().isPresent()) {
			return addressBookDao.findByReference(creation.getReference().get());
		}
		return Optional.absent();
	}

	private ObjectMapper getDefaultObjectMapper() {
		Module module = new SimpleModule("InBatch", new Version(0, 0, 0, null))
			.addDeserializer(AddressBookCreation.class, new AddressBookCreationJsonDeserializer());

		return ProvisioningService.createObjectMapper(module);
	}

}
