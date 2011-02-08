package org.obm.sync.items;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Serializes calendar related items to XML
 * 
 * @author tom
 * 
 */
public abstract class AbstractItemsWriter {

	protected Log logger = LogFactory.getLog(getClass());

	protected void createIfNotNull(Element e, String nodeName, String value) {
		if (value != null && value.length() > 0) {
			DOMUtils.createElementAndText(e, nodeName, value);
		}
	}

}
