package org.minig.imap.command.parser;

import java.util.Collections;
import java.util.List;

import org.minig.imap.NameSpaceInfo;
import org.minig.imap.impl.MailboxNameUTF7Converter;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

import com.google.common.collect.Lists;

@BuildParseTree
public class NamespaceParser extends AbstractImapBaseParser {
	
	public static final String expectedResponseStart = "* NAMESPACE";
	
	public Rule rule() {
		return Sequence(namespaceCommand(), 
				namespace(), ACTION(setPersonalNamespaces()), 
				whitespaces(),
				namespace(), ACTION(setOtherUserNamespaces()),
				whitespaces(),
				namespace(), ACTION(setSharedFolderNamespaces()));
	}
	
	boolean setPersonalNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setPersonal((List<String>) pop());
		push(nsi);
		return true;
	}

	boolean setOtherUserNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setOtherUsers((List<String>) pop());
		push(nsi);
		return true;
	}

	boolean setSharedFolderNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setMailShares((List<String>) pop());
		push(nsi);
		return true;
	}

	
	Rule namespace() {
		return FirstOf(
				Sequence(nilNoStack(), push(Collections.emptyList())),
				group());
	}
	
	Rule group() {
		return Sequence('(', 
				Sequence(push(Lists.newArrayList()),
						OneOrMore(entry())),
				')');
	}

	boolean addEntryToList() {
		String expression = (java.lang.String)pop();
		List<String> list = (List<String>)peek();
		list.add(MailboxNameUTF7Converter.decode(expression));
		return true;
	}
	
	Rule entry() {
		return Sequence('(', 
				string(), ACTION(addEntryToList()),
				whitespaces(), 
				stringNoStack(),
				ZeroOrMore(extension()),
				')',
				whitespaces());
	}
	
	Rule extension() {
		return Sequence(whitespaces(), 
				stringNoStack(),
				whitespaces(),
				'(', stringNoStack(),
				ZeroOrMore(whitespaces(), stringNoStack()),
				')'
			);
	}
	
	Rule namespaceCommand() {
		return Sequence(String(expectedResponseStart), whitespaces(), push(new NameSpaceInfo())); 
	}
}