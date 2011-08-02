package org.obm.push.contacts;

import java.util.Map;

import org.obm.push.bean.MSContact;
import org.obm.push.bean.SearchResult;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;

/**
 * Converts between OBM & MS Exchange contact models
 */
public class ContactConverter {

	/**
	 * OBM to PDA
	 */
	public MSContact convert(Contact c) {
		MSContact msc = new MSContact();

		msc.setFirstName(c.getFirstname());
		msc.setLastName(c.getLastname());
		msc.setMiddleName(c.getMiddlename());
		msc.setSuffix(c.getSuffix());
		msc.setNickName(c.getAka());
		msc.setTitle(c.getTitle());
		msc.setDepartment(c.getService());
		msc.setCompanyName(c.getCompany());
		if (c.getWebsites().size() > 0) {
			msc.setWebPage(c.getWebsite().getUrl());
		}
		msc.setBirthday(c.getBirthday());
		msc.setAnniversary(c.getAnniversary());

		msc.setManagerName(c.getManager());
		msc.setAssistantName(c.getAssistant());
		msc.setSpouse(c.getSpouse());
		// msc.setCategories()
		// msc.setChildren(children)

		msc.setData(c.getComment());

		msc.setMobilePhoneNumber(obmPhone(c, "CELL;VOICE;X-OBM-Ref1"));
		msc.setHomePhoneNumber(obmPhone(c, "HOME;VOICE;X-OBM-Ref1"));
		msc.setHome2PhoneNumber(obmPhone(c, "HOME;VOICE;X-OBM-Ref2"));
		msc.setBusinessPhoneNumber(obmPhone(c, "WORK;VOICE;X-OBM-Ref1"));
		msc.setBusiness2PhoneNumber(obmPhone(c, "WORK;VOICE;X-OBM-Ref2"));

		msc.setBusinessFaxNumber(obmPhone(c, "WORK;FAX;X-OBM-Ref1"));
		msc.setHomeFaxNumber(obmPhone(c, "HOME;FAX;X-OBM-Ref1"));

		msc.setPagerNumber(obmPhone(c, "PAGER;X-OBM-Ref1"));

		msc.setEmail1Address(obmMail(c, "INTERNET;X-OBM-Ref1"));
		msc.setEmail2Address(obmMail(c, "INTERNET;X-OBM-Ref2"));
		msc.setEmail3Address(obmMail(c, "INTERNET;X-OBM-Ref3"));

		Map<String, Address> adds = c.getAddresses();
		if (adds.containsKey("WORK;X-OBM-Ref1")
				|| adds.containsKey("PREF;WORK;X-OBM-Ref1")) {
			Address ad = adds.get("WORK;X-OBM-Ref1");
			if (ad == null) {
				ad = adds.get("PREF;WORK;X-OBM-Ref1");
			}
			msc.setBusinessStreet(ad.getStreet());
			msc.setBusinessPostalCode(ad.getZipCode());
			msc.setBusinessAddressCity(ad.getTown());
			msc.setBusinessState(ad.getState());
			msc.setBusinessAddressCountry(ad.getCountry());
		}

		if (adds.containsKey("HOME;X-OBM-Ref1")
				|| adds.containsKey("PREF;HOME;X-OBM-Ref1")) {
			Address ad = adds.get("HOME;X-OBM-Ref1");
			if (ad == null) {
				ad = adds.get("PREF;HOME;X-OBM-Ref1");
			}
			msc.setHomeAddressStreet(ad.getStreet());
			msc.setHomeAddressPostalCode(ad.getZipCode());
			msc.setHomeAddressCity(ad.getTown());
			msc.setHomeAddressState(ad.getState());
			msc.setHomeAddressCountry(ad.getCountry());
		}

		if (adds.containsKey("OTHER;X-OBM-Ref1")
				|| adds.containsKey("PREF;OTHER;X-OBM-Ref1")) {
			Address ad = adds.get("OTHER;X-OBM-Ref1");
			if (ad == null) {
				ad = adds.get("PREF;OTHER;X-OBM-Ref1");
			}
			msc.setOtherAddressStreet(ad.getStreet());
			msc.setOtherAddressPostalCode(ad.getZipCode());
			msc.setOtherAddressCity(ad.getTown());
			msc.setOtherAddressState(ad.getState());
			msc.setOtherAddressCountry(ad.getCountry());
		}

		Map<String, InstantMessagingId> ims = c.getImIdentifiers();
		int i = 0;
		for (InstantMessagingId im : ims.values()) {
			switch (i++) {
			case 0:
				msc.setIMAddress(im.getId());
				break;
			case 1:
				msc.setIMAddress2(im.getId());
				break;
			case 2:
				msc.setIMAddress3(im.getId());
				break;
			}
			if (i >= 2) {
				break;
			}
		}

		return msc;
	}

