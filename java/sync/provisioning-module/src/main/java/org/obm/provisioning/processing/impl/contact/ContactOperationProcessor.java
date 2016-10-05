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
package org.obm.provisioning.processing.impl.contact;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.impl.AbstractOperationProcessor;
import org.obm.push.utils.DateUtils;
import org.obm.service.contact.ContactService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.AddressBookReference;
import org.obm.sync.dao.Tracking;

import com.google.common.base.Optional;
import com.google.inject.Inject;

public class ContactOperationProcessor extends AbstractOperationProcessor {

	private static final String PAPI_ORIGIN = "papi";

	@Inject
	private ContactService contactService;
	
	@Inject
	private AccessToken.Factory accessTokenFactory;

	@Inject
	ContactOperationProcessor() {
		super(BatchEntityType.CONTACT, HttpVerb.POST);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		try {
			String userEmail = operation.getRequest().getParams().get(Request.USERS_EMAIL_KEY);
			AccessToken token = accessTokenFactory.build(getUserFromDao(userEmail, batch.getDomain()), PAPI_ORIGIN);
			
			contactService.importVCF(token, operation.getRequest().getBody(), 
				buildTrackingIfPresent(token, operation),
				buildAddressBookReferenceIfPresent(operation)
			);
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	private Optional<Tracking> buildTrackingIfPresent(AccessToken token, Operation operation) {
		String reference = operation.getRequest().getParams().get(Request.TRACKING_REF);
		String date = operation.getRequest().getParams().get(Request.TRACKING_DATE);
		
		if (reference != null && date != null) {
			return Optional.of(new Tracking(token, reference, DateUtils.dateUTC(date)));
		}
		
		return Optional.absent();
	} 

	private Optional<AddressBookReference> buildAddressBookReferenceIfPresent(Operation operation) {
		String reference = operation.getRequest().getParams().get(Request.ADDRESSBOOK_REF);
		String origin = operation.getRequest().getParams().get(Request.ADDRESSBOOK_REF_ORIGIN);
		
		if (reference != null && origin != null) {
			return Optional.of(new AddressBookReference(reference, origin));
		}
		
		return Optional.absent();
	} 

}
