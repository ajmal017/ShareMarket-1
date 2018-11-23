package AlgoStrategySetup;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.openqa.selenium.remote.DesiredCapabilities;

import AlgoStrategySetup.Model.PreRequisites;
import Indicators.Connection;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.proxy.ProxyServer;

public class Selenium extends Connection implements Job{
	
	boolean isTesting = false;//vvvvvvvvvvvvvvvvvvvvvvvvvvvIMP, make it to false
	String userName = "DP3137";
	String csrfToken = "";
	String pwd = "Manhpunith7l";
	static int port=4185;
	String companyAns = "EMAX", bankAns = "SBM", mailAns = "GMAIL", creditCard = "HDFC", mobileAns = "NOKIA";
	
	static int entryHour=9, entryMinute=15, entrySecond=0;
	static int nsePreOpenHour=9, nsePreOpenMinute=9, nsePreOpenSecond=0;
	static int exitHour=15, exitMinute=18, exitSecond=0;
	
	static String zExit="Exit", zEntry="Entry", upstox="Upstox", tab1="t1",tab2="t2", tab3="t3", tab4="t4";
	static int Upstox_Sleep=1000000000, zerodha_start_sleep=1000000000, zerodha_exit_sleep=3600000;
	String apiKey = "dPMbue9lq7abjTPCeuJ0Y8tYNEXdwKDd3OQiashl";
	String upstoxUserId = "158352", upstoxPwd="Manhpunith_4", code;
	String upstoxAccessToken="";
	public static float marginMultiplier=5f;
	String entryFileName="EntryBO.js", exitFileName="ExitBO.js", exitWhenHitSLBoFileName="Exit_WhenHitSLBO.js", entryRegularFileName="Entry.js";
	String downloadFilepath = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\";
	