	private String obmPhone(Contact c, String lbl) {
		Phone p = c.getPhones().get(lbl);
		if (p != null) {
			return p.getNumber();
		}
		p = c.getPhones().get("PREF;" + lbl);
		if (p != null) {
			return p.getNumber();
		}
		return null;
	}

	private String obmMail(Contact c, String lbl) {
		String ret = null;
		Email p = c.getEmails().get(lbl);
		if (p != null) {
			ret = p.getEmail();
		} else {
			p = c.getEmails().get("PREF;" + lbl);
			if (p != null) {
				ret = p.getEmail();
			}
		}
		return ret;
	}

	/**
	 * PDA to OBM
	 * 
	 * @param c
	 * @return
	 */
	public Contact contact(MSContact c) {
		Contact oc = new Contact();
		// JobTitle
		// Picture
		// YomiLastName
		// YomiFirstName
		oc.setTitle(c.getTitle());
		oc.setFirstname(c.getFirstName());
		oc.setLastname(c.getLastName());
		oc.setMiddlename(c.getMiddleName());
		oc.setSuffix(c.getSuffix());
		oc.setService(c.getDepartment());
		oc.setCompany(c.getCompanyName());
		oc.setManager(c.getManagerName());
		oc.setSpouse(c.getSpouse());
		oc.setAssistant(c.getAssistantName());
		oc.setAka(c.getNickName());
		oc.setComment(c.getData());

		addPhone(oc, "HOME;VOICE;X-OBM-Ref1", c.getHomePhoneNumber());
		addPhone(oc, "HOME;VOICE;X-OBM-Ref2", c.getHome2PhoneNumber());
		addPhone(oc, "WORK;VOICE;X-OBM-Ref1", c.getBusinessPhoneNumber());
		addPhone(oc, "WORK;VOICE;X-OBM-Ref2", c.getBusiness2PhoneNumber());

		addPhone(oc, "CELL;VOICE;X-OBM-Ref1", c.getMobilePhoneNumber());
		addPhone(oc, "PAGER;X-OBM-Ref1", c.getPagerNumber());

		int i = 0;
		if (c.getRadioPhoneNumber() != null
				&& !c.getRadioPhoneNumber().isEmpty()) {
			i++;
			addPhone(oc, "OTHER;X-OBM-Ref" + i, c.getRadioPhoneNumber());
		}
		if (c.getAssistantPhoneNumber() != null
				&& !c.getAssistantPhoneNumber().isEmpty()) {
			i++;
			addPhone(oc, "OTHER;X-OBM-Ref" + i, c.getAssistantPhoneNumber());
		}
		if (c.getCarPhoneNumber() != null && !c.getCarPhoneNumber().isEmpty()) {
			i++;
			addPhone(oc, "OTHER;X-OBM-Ref" + i, c.getCarPhoneNumber());
		}
		if (c.getCompanyMainPhone() != null
				&& !c.getCompanyMainPhone().isEmpty()) {
			i++;
			addPhone(oc, "OTHER;X-OBM-Ref" + i, c.getCompanyMainPhone());
		}

		addPhone(oc, "WORK;FAX;X-OBM-Ref1", c.getBusinessFaxNumber());
		addPhone(oc, "HOME;FAX;X-OBM-Ref1", c.getHomeFaxNumber());

		addEmail(oc, "INTERNET;X-OBM-Ref1", c.getEmail1Address());
		addEmail(oc, "INTERNET;X-OBM-Ref2", c.getEmail2Address());
		addEmail(oc, "INTERNET;X-OBM-Ref3", c.getEmail3Address());

		addAddress(oc, "WORK;X-OBM-Ref1", c.getBusinessStreet(), c
				.getBusinessPostalCode(), c.getBusinessAddressCity(), c
				.getBusinessAddressCountry(), c.getBusinessState());

		addAddress(oc, "HOME;X-OBM-Ref1", c.getHomeAddressStreet(), c
				.getHomeAddressPostalCode(), c.getHomeAddressCity(), c
				.getHomeAddressCountry(), c.getHomeAddressState());

		addAddress(oc, "OTHER;X-OBM-Ref1", c.getOtherAddressStreet(), c
				.getOtherAddressPostalCode(), c.getOtherAddressCity(), c
				.getOtherAddressCountry(), c.getOtherAddressState());

		if (c.getIMAddress() != null && !c.getIMAddress().isEmpty()) {
			oc.addIMIdentifier("XMPP;X-OBM-Ref1", new InstantMessagingId(
					"XMPP", c.getIMAddress()));
		}
		if (c.getIMAddress2() != null && !c.getIMAddress2().isEmpty()) {
			oc.addIMIdentifier("XMPP;X-OBM-Ref2", new InstantMessagingId(
					"XMPP", c.getIMAddress2()));
		}
		if (c.getIMAddress3() != null && !c.getIMAddress3().isEmpty()) {
			oc.addIMIdentifier("XMPP;X-OBM-Ref3", new InstantMessagingId(
					"XMPP", c.getIMAddress3()));
		}

		if (c.getWebPage() != null) {
			oc.addWebsite(new Website("URL;X-OBM-Ref1", c.getWebPage()));
		}

		oc.setAnniversary(c.getAnniversary());
		oc.setBirthday(c.getBirthday());
		return oc;
	}

