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


public class GTracer implements ActionListener,MouseListener,MouseMotionListener{

  //global variable
  private LinkedList<Integer> assistPoints= new LinkedList<Integer>();
  private ArrayList<Integer> erasePoints= new ArrayList<Integer>();
  private ArrayList<Integer> tracedPoints= new ArrayList<Integer>();
  private int[] mousePos=new int[2];
  private MyCanvas myCanv;

  private static final int EMPTY=-137928;
  private int[] xstart={EMPTY,EMPTY};
  private double xRealStart=0.0;
  private int[] xend={EMPTY,EMPTY};
  private double xRealEnd=10.0;
  private int[] ystart={EMPTY,EMPTY};
  private double yRealStart=0.0;
  private int[] yend={EMPTY,EMPTY};
  private double yRealEnd=10.0;


  //for GUI
  private JLabel labelAP;
  private JRadioButton rbSetOrg,rbSetAP,rbDelAP,rbSetColor,rbSetX1,rbSetX2,rbSetY1,rbSetY2,rbEraser;
  private JButton assistPointResetButton;
  private JFrame ctrlJframe;
  private JButton setAxisButton,axisResetButton,axisAssistButton;
  private JTextField tfXStart,tfXEnd,tfYStart,tfYEnd;

  private JButton eraseButton;
  private JButton colorCutButton;
  private JTextField colorText;

  private JButton openButton;
  private JButton binalizeButton;
  private JButton simpleMaskButton;
  private JButton traceButton;
  private JButton writeButton;
  private JButton reloadButton;
  private JButton resetFilterButton;

