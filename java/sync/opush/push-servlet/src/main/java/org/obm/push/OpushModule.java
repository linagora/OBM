package org.obm.push;
import org.obm.push.OpushImplModule;
import org.obm.push.mail.OpushMailModule;

import com.google.inject.AbstractModule;

public class OpushModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new OpushImplModule());
		install(new OpushMailModule());
	}
}
