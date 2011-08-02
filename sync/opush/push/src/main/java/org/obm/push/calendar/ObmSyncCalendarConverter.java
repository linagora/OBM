package org.obm.push.calendar;

import org.obm.push.backend.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.sync.calendar.Event;

public interface ObmSyncCalendarConverter {
	
	Event convertAsInternal(BackendSession bs, Event oldEvent, IApplicationData data);
	Event convertAsExternal(BackendSession bs, Event oldEvent, IApplicationData data);
	
	Event convertAsInternal(BackendSession bs, IApplicationData appliData);
	Event convertAsExternal(BackendSession bs, IApplicationData appliData);
	
	IApplicationData convert(BackendSession bs, Event event);
}
