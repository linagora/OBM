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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.ContactLabel;
import org.obm.sync.book.Phone;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.inject.Singleton;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

@Singleton
public class VCFtoContactConverter {

	private static final Collection<EmailType> CONSIDERED_EMAIL_TYPES = ImmutableSet.of(
			EmailType.INTERNET, EmailType.WORK, EmailType.HOME);

	
	private static final Predicate<VCard> HAS_AT_LEAST_ONE_PROPERTY = new Predicate<VCard>() {

		@Override
		public boolean apply(VCard vCard) {
			return !vCard.getProperties().isEmpty();
		}
		
	};

	public List<Contact> convert(String vcf) {
		return FluentIterable.from(Ezvcard.parse(Strings.nullToEmpty(vcf)).all())
			.filter(HAS_AT_LEAST_ONE_PROPERTY)
			.transform( new Function<VCard, Contact>() {
				
				@Override
				public Contact apply(VCard vCard) {
					return vCardToContact(vCard);
				}
				
			}).toList();
	}

	private Contact vCardToContact(VCard vCard) {
		Contact contact = new Contact();
		contact.setLastname(vCard.getStructuredName() != null ? vCard.getStructuredName().getFamily() : null);
		contact.setFirstname(vCard.getStructuredName() != null ? vCard.getStructuredName().getGiven() : null);
		contact.setAka(vCard.getFormattedName() != null ? vCard.getFormattedName().getValue() : null);
		contact.setCompany(vCard.getOrganization() != null ? firstValueOrNull(vCard.getOrganization().getValues()) : null);
		contact.setTitle(vCard.getTitles().isEmpty() ? null : vCard.getTitles().get(0).getValue());
		contact.setAnniversary(vCard.getBirthday() != null ? vCard.getBirthday().getDate() : null);
		
		addPhones(contact, vCard.getTelephoneNumbers());
		addAddress(contact, vCard.getAddresses());
		addEmails(contact, vCard.getEmails());
		
		return contact;
	}

	private void addPhones(Contact contact, List<Telephone> phones) {
		if (phones != null) {
			for (Telephone phone : phones) {
				if (hasPhoneType(phone, TelephoneType.CELL)) {
					addPhone(contact, ContactLabel.MOBILE, phone);
				} else if (hasPhoneType(phone, TelephoneType.HOME)) {
					addPhone(contact, ContactLabel.PHONE_HOME, phone);
				} else if (hasPhoneType(phone, TelephoneType.WORK)) {
					addPhone(contact, ContactLabel.PHONE, phone);
				} else if (hasPhoneType(phone, TelephoneType.FAX)) {
					addPhone(contact, ContactLabel.FAX, phone);
				} else if (hasNoWorkPhoneYet(contact)) {
					addPhone(contact, ContactLabel.PHONE, phone);
				}
			}	
		}
	}

	private boolean hasPhoneType(ezvcard.property.Telephone phone, TelephoneType type) {
		return phone.getTypes().contains(type);
	}
	
	private void addPhone(Contact contact, ContactLabel label, Telephone phone) {
		contact.addPhone(label.getContactLabel(), new Phone(phone.getText()));
	}

	private boolean hasNoWorkPhoneYet(Contact contact) {
		return contact.getPhones().get(ContactLabel.PHONE) == null;
	}

	private void addAddress(Contact contact, List<ezvcard.property.Address> addresses) {
		if (addresses != null) {
			for (ezvcard.property.Address add : addresses) {
				if (hasAddressType(add, AddressType.HOME)) {
					addAddress(contact, ContactLabel.ADDRESS_HOME, add);
				} else if (hasAddressType(add, AddressType.WORK)) {
					addAddress(contact, ContactLabel.ADDRESS, add);
				} else if (hasNoWorkAddressYet(contact)) {
					addAddress(contact, ContactLabel.ADDRESS, add);
				}
			}
		}
	}

	private boolean hasAddressType(ezvcard.property.Address addresse, AddressType type) {
		return addresse.getTypes().contains(type);
	}

	private boolean hasNoWorkAddressYet(Contact contact) {
		return contact.getAddresses().get(ContactLabel.ADDRESS) == null;
	}

	private void addAddress(Contact contact, ContactLabel addressHome, ezvcard.property.Address from) {
		contact.addAddress(addressHome.getContactLabel(), new Address(
				from.getStreetAddress(), from.getPostalCode(), from.getPoBox(), 
				from.getLocality(), from.getCountry(), from.getRegion()));	
	}

	private void addEmails(Contact contact, List<Email> emails) {
		if (emails != null) {
			Iterator<ContactLabel> emailsLabel = Iterators.forArray(ContactLabel.EMAIL, ContactLabel.EMAIL2,ContactLabel.EMAIL3); 
			for (ezvcard.property.Email email : emails) {
				if (hasSupportedEmailType(email) && emailsLabel.hasNext() && EmailAddress.isEmailAddress(email.getValue())) {
					contact.addEmail(emailsLabel.next().getContactLabel(), EmailAddress.loginAtDomain(email.getValue()));
				}
			}
		}
	}

	private boolean hasSupportedEmailType(ezvcard.property.Email email) {
		return email.getTypes().isEmpty() || 
			Iterators.any(email.getTypes().iterator(), Predicates.in(CONSIDERED_EMAIL_TYPES));
	}

	private <T> T firstValueOrNull(List<T> values) {
		return Iterables.getFirst(values, null);
	}
	
}
