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

  public ArrayList<Integer> trace1(LinkedList<Integer> pQueue){
    //上と下から挟み込む by Nakamura
    System.out.println("trace starts");
    //traced pos
    ArrayList<Integer> pos= new ArrayList<Integer>();

    for(int i=0;i<pQueue.size()/2-1;i++){
      //start pos
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);
      //end pos
      int endX=pQueue.get(2*(i+1));
      int endY=pQueue.get(2*(i+1)+1);

      int dx=1;//increment
      int ny=10;//search max

      int traceX=startX;
      int traceY=startY;
      while(traceX<endX){
       //上と下から挟み込み
        if(startY>endY){//note window coordinate is upside down
          for(int dy=-ny;dy<=ny;dy++){
            if(traceY+dy<0)continue;
            if(traceY+dy>=height)continue;
            if(endY<=traceY+dy && traceY+dy<=startY){
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
            if(startY<=traceY+dy && traceY+dy<=endY){
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

  public ArrayList<Integer> trace2(LinkedList<Integer> pQueue){
    //??? by Tamura
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

        // T.Tamura  for debug
        //        for ( int j=traceY-10; j<traceY+10; j++ ) {
        //          System.out.println(traceX + "  " + j + "  " + lengthMap[traceX][j]);
        //        }

        // T.Tamura  search of Max. and Min.
        int isum = 0;
        for ( int j=ymax; j<=ymin; j++ ) {
          if(lengthMap[traceX][j] != 0) {
            isum+=1;
          }
        }
        // T.Tamura  for debug
        //      System.out.println("isum  " + isum);
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
                break;
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
        System.out.println(traceX + "  " + ymax + "  " + ymin);
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

      }//while
    }//i

    System.out.println("trace done");
    return pos;
  }

  public ArrayList<Integer> trace3(LinkedList<Integer> pQueue){
    //拡げる by Nakamura
    System.out.println("trace starts");
    //traced pos
    ArrayList<Integer> pos= new ArrayList<Integer>();

    for(int i=0;i<pQueue.size()/2-1;i++){
      //start pos
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);
      //end pos
      int endX=pQueue.get(2*(i+1));
      int endY=pQueue.get(2*(i+1)+1);

      int dx=1;//increment
      int ny=10;//search max

      int traceX=startX;
      int traceY=startY;
      while(traceX<endX){
        int ymax=startY;
        int ymin=startY;
        for(int dy=0;dy<ny;dy++){
          //upward
          if(traceY+dy<height && lengthMap[traceX][traceY+dy]>0)ymax=traceY+dy;
          //downward
          if(traceY-dy>0 && lengthMap[traceX][traceY-dy]>0)ymin=traceY-dy;
        }

        int y=(ymax+ymin)/2;
        pos.add(traceX);
        pos.add(y);

        traceX+=dx;
        if(startY>endY)
          traceY=ymin;
        else
          traceY=ymax;

      }//while

    }//i

    System.out.println("trace done");
    return pos;
  }

  static final int EMPTY=-1123;
  public ArrayList<Integer> trace4(LinkedList<Integer> pQueue){
    //拡げる その2 by Nakamura
    System.out.println("trace starts");
    //traced pos
    ArrayList<Integer> pos= new ArrayList<Integer>();

    for(int i=0;i<pQueue.size()/2-1;i++){
      //start pos
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);
      //end pos
      int endX=pQueue.get(2*(i+1));
      int endY=pQueue.get(2*(i+1)+1);

      int dx=1;//increment
      int ny=10;//search max

      int traceX=startX;
      int traceY=startY;
      while(traceX<endX){
        //search ymax
        int ymax=startY;
        boolean existYMax=false;
        for(int dy=1;dy<ny;dy++){
          if(traceY+dy<height && lengthMap[traceX][traceY+dy]<=0){
            ymax=traceY+dy-1;
            existYMax=true;
            break;
          }
        }
        //search ymin
        int ymin=startY;
        boolean existYMin=false;
        for(int dy=1;dy<ny;dy++){
          if(traceY-dy>0 && lengthMap[traceX][traceY-dy]<=0){
            ymin=traceY-dy+1;
            existYMin=true;
            break;
          }
        }
        //addition
        if(existYMax && existYMin){
          int y=(ymax+ymin)/2;
          pos.add(traceX);
          pos.add(y);
        }

        //search next y
        do{
          traceX+=dx;
          if(startY>endY)
            traceY=nextYup(traceX,ymax);
          else
            traceY=nextYdown(traceX,ymin);
        }while(traceY==EMPTY && traceX<endX);

      }//while

    }//i

    System.out.println("trace done");
    return pos;
  }
  private int nextYup(int xin, int ymin){
    for(int dy=1;dy<10;dy++){
      if(ymin-dy>0 && lengthMap[xin][ymin-dy]>0) return ymin-dy;
    }
    return EMPTY;
  }

  private int nextYdown(int xin, int yin){
    for(int dy=1;dy<10;dy++){
      if(yin+dy<height && lengthMap[xin][yin+dy]>0)return yin+dy;
    }
    return EMPTY;
  }

}