	static boolean isExitWhenHitSL=false;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try{
			
			JobKey jobKey = context.getJobDetail().getKey();
		    Selenium sample = new Selenium();
		    System.out.println("Hello");
		    JobDetail jobDetail = context.getJobDetail();
		    String passedValue=jobDetail.getKey().getName();
		    if(passedValue.equals(upstox)){
		    	System.out.println("Starting upstox");
				sample.startUpstox();
		    }else if(passedValue.equals(tab1) || passedValue.equals(tab2) || passedValue.equals(tab3) 
		    		|| passedValue.equals(tab4) || passedValue.equals(zEntry)){
		    	System.out.println("Starting zerodha entry in "+passedValue);
		    	sample.startZerodha(passedValue);
		    }
		    else if(jobDetail.getKey().getName().equals(zExit)){
		    	System.out.println("Starting zerodha exit");
		    	sample.startZerodha(zExit);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] argv) {
		Selenium sample = new Selenium();
		try {
			sample.start();
		} catch (IOException | InterruptedException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void start() throws IOException, InterruptedException, SQLException {
		Selenium sel = new Selenium();
//		startZerodha(zEntry);
//		startZerodha(zExit);
		startUpstox();
//		server.stop();
//		driver.quit();
	}

	public void startUpstox() throws InterruptedException, IOException, SQLException {
		
		java.sql.Connection dbConnection=null;
		dbConnection = getDbConnection();
		String strFilePath = "C:\\puneeth\\OldLaptop\\Data\\yahoo.har";
		System.setProperty("webdriver.chrome.driver",
				"C:\\puneeth\\OldLaptop\\Puneeth\\StockMarketProj\\chromedriver1.exe");
		WebDriver driver = null;
		ProxyServer server = null;
		// start the proxy
		server = new ProxyServer(++port);

		server.start();
		// captures the moouse movements and navigations
		server.setCaptureHeaders(true);
		server.setCaptureContent(true);
		System.out.println("Started Upstox");
		try{
			// get the Selenium proxy object
			Proxy proxy = server.seleniumProxy();

			// configure it as a desired capability
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(CapabilityType.PROXY, proxy);
			//disable security
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--disable-web-security");

			String downloadFilepath = "C:\\puneeth\\OldLaptop\\Puneeth\\SHARE_MARKET\\";
			File file = new File(downloadFilepath+"upstox.json");
			if(file.delete()){
	            System.out.println("File deleted");
	        }else System.out.println("File doesn't exists");
			
			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("profile.default_content_settings.popups", 0);
			chromePrefs.put("download.default_directory", downloadFilepath);
			options.setExperimentalOption("prefs", chromePrefs);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			driver = new ChromeDriver(capabilities);
			server.newHar("https://api.upstox.com/index/dialog/authorize?apiKey="+apiKey+"&redirect_uri=https://upstox.com&response_type=code");
			driver.get("https://api.upstox.com/index/dialog/authorize?apiKey="+apiKey+"&redirect_uri=https://upstox.com&response_type=code");
		
			driver.findElement(By.id("name")).sendKeys(upstoxUserId);
			driver.findElement(By.id("password")).sendKeys(upstoxPwd);
			driver.findElement(By.id("password2fa")).sendKeys("1989");
			driver.findElement(By.id("password2fa")).submit();
			driver.findElement(By.id("allow")).submit();
			String url = driver.getCurrentUrl();
			Thread.sleep(20000);
			code = url.split("=")[1];
			executeUpstoxJS(driver);
			boolean loop=true;
			String content="";
			while(loop){
				content = readFile(downloadFilepath+"upstox.json");
				if(content !=""){
					loop=false;
				}
			}
			upstoxAccessToken = content;
			executeSqlQuery(dbConnection, "Delete from Upstox");
			executeSqlQuery(dbConnection, "insert into Upstox(access_token) values ('"+upstoxAccessToken+"') ");
			System.out.println("Upstox access token = "+content);
			Toolkit.getDefaultToolkit().beep();
			Thread.sleep(Upstox_Sleep);
			driver.close();
		}catch(Exception e){
			driver.close();
			server.abort();
			startUpstox();
		}		
	}
	
	public String readFile(String fileNameWithPath){
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(fileNameWithPath))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine);
			}
		} catch (IOException e) {
			return "";
		}
		return sb.toString();
	}
	
	public void executeUpstoxJS(WebDriver driver){
		StringBuilder sb = new StringBuilder();
		sb.append("var xhr = new XMLHttpRequest();");
		sb.append("var params = {\"code\":\""+code+"\", \"redirect_uri\":\"https://upstox.com\", \"grant_type\":\"authorization_code\"};");
		sb.append("xhr.open(\"POST\", 'https://api.upstox.com/index/oauth/token', false);");
		sb.append("xhr.setRequestHeader(\"x-api-key\", \""+apiKey+"\");");
		sb.append("xhr.setRequestHeader(\"Content-Type\", \"application/json\");");
		sb.append("xhr.setRequestHeader(\"Authorization\", \"Basic \" + btoa('dPMbue9lq7abjTPCeuJ0Y8tYNEXdwKDd3OQiashl:cdjhncnr0j'));");
		sb.append("xhr.send(JSON.stringify(params));var json = (JSON.parse(xhr.response));");
		sb.append("var blob=new Blob([json.access_token]);");
		sb.append("var link=document.createElement('a');");
		sb.append("link.href=window.URL.createObjectURL(blob);");
		sb.append("link.download=\"upstox.json\";");
		sb.append("link.click();");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(sb.toString());
	}
	public void startZerodha(String passedValue) throws InterruptedException, IOException, SQLException {

		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();

		String strFilePath = "C:\\puneeth\\OldLaptop\\Data\\yahoo.har";
		ProxyServer server = null;
		long sleep=0;
		if(passedValue.equals(tab1) || passedValue.equals(tab2) || passedValue.equals(tab3) 
	    		|| passedValue.equals(tab4) || passedValue.equals(zEntry)){
			System.setProperty("webdriver.chrome.driver",
				"C:\\puneeth\\OldLaptop\\Puneeth\\StockMarketProj\\chromedriver2.exe");
			server = new ProxyServer(++port);
			sleep = zerodha_start_sleep;
		}
		if(passedValue.equals(zExit)){
			System.setProperty("webdriver.chrome.driver",
				"C:\\puneeth\\OldLaptop\\Puneeth\\StockMarketProj\\chromedriver3.exe");
			server = new ProxyServer(++port);
			sleep = zerodha_exit_sleep;
		}
		
		WebDriver driver = null;
		

		server.start();
		// captures the moouse movements and navigations
		server.setCaptureHeaders(true);
		server.setCaptureContent(true);
		try{
			Proxy proxy = server.seleniumProxy();

			// configure it as a desired capability
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			capabilities.setCapability(CapabilityType.PROXY, proxy);
			//disable security
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--disable-web-security");

			
			File file = new File(downloadFilepath+"all.json");
			if(file.delete()){
	            System.out.println("all.json deleted");
	        }else System.out.println("all.json doesn't exists");
			
			if(new File(downloadFilepath+"bo_blocked.json").delete()){
				System.out.println("bo_blocked.json deleted");
			}else System.out.println("bo_blocked.json doesn't exists");

			HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
			chromePrefs.put("profile.default_content_settings.popups", 0);
			chromePrefs.put("download.default_directory", downloadFilepath);
			options.setExperimentalOption("prefs", chromePrefs);
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			driver = new ChromeDriver(capabilities);
			// create a new HAR with the label "apple.com"
			server.newHar("https://kite.zerodha.com");
			driver.get("https://kite.zerodha.com");
			
			validateLogin(dbConnection, driver, server, passedValue);
			Thread.sleep(sleep);
			driver.close();
		}
		catch(Exception e){
			e.printStackTrace();
			if(e.getMessage().contains("/div/div/div/form/div[2]/input") || 
					(e.getStackTrace()!=null && e.getStackTrace().toString().contains("/div/div/div/form/div[2]/input"))){
				startZerodha(passedValue);
				driver.close();
				server.abort();
			}
//			driver.close();
		}
	}
	
	public void getPreOpen(WebDriver driver){
		JavascriptExecutor js = (JavascriptExecutor) driver;
		StringBuilder sb = new StringBuilder();
		sb.append("var hour="+nsePreOpenHour+", minute="+nsePreOpenMinute+", second = "+nsePreOpenSecond+";");
		sb.append("function downloadPreOpen(){"
				+ " var xhr = new XMLHttpRequest();"
				+ " xhr.open(\"GET\", 'https://www.nseindia.com/live_market/dynaContent/live_analysis/pre_open/all.json', false);"
				+ " xhr.send(); var blob=new Blob([xhr.response]); var link=document.createElement('a');"
				+ " link.href=window.URL.createObjectURL(blob); link.download=\"all.json\";link.click();"
				+ " }");
		sb.append("now = new Date();"
				+ " millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, parseInt(minute), second, 0) - now;"
				+ " if (millisTill10 < 0) {"
				+ " millisTill10 += 86400000; console.log(\"it's already after time\");"
				+ " }"
				+ " setTimeout(downloadPreOpen, millisTill10);");
		js.executeScript(sb.toString());
	}

	public void validateLogin(java.sql.Connection dbConnection, WebDriver driver, ProxyServer server
			,String passedValue) throws InterruptedException, IOException, SQLException {
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/input")).sendKeys("DP3137");
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/input")).sendKeys("Manhpunith7l");
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[4]/button")).click();

		Thread.sleep(20000);

		String text1 = driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/label"))
				.getText();
		String text2 = driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/label"))
				.getText();
		if (text1.contains("company")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/input"))
					.sendKeys(companyAns);
		}
		if (text1.contains("In Which bank did you first open your account")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/input")).sendKeys(bankAns);
		}
		if (text1.contains("mail")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/input")).sendKeys(mailAns);
		}
		if (text1.contains("Which is the bank that gave you your first credit card")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/input"))
					.sendKeys(creditCard);
		}
		if (text1.contains("What was the brand of your first mobile")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/div/input"))
					.sendKeys(mobileAns);
		}
		if (text2.contains("company")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/input"))
					.sendKeys(companyAns);
		}
		if (text2.contains("In Which bank did you first open your account")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/input")).sendKeys(bankAns);
		}
		if (text2.contains("mail")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/input")).sendKeys(mailAns);
		}
		if (text2.contains("Which is the bank that gave you your first credit card")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/input"))
					.sendKeys(creditCard);
		}
		if (text2.contains("What was the brand of your first mobile")) {
			driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/div/input"))
					.sendKeys(mobileAns);
		}
		Thread.sleep(3000);
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[4]/button")).click();
		Thread.sleep(6000);

		fetchNetworkCalls(dbConnection, driver, server, passedValue);
	}

	public void fetchNetworkCalls(java.sql.Connection dbConnection, WebDriver driver, ProxyServer server,
			String passedValue) throws InterruptedException, IOException, SQLException {
		
		Har har = server.getHar();
		Thread.sleep(10000);
		List<HarEntry> list = har.getLog().getEntries();
		for (HarEntry h : list) {
			List<HarNameValuePair> nameValue = h.getRequest().getHeaders();
			for (HarNameValuePair pair : nameValue) {
				
				if (pair.getName().equals("X-CSRFToken")) {
					csrfToken = pair.getValue();
					break;
				}
			}
		}
		if(csrfToken==null || csrfToken == ""){
			fetchNetworkCalls(dbConnection, driver, server, passedValue);
		}
		System.out.println("zerodha_csrf_token="+csrfToken);
		if (Integer.parseInt(executeCountQuery(dbConnection, "select count(*) from Upstox"))!=0){
			executeSqlQuery(dbConnection, "Update Upstox set zerodha_csrf_token = '"+csrfToken+"'");
			Toolkit.getDefaultToolkit().beep();
		}
		
		if(passedValue.equals(tab1) || passedValue.equals(tab2) || passedValue.equals(tab3) 
	    		|| passedValue.equals(tab4) || passedValue.equals(zEntry)){
			boolean isPreOpenNotCollected = true;
			getPreOpen(driver);
			while(isPreOpenNotCollected && !isTesting){
				Thread.sleep(60000);
				System.out.println("waiting for preopen");
				File f = new File(downloadFilepath+"all.json");
				if(f.exists() && !f.isDirectory()) { 
					isPreOpenNotCollected = false;
					System.out.println("waiting for preopen ended");
				}
			}
			Toolkit.getDefaultToolkit().beep();
			
			executeEntryJs(driver, server, passedValue);
		}else if (passedValue.equals(zExit)){
			executeExitJs(driver, server);
		}
	}

	public void executeEntryJs(WebDriver driver, ProxyServer server, String passedValue) throws IOException, InterruptedException {
		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		int limitStart=0, limitEnd=0;
		PreOpenSession_TodaysOpenRespectToYestClose pre = new PreOpenSession_TodaysOpenRespectToYestClose();
		PreRequisites preRequisites = new PreRequisites();
		preRequisites.setCsrfToken(csrfToken);
		preRequisites.setHour(entryHour);
		preRequisites.setMinute(entryMinute);
		preRequisites.setSeconds(entrySecond);
		if(passedValue.equals(tab1)){
			limitStart = 0; limitEnd = 100;
		}else if(passedValue.equals(tab2)){ 
			limitStart = 100; limitEnd = 100;
		}else if(passedValue.equals(tab3)){ 
			limitStart = 200; limitEnd = 100;
		}else if(passedValue.equals(tab4)){ 
			limitStart = 300; limitEnd = 100;
		}else{
			limitStart = 0; limitEnd = 1000;
		}
		preRequisites.setWhichTab(passedValue);
		preRequisites.setLimitStart(limitStart);
		preRequisites.setLimitEnd(limitEnd);
		
		String upstox_access_token = executeCountQuery(dbConnection, "select access_token from upstox");
		preRequisites.setUpstox_access_token(upstox_access_token);
		preRequisites.setMarginMultiplier(marginMultiplier);

		StringBuilder symbolsToRun = null;
		try {
			symbolsToRun = pre.updatePreOpenPrice(dbConnection, preRequisites);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String scriptToRun = setFileContent(symbolsToRun.toString(), entryFileName);

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(
				"var jq = document.createElement('script');jq.src = \"https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js\";document.getElementsByTagName('head')[0].appendChild(jq);");
		js.executeScript(scriptToRun);
		
		if(isExitWhenHitSL){
			scriptToRun = setFileContent(symbolsToRun.toString(), exitWhenHitSLBoFileName);
			js = (JavascriptExecutor) driver;
			js.executeScript(scriptToRun);
		}
		Calendar C = new GregorianCalendar();
        int hour = C.get( Calendar.HOUR_OF_DAY );
        int minute = C.get( Calendar.MINUTE );
		boolean isBoBlocked = false;
		while(!isBoBlocked && hour == 9 && minute <30 || isTesting){
			Thread.sleep(10000);
			System.out.println("waiting for bo blocked file to exist");
			File f = new File(downloadFilepath+"bo_blocked.json");
			if(f.exists() && !f.isDirectory()) {
				isBoBlocked = true;
				scriptToRun = setFileContent(symbolsToRun.toString(), entryRegularFileName);
				js = (JavascriptExecutor) driver;
				js.executeScript(scriptToRun);
				System.out.println("waiting for bo blocked file ended");
				isTesting=false;
				break;
			}
			C = new GregorianCalendar();
			hour = C.get( Calendar.HOUR_OF_DAY );
	        minute = C.get( Calendar.MINUTE );
		}
		System.out.println("waiting for bo blocked file ended as BO/CO not blocked");
	}
	
	public void executeExitJs(WebDriver driver, ProxyServer server) throws IOException {
		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		PreOpenSession_TodaysOpenRespectToYestClose pre = new PreOpenSession_TodaysOpenRespectToYestClose();
		PreRequisites preRequisites = new PreRequisites();
		preRequisites.setCsrfToken(csrfToken);
		preRequisites.setHour(exitHour);
		preRequisites.setMinute(exitMinute);
		preRequisites.setSeconds(exitSecond);
		String upstox_access_token = executeCountQuery(dbConnection, "select access_token from upstox");
		preRequisites.setUpstox_access_token(upstox_access_token);
		preRequisites.setMarginMultiplier(marginMultiplier);

		StringBuilder preReq = new StringBuilder();
		preReq.append("var hour="+preRequisites.getHour()+", minute="+preRequisites.getMinute()+", second="+preRequisites.getSeconds()+";");
		preReq.append("var csrfToken='"+preRequisites.getCsrfToken()+"';");
		String scriptToRun = setFileContent(preReq.toString(), exitFileName);

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(
				"var jq = document.createElement('script');jq.src = \"https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js\";document.getElementsByTagName('head')[0].appendChild(jq);");
		js.executeScript(scriptToRun);
	}

	public String setFileContent(String preReq, String fileName) throws IOException {
		String line, line2 = "";
		try {
			FileReader reader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(reader);

			while ((line = bufferedReader.readLine()) != null) {
				line2 = line2 + line + "\n";
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return preReq + "\n" + line2;
	}
}
