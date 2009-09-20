package com.sun.midp.appmanager;

/*
	We don't seem to have a nice sorting algorithm available in CLDC 1.1.1
	... so I'm providing one. 
	TODO: Put this (and the Sortable interface) somewhere in java.util or
	other such appropriate place
*/

public class QuicksortAlg {

	public static void sort(Sortable[] s) {
		quicksort(s, 0, (s.length-1)); }
	
	public static void quicksort(Sortable[] s, int a, int b) {
		if (b <= a) {
			return; }
		int idx = split(s, a, b);
		quicksort(s, a, idx-1);
		quicksort(s, idx+1, b); }
		
	private static int split(Sortable[] s, int a, int b) {
		int i = a - 1;
		int j = b;
		do {
			while ((s[++i].lessthan(s[b]))) {}
			while ((s[b].lessthan(s[--j]))) {
				if (j == a) {
					break; } }
			if (i >= j) {
				break; }
			exchange(s, i, j); } while(true);
		exchange(s, i, b);
		return i; }
	
	private static void exchange(Sortable[] s, int a, int b) {
		Sortable swap = s[a];
		s[a] = s[b];
		s[b] = swap; }
	
}