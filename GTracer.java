import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

//handmade library
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

  private LinkedList<Integer> assistPoints= new LinkedList<Integer>();
  private ArrayList<Integer> tracedPoints= new ArrayList<Integer>();
  private int[] mousePos=new int[2];

  public void mousePressed(MouseEvent e){
    int x=e.getX();
    int y=e.getY();
    mousePos=tracer.getPoint(x,y);
    //left click
    if((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0){
      switch(clickType){
      case 0:
        xstart[0]=mousePos[0];
        xstart[1]=mousePos[1];
        ystart[0]=mousePos[0];
        ystart[1]=mousePos[1];
        break;
      case 1:
        addAssistPoints(mousePos);
        break;
      case 2:
        setBorder(tracer.getRGBRef());
        break;
      case 3:
        xstart[0]=mousePos[0];
        xstart[1]=mousePos[1];
        break;
      case 4:
        xend[0]=mousePos[0];
        xend[1]=mousePos[1];
        break;
      case 5:
        ystart[0]=mousePos[0];
        ystart[1]=mousePos[1];
      case 6:
        yend[0]=mousePos[0];
        yend[1]=mousePos[1];
        break;
      default:
        break;
      }
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
    String cmd = ae.getActionCommand();
    if(cmd.startsWith("open")){
      reset();
      this.open(null);
      myCanv.repaint();
    }else if(cmd.startsWith("check update")){
      UpdateManager up=new UpdateManager();
      up.showDialog();
    }else if(cmd.startsWith("exit")){
      System.exit(0);
    }

    if(ae.getSource() == clickTypeCombo){
      clickType=clickTypeCombo.getSelectedIndex();
    }else if(ae.getSource() == setAxisButton){
      int[] a=tracer.setAxis(xstart);
      if(a!=null){
        xend[0]=a[0];
        xend[1]=a[1];
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
      if(assistPoints.size()>=4){
        tracedPoints=tracer.trace(assistPoints);
        //tracedPoints=tracer.traceByTamura(assistPoints);
      }
    }else if(ae.getSource() == writeButton){
      this.writeTracedPoint();
      //}else if(ae.getSource() == resetButton){
      //reset();
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

  private final String[] clickTypeString = {"set origin","set assist-points","select color",
                                            "set x1","set x2","set y1","set y2"};
  private int clickType=0;

  private JComboBox clickTypeCombo;


  private JFrame ctrlJframe;

  private JButton setAxisButton;
  private JSpinner spXStart,spXEnd,spYStart,spYEnd;
  private JPanel axisPanel(){
    ///////////////
    JPanel jp=new JPanel();

    setAxisButton=new JButton("auto axis set");
    setAxisButton.addActionListener( this );
    setAxisButton.setFocusable(false);
    //label
    JLabel labelXStart=new JLabel("x1");
    JLabel labelXEnd  =new JLabel("x2");
    JLabel labelYStart=new JLabel("y1");
    JLabel labelYEnd  =new JLabel("y2");
    //spinner
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

    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);
    jp.setPreferredSize(new Dimension(200, 100));

    layout.putConstraint( SpringLayout.NORTH,setAxisButton, 0,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST, setAxisButton, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH, labelXStart, 10,SpringLayout.SOUTH, setAxisButton);
    layout.putConstraint( SpringLayout.WEST, labelXStart, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH, spXStart, 0,SpringLayout.SOUTH, labelXStart);
    layout.putConstraint( SpringLayout.WEST, spXStart, 5,SpringLayout.EAST, labelXStart);

    layout.putConstraint( SpringLayout.SOUTH, labelXEnd, 0,SpringLayout.SOUTH, spXStart);
    layout.putConstraint( SpringLayout.WEST, labelXEnd, 10,SpringLayout.EAST, spXStart);
    layout.putConstraint( SpringLayout.SOUTH, spXEnd, 0,SpringLayout.SOUTH, labelXEnd);
    layout.putConstraint( SpringLayout.WEST, spXEnd, 5,SpringLayout.EAST, labelXEnd);

    layout.putConstraint( SpringLayout.NORTH, labelYStart, 10,SpringLayout.SOUTH, labelXStart);
    layout.putConstraint( SpringLayout.WEST, labelYStart, 0,SpringLayout.WEST, labelXStart);
    layout.putConstraint( SpringLayout.SOUTH, spYStart, 0,SpringLayout.SOUTH, labelYStart);
    layout.putConstraint( SpringLayout.WEST, spYStart, 5,SpringLayout.EAST, labelYStart);

    layout.putConstraint( SpringLayout.SOUTH, labelYEnd, 0,SpringLayout.SOUTH, spYStart);
    layout.putConstraint( SpringLayout.WEST, labelYEnd, 10,SpringLayout.EAST, spYStart);
    layout.putConstraint( SpringLayout.SOUTH, spYEnd, 0,SpringLayout.SOUTH, labelYEnd);
    layout.putConstraint( SpringLayout.WEST, spYEnd, 5,SpringLayout.EAST, labelYEnd);

    jp.add(setAxisButton);
    jp.add(labelXStart);
    jp.add(labelXEnd);
    jp.add(labelYStart);
    jp.add(labelYEnd);
    jp.add(spXStart);
    jp.add(spXEnd);
    jp.add(spYStart);
    jp.add(spYEnd);

    LineBorder border = new LineBorder(Color.blue, 2, true);
    jp.setBorder(border);
    return jp;
  }
  private JButton colorCutButton;
  private JButton colorResetButton;
  private JTextField colorText;
  private JLabel colorLabel;

  private JPanel colorPanel(){
    JPanel jp=new JPanel();

    colorText = new JTextField();
    colorText.setText("selected color: null");
    colorLabel = new JLabel();
    colorLabel.setPreferredSize(new Dimension(20, 20));

    colorCutButton=new JButton("color cut");
    colorResetButton=new JButton("reset");
    jp.add(colorText);
    jp.add(colorLabel);
    jp.add(colorCutButton);
    jp.add(colorResetButton);

    LineBorder border = new LineBorder(Color.blue, 2, true);
    jp.setBorder(border);
    return jp;
  }

  private JButton assitPointResetButton;
  private JPanel assistPanel(){
    JPanel jp=new JPanel();
    assitPointResetButton=new JButton("reset");
    jp.add(assitPointResetButton);

    LineBorder border = new LineBorder(Color.blue, 2, true);
    jp.setBorder(border);
    return jp;
  }
  private JPanel settingPanel(){
    //setting panel
    JPanel jp=new JPanel();
    jp.setPreferredSize(new Dimension(200,300));

    JLabel clickTypeLabel=new JLabel("click type");
    clickTypeCombo = new JComboBox(clickTypeString);
    clickTypeCombo.setSelectedIndex(clickType);
    clickTypeCombo.addActionListener(this);
    JLabel axisLabel=new JLabel("axis");
    JPanel axisPanel=axisPanel();
    JLabel colorLabel=new JLabel("color");
    JPanel colorPanel=colorPanel();
    JLabel assistLabel=new JLabel("Assist Points");
    JPanel assistPanel=assistPanel();

    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    layout.putConstraint( SpringLayout.NORTH,clickTypeLabel, 0,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,clickTypeLabel, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,clickTypeCombo, 0,SpringLayout.NORTH, clickTypeLabel);
    layout.putConstraint( SpringLayout.WEST,clickTypeCombo, 5,SpringLayout.EAST, clickTypeLabel);

    layout.putConstraint( SpringLayout.NORTH,axisLabel, 0,SpringLayout.SOUTH, clickTypeLabel);
    layout.putConstraint( SpringLayout.WEST,axisLabel, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,axisPanel, 0,SpringLayout.SOUTH, axisLabel);
    layout.putConstraint( SpringLayout.WEST,axisPanel, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,colorLabel, 0,SpringLayout.SOUTH, axisPanel);
    layout.putConstraint( SpringLayout.WEST,colorLabel, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,colorPanel, 0,SpringLayout.SOUTH, colorLabel);
    layout.putConstraint( SpringLayout.WEST,colorPanel, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,assistLabel, 0,SpringLayout.SOUTH, colorPanel);
    layout.putConstraint( SpringLayout.WEST,assistLabel,5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,assistPanel, 0,SpringLayout.SOUTH, assistLabel);
    layout.putConstraint( SpringLayout.WEST,assistPanel, 5,SpringLayout.WEST, jp);

    jp.add(clickTypeLabel);
    jp.add(clickTypeCombo);
    jp.add(axisLabel);
    jp.add(axisPanel);
    jp.add(colorLabel);
    jp.add(colorPanel);
    jp.add(assistLabel);
    jp.add(assistPanel);

    LineBorder border = new LineBorder(Color.red, 2, true);
    jp.setBorder(border);
    return jp;
  }

  private JButton openButton;
  private JButton binalizeButton;
  private JButton localMaxButton;
  private JButton simpleMaskButton;
  private JButton traceButton;
  private JButton writeButton;
  private JPanel opPanel(){
    JPanel jp=new JPanel();

    openButton=new JButton("open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);

    binalizeButton=new JButton("binalize");
    binalizeButton.addActionListener( this );
    binalizeButton.setFocusable(false);

    colorCutButton=new JButton("color cut");
    colorCutButton.addActionListener( this );
    colorCutButton.setFocusable(false);
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


    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);
    jp.setPreferredSize(new Dimension(100,100));

    layout.putConstraint( SpringLayout.NORTH,openButton, 0,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,openButton, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,binalizeButton, 0,SpringLayout.SOUTH, openButton);
    layout.putConstraint( SpringLayout.WEST,binalizeButton, 0,SpringLayout.WEST, openButton);
    layout.putConstraint( SpringLayout.NORTH,localMaxButton, 0,SpringLayout.NORTH, binalizeButton);
    layout.putConstraint( SpringLayout.WEST, localMaxButton, 0,SpringLayout.EAST, binalizeButton);
    layout.putConstraint( SpringLayout.NORTH,simpleMaskButton, 0,SpringLayout.NORTH, localMaxButton);
    layout.putConstraint( SpringLayout.WEST,simpleMaskButton, 0,SpringLayout.EAST, localMaxButton);

    layout.putConstraint( SpringLayout.NORTH,traceButton, 0,SpringLayout.SOUTH, binalizeButton);
    layout.putConstraint( SpringLayout.WEST,traceButton, 0,SpringLayout.WEST, binalizeButton);
    layout.putConstraint( SpringLayout.NORTH,writeButton, 0,SpringLayout.NORTH, traceButton);
    layout.putConstraint( SpringLayout.WEST,writeButton, 0,SpringLayout.EAST, traceButton);

    jp.add(openButton);
    jp.add(binalizeButton);
    jp.add(localMaxButton);
    jp.add(simpleMaskButton);
    jp.add(traceButton);
    jp.add(writeButton);

    LineBorder border = new LineBorder(Color.red, 2, true);
    jp.setBorder(border);
    return jp;
  }
  private void makeControlFrame(){
    ctrlJframe=new JFrame("Gtracer");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,
                          440,screenDim.height-100);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ctrlJframe.setJMenuBar(getControlMenu());

    ///////////////

    //panel
    JPanel jp=new JPanel();
    //set layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    JLabel settingLabel=new JLabel("setting");
    JPanel spanel=settingPanel();
    JLabel opLabel=new JLabel("operations");
    JPanel oppanel=opPanel();

    try{
      //loupe http://sawat.jf.land.to/loupe.html
      loupe = new Loupe();
      loupe.setPreferredSize(new Dimension(150,150));
      //loupe.setBackground(Color.gray);

      layout.putConstraint( SpringLayout.EAST,oppanel, -5,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST,oppanel, 5,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.SOUTH,oppanel, -5,SpringLayout.SOUTH, jp);
      layout.putConstraint( SpringLayout.WEST,opLabel, 5,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.SOUTH,opLabel, 0,SpringLayout.NORTH, oppanel);

      layout.putConstraint( SpringLayout.SOUTH,spanel, -5,SpringLayout.NORTH, opLabel);
      layout.putConstraint( SpringLayout.EAST,spanel, -5,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST,spanel, 5,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.WEST,settingLabel, 0,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.SOUTH,settingLabel, 0,SpringLayout.NORTH, spanel);

      layout.putConstraint( SpringLayout.EAST,loupe, 0,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST,loupe, 0,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.SOUTH,loupe, 0,SpringLayout.NORTH, settingLabel);
      layout.putConstraint( SpringLayout.NORTH,loupe, 0,SpringLayout.NORTH, jp);




      jp.add(loupe);
      jp.add(opLabel);
      jp.add(oppanel);
      jp.add(settingLabel);
      jp.add(spanel);
    }catch(AWTException e){
      e.printStackTrace();
      System.exit(1);
    }

    ctrlJframe.add(jp);
    ctrlJframe.setVisible(true);
  }

  private JMenuBar getControlMenu(){
    JMenu menu=new JMenu("File");
    JMenuItem openMenu=new JMenuItem("Open");
    openMenu.addActionListener( this );
    openMenu.setActionCommand("open");
    JMenuItem updateCheckMenu=new JMenuItem("Check Update");
    updateCheckMenu.addActionListener( this );
    updateCheckMenu.setActionCommand("check update");
    JMenuItem exitMenu=new JMenuItem("Exit");
    exitMenu.addActionListener( this );
    exitMenu.setActionCommand("exit");

    menu.add(openMenu);
    menu.add(updateCheckMenu);
    menu.add(exitMenu);

    //menu bar
    JMenuBar menuBar;
    menuBar  = new JMenuBar();
    menuBar.add(menu);
    return menuBar;
  }


  public void setBorder(int icolor){
    Color color = new Color(icolor);
    String hexColor = toHexString(color);
    colorText.setText(hexColor);
    Border border = BorderFactory.
      createCompoundBorder(BorderFactory.createEtchedBorder(),
                           BorderFactory.createLineBorder(color, 3));
    colorLabel.setBorder(border);
    colorLabel.setBackground(color);

  }
  private void appendHex(StringBuffer buffer, int i) {
    if(i < 0x10)buffer.append('0');
    buffer.append(Integer.toHexString(i));
  }
  private String toHexString(Color color) {
    StringBuffer buffer = new StringBuffer(9);
    buffer.append('#');
    appendHex(buffer, color.getAlpha());
    appendHex(buffer, color.getRed());
    appendHex(buffer, color.getGreen());
    appendHex(buffer, color.getBlue());

    return buffer.toString();
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
      }
      //y-axis
      g.setColor(Color.green);
      if(ystart[0]!=EMPTY && yend[0]!=EMPTY){
        g.drawLine(ystart[0],ystart[1],yend[0],yend[1]);
      }
      int ra=5;
      int rashift=2;
      if(xstart[0]!=EMPTY)g.fill3DRect(xstart[0]-rashift,xstart[1]-rashift,ra,ra,false);
      if(xend[0]!=EMPTY)g.fill3DRect(xend[0]-rashift,xend[1]-rashift,ra,ra,false);
      if(ystart[0]!=EMPTY)g.fill3DRect(ystart[0]-rashift,ystart[1]-rashift,ra,ra,false);
      if(yend[0]!=EMPTY)g.fill3DRect(yend[0]-rashift,yend[1]-rashift,ra,ra,false);

      //draw points
      g2.setStroke(new BasicStroke(1.0f)); //線の種類を設定
      g.setColor(Color.blue);
      //selected point
      for(int i=0;i<assistPoints.size()/2;i++){
        int r=13;
        //circle
        int x=assistPoints.get(2*i  )-r/2;
        int y=assistPoints.get(2*i+1)-r/2;
        g.drawOval(x,y,r,r);
        //point
        r=1;
        x=assistPoints.get(2*i  );
        y=assistPoints.get(2*i+1);
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
