package AlgoStrategySetup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import AlgoStrategySetup.Model.PreRequisites;
import Indicators.Connection;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.proxy.ProxyServer;

public class Selenium {

	String userName = "DP3137";
	String csrfToken = "";
	String pwd = "Manhpunith7l";
	String companyAns = "EMAX", bankAns = "SBM", mailAns = "GMAIL", creditCard = "HDFC", mobileAns = "NOKIA";
	static int hour, minute, second;

	public static void main(String[] argv) {
		hour = Integer.parseInt(argv[0]);
		minute = Integer.parseInt(argv[1]);
		second = Integer.parseInt(argv[2]);
		Selenium sample = new Selenium();
		try {
			sample.start();
		} catch (IOException | InterruptedException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startZerodha() throws InterruptedException, IOException {
		String strFilePath = "C:\\puneeth\\OldLaptop\\Data\\yahoo.har";
		System.setProperty("webdriver.chrome.driver",
				"C:\\puneeth\\OldLaptop\\Puneeth\\StockMarketProj\\chromedriver.exe");
		WebDriver driver = null;
		ProxyServer server = null;
		// start the proxy
		server = new ProxyServer(4480);

		server.start();
		// captures the moouse movements and navigations
		server.setCaptureHeaders(true);
		server.setCaptureContent(true);

		// get the Selenium proxy object
		Proxy proxy = server.seleniumProxy();

		// configure it as a desired capability
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.PROXY, proxy);

		driver = new ChromeDriver(capabilities);
		// create a new HAR with the label "apple.com"
		server.newHar("https://kite.zerodha.com");
		driver.get("https://kite.zerodha.com");
		validateLogin(driver, server);
	}

	public void validateLogin(WebDriver driver, ProxyServer server) throws InterruptedException, IOException {
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[2]/input")).sendKeys("DP3137");
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[3]/input")).sendKeys("Manhpunith7l");
		driver.findElement(By.xpath("//*[@id=\"container\"]/div/div/div/form/div[4]/button")).click();

		Thread.sleep(3000);

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

		fetchNetworkCalls(driver, server);
	}

	public void fetchNetworkCalls(WebDriver driver, ProxyServer server) throws InterruptedException, IOException {
		Har har = server.getHar();
		Thread.sleep(6000);
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
		System.out.println(csrfToken);
		executeJs(driver, server);
	}

	public void executeJs(WebDriver driver, ProxyServer server) throws IOException {
		java.sql.Connection dbConnection = null;
		Connection con = new Connection();
		dbConnection = con.getDbConnection();
		PreOpenSession_TodaysOpenRespectToYestClose pre = new PreOpenSession_TodaysOpenRespectToYestClose();
		PreRequisites preRequisites = new PreRequisites();
		preRequisites.setCsrfToken(csrfToken);
		preRequisites.setHour(hour);
		preRequisites.setMinute(minute);
		preRequisites.setSeconds(second);

		StringBuilder symbolsToRun = null;
		try {
			symbolsToRun = pre.updatePreOpenPrice(dbConnection, preRequisites);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String scriptToRun = setFileContent(symbolsToRun.toString());

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(
				"var jq = document.createElement('script');jq.src = \"https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js\";document.getElementsByTagName('head')[0].appendChild(jq);");
		js.executeScript(scriptToRun);
	}

	public void start() throws IOException, InterruptedException, SQLException {
		Selenium sel = new Selenium();

		startZerodha();

		// server.stop();
		// driver.quit();
	}

	public String setFileContent(String reqSymbols) throws IOException {
		String line, line2 = "";
		try {
			FileReader reader = new FileReader("Test.js");
			BufferedReader bufferedReader = new BufferedReader(reader);

			while ((line = bufferedReader.readLine()) != null) {
				line2 = line2 + line + "\n";
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reqSymbols + "\n" + line2;
	}
}
