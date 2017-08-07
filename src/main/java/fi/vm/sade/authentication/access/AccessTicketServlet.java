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

package fi.vm.sade.authentication.access;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.authentication.cas.CasClient;
import fi.vm.sade.properties.OphProperties;

 
/**
 * accessTicket servet tekee CAS kirjautumisen ja siihen liittyvät toimenpiteet käyttäjän 
 * puolesta tiettyyn palveluun ja palauttaa käyttäjällä accessTicketin jonka avulla voidaan 
 * käyttää tuota määrättyä palvelua. 
 *						
 *
 * @author Riku Karjalainen <riku.karjalainen@proactum.fi>
 *
 */
@WebServlet(urlPatterns={"/accessTicket"} )
public class AccessTicketServlet extends HttpServlet {
           
	private static final long serialVersionUID = 258379666492210097L;

	private static final Logger logger = LoggerFactory.getLogger(AccessTicketServlet.class);
	
	private String casURL;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
                OphProperties ophProperties = new ServiceAccessOphProperties();
                casURL = ophProperties.url("cas.v1.tickets");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doIt(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doIt(request, response);
	}
	
	protected void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 try{
			 justDoIt(request, response);
			 
		 }catch(Exception e){
			 throw new ServletException("Failed to get ticket.", e);
		 }
	}
	
	protected void justDoIt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PrintWriter out = response.getWriter();

        String user = request.getParameter("client_id");
        String pass = request.getParameter("client_secret");
        String serviceUrl = request.getParameter("service_url");
                
        logger.debug("casURL: {}",casURL);
        logger.debug("user: {}",user);
        logger.debug("serviceUrl: {}",serviceUrl);
        String serviceTicket = CasClient.getTicket(casURL,
															user, pass, 
															serviceUrl + "/j_spring_cas_security_check");
        
		if(serviceTicket == null){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        out.println("Failed to get Ticket with CasClient. Parameters = "+
			" User:'"+user+"'"+", ServiceUrl:'"+serviceUrl+"'"+", CasHost:'"+casURL+"'");	
		}else{
			response.setStatus(HttpServletResponse.SC_CREATED);
	        out.println(serviceTicket);	
		}
    }

}
