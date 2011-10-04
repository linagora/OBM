package org.obm.sync.solr;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes by uniqueID in a Solr index.
 */
public class Remover implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Remover.class);

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
