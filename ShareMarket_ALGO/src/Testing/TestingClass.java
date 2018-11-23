package Testing;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestingClass {

	public static void main(String[] args) {
		Calendar C = new GregorianCalendar();
        int hour = C.get( Calendar.HOUR_OF_DAY );
        int minute = C.get( Calendar.MINUTE );

        if( hour == 14 && minute > 0 )
            System.out.println("time > 14");
	}

}