	private void addAddress(Contact oc, String lbl, String street,
			String postalCode, String city, String country, String state) {
		if (!isEmpty(street) || !isEmpty(postalCode) || !isEmpty(city)
				|| !isEmpty(country) || !isEmpty(state)) {
			oc.addAddress(lbl, new Address(street, postalCode, null, city,
					country, state));
		}
	}

	private void addEmail(Contact oc, String label, String email) {
		if (email != null) {
			oc.addEmail(label, new Email(email));
		}
	}

	private void addPhone(Contact obmContact, String label, String msPhone) {
		if (msPhone != null) {
			obmContact.addPhone(label, new Phone(msPhone));
		}
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public SearchResult convertToSearchResult(Contact c) {
		SearchResult sr = new SearchResult();
		sr.setAlias(c.getAka());
		sr.setCompany(c.getCompany());
		sr.setDisplayName(c.getFirstname()+" "+c.getLastname());
		if(c.getEmails().values().iterator().hasNext()){
			sr.setEmailAddress(c.getEmails().values().iterator().next().getEmail());
		}
		sr.setFirstName(c.getFirstname());
		sr.setLastName(c.getLastname());
		sr.setHomePhone(obmPhone(c, "HOME;VOICE;X-OBM-Ref1"));
		sr.setMobilePhone(obmPhone(c, "CELL;VOICE;X-OBM-Ref1"));
		sr.setOffice(c.getService());
		sr.setPhone(obmPhone(c, "WORK;VOICE;X-OBM-Ref1"));
		sr.setTitle(c.getTitle());
		
		return sr;
	}

}
