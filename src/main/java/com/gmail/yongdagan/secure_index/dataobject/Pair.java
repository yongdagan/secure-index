package com.gmail.yongdagan.secure_index.dataobject;

public class Pair<A, B> {
	
	private A first;
	private B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public void setFirst(A first) {
		this.first = first;
	}
	public A getFirst() {
		return first;
	}
	public void setSecond(B second) {
		this.second = second;
	}
	public B getSecond() {
		return second;
	}
	
}
