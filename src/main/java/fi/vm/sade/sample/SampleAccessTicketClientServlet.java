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

import java.io.*;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

/**
 * Tässä Esimerkissä:
 *
 * service-access palvelun host on <i>centosx</i>.
 * service-access palvelulta pyydetään accessTicket 
 * palveluun <i>https://itest-virkailija.oph.ware.fi:443/vtj-service</i>
 * Käyttäen cas palvelua joka on osoitteessa <i>https://itest-virkailija.oph.ware.fi/cas</i>
 *
 * Full service url: https://itest-virkailija.oph.ware.fi/vtj-service/resources/vtj/160162-9968
 *						
 *
 * @author Riku Karjalainen <riku.karjalainen@proactum.fi>
 *
 */
@WebServlet(urlPatterns={"/sample"} )
public class SampleAccessTicketClientServlet extends HttpServlet {
           
	private static final long serialVersionUID = 2276285147642980437L;

	private static final String PROPERTY_FILE = "sample-access-ticket-client.properties";
	
	private String clientId;
    private String clientSecret;
    private String serviceAccessURL;
	private String serviceURL;
	private String serviceQueryURL;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
        Properties prop = new Properties();
        
        try {
            //load a properties file from class path, inside static method
            prop.load(SampleAccessTicketClientServlet.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
            
            clientId = prop.getProperty("client_id");
            clientSecret = prop.getProperty("client_secret");
    		serviceAccessURL = prop.getProperty("service-access-url");
    		serviceQueryURL = prop.getProperty("sample-service-query");
    		serviceURL = prop.getProperty("sample-service-url");
        } catch (IOException ex) {
            new ServletException("Faild to load properties from '"+PROPERTY_FILE+"'.", ex);
        }

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        
        // Get access ticket (access_token)
        HttpClient client = new HttpClient();
        
        PostMethod post = new PostMethod(serviceAccessURL+"/accessTicket");
        post.addParameter("client_id", clientId);
        post.addParameter("client_secret", clientSecret);
        post.addParameter("service_url", serviceURL);
        
        client.executeMethod(post);
        
        printResponse(out, post, client);
        
        String responseTxt = post.getResponseBodyAsString();
        if(StringUtils.isBlank(responseTxt) || post.getStatusCode() != HttpServletResponse.SC_CREATED){
        	out.println("Failed to get ticket from :"+serviceAccessURL);
        	out.println("Response :"+responseTxt);
        	out.println("HTTP Status code :"+post.getStatusCode());
        	
        	return;
        }
        
        String ticket = responseTxt.trim();
        
        /*
          At this point we have HttpClient with JSESSION that is authenticated to service. 
          We also have valid access ticket (access_token) to service. 
          
          To make request to service
          a) We can use the same HttpClient instance that has valiad authenticated JSession to service.
          b) don't need to sent ticket with every request as long as the JSession is valid.
        
        */ 
        
        // Make without sending ticket -> We get, status 401
        // FIXME: CAS filtteri palauttaa 200 statuksen ja login sivun vaikka pitäisi tulla 403.
        GetMethod get = new GetMethod(serviceQueryURL);
        //client.executeMethod(get);
        //printResponse(out, get, client);
        
        // Make call with ticket as request parameter -> status 200
        get = new GetMethod(serviceQueryURL+"?ticket="+ticket);
        client.executeMethod(get);
        printResponse(out, get, client);
        
        /* At this point we should have HttpClient with Jsession that is authenticated to service. 
          Further calls can be made without ticket as long as Jsession is valid ad it's passed to server with cookie.  
        */
        
        // Make call with session -> status 200
        get = new GetMethod(serviceQueryURL);
        client.executeMethod(get);
        printResponse(out, get, client);
        
        // Ticket can be sent to service also as request header.
        // We recommend to set ticket as request header to every call.

        // Reset Client and make the same call with ticket as request header.
        //
        client = new HttpClient();
        get = new GetMethod(serviceQueryURL);
        get.setRequestHeader("CasSecurityTicket", ticket);
        client.executeMethod(get);
        printResponse(out, get, client);
        
        get = new GetMethod(serviceQueryURL);// FIXME: CAS filtteri käyttää sittenkin tätä eikä sessiota. Plaah!!!
        get.setRequestHeader("CasSecurityTicket", "laa laaa laa, tällä ei ole väliä koska HttpClient:in sessio on voimassa.");
        client.executeMethod(get);
        printResponse(out, get, client);
	}
 
	private void printResponse(PrintWriter out, HttpMethodBase method, HttpClient client) throws IOException{
		
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request,response);
    }

}
