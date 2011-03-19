import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.Border;

public class Loupe extends JPanel {
  private static final int SLIDER_ACCURACY = 5;
  private static final int INITIAL_SCALE = 3;
  private static final int MAX_SCALE = 6;
  private static final int MIN_SCALE = 0;

  private final Robot robot;
  private final JLabel captureLabel;
  private final JSlider slider;

  public Loupe() throws AWTException {
    super();
    this.robot = new Robot();
    robot.setAutoDelay(0);

    this.captureLabel = new JLabel();
    captureLabel.setBorder(BorderFactory.createEtchedBorder());
    captureLabel.setPreferredSize(new Dimension(200,200));

    slider = new JSlider(SwingConstants.VERTICAL,
                         MIN_SCALE * SLIDER_ACCURACY,
                         MAX_SCALE * SLIDER_ACCURACY,
                         INITIAL_SCALE * SLIDER_ACCURACY);

    //addition
    SpringLayout layout = new SpringLayout();
    this.setLayout( layout );
    layout.putConstraint( SpringLayout.SOUTH, captureLabel, -10,SpringLayout.SOUTH, this);
    layout.putConstraint( SpringLayout.NORTH, captureLabel, 10,SpringLayout.NORTH, this);
    layout.putConstraint( SpringLayout.EAST, captureLabel, -10,SpringLayout.WEST, slider);
    layout.putConstraint( SpringLayout.WEST, captureLabel, 10,SpringLayout.WEST, this);
    layout.putConstraint( SpringLayout.EAST, slider, -20,SpringLayout.EAST, this);
    layout.putConstraint( SpringLayout.SOUTH, slider, -10,SpringLayout.SOUTH, this);
    layout.putConstraint( SpringLayout.NORTH, slider, 10,SpringLayout.NORTH, this);
    this.add(captureLabel);
    this.add(slider);

    initSliderLabels();
    initTimer();
  }

  private void initSliderLabels() {
    Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
    for (int i = MIN_SCALE, n = MAX_SCALE; i <= n; i++) {
      labels.put(i * SLIDER_ACCURACY,
                 new JLabel("×" + (1 << (i))));
    }
    slider.setLabelTable(labels);
    slider.setPaintLabels(true);
  }
  private void initTimer() {
    Timer timer = new Timer(true);
    timer.schedule(new TimerTask() {
        @Override
          public void run() {
          capture();
        }
      }, 500, 100);
  }

  protected void capture() {
    Point location = MouseInfo.getPointerInfo().getLocation();
    double scale = Math.pow(2, (double) slider.getValue() / SLIDER_ACCURACY);
    int width = (int) (captureLabel.getWidth() / scale);
    int height = (int) (captureLabel.getHeight() / scale);
    if(width <= 0 || height <= 0)  return;

    BufferedImage image = robot.createScreenCapture(new Rectangle(location.x - width / 2, location.y - height / 2, width, height));

    captureLabel.setIcon(new ExpandImageIcon(image, scale));
  }


  static class ExpandImageIcon extends ImageIcon {
    private static final Color guidLineColor = new Color(0, 0, 255, 128);
    private static final Color gridColor = new Color(128, 128, 128, 128);
    private final double scale;

    public ExpandImageIcon(Image image, double scale) {
      super(image);
      this.scale = scale;
    }

    @Override
      public int getIconHeight() {
      return (int) (super.getIconHeight() * scale);
    }

    @Override
      public int getIconWidth() {
      return (int) (super.getIconWidth() * scale);
    }

    private void paintGuideLine(Graphics g, int x, int y, Image image) {
      g.setColor(guidLineColor);
      int w = image.getWidth(getImageObserver());
      int h = image.getHeight(getImageObserver());
      if (w < 0 || h < 0) { return; }
      int xc = x + (int) (scale * (w / 2));
      int yc = y + (int) (scale * (h / 2));

      g.drawLine(xc, y, xc, y + getIconHeight());
      g.drawLine(x, yc, x + getIconWidth(), yc);

      int xcPlus1 = xc + (int) scale;
      int ycPlus1 = yc + (int) scale;
      g.drawLine(xcPlus1, y, xcPlus1, y + getIconHeight());
      g.drawLine(x, ycPlus1, x + getIconWidth(), ycPlus1);
    }

    @Override
      public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        Image image = getImage();
        g.drawImage(image, x, y, getIconWidth(), getIconHeight(), c);
        paintGuideLine(g, x, y, image);
      }

  }//class expandimageicon

}
