package Indicators;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.PreparedStatement;

import FetchData.HeroKuApp;
import FetchData.JsonParser;
import FetchData.QuandlRest;

public class Test extends Connection{
	public static String inputFile = "C:/Puneeth/SHARE_MARKET/";
	
	public int createTable(java.sql.Connection dbConnection, String symbol) throws SQLException{
    	String sql="";
    	int tableExist=0;
    	try{
    		
        	DatabaseMetaData metadata = (DatabaseMetaData) dbConnection.getMetaData();
        	String[] types = {"TABLE"};
        	ResultSet resultSet = metadata.getTables(null, null, "%", types);
        	while (resultSet.next()) {
        		String tableName = resultSet.getString(3);
//        		System.out.println(tableName);
        		if(tableName.equalsIgnoreCase(symbol)){
        			tableExist = 1;
        			break;
        		}
        	}
        	
        	if (tableExist!=1){

            	sql =sql + "CREATE TABLE  "+symbol+""
            			+ "(TradeDate datetime NOT NULL, strike_price nvarchar(20) NOT NULL, Open nvarchar(10) NOT NULL,High nvarchar(10) NOT NULL,Low nvarchar(10) NOT NULL,Close nvarchar(10) NOT NULL, "
            			+ " contracts nvarchar(50) NOT NULL, Volume nvarchar(50) NOT NULL default '', OPEN_INTEREST nvarchar(50) NOT NULL default '', "
            			+ " Change_IN_OI nvarchar(50) default 0, ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            	System.out.println(sql);
            	executeSqlQuery(dbConnection, sql);
        	}
//        	if(resultSet!=null) resultSet.close();
        	//return tableExist;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
		return tableExist;
    	
    }

	
	public int alterTable(java.sql.Connection dbConnection, String symbol, String Ssql) throws SQLException{
    	String sql="";
    	int tableExist=0;
    	try{
    		
        	DatabaseMetaData metadata = (DatabaseMetaData) dbConnection.getMetaData();
        	String[] types = {"TABLE"};
        	ResultSet resultSet = metadata.getTables(null, null, "%", types);
        	while (resultSet.next()) {
        		String tableName = resultSet.getString(3);
//        		System.out.println(tableName);
        		if(tableName.equalsIgnoreCase(symbol)){
        			tableExist = 1;
        			break;
        		}
        	}
        	
        	if (tableExist==1){
            	executeSqlQuery(dbConnection, Ssql);
        	}
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
		return tableExist;
    	
    }

	
	public void CreateOrUpdateTimeStamp(java.sql.Connection dbConnection, String symbol, String date, String open, String high, String low, String close){
        
        try{
            	   	String query = "insert into "+symbol+"(tradedate, open, high, low, close) values('"+date+" 00:00:00'"+","+open+","+high+","+low+","+close+")";
            	   	System.out.println(query);
                    executeSqlQuery(dbConnection, query);
                    
        }
        catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	
	public static void stringToDom(String xmlSource, String symbol) 
	        throws SAXException, ParserConfigurationException, IOException, TransformerException {
	    // Parse the given input
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

	    // Write the parsed document to an xml file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);

	    StreamResult result =  new StreamResult(new File(inputFile+symbol+".xml"));
	    transformer.transform(source, result);
	}
	
	public void importDataToTable(java.sql.Connection dbConnection, String symbol) throws TransformerException, SQLException{
		try {
	    	  String xmlData="";
	    	  
//	    	  Test db = new Test();
	    	  if( createTable(dbConnection, symbol) ==1){
//		    		 return;
	    	  }
	    	 
//	    	 
//			
	         DocumentBuilderFactory dbFactory 
	            = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder;

	         dBuilder = dbFactory.newDocumentBuilder();

	         Document doc = dBuilder.parse(new InputSource(inputFile+symbol+".xml"));
	         doc.getDocumentElement().normalize();

	         XPath xPath =  XPathFactory.newInstance().newXPath();

	         String expression = "/quandl-response/dataset";	     
	         
	         NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
//	         System.out.println(nodeList.getLength());
	         for (int i = 0; i < nodeList.getLength(); i++) {
	        	 Node nNode = nodeList.item(i); 
//	        	 System.out.println(nNode.getNodeName());
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        		 Element eElement = (Element) nNode;
//	        		 System.out.println(eElement.getElementsByTagName("dataset-code").item(0).getTextContent());
	        	 }
	         }
	         int length=0;
	         List list = new ArrayList<String>();
	         nodeList = (NodeList) xPath.compile("/quandl-response/dataset/column-names/column-name").evaluate(doc, XPathConstants.NODESET);
	         
	         for (int i = 0; i < nodeList.getLength(); i++) {
	        	 Node nNode = nodeList.item(i); 
//	        	 System.out.println(nNode.getNodeName());
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        		 Element eElement = (Element) nNode;
	        		 list.add(i, eElement.getTextContent());
//	        		 System.out.println(list.get(i));
	        	 }
	         }
	         
	         int temp=0;
	         String date="";
	         String open="";
	         String high="";
	         String low="";
	         String close="";
	         List symbolList = new ArrayList<Symbol>();
	         nodeList = (NodeList) xPath.compile("/quandl-response/dataset/data/datum/datum").evaluate(doc, XPathConstants.NODESET);
//	         System.out.println(nodeList.getLength());
	         for (int i = 0; i < nodeList.getLength(); i++) {
	        	 Node nNode = nodeList.item(i); 
	        	 //temp=0;
	        	 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	        		 Element eElement = (Element) nNode;
	        		 list.add(i, eElement.getTextContent());
	        		 if (temp==8){
	        			 temp=0;
	        		 }
	        		 if(temp==0){
	        			 date = eElement.getTextContent();
	        		 }else if(temp==1){
	        			 open = eElement.getTextContent();
	        		 }else if(temp==2){
	        			 high = eElement.getTextContent();
	        		 }else if(temp==3){
	        			 low = eElement.getTextContent();
	        		 }else if(temp==5){
	        			 close = eElement.getTextContent();
	        		 }
	        		 if(!date.equals("") && !open.equals("") && !high.equals("") && !low.equals("") && !close.equals("")){
	        			 symbolList.add(new Symbol(date, open, high, low, close)) ;
	        			 
	        			 date="";open="";high="";low="";close="";
	        		 }
	        		 temp++;
	        		 
	        	 }
	         }
//	         System.out.println(symbolList.size());
	         for(int i = 0 ; i < symbolList.size() ; i++){
	        	 Symbol s= (Symbol)symbolList.get(i);
	        	 CreateOrUpdateTimeStamp(dbConnection, symbol, s.getDate(), s.getOpen(), s.getHigh(), s.getLow(), s.getClose());
//	        	 System.out.println(symbol);
//	        	 System.out.println(s.getClose());
	         }
	      } catch (ParserConfigurationException e) {
	         e.printStackTrace();
	      } catch (SAXException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (XPathExpressionException e) {
	         e.printStackTrace();
	      }
	}
   public static void main(String[] args) throws TransformerException, SQLException {
      Test t = new Test();
      PSAR psar = new PSAR();
      java.sql.Connection dbConnection = null;
      try{
    	  Connection con = new Connection();
    	  	dbConnection = con.getDbConnection();
    	  	ResultSet rs=null;
    	  	boolean updateForTodayAndNextDay=true; 
    	  	boolean updateForallDays=true;
    	  	 QuandlRest q = new QuandlRest();
    	  	 JsonParser j = new JsonParser();
    	  	HeroKuApp h = new HeroKuApp();
    	  	String xmlData=""; 
    	  	 String name="", commaList="",sql="";
    	  	 int check=0;int totalCount=0;int incr; int bulkSendCount;
    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s");
    	  	while (rs.next()){
    	  		name= rs.getString("s.name");
//    	  		name=name+"_1M";
//    	  		sql ="alter table "+name+" add column HA_OPEN varchar(50)";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column HA_HIGH varchar(50)";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column HA_LOW varchar(50)";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column HA_CLOSE varchar(50)";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column BollingerBW varchar(50) default null";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column BB_std_dev_20 varchar(50) default null";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column BB_middleBand_20 varchar(50) default null";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column BB_lowerBand_20 varchar(50) default null";
//    	  		con.executeSqlQuery(dbConnection, sql);
//    	  		sql ="alter table "+name+" add column BB_upperBand_20 varchar(50) default null";
//    	  		con.executeSqlQuery(dbConnection, sql);
    	  		System.out.println(name);
//    	  		sql ="CREATE TABLE  "+name+""
//            			+ "(TradeDate datetime NOT NULL, Open nvarchar(10) NOT NULL,High nvarchar(10) NOT NULL,Low nvarchar(10) NOT NULL,Close nvarchar(10) NOT NULL, "
//            			+ " PRIMARY KEY (`TradeDate`) ) ";
//    	  		sql = "delete from vedl where year(tradedate)='2018'"+ 
//" and month(tradedate)='3' and day(tradedate)='26'";
            	con.executeSqlQuery(dbConnection, sql);
    	  	}
      }
	  	catch(Exception e){
			e.printStackTrace();
		}
      finally{
  	  	if(dbConnection!=null)
				dbConnection.close();
      }
//      t.importDataToTable("ucobank");
   }

}