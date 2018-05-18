package cn.jintongsoft.nlp.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Process {

	public Stack<Node> s;

	public Queue<Token> q;

	public Process parent;

	public SyntaxTree tree;

	public boolean isTransaction;
	
	public Set<Rule> target;

	public Process() {
		this.s = new Stack<Node>();
		this.q = new LinkedList<Token>();
		this.tree = new SyntaxTree();
	}

	public int getAllPreserved() {
		if (parent != null) {
			return q.size() + parent.getAllPreserved();
		} else {
			return q.size();
		}
	}

	public boolean isFinish() {
		if (s.isEmpty() && q.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return s.toString();
	}

	public Process copy() {
		Process p = new Process();
		p.s = mergeStack(new Stack<Node>(), s);
		p.tree = tree.copy();
		p.q = copyQueue(q);

		return p;
	}

	public Process finishTrans(Rule rule) {
		if (this.parent == null) {
			return this;
		}
		for(Rule t : this.target){
			if (t.equals(rule)) {
				this.parent.s = this.s;
				this.parent.q = this.q;
				this.parent.tree = this.tree;
				return this.parent.finishTrans(rule);
			} 
		}
		return this;
	}

	public Process finishAll() {
		if (this.parent == null) {
			return this;
		}
		this.parent.s = this.s;
		this.parent.q = this.q;
		this.parent.tree = this.tree;

		return this.parent.finishAll();
	}

	public Stack<Node> mergeStack(Stack<Node> s1, Stack<Node> s2) {
		Enumeration<Node> e = s2.elements();
		while (e.hasMoreElements()) {
			Node n = e.nextElement();
			s1.push(n);
		}
		return s1;
	}

	public Queue<Token> copyQueue(Queue<Token> q) {
		Queue<Token> newQ = new LinkedList<Token>();
		for (Token t : q) {
			newQ.add(t);
		}
		return newQ;
	}

	public void putReversed(Stack<Node> s, Node[] nodes) {
		List<Node> temp = Arrays.asList(Arrays.copyOf(nodes, nodes.length));
		Collections.reverse(temp);
		for (Node node : temp) {
			s.push(node);
		}
	}

	public void accept(Rule rule) {
		this.s.pop();
		putReversed(this.s, rule.right);
		this.tree.addTreeNode(rule);
	}

}
