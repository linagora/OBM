package org.obm.push.store;


import org.obm.push.bean.Device;
import org.obm.push.exception.DaoException;

public interface HearbeatDao {

	long findLastHearbeat(Device device) throws DaoException;

	void updateLastHearbeat(Device device, long hearbeat) throws DaoException;

}
