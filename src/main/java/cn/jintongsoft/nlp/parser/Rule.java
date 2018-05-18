package cn.jintongsoft.nlp.parser;

public class Rule {
	
	public NDNode left;
	
	public Node[] right;

	public boolean rollback;
	
	public String toString(){
		String temp = left.getName() +" -> "; 
	    for(Node node : right){
	    	temp += " " + node.getName();
	    }
	    return temp;
	}
	
	public Rule( NDNode left ,  Node[] right){
		this.left = left;
		this.right = right;
	}
	
	public Rule(){
		
	}

	public Rule(NDNode left, Node[] right, boolean rollback) {
		this.left = left;
		this.right = right;
		this.rollback = rollback;
	}
	
	public Rule(NDNode left, Node[] right, boolean rollback, int priority) {
		this.left = left;
		this.right = right;
		this.rollback = rollback;
	}

	public boolean isEmpty() {
		return right[0] == Bean.EMPTY;
	}

	public Rule clone(){
		Rule r = new Rule();
		r.left = this.left;
		r.right = this.right;
		r.rollback = this.rollback;
		return r;
	}
	
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(obj.getClass().equals(this.getClass())){
			if(((Rule)obj).left.equals(this.left) && ((Rule)obj).right.length == right.length){
				for(int i = 0; i < right.length; i++){
					if(!right[i].equals(((Rule)obj).right[i])){
						return false;
					}
				}
				return true;
			}
				

		}
		return false;
	}

}

