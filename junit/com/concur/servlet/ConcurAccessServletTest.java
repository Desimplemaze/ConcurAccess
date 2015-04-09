/**
 * 
 */
package com.concur.servlet;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author csam
 *
 */
public class ConcurAccessServletTest {
	private static final String userName = "chon.sam@gemini-systems.com";
	private static final String password = "concurbluemix";
	private static final String consumerKey = "iIoHSuWNHBwmN4wJQ146ZQ";
	
	
	
	
	@Test
	public void getReports(){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder objDocumentBuilder = null;
		Document doc = null;

		try {
			URL url = new URL(
					"https://www.concursolutions.com/api/expense/expensereport/v2.0/Reports/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// adding the headers
			conn.setRequestProperty("Accept", "application/xml");
			conn.setRequestProperty("Authorization", "OAuth "
					+ getTokenString());

			objDocumentBuilder = factory.newDocumentBuilder();
			doc = objDocumentBuilder.parse(conn.getInputStream());

			// closes the connection
			conn.disconnect();
						//expected, actual output
			assertEquals("ReportsList", doc.getFirstChild().getNodeName());
			
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	//below are helper methods
	private String getBase64(String userName, String password) {
		String userNamePassword = userName + ":" + password;
		return DatatypeConverter.printBase64Binary(userNamePassword.getBytes());
	}

	private String getTokenString() {
		String tokenString = "";

		// document builder for xml file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder builder = factory.newDocumentBuilder();
			URL url = new URL(
					"https://www.concursolutions.com/net2/oauth2/accesstoken.ashx");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// adding the headers
			conn.setRequestProperty("Accept", "application/xml");
			conn.setRequestProperty("Authorization",
					"Basic " + getBase64(userName, password));
			conn.setRequestProperty("X-ConsumerKey", consumerKey);
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			// building a document using the inputstream and builder
			Document doc = builder.parse(conn.getInputStream());

			// trying to access the token node value
			Node root = doc.getFirstChild();
			NodeList nodeList = root.getChildNodes();
			Node tokenNode = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				if ((nodeList.item(i).getNodeName()).equalsIgnoreCase("token")) {
					tokenNode = nodeList.item(i);
				}
			}
			// setting the token string value
			tokenString = tokenNode.getTextContent();
			conn.disconnect();// disconnecting the connection

		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		return tokenString;
	}

}
