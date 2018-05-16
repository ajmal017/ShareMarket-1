package FetchData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Indicators.Connection;

public class RenameFile extends Connection{
	public static void main(String[] args) {
		new RenameFile().rename();
	}
	public void rename()
	{
		List<String> results = new ArrayList<String>();


		File[] files = new File("C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Daily/test").listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 
		System.out.println(files.length);
		for (File file : files) {
		    if (file.isFile()) {
		    	
		    	
		    	String date =(file.getName().split("cm"))[1].split("bhav")[0].substring(0, 2);
		    	if(date.substring(0,1).equals("0")){
		    		date = date.split("0")[1];
		    	}
		    	int month= getMonthNumber(((file.getName().split("cm"))[1].split("bhav")[0].substring(2, 5)));
		    	String year = ((file.getName().split("cm"))[1].split("bhav")[0].substring(5, 9));
		    	System.out.println(year+"-"+month+"-"+date);
		    	File newFile =new File("C:/puneeth/OldLaptop/Puneeth/SHARE_MARKET/Daily/test/"+year+"-"+month+"-"+date+".zip");
		    	file.renameTo(newFile);
		        results.add(file.getName());
		    }
		}
	}
}
