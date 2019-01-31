package Testing;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class TestingClass {

	public static void main(String[] args) throws InterruptedException {
		int count=0;
		while (count < 1 && new Date().getHours() <= 9){
			
			Toolkit.getDefaultToolkit().beep();
			Thread.sleep(1000);
			count++;
		}
	}

}
