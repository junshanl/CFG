package cn.jintongsoft.nlp.parser;

import java.util.ArrayList;

public class CFG {
	ArrayList<Rule> content;
	
	public CFG(ArrayList<Rule> gramma){
		this.content = gramma;
	}

	public CFG() {
		this.content = new ArrayList<Rule>();
	}

	public void add(Rule rule) {
		this.content.add(rule);
	}

	public void append(CFG cfg) {
		for(Rule rule : cfg.content){
			content.add(rule);
		}
	}
	
}
