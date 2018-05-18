package cn.jintongsoft.nlp.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Lexeme {

	private int index;

	private String text;

	private boolean isEnd;

	private int pixel;

	private Pattern pattern;

	private ArrayList<Token> tokens;
	
	private LinkedHashMap<Pattern, DNode> lex; 
	
	public void init(String text,LinkedHashMap<Pattern, DNode> lex) {
		index = 0;
		pixel = 0;
		this.text = text;
		isEnd = false;
		tokens = new ArrayList<Token>();
		this.lex = lex;
	}

	public void read() {
		++index;
		if ( index >= text.length()) {
			isEnd = true;
		}
	}

	public void scan() {
		String segment = null;
		try {
			segment = text.substring(pixel, index + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Pattern pattern = null;
		for (Pattern p : lex.keySet()) {
			if (p.matcher(segment).find()) {
				pattern = p;
				break;
			}
		}
		if (this.pattern == null) {
			this.pattern = pattern;
		} else if (compare(this.pattern, pattern) < 0 || pattern == null) {
			this.tokens.add(new Token(text.substring(pixel, index), lex.get(this.pattern)));
			pixel = index;
			this.pattern = null;
			scan();
		} else {
			this.pattern = pattern;
		}
	}
	
	public void end(){
		this.tokens.add(new Token(text.substring(pixel, index), lex.get(this.pattern)));
	}

	public void run() {
		while (!isEnd) {
			scan();
			read();
		}
		end();
	}

	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}
	
	public int compare(Pattern p1 , Pattern p2){
		if(p1 == null){
			return 1;
		}
		
		int index1 = 0;
		for(Pattern p: lex.keySet()){
			if(p.equals(p1)){
				break;
			}
			index1++;
		}
		
		int index2 = 0;
		for(Pattern p: lex.keySet()){
			if(p.equals(p2)){
				break;
			}
			index2++;
		}
		return index1 - index2;
	} 

}


