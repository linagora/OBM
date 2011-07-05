package org.minig.imap.idle;

public interface IIdleCallback {
	void receive(IdleLine line);
	void disconnectedCallBack();
}
