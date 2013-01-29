/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common.calendar;

import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.utils.DisplayNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.contact.ContactDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.resource.Resource;
import fr.aliacom.obm.common.resource.ResourceDao;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;

@Singleton
public class AttendeeServiceJdbcImpl implements AttendeeService {

	private final UserDao userDao;
	private final ContactDao contactDao;
	private final ResourceDao resourceDao;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	@VisibleForTesting
	AttendeeServiceJdbcImpl(UserDao userDao, ContactDao contactDao, ResourceDao resourceDao) {
		this.userDao = userDao;
		this.resourceDao = resourceDao;
		this.contactDao = contactDao;
	}

	@Override
	public UserAttendee findUserAttendee(String name, String email, ObmDomain domain) {
		ObmUser user = userDao.findUser(email, domain);
		
		if (user != null) {
			return UserAttendee
					.builder()
					.entityId(user.getEntityId())
					.displayName(DisplayNameUtils.getDisplayName(user.getCommonName(), user.getFirstName(), user.getLastName()))
					.email(user.getEmail())
					.build();
		}
		
		return null;
	}

	@Override
	public ContactAttendee findContactAttendee(String name, String email, boolean createIfNeeded, ObmDomain domain, Integer ownerId) {
		try {
			Contact contact = contactDao.findAttendeeContactFromEmailForUser(email, ownerId);
			
			if (contact != null) {
				return attendeeFromContact(contact);
			}
			
			if (createIfNeeded) {
				contact = contactDao.createCollectedContact(name, email, domain, ownerId);
				
				return attendeeFromContact(contact);
			}
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		return null;
	}

	@Override
	public ResourceAttendee findResourceAttendee(String name, String email, ObmDomain domain, Integer ownerId) {
		try {
			Resource resource = resourceDao.findAttendeeResourceFromEmailForUser(email, ownerId);
			
			// Not found by email, fallback to a search by name
			if (resource == null) {
				resource = resourceDao.findAttendeeResourceFromNameForUser(name, ownerId);
			}
			
			if (resource != null) {
				return ResourceAttendee
						.builder()
						.entityId(resource.getEntityId())
						.displayName(resource.getName())
						.email(resource.getEmail())
						.build();
			}
		}
		catch (Exception e) {
			logger.error("Couldn't retrieve resource attendee from database.", e);
		}
		
		return null;
	}

	@Override
	public Attendee findAttendee(String name, String email, boolean createContactIfNeeded, ObmDomain domain, Integer ownerId) {
		Attendee attendee = findUserAttendee(name, email, domain);
		
		if (attendee == null) {
			attendee = findResourceAttendee(name, email, domain, ownerId);
			
			if (attendee == null) {
				attendee = findContactAttendee(name, email, createContactIfNeeded, domain, ownerId);
			}
		}
		
		return attendee;
	}
	
	private ContactAttendee attendeeFromContact(Contact contact) {
		return ContactAttendee
				.builder()
				.entityId(contact.getEntityId())
				.displayName(DisplayNameUtils.getDisplayName(contact.getCommonname(), contact.getFirstname(), contact.getLastname()))
				.email(Iterables.get(contact.getEmails().values(), 0).getEmail())
				.build();
	}

}
