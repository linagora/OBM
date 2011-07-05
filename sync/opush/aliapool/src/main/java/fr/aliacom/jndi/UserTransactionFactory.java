package fr.aliacom.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.aliapool.tm.TransactionManager;

public class UserTransactionFactory implements ObjectFactory {

	private Log logger;

	public UserTransactionFactory() {
		logger = LogFactory.getLog(getClass());
		logger.info("UserTransactionFactory created");
	}

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {
		return TransactionManager.getInstance();
	}

}
