/*
 * Created on Nov 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.aliasource.obm.aliapool.pool;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tom
 *
 */
abstract class AbstractStatementProxy {
	
	private Set<ResultSet> results;
	
	protected AbstractStatementProxy() {
		results = new HashSet<ResultSet>();
	}
	
	protected void addResult(ResultSet rs) {
		results.add(rs);
	}
	
	protected void closeResult(ResultSet rs) {
		results.remove(rs);
	}
	
	public void checkOpenResults() {
		if (!results.isEmpty()) {
			new Throwable("Some results are still open").printStackTrace();
		}
	}

}
