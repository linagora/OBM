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
package org.obm.push.protocol.data;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSContact;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

public class ContactEncoder {

	private final SimpleDateFormat sdf;

	@Inject
	protected ContactEncoder() {
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public void encode(Device device, Element parent, IApplicationData data) {
		// TODO Auto-generated method stub
		MSContact c = (MSContact) data;

		// DOMUtils.createElement(parent, "Contacts:CompressedRTF");

		// if (bs.getProtocolVersion() > 12) {
		// Element body = DOMUtils.createElement(parent, "AirSyncBase:Body");
		// e(body, "AirSyncBase:Type", "3");
		// e(body, "AirSyncBase:EstimatedDataSize", "5500"); // FIXME random
		// // value....
		// e(body, "AirSyncBase:Truncated", "1");
		// }

		DOMUtils.createElementAndText(parent, "Contacts:FileAs", c.getFileAs());

		e(parent, "Contacts:FirstName", c.getFirstName());
		e(parent, "Contacts:LastName", c.getLastName());
		e(parent, "Contacts:MiddleName", c.getMiddleName());
		e(parent, "Contacts:Suffix", c.getSuffix());
		e(parent, "Contacts2:NickName", c.getNickName());

		e(parent, "Contacts:JobTitle", c.getJobTitle());
		e(parent, "Contacts:Title", c.getTitle());
		e(parent, "Contacts:Department", c.getDepartment());
		e(parent, "Contacts:CompanyName", c.getCompanyName());

		e(parent, "Contacts:Spouse", c.getSpouse());
		e(parent, "Contacts:AssistantName", c.getAssistantName());
		e(parent, "Contacts2:ManagerName", c.getManagerName());
		if (c.getCategories() != null && c.getCategories().size() > 0) {
			Element cats = DOMUtils
					.createElement(parent, "Contacts:Categories");
			for (String cat : c.getCategories()) {
				e(cats, "Contacts:Category", cat);
			}
		}
		if (c.getChildren() != null && c.getChildren().size() > 0) {
			Element ec = DOMUtils.createElement(parent, "Contacts:Children");
			for (String Child : c.getCategories()) {
				e(ec, "Contacts:Category", Child);
			}
		}

		if (c.getAnniversary() != null) {
			e(parent, "Contacts:Anniversary", sdf.format(c.getAnniversary()));
		}
		if (c.getBirthday() != null) {
			e(parent, "Contacts:Birthday", sdf.format(c.getBirthday()));
		}

		e(parent, "Contacts:Webpage", c.getWebPage());

		e(parent, "Contacts:BusinessAddressStreet", c.getBusinessStreet());
		e(parent, "Contacts:BusinessAddressPostalCode", c
				.getBusinessPostalCode());
		e(parent, "Contacts:BusinessAddressCity", c.getBusinessAddressCity());
		e(parent, "Contacts:BusinessAddressCountry", c
				.getBusinessAddressCountry());
		e(parent, "Contacts:BusinessAddressState", c.getBusinessState());

		e(parent, "Contacts:HomeAddressStreet", c.getHomeAddressStreet());
		e(parent, "Contacts:HomeAddressPostalCode", c
				.getHomeAddressPostalCode());
		e(parent, "Contacts:HomeAddressCity", c.getHomeAddressCity());
		e(parent, "Contacts:HomeAddressCountry", c.getHomeAddressCountry());
		e(parent, "Contacts:HomeAddressState", c.getHomeAddressState());

		e(parent, "Contacts:OtherAddressStreet", c.getOtherAddressStreet());
		e(parent, "Contacts:OtherAddressPostalCode", c
				.getOtherAddressPostalCode());
		e(parent, "Contacts:OtherAddressCity", c.getOtherAddressCity());
		e(parent, "Contacts:OtherAddressCountry", c.getOtherAddressCountry());
		e(parent, "Contacts:OtherAddressState", c.getOtherAddressState());

		e(parent, "Contacts:HomeTelephoneNumber", c.getHomePhoneNumber());
		e(parent, "Contacts:Home2TelephoneNumber", c.getHome2PhoneNumber());
		e(parent, "Contacts:MobileTelephoneNumber", c.getMobilePhoneNumber());
		e(parent, "Contacts:BusinessTelephoneNumber", c
				.getBusinessPhoneNumber());
		e(parent, "Contacts:Business2TelephoneNumber", c
				.getBusiness2PhoneNumber());
		e(parent, "Contacts:HomeFaxNumber", c.getHomeFaxNumber());
		e(parent, "Contacts:BusinessFaxNumber", c.getBusinessFaxNumber());

		e(parent, "Contacts:PagerNumber", c.getPagerNumber());

		e(parent, "Contacts2:IMAddress", c.getIMAddress());
		e(parent, "Contacts2:IMAddress2", c.getIMAddress2());
		e(parent, "Contacts2:IMAddress3", c.getIMAddress3());

		e(parent, "Contacts:Email1Address", c.getEmail1Address());
		e(parent, "Contacts:Email2Address", c.getEmail2Address());
		e(parent, "Contacts:Email3Address", c.getEmail3Address());

		String dataBody = "";
		if(c.getData() != null){
			dataBody = c.getData().trim();
		}
		if (device.getProtocolVersion().compareTo(ProtocolVersion.V120) > 0) {
			Element body = DOMUtils.createElement(parent, "AirSyncBase:Body");
			e(body, "AirSyncBase:Type", Type.PLAIN_TEXT.toString());
			e(body, "AirSyncBase:EstimatedDataSize", ""+dataBody.length());
			if (dataBody.length() > 0) {
				// Nokia bug when the body contains only \r\n
				DOMUtils.createElementAndText(body, "AirSyncBase:Data", dataBody);
			}
			e(parent, "AirSyncBase:NativeBodyType", "3");
		} else {
			if (dataBody.length() > 0) {
				e(parent, "Contacts:BodySize", "" + dataBody.length());
				e(parent, "Contacts:Body", dataBody);
			}
		}
		// DOMUtils.createElement(parent, "Contacts:Picture");
	}

	public Element encodedApplicationData(Device device, IApplicationData data) {
		Document doc = DOMUtils.createDoc(null, null);
		Element root = doc.getDocumentElement();
		encode(device, root, data);
		return root;
	}

	private void e(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}
}
