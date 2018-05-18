package cn.jintongsoft.nlp.parser;

public class Token {

	private String seg;

	private DNode pattern;

	public Token(String seg, DNode pattern) {
		super();
		this.seg = seg;
		this.pattern = pattern;
	}

	public String getSeg() {
		return seg;
	}

	public void setSeg(String seg) {
		this.seg = seg;
	}

	public DNode getPattern() {
		return pattern;
	}

	public void setPattern(DNode pattern) {
		this.pattern = pattern;
	}
	
	public String toString(){
		return seg;
	}

}
