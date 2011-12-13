package org.obm.opush;

import org.obm.opush.env.AbstractOpushEnv;
import org.obm.opush.env.AbstractOverrideModule;
import org.obm.push.backend.IContentsExporter;

import com.google.inject.Module;
import com.google.inject.util.Modules;

public class PingHandlerTestModule extends AbstractOpushEnv {
	
	public PingHandlerTestModule() {
		super();
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		Module overrideModule = super.overrideModule();
		
		AbstractOverrideModule contentsExporterBackend = bindContentsExporterBackend();
		
		return Modules.combine(overrideModule, contentsExporterBackend);
	}

	private AbstractOverrideModule bindContentsExporterBackend() {
		AbstractOverrideModule contentsExporterBackend = new AbstractOverrideModule() {

			@Override
			protected void configureImpl() {
				bindWithMock(IContentsExporter.class);
			}
		};
		getMockMap().addMap(contentsExporterBackend.getMockMap());
		return contentsExporterBackend;
	}
}