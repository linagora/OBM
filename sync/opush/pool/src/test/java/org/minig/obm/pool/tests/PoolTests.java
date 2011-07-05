package org.minig.obm.pool.tests;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.minig.obm.pool.OBMPoolActivator;

public class PoolTests extends TestCase {

	public void testCreatePool() {
		OBMPoolActivator opa = OBMPoolActivator.getInstance();
		assertNotNull(opa);
	}
	
	public void testSQL() throws SQLException {
		OBMPoolActivator opa = OBMPoolActivator.getInstance();
		assertNotNull(opa);
		Connection con = opa.getConnection();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("SELECT 1");
		rs.next();
		int ret = rs.getInt(1);
		assertTrue(ret == 1);
		rs.close();
		st.close();
		con.close();
	}
	
}
