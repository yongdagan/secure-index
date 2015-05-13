package com.gmail.yongdagan.secure_index.dataobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Index {
	
	private HashMap<String, TermNode> terms;
	private List<Pair<Integer, String>> docIds;
	private int docCurId;
	private int docStartId;
	
	public boolean isEmpty() {
		return terms.size() == 0;
	}
	
	public void markDocId(String fileName) {
		docIds.add(new Pair<Integer, String>(docCurId, fileName));
		docCurId ++;
	}
	
	public int getDocNum() {
		return docCurId - docStartId + 1;
	}
	
	public Index(int docCurId) {
		terms = new HashMap<String, TermNode>();
		docIds = new ArrayList<Pair<Integer,String>>();
		this.docCurId = docCurId;
		this.docStartId = docCurId;
	}
	
	public void addTerm(String termName) {
		TermNode termNode = null;
		if(terms.containsKey(termName)){
			termNode = terms.get(termName);
		} else {
			termNode = new TermNode();
		}
		termNode.addDoc(docCurId);
		terms.put(termName, termNode);
	}
	
	public int getSize() {
		return terms.size();
	}
	
	public Set<Map.Entry<String, TermNode>> getTerms() {
		return terms.entrySet();
	}
	
	public List<Pair<Integer, String>> getDocIds() {
		return docIds;
	}

	void print() {
		for(Pair<Integer, String> docId : docIds) {
			System.out.println(docId.getFirst() + "\t" + docId.getSecond());
		}
		System.out.println();
		for(Map.Entry<String, TermNode> term : terms.entrySet()) {
			System.out.print(term.getKey() + ":");
			term.getValue().print();
			System.out.println();
		}
	}

}
