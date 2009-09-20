package com.sun.midp.appmanager;

// Simple comparative interface required to be implemented
// for the QuickSortAlg class to sort something.
public interface Sortable {
	public boolean lessthan(Sortable cmp);
}