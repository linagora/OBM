package org.minig.imap.tls;

import javax.net.ssl.SSLContext;

import org.apache.mina.filter.SSLFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinigTLSFilter extends SSLFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(MinigTLSFilter.class);
	
	private static SSLContext CTX;

	static {
		try {
			CTX = BogusSSLContextFactory.getInstance(false);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	public MinigTLSFilter() {
		super(CTX);
	}

}
