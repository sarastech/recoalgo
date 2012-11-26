package com.pingpong.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FBDataServlet
 */

public class FBDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int THREAD_POOL_SIZE = 25;
	private ExecutorService processor;
       
    public FBDataServlet() {
        super();
        init();
        // TODO Auto-generated constructor stub
    }
    
    public void init() {
        processor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    public void destroy() {
        processor.shutdownNow();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("doGet");
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		final String action = request.getParameter("action");
		final String userId = request.getParameter("userid");
		final String token = request.getParameter("authtoken");
		
		if(action != null && action.length() != 0 && userId != null && userId.length() != 0){
			response.getOutputStream().println("Status OK.");
		} else
		{
			response.getOutputStream().println("ERROR. Incorrect URLString - missing action and userId tags.");
			return ;
		}
		
		System.out.println("doPost");
		
		processor.execute(new Runnable() {public void run() {try {
			process(action, userId, token);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}});
	}
	
	
	private void process(String action, String userId, String token) throws ServletException, IOException {
		String path = "/home/kripaks/workspace/fbdataservice";//getServletContext().getRealPath(File.pathSeparator);
		try{
			System.out.println("Action ="+action);
			System.out.println("UserId ="+userId);
			System.out.println("AuthToken ="+token);
			if(action != null && action.length() != 0 && userId != null && userId.length() != 0){
				userId = userId.toLowerCase();
				if(action.equalsIgnoreCase("collect")){
					FBDataCollector fb = new FBDataCollector(path, userId, token);
					fb.getData();
				}
			}
			else{
				System.out.println("Invalid URL parameters.");
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
