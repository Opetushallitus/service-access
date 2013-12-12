/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

public class SampleApp {

	private static final String PROPERTY_FILE = "sample-access-ticket-client.properties";
	
	private String clientId;
    private String clientSecret;
    private String serviceAccessURL;
	private String serviceURL;
	private String serviceQueryURL;
	
	public static void main(String[] args){
		try {
			new SampleApp().execute();
		} catch (Exception e) {
			System.out.println("Failed to execute SampleApp.");
			e.printStackTrace();
		}
	}
	
	public void execute() throws Exception{
		init();
		
		PrintStream out = System.out;
        
       
        HttpClient client = new HttpClient();
       
        // Get access ticket (access_token)
        PostMethod post = new PostMethod(serviceAccessURL+"/accessTicket");
        post.addParameter("client_id", clientId);
        post.addParameter("client_secret", clientSecret);
        post.addParameter("service_url", serviceURL);
        
        System.out.println("clientId:"+clientId);
        System.out.println("clientSecret:"+clientSecret);
        System.out.println("serviceURL:"+serviceURL);
        
        client.executeMethod(post);
        
        debugResponse(out, post, client);
        
        String ticket = StringUtils.trim( post.getResponseBodyAsString() );
        if(post.getStatusCode() != HttpServletResponse.SC_CREATED ||
        		StringUtils.isBlank(ticket)){
        	out.println("Failed to get ticket from :"+serviceAccessURL);
        	out.println("Response :"+ticket);
        	out.println("HTTP Status code :"+post.getStatusCode());
        	
        	return;
        }
        
        // Make service call with ticket.

        client = new HttpClient();
        GetMethod get = new GetMethod(serviceQueryURL);
        get.setRequestHeader("CasSecurityTicket", ticket);
        client.executeMethod(get);
        
        debugResponse(out, get, client);
	}
	
	private void init() throws Exception {
		
        // Read properties
		
		Properties prop = new Properties();
        
	    //load a properties file from class path, inside static method
	    prop.load(SampleApp.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
	    
	    clientId = prop.getProperty("client_id");
	    clientSecret = prop.getProperty("client_secret");
		serviceAccessURL = prop.getProperty("service-access-url");
		serviceQueryURL = prop.getProperty("sample-service-query");
		serviceURL = prop.getProperty("sample-service-url");
		
		System.out.println("clientId:"+clientId);
		System.out.println("serviceAccessURL:"+serviceAccessURL);
		System.out.println("serviceURL:"+serviceURL);
		System.out.println("serviceQueryURL:"+serviceQueryURL);
		
	}
	
	private void debugResponse(PrintStream out, HttpMethodBase method, HttpClient client) throws IOException{
		
		// Print http response
		
		String responseTxt = method.getResponseBodyAsString();
		 
		out.println("----\n\nStatus : "+method.getStatusCode());
		out.println("\nURI: "+method.getURI());
        out.println("\nResponse Path: "+method.getPath());
        out.println("\nRequest Headers: "+method.getRequestHeaders().length);
        for(Header h : method.getRequestHeaders()){
        	out.println("  "+h.getName()+" = "+h.getValue()); 
        }
        out.println("\nCookies: "+client.getState().getCookies().length);
        for(org.apache.commons.httpclient.Cookie c : client.getState().getCookies()){
        	out.println("  "+c.getName()+" = "+c.getValue()); 
        }
        out.println("Response Text: ");
        out.println(responseTxt);
	}
}