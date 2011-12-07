package org.obm.opush.env;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class JUnitGuiceRule implements MethodRule {

	private Injector injector;

	public JUnitGuiceRule(Class<? extends Module> moduleClass) {
		Injector metaInjector = Guice.createInjector();
		Module module = metaInjector.getInstance(moduleClass);
		injector = Guice.createInjector(Stage.DEVELOPMENT, module);
	}
		
	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				injector.injectMembers(target);
				base.evaluate();
			}
		};
	}
}
