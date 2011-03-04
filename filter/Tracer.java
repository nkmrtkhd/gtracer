package filter;

import java.util.*;
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
    setLengthMap(0f,1f,0f,1f);

    //create buffer
    tracedImg = new BufferedImage(originalImg.getWidth(),originalImg.getHeight(),
                                  BufferedImage.TYPE_INT_ARGB);

  }

  public void setLengthMap(float sxstart,float sxend,float systart,float syend){

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

  public ArrayList<Integer> trace(ArrayList<Integer> pQueue){
    System.out.println("trace starts");
    //traced pos
    ArrayList<Integer> pos= new ArrayList<Integer>();

    for(int i=0;i<pQueue.size()/2-1;i++){
      //start pos
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);
      //end pos
      int nextX=pQueue.get(2*(i+1));
      int nextY=pQueue.get(2*(i+1)+1);
      pos.add(startX);
      pos.add(startY);

      int traceX=startX;
      int traceY=startY;

      int dx=1;//increment
      int ny=10;//search max
      while( traceX <nextX ){
        traceX+=dx;
        if(startY>nextY){//note window coordinate is upside down
          for(int dy=-ny;dy<=ny;dy++){
            if(traceY+dy<0)continue;
            if(traceY+dy>=height)continue;
            if(nextY<=traceY+dy && traceY+dy<=startY){
              if( lengthMap[traceX][traceY+dy] != 0 ) {
                pos.add(traceX);
                pos.add(traceY+dy);
                traceY=traceY+dy;
                break;
              }
            }
          }
        }else{
          for(int dy=ny;dy>=-ny;dy--){
            if(dy<0)continue;
            if(dy>=height)continue;
            if(startY<=traceY+dy && traceY+dy<=nextY){
              if( lengthMap[traceX][traceY+dy] != 0 ) {
                pos.add(traceX);
                pos.add(traceY+dy);
                traceY=traceY+dy;
                break;
              }
            }
          }
        }
      }//while
    }//i

    System.out.println("trace done");
    return pos;
  }

  public int[] getPoint(int x, int y){
    int n=0;
    int[] point={x,y};
    loop: while(true){
      //(x,y)を中心にn近傍を探す
      for(int xx=x-n;xx<=x+n;xx++){
        if(xx<0)continue;
        if(xx>width-1)continue;
        for(int yy=y-n;yy<=y+n;yy++){
          if(yy<0)continue;
          if(yy>height-1)continue;
          int d=lengthMap[xx][yy];
          if(d>0){
            point[0]=xx;
            point[1]=yy;
            break loop;
          }
        }
      }
      n++;//捜索範囲をどんどん外側に
    }
    return point;
  }

  public BufferedImage makeImage(int filterType ){
    if(filterType==1)LengthMap.setChessBoard(lengthMap,width,height);
    if(filterType==2)LengthMap.setCityBlock(lengthMap,width,height);
    if(filterType==3)Skeltonization.localMax(lengthMap,width,height);
    if(filterType==4)Skeltonization.simpelMask(lengthMap,width,height);
    if(filterType==5)Skeltonization.hildthMask(lengthMap,width,height);

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
