package org.obm.push.protocol.data;

import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmail;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class EmailDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		MSEmail mail = new MSEmail();
		mail.setRead(parseDOMInt2Boolean(DOMUtils.getUniqueElement(syncData,
				"Read")));

		return mail;
	}
}
