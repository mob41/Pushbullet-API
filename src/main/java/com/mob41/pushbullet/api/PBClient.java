package com.mob41.pushbullet.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class PBClient {
	
	private static final String tokenapiurl = "https://api.pushbullet.com/oauth2/token";
	private static final String deviceapiurl = "https://api.pushbullet.com/v2/devices";
	private static final String pushapiurl = "https://api.pushbullet.com/v2/pushes";

	private final String client_id;
	private final String client_secret;
	private final String apikey;
	
	public PBClient(String client_id, String client_secret, String apikey){
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.apikey = apikey;
	}
	
	public String convertCodeToAccessToken(String code) throws Exception{
		JSONObject json = new JSONObject();
		json.put("client_id", client_id);
		json.put("client_secret", client_secret);
		json.put("grant_type", "authorization_code");
		json.put("code", code);
		URLConnection connection = new URL(tokenapiurl).openConnection();
		connection.setDoOutput(true); // Triggers POST.
		connection.setRequestProperty("Access-Token", apikey);
		//application/x-www-form-urlencoded;charset=utf-8
		connection.setRequestProperty("Content-Type", "application/json");

		try (OutputStream output = connection.getOutputStream()) {
		    output.write(json.toString().getBytes("utf-8"));
		}
		InputStream response = connection.getInputStream();
		JSONObject jsonout = new JSONObject(getStringFromInputStream(response));
		return jsonout.getString("access_token");
	}
	
	public JSONObject getUserDevices(String accesstoken) throws Exception{
		URLConnection connection = new URL(deviceapiurl).openConnection();
		connection.setRequestProperty("Access-Token", accesstoken);

		InputStream response = connection.getInputStream();
		JSONObject json = new JSONObject(getStringFromInputStream(response));
		return json;
	}
	
	public JSONObject getPushes() throws Exception{
		URLConnection connection = new URL(pushapiurl).openConnection();
		connection.setRequestProperty("Access-Token", apikey);

		InputStream response = connection.getInputStream();
		JSONObject json = new JSONObject(getStringFromInputStream(response));
		return json;
	}
	
	public JSONObject pushNote(String title, String body) throws Exception{
		return pushNote(title, body, null, apikey);
	}
	
	public JSONObject pushNote(String title, String body, String access_token) throws Exception{
		return pushNote(title, body, null, access_token);
	}
	
	public JSONObject pushNote(String title, String body, String device_ident, String access_token) throws Exception{
		URLConnection connection = new URL(pushapiurl).openConnection();
		connection.setRequestProperty("Access-Token", access_token);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
		JSONObject json = new JSONObject();
		if (device_ident != null){
			json.put("device_iden", device_ident);
		}
		json.put("title", title);
		json.put("body", body);
		json.put("type", "note");

		try (OutputStream output = connection.getOutputStream()) {
		    output.write(json.toString().getBytes("utf-8"));
		}
		InputStream response = connection.getInputStream();
		JSONObject responsejson = new JSONObject(getStringFromInputStream(response));
		return responsejson;
	}
	
	public JSONObject pushLink(String title, String body, String url) throws Exception{
		return pushLink(title, body, url, null, apikey);
	}
	
	public JSONObject pushLink(String title, String body, String url, String access_token) throws Exception{
		return pushLink(title, body, url, null, access_token);
	}
	
	public JSONObject pushLink(String title, String body, String url, String device_ident, String access_token) throws Exception{
		URLConnection connection = new URL(pushapiurl).openConnection();
		connection.setRequestProperty("Access-Token", access_token);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
		JSONObject json = new JSONObject();
		if (device_ident != null){
			json.put("device_iden", device_ident);
		}
		json.put("url", url);
		json.put("title", title);
		json.put("body", body);
		json.put("type", "note");

		try (OutputStream output = connection.getOutputStream()) {
		    output.write(json.toString().getBytes("utf-8"));
		}
		InputStream response = connection.getInputStream();
		JSONObject responsejson = new JSONObject(getStringFromInputStream(response));
		return responsejson;
	}
	
	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}
