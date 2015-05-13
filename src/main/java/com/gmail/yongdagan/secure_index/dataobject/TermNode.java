package com.gmail.yongdagan.secure_index.dataobject;

import java.util.ArrayList;
import java.util.List;

public class TermNode {
	
	List<Pair<Integer, Long>> docs;
	
	public TermNode() {
		docs = new ArrayList<Pair<Integer,Long>>();
	}
	
	public int getSize() {
		return docs.size();
	}
	
	public void addDoc(Integer docId) {
		Pair<Integer, Long> pair = new Pair<Integer, Long>(docId, 1L);
		if (docs.isEmpty()) {
			docs.add(pair);
		} else {
			// check the last docId
			int i = docs.size() - 1;
			Pair<Integer, Long> cur = docs.get(i);
			if (cur.getFirst().equals(docId)) {
				cur.setSecond(cur.getSecond() + 1);
				docs.set(i, cur);
			} else {
				docs.add(pair);
			}
		}
	}
	
	public List<Pair<Integer, Long>> getDocs() {
		return docs;
	}
	
	void print() {
		for(Pair<Integer, Long> doc : docs) {
			System.out.print("\t" + doc.getFirst() + " " + doc.getSecond());
		}
	}

}
