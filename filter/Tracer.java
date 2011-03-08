package filter;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

import filter.*;


public class Tracer{

  private int height;
  private int width;
  private short[][] lengthMap;
  private BufferedImage originalImg;
  private BufferedImage tracedImg;

  //constructor
  public Tracer(BufferedImage originalImg){
    this.originalImg=originalImg;
    setLengthMap();

    //create buffer
    tracedImg = new BufferedImage(originalImg.getWidth(),originalImg.getHeight(),
                                  BufferedImage.TYPE_INT_ARGB);

  }

  public void setLengthMap(){

    width=originalImg.getWidth();
    height=originalImg.getHeight();

    lengthMap=null;
    lengthMap=new short[width][height];

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

        //しきい値より小さい時は
        if(m<250){
          if(x!=0 || y!=0 || x!=width-1 || y!=height-1)lengthMap[x][y]=Short.MAX_VALUE;
        }
      }
    }
  }
  public BufferedImage makeImage(int filterType ){
    if(filterType==1)LengthMap.setChessBoard(lengthMap,width,height);
    if(filterType==2)LengthMap.setCityBlock(lengthMap,width,height);
    if(filterType==3)Skeltonization.localMax(lengthMap,width,height);
    if(filterType==4)Skeltonization.simpleMask(lengthMap,width,height);

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


  public ArrayList<Integer> trace(LinkedList<Integer> pQueue){
    System.out.println("trace starts");
    //traced pos
    ArrayList<Integer> pos= new ArrayList<Integer>();

    for(int i=0;i<pQueue.size()/2-1;i++){
      //start pos
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);

// T.Tamura Note that Max and Min are opposite
      System.out.println("startX  " + startX);
      System.out.println("startY  " + startY);
      int ymax = startY;
      int ymin = startY;
      for ( int j=startY; j>startY-200; j-- ) {
        if( lengthMap[startX][j] == 0 ) {
          ymax = j+1;
          break;
        }
      }
      for ( int j=startY; j<startY+200; j++ ) {
        if( lengthMap[startX][j] == 0 ) {
          ymin = j-1;
          break;
        }
      }
      System.out.println("ymax  " + ymax);
      System.out.println("ymin  " + ymin);
      for ( int j=startY-10; j<startY+10; j++ ) {
        System.out.println(j + "  " + lengthMap[startX][j]);
      }

      //end pos
      int nextX=pQueue.get(2*(i+1));
      int nextY=pQueue.get(2*(i+1)+1);

//    pos.add(startX);
//    pos.add(startY);
// T.Tamura  pos should be real, not integer
      int imod= (ymax+ymin)%2 ;
      if (imod == 0) {
        pos.add(startX);
        pos.add((ymax+ymin)/2);
      } else {
        pos.add(startX);
        pos.add((ymax+ymin+1)/2);
      }

      int traceX=startX;
      int traceY=startY;

      int dx=1;//increment
      int ny=10;//search max
      while( traceX <nextX ){

        traceX+=dx;

        for ( int j=traceY-10; j<traceY+10; j++ ) {
          System.out.println(traceX + "  " + j + "  " + lengthMap[traceX][j]);
        }

// T.Tamura  search of Max. and Min.
        int isum = 0;
        for ( int j=ymax; j<=ymin; j++ ) {
          if(lengthMap[traceX][j] != 0) {
            isum+=1;
          }
        }
        System.out.println("isum  " + isum);
        if(isum != 0) {
          int ymaxtmp = ymax;
          int ymintmp = ymin;
          if ( lengthMap[traceX][ymax] == 0 ) {
            for ( int j=ymaxtmp; j<ymintmp; j++ ) {
              if( lengthMap[traceX][j] != 0 ) {
                ymax = j;
                break;
              }
            }
          } else {
            for ( int j=ymaxtmp; j>ymaxtmp-100; j-- ) {
              if( lengthMap[traceX][j] == 0 ) {
                ymax = j+1;
                break;
              }
            }      
          }
          if ( lengthMap[traceX][ymin] == 0 ) {
            for ( int j=ymintmp; j>ymaxtmp; j-- ) {
              if( lengthMap[traceX][j] != 0 ) {
                ymin = j;
              }
            }
          } else {
            for ( int j=ymintmp; j<ymintmp+100; j++ ) {
              if( lengthMap[traceX][j] == 0 ) {
                ymin = j-1;
                break;
              }
            }      
          }
        } else {
          int ymaxtmp = ymax-ny;
          int ymintmp = ymin+ny;
            for ( int j=ymaxtmp; j<ymintmp; j++ ) {
              if( lengthMap[traceX][j] != 0 ) {
                ymax = j;
                break;
              }
            }        
            for ( int j=ymintmp; j>ymaxtmp; j-- ) {
              if( lengthMap[traceX][j] != 0 ) {
                ymin = j;
                break;
              }
            }
        } // if(isum)
        System.out.println(ymax + "  " + ymin);
// pos should be real, not integer
        imod= (ymax+ymin)%2 ;
        if (imod == 0) {
          pos.add(traceX);
          pos.add((ymax+ymin)/2);
          traceY = (ymax+ymin)/2;
        } else {
          pos.add(traceX);
          pos.add((ymax+ymin+1)/2);
          traceY = (ymax+ymin+1)/2;
        }


//        if(startY>nextY){//note window coordinate is upside down
//          for(int dy=-ny;dy<=ny;dy++){
//            if(traceY+dy<0)continue;
//            if(traceY+dy>=height)continue;
//            if(nextY<=traceY+dy && traceY+dy<=startY){
//              if( lengthMap[traceX][traceY+dy] != 0 ) {
//                pos.add(traceX);
//                pos.add(traceY+dy);
//                traceY=traceY+dy;
//                break;
//              }
//            }
//          }
//        }else{
//          for(int dy=ny;dy>=-ny;dy--){
//            if(dy<0)continue;
//            if(dy>=height)continue;
//            if(startY<=traceY+dy && traceY+dy<=nextY){
//              if( lengthMap[traceX][traceY+dy] != 0 ) {
//                pos.add(traceX);
//                pos.add(traceY+dy);
//                traceY=traceY+dy;
//                break;
//              }
//            }
//          }
//        }
      
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

}
