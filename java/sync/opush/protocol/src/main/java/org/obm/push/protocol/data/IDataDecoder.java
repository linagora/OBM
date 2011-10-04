package org.obm.push.protocol.data;

import org.obm.push.bean.IApplicationData;
import org.w3c.dom.Element;

public interface IDataDecoder {

	IApplicationData decode(Element syncData);

}
