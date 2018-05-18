package cn.jintongsoft.nlp.parser;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

public class SyntaxTree {

	public TreeNode root;

	public Stack<TreeNode> unaccepted;

	public SyntaxTree() {
		root = new TreeNode();
		unaccepted = new Stack<TreeNode>();
		unaccepted.push(root);
	}

	public SyntaxTree(TreeNode root, Stack<TreeNode> unaccepted) {
		this.root = root;
		this.unaccepted = unaccepted;
	}

	public void traverse(TreeNode root, VisitFunction f) {
		f.visit(root);
		for (TreeNode child : root.children) {
			traverse(child, f);
		}
	}

	public void addTreeNode(Rule rule) {
		TreeNode treeNode = new TreeNode();
		treeNode.rule = rule;

		treeNode.parent = this.unaccepted.peek();
		treeNode.parent.children.add(treeNode);
		unaccepted.push(treeNode);
	}

	public List<Rule> addTreeLeaf(DNode dnode, Token token) {
		TreeNode treeNode = new TreeNode();
		treeNode.parent = unaccepted.peek();
		treeNode.parent.children.add(treeNode);
		treeNode.token = token;
		treeNode.isLeaf = true;
		treeNode.accpeted = true;
		return acceptBackPropargate();
	}

	public void attachSubTree(SyntaxTree tree) {
		TreeNode treeNode = tree.root;
		treeNode.parent = this.unaccepted.peek();
		treeNode.parent.children.add(treeNode);

		Enumeration<TreeNode> e = tree.unaccepted.elements();
		while (e.hasMoreElements()) {
			unaccepted.push(e.nextElement());
		}
		acceptBackPropargate();
	}

	public List<Rule> acceptBackPropargate() {
		List<Rule> acceptList = new ArrayList<Rule>();
		if (unaccepted.isEmpty()) {
			return acceptList;
		}
		TreeNode parent = unaccepted.peek();
		while (isAccept(parent)) {
			parent.accpeted = true;
			unaccepted.pop();
			acceptList.add(parent.rule);
			if (!unaccepted.isEmpty()) {
				parent = unaccepted.peek();
			} else {
				break;
			}
		}
		return acceptList;
	}

	public boolean isAccept(TreeNode node) {
		if (node.rule == null) { // root
			return false;
		}
		if (node.rule.right.length != node.children.size())
			return false;
		for (TreeNode child : node.children) {
			if (child.accpeted == false) {
				return false;
			}
		}
		return true;
	}

	public List<TreeNode> getAllTreeNodes(Node node) {
		return traverse(root, node);
	}

	public List<TreeNode> getAllTreeNodes(TreeNode root, Node node) {
		return traverse(root, node);
	}

	private List<TreeNode> traverse(TreeNode treeNode, Node node) {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		if (treeNode.rule != null && treeNode.rule.left.equals(node))
			nodes.add(treeNode);

		for (TreeNode child : treeNode.children) {
			nodes.addAll(traverse(child, node));
		}
		return nodes;
	}

	public String getContent(TreeNode treeNode) {
		StringBuffer buffer = new StringBuffer();

		if (treeNode.isLeaf) {
			if (treeNode.token != null) {
				return treeNode.token.getSeg();
			} else {
				return "";
			}
		}

		for (TreeNode child : treeNode.children) {
			buffer.append(getContent(child));
		}
		return buffer.toString();
	}

	public List<Token> getTokens(TreeNode treeNode) {
		List<Token> tokens = new ArrayList<Token>();
		
		if (treeNode.isLeaf) {
			if (treeNode.token != null) {
				tokens.add(treeNode.token);
				return tokens;
			}
		}

		for (TreeNode child : treeNode.children) {
			tokens.addAll(getTokens(child));
		}
		
		return tokens;
	}

	public SyntaxTree copy() {
		Stack<TreeNode> unaccepted = new Stack<TreeNode>();

		TreeNode root = this.root.copy(null);
		traverse(root, new PutUnaccepted(unaccepted));

		SyntaxTree st = new SyntaxTree(root, unaccepted);
		return st;
	}

}

interface VisitFunction {
	public void visit(TreeNode tn);
}

class PutUnaccepted implements VisitFunction {

	Stack<TreeNode> unaccepted;

	public PutUnaccepted(Stack<TreeNode> unaccepted) {
		this.unaccepted = unaccepted;
	}

	@Override
	public void visit(TreeNode tn) {
		if (!tn.accpeted)
			unaccepted.push(tn);
	}

}
