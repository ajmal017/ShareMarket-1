package Indicators;



import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.jcarrierpigeon.Notification;
import net.sf.jcarrierpigeon.NotificationQueue;
import net.sf.jcarrierpigeon.WindowPosition;

public class Notifier extends JFrame {
  private final LinearGradientPaint lpg;

  public static void main(String []a){
	  Notifier window= new Notifier();
//	  Notification note = new Notification(window, WindowPosition.BOTTOMRIGHT, 25, 25, 1000);
//	  NotificationQueue queue = new NotificationQueue();
//	  queue.add(note);

	  window.alert("A");
  }
  public Notifier() {
    setUndecorated(true);
    setSize(1300, 900);

    // size of the screen
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // height of the task bar
    final Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(
        getGraphicsConfiguration());
    final int taskBarSize = scnMax.bottom;

    setLocation(screenSize.width - getWidth(), screenSize.height - taskBarSize
        - getHeight());

    // background paint
    lpg = new LinearGradientPaint(0, 0, 0, getHeight() / 2, new float[] { 0f,
        0.3f, 1f }, new Color[] { new Color(0.8f, 0.8f, 1f),
        new Color(0.7f, 0.7f, 1f), new Color(0.6f, 0.6f, 1f) });

    // blue background panel
    setContentPane(new BackgroundPanel());
  }

  private class BackgroundPanel extends JPanel {
    public BackgroundPanel() {
      setOpaque(true);
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D g2d = (Graphics2D) g;
      // background
      g2d.setPaint(lpg);
      g2d.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
      g2d.setColor(Color.BLACK);

      // border
      g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
  }

  public void alert(final String symbols) {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e1) {
          e1.printStackTrace();
        }

        final Notifier f = new Notifier();

        final Container c = f.getContentPane();
        c.setLayout(new GridBagLayout());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;
        final String html1 = "<html><body style='width: ";
        final String html2 = "px'>";
        final JLabel l = new JLabel(html1 + "900" + html2 + symbols+"</body></html>");
        l.setOpaque(false);

        c.add(l, constraints);

        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;

        final JButton b = new JButton(new AbstractAction("x") {

          @Override
          public void actionPerformed(final ActionEvent e) {
            f.dispose();
          }
        });

        b.setOpaque(false);
        b.setMargin(new Insets(1, 4, 1, 4));
        b.setFocusable(false);

        c.add(b, constraints);
        Calendar cal = Calendar.getInstance();
        f.setVisible(true);
//       for (long x=0; x< 1; ){
//    	   long t = cal.getTimeInMillis()/1000;
//    	   System.out.println(t);
//       }
//       f.setVisible(false);
      }
    });
  }
}