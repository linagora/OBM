/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.ServerCapability;
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
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.mailingList.MailingListItemsWriter;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.SettingItemsWriter;
import org.obm.sync.setting.VacationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.UserSettings;

public class XmlResponder {

	private final HttpServletResponse resp;
	private final Logger logger =  LoggerFactory.getLogger(getClass());
	private final CalendarItemsWriter ciw;
	private final BookItemsWriter biw;
	private final SettingItemsWriter siw;
	private final MailingListItemsWriter mliw;

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
			res = emitResponse(doc);
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
		try {
			return emitResponse(toXML(at));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return "";
	}

	public Document toXML(AccessToken at) throws FactoryConfigurationError {
		return prepareAccessTokenXML(at.getSessionId(), at.getUserEmail(), at.getUserDisplayName(), at.getVersion(), at.getDomain(), at.getUserSettings(), at.getServerCapabilities());
	}

	public Document prepareAccessTokenXML(String sessionId, String userEmail, String displayName,
			MavenVersion version, ObmDomain tokenDomain, UserSettings userSettings, Map<ServerCapability, String> serverCapabilities)
			throws FactoryConfigurationError {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/token.xsd", "token");
		Element root = doc.getDocumentElement();
		DOMUtils.createElementAndText(root, "sid", sessionId);
		Element v = DOMUtils.createElement(root, "version");
		v.setAttribute("major", version.getMajor());
		v.setAttribute("minor", version.getMinor());
		v.setAttribute("release", version.getRelease());

		DOMUtils.createElementAndText(root, "email", userEmail);
		if (!Strings.isNullOrEmpty(displayName)) {
			DOMUtils.createElementAndText(root, "displayname", displayName);
		}

		Element domain = DOMUtils.createElementAndText(root, "domain", tokenDomain.getName());
		domain.setAttribute("uuid", tokenDomain.getUuid().get());

		appendUserSettings(root, userSettings);
		appendServerCapabilities(root, serverCapabilities);

		return doc;
	}
	
	private void appendUserSettings(Element root, UserSettings userSettings) {
		Element settingsElement = DOMUtils.createElement(root, "settings");

		if (userSettings != null) {
			Map<String, String> rawSettings = userSettings.getRawSettings();

			if (rawSettings != null) {
				for (Entry<String, String> setting : rawSettings.entrySet()) {
					Element settingElement = DOMUtils.createElementAndText(settingsElement, "setting", setting.getValue());

					settingElement.setAttribute("name", setting.getKey());
				}
			}
		}
	}
	
	private void appendServerCapabilities(Element root, Map<ServerCapability, String> serverCapabilities) {
		Element settingsElement = DOMUtils.createElement(root, "server-capabilities");

		if (serverCapabilities != null) {
			for (Entry<ServerCapability, String> capability : serverCapabilities.entrySet()) {
				Element capabilityElement = DOMUtils.createElementAndText(settingsElement, "server-capability", capability.getValue());

				capabilityElement.setAttribute("name", capability.getKey().name());
			}
		}
	}

	public String emitResponse(Document doc) {
		String res = "";
		try {
			resp.setContentType("text/xml;charset=UTF-8");
			DOMUtils.serialize(doc, resp.getOutputStream());

			DOMUtils.logDom(doc);
			res = DOMUtils.serialize(doc);
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
			res = emitResponse(doc);
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
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendEvent(Event event) {
		return emitResponse(ciw.getXMLDocumentFrom(event));
	}

	public String sendCalendarChanges(EventChanges eventChanges) {
		return emitResponse(ciw.getXMLDocumentFrom(eventChanges));
	}

	public String sendCalendarInformations(Collection<CalendarInfo> lc) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/calendarinfos.xsd",
			"calendar-infos");
			Element root = doc.getDocumentElement();
			for (CalendarInfo ci : lc) {
				ciw.appendInfo(root, ci);
			}
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendResourceInformation(Collection<ResourceInfo> resourceInfo) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/resourceinfo.xsd",
			"resourceInfoGroup");
			Element root = doc.getDocumentElement();
			for (ResourceInfo ri : resourceInfo) {
				ciw.appendResourceInfo(root, ri);
			}
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendContact(Contact contact) {
		return emitResponse(biw.getXMLDocumentFrom(contact));
	}

	public String sendContactChanges(ContactChanges contactChanges) {
		return emitResponse(biw.getXMLDocumentFrom(contactChanges));
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
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListAddressBooks(List<AddressBook> addressbooks) {
		return emitResponse(biw.getXMLDocumentFromAddressBooks(addressbooks));
	}

	public String sendListEvent(List<Event> events) {
		return emitResponse(ciw.getXMLDocumentFrom(events));
	}

	public String sendListContact(List<Contact> contacts) {
		return emitResponse(biw.getXMLDocumentFromContacts(contacts));
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
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendFolder(Folder folder) {
		return emitResponse(biw.getXMLDocumentFrom(folder));
	}

	public String sendFreeBusyRequest(FreeBusyRequest freeBusy) {
		String res = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusyRequest.xsd",
			"freeBusyRequest");
			Element root = doc.getDocumentElement();
			ciw.appendFreeBusyRequest(root, freeBusy);
			res = emitResponse(doc);
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
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendVacation(VacationSettings vs) {
		return emitResponse(siw.getVacationDOM(vs));
	}

	public String sendEmailForwarding(ForwardingSettings fs) {
		return emitResponse(siw.getForwardingDOM(fs));
	}

	public String sendMailingList(MailingList ml) {
		String res = "";
		try {
			Document doc = mliw.getMailingListsAsXML(ml);
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListMailingLists(List<MailingList> ret) {
		String res = "";
		try {
			Document doc = mliw.getMailingListsAsXML(ret.toArray(new MailingList[0]));
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public String sendListMailingListEmails(List<MLEmail> ret) {
		String res = "";
		try {
			Document doc = mliw.getMailingListEmailsAsXML(ret);
			res = emitResponse(doc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	public void sendAddressBookChanges(AddressBookChangesResponse response) {
		emitResponse(biw.getXMLDocumentFrom(response));
	}

	public void sendlistAddressBooksChanged(FolderChanges folderChanges) {
		emitResponse(biw.getXMLDocumentFrom(folderChanges));
	}

	public String sendCountContacts(int count) {
		return emitResponse(biw.getXMLDocumentFrom(count));
	}

}
