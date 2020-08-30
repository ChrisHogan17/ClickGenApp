package edu.washington.hoganc17.clickgen.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.Header;
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
}
