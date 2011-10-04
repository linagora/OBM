/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookItemsWriter;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsWriter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChangesResponse;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.FolderChangesResponse;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.mailingList.MailingListItemsWriter;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.SettingItemsWriter;
import org.obm.sync.setting.VacationSettings;
import org.obm.sync.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class XmlResponder {

	private HttpServletResponse resp;
	private Logger logger =  LoggerFactory.getLogger(getClass());
	private CalendarItemsWriter ciw;
	private BookItemsWriter biw;
	private SettingItemsWriter siw;
	private MailingListItemsWriter mliw;

	public XmlResponder(HttpServletResponse resp) {
		this.resp = resp;
		this.ciw = new CalendarItemsWriter();
		this.biw = new BookItemsWriter();
		this.siw = new SettingItemsWriter();
		this.mliw = new MailingListItemsWriter();
	}

	private String sendError(String message, String type) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/error.xsd", "error");
			Element root = doc.getDocumentElement();
			DOMUtils.createElementAndText(root, "message", message);
			if(!Strings.isNullOrEmpty(type)){
				DOMUtils.createElementAndText(root, "type", type);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}
	
	public String sendError(String message) {
		return sendError(Strings.nullToEmpty(message), null);
	}

	public String sendError(Exception e) {
		return sendError(Strings.nullToEmpty(e.getMessage()), e.getClass().getName());
	}

	public String sendToken(AccessToken at) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/token.xsd", "token");
			Element root = doc.getDocumentElement();
			DOMUtils.createElementAndText(root, "sid", at.getSessionId());
			Element v = DOMUtils.createElement(root, "version");
			MavenVersion version = at.getVersion();
			v.setAttribute("major", version.getMajor());
			v.setAttribute("minor", version.getMinor());
			v.setAttribute("release", version.getRelease());
			DOMUtils.createElementAndText(root, "domain", at.getDomain());
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	private String sendDom(Document doc) {
		String res = "";
		try {
			resp.setContentType("text/xml;charset=UTF-8");
			DOMUtils.serialise(doc, resp.getOutputStream());

			DOMUtils.logDom(doc);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtils.serialise(doc, out);
			res = out.toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return res;
	}

	public String sendBoolean(boolean value) {
		return sendString(String.valueOf(value));
	}

	public String sendInt(int value) {
		return sendString(String.valueOf(value));
	}

	public String sendLong(long value) {
		return sendString(String.valueOf(value));
	}

	public String sendString(String value) {
		return sendArrayOfString(value);
	}

	public String sendArrayOfString(String... ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/string.xsd", "string");
			Element root = doc.getDocumentElement();
			for (String value : ret) {
				DOMUtils.createElementAndText(root, "value", value);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendKeyList(KeyList ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/keylist.xsd", "keylist");
			Element root = doc.getDocumentElement();
			for (String key : ret.getKeys()) {
				DOMUtils.createElementAndText(root, "key", key);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendEvent(Event ev) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/event.xsd", "event");
			Element root = doc.getDocumentElement();
			ciw.appendEvent(root, ev);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendCalendarChanges(EventChanges cc) {
		return sendDom(ciw.writeChanges(cc));
	}

	public String sendCalendarInformations(CalendarInfo[] lc) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/calendarinfos.xsd",
			"calendar-infos");
			Element root = doc.getDocumentElement();
			for (CalendarInfo ci : lc) {
				ciw.appendInfo(root, ci);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendContact(Contact contact) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact.xsd", "contact");
			Element root = doc.getDocumentElement();
			biw.appendContact(root, contact);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendContactChanges(ContactChangesResponse cc) {
		return sendDom(biw.writeChanges(cc));
	}

	public String sendCategories(List<Category> ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/categories.xsd", "categories");
			Element root = doc.getDocumentElement();
			for (Category c : ret) {
				ciw.appendCategory(root, c);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListAddressBooks(List<AddressBook> ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/books.xsd", "books");
			Element root = doc.getDocumentElement();
			for (AddressBook book: ret) {
				biw.appendAddressBook(root, book);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListEvent(List<Event> evs) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/events.xsd", "events");
			Element root = doc.getDocumentElement();
			for (Event ev : evs) {
				ciw.appendEvent(root, ev);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListEventTimeUpdate(List<EventTimeUpdate> evs) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/events.xsd",
			"eventTimeUpdates");
			Element root = doc.getDocumentElement();
			for (EventTimeUpdate etu : evs) {
				ciw.appendEventTimeUpdate(root, etu);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListContact(List<Contact> ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact.xsd", "contacts");
			Element root = doc.getDocumentElement();
			for (Contact contact : ret) {
				biw.appendContact(root, contact);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendSettings(Map<String, String> ret) {
		String res = "";
		Document doc;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/settings.xsd", "contacts");
			Element root = doc.getDocumentElement();
			for (Entry<String, String> entry : ret.entrySet()) {
				siw.appendSetting(root, entry.getKey(), entry.getValue());
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListEventParticipationState(List<EventParticipationState> e) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/events.xsd",
			"eventParticipationStates");
			Element root = doc.getDocumentElement();
			for (EventParticipationState etu : e) {
				ciw.appendEventParticipationState(root, etu);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendFolderChanges(FolderChangesResponse fc) {
		return sendDom(biw.writeFolderChanges(fc));
	}

	public String sendFolder(Folder ret) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact.xsd", "folder");
			Element root = doc.getDocumentElement();
			biw.appendFolder(root, ret);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendFreeBusyRequest(FreeBusyRequest freeBusy) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusyRequest.xsd",
			"freeBusyRequest");
			Element root = doc.getDocumentElement();
			ciw.appendFreeBusyRequest(root, freeBusy);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListFreeBusy(List<FreeBusy> freeBusys) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusys.xsd", "freeBusys");
			Element root = doc.getDocumentElement();
			for (FreeBusy fb : freeBusys) {
				Element e = DOMUtils.createElement(root, "freebusy");
				ciw.appendFreeBusy(e, fb);
			}
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendVacation(VacationSettings vs) {
		return sendDom(siw.getVacationDOM(vs));
	}

	public String sendEmailForwarding(ForwardingSettings fs) {
		return sendDom(siw.getForwardingDOM(fs));
	}

	public String sendMailingList(MailingList ml) {
		String res = "";
		try {
			Document doc = mliw.getMailingListsAsXML(ml);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListMailingLists(List<MailingList> ret) {
		String res = "";
		try {
			Document doc = mliw.getMailingListsAsXML(ret.toArray(new MailingList[0]));
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListMailingListEmails(List<MLEmail> ret) {
		String res = "";
		try {
			Document doc = mliw.getMailingListEmailsAsXML(ret);
			res = sendDom(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public void sendAddressBookChanges(AddressBookChangesResponse response) {
		sendDom(biw.writeAddressBookChanges(response));
	}

}
