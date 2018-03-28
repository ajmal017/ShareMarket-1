package Indicators;

public class Symbol {

	private String date;
	private String open;
	private String high;
	private String low;
	private String close;
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	

	public Symbol(String date, String open2, String high2, String low2, String close2) {
		super();
		this.date = date;
		this.open = open2;
		this.high = high2;
		this.low = low2;
		this.close = close2;
	}

	public String getOpen() {
		return open;
	}
	

	public void setOpen(String open) {
		this.open = open;
	}
	

	public String getHigh() {
		return high;
	}
	

	public void setHigh(String high) {
		this.high = high;
	}
	

	public String getLow() {
		return low;
	}
	

	public void setLow(String low) {
		this.low = low;
	}
	

	public String getClose() {
		return close;
	}
	

	public void setClose(String close) {
		this.close = close;
	}
	
	
	
	
}
