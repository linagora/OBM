package org.obm.push.store;

import java.sql.SQLException;

import org.obm.push.bean.Device;

public interface HearbeatDao {

	long findLastHearbeat(Device device) throws SQLException;

	void updateLastHearbeat(Device device, long hearbeat) throws SQLException;

}
