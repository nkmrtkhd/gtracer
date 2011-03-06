import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

//hand made library
import filter.*;


public class GTracer implements ActionListener,
                                MouseListener,
                                ChangeListener{

  //main function
  public static void main(String[] args) {
    if(args.length>0)
      new GTracer(args[0]);
    else
      new GTracer(null);

  }

  //class variables
  private BufferedImage originalImg;
  private BufferedImage tracedImg;
  private Tracer tracer;

  //constructor
  public GTracer(String inputFile){
    if(inputFile!=null)this.open(inputFile);
    makeControlFrame();
    makeCanvasFrame();
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

  ArrayList<Integer> pQueue= new ArrayList<Integer>();
  ArrayList<Integer> tracedPos= new ArrayList<Integer>();
  int[] mousePos=new int[2];
  public void mousePressed(MouseEvent e){
    //Dimension size = myCanv.getSize();
    int x=e.getX();
    int y=e.getY();

    mousePos=tracer.getPoint(x,y);//convertion
    if(rbPoints.isSelected()){
      pQueue.add(mousePos[0]);
      pQueue.add(mousePos[1]);
    }else if(rbXStart.isSelected()){
      xstart[0]=mousePos[0];
      xstart[1]=mousePos[1];
      ystart[0]=mousePos[0];
      ystart[1]=mousePos[1];
    }else if(rbXEnd.isSelected()){
      xend[0]=mousePos[0];
      xend[1]=mousePos[1];
    }else if(rbYStart.isSelected()){
      ystart[0]=mousePos[0];
      ystart[1]=mousePos[1];
    }else if(rbYEnd.isSelected()){
      yend[0]=mousePos[0];
      yend[1]=mousePos[1];
    }

    myCanv.repaint();
  }
  public void mouseReleased(MouseEvent e){
  }

  private void updateLabel(){
    xminLabel.setText(String.format("x min: %.2f",xcutmin));
    xmaxLabel.setText(String.format("x max: %.2f",xcutmax));
    yminLabel.setText(String.format("y min: %.2f",ycutmin));
    ymaxLabel.setText(String.format("y max: %.2f",ycutmax));
  }

  public void stateChanged(ChangeEvent ce){
    if(ce.getSource()==xminSlider){
      int t = xminSlider.getValue();
      xcutmin=0.01f*t;
    }else if(ce.getSource()==xmaxSlider){
      int t = xmaxSlider.getValue();
      xcutmax=0.01f*t;
    }else if(ce.getSource()==yminSlider){
      int t = yminSlider.getValue();
      ycutmin=0.01f*t;
    }else if(ce.getSource()==ymaxSlider){
      int t = ymaxSlider.getValue();
      ycutmax=0.01f*t;
    }else if(ce.getSource()==spXStart){
      xRealStart=((Double)spXStart.getValue()).doubleValue();
    }else if(ce.getSource()==spXEnd){
      xRealEnd=((Double)spXEnd.getValue()).doubleValue();
    }else if(ce.getSource()==spYStart){
      yRealStart=((Double)spYStart.getValue()).doubleValue();
    }else if(ce.getSource()==spYEnd){
      yRealEnd=((Double)spYEnd.getValue()).doubleValue();
    }
    updateLabel();
    myCanv.repaint();
  }

  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == openButton){
      this.open(null);
    }else if(ae.getSource() == chessButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(1);
    }else if(ae.getSource() == cityButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(2);
    }else if(ae.getSource() == localMaxButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(3);
    }else if(ae.getSource() == simpleMaskButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(4);
    }else if(ae.getSource() == hildthMaskButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(5);
    }else if(ae.getSource() == traceButton){
      if(pQueue.size()>=4){
        tracedPos=tracer.trace(pQueue);
      }
    }else if(ae.getSource() == writeButton){
      this.writeTracedPoint();
    }else if(ae.getSource() == resetButton){
      xcutmin=0f;
      xcutmax=1f;
      ycutmin=0f;
      ycutmax=1f;
      pQueue.clear();
      tracedPos.clear();
      tracer.setLengthMap(xcutmin,xcutmax,ycutmin,ycutmax);
      tracedImg=null;
    }
    myCanv.repaint();
  }

  /** 引数がnullだったらダイアログで選んで，open */
  private void open(String filename){
    if(filename==null){
      String currentDir=System.getProperty("user.dir");
      JFileChooser jfc = new JFileChooser( (new File(currentDir)).getAbsolutePath() );
      jfc.setDialogTitle("open image");

      int s = jfc.showOpenDialog( null );
      if( s == JFileChooser.APPROVE_OPTION ){
        File file = jfc.getSelectedFile();
        filename = new String( file.getAbsolutePath() );
      }
    }
    //load image file
    try{
      File imgfile = new File(filename);
      originalImg = ImageIO.read(imgfile);
    }catch (Exception e) {
    }
    //tracer
    tracer=null;
    tracer=new Tracer(originalImg);
  }


  private void writeTracedPoint(){
    String currentDir=System.getProperty("user.dir");
    JFileChooser jfc = new JFileChooser( (new File(currentDir)).getAbsolutePath() );
    jfc.setDialogTitle("write file");

    String str=null;
    int s = jfc.showSaveDialog( null );
    if( s == JFileChooser.APPROVE_OPTION ){
      File file = jfc.getSelectedFile();
      str = new String( file.getAbsolutePath() );
    }
    if(str!=null){
      try {
        FileWriter fw = new FileWriter(str);
        BufferedWriter bw = new BufferedWriter( fw );
        PrintWriter pw = new PrintWriter( bw );


        double dxi=1.0/(xend[0]-xstart[0]);
        double dxreal=(xRealEnd-xRealStart);
        double dyi=1.0/(ystart[1]-yend[1]);//note y is upside down
        double dyreal=(yRealEnd-yRealStart);
        for(int i=0;i<tracedPos.size()/2;i++){
          double x=(tracedPos.get(2*i)-xstart[0])*dxi*dxreal+xRealStart;
          double y=(ystart[1]-tracedPos.get(2*i+1))*dyi*dyreal+yRealStart;
          pw.println( String.format("%f %f", x,y) );
        }

        pw.close();
        bw.close();
        fw.close();
        //System.out.println("saved slice position");
      }catch ( IOException ioe ){
      }
    }
  }

  //create canvas frame
  private MyCanvas myCanv;
  private void makeCanvasFrame(){
    JFrame canvasJframe=new JFrame("Gtracer");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    canvasJframe.setBounds( 0, 270,
                            screenDim.width - 100,
                            screenDim.height - 280);
    //how to action, when close
    canvasJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //canvas
    myCanv=new MyCanvas();
    myCanv.setPreferredSize(new Dimension(500, 500));//what do i do ?
    myCanv.setBackground(new Color(200,200,200));
    myCanv.addMouseListener(this);

    JScrollPane sp = new JScrollPane(myCanv,
                                     ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    canvasJframe.setLayout(new GridLayout(1, 1));
    canvasJframe.add(sp);
    canvasJframe.setVisible(true);
  }


  private float xcutmin=0f;
  private float xcutmax=1f;
  private float ycutmin=0f;
  private float ycutmax=1f;
  private static final int EMPTY=-137928;
  private int[] xstart={EMPTY,EMPTY};
  private double xRealStart=0.0;
  private int[] xend={EMPTY,EMPTY};
  private double xRealEnd=10.0;
  private int[] ystart={EMPTY,EMPTY};
  private double yRealStart=0.0;
  private int[] yend={EMPTY,EMPTY};
  private double yRealEnd=10.0;
  private JRadioButton rbXStart,rbXEnd,rbYStart,rbYEnd,rbPoints;
  private JSpinner spXStart,spXEnd,spYStart,spYEnd;
  private JButton openButton;
  private JButton chessButton;
  private JButton cityButton;
  private JButton localMaxButton;
  private JButton simpleMaskButton;
  private JButton hildthMaskButton;
  private JButton traceButton;
  private JButton writeButton;
  private JButton resetButton;
  private JLabel xminLabel,xmaxLabel;
  private JLabel yminLabel,ymaxLabel;
  private JSlider xminSlider, xmaxSlider;
  private JSlider yminSlider, ymaxSlider;
  private void makeControlFrame(){
    JFrame ctrlJframe=new JFrame("Gtracer");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,
                          screenDim.width - 100,
                          240);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //button
    openButton=new JButton("open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);
    //button
    chessButton=new JButton("chess");
    chessButton.addActionListener( this );
    chessButton.setFocusable(false);
    cityButton=new JButton("city");
    cityButton.addActionListener( this );
    cityButton.setFocusable(false);
    //thining
    localMaxButton=new JButton("localMax");
    localMaxButton.addActionListener( this );
    localMaxButton.setFocusable(false);
    simpleMaskButton=new JButton("simple Mask");
    simpleMaskButton.addActionListener( this );
    simpleMaskButton.setFocusable(false);
    hildthMaskButton=new JButton("Hildth Mask");
    hildthMaskButton.addActionListener( this );
    hildthMaskButton.setFocusable(false);


    traceButton=new JButton("trace");
    traceButton.addActionListener( this );
    traceButton.setFocusable(false);

    writeButton=new JButton("write file");
    writeButton.addActionListener( this );
    writeButton.setFocusable(false);

    resetButton=new JButton("reset");
    resetButton.addActionListener( this );
    resetButton.setFocusable(false);

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

    rbPoints=new JRadioButton("Points", true);
    rbPoints.addChangeListener(this);
    rbXStart=new JRadioButton("x start", false);
    rbXStart.addChangeListener(this);
    rbXEnd  =new JRadioButton("x end", false);
    rbXEnd.addChangeListener(this);
    rbYStart=new JRadioButton("y start", false);
    rbYStart.addChangeListener(this);
    rbYEnd  =new JRadioButton("y end", false);
    rbYEnd.addChangeListener(this);

    ButtonGroup group = new ButtonGroup();
    group.add(rbPoints);
    group.add(rbXStart);
    group.add(rbXEnd);
    group.add(rbYStart);
    group.add(rbYEnd);


    spXStart = new JSpinner(new SpinnerNumberModel(xRealStart, null, null, 1));
    spXStart.setFocusable(false);
    spXStart.setPreferredSize(new Dimension(55, 25));
    spXStart.addChangeListener(this);
    spXEnd = new JSpinner(new SpinnerNumberModel(xRealEnd, null, null, 1));
    spXEnd.setFocusable(false);
    spXEnd.setPreferredSize(new Dimension(55, 25));
    spXEnd.addChangeListener(this);
    spYStart = new JSpinner(new SpinnerNumberModel(yRealStart, null, null, 1));
    spYStart.setFocusable(false);
    spYStart.setPreferredSize(new Dimension(55, 25));
    spYStart.addChangeListener(this);
    spYEnd = new JSpinner(new SpinnerNumberModel(yRealEnd, null, null, 1));
    spYEnd.setFocusable(false);
    spYEnd.setPreferredSize(new Dimension(55, 25));
    spYEnd.addChangeListener(this);

    //panel
    JPanel jp=new JPanel();
    //set layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout( layout );

    //open
    layout.putConstraint( SpringLayout.NORTH, openButton, 0,SpringLayout.NORTH, jp );
    layout.putConstraint( SpringLayout.WEST, openButton, 5,SpringLayout.WEST, jp );

    //cut
    layout.putConstraint( SpringLayout.NORTH, xminLabel, 10,SpringLayout.SOUTH, openButton);
    layout.putConstraint( SpringLayout.WEST, xminLabel, 5,SpringLayout.WEST, openButton);
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

    //radiobutton
    layout.putConstraint( SpringLayout.NORTH, rbPoints, 10,SpringLayout.SOUTH, xminLabel);
    layout.putConstraint( SpringLayout.WEST, rbPoints, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, rbXStart, 0,SpringLayout.SOUTH, rbPoints);
    layout.putConstraint( SpringLayout.WEST, rbXStart, 10,SpringLayout.EAST, rbPoints);
    layout.putConstraint( SpringLayout.SOUTH, spXStart, 0,SpringLayout.SOUTH, rbXStart);
    layout.putConstraint( SpringLayout.WEST, spXStart, 0,SpringLayout.EAST, rbXStart);
    layout.putConstraint( SpringLayout.SOUTH, rbXEnd, 0,SpringLayout.SOUTH, spXStart);
    layout.putConstraint( SpringLayout.WEST, rbXEnd, 0,SpringLayout.EAST, spXStart);
    layout.putConstraint( SpringLayout.SOUTH, spXEnd, 0,SpringLayout.SOUTH, rbXEnd);
    layout.putConstraint( SpringLayout.WEST, spXEnd, 0,SpringLayout.EAST, rbXEnd);
    layout.putConstraint( SpringLayout.SOUTH, rbYStart, 0,SpringLayout.SOUTH, spXEnd);
    layout.putConstraint( SpringLayout.WEST, rbYStart, 10,SpringLayout.EAST, spXEnd);
    layout.putConstraint( SpringLayout.SOUTH, spYStart, 0,SpringLayout.SOUTH, rbYStart);
    layout.putConstraint( SpringLayout.WEST, spYStart, 0,SpringLayout.EAST, rbYStart);
    layout.putConstraint( SpringLayout.SOUTH, rbYEnd, 0,SpringLayout.SOUTH, spYStart);
    layout.putConstraint( SpringLayout.WEST, rbYEnd, 0,SpringLayout.EAST, spYStart);
    layout.putConstraint( SpringLayout.SOUTH, spYEnd, 0,SpringLayout.SOUTH, rbYEnd);
    layout.putConstraint( SpringLayout.WEST, spYEnd, 0,SpringLayout.EAST, rbYEnd);


    //lengthmap
    layout.putConstraint( SpringLayout.NORTH, chessButton, 5,SpringLayout.SOUTH, rbPoints);
    layout.putConstraint( SpringLayout.WEST, chessButton, 5,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.SOUTH, cityButton, 0,SpringLayout.SOUTH, chessButton);
    layout.putConstraint( SpringLayout.WEST, cityButton, 0,SpringLayout.EAST, chessButton);
    //
    layout.putConstraint( SpringLayout.NORTH, localMaxButton, 5,SpringLayout.SOUTH, cityButton);
    layout.putConstraint( SpringLayout.WEST, localMaxButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, simpleMaskButton, 0,SpringLayout.SOUTH, localMaxButton);
    layout.putConstraint( SpringLayout.WEST, simpleMaskButton, 0,SpringLayout.EAST, localMaxButton);
    layout.putConstraint( SpringLayout.SOUTH, hildthMaskButton, 0,SpringLayout.SOUTH, simpleMaskButton);
    layout.putConstraint( SpringLayout.WEST, hildthMaskButton, 0,SpringLayout.EAST, simpleMaskButton);

    //trace
    layout.putConstraint( SpringLayout.NORTH, traceButton, 5,SpringLayout.SOUTH, localMaxButton);
    layout.putConstraint( SpringLayout.WEST, traceButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, writeButton, 0,SpringLayout.SOUTH, traceButton);
    layout.putConstraint( SpringLayout.WEST, writeButton, 0,SpringLayout.EAST, traceButton);
    layout.putConstraint( SpringLayout.SOUTH, resetButton, 0,SpringLayout.SOUTH, writeButton);
    layout.putConstraint( SpringLayout.WEST, resetButton, 0,SpringLayout.EAST, writeButton);






    //add to jpanel
    jp.add(openButton);
    jp.add(rbPoints);
    jp.add(rbXStart);
    jp.add(rbXEnd);
    jp.add(rbYStart);
    jp.add(rbYEnd);
    jp.add(spXStart);
    jp.add(spXEnd);
    jp.add(spYStart);
    jp.add(spYEnd);


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
    jp.add(localMaxButton);
    jp.add(simpleMaskButton);
    jp.add(hildthMaskButton);

    jp.add(traceButton);
    jp.add(writeButton);
    jp.add(resetButton);

    ctrlJframe.add(jp);
    ctrlJframe.setVisible(true);
  }

  /** private class for rendering image*/
  private class MyCanvas extends JPanel{
    public void paint(Graphics g){
      Graphics2D g2 = (Graphics2D)g;

      if(tracedImg==null){
        g.drawImage(originalImg,0,0,this);
      }else{
        g.drawImage(tracedImg,0,0,this);
      }

      //draw axis
      g2.setStroke(new BasicStroke(1.5f)); //線の種類を設定
      //x-axis
      g.setColor(Color.red);
      if(xstart[0]!=EMPTY && xend[0]!=EMPTY){
        g.drawLine(xstart[0],xstart[1],xend[0],xend[1]);
      }else if(rbXStart.isSelected() || rbXEnd.isSelected()){
        int r=12;
        g.drawOval(mousePos[0]-r/2,mousePos[1]-r/2,r,r);
      }
      //y-axis
      g.setColor(Color.green);
      if(ystart[0]!=EMPTY && yend[0]!=EMPTY){
        g.drawLine(ystart[0],ystart[1],yend[0],yend[1]);
      }else if(rbYStart.isSelected() || rbYEnd.isSelected()){
        int r=12;
        g.drawOval(mousePos[0]-r/2,mousePos[1]-r/2,r,r);
      }

      //draw points
      g2.setStroke(new BasicStroke(1.0f)); //線の種類を設定
      g.setColor(Color.blue);
      //selected point
      for(int i=0;i<pQueue.size()/2;i++){
        int r=12;
        Dimension size = myCanv.getSize();
        int x=pQueue.get(2*i  )-r/2;
        int y=pQueue.get(2*i+1)-r/2;
        g.drawOval(x,y,r,r);
      }
      //traced point
      for(int i=0;i<tracedPos.size()/2;i++){
        int r=5;
        //Dimension size = myCanv.getSize();
        int x=tracedPos.get(2*i  )-r/2;
        int y=tracedPos.get(2*i+1)-r/2;
        g.drawRect(x,y,r,r);
      }

    }
  }//end of mycanvas

}