  private Color borderColor=new Color(80,80,80);
  private Color innerBorderColor=new Color(80,80,80);
  private Color panelColor=new Color(200,200,200);
  private Color innerPanelColor=new Color(200,200,200);



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
    this.setLookAndFeel();
    makeControlFrame();
    makeCanvasFrame();
    if(inputFile!=null)this.open(inputFile);
  }

  //mouse
  public void mouseMoved(MouseEvent e) {
  }
  public void mouseDragged(MouseEvent e) {
    if(rbEraser.isSelected()){
      int x=e.getX();
      int y=e.getY();
      mousePos=tracer.getPoint(x,y);
      erase(mousePos);
      myCanv.repaint();
    }
  }

  public void mouseClicked(MouseEvent e){
  }
  public void mouseEntered(MouseEvent e){
  }
  public void mouseExited(MouseEvent e){
  }

  public void mousePressed(MouseEvent e){
    int x=e.getX();
    int y=e.getY();
    mousePos=tracer.getPoint(x,y);
    //left click
    if((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0){
      if(rbSetOrg.isSelected()){
        xstart[0]=mousePos[0];
        xstart[1]=mousePos[1];
        ystart[0]=mousePos[0];
        ystart[1]=mousePos[1];
      }else if(rbSetAP.isSelected()){
        addAssistPoints(mousePos);
      }else if(rbDelAP.isSelected()){
        deleteAssistPoints(mousePos);
      }else if(rbSetColor.isSelected()){
        setBorder(tracer.getRGBRef());
      }else if(rbEraser.isSelected()){
        erase(mousePos);
      }else if(rbSetX1.isSelected()){
        xstart[0]=mousePos[0];
        xstart[1]=mousePos[1];
      }else if(rbSetX2.isSelected()){
        xend[0]=mousePos[0];
        xend[1]=mousePos[1];
      }else if(rbSetY1.isSelected()){
        ystart[0]=mousePos[0];
        ystart[1]=mousePos[1];
      }else if(rbSetY2.isSelected()){
        yend[0]=mousePos[0];
        yend[1]=mousePos[1];
      }
    }
    labelAP.setText(String.format("# of Assist Points= %d",assistPoints.size()/2));
    myCanv.repaint();
  }
  public void mouseReleased(MouseEvent e){

  }
  private void erase(int[] pos){
    erasePoints.add(pos[0]);
    erasePoints.add(pos[1]);
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

  private void deleteAssistPoints(int[] pos){
    int rmin=1000;
    int it=-1;
    for(int i=0;i<assistPoints.size()/2;i++){
      int dx=pos[0]-assistPoints.get(2*i);
      int dy=pos[1]-assistPoints.get(2*i+1);
      int r=dx*dx+dy*dy;
      if(r<rmin){
        rmin=r;
        it=i;
      }
    }
    if(it>=0){
      assistPoints.remove(2*it+1);
      assistPoints.remove(2*it);
    }
  }


  public void actionPerformed(ActionEvent ae){
    xRealStart=Double.parseDouble(tfXStart.getText());
    xRealEnd=Double.parseDouble(tfXEnd.getText());
    yRealStart=Double.parseDouble(tfYStart.getText());
    yRealEnd=Double.parseDouble(tfYEnd.getText());
    String cmd = ae.getActionCommand();
    if(cmd.startsWith("open")){
      resetAll();
      this.open(null);
    }else if(cmd.startsWith("check update")){
      UpdateManager up=new UpdateManager();
      up.showDialog();
    }else if(cmd.startsWith("exit")){
      System.exit(0);
    }

    if(ae.getSource() == setAxisButton){
      int[] a=tracer.setAxis(xstart);
      if(a!=null){
        xend[0]=a[0];
        xend[1]=a[1];
        yend[0]=a[2];
        yend[1]=a[3];
      }
    }else if(ae.getSource() == axisAssistButton){
      int[] a=tracer.assistAxis(xstart);
      if(a!=null){
        xend[0]=a[0];
        xend[1]=a[1];
        yend[0]=a[2];
        yend[1]=a[3];
      }
    }else if(ae.getSource() == binalizeButton){
      tracer.setLengthMap(false);
      tracedImg=null;
      tracedImg=tracer.makeImage(0);
    }else if(ae.getSource() == eraseButton){
      tracer.erase(erasePoints);
      tracedImg=null;
      tracedImg=tracer.makeImage(0);
      erasePoints.clear();
    }else if(ae.getSource() == colorCutButton){
      tracer.setLengthMap(true);
      tracedImg=null;
      tracedImg=tracer.makeImage(0);
    }else if(ae.getSource() == simpleMaskButton){
      tracedImg=null;
      tracedImg=tracer.makeImage(2);
    }else if(ae.getSource() == resetFilterButton){
      if(tracer!=null)tracer.setLengthMap(false);
      tracedImg=null;
    }else if(ae.getSource() == traceButton){
      if(assistPoints.size()>=4){
        tracedPoints=tracer.trace(assistPoints);
        //tracedPoints=tracer.traceByTamura(assistPoints);
      }
    }else if(ae.getSource() == writeButton){
      this.writeTracedPoint();
    }else if(ae.getSource() == axisResetButton){
      xstart[0]=EMPTY;
      xstart[1]=EMPTY;
      xend[0]=EMPTY;
      xend[1]=EMPTY;
      ystart[0]=EMPTY;
      ystart[1]=EMPTY;
      yend[0]=EMPTY;
      yend[1]=EMPTY;
    }else if(ae.getSource() == assistPointResetButton){
      assistPoints.clear();
      tracedPoints.clear();
    }else if(ae.getSource() == reloadButton){
      resetAll();
    }

    labelAP.setText(String.format("# of Assist Points= %d",assistPoints.size()/2));
    myCanv.repaint();
  }
  private void resetAll(){
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
    myCanv.setPreferredSize(tracer.getSize());
    myCanv.repaint();
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
    myCanv.setPreferredSize(new Dimension(100, 100));//what do i do ?
    myCanv.setBackground(new Color(200,200,200));
    myCanv.addMouseListener(this);
    myCanv.addMouseMotionListener(this);
    JScrollPane sp = new JScrollPane(myCanv,
                                     ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    canvasJframe.setLayout(new GridLayout(1, 1));
    canvasJframe.add(sp);
    canvasJframe.setVisible(true);
  }

  private JPanel axisPanel(){
    ///////////////
    JPanel jp=new JPanel();
    jp.setPreferredSize(new Dimension(400, 145));

    LineBorder lineborder = new LineBorder(innerBorderColor, 2);
    TitledBorder border = new TitledBorder(lineborder,"Axis", TitledBorder.LEFT, TitledBorder.TOP);
    jp.setBorder(border);
    jp.setBackground(innerPanelColor);

    setAxisButton=new JButton("auto set");
    setAxisButton.addActionListener( this );
    setAxisButton.setFocusable(false);
    setAxisButton.setBackground(innerPanelColor);
    axisAssistButton=new JButton("assist");
    axisAssistButton.addActionListener( this );
    axisAssistButton.setFocusable(false);
    axisAssistButton.setBackground(innerPanelColor);
    axisResetButton=new JButton("reset");
    axisResetButton.addActionListener( this );
    axisResetButton.setFocusable(false);
    axisResetButton.setBackground(innerPanelColor);

    //label
    JLabel labelManu=new JLabel("Manual set");
    JLabel labelRange=new JLabel("Range");

    JLabel labelXStart=new JLabel("x start");
    JLabel labelXEnd  =new JLabel("x end");
    JLabel labelYStart=new JLabel("y start");
    JLabel labelYEnd  =new JLabel("y end");
    //spinner
    tfXStart = new JTextField("0");
    tfXStart.setInputVerifier(new MyInputVerifier());
    tfXStart.setPreferredSize(new Dimension(50, 25));
    tfXEnd = new JTextField("1.0");
    tfXEnd.setInputVerifier(new MyInputVerifier());
    tfXEnd.setPreferredSize(new Dimension(50, 25));
    tfYStart = new JTextField("0");
    tfYStart.setInputVerifier(new MyInputVerifier());
    tfYStart.setPreferredSize(new Dimension(50, 25));
    tfYEnd = new JTextField("1.0");
    tfYEnd.setInputVerifier(new MyInputVerifier());
    tfYEnd.setPreferredSize(new Dimension(50, 25));

    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    //auto
    layout.putConstraint( SpringLayout.NORTH,rbSetOrg, 2,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST, rbSetOrg, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH,setAxisButton, 4,SpringLayout.SOUTH, rbSetOrg);
    layout.putConstraint( SpringLayout.WEST, setAxisButton, 5,SpringLayout.EAST, rbSetOrg);
    layout.putConstraint( SpringLayout.SOUTH,axisResetButton, 0,SpringLayout.SOUTH, setAxisButton);
    layout.putConstraint( SpringLayout.EAST, axisResetButton, -5,SpringLayout.EAST, jp);

    //range
    layout.putConstraint( SpringLayout.NORTH,labelRange, 10,SpringLayout.SOUTH, rbSetOrg);
    layout.putConstraint( SpringLayout.WEST, labelRange, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH, labelXStart, 10,SpringLayout.SOUTH, labelRange);
    layout.putConstraint( SpringLayout.WEST, labelXStart, 10,SpringLayout.WEST, labelRange);
    layout.putConstraint( SpringLayout.SOUTH, tfXStart, 0,SpringLayout.SOUTH, labelXStart);
    layout.putConstraint( SpringLayout.WEST, tfXStart, 5,SpringLayout.EAST, labelXStart);

    layout.putConstraint( SpringLayout.SOUTH, labelXEnd, 0,SpringLayout.SOUTH, tfXStart);
    layout.putConstraint( SpringLayout.WEST, labelXEnd, 10,SpringLayout.EAST, tfXStart);
    layout.putConstraint( SpringLayout.SOUTH, tfXEnd, 0,SpringLayout.SOUTH, labelXEnd);
    layout.putConstraint( SpringLayout.WEST, tfXEnd, 5,SpringLayout.EAST, labelXEnd);

    layout.putConstraint( SpringLayout.NORTH, labelYStart, 10,SpringLayout.SOUTH, labelXStart);
    layout.putConstraint( SpringLayout.WEST, labelYStart, 0,SpringLayout.WEST, labelXStart);
    layout.putConstraint( SpringLayout.SOUTH, tfYStart, 0,SpringLayout.SOUTH, labelYStart);
    layout.putConstraint( SpringLayout.WEST, tfYStart, 0,SpringLayout.WEST, tfXStart);

    layout.putConstraint( SpringLayout.SOUTH, labelYEnd, 0,SpringLayout.SOUTH, tfYStart);
    layout.putConstraint( SpringLayout.WEST, labelYEnd, 0,SpringLayout.WEST, labelXEnd);
    layout.putConstraint( SpringLayout.SOUTH, tfYEnd, 0,SpringLayout.SOUTH, labelYEnd);
    layout.putConstraint( SpringLayout.WEST, tfYEnd, 0,SpringLayout.WEST, tfXEnd);

    //manual
    layout.putConstraint( SpringLayout.SOUTH,rbSetY2, -5,SpringLayout.SOUTH, jp);
    layout.putConstraint( SpringLayout.EAST, rbSetY2, -5,SpringLayout.EAST, jp);
    layout.putConstraint( SpringLayout.SOUTH,rbSetY1, 0,SpringLayout.SOUTH, rbSetY2);
    layout.putConstraint( SpringLayout.EAST, rbSetY1, -5,SpringLayout.WEST, rbSetY2);

    layout.putConstraint( SpringLayout.SOUTH,rbSetX2, 0,SpringLayout.NORTH, rbSetY2);
    layout.putConstraint( SpringLayout.WEST, rbSetX2, 0,SpringLayout.WEST, rbSetY2);
    layout.putConstraint( SpringLayout.SOUTH,rbSetX1, 0,SpringLayout.NORTH, rbSetY1);
    layout.putConstraint( SpringLayout.WEST, rbSetX1, 0,SpringLayout.WEST, rbSetY1);



    layout.putConstraint( SpringLayout.SOUTH,axisAssistButton, 0,SpringLayout.NORTH, rbSetX2);
    layout.putConstraint( SpringLayout.EAST, axisAssistButton, 0,SpringLayout.EAST, rbSetX2);

    layout.putConstraint( SpringLayout.SOUTH,labelManu, -3,SpringLayout.SOUTH, axisAssistButton);
    layout.putConstraint( SpringLayout.EAST, labelManu, -5,SpringLayout.WEST, axisAssistButton);

    jp.add(axisAssistButton);
    jp.add(labelManu);
    jp.add(labelRange);
    jp.add(rbSetOrg);
    jp.add(rbSetX1);
    jp.add(rbSetX2);
    jp.add(rbSetY1);
    jp.add(rbSetY2);
    jp.add(setAxisButton);
    jp.add(axisResetButton);
    jp.add(labelXStart);
    jp.add(labelXEnd);
    jp.add(labelYStart);
    jp.add(labelYEnd);
    jp.add(tfXStart);
    jp.add(tfXEnd);
    jp.add(tfYStart);
    jp.add(tfYEnd);

    return jp;
  }

  private JPanel filterPanel(){
    JPanel jp=new JPanel();
    jp.setPreferredSize(new Dimension(200,120));

    LineBorder lineborder = new LineBorder(innerBorderColor, 2);
    TitledBorder border = new TitledBorder(lineborder,"Filters", TitledBorder.LEFT, TitledBorder.TOP);
    jp.setBorder(border);
    jp.setBackground(innerPanelColor);
    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    colorText = new JTextField();
    colorText.setText("selected color: null");
    colorCutButton=new JButton("color cut");
    colorCutButton.addActionListener( this );
    colorCutButton.setFocusable(false);
    colorCutButton.setBackground(innerPanelColor);
    eraseButton=new JButton("erase");
    eraseButton.addActionListener( this );
    eraseButton.setFocusable(false);
    eraseButton.setBackground(innerPanelColor);


    resetFilterButton=new JButton("reset");
    resetFilterButton.addActionListener( this );
    resetFilterButton.setFocusable(false);
    resetFilterButton.setBackground(innerPanelColor);

    layout.putConstraint( SpringLayout.NORTH,binalizeButton, 1,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,binalizeButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH,simpleMaskButton, 0,SpringLayout.SOUTH, binalizeButton);
    layout.putConstraint( SpringLayout.WEST,simpleMaskButton, 0,SpringLayout.EAST, binalizeButton);

    layout.putConstraint( SpringLayout.NORTH,resetFilterButton, 1,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.EAST, resetFilterButton, -5,SpringLayout.EAST, jp);

    layout.putConstraint( SpringLayout.NORTH,rbEraser, 6,SpringLayout.SOUTH, binalizeButton);
    layout.putConstraint( SpringLayout.WEST, rbEraser, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH,eraseButton, 2,SpringLayout.SOUTH, rbEraser);
    layout.putConstraint( SpringLayout.WEST, eraseButton, 5,SpringLayout.EAST, rbEraser);


    layout.putConstraint( SpringLayout.NORTH,rbSetColor, 6,SpringLayout.SOUTH, rbEraser);
    layout.putConstraint( SpringLayout.WEST, rbSetColor, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.SOUTH,colorText, 0,SpringLayout.SOUTH, rbSetColor);
    layout.putConstraint( SpringLayout.WEST, colorText, 5,SpringLayout.EAST, rbSetColor);
    layout.putConstraint( SpringLayout.SOUTH,colorCutButton, 3,SpringLayout.SOUTH, colorText);
    layout.putConstraint( SpringLayout.WEST, colorCutButton, 0,SpringLayout.EAST, colorText);

    jp.add(resetFilterButton);
    jp.add(binalizeButton);
    jp.add(rbEraser);
    jp.add(eraseButton);
    //jp.add(simpleMaskButton);
    jp.add(rbSetColor);
    jp.add(colorText);
    jp.add(colorCutButton);

    return jp;
  }

  private JPanel assistPanel(){
    JPanel jp=new JPanel();
    jp.setPreferredSize(new Dimension(200,80));
    LineBorder lineborder = new LineBorder(innerBorderColor, 2);
    TitledBorder border = new TitledBorder(lineborder,"Assist Point", TitledBorder.LEFT, TitledBorder.TOP);
    jp.setBorder(border);
    jp.setBackground(innerPanelColor);
    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    assistPointResetButton=new JButton("reset");
    assistPointResetButton.addActionListener( this );
    assistPointResetButton.setFocusable(false);
    assistPointResetButton.setBackground(innerPanelColor);


    layout.putConstraint( SpringLayout.NORTH,rbSetAP, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST, rbSetAP, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,rbDelAP, 2,SpringLayout.SOUTH, rbSetAP);
    layout.putConstraint( SpringLayout.WEST, rbDelAP, 0,SpringLayout.WEST, rbSetAP);

    labelAP=new JLabel();
    labelAP.setText(String.format("# of Assist Points= %d",assistPoints.size()/2));
    layout.putConstraint( SpringLayout.SOUTH, labelAP, 0,SpringLayout.NORTH, rbDelAP);
    layout.putConstraint( SpringLayout.WEST, labelAP, 5,SpringLayout.EAST, rbDelAP);


    layout.putConstraint( SpringLayout.SOUTH,assistPointResetButton, 3,SpringLayout.SOUTH, rbSetAP);
    layout.putConstraint( SpringLayout.EAST, assistPointResetButton, 0,SpringLayout.EAST, jp);

    jp.add(rbSetAP);
    jp.add(rbDelAP);
    jp.add(labelAP);
    jp.add(assistPointResetButton);

    return jp;
  }

  private JPanel settingPanel(){
    //setting panel
    JPanel jp=new JPanel();
    jp.setPreferredSize(new Dimension(250,460));
    LineBorder lineborder = new LineBorder(borderColor, 2);
    TitledBorder border = new TitledBorder(lineborder,"Operations", TitledBorder.LEFT, TitledBorder.TOP);
    jp.setBorder(border);
    jp.setBackground(panelColor);


    rbSetOrg=new JRadioButton("set Origin");
    rbSetAP=new JRadioButton("set AssistPoint",true);
    rbDelAP=new JRadioButton("delete AssistPoint");
    rbSetColor=new JRadioButton("set color");
    rbSetX1=new JRadioButton("x start");
    rbSetX2=new JRadioButton("x end");
    rbSetY1=new JRadioButton("y start");
    rbSetY2=new JRadioButton("y end");
    rbEraser=new JRadioButton("eraser");
    rbSetOrg.setBackground(innerPanelColor);
    rbSetAP.setBackground(innerPanelColor);
    rbDelAP.setBackground(innerPanelColor);
    rbSetColor.setBackground(innerPanelColor);
    rbSetX1.setBackground(innerPanelColor);
    rbSetX2.setBackground(innerPanelColor);
    rbSetY1.setBackground(innerPanelColor);
    rbSetY2.setBackground(innerPanelColor);
    rbEraser.setBackground(innerPanelColor);

    ButtonGroup group = new ButtonGroup();
    group.add(rbSetOrg);
    group.add(rbSetAP);
    group.add(rbDelAP);
    group.add(rbSetColor);
    group.add(rbSetX1);
    group.add(rbSetX2);
    group.add(rbSetY1);
    group.add(rbSetY2);
    group.add(rbEraser);

    openButton=new JButton("open");
    openButton.addActionListener( this );
    openButton.setFocusable(false);
    openButton.setActionCommand("open");
    openButton.setBackground(innerPanelColor);
    reloadButton=new JButton("reload");
    reloadButton.addActionListener( this );
    reloadButton.setFocusable(false);
    reloadButton.setBackground(innerPanelColor);

    binalizeButton=new JButton("binalize");
    binalizeButton.addActionListener( this );
    binalizeButton.setFocusable(false);
    binalizeButton.setBackground(innerPanelColor);
    simpleMaskButton=new JButton("thining");
    simpleMaskButton.addActionListener( this );
    simpleMaskButton.setFocusable(false);
    simpleMaskButton.setBackground(innerPanelColor);

    traceButton=new JButton("trace");
    traceButton.addActionListener( this );
    traceButton.setFocusable(false);
    traceButton.setBackground(innerPanelColor);
    writeButton=new JButton("export");
    writeButton.addActionListener( this );
    writeButton.setFocusable(false);
    writeButton.setBackground(innerPanelColor);

    JPanel axisPanel=axisPanel();
    JPanel filterPanel=filterPanel();
    JPanel assistPanel=assistPanel();

    //setting panel
    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    layout.putConstraint( SpringLayout.NORTH,openButton, 5,SpringLayout.NORTH, jp);
    layout.putConstraint( SpringLayout.WEST,openButton, 5,SpringLayout.WEST, jp);
    layout.putConstraint( SpringLayout.NORTH,reloadButton, 0,SpringLayout.NORTH, openButton);
    layout.putConstraint( SpringLayout.EAST,reloadButton, -5,SpringLayout.EAST, jp);

    layout.putConstraint( SpringLayout.NORTH,filterPanel, 5,SpringLayout.SOUTH, openButton);
    layout.putConstraint( SpringLayout.EAST,filterPanel, -5,SpringLayout.EAST, jp);
    layout.putConstraint( SpringLayout.WEST,filterPanel, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,axisPanel, 5,SpringLayout.SOUTH, filterPanel);
    layout.putConstraint( SpringLayout.EAST,axisPanel, -5,SpringLayout.EAST, jp);
    layout.putConstraint( SpringLayout.WEST,axisPanel, 5,SpringLayout.WEST, jp);


    layout.putConstraint( SpringLayout.NORTH,assistPanel, 5,SpringLayout.SOUTH, axisPanel);
    layout.putConstraint( SpringLayout.EAST,assistPanel, -5,SpringLayout.EAST, jp);
    layout.putConstraint( SpringLayout.WEST,assistPanel, 5,SpringLayout.WEST, jp);

    layout.putConstraint( SpringLayout.NORTH,traceButton, 5,SpringLayout.SOUTH, assistPanel);
    layout.putConstraint( SpringLayout.WEST,traceButton, 5,SpringLayout.WEST, binalizeButton);
    layout.putConstraint( SpringLayout.NORTH,writeButton, 0,SpringLayout.NORTH, traceButton);
    layout.putConstraint( SpringLayout.WEST,writeButton, 5,SpringLayout.EAST, traceButton);

    jp.add(openButton);
    jp.add(reloadButton);
    jp.add(axisPanel);
    jp.add(filterPanel);
    jp.add(assistPanel);
    jp.add(traceButton);
    jp.add(writeButton);

    return jp;
  }

  private void makeControlFrame(){
    ctrlJframe=new JFrame("Gtracer");
    //window size
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    ctrlJframe.setBounds( 0, 0,
                          460,screenDim.height-100);
    //how to action, when close
    ctrlJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ctrlJframe.setJMenuBar(getControlMenu());

    ///////////////

    //panel
    JPanel jp=new JPanel();
    jp.setBackground(panelColor);
    //set layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout(layout);

    JPanel spanel=settingPanel();
    jp.setBackground(panelColor);

    try{
      //loupe http://sawat.jf.land.to/loupe.html
      loupe = new Loupe();
      loupe.setPreferredSize(new Dimension(200,150));
      loupe.setBackground(panelColor);
      LineBorder lineborder = new LineBorder(borderColor, 2);
      TitledBorder border = new TitledBorder(lineborder,"Loupe", TitledBorder.LEFT, TitledBorder.TOP);
      loupe.setBorder(border);
      loupe.setBackground(panelColor);


      layout.putConstraint( SpringLayout.SOUTH,spanel, -10,SpringLayout.SOUTH, jp);
      layout.putConstraint( SpringLayout.EAST,spanel, -5,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST,spanel, 5,SpringLayout.WEST, jp);

      layout.putConstraint( SpringLayout.EAST,loupe, -5,SpringLayout.EAST, jp);
      layout.putConstraint( SpringLayout.WEST,loupe, 5,SpringLayout.WEST, jp);
      layout.putConstraint( SpringLayout.SOUTH,loupe, -10,SpringLayout.NORTH, spanel);
      layout.putConstraint( SpringLayout.NORTH,loupe, 10,SpringLayout.NORTH, jp);


      jp.add(loupe);
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
    colorText.setBorder(border);
    colorText.setForeground(color);

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
      g2.setStroke(new BasicStroke(1f)); //線の種類を設定
      //x-axis
      g.setColor(Color.red);
      if(xstart[0]!=EMPTY && xend[0]!=EMPTY){
        g.drawLine(xstart[0],xstart[1],xend[0],xend[1]);
      }
      //y-axis
      g.setColor(Color.red);
      if(ystart[0]!=EMPTY && yend[0]!=EMPTY){
        g.drawLine(ystart[0],ystart[1],yend[0],yend[1]);
      }
      //axis-end points
      g.setColor(Color.green);
      int r=1;
      if(xstart[0]!=EMPTY)g.fill3DRect(xstart[0],xstart[1],r,r,false);
      if(xend[0]!=EMPTY)g.fill3DRect(xend[0],xend[1],r,r,false);
      if(ystart[0]!=EMPTY)g.fill3DRect(ystart[0],ystart[1],r,r,false);
      if(yend[0]!=EMPTY)g.fill3DRect(yend[0],yend[1],r,r,false);

      //erase point
      g.setColor(Color.pink);
      for(int i=0;i<erasePoints.size()/2;i++){
        //center point
        r=1;
        int x=erasePoints.get(2*i  );
        int y=erasePoints.get(2*i+1);
        g.fillRect(x,y,r,r);
      }

      //assist point
      g.setColor(Color.blue);
      r=13;
      for(int i=0;i<assistPoints.size()/2;i++){
        //big circle
        int x=assistPoints.get(2*i  );
        int y=assistPoints.get(2*i+1);
        g.drawOval(x-r/2,y-r/2,r,r);
      }
      //traced point
      r=5;
      for(int i=0;i<tracedPoints.size()/2;i++){
        int x=tracedPoints.get(2*i  );
        int y=tracedPoints.get(2*i+1);
        g.setColor(Color.green);
        g.fillRect(x-r/2,y-r/2,r,r);
      }
      int r2=1;
      for(int i=0;i<tracedPoints.size()/2;i++){
        int x=tracedPoints.get(2*i  );
        int y=tracedPoints.get(2*i+1);
        g.setColor(Color.magenta);
        g.drawOval(x-r2,y-r2,r2,r2);
      }
      /*
       * //linear segment
       * for(int i=0;i<tracedPoints.size()/2-1;i++){
       *   int x1=tracedPoints.get(2*i  );
       *   int y1=tracedPoints.get(2*i+1);
       *   int x2=tracedPoints.get(2*i +2);
       *   int y2=tracedPoints.get(2*i+1 +2);
       *   g.drawLine(x1,y1,x2,y2);
       * }
       */

    }
  }//end of mycanvas


  //////////////////////////////////////////////////////////////////////
  // input verifier
  //////////////////////////////////////////////////////////////////////
  class MyInputVerifier extends InputVerifier{
    @Override
      public boolean verify(JComponent c) {
      boolean verified = false;
      JTextField textField = (JTextField)c;
      try{
        Double.parseDouble(textField.getText());
        verified = true;
      }catch(NumberFormatException e) {
        UIManager.getLookAndFeel().provideErrorFeedback(c);
        Toolkit.getDefaultToolkit().beep();
      }
      return verified;
    }
  }

  //////////////////////////////////////////////////////////////////////
  // LookAndFeel matters
  //////////////////////////////////////////////////////////////////////
  private void setLookAndFeel(){
    // Possible Look & Feels
    String mac     = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    String metal   = "javax.swing.plaf.metal.MetalLookAndFeel";
    String motif   = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    String gtk     = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    String nimbus  = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
    /*
     * //show LF index
     * UIManager.LookAndFeelInfo[] installedLafs = UIManager.getInstalledLookAndFeels();
     * for(int i=0; i<installedLafs.length; i++){
     *   UIManager.LookAndFeelInfo info=installedLafs[i];
     *   System.out.println(info.getName());
     * }
     */
    try{
      UIManager.setLookAndFeel( nimbus );
    }catch( Exception ex ){
      //System.out.println(" Nimbus not available!!");
    }
  }

}//end of this class
