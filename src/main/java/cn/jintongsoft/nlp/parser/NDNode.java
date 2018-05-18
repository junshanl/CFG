package cn.jintongsoft.nlp.parser;

public class NDNode implements Node{
	
	private String name ;
	
	public boolean preserved;
	
	public NDNode(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
	
}
