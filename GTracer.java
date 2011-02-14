import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

//hand made library
import filter.*;


public class GTracer extends JFrame implements ActionListener{

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
    tracedImg=null;

    //window size
    setBounds( 0, 0, 850, 500);
    //create panel and add to this(JFrame)
    add(makePanel());
    //how to action, when close
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);

    //tracer
    tracer=new Tracer();
  }


  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == traceButton){
      tracedImg=tracer.startTrace(originalImg);
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


  private JButton traceButton;
  private JButton saveButton;
  private MyCanvas myCanv;
  private JPanel makePanel(){

    //button
    traceButton=new JButton("trace");
    traceButton.addActionListener( this );
    traceButton.setFocusable(false);
    saveButton=new JButton("save");
    saveButton.addActionListener( this );
    saveButton.setFocusable(false);

    //canvas
    myCanv=new MyCanvas();
    myCanv.setBackground(Color.white);

    //panel
    JPanel jp=new JPanel();
    //set layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout( layout );
    layout.putConstraint( SpringLayout.SOUTH, traceButton, -5,SpringLayout.SOUTH, jp );
    layout.putConstraint( SpringLayout.WEST, traceButton, 5,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.SOUTH, saveButton, 0,SpringLayout.SOUTH, traceButton );
    layout.putConstraint( SpringLayout.WEST, saveButton, 5,SpringLayout.EAST, traceButton);

    layout.putConstraint( SpringLayout.SOUTH, myCanv, 0,SpringLayout.NORTH, traceButton);
    layout.putConstraint( SpringLayout.NORTH, myCanv, 0,SpringLayout.NORTH, jp );
    layout.putConstraint( SpringLayout.WEST, myCanv, 0,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.EAST, myCanv, 0,SpringLayout.EAST, jp );

    //add to jpanel
    jp.add(myCanv);
    jp.add(traceButton);
    jp.add(saveButton);
    return jp;
  }

  ///private class
  private class MyCanvas extends Canvas{
    public void paint(Graphics g){
      g.drawImage(originalImg,0,20,this);

      if(tracedImg!=null)g.drawImage(tracedImg,410,20,this);
    }

  }
}
