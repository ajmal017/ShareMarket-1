package Indicators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MonthlyData extends Connection{
	public static void main(String[] args) {
		MonthlyData weeklyData = new MonthlyData();
		weeklyData.getNames();
	}
	
	public void getNames(){
		Connection con = new Connection();
		java.sql.Connection dbConnection = null;
	  	dbConnection = con.getDbConnection();
	  	ResultSet rs=null;String name="";
	  	TemporaryTable tmp = new TemporaryTable();
	  	try{
	  		rs = con.executeSelectSqlQuery(dbConnection, "select name from symbols  order by id asc");
		  	while(rs.next()){
		  		name = rs.getString("name")+"";
		  		System.out.println(name);
		  		calculateMonthlyData(con, dbConnection, name);
		  	}
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	finally{
	  		if(rs !=null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	  	}
	  	
	}
	public void calculateMonthlyData(Connection con, java.sql.Connection dbConnection, String name) {
		String weekEnd="", sql="";ResultSet rs=null;
		Date date1=null;int year =0, month=0, day=0;String dayName="";
		String startDate="";
		sql = "select tradedate t, open,max(high*1) as high, min(low*1) as low, "+
				" (select close from "+name+" b where month(tradedate)=month(t) and year(tradedate)=year(t) order by tradedate desc limit 1) as close "+
				" from "+name+" group by month(tradedate), year(tradedate) "+
				" order by year(tradedate),month(tradedate) ";
		try{
			float open,high,low,close;
//			con.executeSqlQuery(dbConnection, "delete from "+name+"_1M");
			String date="";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			while (rs.next()){
				if(!rs.isLast()){
					open = rs.getFloat("open");
					high = rs.getFloat("high");
					low = rs.getFloat("low");
					close = rs.getFloat("close");
					date = rs.getString("t");
					sql = "insert into "+name+"_1M(tradedate,open,high,low,close) "
							+ " values ('"+date+"', "+open+","+high+","+low+","+close+")";
					con.executeSqlQuery(dbConnection, sql);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
}
