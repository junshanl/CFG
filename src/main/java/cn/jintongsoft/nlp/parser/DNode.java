package cn.jintongsoft.nlp.parser;

import java.util.regex.Pattern;

public class DNode implements Node{

	private String name;
	
	private Pattern pattern;

	public DNode(String name, Pattern pattern) {
		this.name = name;
		this.pattern = pattern;
	}
	
	public DNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Pattern getPattern() {
		return pattern;
	}

	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public String toString(){
		return name;
	}
	
}