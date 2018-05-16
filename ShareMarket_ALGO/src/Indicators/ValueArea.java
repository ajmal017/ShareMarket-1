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

public class ValueArea extends Connection{


   public static void main(String[] args)  {
	      Test t = new Test();
	      ValueArea superTrend = new ValueArea();
	      java.sql.Connection dbConnection = null;
	      boolean updateSymbolsTableData=true; boolean updateAllData=true;
	      try{
	    	  
	    	  Connection con = new Connection();
	    	  	dbConnection = con.getDbConnection();
	    	  	ResultSet rs=null;
	    	  	rs = con.executeSelectSqlQuery(dbConnection, "SELECT s.name FROM symbols s, margintables m where m.name=s.name  and volume > 100000000 order by volume desc limit 1");
	    	  	String name="";
	    	  	boolean updateForTodayAndNextDay=true; boolean updateForallDays=true;
	    	  	boolean updateResultTable=true;float diffPerc=0.20f;boolean isIntraDayData=false;
	    	  	String iter="1d";
	    	  	String path="C:/Puneeth/SHARE_MARKET/Hist_Data/Intraday/";
	    	  	while (rs.next()){
	    	  		name= rs.getString("name");
	    	  		if(!iter.equals("1d"))
	    	  			name =name+"_"+iter+"";
	    	  		
	    	  		System.out.println(name);	
	    	  		superTrend.LoadData(dbConnection, name, updateForTodayAndNextDay, updateForallDays, 
	    	  				isIntraDayData, path+"/adx/"+iter+"/");
//	    	  		superTrend.UpdateADXResults(dbConnection, name, updateSymbolsTableData, updateAllData, updateResultTable, 
//	    	  				diffPerc, isIntraDayData);
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
		   boolean isIntraDayData, String path) {
	   ResultSet rs=null;
	  	List open = new ArrayList<Float>();
	  	List high = new ArrayList<Float>();
	  	List low = new ArrayList<Float>();
	  	List close = new ArrayList<Float>();
	  	List date = new ArrayList<String>();
	  	List volume = new ArrayList<String>();
	  	try {
			rs = executeSelectSqlQuery(con, "SELECT open, high, low, close, tradedate, volume FROM "+name+"  order by tradedate;");
		  	while (rs.next()){
		  		open.add(rs.getFloat("open"));
		  		high.add(rs.getFloat("high"));
		  		low.add(rs.getFloat("low"));
		  		close.add(rs.getFloat("close"));
		  		date.add(rs.getString("tradedate"));
		  		volume.add(rs.getLong("volume"));
		  	}
//		  	System.out.println(date.size());
		  	calculateValueArea(con, name,open, high, low, close, date, volume, updateForTodayAndNextDay, updateForallDays, 
		  			isIntraDayData, path);
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
   
  
   
   
	void calculateValueArea(java.sql.Connection con, String name, List<Float> open, List<Float> high,  List<Float> low, 
			List<Float> close, List date, List<Integer> volume, boolean updateForTodayAndNextDay, boolean updateForallDays,
			boolean isIntraDayData, String path) throws IOException, SQLException{
		TemporaryTable tmp = new TemporaryTable();
		String sql="", tradeDate="";
		List openI = new ArrayList<Float>();
	  	List highI = new ArrayList<Float>();
	  	List lowI = new ArrayList<Float>();
	  	List closeI = new ArrayList<Float>();
	  	List dateI = new ArrayList<String>();
	  	List volumeI = new ArrayList<String>();
		BufferedWriter output = null;
		int iter=60;
	  	tmp.createTempTable(con, name);
	  	File file = new File(""+path+name+".txt");
        output = new BufferedWriter(new FileWriter(file));
        String dir="", direction="";
        ResultSet rs=null;
		for (int i=0; i<= date.size()-1; i++){
			openI.clear();highI.clear();lowI.clear();closeI.clear();dateI.clear();volumeI.clear();
	  			tradeDate = (String) date.get(i);
	  			sql="select coalesce(sum(volume),0) from "+name+"_"+iter+" where date(tradedate) = Date('"+date.get(i)+" ')";
//	  			System.out.println(sql);
	  			long sumOfVolume = Long.parseLong(executeCountQuery(con, sql));
	  			sql="select coalesce(max(volume),0) from "+name+"_"+iter+" where date(tradedate) = Date('"+date.get(i)+" ')";
	  			long max = Long.parseLong(executeCountQuery(con, sql));
//	  			System.out.println(sql);
	  			long perc_70=max*70/100, perc_70_t=0;
	  			float high_t=0f, low_t=0f;
	  			rs = executeSelectSqlQuery(con, "select * from "+name+"_"+iter+" where date(tradedate) = Date('"+date.get(i)+" ') ");
	  			while(rs.next()){
	  				openI.add(rs.getFloat("open"));
			  		highI.add(rs.getFloat("high"));
			  		lowI.add(rs.getFloat("low"));
			  		closeI.add(rs.getFloat("close"));
			  		dateI.add(rs.getString("tradedate"));
			  		volumeI.add(rs.getLong("volume"));
	  			}
	  			for (int j=0; j< dateI.size(); j++){
	  				
	  				if(max==(long)volumeI.get(j)  && j==dateI.size()-1){
	  					
	  					perc_70_t = max; high_t=(float)highI.get(j);low_t=(float)lowI.get(j);
	  					for (int k=1; k< 10; k++){
	  						perc_70_t = perc_70_t + (long)volumeI.get(j-k);
	  						if((float)highI.get(j-k) > high_t)  high_t =(float)highI.get(j-k);
	  						if((float)lowI.get(j-k) < low_t)  low_t =(float)lowI.get(j-k);
	  						if(perc_70_t>perc_70){
//	  							System.out.println(max+","+(long)volumeI.get(j)+","+dateI.get(j)+","+j+","+(dateI.size()-1)+","+high_t+","+low_t);
//	  							high_t=0; low_t=0;
	  							break;
	  						}
	  					}
	  				}
	  			}
	  			if(updateForallDays == true){
					output.write(tradeDate);output.write(","+high_t);	output.write(","+low_t);
                	output.write("\r\n");
				}
		}
		
		if ( output != null ) output.close();
		if(updateForallDays == true){
			sql = "LOAD DATA LOCAL INFILE '"+path+name+".txt' "+
					 " INTO TABLE "+name+"_Temp "+
					 " FIELDS TERMINATED BY ',' "+
					 " LINES TERMINATED BY '\n'" +
					 " "+
					 " (tradedate, S1, S2)";
           
          		executeSqlQuery(con, sql);
          		
          		sql = "UPDATE "+name+"_Temp b, "+name+" a"+
				" SET a.S1 = b.S1, a.S2 = b.S2 "+
				" WHERE a.tradedate = b.tradedate ";
//          		System.out.println(sql);
          		executeSqlQuery(con, sql);
          		tmp.dropTempTable(con, name);
		}
	}
			
}

