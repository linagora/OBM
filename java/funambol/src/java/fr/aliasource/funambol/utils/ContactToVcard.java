package fr.aliasource.funambol.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.funambol.common.pim.common.Converter;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.common.XTag;
import com.funambol.common.pim.contact.Address;
import com.funambol.common.pim.contact.Contact;
import com.funambol.common.pim.contact.Email;
import com.funambol.common.pim.contact.Name;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.contact.Phone;
import com.funambol.common.pim.contact.Title;
import com.funambol.common.pim.contact.WebPage;
import com.funambol.common.pim.converter.BaseConverter;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.utility.TimeUtils;

/**
 * This object is a converter from a Contact object model to a vCard string
 * 
 * @see Converter , Marco Magistrali
 * @version $Id: ContactToVcard.java,v 1.5 2008-08-26 15:51:24 luigiafassina Exp
 *          $
 */
public class ContactToVcard extends BaseConverter {

	private String newLine = "\r\n"; // default

	// ------------------------------------------------------------- Constructor

	public ContactToVcard(TimeZone timezone, String charset) {
		super(timezone, charset);
	}

	// ---------------------------------------------------------- Public methods

	/**
	 * Performs the conversion.
	 * 
	 * @param contact
	 *            the Contact to be converted in vCard format
	 * 
	 * @return a string containing the vCard representation of this Contact
	 */
	public String convert(Object obj) throws ConverterException {

		Contact contact = (Contact) obj;
		StringBuffer output = new StringBuffer("BEGIN:VCARD" + newLine
				+ "VERSION:2.1" + newLine);

		if (contact.getName() != null) {
			output.append(composeFieldName(contact.getName()));
			output.append(composeFieldFormalName(contact.getName()
					.getDisplayName()));
			output
					.append(composeFieldNickname(contact.getName()
							.getNickname()));
		}
		if (contact.getPersonalDetail() != null) {
			output.append(composeFieldAddress(contact.getPersonalDetail()
					.getAddress(), "HOME"));
			output.append(composeFieldAddress(contact.getPersonalDetail()
					.getOtherAddress(), "OTHER"));
			output.append(composeFieldBirthday(contact.getPersonalDetail()
					.getBirthday()));
			output.append(composeFieldOtherLabel(contact.getPersonalDetail()
					.getOtherAddress().getLabel()));
			output.append(composeFieldPersonalLabel(contact.getPersonalDetail()
					.getAddress().getLabel()));
			output.append(composeFieldTelephone(contact.getPersonalDetail()
					.getPhones()));
			output.append(composeFieldEmail(contact.getPersonalDetail()
					.getEmails()));
			output.append(composeFieldWebPage(contact.getPersonalDetail()
					.getWebPages()));
		}
		if (contact.getBusinessDetail() != null) {
			output.append(composeFieldAddress(contact.getBusinessDetail()
					.getAddress(), "WORK"));
			output.append(composeFieldRole(contact.getBusinessDetail()
					.getRole()));
			output.append(composeFieldTitle(contact.getBusinessDetail()
					.getTitles()));
			output
					.append(composeFieldOrg(contact.getBusinessDetail()
							.getCompany(), contact.getBusinessDetail()
							.getDepartment()));
			output.append(composeFieldBusinessLabel(contact.getBusinessDetail()
					.getAddress().getLabel()));
			output.append(composeFieldTelephone(contact.getBusinessDetail()
					.getPhones()));
			output.append(composeFieldEmail(contact.getBusinessDetail()
					.getEmails()));
			output.append(composeFieldWebPage(contact.getBusinessDetail()
					.getWebPages()));
		}
		output.append(composeFieldNote(contact.getNotes()));
		output.append(composeFieldXTag(contact.getXTags()));
		output.append(composeFieldRevision(contact.getRevision()));
		output.append(composeFieldCategories(contact.getCategories()));
		output
				.append(composeFieldPhoto(contact.getPersonalDetail()
						.getPhoto()));
		output.append(composeFieldUid(contact.getUid()));
		output.append(composeFieldFolder(contact.getFolder()));

		output.append("END:VCARD").append(newLine);
		return output.toString();
	}

