package com.pingpong.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FBDataCollector {

	ArrayList<String> FRIEND_URLS = null;
	ArrayList<String> ME_URLS = null;
	
	private String m_authToken = null;
	private String m_userId = null;
	private String m_path = null;
	
	public FBDataCollector(String path, String userId, String authToken) {
		m_path = path;
		m_authToken = authToken;
		m_userId = userId;
		
		FRIEND_URLS =new ArrayList<String>(Arrays.asList("fbprofile","feed", "statuses", "likes", "movies", "music","books", "photos", "events", "checkins", "locations" ));
		ME_URLS = new ArrayList<String>(FRIEND_URLS);
		ME_URLS.addAll(Arrays.asList("friends"));
		
	}

	public void getData() throws Exception {
		
		long start = System.currentTimeMillis();
		//System.out.println("Starting time = " + new Date(start));
		String friendId = "me";
		int count = 0;

		HashMap<String, String> friendsMap = getFriendsList();

		JSONArray array = new JSONArray();
		JSONObject req = new JSONObject();

		for (int j = 0; j < ME_URLS.size(); j++) {
			req = new JSONObject();
			req.put("method", "GET");
			if (ME_URLS.get(j) == "fbprofile")
				req.put("relative_url", "me" );
			else
				req.put("relative_url", "me/" + ME_URLS.get(j));
			array.add(req);
		}
		String content = connectFB(m_userId, friendId, array.toString(),false, "POST");
		
		if (! MongoDBConnector.hasProfile(m_userId,"frilp_user")){			
			MongoDBConnector.insert("{ \"type\": \"frilp_user\", \"id\":\""+m_userId+"\"}");
			splitJSON(m_userId,content, ME_URLS);
		}
		
		Iterator<String> keySet = friendsMap.keySet().iterator();
		while (keySet.hasNext()) {
			friendId = keySet.next();
			if (! MongoDBConnector.hasProfile(friendId,"fbprofile")){
				array.clear();
				for (int j = 0; j < FRIEND_URLS.size(); j++) {
					req = new JSONObject();
					req.put("method", "GET");
					if (FRIEND_URLS.get(j) == "fbprofile" )
						req.put("relative_url", friendId );
					else
						req.put("relative_url", friendId + "/" + FRIEND_URLS.get(j));
					array.add(req);
				}
	
				content = connectFB(m_userId, friendId, array.toString(), false,
						"POST");
	
				if (content != null) {
					splitJSON(friendId, content, FRIEND_URLS);
				}
				
				System.out.println("(UserID, FriendID) : ("+m_userId+","+friendId+")");
			}
			//else
				//System.out.println("Profile already collected: "+ friendId);
		}
		
		long end = System.currentTimeMillis();
		//System.out.println("Ending time = " + new Date(end));
		long taken = end - start;
	}

	private HashMap<String,String> getFriendsList() throws Exception {
		HashMap<String, String> map = null;
		JSONObject item = null;
		String id = null;
		
		String content = connectFB(m_userId, "me", "friends", false, "GET");
		
		map = new HashMap<String, String>();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);

		JSONObject objJson = (JSONObject) obj;
		JSONArray jsonList = (JSONArray) objJson.get("data");
		for (int i = 0; i < jsonList.size(); i++) {
			item = (JSONObject) jsonList.get(i);
			id = (String) item.get("id");
			map.put(id, (String) item.get("name"));
		}

		return map;
	}

	private void splitJSON(String userId, String content, ArrayList<String>URLS) throws Exception {

		String data = null;
		JSONObject item = null;
		JSONParser parser1 = null;
		JSONArray array = new JSONArray();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		JSONArray objJson = (JSONArray) obj;
		
		
		
		for (int i = 0; i < URLS.size(); i++) {
			try {
				item = (JSONObject) objJson.get(i);
				if (item == null) {
					continue;
				}
				data = (String) item.get("body");
				if (data == null) {
					System.out.println(data);
					
					continue;
				}
				parser1 = new JSONParser();
				Object obj1 = parser1.parse(data);
				JSONObject dataObj = (JSONObject) obj1;
				if (dataObj != null) {
					if (URLS.get(i).equals("fbprofile"))
						array.add(dataObj);
					else
						array = (JSONArray) dataObj.get("data");
					if (array != null && array.size() != 0) {	
						if (URLS.get(i).equals("fbprofile")){
						data = data.replaceFirst("\\{", "\\{ \"type\":\"fbprofile\", \"id\":\""+userId+"\",");
						
					}
					else{
						data = data.replaceFirst("\\{", "\\{ \"type\":\"" + URLS.get(i) + "\", \"id\":\""+userId+"\",");
					}
						//System.out.println(data);
						MongoDBConnector.insert(data);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage() + " (UserID, FriendID) : ("
						+ m_userId + ", " + userId + " )" );
				ex.printStackTrace();
			}

		}
	}

	
	private String connectFB(String rootName, String userId,
			String urlString, boolean writeFile, String method)
			throws Exception {
		HttpsURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		String content = null;
		String urlParams = null;

		URL serverAddress = null;

		try {
			if (method.equalsIgnoreCase("get")) {
				serverAddress = new URL("https://graph.facebook.com/"
						+ userId + "/" + urlString + "?access_token="
						+ m_authToken);
			} else if (method.equalsIgnoreCase("post")) {
				serverAddress = new URL("https://graph.facebook.com");
			}
			// set up communication
			connection = null;

			// Set up the initial connection
			connection = (HttpsURLConnection) serverAddress.openConnection();

			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.setReadTimeout(60000);

			if (method.equalsIgnoreCase("post")) {
				urlParams = "batch=" + URLEncoder.encode(urlString, "UTF-8")
						+ "&access_token="
						+ URLEncoder.encode(m_authToken, "UTF-8");
				wr = new OutputStreamWriter(connection.getOutputStream());
				wr.write(urlParams);
				wr.flush();
				wr.close();
			} 
			// read the result from the server
			rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			sb = new StringBuilder();

			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}

			content = sb.toString();

			if (writeFile) {
				System.out.println(m_path + File.separatorChar + "fb"
						+ File.separatorChar + rootName + "_" + userId
						+ ".json");
				Writer output = new BufferedWriter(new FileWriter(m_path
						+ File.separatorChar + "fb" + File.separatorChar
						+ rootName + "_" + userId + ".json"));
				output.write(content);
				output.close();

			}

		} catch (Exception e) {			
			System.out.println(e.getMessage() + " for UserID :" + m_userId);
			e.printStackTrace();
		} finally {
			// close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			sb = null;
			wr = null;
			connection = null;
		}

		return content;

	}

}
