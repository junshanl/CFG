package cn.jintongsoft.nlp.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class LLParser {

	private static final Logger logger = LogManager.getLogger(LLParser.class);

	public Map<Rule, Set<Node>> rawFirst(CFG cfg) {
		List<Rule> rules = cfg.content;
		Map<Rule, Set<Node>> firstTable = new HashMap<Rule, Set<Node>>();

		for (Rule rule : rules) {
			Set<Node> set = new HashSet<Node>();
			set.add(rule.right[0]);
			firstTable.put(rule, set);
		}
		return firstTable;
	}

	public Map<Rule, Set<Node>> createFirst(CFG cfg) {
		Map<Rule, Set<Node>> firstTable = rawFirst(cfg);
		return converge(firstTable);
	}

	public Map<Rule, Set<Node>> converge(Map<Rule, Set<Node>> map) {
		Map<Rule, Set<Node>> determinedMap = getDeterminedLeft(map);

		if (determinedMap.size() == map.size()) {
			return map;
		}

		for (Entry<Rule, Set<Node>> entry : map.entrySet()) {
			Set<Node> value = entry.getValue();
			Set<Node> remove = new HashSet<Node>();
			Set<Node> add = new HashSet<Node>();
			for (Node node : value) {
				for (Entry<Rule, Set<Node>> deter : determinedMap.entrySet()) {
					if (deter.getKey().left.equals(node)) {
						remove.add(node);
						add.addAll(deter.getValue());
					}
				}
			}
			value.addAll(add);
			value.removeAll(remove);
		}
		return converge(map);
	}

	public Map<Rule, Set<Node>> getDeterminedLeft(Map<Rule, Set<Node>> map) {
		Set<Rule> nondeterminedRules = new HashSet<Rule>();
		for (Entry<Rule, Set<Node>> entry : map.entrySet()) {
			Rule rule = entry.getKey();
			Set<Node> first = entry.getValue();
			if (!areAllDetemined(first)) {
				nondeterminedRules.add(rule);
			}
		}

		Set<Node> nondeterminedNode = new HashSet<Node>();
		for (Rule rule : nondeterminedRules) {
			nondeterminedNode.add(rule.left);
		}

		Map<Rule, Set<Node>> determinedRules = new HashMap<Rule, Set<Node>>();
		for (Entry<Rule, Set<Node>> entry : map.entrySet()) {
			Rule rule = entry.getKey();
			Set<Node> first = entry.getValue();
			if (areAllDetemined(first)) {
				determinedRules.put(rule, first);
			}
		}

		Map<Rule, Set<Node>> determined = new HashMap<Rule, Set<Node>>();
		for (Entry<Rule, Set<Node>> entry : determinedRules.entrySet()) {
			Rule rule = entry.getKey();
			Set<Node> first = entry.getValue();
			if (!nondeterminedNode.contains(rule.left)) {
				determined.put(rule, first);
			}
		}

		return determined;
	}

	public boolean areAllDetemined(Set<Node> set) {
		for (Node node : set) {
			if (node.getClass().equals(NDNode.class)) {
				return false;
			}
		}
		return true;
	}

	public boolean areAllDetemined(Map<Node, Rule> set) {
		for (Node node : set.keySet()) {
			if (node.getClass().equals(NDNode.class)) {
				return false;
			}
		}
		return true;
	}

	public Map<NDNode, Map<Node, Rule>> rawFollowByFirst(CFG cfg, Map<Rule, Set<Node>> first) {
		List<Rule> rules = cfg.content;
		Map<NDNode, Map<Node, Rule>> followDNode = new HashMap<NDNode, Map<Node, Rule>>();

		for (Rule rule : rules) {
			followDNode.put(rule.left, new HashMap<Node, Rule>());
		}
		followDNode.get(cfg.content.get(0).left).put(Bean.END, null);

		for (Rule rule : rules) {
			for (int i = 0; i < rule.right.length - 1; i++) {
				Map<Node, Rule> map = followDNode.get(rule.right[i]);
				if (map != null) {
					for (int j = i + 1; j < rule.right.length; j++) {
						if (rule.right[j].getClass().equals(DNode.class)) {
							map.put(rule.right[j], null);
							break;
						}
						for (Entry<Rule, Set<Node>> entry : first.entrySet()) {
							if (entry.getKey().left.equals(rule.right[j])) {
								for (Node firstNode : entry.getValue()) {
									map.put(firstNode, entry.getKey());
								}
								map.remove(Bean.EMPTY);
							}
						}
						if (!checkEmpty(cfg, rule.right[j])) {
							break;
						}
					}
				}
			}
		}
		return followDNode;
	}

	public boolean checkEmpty(CFG cfg, Node node) {
		for (Rule rule : cfg.content) {
			if (rule.left.equals(node) && rule.right[0].equals(Bean.EMPTY))
				return true;
		}
		return false;
	}

	public Map<NDNode, Map<Node, Rule>> rawFollow(CFG cfg) {
		List<Rule> rules = cfg.content;

		Map<NDNode, Map<Node, Rule>> followTable = new HashMap<NDNode, Map<Node, Rule>>();
		for (Rule rule : rules) {
			followTable.put(rule.left, new HashMap<Node, Rule>());
		}

		for (Rule rule : rules) {
			for (int i = rule.right.length - 1; i >= 0; i--) {
				if (rule.right[i].getClass().equals(NDNode.class)) {
					Map<Node, Rule> nodes = followTable.get(rule.right[i]);
					if (!rule.left.equals(rule.right[i]))
						nodes.put(rule.left, null);
					if (!checkEmpty(cfg, rule.right[i]))
						break;
				} else {
					break;
				}
			}
		}
		return followTable;
	}

	public Map<NDNode, Map<Node, Rule>> createFollow(CFG cfg, Map<Rule, Set<Node>> first) {
		Map<NDNode, Map<Node, Rule>> followHierical = rawFollow(cfg);
		Map<NDNode, Map<Node, Rule>> followDetermined = rawFollowByFirst(cfg, first);
		Map<NDNode, Map<Node, Rule>> mergeMap = merge(followHierical, followDetermined);
		return convergeByNDNode(mergeMap);
	}

	public Map<NDNode, Map<Node, Rule>> merge(Map<NDNode, Map<Node, Rule>> m1, Map<NDNode, Map<Node, Rule>> m2) {
		Map<NDNode, Map<Node, Rule>> mergeMap = new HashMap<NDNode, Map<Node, Rule>>();
		for (Entry<NDNode, Map<Node, Rule>> entry : m1.entrySet()) {
			Map<Node, Rule> map1 = entry.getValue();
			Map<Node, Rule> map2 = m2.get(entry.getKey());
			map1.putAll(map2);
			mergeMap.put(entry.getKey(), map1);
		}
		return mergeMap;
	}

	public Map<NDNode, Map<Node, Rule>> convergeByNDNode(Map<NDNode, Map<Node, Rule>> map) {
		Map<NDNode, Map<Node, Rule>> determinedMap = new HashMap<NDNode, Map<Node, Rule>>();
		for (Entry<NDNode, Map<Node, Rule>> entry : map.entrySet()) {
			NDNode node = entry.getKey();
			Map<Node, Rule> follow = entry.getValue();
			if (areAllDetemined(follow)) {
				determinedMap.put(node, follow);
			}
		}

		if (determinedMap.size() == map.size()) {
			return map;
		}

		for (Entry<NDNode, Map<Node, Rule>> entry : map.entrySet()) {
			Map<Node, Rule> value = entry.getValue();
			Set<Node> remove = new HashSet<Node>();
			Map<Node, Rule> add = new HashMap<Node, Rule>();
			for (Node node : value.keySet()) {
				Map<Node, Rule> temp;
				if ((temp = determinedMap.get(node)) != null) {
					add.putAll(temp);
					remove.add(node);
				}
			}
			value.putAll(add);

			for (Node n : remove) {
				value.remove(n);
			}
		}
		return convergeByNDNode(map);
	}

	public SyntaxTree parse(CFG cfg, ArrayList<Token> tokens) {
		Map<Rule, Set<Node>> first = createFirst(cfg);
		Map<NDNode, Map<Node, Rule>> follow = createFollow(cfg, first);
		if (tokens == null) {
			return null;
		}
		return parse(cfg, cfg.content.get(0).left, first, follow, tokens);
	}

	public SyntaxTree parse(CFG cfg, NDNode root, Map<Rule, Set<Node>> first, Map<NDNode, Map<Node, Rule>> follow,
			ArrayList<Token> tokens) {
		Stack<Node> s = new Stack<Node>();
		s.push(Bean.END);
		s.push(root);

		Queue<Token> input = new LinkedList<Token>(tokens);
		input.add(new Token("END", Bean.END));

		Process p = new Process();
		p.s = s;
		p.q = input;

		parse(cfg, p, first, follow);
		return p.tree;
	}

	private void parse(CFG cfg, Process p, Map<Rule, Set<Node>> first, Map<NDNode, Map<Node, Rule>> follow) {
		while (true) {
			if (showLog) {
				String q = null;
				if (p.q.size() > 30) {
					q = p.q.toString().substring(0, 30) + "...";
				} else {
					q = p.q.toString();
				}
				logger.info(
						count + ":" + p.q.size() + " " + p.isTransaction + " " + p.s + " " + q.replaceAll("\n", ""));
			}

			if (p.s.isEmpty()) {
				return;
			}

			if (p.q.isEmpty()) {
				return;
			}

			Node currentNode = null;
			DNode dnode = null;

			currentNode = p.s.peek();
			dnode = p.q.element().getPattern();

			if (currentNode.getClass().equals(DNode.class)) {
				if (currentNode.equals(Bean.EMPTY) || dnode.equals(currentNode)) {
					Token token = null;
					p.s.pop();
					if (!currentNode.equals(Bean.EMPTY))
						token = p.q.poll();
					p.tree.addTreeLeaf((DNode) currentNode, token);

				} else {
					p.q.poll();
				}
			} else {
				Map<Rule, Set<Rule>> branches = new LinkedHashMap<Rule, Set<Rule>>();
				if (dnode.equals(Bean.END)) {
					for (Entry<Rule, Set<Node>> rule : first.entrySet()) {
						if (rule.getValue().contains(Bean.EMPTY) && rule.getKey().left.equals(currentNode)) {
							Map<Node, Rule> map = follow.get(currentNode);
							if (map.keySet().contains(Bean.END)) {
								branches.put(rule.getKey(), null);
							}
						}
					}
				} else {
					branches = look(currentNode, dnode, first, follow);
				}
				if (branches.size() > 0) {
					List<Map.Entry<Rule, Set<Rule>>> list = new ArrayList<Map.Entry<Rule, Set<Rule>>>(
							branches.entrySet());
					Collections.sort(list, new RuleComparator(cfg.content));

					for (int i = 0; i < list.size(); i++) {
						Rule rule = list.get(i).getKey();
						Set<Rule> target = list.get(i).getValue();

						if (i < branches.size() - 1) {
							if (showLog) {
								for (Entry<Rule, Set<Rule>> branch : list) {
									logger.info(branch.getKey().rollback + "  " + branch.getKey() + " : "
											+ branch.getValue());
								}
							}
							Process temp;
							temp = transaction(cfg, p, rule, target, first, follow);
							if (temp != null) {
								break;
							} else {
								continue;
							}
						} else {
							p.accept(rule);
						}
					}
				} else {
					p.q.poll();
				}
			}
		}
	}

	private Process transaction(CFG cfg, Process p, Rule branch, Set<Rule> target, Map<Rule, Set<Node>> first,
			Map<NDNode, Map<Node, Rule>> follow) {
		Process trans = p.copy();
		trans.parent = p;
		trans.accept(branch);
		trans.isTransaction = true;
		trans.target = target;

		trans = parse(cfg, trans, first, follow, null);
		if (trans != null) {
			if (showLog) {
				logger.info("success...");
			}
			return trans;
		}
		if (showLog) {
			logger.info("rollback...");
		}
		count = 0;
		return null;
	}

	private int count;

	public boolean showLog;

	private Process parse(CFG cfg, Process p, Map<Rule, Set<Node>> first, Map<NDNode, Map<Node, Rule>> follow,
			ArrayList<Token> errors) {
		if (showLog) {
			String q;
			if (p.q.size() > 30) {
				q = p.q.toString().substring(0, 30) + "...";
			} else {
				q = p.q.toString();
			}
			logger.info(count + ":" + p.q.size() + " " + p.isTransaction + " " + p.s + " " + q.replaceAll("\n", ""));
		}

		Node currentNode = null;
		DNode dnode = null;
		currentNode = p.s.peek();
		dnode = p.q.element().getPattern();

		if (currentNode.getClass().equals(DNode.class)) {
			if (currentNode.equals(Bean.EMPTY) || dnode.equals(currentNode)) {
				Token token = null;
				p.s.pop();
				if (!currentNode.equals(Bean.EMPTY)) {
					token = p.q.poll();
				}
				List<Rule> accepted = p.tree.addTreeLeaf((DNode) currentNode, token);
				Process f = p;
				if (f.isTransaction) {
					boolean accept = false;
					while (f.isTransaction) {
						for (Rule rule : accepted) {
							for (Rule t : f.target) {
								if (t.equals(rule)) {
									accept = true;
									if (showLog) {
										logger.info("accepting... " + f + rule);
									}
									f = f.finishTrans(rule);
									break;
								}
							}
							if (accept) {
								break;
							}
						}
						if (!accept) {
							break;
						} else {
							accept = false;
						}
					}
					if (showLog) {
						if (!p.equals(f)) {
							Process unfinish = f;
							while (unfinish != null) {
								logger.info("unfinished... " + unfinish + " " + unfinish.target);
								unfinish = unfinish.parent;
							}
						}
					}
					if (f.isTransaction) {
						return parse(cfg, f, first, follow, null);
					} else {
						return f;
					}
				}
				if (p.isFinish()) {
					return p.finishAll();
				} else {
					return parse(cfg, p, first, follow, errors);
				}
			} else {
				if (!p.isTransaction) {
					p.q.poll();
					return parse(cfg, p, first, follow, errors);
				} else {
					return null;
				}
			}
		} else {
			Map<Rule, Set<Rule>> branches = new LinkedHashMap<Rule, Set<Rule>>();
			if (dnode.equals(Bean.END)) {
				for (Entry<Rule, Set<Node>> rule : first.entrySet()) {
					if (rule.getValue().contains(Bean.EMPTY) && rule.getKey().left.equals(currentNode)) {
						branches.put(rule.getKey(), null);
					}
				}
			} else {
				branches = look(currentNode, dnode, first, follow);
			}
			if (branches.size() > 0) {
				List<Map.Entry<Rule, Set<Rule>>> list = new ArrayList<Map.Entry<Rule, Set<Rule>>>(branches.entrySet());
				Collections.sort(list, new RuleComparator(cfg.content));
				for (int i = 0; i < list.size(); i++) {
					Rule rule = list.get(i).getKey();
					Set<Rule> target = list.get(i).getValue();

					if (i < branches.size() - 1) {
						if (showLog) {
							for (Entry<Rule, Set<Rule>> branch : list.subList(i, list.size())) {
								logger.info(
										branch.getKey().rollback + "  " + branch.getKey() + " : " + branch.getValue());
							}
						}

						Process trans = p.copy();
						trans.parent = p;
						trans.accept(rule);
						trans.isTransaction = true;
						trans.target = target;

						Process temp;
						temp = parse(cfg, trans, first, follow, errors);
						if (temp != null) {
							return temp;
						} else {
							continue;
						}
					} else {
						if (showLog) {
							if (list.size() > 1) {
								Entry<Rule, Set<Rule>> branch = list.get(branches.size() - 1);
								logger.info(cfg.content.indexOf(branch) + "  " + branch.getKey().rollback + "  ");
								logger.info(branch.getKey());
							}
						}
						p.accept(rule);

						Process temp;
						temp = parse(cfg, p, first, follow, errors);
						if (temp != null) {
							return temp;
						} else {
							return null;
						}
					}
				}
				if (p.isFinish()) {
					return p;
				} else {
					return parse(cfg, p, first, follow, errors);
				}
			} else {
				return null;
			}
		}
	}

	// 分支rule，以及目标包含rollback的rule
	private Map<Rule, Set<Rule>> look(Node currentNode, DNode dnode, Map<Rule, Set<Node>> first,
			Map<NDNode, Map<Node, Rule>> follow) {
		Map<Rule, Rule> copy = new HashMap<Rule, Rule>();
		for (Entry<Rule, Set<Node>> firstSet : first.entrySet()) {
			if (firstSet.getKey().left.equals(currentNode)) {

				if (firstSet.getValue().contains(dnode)) {
					copy.put((Rule) firstSet.getKey().clone(), (Rule) firstSet.getKey().clone());
				}
				if (firstSet.getValue().contains(Bean.EMPTY)) {
					Map<Node, Rule> map = follow.get(currentNode);
					if (map != null && map.keySet().contains(dnode)) {
						if (map.get(dnode) == null) {
							copy.put((Rule) firstSet.getKey().clone(), null);
						} else {
							copy.put((Rule) firstSet.getKey().clone(), (Rule) map.get(dnode).clone());
						}

					}
				}
			}
		}
		Map<Rule, Set<Rule>> rules = new HashMap<Rule, Set<Rule>>();
		for (Entry<Rule, Rule> entry : copy.entrySet()) {
			Node current = null;
			Rule rule = entry.getKey().clone();
			Set<Rule> furthers = new HashSet<Rule>();
			furthers.add(entry.getValue());
			Set<Rule> target = new HashSet<Rule>();

			while (true) {
				if (furthers.size() == 0) {
					break;
				}
				Set<Rule> next = new HashSet<Rule>();

				for (Rule f : furthers) {
					if (f == null) {
						break;
					}
					current = f.right[0];
					if (f.rollback) {
						target.add(f);
					} else {
						next.addAll(lookSkipEmpty(current, dnode, first, follow));
					}
				}
				furthers = next;
			}
			rules.put(rule.clone(), target);
		}
		return rules;
	}

	private Set<Rule> lookSkipEmpty(Node currentNode, DNode dnode, Map<Rule, Set<Node>> first,
			Map<NDNode, Map<Node, Rule>> follow) {
		Set<Rule> rules = new HashSet<Rule>();

		for (Entry<Rule, Set<Node>> firstSet : first.entrySet()) {
			if (firstSet.getKey().left.equals(currentNode)) {
				if (firstSet.getValue().contains(dnode)) {
					rules.add(firstSet.getKey());
				}
			} else if (firstSet.getValue().contains(Bean.EMPTY)) {
				Map<Node, Rule> map = follow.get(firstSet.getValue());
				if (map != null && map.keySet().contains(dnode)) {
					rules.add(map.get(dnode));
				}
			}
		}
		return rules;
	}

}

class RuleComparator implements Comparator<Map.Entry<Rule, Set<Rule>>> {

	private List<Rule> list;

	public RuleComparator(List<Rule> list) {
		this.list = list;
	}

	@Override
	public int compare(Entry<Rule, Set<Rule>> o1, Entry<Rule, Set<Rule>> o2) {
		Rule r1 = o1.getKey();
		Rule r2 = o2.getKey();
		boolean rb1 = containsRollback(o1.getValue());
		boolean rb2 = containsRollback(o2.getValue());

		return rb1 == rb2 ? (list.indexOf(r1) < list.indexOf(r2) ? -1 : 1) : (rb1 == false ? 1 : -1);

	}

	public boolean containsRollback(Set<Rule> rules) {
		boolean flag = false;
		for (Rule rule : rules) {
			if (rule.rollback) {
				flag = true;
				break;
			}
		}
		return flag;
	}

}