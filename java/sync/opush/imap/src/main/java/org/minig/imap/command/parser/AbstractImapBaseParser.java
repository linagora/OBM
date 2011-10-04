package org.minig.imap.command.parser;

import java.nio.charset.Charset;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.SkipNode;
import org.parboiled.common.Reference;

public class AbstractImapBaseParser extends BaseParser<Object> {

	/**
	 * parse a printable character but don't push anything on the stack
	 */
	@SkipNode
	Rule quotedCharacter() {
		//exclude 0x0A (CR), 0x0D (LF), 0x22 (dquote)
		return FirstOf(CharRange((char)0x01, (char)0x09),
				CharRange((char)0x0B, (char)0x0C),
				CharRange((char)0x0E, (char)0x21),
				CharRange((char)0x23, (char)0x7F));
	}
	
	/**
	 * parse a string of printable character and push it on the stack
	 */
	Rule stringContent() {
		return Sequence(ZeroOrMore(quotedCharacter()), push(match()));
	}
	
	/**
	 * parse NIL and push a null object on the stack
	 */
	Rule nil() {
		return Sequence(String("NIL"), push(null));
	}

	/**
	 * parse NIL
	 */
	Rule nilNoStack() {
		return String("NIL");
	}
	
	/**
	 * parse a space character, don't touch the stack
	 */
	Rule whitespaces() {
		return OneOrMore(' ');
	}

	/**
	 * parse a double-quoted string and push its content without quotes on the stack
	 */
	Rule string() {
		return FirstOf(Sequence('"', stringContent(), '"'),
				literal());
	}
	
	Rule literal() {
		Reference<Integer> count = new Reference<Integer>();
		return Sequence('{', number(), assignCount(count), '}', literalStringPart(count));
	}

	boolean assignCount(Reference<Integer> count) {
		return count.set((Integer) pop());
	}
	
	Rule literalStringPart(Reference<Integer> count) {
		return Sequence(
				ZeroOrMore(count.get() > 0, ANY, count.set(count.get() - match().getBytes(Charset.forName("ASCII")).length)),
				count.get() == 0,
				push(match())
		); 
	}

	/**
	 * parse a double-quoted string
	 */
	Rule stringNoStack() {
		return Sequence('"', stringContent(), '"', drop());
	}
	
	/**
	 * parse a double-quoted string and push it on the stack or NIL and push null on the stack
	 */
	Rule nstring() {
		return FirstOf(string(), nil());
	}
	
	/**
	 * parse a double-quoted string
	 */
	Rule nstringNoStack() {
		return Sequence(FirstOf(string(), nil()), drop());
	}
	
	/**
	 * parse an {@link Integer} and push it on the stack
	 */
	Rule number() {
		return Sequence(OneOrMore(CharRange('0', '9')), push(match()), convertTopFromStringToInt());
	}

	boolean convertTopFromStringToInt() {
		String s = (java.lang.String) pop();
		int value = Integer.valueOf(s);
		push(value);
		return true;
	}
	
	Rule SequenceWithWhitespaces(Object... rules) {
		Object[] array = new Object[rules.length * 2 - 1];
		int i = 0;
		for (Object rule: rules) {
			if (i != 0) {
				array[i++] = whitespaces();
			}
			array[i++] = rule;
		}
		return Sequence(array);
	}
	
	
}
