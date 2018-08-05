package AlgoStrategySetup.Model;

public class PreRequisites {

	private String csrfToken;
	private String upstox_access_token;
	
	private int hour;
	private int minute;
	private int seconds;
	
	private String whichTab;
	
	public String getWhichTab() {
		return whichTab;
	}
	public void setWhichTab(String whichTab) {
		this.whichTab = whichTab;
	}
	
	private int hourNSE;
	private int minuteNSE;
	private int secondsNSE;
	
	private int limitStart;
	private int limitEnd;
	
	public int getLimitEnd() {
		return limitEnd;
	}
	public int getLimitStart() {
		return limitStart;
	}
	public void setLimitEnd(int limitEnd) {
		this.limitEnd = limitEnd;
	}
	public void setLimitStart(int limitStart) {
		this.limitStart = limitStart;
	}
	
	private float marginMultiplier;
	
	public float getMarginMultiplier() {
		return marginMultiplier;
	}
	public void setMarginMultiplier(float marginMultiplier) {
		this.marginMultiplier = marginMultiplier;
	}
	
	public int getHour() {
		return hour;
	}
	public int getMinute() {
		return minute;
	}
	public int getSeconds() {
		return seconds;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	 public void setMinute(int minute) {
		this.minute = minute;
	}
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	
	public int getHourNSE() {
		return hourNSE;
	}
	public int getMinuteNSE() {
		return minuteNSE;
	}
	public int getSecondsNSE() {
		return secondsNSE;
	}
	public void setHourNSE(int hourNSE) {
		this.hourNSE = hourNSE;
	}
	public void setMinuteNSE(int minuteNSE) {
		this.minuteNSE = minuteNSE;
	}
	public void setSecondsNSE(int secondsNSE) {
		this.secondsNSE = secondsNSE;
	}
	
	public String getCsrfToken() {
		return csrfToken;
	}
	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}
	public String getUpstox_access_token() {
		return upstox_access_token;
	}
	public void setUpstox_access_token(String upstox_access_token) {
		this.upstox_access_token = upstox_access_token;
	}
}
