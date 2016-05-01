package com.mob41.pushbullet.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mob41.pushbullet.hash.AES;

public class PBServer{
	
	/*
	 JSONObject:
	 
	 {username}, {entoken}, {ensalt} (Layer 2)
	 */
	private List<JSONObject> pbdata = new ArrayList<JSONObject>(50);
	
	private final String salt;
	private final String client_id;
	private final String client_secret;
	private final String apikey;
	
	public PBServer(String salt, String client_id, String client_secret, String apikey){
		this.salt = salt;
		this.client_id = client_id;
		this.client_secret = client_secret;
		this.apikey = apikey;
		reloadFile();
	}
	
	public void reloadFile(){
		pbdata = new ArrayList<JSONObject>(50);
		loadFile();
	}
	
	public void pushToAllUsers(String title, String desc) throws Exception{
		JSONObject pbdata;
		String detoken;
		String accesstoken;
		PBClient pbclient;
		for (int i = 0; i < getSize(); i++){
			pbdata = this.pbdata.get(i);
			detoken = AES.decrypt(pbdata.getString("entoken"), pbdata.getString("ensalt"));
			accesstoken = this.deentok(detoken);
			pbclient = new PBClient(client_id, client_secret, apikey);
			pbclient.pushNote(title, desc, accesstoken);
		}
	}
	
	public int getSize(){
		return pbdata.size();
	}
	
	public int getCodeIndex(String username){
		JSONObject data;
		int i;
		for (i = 0; i < pbdata.toArray().length; i++){
			data = pbdata.get(i);
			if (data.getString("username").equals(username)){
				return i;
			}
		}
		return -1;
	}
	
	public int getMaskedPBCodeIndex(String maskedpbcode){
		String mask;
		JSONObject pbcodearr;
		int i;
		for (i = 0; i < pbdata.toArray().length; i++){
			pbcodearr = pbdata.get(i);
			mask = "******" + pbcodearr.getString("entoken").substring(5, 10) + "******";
			if (mask.equals(maskedpbcode)){
				return i;
			}
		}
		return -1;
	}
	
	public int amountOfPBCode(String username){
		int amount = 0;
		int i;
		JSONObject pbcode;
		for (i = 0; i < pbdata.toArray().length; i++){
			pbcode = pbdata.get(i);
			if (pbcode.getString("username").equals(username)){
				amount++;
			}
		}
		return amount;
	}
	
	public JSONObject[] getUserPB(String username){
		int amount = amountOfPBCode(username);
		if (amount <= 0){
			return null;
		}
		JSONObject data;
		JSONObject[] output = new JSONObject[amount];
		int i;
		int j = 0;
		for (i = 0; i < pbdata.toArray().length; i++){
			data = pbdata.get(i);
			if (data.getString("username").equals(username)){
				output[j] = pbdata.get(i);
				j++;
			}
		}
		return output;
	}
	
	public boolean removeByMask(String maskedpbcode){
		int index = getMaskedPBCodeIndex(maskedpbcode);
		try {
			pbdata.remove(index);
			writeIn();
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
	public boolean register(String username, String access_token){
		try {
			if (isAccessTokenExist(access_token)){
				return false;
			}
			JSONObject build = buildCode(username, access_token);
			pbdata.add(build);
			writeIn();
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean isAccessTokenExist(String accesstoken){
		try {
			int i;
			JSONObject pbcode;
			for (i = 0; i < pbdata.toArray().length; i++){
				pbcode = pbdata.get(i);
				String encode = pbcode.getString("entoken");
				String decode = AES.decrypt(encode, pbcode.getString("ensalt"));
				String accesscode = this.deentok(decode);
				if (accesscode.equals(accesstoken)){
					return true;
				}
			}
			return false;
		} catch (Exception e){
			return false;
		}
	}
	
	public boolean isEncryptedTokenExist(String entoken){
		int i;
		JSONObject pbcode;
		for (i = 0; i < pbdata.toArray().length; i++){
			pbcode = pbdata.get(i);
			String encode = pbcode.getString("entoken");
			if (encode.equals(entoken)){
				return true;
			}
		}
		return false;
	}
	
	public JSONObject buildCode(String username, String access_token) throws Exception{
		String entoken = AES.encrypt(access_token, salt);
		String salt2 = AES.getRandomByte();
		String enentoken = AES.encrypt(entoken, salt2);
		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("entoken", entoken);
		json.put("ensalt", salt2);
		return json;
	}
	
	public String deentok(String entoken) throws Exception{
		String detoken = AES.decrypt(entoken, salt);
		return detoken;
	}
	
	public JSONObject getPBObject(int i){
		return pbdata.get(i);
	}
	
	private void loadFile(){
		try {
			File file = new File("ha_pbsave.properties");
			if (!file.exists()){
				createFile();
				return;
			}
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(file);
			prop.load(in);
			String data;
			int pbs = Integer.parseInt(prop.getProperty("pbs"));
			for (int i = 0; i < pbs; i++){
				data = prop.getProperty("pb" + i);
				pbdata.add(new JSONObject(data));
			}
			in.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void writeIn(){
		try {
			File file = new File("ha_pbsave.properties");
			if (!file.exists()){
				createFile();
				return;
			}
			Properties prop = new Properties();
			Object[] data = pbdata.toArray();
			int pbs = data.length;
			prop.setProperty("pbs", Integer.toString(pbs));
			for (int i = 0; i < pbs; i++){
				prop.setProperty("pb" + i, (String) data[i]);
			}
			FileOutputStream out = new FileOutputStream(file);
			prop.store(out, "PBSave");
			out.flush();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void createFile(){
		try {
			File file = new File("ha_pbsave.properties");
			if (!file.exists()){
				file.createNewFile();
			}
			Properties prop = new Properties();
			prop.setProperty("pbs", "0");
			FileOutputStream out = new FileOutputStream(file);
			prop.store(out, "PBSave");
			out.flush();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
