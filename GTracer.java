import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

//hand made library
import filter.*;


public class GTracer extends JFrame implements ActionListener,MouseListener,ChangeListener{

  //main function
  public static void main(String[] args) {
    new GTracer("sample.png");
  }

  //class variables
  private BufferedImage originalImg;
  private BufferedImage tracedImg;
  private Tracer tracer;

  //constructor
  public GTracer(String filename){
    super("GTracer");//new JFrame

    //load image file
    try{
      File imgfile = new File(filename);
      originalImg = ImageIO.read(imgfile);
    }catch (Exception e) {
    }

    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds( 0, 0,
               screenDim.width - 100,
               screenDim.height - 100);
    //create panel and add to this(JFrame)
    add(makePanel());
    //how to action, when close
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);


    //tracer
    tracer=new Tracer(originalImg);
  }

  //mouse
  public void mouseMoved(MouseEvent e) {
  }
  public void mouseDragged(MouseEvent e) {
  }
  public void mouseClicked(MouseEvent e){
  }
  public void mouseEntered(MouseEvent e){
  }
  public void mouseExited(MouseEvent e){
  }
  double[] p=new double[2];
  public void mousePressed(MouseEvent e){
    Dimension size = myCanv.getSize();
    double x=e.getX()/(double)size.width;
    double y=e.getY()/(double)size.height;

    p[0]=x;p[1]=y;//test
    //p=tracer.point(x,y);//convert

    myCanv.repaint();
  }
  public void mouseReleased(MouseEvent e){
  }
  private void updateLabel(){
    xminLabel.setText(String.format("x min: %.2f",xstart));
    xmaxLabel.setText(String.format("x max: %.2f",xend));
    yminLabel.setText(String.format("y min: %.2f",ystart));
    ymaxLabel.setText(String.format("y max: %.2f",yend));
  }
  public void stateChanged(ChangeEvent ce){
    if(ce.getSource()==xminSlider){
      int t = xminSlider.getValue();
      xstart=0.01f*t;
    }else if(ce.getSource()==xmaxSlider){
      int t = xmaxSlider.getValue();
      xend=0.01f*t;
    }else if(ce.getSource()==yminSlider){
      int t = yminSlider.getValue();
      ystart=0.01f*t;
    }else if(ce.getSource()==ymaxSlider){
      int t = ymaxSlider.getValue();
      yend=0.01f*t;
    }
    updateLabel();
    tracer.setLengthMap(xstart,xend,ystart,yend);
    tracedImg=tracer.doFilter(0);
    myCanv.repaint();
  }

  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == chessButton){
      tracedImg=null;
      tracedImg=tracer.doFilter(1);
    }else if(ae.getSource() == cityButton){
      tracedImg=null;
      tracedImg=tracer.doFilter(2);
    }else if(ae.getSource() == boneButton){
      tracedImg=null;
      tracedImg=tracer.doFilter(3);
    }else if(ae.getSource() == resetButton){
      xstart=0f;
      xend=1f;
      ystart=0f;
      yend=1f;
      tracer.setLengthMap(xstart,xend,ystart,yend);
      tracedImg=null;
    }else if(ae.getSource() == saveButton){
      String currentDir=System.getProperty("user.dir");
      JFileChooser jfc = new JFileChooser( (new File(currentDir)).getAbsolutePath() );
      jfc.setDialogTitle("save image");

      String str = null;
      int s = jfc.showSaveDialog( null );
      if( s == JFileChooser.APPROVE_OPTION ){
        File file = jfc.getSelectedFile();
        str = new String( file.getAbsolutePath() );
      }

      //save
      if(str!=null){
        File imgfile = new File(str+".png");
        try{
          ImageIO.write(tracedImg, "png", imgfile);
        }catch(Exception e){
        }
      }
    }
    myCanv.repaint();
  }

  //get extension
  private static String getSuffix(String fileName) {
    if(fileName == null)return null;
    int point = fileName.lastIndexOf(".");
    if (point != -1)return fileName.substring(point + 1);
    return fileName;
  }

  private float xstart=0f;
  private float xend=1f;
  private float ystart=0f;
  private float yend=1f;
  private JButton chessButton;
  private JButton cityButton;
  private JButton boneButton;
  private JButton resetButton;
  private JButton saveButton;
  private MyCanvas myCanv;
  private JLabel xminLabel,xmaxLabel;
  private JLabel yminLabel,ymaxLabel;
  private JSlider xminSlider, xmaxSlider;
  private JSlider yminSlider, ymaxSlider;
  private JPanel makePanel(){

    //button
    chessButton=new JButton("chess");
    chessButton.addActionListener( this );
    chessButton.setFocusable(false);
    cityButton=new JButton("city");
    cityButton.addActionListener( this );
    cityButton.setFocusable(false);
    boneButton=new JButton("thinning");
    boneButton.addActionListener( this );
    boneButton.setFocusable(false);

    resetButton=new JButton("reset");
    resetButton.addActionListener( this );
    resetButton.setFocusable(false);

    saveButton=new JButton("save");
    saveButton.addActionListener( this );
    saveButton.setFocusable(false);

    //canvas
    myCanv=new MyCanvas();
    myCanv.setPreferredSize(new Dimension(600, 600));
    myCanv.setBackground(Color.white);
    myCanv.addMouseListener(this);
    //slider
    xminLabel=new JLabel("x min");
    xmaxLabel=new JLabel("x max");
    yminLabel=new JLabel("y min");
    ymaxLabel=new JLabel("y max");
    updateLabel();
    xminSlider = new JSlider( 0, 100, 0 );
    xminSlider.setFocusable(false);
    xminSlider.addChangeListener( this );
    xmaxSlider = new JSlider( 0, 100, 100 );
    xmaxSlider.setFocusable(false);
    xmaxSlider.addChangeListener( this );
    yminSlider = new JSlider( 0, 100, 0);
    yminSlider.setFocusable(false);
    yminSlider.addChangeListener( this );
    ymaxSlider = new JSlider( 0, 100, 100);
    ymaxSlider.setFocusable(false);
    ymaxSlider.addChangeListener( this );

    //panel
    JPanel jp=new JPanel();
    //set layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout( layout );
    layout.putConstraint( SpringLayout.SOUTH, chessButton, -5,SpringLayout.SOUTH, jp );
    layout.putConstraint( SpringLayout.WEST, chessButton, 5,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.SOUTH, cityButton, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST, cityButton, 5,SpringLayout.EAST, chessButton);
    layout.putConstraint( SpringLayout.SOUTH, boneButton, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST, boneButton, 5,SpringLayout.EAST, cityButton);

    layout.putConstraint( SpringLayout.SOUTH, resetButton, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST, resetButton, 50,SpringLayout.EAST, boneButton);
    layout.putConstraint( SpringLayout.SOUTH, saveButton, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.WEST, saveButton, 5,SpringLayout.EAST, resetButton);


    layout.putConstraint( SpringLayout.SOUTH, xminLabel, -10,SpringLayout.NORTH, chessButton);
    layout.putConstraint( SpringLayout.WEST, xminLabel, 0,SpringLayout.WEST, chessButton);
    layout.putConstraint( SpringLayout.NORTH, xminSlider, 0,SpringLayout.NORTH,xminLabel);
    layout.putConstraint( SpringLayout.WEST, xminSlider, 0,SpringLayout.EAST, xminLabel);

    layout.putConstraint( SpringLayout.NORTH, xmaxLabel, 0,SpringLayout.NORTH, xminLabel);
    layout.putConstraint( SpringLayout.WEST, xmaxLabel, 10,SpringLayout.EAST, xminSlider);
    layout.putConstraint( SpringLayout.NORTH, xmaxSlider, 0,SpringLayout.NORTH, xmaxLabel);
    layout.putConstraint( SpringLayout.WEST, xmaxSlider, 0,SpringLayout.EAST, xmaxLabel);

    layout.putConstraint( SpringLayout.NORTH, yminLabel, 0,SpringLayout.NORTH, xminLabel);
    layout.putConstraint( SpringLayout.WEST, yminLabel, 20,SpringLayout.EAST, xmaxSlider);
    layout.putConstraint( SpringLayout.NORTH, yminSlider, 0,SpringLayout.NORTH,yminLabel);
    layout.putConstraint( SpringLayout.WEST, yminSlider, 0,SpringLayout.EAST, yminLabel);

    layout.putConstraint( SpringLayout.NORTH, ymaxLabel, 0,SpringLayout.NORTH, xminLabel);
    layout.putConstraint( SpringLayout.WEST, ymaxLabel, 10,SpringLayout.EAST, yminSlider);
    layout.putConstraint( SpringLayout.NORTH, ymaxSlider, 0,SpringLayout.NORTH, ymaxLabel);
    layout.putConstraint( SpringLayout.WEST, ymaxSlider, 0,SpringLayout.EAST, ymaxLabel);




    layout.putConstraint( SpringLayout.SOUTH, myCanv, 0,SpringLayout.NORTH, xminSlider);
    layout.putConstraint( SpringLayout.NORTH, myCanv, 0,SpringLayout.NORTH, jp );
    layout.putConstraint( SpringLayout.EAST, myCanv, 0,SpringLayout.EAST, jp );
    layout.putConstraint( SpringLayout.WEST, myCanv, 0,SpringLayout.WEST, jp );


    //add to jpanel
    jp.add(myCanv);
    jp.add(xminLabel);
    jp.add(xmaxLabel);
    jp.add(yminLabel);
    jp.add(ymaxLabel);
    jp.add(xminSlider);
    jp.add(xmaxSlider);
    jp.add(yminSlider);
    jp.add(ymaxSlider);
    jp.add(chessButton);
    jp.add(cityButton);
    jp.add(boneButton);
    jp.add(resetButton);
    jp.add(saveButton);
    return jp;
  }

  ///private class
  private class MyCanvas extends Canvas{
    public void paint(Graphics g){
      if(tracedImg==null){
        g.drawImage(originalImg,10,10,this);
      }else{
        g.drawImage(tracedImg,10,10,this);
        int r=10;
        Dimension size = myCanv.getSize();
        int x=(int)(p[0]*size.width)-r/2;
        int y=(int)(p[1]*size.height)-r/2;
        g.drawOval(x,y,r,r);
      }
    }
  }
}