	/**
	 * Sets a new string as the new-line marker.
	 * 
	 * @param newLine
	 *            the string to use as a new-line marker
	 */
	public void setNewLine(String newLine) {
		this.newLine = newLine;
	}

	/**
	 * Returns the string used as the new-line marker.
	 * 
	 * @param newLine
	 *            the string to use as a new-line marker
	 */
	public String getNewLine() {
		return newLine;
	}

	// --------------------------------------------------------- Private methods

	/**
	 * @return a representation of the v-card field N:
	 */
	private StringBuffer composeFieldName(Name name) throws ConverterException {
		if (name.getLastName().getPropertyValue() == null
				&& name.getFirstName().getPropertyValue() == null
				&& name.getMiddleName().getPropertyValue() == null
				&& name.getSalutation().getPropertyValue() == null
				&& name.getSuffix().getPropertyValue() == null) {
			return new StringBuffer(0);
		}

		StringBuffer output = new StringBuffer(120); // Estimate 120 as needed
		ArrayList properties = new ArrayList();

		if (name.getLastName().getPropertyValue() != null) {
			output.append(escapeSeparator((String) name.getLastName()
					.getPropertyValue()));
			properties.add(name.getLastName());
		}
		output.append(';');
		if (name.getFirstName().getPropertyValue() != null) {
			output.append(escapeSeparator((String) name.getFirstName()
					.getPropertyValue()));
			properties.add(name.getFirstName());
		}
		output.append(';');
		if (name.getMiddleName().getPropertyValue() != null) {
			output.append(escapeSeparator((String) name.getMiddleName()
					.getPropertyValue()));
			properties.add(name.getMiddleName());
		}
		output.append(';');
		if (name.getSalutation().getPropertyValue() != null) {
			output.append(escapeSeparator((String) name.getSalutation()
					.getPropertyValue()));
			properties.add(name.getSalutation());
		}
		output.append(';');
		if (name.getSuffix().getPropertyValue() != null) {
			output.append(escapeSeparator((String) name.getSuffix()
					.getPropertyValue()));
			properties.add(name.getSuffix());
		}

		return composeVCardComponent(output.toString(), properties, "N");
	}

