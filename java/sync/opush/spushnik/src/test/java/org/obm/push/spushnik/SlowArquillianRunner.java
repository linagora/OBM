package org.obm.push.spushnik;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;
import org.obm.filter.SlowFilterRunnerDelegation;

public class SlowArquillianRunner extends Arquillian {

	private SlowFilterRunnerDelegation delegate;

	public SlowArquillianRunner(Class<?> klass) throws InitializationError {
		super(klass);
		delegate = new SlowFilterRunnerDelegation();
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		return delegate.getTestRules(super.getTestRules(target));
	}
	
	@Override
	protected List<TestRule> classRules() {
		return delegate.classRules(super.classRules());
	}

}
