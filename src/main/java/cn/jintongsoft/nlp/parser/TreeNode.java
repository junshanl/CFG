package cn.jintongsoft.nlp.parser;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

	public TreeNode parent;

	public Rule rule;
	
	public boolean accpeted;

	public List<TreeNode> children;

	public boolean isLeaf;

	public Token token;

	public int countAllLeaves;

	public ArrayList<Token> errors;

	public TreeNode() {
		children = new ArrayList<TreeNode>();
	}
	
	public String toString(){
		if(rule == null)
			return "root";
		return rule.toString();
	}

	public TreeNode copy(TreeNode parentNode) {
		TreeNode node = new TreeNode();
		node.parent = parentNode;
		node.rule = this.rule;
		node.accpeted = this.accpeted;
		node.children = new ArrayList<TreeNode>();
		
		for(TreeNode child : this.children){
			node.children.add(child.copy(node));
		}
		
		node.isLeaf = this.isLeaf;
		node.token = this.token;
		return node;
	}

}
