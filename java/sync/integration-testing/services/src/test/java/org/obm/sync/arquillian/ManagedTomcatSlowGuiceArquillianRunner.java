package org.obm.sync.arquillian;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.rules.TestRule;
import org.junit.runners.model.InitializationError;
import org.obm.filter.SlowFilterRunnerDelegation;
import org.obm.guice.GuiceRunnerDelegation;
import org.obm.sync.ManagedTomcatInstaller;

public class ManagedTomcatSlowGuiceArquillianRunner extends Arquillian {

	private SlowFilterRunnerDelegation slowRunnerDelegate;
	private GuiceRunnerDelegation guiceRunnerDelegate;

	public ManagedTomcatSlowGuiceArquillianRunner(Class<?> klass) throws InitializationError {
		super(klass);
		slowRunnerDelegate = new SlowFilterRunnerDelegation();
		guiceRunnerDelegate = new GuiceRunnerDelegation();
		ManagedTomcatInstaller.install();
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		return slowRunnerDelegate.getTestRules(super.getTestRules(target));
	}
	
	@Override
	protected List<TestRule> classRules() {
		return slowRunnerDelegate.classRules(super.classRules());
	}

	@Override
	protected Object createTest() throws Exception {
		return guiceRunnerDelegate.createTest(getTestClass(), super.createTest());
	}
}
