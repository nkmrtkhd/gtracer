import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

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
    setTitle("GTracer");

    try{
      File imgfile = new File(filename);
      originalImg = ImageIO.read(imgfile);
    }catch (Exception e) {
    }
    tracedImg=null;

    setBounds( 0, 0, 850, 500);
    add(makePanel());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);

    tracer=new Tracer();
  }


  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == doTraceButton){
      tracedImg=tracer.startTrace(originalImg);
    }
    myCanv.repaint();
  }


  private JButton doTraceButton;
  MyCanvas myCanv;
  private JPanel makePanel(){
    doTraceButton=new JButton("trace");
    doTraceButton.addActionListener( this );
    doTraceButton.setFocusable(false);

    myCanv=new MyCanvas();
    myCanv.setBackground(Color.white);

    JPanel jp=new JPanel();
    //layout
    SpringLayout layout = new SpringLayout();
    jp.setLayout( layout );
    layout.putConstraint( SpringLayout.SOUTH, doTraceButton, -5,SpringLayout.SOUTH, jp );
    layout.putConstraint( SpringLayout.WEST, doTraceButton, 5,SpringLayout.WEST, jp );

    layout.putConstraint( SpringLayout.SOUTH, myCanv, 0,SpringLayout.NORTH, doTraceButton );
    layout.putConstraint( SpringLayout.NORTH, myCanv, 0,SpringLayout.NORTH, jp );
    layout.putConstraint( SpringLayout.WEST, myCanv, 0,SpringLayout.WEST, jp );
    layout.putConstraint( SpringLayout.EAST, myCanv, 0,SpringLayout.EAST, jp );

    jp.add(myCanv);
    jp.add(doTraceButton);
    return jp;
  }

  ///
  class MyCanvas extends Canvas{
    public void paint(Graphics g){
      g.drawImage(originalImg,0,20,this);

      if(tracedImg!=null)g.drawImage(tracedImg,410,20,this);
    }

  }
}
