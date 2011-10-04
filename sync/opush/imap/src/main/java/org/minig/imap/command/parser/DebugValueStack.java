package org.minig.imap.command.parser;

import org.parboiled.support.DebuggingValueStack;

public class DebugValueStack extends DebuggingValueStack<Object> {

	@Override
	public void push(Object value) {
		super.push(0, value);
	}
}