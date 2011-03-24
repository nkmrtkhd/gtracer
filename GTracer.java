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


public class GTracer implements ActionListener,MouseListener,ChangeListener{

  //main function
  public static void main(String[] args){
    if(args.length>0)
      new GTracer(args[0]);
    else
      new GTracer(null);
  }

  //class variables
  private BufferedImage originalImg;
  private BufferedImage tracedImg;
  private Tracer tracer;
  private Loupe loupe;

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

  LinkedList<Integer> assistPoints= new LinkedList<Integer>();
  ArrayList<Integer> tracedPoints= new ArrayList<Integer>();
  int[] mousePos=new int[2];
  public void mousePressed(MouseEvent e){
    int x=e.getX();
    int y=e.getY();
    mousePos=tracer.getPoint(x,y);
    //left click
    if((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0){
      if(rbPoints.isSelected()){
        addAssistPoints(mousePos);
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
    }
    //right click
    if((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0){
      loupe.setBorder(tracer.getRGBRef());
    }

    myCanv.repaint();
  }
  public void mouseReleased(MouseEvent e){
  }
  private void addAssistPoints(int[] pos){
    //search that pos[0] should be, and insert it
    boolean addEnd=true;
    for(int i=0;i<assistPoints.size()/2;i++){
      if(pos[0]<assistPoints.get(2*i)){
        assistPoints.add(2*i,pos[0]);
        assistPoints.add(2*i+1,pos[1]);
        addEnd=false;
        break;
      }
    }
    //if there is no correct position, add last
    if(addEnd){
      assistPoints.add(pos[0]);
      assistPoints.add(pos[1]);
    }
  }

  public void stateChanged(ChangeEvent ce){
    xRealStart=((Double)spXStart.getValue()).doubleValue();
    xRealEnd=((Double)spXEnd.getValue()).doubleValue();
    yRealStart=((Double)spYStart.getValue()).doubleValue();
    yRealEnd=((Double)spYEnd.getValue()).doubleValue();
    myCanv.repaint();
  }

  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == openButton){
      reset();
      this.open(null);
      myCanv.repaint();
    }else if(ae.getSource() == setAxisButton){
      int[] a=tracer.setAxis(xstart);
      if(a!=null){
        xend[0]  =a[0];
        xend[1]  =a[1];
        yend[0]=a[2];
        yend[1]=a[3];
        myCanv.repaint();
      }
    }else if(ae.getSource() == binalizeButton){
      tracer.setLengthMap(false);
      tracedImg=null;
      tracedImg=tracer.makeImage(0);
    }else if(ae.getSource() == colorCutButton){
      tracer.setLengthMap(true);
      tracedImg=null;
      tracedImg=tracer.makeImage(0);
    }else if(ae.getSource() == localMaxButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(1);
    }else if(ae.getSource() == simpleMaskButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(2);
    }else if(ae.getSource() == traceButton){
      if(assistPoints.size()>=4) tracedPoints=tracer.trace(assistPoints);
    }else if(ae.getSource() == writeButton){
      this.writeTracedPoint();
    }else if(ae.getSource() == resetButton){
      reset();
    }
    myCanv.repaint();
  }
  private void reset(){
    assistPoints.clear();
    tracedPoints.clear();
    if(tracer!=null)tracer.setLengthMap(false);
    tracedImg=null;
  }


  /** 引数がnullだったらダイアログで選んで，open */
  private void open(String filename){
    if(filename==null){
      String currentDir=System.getProperty("user.dir");
      JFileChooser jfc = new JFileChooser( (new File(currentDir)).getAbsolutePath() );

      ImagePreview preview = new ImagePreview(jfc);
      jfc.addPropertyChangeListener(preview);
      jfc.setAccessory(preview);
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
        for(int i=0;i<tracedPoints.size()/2;i++){
          double x=(tracedPoints.get(2*i)-xstart[0])*dxi*dxreal+xRealStart;
          double y=(ystart[1]-tracedPoints.get(2*i+1))*dyi*dyreal+yRealStart;
          pw.println( String.format("%f %f", x,y) );
        }

        pw.close();
        bw.close();
        fw.close();
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
    canvasJframe.setBounds( 450, 0,
                            screenDim.width - 500,
                            screenDim.height - 100);
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
  private JButton setAxisButton;
  private JButton binalizeButton;
  private JButton colorCutButton;
  private JButton localMaxButton;
  private JButton simpleMaskButton;
  private JButton traceButton;
  private JButton writeButton;
  private JButton resetButton;

  private JFrame ctrlJframe;
  private void makeControlFrame(){
    ctrlJframe=new JFrame("Gtracer");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,
                          440,
                          screenDim.height-100);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //button
    openButton=new JButton("open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);
    setAxisButton=new JButton("set axis");
    setAxisButton.addActionListener( this );
    setAxisButton.setFocusable(false);

    //button
    binalizeButton=new JButton("binalize");
    binalizeButton.addActionListener( this );
    binalizeButton.setFocusable(false);
    colorCutButton=new JButton("color cut");
    colorCutButton.addActionListener( this );
    colorCutButton.setFocusable(false);
    //thining
    localMaxButton=new JButton("local max mask");
    localMaxButton.addActionListener( this );
    localMaxButton.setFocusable(false);
    simpleMaskButton=new JButton("simple mask");
    simpleMaskButton.addActionListener( this );
    simpleMaskButton.setFocusable(false);


    traceButton=new JButton("trace");
    traceButton.addActionListener( this );
    traceButton.setFocusable(false);

    writeButton=new JButton("write file");
    writeButton.addActionListener( this );
    writeButton.setFocusable(false);

    resetButton=new JButton("reset");
    resetButton.addActionListener( this );
    resetButton.setFocusable(false);


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

    try{
      //loupe http://sawat.jf.land.to/loupe.html
      loupe = new Loupe();
      loupe.setPreferredSize(new Dimension(400,400));

      //layout.putConstraint( SpringLayout.SOUTH, loupe, 0,SpringLayout.SOUTH, jp);
      layout.putConstraint( SpringLayout.NORTH, loupe, 0,SpringLayout.NORTH, jp);
      layout.putConstraint( SpringLayout.EAST, loupe, 10,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST, loupe, 10,SpringLayout.WEST, jp);
      jp.add(loupe);
    }catch(AWTException e){
      e.printStackTrace();
    }


    //open
    layout.putConstraint( SpringLayout.NORTH, openButton, 0,SpringLayout.SOUTH, loupe);
    layout.putConstraint( SpringLayout.WEST, openButton, 5,SpringLayout.WEST, jp );

    layout.putConstraint( SpringLayout.NORTH, setAxisButton, 0,SpringLayout.NORTH, openButton);
    layout.putConstraint( SpringLayout.WEST, setAxisButton, 5,SpringLayout.EAST, openButton);

    //radiobutton
    layout.putConstraint( SpringLayout.NORTH, rbYEnd, 10,SpringLayout.SOUTH, openButton);
    layout.putConstraint( SpringLayout.WEST, rbYEnd, 0,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, spYEnd, 0,SpringLayout.SOUTH, rbYEnd);
    layout.putConstraint( SpringLayout.WEST, spYEnd, 0,SpringLayout.EAST, rbYEnd);

    layout.putConstraint( SpringLayout.NORTH, rbYStart, 10,SpringLayout.SOUTH, rbYEnd);
    layout.putConstraint( SpringLayout.WEST, rbYStart, 0,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, spYStart, 0,SpringLayout.SOUTH, rbYStart);
    layout.putConstraint( SpringLayout.WEST, spYStart, 0,SpringLayout.EAST, rbYStart);

    layout.putConstraint( SpringLayout.NORTH, rbXStart, 10,SpringLayout.SOUTH, rbYStart);
    layout.putConstraint( SpringLayout.WEST, rbXStart, 0,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, spXStart, 0,SpringLayout.SOUTH, rbXStart);
    layout.putConstraint( SpringLayout.WEST, spXStart, 0,SpringLayout.EAST, rbXStart);
    layout.putConstraint( SpringLayout.SOUTH, rbXEnd, 0,SpringLayout.SOUTH, spXStart);
    layout.putConstraint( SpringLayout.WEST, rbXEnd, 0,SpringLayout.EAST, spXStart);
    layout.putConstraint( SpringLayout.SOUTH, spXEnd, 0,SpringLayout.SOUTH, rbXEnd);
    layout.putConstraint( SpringLayout.WEST, spXEnd, 0,SpringLayout.EAST, rbXEnd);

    layout.putConstraint( SpringLayout.NORTH, rbPoints, 10,SpringLayout.SOUTH, rbXStart);
    layout.putConstraint( SpringLayout.WEST, rbPoints, 0,SpringLayout.WEST, jp);



    //lengthmap
    layout.putConstraint( SpringLayout.NORTH, binalizeButton, 10,SpringLayout.SOUTH, rbPoints);
    layout.putConstraint( SpringLayout.WEST, binalizeButton, 5,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.SOUTH, colorCutButton, 0,SpringLayout.SOUTH, binalizeButton);
    layout.putConstraint( SpringLayout.WEST, colorCutButton, 0,SpringLayout.EAST, binalizeButton);
    layout.putConstraint( SpringLayout.NORTH, resetButton, 0,SpringLayout.NORTH, colorCutButton);
    layout.putConstraint( SpringLayout.WEST, resetButton, 0,SpringLayout.EAST, colorCutButton);
    //mask
    layout.putConstraint( SpringLayout.NORTH, localMaxButton, 5,SpringLayout.SOUTH, colorCutButton);
    layout.putConstraint( SpringLayout.WEST, localMaxButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, simpleMaskButton, 0,SpringLayout.SOUTH, localMaxButton);
    layout.putConstraint( SpringLayout.WEST, simpleMaskButton, 0,SpringLayout.EAST, localMaxButton);

    //trace
    layout.putConstraint( SpringLayout.NORTH, traceButton, 5,SpringLayout.SOUTH, localMaxButton);
    layout.putConstraint( SpringLayout.WEST, traceButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH, writeButton, 0,SpringLayout.SOUTH, traceButton);
    layout.putConstraint( SpringLayout.WEST, writeButton, 0,SpringLayout.WEST, traceButton);


    //add to jpanel
    jp.add(openButton);
    jp.add(setAxisButton);
    jp.add(rbPoints);
    jp.add(rbXStart);
    jp.add(rbXEnd);
    jp.add(rbYStart);
    jp.add(rbYEnd);
    jp.add(spXStart);
    jp.add(spXEnd);
    jp.add(spYStart);
    jp.add(spYEnd);


    jp.add(binalizeButton);
    jp.add(colorCutButton);
    jp.add(localMaxButton);
    jp.add(simpleMaskButton);

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

      g2.clearRect(0, 0, getWidth(), getHeight());
      if(tracedImg==null){
        g.drawImage(originalImg,0,0,this);
      }else{
        g.drawImage(tracedImg,0,0,this);
      }

      //draw axis
      g2.setStroke(new BasicStroke(2f)); //線の種類を設定
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
      for(int i=0;i<assistPoints.size()/2;i++){
        int r=12;
        //circle
        int x=assistPoints.get(2*i  )-r/2;
        int y=assistPoints.get(2*i+1)-r/2;
        g.drawOval(x,y,r,r);
        //point
        r=1;
        x=assistPoints.get(2*i  );
        y=assistPoints.get(2*i+1);
        //g.drawOval(x,y,r,r);
        g.fillOval(x,y,r,r);
      }
      //traced point
      g.setColor(Color.red);
      for(int i=0;i<tracedPoints.size()/2;i++){
        int x=tracedPoints.get(2*i  );
        int y=tracedPoints.get(2*i+1);
        g.fill3DRect(x,y,3,3,false);
      }
      for(int i=0;i<tracedPoints.size()/2-1;i++){
        int x1=tracedPoints.get(2*i  );
        int y1=tracedPoints.get(2*i+1);
        int x2=tracedPoints.get(2*i +2);
        int y2=tracedPoints.get(2*i+1 +2);
        g.drawLine(x1,y1,x2,y2);
      }

    }
  }//end of mycanvas

}
