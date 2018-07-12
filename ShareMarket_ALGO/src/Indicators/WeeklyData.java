package Indicators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class WeeklyData extends Connection{
	public static void main(String[] args) {
		Date date1 = (new GregorianCalendar(2017, Calendar.JANUARY, 30)).getTime();
		WeeklyData weeklyData = new WeeklyData();
		System.out.println(new SimpleDateFormat("EEEE").format(date1));
		weeklyData.getNames();
	}
	
	public void getNames(){
		Connection con = new Connection();
		java.sql.Connection dbConnection = null;
	  	dbConnection = con.getDbConnection();
	  	ResultSet rs=null;String name="";
	  	TemporaryTable tmp = new TemporaryTable();
	  	try{
	  		rs = con.executeSelectSqlQuery(dbConnection, "select name from symbols where volume > 5000000 order by id asc");
		  	while(rs.next()){
		  		name = rs.getString("name")+"";
		  		System.out.println(name);
		  		
		  		calculateWeeklyData(con, dbConnection, name);
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
	public void calculateWeeklyData(Connection con, java.sql.Connection dbConnection, String name) {
		String weekEnd="", sql="";ResultSet rs=null;
		Date date1=null;int year =0, month=0, day=0;String dayName="";
		String startDate="";
		
		/*String c = con.executeCountQuery(dbConnection, "select count(*) from `"+name+"_7d`");
  		if(!c.equals("") && Integer.parseInt(c) !=0){
  			return;
  		}*/
  		
  		
		int curYear = Integer.parseInt(con.executeCountQuery(dbConnection, "select Year(now())"));
		int curMonth = Integer.parseInt(con.executeCountQuery(dbConnection, "select Month(now())"));
		int curDay = Integer.parseInt(con.executeCountQuery(dbConnection, "select Day(now())"));
		int i=1;
		sql = "select tradedate as date from "+name+" where dayName(tradedate)='Monday' order by tradedate asc limit 1";
		startDate = con.executeCountQuery(dbConnection, sql);
		try{
			while(true){
				sql = "select DATE_ADD('"+startDate+"',INTERVAL "+i*7+" DAY) as date";
				weekEnd = con.executeCountQuery(dbConnection, sql);
				sql = "SELECT DATEDIFF(now(), '"+weekEnd+"') AS DiffDate";
				if(Integer.parseInt(con.executeCountQuery(dbConnection, sql)) < 0){
					break;
				}
				callRecursiveFunction(con, dbConnection, weekEnd, name);
				i++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public boolean callRecursiveFunction(Connection con, java.sql.Connection dbConnection, String date, String name){
		float weekHigh=0f, weekLow=99999999f, weekOpen=0f, weekClose=0f;String sql="";String weekCloseDate="";
		ResultSet rs=null;boolean isHavingdata = false;
		try{
			sql = "select * from "+name+" where tradedate >= '"+date+"' and tradedate < DATE_ADD('"+date+"',INTERVAL 7 DAY)";
			rs = con.executeSelectSqlQuery(dbConnection, sql);
			
			while(rs.next()){
				isHavingdata = true;
				if(rs.isFirst()) weekOpen = rs.getFloat("open");
				if(rs.isLast()) {
					weekClose = rs.getFloat("close");
					weekCloseDate = rs.getString("tradedate"); 
				}
				weekHigh = Math.max(weekHigh, rs.getFloat("high"));
				weekLow = Math.min(weekLow, rs.getFloat("low"));
			}
			if(isHavingdata==true){
				sql = "Insert into "+name+"_7d(tradedate, open, high,low,close) values ("
						+ "'"+weekCloseDate+"','"+weekOpen+"', '"+weekHigh+"','"+weekLow+"','"+weekClose+"')";
				con.executeSqlQuery(dbConnection, sql);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			if(rs!=null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return isHavingdata;
	}
	public Date getDateObject(int year, int month, int day){
		Date date1 = null;
		if(month==1)
			date1 = (new GregorianCalendar(year, Calendar.JANUARY, day)).getTime();
		else if(month==2)
			date1 = (new GregorianCalendar(year, Calendar.FEBRUARY, day)).getTime();
		else if(month==3)
			date1 = (new GregorianCalendar(year, Calendar.MARCH, day)).getTime();
		else if(month==4)
			date1 = (new GregorianCalendar(year, Calendar.APRIL, day)).getTime();
		else if(month==5)
			date1 = (new GregorianCalendar(year, Calendar.MAY, day)).getTime();
		else if(month==6)
			date1 = (new GregorianCalendar(year, Calendar.JUNE, day)).getTime();
		else if(month==7)
			date1 = (new GregorianCalendar(year, Calendar.JULY, day)).getTime();
		else if(month==8)
			date1 = (new GregorianCalendar(year, Calendar.AUGUST, day)).getTime();
		else if(month==9)
			date1 = (new GregorianCalendar(year, Calendar.SEPTEMBER, day)).getTime();
		else if(month==10)
			date1 = (new GregorianCalendar(year, Calendar.OCTOBER, day)).getTime();
		else if(month==11)
			date1 = (new GregorianCalendar(year, Calendar.NOVEMBER, day)).getTime();
		else if(month==12)
			date1 = (new GregorianCalendar(year, Calendar.DECEMBER, day)).getTime();
		return date1;
	}
}
