package org.obm.annotations.transactional;

import javax.transaction.TransactionManager;

import org.obm.annotations.transactional.TransactionProvider;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class TransactionalModule extends AbstractModule{

	@Override
	protected void configure() {

		bind(TransactionManager.class).toProvider(TransactionProvider.class);

		TransactionalInterceptor transactionalInterceptor = new TransactionalInterceptor();
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), 
				transactionalInterceptor);
		requestInjection(transactionalInterceptor);
	}

}
