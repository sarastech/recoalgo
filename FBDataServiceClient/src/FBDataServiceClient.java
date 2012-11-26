import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;


public class FBDataServiceClient {

	public String fetchFBData(String userId, String authToken, String action) throws Exception {
		HttpURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		String content = null;
		String urlParams = null;
		URL serverAddress = null;

		try {
			serverAddress = new URL("http://localhost:8080/fbdataservice/set");
			
			// set up out communications stuff
			//connection = null;

			// Set up the initial connection
			connection = (HttpURLConnection) serverAddress.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            
			String urlString = "action="+URLEncoder.encode(action, "UTF-8")+"&userid="+URLEncoder.encode(userId, "UTF-8");
			if(action.equalsIgnoreCase("collect")){
				urlString = urlString + "&authtoken="+URLEncoder.encode(authToken, "UTF-8");
			}
			
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlString.getBytes().length));
			wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(urlString);
			wr.flush();
			wr.close();
			
			// read the result from the server
			rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			sb = new StringBuilder();
			
			if(rd != null){					
				content = rd.readLine().toString();
				System.out.println(content);
			}

		} catch (Exception e) {
			//return null;
			e.printStackTrace();
			
		}  finally {
			// close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			sb = null;
			wr = null;
			connection = null;
		}

		return content;

	}
	
	public static void main(String[] arg)throws Exception{
		String userId= "657673551";
		String authToken = "AAACEdEose0cBACWjLpR1wSlrJJ0hxNJEagcabYNm2WZC6UHYKq5fl6rEnPrsMpXX0O1u8iXbJRCx4SOf6crki6ZBNZAv1dMenmuEXdCyQZDZD";
		
		String action = "collect";
		
		FBDataServiceClient client = new FBDataServiceClient();
		client.fetchFBData(userId, authToken, "collect");
		
	}

}
