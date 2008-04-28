package fr.aliasource.obm.autoconf.impl;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class MemoryContextFactory implements InitialContextFactory {
	private static MemoryContext ic = new MemoryContext("java:",
			new HashMap<String, Object>());

	/**
	 * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
	 */
	public Context getInitialContext(Hashtable<?,?> environment)
			throws NamingException {
		return ic;
	}

}
