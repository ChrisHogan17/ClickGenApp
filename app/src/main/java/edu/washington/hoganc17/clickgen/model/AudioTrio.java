package edu.washington.hoganc17.clickgen.model;

import java.io.InputStream;
import java.util.List;

public class AudioTrio {

	private List<Integer> beatsArray;
	private int sr;
	private InputStream is;

	public AudioTrio(List<Integer> beatsArray, int sr, InputStream is) {
		this.beatsArray = beatsArray;
		this.sr = sr;
		this.is = is;
	}

	public List<Integer> getBeatsArray() {
		return beatsArray;
	}

	public int getSr() {
		return sr;
	}

	public InputStream getIs() {
		return is;
	}
}
