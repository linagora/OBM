package fr.aliasource.obm.items.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps funis labels to OBM entity labels
 * 
 * @author tom
 *
 */
public class LabelMapping {

	private Map<String, String> clientToObm;
	private Map<String, String> obmToClient;

	public LabelMapping() {
		clientToObm = new HashMap<String, String>();
		obmToClient = new HashMap<String, String>();

		addMapping("MobileTelephoneNumber", "CELL;VOICE;X-OBM-Ref1"); // mobiletel
		addMapping("Mobile2TelephoneNumber", "CELL;VOICE;X-OBM-Ref2"); // radiotel
		addMapping("Mobile3TelephoneNumber", "CELL;CAR;VOICE;X-OBM-Ref1"); // cartel
		addMapping("PagerNumber", "PAGER;X-OBM-Ref1"); // pager
		addMapping("BusinessTelephoneNumber", "WORK;VOICE;X-OBM-Ref1"); // worktel
		addMapping("Business2TelephoneNumber", "WORK;VOICE;X-OBM-Ref2"); // work2tel
		addMapping("Business3TelephoneNumber", "WORK;VOICE;X-OBM-Ref3"); // assistanttel
		addMapping("BusinessFaxNumber", "WORK;FAX;X-OBM-Ref1"); // workfax
		addMapping("HomeTelephoneNumber", "HOME;VOICE;X-OBM-Ref1"); // homephone
		addMapping("OtherTelephoneNumber", "OTHER;VOICE;X-OBM-Ref1"); // telnokia
		addMapping("HomeFaxNumber", "HOME;FAX;X-OBM-Ref1"); // homefax
		addMapping("Email1Address", "INTERNET;X-OBM-Ref1"); // email
		addMapping("OtherEmail2Address", "INTERNET;X-OBM-Ref2"); // email2
		addMapping("OtherEmail3Address", "INTERNET;X-OBM-Ref3"); // email3
		addMapping("WebPage", "URL;X-OBM-Ref1"); // webpage
		addMapping("work", "WORK;X-OBM-Ref1"); // workaddr
		addMapping("home", "HOME;X-OBM-Ref1"); // homeaddr
		addMapping("other", "OTHER;X-OBM-Ref1"); // otheraddr
	}

	private void addMapping(String client, String obm) {
		clientToObm.put(client, obm);
		obmToClient.put(obm, client);
	}
	
	public String toOBM(String clientLabel) {
		if (clientToObm.containsKey(clientLabel)) {
			return clientToObm.get(clientLabel);
		} else {
			return clientLabel;
		}
	}

	public String toFunis(String obmLabel) {
		String lbl = obmLabel.replace("PREF;", "");
		if (obmToClient.containsKey(lbl)) {
			return obmToClient.get(lbl);
		} else {
			return obmLabel;
		}
	}
}
