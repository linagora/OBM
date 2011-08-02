package org.obm.push.data;

import org.obm.push.backend.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.SyncCollection;
import org.w3c.dom.Element;

public interface IDataEncoder {

	void encode(BackendSession bs, Element parent, IApplicationData data, SyncCollection c, boolean isResponse);

}
