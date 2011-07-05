package org.obm.push.provisioning;

import org.w3c.dom.Element;

/**
 * Policy type used in EAS protocol 2.5
 * 
 * http://social.msdn.microsoft.com/Forums/en-US/os_exchangeprotocols/thread/243320fa-89cb-4af0-934d-438aae5a8277
 * 
 * @author tom
 *
 */
public class MSWAPProvisioningXML extends Policy {

	@Override
	public void serialize(Element data) {
		// copied from exchange 2007 protocol 2.5 response
		data
				.setTextContent("<wap-provisioningdoc>"
						+ "<characteristic "
						+ "type=\"SecurityPolicy\"><parm name=\"4131\" value=\"1\"/><parm name=\"4133\" value=\"1\"/>"
						+ "</characteristic></wap-provisioningdoc>");
	}

}
