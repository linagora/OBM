package org.obm.push.data;

import org.obm.push.backend.MSEmail;
import org.obm.push.store.IApplicationData;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author adrienp
 * 
 */
public class EmailDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		MSEmail mail = new MSEmail();
		mail.setRead(parseDOMInt2Boolean(DOMUtils.getUniqueElement(syncData,
				"Read")));

		return mail;
	}
}
