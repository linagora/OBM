package org.obm.push.mail.imap;

import org.junit.runners.model.InitializationError;
import org.obm.filter.SlowFilterRunner;

import com.google.inject.Guice;
import com.google.inject.Module;

public class SlowGuiceRunner extends SlowFilterRunner {
	
	public SlowGuiceRunner(Class<?> klass) throws InitializationError {
        super(klass);
	}
	
	@Override
	protected Object createTest() throws Exception {
	        GuiceModule moduleAnnotation = getTestClass().getJavaClass().getAnnotation(GuiceModule.class);
	        Class<? extends Module> module = moduleAnnotation.value();
	        Object testInstance = super.createTest();
	        Guice.createInjector(module.newInstance()).injectMembers(testInstance);
	        return testInstance;
	}
}
