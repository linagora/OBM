package org.obm.opush.command.sync;

import org.obm.opush.ModuleUtils;
import org.obm.opush.env.AbstractOpushEnv;
import org.obm.opush.env.AbstractOverrideModule;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class SyncHandlerTestModule  extends AbstractOpushEnv {
	
	public SyncHandlerTestModule() {
		super();
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		Module overrideModule = super.overrideModule();
		
		Module contentsExporterBackend = bindContentsExporterBackendModule();
		
		return Modules.combine(overrideModule, contentsExporterBackend);
	}

	private Module bindContentsExporterBackendModule() {
		AbstractOverrideModule contentsExporterBackend = ModuleUtils.buildContentsExporterBackendModule();
		getMockMap().addMap(contentsExporterBackend.getMockMap());
		return contentsExporterBackend;
	}
}
