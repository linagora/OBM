package org.obm.push.store.jdbc;

import org.obm.dbcp.IDBCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractJdbcImpl {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final IDBCP dbcp;

	protected AbstractJdbcImpl (IDBCP dbcp) {
		this.dbcp = dbcp;
	}
}