	/**
	 * @return a representation of the v-card field FN:
	 */
	private StringBuffer composeFieldFormalName(Property displayName)
			throws ConverterException {
		if (displayName.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(displayName);

			return composeVCardComponent(escapeSeparator((String) displayName
					.getPropertyValue()), properties, "FN");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field NICKNAME:
	 */
	private StringBuffer composeFieldNickname(Property nickname)
			throws ConverterException {

		if (nickname.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(nickname);

			return composeVCardComponent(escapeSeparator((String) nickname
					.getPropertyValue()), properties, "NICKNAME");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field ADR, ADR;HOME, ADR;WORK if
	 *         type = HOME then set ADR;HOME if type = OTHER then set ADR if
	 *         type = WORK then set ADR;WORK
	 */
	private StringBuffer composeFieldAddress(Address address, String type)
			throws ConverterException {
		if ((address == null)
				|| (address.getPostOfficeAddress().getPropertyValue() == null
						&& address.getRoomNumber().getPropertyValue() == null
						&& address.getStreet().getPropertyValue() == null
						&& address.getCity().getPropertyValue() == null
						&& address.getState().getPropertyValue() == null
						&& address.getPostalCode().getPropertyValue() == null
						&& address.getCountry().getPropertyValue() == null && address
						.getExtendedAddress().getPropertyValue() == null)) {
			return new StringBuffer(0);
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		if (address.getPostOfficeAddress().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address
					.getPostOfficeAddress().getPropertyValue()));
			properties.add(address.getPostOfficeAddress());
		}
		output.append(';');
		if (address.getExtendedAddress().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getExtendedAddress()
					.getPropertyValue()));
			properties.add(address.getExtendedAddress());
		}
		output.append(';');
		if (address.getStreet().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getStreet()
					.getPropertyValue()));
			properties.add(address.getStreet());
		}
		output.append(';');
		if (address.getCity().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getCity()
					.getPropertyValue()));
			properties.add(address.getCity());
		}
		output.append(';');
		if (address.getState().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getState()
					.getPropertyValue()));
			properties.add(address.getState());
		}
		output.append(';');
		if (address.getPostalCode().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getPostalCode()
					.getPropertyValue()));
			properties.add(address.getPostalCode());
		}
		output.append(';');
		if (address.getCountry().getPropertyValue() != null) {
			output.append(escapeSeparator((String) address.getCountry()
					.getPropertyValue()));
			properties.add(address.getCountry());
		}

		if (("HOME").equals(type)) {
			return composeVCardComponent(output.toString(), properties,
					"ADR;HOME");
		} else if (("OTHER").equals(type)) {
			return composeVCardComponent(output.toString(), properties, "ADR");
		} else if (("WORK").equals(type)) {
			return composeVCardComponent(output.toString(), properties,
					"ADR;WORK");
		}

		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field PHOTO:
	 */
	private StringBuffer composeFieldPhoto(Property photo)
			throws ConverterException {
		if (photo.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(photo);

			//
			// The charset must be null (not set) since:
			// 1. it is useless since the content is in base64
			// 2. on some Nokia phone it doesn't work since for some reason the
			// phone
			// adds a new photo and the result is that a contact has two photos
			// Examples of wrong phones: Nokia N91, 7610, 6630
			//
			return composeVCardComponent((String) photo.getPropertyValue(),
					properties, "PHOTO");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field BDAY:
	 */
	private String composeFieldBirthday(String birthday)
			throws ConverterException {

		if (birthday == null) {
			return "";
		}

		try {
			return ("BDAY:" + TimeUtils.normalizeToISO8601(birthday, timezone) + newLine);
		} catch (Exception ex) {
			throw new ConverterException("Error parsing birthday", ex);
		}
	}

	/**
	 * @return a representation of the v-card field TEL:
	 */
	private String composeFieldTelephone(List phones) throws ConverterException {

		if ((phones == null) || phones.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		Phone telephone = null;
		String phoneType = null;

		int size = phones.size();
		for (int i = 0; i < size; i++) {

			telephone = (Phone) phones.get(i);
			phoneType = composePhoneType(telephone.getPhoneType());

			properties.clear();
			properties.add(0, telephone);

			output.append(composeVCardComponent(
					escapeSeparator((String) telephone.getPropertyValue()),
					properties, "TEL" + phoneType));
		}

		return output.toString();
	}

	/**
	 * @return the v-card representation of a telephone type
	 */
	private String composePhoneType(String type) {
		if (type == null) {
			return "";
		}

		//
		// Mobile phone
		//
		if (("MobileTelephoneNumber").equals(type)) {
			return ";CELL";
		} else if (("MobileHomeTelephoneNumber").equals(type)) {
			return ";CELL;HOME";
		} else if (("MobileBusinessTelephoneNumber").equals(type)) {
			return ";CELL;WORK";
		}

		//
		// Voice
		//
		if (("OtherTelephoneNumber").equals(type)) {
			return ";VOICE";
		} else if (("HomeTelephoneNumber").equals(type)) {
			return ";VOICE;HOME";
		} else if (("BusinessTelephoneNumber").equals(type)) {
			return ";VOICE;WORK";
		}

		//
		// FAX
		//
		if (("OtherFaxNumber").equals(type)) {
			return ";FAX";
		} else if (("HomeFaxNumber").equals(type)) {
			return ";FAX;HOME";
		} else if (("BusinessFaxNumber").equals(type)) {
			return ";FAX;WORK";
		}

		//
		// Pager
		//
		if (("PagerNumber").equals(type)) {
			return ";PAGER";
		}

		for (int j = 2; j <= 10; j++) {
			//
			// Mobile phone
			//
			if (("Mobile" + j + "TelephoneNumber").equals(type)) {
				return ";CELL";
			} else if (("MobileHome" + j + "TelephoneNumber").equals(type)) {
				return ";CELL;HOME";
			} else if (("MobileBusiness" + j + "TelephoneNumber").equals(type)) {
				return ";CELL;WORK";
			}

			//
			// Voice
			//
			if (("Other" + j + "TelephoneNumber").equals(type)) {
				return ";VOICE";
			} else if (("Home" + j + "TelephoneNumber").equals(type)) {
				return ";VOICE;HOME";
			} else if (("Business" + j + "TelephoneNumber").equals(type)) {
				return ";VOICE;WORK";
			}

			//
			// Fax
			//
			if (("Other" + j + "FaxNumber").equals(type)) {
				return ";FAX";
			} else if (("Home" + j + "FaxNumber").equals(type)) {
				return ";FAX;HOME";
			} else if (("Business" + j + "FaxNumber").equals(type)) {
				return ";FAX;WORK";
			}

			//
			// Pager
			//
			if (("PagerNumber" + j).equals(type)) {
				return ";PAGER";
			}
		}

		//
		// Others
		//
		if (("CarTelephoneNumber").equals(type)) {
			return ";CAR;VOICE";
		} else if (("CompanyMainTelephoneNumber").equals(type)) {
			return ";WORK;PREF";
		} else if (("PrimaryTelephoneNumber").equals(type)) {
			return ";PREF;VOICE";
		}

		return "";
	}

	/**
	 * @return a representation of the v-card field EMAIL:
	 */
	private String composeFieldEmail(List emails) throws ConverterException {

		if ((emails == null) || emails.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		Email email = null;
		String emailType = null;

		int size = emails.size();
		for (int i = 0; i < size; i++) {

			email = (Email) emails.get(i);
			emailType = composeEmailType(email.getEmailType());

			properties.clear();
			properties.add(0, email);

			output.append(composeVCardComponent(escapeSeparator((String) email
					.getPropertyValue()), properties, "EMAIL" + emailType));
		}

		return output.toString();
	}

	/**
	 * @return the v-card representation of a email type
	 */
	private String composeEmailType(String type) {
		if (type == null) {
			return "";
		}

		if (("Email1Address").equals(type)) {
			return ";INTERNET";
		} else if (("Email2Address").equals(type)) {
			return ";INTERNET;HOME";
		} else if (("Email3Address").equals(type)) {
			return ";INTERNET;WORK";
		}

		for (int j = 2; j <= 10; j++) {
			if (("Other" + j + "EmailAddress").equals(type)) {
				return ";INTERNET";
			} else if (("HomeEmail" + j + "Address").equals(type)) {
				return ";INTERNET;HOME";
			} else if (("BusinessEmail" + j + "Address").equals(type)) {
				return ";INTERNET;WORK";
			}
		}

		if (("IMAddress").equals(type)) {
			return ";INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER";
		}
		return "";
	}

	private String composeFieldWebPage(List webpages) throws ConverterException {

		if ((webpages == null) || webpages.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		WebPage address = null;
		String webpageType = null;

		int size = webpages.size();
		for (int i = 0; i < size; i++) {

			address = (WebPage) webpages.get(i);
			webpageType = composeWebPageType(address.getWebPageType());

			properties.add(0, address);

			output.append(composeVCardComponent(
					escapeSeparator((String) address.getPropertyValue()),
					properties, webpageType));
		}

		return output.toString();
	}

	/**
	 * @return the v-card representation of a web page type
	 */
	private String composeWebPageType(String type) {
		if (type == null) {
			return "";
		} else if (("WebPage").equals(type)) {
			return "URL";
		} else if (("HomeWebPage").equals(type)) {
			return "URL;HOME";
		} else if (("BusinessWebPage").equals(type)) {
			return "URL;WORK";
		}

		for (int j = 2; j <= 10; j++) {
			if (("WebPage" + j).equals(type)) {
				return "URL";
			} else if (("Home" + j + "WebPage").equals(type)) {
				return "URL;HOME";
			} else if (("Business" + j + "WebPage").equals(type)) {
				return "URL;WORK";
			}
		}

		return "";
	}

	/**
	 * @return a representation of the v-card field LABEL;HOME:
	 */
	private StringBuffer composeFieldPersonalLabel(Property label)
			throws ConverterException {

		if (label.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(label);

			return composeVCardComponent(escapeSeparator((String) label
					.getPropertyValue()), properties, "LABEL;HOME");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field LABEL;OTHER:
	 */
	private StringBuffer composeFieldOtherLabel(Property label)
			throws ConverterException {

		if (label.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(label);

			return composeVCardComponent(escapeSeparator((String) label
					.getPropertyValue()), properties, "LABEL;OTHER");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field LABEL;WORK:
	 */
	private StringBuffer composeFieldBusinessLabel(Property label)
			throws ConverterException {

		if (label.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(label);

			return composeVCardComponent(escapeSeparator((String) label
					.getPropertyValue()), properties, "LABEL;WORK");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field ROLE:
	 */
	private StringBuffer composeFieldRole(Property role)
			throws ConverterException {

		if (role.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(role);

			return composeVCardComponent(escapeSeparator((String) role
					.getPropertyValue()), properties, "ROLE");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the v-card field TITLE:
	 */
	private String composeFieldTitle(List titles) throws ConverterException {
		if ((titles == null) || titles.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		Title title = null;

		int size = titles.size();
		for (int i = 0; i < size; i++) {

			title = (Title) titles.get(i);
			properties.add(0, title);

			output.append(composeVCardComponent(escapeSeparator((String) title
					.getPropertyValue()), properties, "TITLE"));
		}

		return output.toString();
	}

	/**
	 * @return a representation of the v-card field ORG:
	 */
	private StringBuffer composeFieldOrg(Property company, Property department)
			throws ConverterException {
		if (company.getPropertyValue() == null
				&& department.getPropertyValue() == null) {
			return new StringBuffer(0);
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		if (company.getPropertyValue() != null) {
			output.append(escapeSeparator((String) company.getPropertyValue()));
			properties.add(company);
		}
		output.append(';');
		if (department.getPropertyValue() != null) {
			output.append(escapeSeparator((String) department
					.getPropertyValue()));
			properties.add(department);
		}

		return composeVCardComponent(output.toString(), properties, "ORG");
	}

	/**
	 * @return a representation of the v-card field XTag:
	 */
	private String composeFieldXTag(List xTags) throws ConverterException {
		if ((xTags == null) || xTags.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		Property xtag = null;

		int size = xTags.size();
		for (int i = 0; i < size; i++) {

			XTag xtagObj = (XTag) xTags.get(i);

			xtag = xtagObj.getXTag();

			properties.clear();
			properties.add(0, xtag);

			output.append(composeVCardComponent(escapeSeparator((String) xtag
					.getPropertyValue()), properties, (String) xtagObj
					.getXTagValue()));
		}
		return output.toString();
	}

	/**
	 * @return a representation of the v-card field NOTE:
	 */
	private String composeFieldNote(List notes) throws ConverterException {

		if ((notes == null) || notes.isEmpty()) {
			return "";
		}

		StringBuffer output = new StringBuffer();
		ArrayList properties = new ArrayList();

		Note note = null;

		int size = notes.size();
		for (int i = 0; i < size; i++) {

			note = (Note) notes.get(i);
			properties.add(0, note);

			output.append(composeVCardComponent(escapeSeparator((String) note
					.getPropertyValue()), properties, "NOTE"));
		}

		return output.toString();
	}

	/**
	 * @return a representation of the v-card field UID:
	 */
	private String composeFieldUid(String uid) {
		if (uid != null) {
			return "UID:" + uid + newLine;
		}
		return "";
	}

	/**
	 * @return a representation of the v-card field TZ:
	 */
	private String composeFieldTimezone(String tz) {
		if (tz != null) {
			return "TZ:" + tz + newLine;
		}
		return "";
	}

	/**
	 * @return a representation of the v-card field REV:
	 */
	private String composeFieldRevision(String revision) {
		if (revision != null) {
			return "REV:" + revision + newLine;
		}
		return "";
	}

	/**
	 * @return a representation of the v-card field CATEGORIES:
	 */
	private StringBuffer composeFieldCategories(Property categories)
			throws ConverterException {

		if (categories.getPropertyValue() != null) {

			ArrayList properties = new ArrayList();
			properties.add(categories);

			return composeVCardComponent(escapeSeparator((String) categories
					.getPropertyValue()), properties, "CATEGORIES");
		}
		return new StringBuffer(0);
	}

	/**
	 * @return a representation of the vCard field X-FUNAMBOL-FOLDER
	 */
	private String composeFieldFolder(String folder) {
		if (folder != null) {
			return "X-FUNAMBOL-FOLDER:" + escapeSeparator(folder) + newLine;
		}
		return "";
	}
}
