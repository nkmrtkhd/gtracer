import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class Loupe extends JFrame {
  private static final int SLIDER_ACCURACY = 5;
  private static final int INITIAL_SCALE = 3;
  private static final int MAX_SCALE = 6;
  private static final int MIN_SCALE = 0;

  public static void main(String[] args) throws AWTException {

    Loupe f = new Loupe();
    f.setAlwaysOnTop(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }

  private volatile boolean captureEnable = true;
  private final Robot robot;
  private final JLabel captureLabel;
  private final JSlider slider;
  private final JTextField text;
  private GridMode gridMode = GridMode.GUIDE_LINE;
  private volatile boolean autoPick = false;

  public Loupe() throws AWTException {
    super("Loupe");

    this.robot = new Robot();
    robot.setAutoDelay(0);

    this.captureLabel = new JLabel();
    captureLabel.setBorder(BorderFactory.createEtchedBorder());
    getContentPane().add(captureLabel, BorderLayout.CENTER);
    captureLabel.setPreferredSize(new Dimension(200, 200));
    captureLabel.setToolTipText("<html><h3>使い方</h3>" +
                                "<ul style='width:300px'>" +
                                "<li>どれかキーを押すと、マウスカーソルがさしているピクセルの色を16進数で表示します。</li>" +
                                "<li>\'c\'キーを押すと、クリップボードにコピーします。貼り付け先によってピクセルの色(16進数)と拡大イメージが変わります。</li>" +
                                "<li>\'g\'キーを押すとグリッド線を表示します。</li>" +
                                "<li>\'a\'キーを押すと自動的に色の抽出をします。</li>" +
                                "<li>\'l\'キーを押すと拡大イメージのキャプチャーをロックします。もう一度押すと解除。</li></ul>" +
                                "</html>");

    text = new JTextField();
    text.setText("Press the space key");

    getContentPane().add(text, BorderLayout.SOUTH);

    slider = new JSlider(SwingConstants.VERTICAL,
                         MIN_SCALE * SLIDER_ACCURACY,
                         MAX_SCALE * SLIDER_ACCURACY,
                         INITIAL_SCALE * SLIDER_ACCURACY);
    getContentPane().add(slider, BorderLayout.EAST);

    initSliderLabels();
    initEventListener();
    initTimer();
  }

  private void initSliderLabels() {
    Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
    for (int i = MIN_SCALE, n = MAX_SCALE; i <= n; i++) {
      labels.put(i * SLIDER_ACCURACY,
                 new JLabel("×" + (1 << (i))));
      //               new JLabel(String.format("× %.1f", Math.pow(2, i)))); // if(MIN_SCALE < 0)
    }
    slider.setLabelTable(labels);
    slider.setPaintLabels(true);
  }

  private void appendHex(StringBuffer buffer, int i) {
    if (i < 0x10)
      buffer.append('0');
    buffer.append(Integer.toHexString(i));
  }

  protected void capture() {
    Point location = MouseInfo.getPointerInfo().getLocation();

    double scale = Math
      .pow(2, (double) slider.getValue() / SLIDER_ACCURACY);
    int width = (int) (captureLabel.getWidth() / scale);
    int height = (int) (captureLabel.getHeight() / scale);

    if (width <= 0 || height <= 0) {
      return;
    }

    BufferedImage image = robot.createScreenCapture(new Rectangle(
                                                                  location.x - width / 2, location.y - height / 2, width, height));

    captureLabel.setIcon(new ExpandImageIcon(image, scale, gridMode));

    if (autoPick) {
      pickUp();
    }
  }

  private String toHexString(Color color) {
    StringBuffer buffer = new StringBuffer(7);
    buffer.append('#');
    appendHex(buffer, color.getRed());
    appendHex(buffer, color.getGreen());
    appendHex(buffer, color.getBlue());

    return buffer.toString();
  }

  protected void copyToClipbord() {
    text.setText("Copty to Clipboard!");

    EventQueue.invokeLater(new Runnable() {
        public void run() {
          Clipboard clipboard = Toolkit.getDefaultToolkit()
            .getSystemClipboard();
          Border border = captureLabel.getBorder();

          Insets insets = border.getBorderInsets(captureLabel);

          int w = captureLabel.getWidth() - insets.left - insets.right;
          int h = captureLabel.getHeight() - insets.top - insets.bottom;

          BufferedImage bi = new BufferedImage(w, h,
                                               BufferedImage.TYPE_INT_RGB);
          Graphics graphics = bi.getGraphics();
          try {
            graphics.translate(-insets.left, -insets.top);
            captureLabel.paint(graphics);
          } finally {
            graphics.dispose();
          }

          ImageAndTextSelection selection = new ImageAndTextSelection(bi,
                                                                      pickUp());
          clipboard.setContents(selection, selection);
        }
      });
  }

  private void initEventListener() {
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
        public void eventDispatched(AWTEvent event) {
          if (event.getID() == KeyEvent.KEY_PRESSED) {
            KeyEvent ke = (KeyEvent) event;
            switch (ke.getKeyChar()) {
            case 'l': toggleCaptureEnable(); break;
            case 'c': copyToClipbord(); break;
            case 'g': changeGridMode(); break;
            case 'f': capture(); break;
            case 'a': toggleAutoPick(); break;
            default: pickUp();
            }
          }
          else if(event.getID() == MouseWheelEvent.MOUSE_WHEEL) {
            MouseWheelEvent mwe = (MouseWheelEvent) event;
            slider.setValue(slider.getValue() +
                            mwe.getWheelRotation());
          }
        }

      }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
  }

  private void initTimer() {
    Timer timer = new Timer(true);
    timer.schedule(new TimerTask() {
        @Override
          public void run() {
          if (captureEnable)
            capture();
        }
      }, 500, 100);
  }

  public String pickUp() {
    Point location = MouseInfo.getPointerInfo().getLocation();

    Color color = robot.getPixelColor(location.x, location.y);

    String hexColor = toHexString(color);
    text.setText(hexColor);
    Border border = BorderFactory
      .createCompoundBorder(BorderFactory.createEtchedBorder(),
                            BorderFactory.createLineBorder(color, 3));

    text.setBorder(border);

    return hexColor;
  }

  protected synchronized void toggleCaptureEnable() {
    captureEnable = !captureEnable;

    text.setText((this.captureEnable ? "The screen capture was begun."
                  : "The screen capture was locked."));
  }

  protected synchronized void toggleAutoPick() {
    this.autoPick = !this.autoPick;

    text.setText((this.autoPick ? "Set to auto-pick enabled."
                  : "Set to auto-pick disabled."));
  }

  protected void changeGridMode() {
    GridMode[] modes = GridMode.values();
    gridMode = modes[(gridMode.ordinal() + 1) % modes.length];

    text.setText("GridMode is chaged.");
  }

  enum GridMode {GUIDE_LINE, GRID, NONE };

  static class ExpandImageIcon extends ImageIcon {
    private static final Color guidLineColor = new Color(0, 0, 255, 128);
    private static final Color gridColor = new Color(128, 128, 128, 128);
    private final double scale;
    private final GridMode gridMode;

    public ExpandImageIcon(Image image, double scale, GridMode gridMode) {
      super(image);
      this.scale = scale;
      this.gridMode = gridMode;
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
        switch (gridMode) {
        case GUIDE_LINE:
          paintGuideLine(g, x, y, image);
          break;
        case GRID:
          paintGridLine(g, x, y);
          break;
        case NONE:
          break;
        default:
          throw new AssertionError();
        }
      }

    private void paintGridLine(Graphics g, int x, int y) {
      g.setColor(gridColor);
      for (int xi = 0; xi * scale < x + getIconWidth(); xi++) {
        g.drawLine(x + (int) (xi * scale), y, x + (int) (xi * scale), y
                   + getIconHeight());
      }
      for (int yi = 0; yi * scale < y + getIconHeight(); yi++) {
        g.drawLine(x, y + (int) (yi * scale), x + getIconWidth(), y
                   + (int) (yi * scale));
      }
    }
  }

  static class ImageAndTextSelection implements Transferable, ClipboardOwner {
    private String hexColor;
    private Image image;

    public ImageAndTextSelection(Image image, String text) {
      this.image = image;
      this.hexColor = text;
    }

    public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException {
      if (DataFlavor.imageFlavor.equals(flavor)) {
        return image;
      } else if (DataFlavor.stringFlavor.equals(flavor)) {
        return hexColor;
      }
      throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { DataFlavor.imageFlavor,
                                DataFlavor.stringFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return DataFlavor.imageFlavor.equals(flavor)
        || DataFlavor.stringFlavor.equals(flavor);
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      image = null;
    }
  }
}
