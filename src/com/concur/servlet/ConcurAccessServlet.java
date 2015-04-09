package com.concur.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.bind.DatatypeConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.concur.classes.ReportSummary;

/**
 * Servlet implementation class ConcurAccessServlet
 */
public class ConcurAccessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String userName = "chon.sam@gemini-systems.com";
	private static final String password = "concurbluemix";
	private static final String consumerKey = "iIoHSuWNHBwmN4wJQ146ZQ";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConcurAccessServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<p>"+getTokenString()+"</p>");
		
		
		// document builder for xml file
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

			// prints a really long string of the xml file.
			//out.println(getResultFromConn(conn.getInputStream()));
			
			objDocumentBuilder = factory.newDocumentBuilder();
			
			//****************** for some reason using conn here will throw an exception of saying premature end of file.****************
			doc = objDocumentBuilder.parse(conn.getInputStream());
			out.print(doc.getFirstChild());
//			Connection dconn = getConnection();
//			runQuery(dconn, doc);

			// closes the connection
			conn.disconnect();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}

	}

	/**
	 * this method will get the token string from the user
	 * 
	 * @return Access token string
	 */
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

	/**
	 * Encodes the username and password in the format username:password
	 * 
	 * @param userName
	 * @param password
	 * @return encoded username:password
	 */
	private String getBase64(String userName, String password) {
		String userNamePassword = userName + ":" + password;
		return DatatypeConverter.printBase64Binary(userNamePassword.getBytes());
	}

	/**
	 * takes the conn input stream and conerts it to a string to be diplayed
	 * 
	 * @param is
	 * @return xml in string form
	 */
	private String getResultFromConn(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String aux = "";

		try {
			while ((aux = reader.readLine()) != null) {
				sb.append(aux);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

	private Connection getConnection() throws SQLException {

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);
		// connect to database:
		// conn = DriverManager.getConnection();
		System.out.println("Connected to database");
		return conn;
	}

	private void runQuery(Connection conn, Document doc) throws SQLException {
		LinkedList<ReportSummary> rs = parseDocument(doc);
		System.out.println("Creating statements...");
		for (int i = 0; i < rs.size(); i++) {
			ReportSummary s = rs.get(i);
			Statement stmt = conn.createStatement();
			String sql = "INSERT INTO table VALUES (" + ", " + ", " + ")";
			stmt.executeUpdate(sql);

		}
	}

	private LinkedList<ReportSummary> parseDocument(Document dom) {
		LinkedList<ReportSummary> rs = new LinkedList<ReportSummary>();
		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("ReportSummary");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the employee element
				Element el = (Element) nl.item(i);

				// get the Employee object
				ReportSummary e = getSum(el);

				// add it to list
				rs.add(e);
			}
		}
		return rs;
	}

	private ReportSummary getSum(Element repEl) {
		String reportName;
		String reportID;
		String reportCurrency;
		String reportDate;
		String lastComment;
		String approvalstatus;
		String reportDetails;
		String expenseUserLoginID;
		String approverLoginID;
		String employeeName;
		String paymentStatus;
		int reportTotal;

		// for each <employee> element get text or int values of
		// name ,id, age and name
		reportName = getTextValue(repEl, "ReportName");
		reportID = getTextValue(repEl, "ReportId");
		reportCurrency = getTextValue(repEl, "ReportCurrency");
		reportDate = getTextValue(repEl, "ReportDate");
		lastComment = getTextValue(repEl, "LastComment");
		approvalstatus = getTextValue(repEl, "ApprovalStatus");
		reportDetails = getTextValue(repEl, "ReportDetailsURL");
		expenseUserLoginID = getTextValue(repEl, "ExpenseUserLoginID");
		approverLoginID = getTextValue(repEl, "ApproverLoginID");
		employeeName = getTextValue(repEl, "EmployeeName");
		paymentStatus = getTextValue(repEl, "PaymentStatus");
		reportTotal = getIntValue(repEl, "ReportTotal");

		// Create a new Employee with the value read from the xml nodes
		ReportSummary rs = new ReportSummary(reportName, reportID,
				reportCurrency, reportTotal, reportDate, lastComment,
				approvalstatus, reportDetails, expenseUserLoginID,
				approverLoginID, employeeName, paymentStatus);

		return rs;
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(Element ele, String tagName) {
		// in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele, tagName));
	}
}
