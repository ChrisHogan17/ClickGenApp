package edu.washington.hoganc17.clickgen.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

public class AudioTrio {

	private List<Integer> beatsArray;
	private int sr;
	private InputStream inputStream;

	public AudioTrio(List<Integer> beatsArray, int sr, InputStream is) {
		this.beatsArray = beatsArray;
		this.sr = sr;
		this.inputStream = is;
	}

	public List<Integer> getBeatsArray() {
		return beatsArray;
	}

	public int getSr() {
		return sr;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
}
