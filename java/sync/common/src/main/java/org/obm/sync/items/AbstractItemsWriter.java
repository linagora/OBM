package org.obm.sync.items;

import org.obm.sync.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Serializes calendar related items to XML
 * 
 * @author tom
 * 
 */
public abstract class AbstractItemsWriter {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected void createIfNotNull(Element e, String nodeName, String value) {
		if (value != null && value.length() > 0) {
			DOMUtils.createElementAndText(e, nodeName, value);
		}
	}

}
