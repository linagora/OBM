package fr.aliasource.funambol.utils;

import java.util.List;

import com.funambol.common.pim.contact.Address;
import com.funambol.common.pim.contact.BusinessDetail;
import com.funambol.common.pim.contact.ContactDetail;
import com.funambol.common.pim.contact.Email;
import com.funambol.common.pim.contact.Note;
import com.funambol.common.pim.contact.Phone;
import com.funambol.common.pim.contact.Title;

import fr.aliacom.obm.wcontact.Contact;

public class ContactHelper extends Helper {

	public static final String WORK_PHONE	= "BusinessTelephoneNumber";
	public static final String WORK_FAX		= "BusinessFaxNumber";
	public static final String WORK_EMAIL	= "Email1Address";
	public static final String WORK_TITLE	= "JobTitle";
	public static final String HOME_PHONE	= "HomeTelephoneNumber";
	public static final String HOME_FAX		= "HomeFaxNumber";
	public static final String HOME_MOBILE	= "MobileTelephoneNumber";
	public static final String OTHER_PHONE	= "OtherTelephoneNumber";
	public static final String COMMENT		= "Body";
	public static final String HOME_EMAIL 	= "Email2Address";
	
	public static void setFoundationPhone(ContactDetail detail, String phone, String type) {
		
		if ( ! nullToEmptyString(phone).equals("") ) {
			Phone ph = new Phone();
		
			ph.setPhoneType(type);
			ph.setPropertyValue(phone);
			
			detail.addPhone(ph);
		}
	}
	
	public static String getPhone(List phones, String type) {
		String result = "";
	
		if (phones != null) {
			for (int i=0 ; i < phones.size() ; i++ ) {
				if ( ((Phone)phones.get(i)).getPhoneType().equalsIgnoreCase(type) ) {
					result = ((Phone)phones.get(i)).getPropertyValueAsString() ;
					break;
				}	
			}
		}
		
		return result;
	}

	public static void setFoundationTitle(BusinessDetail bus, String title, String type) {
		if ( ! nullToEmptyString(title).equals("") ) {
			Title t = new Title();
			
			t.setTitleType(type);
			t.setPropertyValue(title);
			
			bus.addTitle(t);
		}
	}
	
	public static String getTitle(List titles, String type) {
		String result = "";

		if (titles != null) {
			for (int i=0 ; i < titles.size() ; i++ ) {
				if ( ((Title)titles.get(i)).getTitleType().equalsIgnoreCase(type) ) {
					result = ((Title)titles.get(i)).getPropertyValueAsString() ;
					break;
				}	
			}
		}
		
		return result;
	}
	
	
	public static void setFoundationEmail(ContactDetail detail, String email, String type) {
		
		if ( !nullToEmptyString(email).equals("") ) {
			Email em = new Email();
			
			em.setEmailType(type);
			em.setPropertyValue(email);
			
			detail.addEmail(em);
		}
	}
	
	public static String getEmail(List emails, String type) {
		String result = "";
		
		if (emails != null) {
			for (int i=0 ; i < emails.size() ; i++ ) {
				if ( ((Email)emails.get(i)).getEmailType().equalsIgnoreCase(type) ) {
					result = ((Email)emails.get(i)).getPropertyValueAsString() ;
					break;
				}	
			}
		}
			
		return result;
	}
	
	public static String getStreetFromObm(Contact obmcontact) {
		String result = "";
    	result = nullToEmptyString(obmcontact.getAddress1());
    	if ( ! nullToEmptyString(obmcontact.getAddress2()).equals("") ) {
    		result += "\n" + nullToEmptyString(obmcontact.getAddress2());
    	}
    	if ( ! ContactHelper.nullToEmptyString(obmcontact.getAddress3()).equals("") ) {
    		result += "\n" + nullToEmptyString(obmcontact.getAddress3());
    	}
    	
		return result;
	}
	
	public static void constructObmStreet(Contact obmcontact, String street) {
		
		street = street.replaceAll("\r","");
		String[] addr = street.split("\n");
		
		if (addr.length > 0) {
			obmcontact.setAddress1(addr[0]);
		}
		if (addr.length > 1) {
			obmcontact.setAddress2(addr[1]);
		}
		
		if (addr.length > 2){
			String addr3 = "";
			for (int i = 2 ; i < addr.length ; i++ ) {
				if (i > 2) {
					addr3 += " ";
				}
				addr3 += addr[i];
			}
			obmcontact.setAddress3(addr3);
		}
	}
	
	public static void setFoundationNote(com.funambol.common.pim.contact.Contact foundation, String note, String type) {
		
		if ( !nullToEmptyString(note).equals("") ) {
			Note nt = new Note();
			
			nt.setNoteType(type);
			nt.setPropertyValue(note);
			
			foundation.addNote(nt);
		}	
	}
	
	public static String getNote(List notes, String type) {
		String result = "";
		
		if (notes != null) {
			for (int i=0 ; i < notes.size() ; i++ ) {
				if ( ((Note)notes.get(i)).getNoteType().equalsIgnoreCase(type) ) {
					result = ((Note)notes.get(i)).getPropertyValueAsString() ;
					break;
				}	
			}
		}
		return result;
	}
	
	public static String getCountry(Address addr) {
		return nullToEmptyString(addr.getCountry().getPropertyValueAsString());
	}

	public static String getLastName(
			com.funambol.common.pim.contact.Contact foundation) {
		String result = nullToEmptyString(foundation.getName().getLastName()
				.getPropertyValueAsString());
		if (result.equalsIgnoreCase("")) {
			result = "-";
		}
		return result;
	}

	public static String constructDisplayName(String firstName, String lastName) {
		
		if (firstName == null) {
			return lastName;
		} else {
			if (firstName.equals("")) {
				return lastName;
			} else {
				return firstName + "," + lastName;
			}
		}
	}
}
	
