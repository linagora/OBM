package org.obm.sync.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

/**
 * Removes by uniqueID in a Solr index.
 */
public class Remover implements Runnable {

	private static final Log logger = LogFactory.getLog(Remover.class);

	private CommonsHttpSolrServer srv;
	private String id;

	public Remover(CommonsHttpSolrServer srv, String id) {
		this.srv = srv;
		this.id = id;
	}

	@Override
	public void run() {
		try {
			srv.deleteById(id);
			srv.commit();
			logger.info("id " + id + " removed from SOLR index");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
