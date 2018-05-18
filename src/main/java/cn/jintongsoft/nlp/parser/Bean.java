package cn.jintongsoft.nlp.parser;

import java.util.regex.Pattern;

public class Bean {
	
	public static final DNode EMPTY = new DNode("EMPTY",Pattern.compile(""));

	public static final DNode END = new DNode("END",Pattern.compile(""));
	
}
