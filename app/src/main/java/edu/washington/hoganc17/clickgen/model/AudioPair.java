package edu.washington.hoganc17.clickgen.model;

import java.util.List;

public class AudioPair {

	private List<Integer> beatsArray;
	private int sr;
	
	public AudioPair(List<Integer> beatsArray, int sr) {
		this.beatsArray = beatsArray;
		this.sr = sr;
	}
	
	public List<Integer> getBeatsArray() {
		return beatsArray;
	}
	
	public int getSr() {
		return sr;
	}
}
