package edu.washington.hoganc17.clickgen.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FileUploadUtils {

//	public static void main(String[] args) {
//		try {
//			File f = new File("./files/dmv.wav");
//			AudioPair gen = generate(new FileInputStream(f), "http://192.168.0.76:5000/generate");
//
//			System.out.println(gen.getSr());
//			System.out.println(gen.getBeatsArray());
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			System.out.println("Did not get a valid resposne.");
//		}
//	}

	public static InputStream requestFile(InputStream input, String url, String filename) throws IOException, NullPointerException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(url);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		builder.addBinaryBody(
				"audioFile",
				input,
				ContentType.APPLICATION_OCTET_STREAM,
				filename
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		HttpEntity responseEntity = response.getEntity();
		Header[] responseHeaders = response.getHeaders();

		File saveFile = File.createTempFile("converted", "wav");

		if (responseEntity != null) {
			try (FileOutputStream outstream = new FileOutputStream(saveFile)) {
				responseEntity.writeTo(outstream);
			}
		}

		InputStream is = new FileInputStream(saveFile);
		saveFile.delete();

		return is;
	}

	public static AudioTrio generate(InputStream input, String url, String filename) throws IOException, NullPointerException, JSONException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(url);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		builder.addBinaryBody(
				"audioFile",
				input,
				ContentType.APPLICATION_OCTET_STREAM,
				filename
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		HttpEntity responseEntity = response.getEntity();
		Header[] responseHeaders = response.getHeaders();

		File saveFile = File.createTempFile("converted", "wav");

		if (responseEntity != null) {
			try (FileOutputStream outstream = new FileOutputStream(saveFile)) {
				responseEntity.writeTo(outstream);
			}
		}

		InputStream is = new FileInputStream(saveFile);
		saveFile.delete();

		JSONObject beatsObj = null;
		for( Header h : responseHeaders ) {
			if(h.getName().equals("X-Beats"))
				beatsObj = new JSONObject(h.getValue());
		}

		JSONArray beatsArray = beatsObj.optJSONArray("beats");
		int sr = beatsObj.optInt("sr");
		if(beatsArray != null && sr != 0) {
			List<Integer> beatsArrayInt = new ArrayList<Integer>();

			for( int i = 0; i < beatsArray.length(); i++) {
				beatsArrayInt.add(beatsArray.optInt(i));
			}

			AudioTrio ret = new AudioTrio(beatsArrayInt, sr, is);

			return(ret);

		} else {
			throw new NullPointerException();
		}
	}

	public static InputStream getFile(String key, String type, String url) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String newUrl = url + "/" + key + "_" + type + ".wav";
		HttpGet uploadFile = new HttpGet(newUrl);


		CloseableHttpResponse response = httpClient.execute(uploadFile);
		HttpEntity responseEntity = response.getEntity();

		File saveFile = File.createTempFile("converted", "wav");

		if (responseEntity != null) {
			try (FileOutputStream outstream = new FileOutputStream(saveFile)) {
				responseEntity.writeTo(outstream);
			}
		}

		InputStream is = new FileInputStream(saveFile);
		saveFile.delete();

		return is;
	}

	public static String generate(InputStream input, String url, String filename, float click_freq, float click_dur) throws IOException, URISyntaxException, JSONException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		URIBuilder uriBuilder = new URIBuilder(url);


		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("click_freq", click_freq + ""));
		postParameters.add(new BasicNameValuePair("click_dur", click_dur + ""));

		uriBuilder.addParameters(postParameters);

		HttpPost uploadFile = new HttpPost(uriBuilder.build());
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();



		uploadFile.setEntity(new UrlEncodedFormEntity(postParameters));

		builder.addBinaryBody(
				"audioFile",
				input,
				ContentType.APPLICATION_OCTET_STREAM,
				filename
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);

		CloseableHttpResponse response = httpClient.execute(uploadFile);
		HttpEntity responseEntity = response.getEntity();
		Header[] responseHeaders = response.getHeaders();

		JSONObject headerObj = null;
		for( Header h : responseHeaders ) {
			if(h.getName().equals("X-Urls"))
				headerObj = new JSONObject(h.getValue());
		}

		String key = headerObj.optString("key");

		return key;
	}
}
