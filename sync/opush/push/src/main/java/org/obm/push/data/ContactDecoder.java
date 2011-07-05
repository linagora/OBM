package org.obm.push.data;

import org.obm.push.backend.MSContact;
import org.obm.push.data.email.Type;
import org.obm.push.store.IApplicationData;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

// Nouveau contact
//<Commands>
//<Add>
//<ClientId>2147483657</ClientId>
//<ApplicationData>
//<FileAs>Tttt</FileAs>
//<FirstName>Tttt</FirstName>
//<Picture/>
//</ApplicationData>
//</Add>
//</Commands>

public class ContactDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		MSContact contact = new MSContact();

		contact.setAssistantName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "AssistantName")));
		contact.setAssistantPhoneNumber(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "AssistantTelephoneNumber")));
		contact.setAssistnamePhoneNumber(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "AssistnameTelephoneNumber")));
		contact.setBusiness2PhoneNumber(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "Business2TelephoneNumber")));
		contact.setBusinessPhoneNumber(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "BusinessTelephoneNumber")));
		contact.setWebPage(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Webpage")));

		contact.setDepartment(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "Department")));
		contact.setEmail1Address(parseDOMEmail(DOMUtils.getUniqueElement(
				syncData, "Email1Address")));
		contact.setEmail2Address(parseDOMEmail(DOMUtils.getUniqueElement(
				syncData, "Email2Address")));
		contact.setEmail3Address(parseDOMEmail(DOMUtils.getUniqueElement(
				syncData, "Email3Address")));
		contact.setBusinessFaxNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "BusinessFaxNumber")));
		contact.setFileAs(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"FileAs")));
		contact.setFirstName(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"FirstName")));
		contact.setMiddleName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "MiddleName")));
		contact.setHomeAddressCity(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeAddressCity")));
		contact.setHomeAddressCountry(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeAddressCountry")));
		contact.setHomeFaxNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeFaxNumber")));
		contact.setHomePhoneNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeTelephoneNumber")));
		contact.setHome2PhoneNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "Home2TelephoneNumber")));
		contact.setHomeAddressPostalCode(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "HomeAddressPostalCode")));
		contact.setHomeAddressState(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeAddressState")));
		contact.setHomeAddressStreet(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "HomeAddressStreet")));
		contact.setMobilePhoneNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "MobileTelephoneNumber")));
		contact.setSuffix(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Suffix")));
		contact.setCompanyName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "CompanyName")));
		contact.setOtherAddressCity(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OtherAddressCity")));
		contact.setOtherAddressCountry(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "OtherAddressCountry")));
		contact.setCarPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "CarTelephoneNumber")));
		contact.setOtherAddressPostalCode(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "OtherAddressPostalCode")));
		contact.setOtherAddressState(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OtherAddressState")));
		contact.setOtherAddressStreet(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OtherAddressStreet")));
		contact.setPagerNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "PagerNumber")));
		contact.setTitle(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Title")));
		contact.setBusinessPostalCode(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "BusinessAddressPostalCode")));
		contact.setBusinessState(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "BusinessAddressState")));
		contact.setBusinessStreet(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "BusinessAddressStreet")));
		contact.setBusinessAddressCountry(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "BusinessAddressCountry")));
		contact.setBusinessAddressCity(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "BusinessAddressCity")));
		contact.setLastName(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"LastName")));
		contact.setSpouse(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Spouse")));
		contact.setJobTitle(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"JobTitle")));
		contact.setYomiFirstName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "YomiFirstName")));
		contact.setYomiLastName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "YomiLastName")));
		contact.setYomiCompanyName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "YomiCompanyName")));
		contact.setOfficeLocation(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "OfficeLocation")));
		contact.setRadioPhoneNumber(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "RadioTelephoneNumber")));
		contact.setPicture(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Picture")));
		contact.setAnniversary(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"Anniversary")));
		contact.setBirthday(parseDOMDate(DOMUtils.getUniqueElement(syncData,
				"Birthday")));

		contact.setCategories(parseDOMStringCollection(DOMUtils
				.getUniqueElement(syncData, "Categories"), "Category"));
		contact.setChildren(parseDOMStringCollection(DOMUtils.getUniqueElement(
				syncData, "Children"), "Child"));
		// Contacts2

		contact.setCustomerId(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "CustomerId")));
		contact.setGovernmentId(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "GovernmentId")));
		contact.setIMAddress(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"IMAddress")));
		contact.setIMAddress2(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "IMAddress2")));
		contact.setIMAddress3(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "IMAddress3")));
		contact.setManagerName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "ManagerName")));
		contact.setCompanyMainPhone(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "CompanyMainPhone")));
		contact.setAccountName(parseDOMString(DOMUtils.getUniqueElement(
				syncData, "AccountName")));
		contact.setNickName(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"NickName")));
		contact.setMMS(parseDOMString(DOMUtils
				.getUniqueElement(syncData, "MMS")));

		contact.setData(parseDOMString(DOMUtils.getUniqueElement(syncData,
				"Data")));
		//NOKIA create contact with \r\n in body
		if(contact.getData() != null){
			contact.getData().trim();
		}

		Element body = DOMUtils.getUniqueElement(syncData, "Body");
		if (body != null) {
			Element data = DOMUtils.getUniqueElement(body, "Data");
			if (data != null) {
				Type bodyType = Type.fromInt(Integer.parseInt(DOMUtils.getUniqueElement(body,
						"Type").getTextContent()));
				String txt = data.getTextContent();
				if (bodyType == Type.PLAIN_TEXT) {
					contact.setData(data.getTextContent());
				} else if (bodyType == Type.RTF) {
					contact.setData(RTFUtils.extractB64CompressedRTF(txt));
				} else {
					logger.warn("Unsupported body type: " + bodyType + "\n"
							+ txt);
				}
			}
		} 
		Element rtf = DOMUtils.getUniqueElement(syncData, "CompressedRTF");
		if (rtf != null) {
			String txt = rtf.getTextContent();
			contact.setData(RTFUtils.extractB64CompressedRTF(txt));
		}

		return contact;
	}
}
