package AlgoStrategySetup.Model;

public class PreRequisites {

	private String csrfToken;
	
	private int hour;
	private int minute;
	private int seconds;
	
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
	
	public String getCsrfToken() {
		return csrfToken;
	}
	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}
}
