package org.obm.opush;

import org.obm.opush.env.AbstractOpushEnv;

import com.google.inject.Module;

public class PingHandlerTestModule extends AbstractOpushEnv {
	
	public PingHandlerTestModule() {
		super();
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		Module overrideModule = super.overrideModule();
		return overrideModule;
	}
}