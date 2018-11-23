package Indicators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

public class Camarilla extends Connection{

	static boolean isCalculateForIndex = false;
   public static void main(String[] args)  {
	      Test t = new Test();
	      Camarilla obv = new Camarilla();
	      java.sql.Connection dbConnection = null;
	      
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s where ismargin=1 ");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=false; boolean updateForallDays=true;
	    	  	boolean updateResultTable=false;float diffPerc=0.20f;
	    	  	boolean insertAllDataToResult = true;
	    	  	
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/oldLaptop/puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	if(!isCalculateForIndex){
	    	  		while (rs.next()){
		    	  		name= rs.getString("name");
		    	  		if(!iter.equals("1d"))
		    	  			name =name+"_"+iter+"";
		    	  		System.out.println(name);	
		    	  		obv.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, path+iter+"/camarilla/");
		    	  	}
	    	  	}else{
	    	  		obv.LoadData(dbConnection, "NIFTY_50", updateForTodayAndNextDay, updateForallDays, path+iter+"/camarilla/");
	    	  	}
	    	  	
	      }
		  	
	      catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      catch(Exception e){
				e.printStackTrace();
			}
	      finally{
	  	  	if(dbConnection!=null)
				try {
					dbConnection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	      }
   }
   
   
   
   public void LoadData(java.sql.Connection con, String name, boolean updateForTodayAndNextDay, boolean updateForallDays,
		   String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	try {
	  		rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate FROM `"+name+"` ");
			
		 
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  	}
//		  	System.out.println(date.size());
		  	calculateCamarilla(con, name,open, high, low, close, date, updateForTodayAndNextDay, updateForallDays, 
		  			path);
		  	if(rs!=null) rs.close();
	  	}
	  	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  	catch(Exception e){
	    	  e.printStackTrace();
	    }
   }
   
  
   
   
	void calculateCamarilla(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, boolean updateForTodayAndNextDay, boolean updateForallDays,
			String path) throws IOException, SQLException{
		
		long obv_prev=0, obv=0;
		
		TemporaryTable tmp = new TemporaryTable();
		float h1 = 0, h2=0,h3=0,h4=0,h5=0,h6=0;
		float l1=0, l2=0,l3=0,l4=0,l5=0,l6=0;
		String sql="", tradeDate="";
		
		
		BufferedWriter output = null;
		if(updateForallDays == true){	
			tmp.createTempTable(con, name);
		  	File file = new File(""+path+name+".txt");
	        output = new BufferedWriter(new FileWriter(file));
		}
	  	
        
        String todaysDate = executeCountQuery(con, "select date(now())");
		for (int i=0; i<= date.size()-1; i++){
	  		tradeDate = (String) date.get(i);
			
			if(i==0){
				
			}else if(i>0){
				float diff = high.get(i-1)-low.get(i-1);
				h1 =  (float) ((0.0916*diff)+close.get(i-1));
				h2 =  (float) ((0.183*diff)+close.get(i-1));
				h3 =  (float) ((0.275*diff)+close.get(i-1));
				h4 =  (float) ((0.555*diff)+close.get(i-1));
				h5 =  (float) ((0.8244*diff)+close.get(i-1));
				h6 =  (float) ((1.0076*diff)+close.get(i-1));
				
				l1 =  (float) (close.get(i-1)-(0.0916*diff));
				l2 =  (float) (close.get(i-1)-(0.183*diff));
				l3 =  (float) (close.get(i-1)-(0.275*diff));
				l4 =  (float) (close.get(i-1)-(0.55*diff));
				l5 =  (float) (close.get(i-1)-(0.8244*diff));
				l6 =  (float) (close.get(i-1)-(1.0992*diff));
			}
			if(updateForTodayAndNextDay == true && i== (date.size()-1)){
				output.write(tradeDate);	
				output.write(","+h1);output.write(","+h2);output.write(","+h3);output.write(","+h4);output.write(","+h5);output.write(","+h6);
				output.write(","+l1);output.write(","+l2);output.write(","+l3);output.write(","+l4);output.write(","+l5);output.write(","+l6);
            	output.write("\r\n");
			}else if(updateForallDays == true){
				output.write(tradeDate);	
				output.write(","+h1);output.write(","+h2);output.write(","+h3);output.write(","+h4);output.write(","+h5);output.write(","+h6);
				output.write(","+l1);output.write(","+l2);output.write(","+l3);output.write(","+l4);output.write(","+l5);output.write(","+l6);
            	output.write("\r\n");
			}
			if(updateForTodayAndNextDay == true && (tradeDate.split(" ")[0]).equalsIgnoreCase(todaysDate)){
				output.write(tradeDate);	
				output.write(","+h1);output.write(","+h2);output.write(","+h3);output.write(","+h4);output.write(","+h5);output.write(","+h6);
				output.write(","+l1);output.write(","+l2);output.write(","+l3);output.write(","+l4);output.write(","+l5);output.write(","+l6);
            	output.write("\r\n");
			}
		}
		if ( output != null ) output.close();
		
		if(updateForallDays == true ){
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, Camarilla_h1,Camarilla_h2,Camarilla_h3,Camarilla_h4,Camarilla_h5,Camarilla_h6,"
					 + "Camarilla_l1,Camarilla_l2,Camarilla_l3,Camarilla_l4,Camarilla_l5,Camarilla_l6) ";
          
         		executeSqlQuery(con, sql);
         		
         		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.Camarilla_h1 = b.Camarilla_h1*1,a.Camarilla_h2 = b.Camarilla_h2*1,a.Camarilla_h3 = b.Camarilla_h3*1,"
				+ "a.Camarilla_h4 = b.Camarilla_h4*1,a.Camarilla_h5 = b.Camarilla_h5*1,a.Camarilla_h6 = b.Camarilla_h6*1,"
				+ " a.Camarilla_l1 = b.Camarilla_l1*1,a.Camarilla_l2 = b.Camarilla_l2*1,a.Camarilla_l3 = b.Camarilla_l3*1,"
				+ "a.Camarilla_l4 = b.Camarilla_l4*1,a.Camarilla_l5 = b.Camarilla_l5*1,a.Camarilla_l6 = b.Camarilla_l6*1 "+
				" WHERE a.tradedate = b.tradedate ";
//         		System.out.println(sql);
         		executeSqlQuery(con, sql);
         		tmp.dropTempTable(con, name);
		}
		if(isCalculateForIndex){
			System.exit(0);
		}
			
			
		
	}
			
}

