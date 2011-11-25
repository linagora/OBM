package org.obm.push.protocol.data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.inject.Inject;

public class ContactEncoder implements IDataEncoder {

	private SimpleDateFormat sdf;

	@Inject
	private ContactEncoder() {
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Override
	public void encode(BackendSession bs, Element parent,
			IApplicationData data, SyncCollection collectio, boolean isResponse) {
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

		DOMUtils.createElementAndText(parent, "Contacts:FileAs", getFileAs(c));

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
		if (bs.getProtocolVersion().compareTo(BigDecimal.valueOf(12)) > 0) {
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

	private String getFileAs(MSContact c) {
		if (c.getFirstName() != null && c.getLastName() != null
				&& c.getFirstName().length() > 0) {
			return c.getLastName() + ", " + c.getFirstName();
		} else if (c.getFirstName() != null && c.getFirstName().length() > 0) {
			return c.getFirstName();
		} else {
			return c.getLastName();
		}
	}

	private void e(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}

}
