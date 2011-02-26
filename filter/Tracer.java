package filter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

import filter.*;


public class Tracer{

  int height;
  int width;
  short[][] lengthMap;
  BufferedImage originalImg;
  BufferedImage tracedImg;

  //constructor
  public Tracer(BufferedImage originalImg){
    this.originalImg=originalImg;
    load(0f,1f,0f,1f);

    tracedImg=null;
    tracedImg = new BufferedImage(originalImg.getWidth(),originalImg.getHeight(),
                                  originalImg.getType());
    Graphics2D g2 = tracedImg.createGraphics();
    g2.drawImage(originalImg, 0, 0, null);

  }

  public void load(float sxstart,float sxend,float systart,float syend){

    width=originalImg.getWidth();
    height=originalImg.getHeight();

    lengthMap=null;
    lengthMap=new short[width][height];

    int xs=(int)(sxstart*width);
    int xe=(int)(sxend*width);
    int ys=(int)(systart*height);
    int ye=(int)(syend*height);
    //convert to gray scale
    for (int x = 0; x < width;x++){
      for (int y = 0; y < height;y++){
        int rgb = originalImg.getRGB(x, y);
        //r,g,b,a, has 0~255
        int a = (rgb >> 24) & 0xff;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        //unknown method
        //int m = (2*r + 4*g + b) / 7;

        //NTSC arithmetic average
        //Y = 0.298912*R + 0.586611*G + 0.114478*B
        //予め1024倍して整数化して，最後に1024で割る(右へ10bitシフト)
        int m=306*r+600*g+117*b;
        m=m>>10;

        //middle value method
        /*
         * int max=r;
         * if(max<g)max=g;
         * if(max<b)max=b;
         * int min=r;
         * if(min>g)min=g;
         * if(min>b)min=b;
         * int m = (max+min)/2;
         */

        if(m<250){
          if(x!=0 || y!=0 || x!=width-1 || y!=height-1)lengthMap[x][y]=Short.MAX_VALUE;
        }
        if(x<xs || xe<x || y<ys ||ye<y)lengthMap[x][y]=0;
      }
    }
  }

  public BufferedImage doFilter(int filterType ){
    if(filterType==1)LengthMap.setChessBoard(lengthMap,width,height);
    if(filterType==2)LengthMap.setCityBlock(lengthMap,width,height);
    if(filterType==3)LengthMap.delete(lengthMap,width,height);

    for (int x = 0; x < width;x++){
      for (int y = 0; y < height;y++){
        int m=255;//white
        if(lengthMap[x][y]>0)m=0;//black
        if(x==0 || x==width-1 || y==0 ||y==height-1)m=0;//border is black
        int a=255;
        tracedImg.setRGB(x, y, new Color(m, m, m, a).getRGB());
      }
    }

    return tracedImg;
  }

}
