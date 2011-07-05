package org.obm.push.data;

import org.obm.push.store.IApplicationData;
import org.w3c.dom.Element;

public interface IDataDecoder {

	IApplicationData decode(Element syncData);

}
