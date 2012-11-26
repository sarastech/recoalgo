package com.pingpong.servlet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FBAggregator {
	
	
	//String[] m_urls = {"feed","likes","movies","music","books","notes","photos","albums","videos","videos/uploaded","events","groups","checkins","locations"};
	String[] m_meurls = {"feed","statuses","likes","movies","music","books","photos","videos","events","checkins","locations","friendlists","groups"};
	String[] m_urls = {"feed","likes","movies","music","books","photos","videos","events","checkins","locations"};
	String m_authToken = null;
	String m_username = "Raja";
	String m_path = null;
	
	public FBAggregator(String path, String authToken, String name){
		m_path = path;
		m_path = "/Users/Raja/Documents/Android/workspace/fbdataservice";
		m_authToken = authToken;
		m_username = name;
	}
	
	
	public void getData()throws Exception{
		long start = System.currentTimeMillis();
		System.out.println("Starting time = "+new Date(start));
		String friendId = "me";
		String friendName = "";
		int count = 0;
		int urlLength = m_urls.length;
		//String token = "AAACEdEose0cBAHJFv6dYiabqJFuVyt9y5GQXkvWjmzqjubEIlxpZA2VZC0aEss5aKsD2dJe7lHCZA0MOF1CgqHPHmZAopZA8ZC8PECyfUs1gZDZD";
		
		HashMap friendsMap = getFriendsList("myname");
		//System.out.println("No of friends = "+friendsMap.size());
		JSONArray array = new JSONArray();
		JSONObject req = new JSONObject();
		//req.put("method", "GET");
		//req.put("relative_url", "me");
		//array.add(req);
		
		for(int j=0;j<m_meurls.length;j++){
			req = new JSONObject();
			req.put("method", "GET");
			req.put("relative_url", "me/"+m_meurls[j]);
			array.add(req);
		}
		//System.out.println(array.toString());
		//System.out.println(array.toJSONString());
		String content = connectFB(m_username, friendId, friendName, array.toString(), false, "POST");
		
		splitMeJSON(content);
		
		//getGroupMembers(content, token);
		//getFriendListMembers(content, token);
		
		Iterator keySet = friendsMap.keySet().iterator();
		while(keySet.hasNext()){
			friendId = (String)keySet.next();
			friendName = (String)friendsMap.get(friendId);
			array.clear();
			for(int j=0;j<urlLength;j++){
				req = new JSONObject();
				req.put("method", "GET");
				req.put("relative_url", friendId+"/"+m_urls[j]);
				array.add(req);
			}
			
			//System.out.println(array.toString());
			content = connectFB(m_username, friendId, friendName, array.toString(), false, "POST");
			//System.out.println(content);
			if(content != null){
				splitFriendJSON(friendId, content);
			}
			System.out.println(++count);
			//break;
			
		}
		
		//Iterator keySet = friendsMap.keySet().iterator();
		
		/*while(keySet.hasNext()){
			friendId = (String)keySet.next();
			friendName = (String)friendsMap.get(friendId);
			for(int j=0;j<urlLength;j++){
				connectFB("me", friendId, friendName, m_urls[j], token, true);
			}
		}*/
		long end = System.currentTimeMillis();
		System.out.println("Ending time = "+new Date(end));
		long taken = end  - start;
		System.out.println("Time taken = "+taken/1000);
		
		MongoDBConnector.retrieveData(m_username);
		
	}
	
	public String constructBatchURL(){
		JSONArray array = new JSONArray();
		
		JSONObject req = new JSONObject();
		req.put("method", "GET");
		req.put("relative_url", "me");
		array.add(req);
		req.put("method", "GET");
		req.put("relative_url", "me/friends");
		array.add(req);
		//System.out.println(array.toJSONString());
		String str = "[{\"method\":\"GET\", \"relative_url\":\"me\"},{\"method\":\"GET\", \"relative_url\":\"me/friends\"}]";
		return str;
	}
	
	private HashMap getFriendsList(String name)throws Exception{
		HashMap map = null;
		
		String content = connectFB(m_username, "me", name, "friends", false, "GET");
		map = parse(content);
		
		return map;
	}
	
	private void splitMeJSON(String content)throws Exception{
		
		String data = null;
		JSONObject item = null;
		JSONParser parser1 = null;
		JSONArray array = new JSONArray();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		JSONArray objJson = (JSONArray)obj;
		
		for(int i=0;i<m_meurls.length;i++){
			item = (JSONObject)objJson.get(i);
			if(item == null){
				continue;
			}
			data = (String)item.get("body");
			if(data == null){
				continue;
			}
			parser1 = new JSONParser();
			Object obj1 = parser1.parse(data);
			JSONObject dataObj = (JSONObject)obj1;
			if(dataObj != null){
				array = (JSONArray)dataObj.get("data");
				if(array != null && array.size() != 0){
					data = data.replaceFirst("data", m_meurls[i]);
					MongoDBConnector.insert(data, m_username);
				}
			}
			
		}
	}
	
	private void splitFriendJSON(String friendId, String content)throws Exception{
		
		String data = null;
		JSONObject item = null;
		JSONParser parser1 = null;
		JSONArray array = new JSONArray();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		JSONArray objJson = (JSONArray)obj;
		
		for(int i=0;i<m_urls.length;i++){
			item = (JSONObject)objJson.get(i);
			if(item == null){
				continue;
			}
			data = (String)item.get("body");
			if(data == null){
				continue;
			}
			
			parser1 = new JSONParser();
			Object obj1 = parser1.parse(data);
			JSONObject dataObj = (JSONObject)obj1;
			if(dataObj != null){
			array = (JSONArray)dataObj.get("data");
				if(array != null && array.size() != 0){
					data = data.replaceFirst("data", friendId+"_"+m_urls[i]);
					MongoDBConnector.insert(data, m_username);
				}
			}
			
		}
	}
	
	
	private void getGroupMembers(String content, String authToken)throws Exception{
		String id = null;
		String name = null;
		JSONObject req = null;
		JSONArray array = new JSONArray();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		//System.out.println(obj);
		JSONArray objJson = (JSONArray)obj;
		
		//JSONArray jsonList = (JSONArray)objJson.get("data");
		//for(int i=0;i<jsonList.size();i++){
		JSONObject item = (JSONObject)objJson.get(12);
		String data = (String)item.get("body");
		
		JSONParser parser1 = new JSONParser();
		Object obj1 = parser1.parse(data);
		JSONObject dataObj = (JSONObject)obj1;
		JSONArray groupList = (JSONArray)dataObj.get("data");
		for(int i=0;i<groupList.size();i++){
			item = (JSONObject)groupList.get(i);
			id = (String)item.get("id");
			name = (String)item.get("name");
			
			req = new JSONObject();
			req.put("method", "GET");
			req.put("relative_url", id+"/members");
			array.add(req);
			req.put("method", "GET");
			req.put("relative_url", id+"/feed");
			array.add(req);
			
		}
		//System.out.println(array.toString());
		connectFBGroup(m_username, array.toString(), authToken, "group");
		//connectFBGroup(m_username, id+"/members", authToken, "member_"+name);
		//connectFBGroup(m_username, id+"/feed", authToken, "feed_"+name);
	}
	
	
	private void getFriendListMembers(String content, String authToken)throws Exception{
		String id = null;
		String name = null;
		JSONObject req = null;
		JSONArray array = new JSONArray();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		//System.out.println(obj);
		JSONArray objJson = (JSONArray)obj;
		
		//JSONArray jsonList = (JSONArray)objJson.get("data");
		//for(int i=0;i<jsonList.size();i++){
		JSONObject item = (JSONObject)objJson.get(11);
		String data = (String)item.get("body");
		
		JSONParser parser1 = new JSONParser();
		Object obj1 = parser1.parse(data);
		JSONObject dataObj = (JSONObject)obj1;
		JSONArray groupList = (JSONArray)dataObj.get("data");
		for(int i=0;i<groupList.size();i++){
			item = (JSONObject)groupList.get(i);
			id = (String)item.get("id");
			name = (String)item.get("name");
			System.out.println(name);
			req = new JSONObject();
			req.put("method", "GET");
			req.put("relative_url", id+"/members");
			array.add(req);
		}
		//System.out.println(array.toString());
		connectFBGroup(m_username, array.toString(), authToken, "friendlist");
	}
	
	private String connectFB(String rootName, String friendId, String friendName, String urlString, boolean writeFile, String method)throws Exception{
		HttpsURLConnection connection = null;
	      OutputStreamWriter wr = null;
	      BufferedReader rd  = null;
	      StringBuilder sb = null;
	      String line = null;
	      String content = null;
	      String urlParams = null;
	    
	      URL serverAddress = null;
	      //System.out.println("Starting time => HTTP= "+new Date(System.currentTimeMillis()));
	    
	      try {
	    	  if(method.equalsIgnoreCase("get")){
	    		  serverAddress = new URL("https://graph.facebook.com/"+friendId+"/"+urlString+"?access_token="+m_authToken);
	    	  }
	    	  else if(method.equalsIgnoreCase("post")){
	    		  serverAddress = new URL("https://graph.facebook.com");
	    	  }
	          //set up out communications stuff
	          connection = null;
	        
	          //Set up the initial connection
	          connection = (HttpsURLConnection)serverAddress.openConnection();
	          
	          connection.setRequestMethod(method);
	          connection.setDoOutput(true);
	          connection.setReadTimeout(10000);
	          //String batchURL = constructBatchURL();
	          
	          //String urlParams = "batch="+URLEncoder.encode("[{\"method\":\"GET\", \"relative_url\":\"me\"},{\"method\":\"GET\", \"relative_url\":\"me/friends\"}]", "UTF-8")+"&access_token="+URLEncoder.encode(authToken, "UTF-8");
	          if(method.equalsIgnoreCase("post")){
	        	  urlParams = "batch="+URLEncoder.encode(urlString, "UTF-8")+"&access_token="+URLEncoder.encode(m_authToken, "UTF-8");
	        	  wr = new OutputStreamWriter(connection.getOutputStream());
		          wr.write(urlParams);
		          wr.flush();
		          wr.close();
	          }
	          else if(method.equalsIgnoreCase("post")){
	        	  connection.connect();
	          }
	          //connection.connect();
	        
	          //get the output stream writer and write the output to the server
	          //not needed in this example
	          
	        
	          //read the result from the server
	          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          sb = new StringBuilder();
	        
	          while ((line = rd.readLine()) != null)
	          {
	              sb.append(line + '\n');
	          }
	          
	          content = sb.toString();
	          //System.out.println(content);
	          //System.out.println("Ending time => HTTP= "+new Date(System.currentTimeMillis()));
	          //parse(sb.toString());
	          
	          //BufferedReader input =  new BufferedReader(new FileReader(friendId+"_"+urlString+".json"));
	          
	          if(writeFile){
	        	  //System.out.println("Starting time => Writing into file = "+new Date(System.currentTimeMillis()));
	        	  //urlString = urlString.replace('/', '_');
	        	  
		          //Writer output = new BufferedWriter(new FileWriter(rootName+"_"+friendName+"_"+friendId+".json"));
	        	  System.out.println(m_path+File.separatorChar+"fb"+File.separatorChar+rootName+"_"+friendId+".json");
	        	  Writer output = new BufferedWriter(new FileWriter(m_path+File.separatorChar+"fb"+File.separatorChar+rootName+"_"+friendId+".json"));
		          output.write(content);
		          output.close();
		          //System.out.println("Ending time => Writing into file = "+new Date(System.currentTimeMillis()));
	          }
	                    
	      } catch (MalformedURLException e) {
	          e.printStackTrace();
	      } catch (ProtocolException e) {
	          e.printStackTrace();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      finally
	      {
	          //close the connection, set all objects to null
	          connection.disconnect();
	          rd = null;
	          sb = null;
	          wr = null;
	          connection = null;
	      }	
	      
	      return content;

	}
	
	private String connectFBGroup(String rootName, String urlString, String authToken, String name)throws Exception{
		HttpsURLConnection connection = null;
	      OutputStreamWriter wr = null;
	      BufferedReader rd  = null;
	      StringBuilder sb = null;
	      String line = null;
	      String content = null;
	      String urlParams = null;
	    
	      URL serverAddress = null;
	      //System.out.println("Starting time => HTTP= "+new Date(System.currentTimeMillis()));
	    
	      try {
	    	  
	    	  serverAddress = new URL("https://graph.facebook.com");
	    	  
	          //set up out communications stuff
	          connection = null;
	        
	          //Set up the initial connection
	          connection = (HttpsURLConnection)serverAddress.openConnection();
	          
	          connection.setRequestMethod("POST");
	          connection.setDoOutput(true);
	          connection.setReadTimeout(10000);
	          
	          urlParams = "batch="+URLEncoder.encode(urlString, "UTF-8")+"&access_token="+URLEncoder.encode(authToken, "UTF-8");
        	  wr = new OutputStreamWriter(connection.getOutputStream());
	          wr.write(urlParams);
	          wr.flush();
	          wr.close();
	          
	          //connection.connect();
	          //read the result from the server
	          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          sb = new StringBuilder();
	        
	          while ((line = rd.readLine()) != null)
	          {
	              sb.append(line + '\n');
	          }
	          
	          content = sb.toString();
	        	Writer output = new BufferedWriter(new FileWriter(m_path+File.separatorChar+"fb"+File.separatorChar+rootName+"_"+name+".json"));
		          output.write(sb.toString());
		          output.close();
		          //System.out.println("Ending time => Writing into file = "+new Date(System.currentTimeMillis()));
	          
	                    
	      } catch (MalformedURLException e) {
	          e.printStackTrace();
	      } catch (ProtocolException e) {
	          e.printStackTrace();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      finally
	      {
	          //close the connection, set all objects to null
	          connection.disconnect();
	          rd = null;
	          sb = null;
	          wr = null;
	          connection = null;
	      }	
	      
	      return content;

	}

	
	private HashMap parse(String content)throws Exception{
		JSONObject item = null;
		String id = null;
		List list = new ArrayList();
		HashMap<String, String> map = new HashMap<String, String>();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(content);
		//System.out.println(obj);
		JSONObject objJson = (JSONObject)obj;
		JSONArray jsonList = (JSONArray)objJson.get("data");
		for(int i=0;i<jsonList.size();i++){
			item = (JSONObject)jsonList.get(i);
			id = (String)item.get("id");
			list.add(id);
			map.put(id, (String)item.get("name"));
		}
		
		return map;
   }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//String token = "AAACEdEose0cBAFIBbbslZBDXVgcSXsx5lzvrHf3Eg8TLF5dVToCGZCU36XCjA79zrjZC26T7MXiIsKr35EkDdaKMq4pcrgq9uNW7P7ZCcwZDZD";
		// TODO Auto-generated method stub
		//FBAggregator agg = new FBAggregator("");
		//agg.getData();
		//agg.construct();
		//agg.connectFB("","","","",token, false);
		//agg.constructBatchURL();
	}

}
