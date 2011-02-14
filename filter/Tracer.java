package filter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;


public class Tracer{
  //constructor
  public Tracer(){
  }

  public BufferedImage startTrace(BufferedImage orgBimg){
    //copy original to copyBimg
    BufferedImage copyBimg = new BufferedImage(orgBimg.getWidth(),
                                               orgBimg.getHeight(),
                                               orgBimg.getType());
    Graphics2D g2 = copyBimg.createGraphics();
    g2.drawImage(orgBimg, 0, 0, null);

    //convert to gray scale
    for (int y = 0; y < orgBimg.getHeight();y++){
      for (int x = 0; x < orgBimg.getWidth();x++){
        int rgb = orgBimg.getRGB(x, y);
        int a = (rgb>> 24) & 0xff;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int m = (2*r + 4*g + b) / 7;
        copyBimg.setRGB(x, y, new Color(m, m, m, a).getRGB());
      }
    }

    return copyBimg;
  }

}
