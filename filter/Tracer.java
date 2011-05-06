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
  private int rgbRef=-123456789;
  static final int EMPTY=-1123;

  public int getRGBRef(){
    return rgbRef;
  }

  //return width, height of loaded image
  public Dimension getSize(){
    return new Dimension(width,height);
  }

  //constructor
  public Tracer(BufferedImage originalImg){
    this.originalImg=originalImg;
    setLengthMap(false);

    //create buffer
    tracedImg = new BufferedImage(originalImg.getWidth(),originalImg.getHeight(),
                                  BufferedImage.TYPE_INT_ARGB);

  }

  public void setLengthMap(boolean isColorCut){
    width=originalImg.getWidth();
    height=originalImg.getHeight();

    lengthMap=null;
    lengthMap=new short[width][height];

    //convert to gray scale
    for (int x = 0; x < width;x++){
      for (int y = 0; y < height;y++){
        int rgb = originalImg.getRGB(x, y);
        if(isColorCut && rgbRef!=-123456789){
          //color cut
          if(rgb==rgbRef)lengthMap[x][y]=Short.MAX_VALUE;

        }else{
          //しきい値より小さい時は

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
        }

      }
    }
  }

  public BufferedImage makeImage(int filterType){
    if(filterType==1){
      LengthMap.setChessBoard(lengthMap,width,height);
      //LengthMap.setCityBlock(lengthMap,width,height);
      //Skeltonization.localMax(lengthMap,width,height);
    }
    if(filterType==2){
      LengthMap.setChessBoard(lengthMap,width,height);
      //LengthMap.setCityBlock(lengthMap,width,height);
      Skeltonization.simpleMask(lengthMap,width,height);
    }

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
            rgbRef = originalImg.getRGB(xx, yy);
            break loop;
          }
        }
      }
      n++;//捜索範囲をどんどん外側に
    }
    return point;
  }

  public void erase(ArrayList<Integer> queue){
    for(int i=0;i<queue.size()/2;i++){
      int x=queue.get(2*i);
      int y=queue.get(2*i+1);
      lengthMap[x][y]=0;
    }
  }

  /**
   * Tracer
   */
  public ArrayList<Integer> trace(LinkedList<Integer> pQueue){
    //increment
    int INC_X=1;
    //線分の幅．100px以上の線幅の線は稀だろう
    int BAND_Y=100;
    //traced points
    ArrayList<Integer> tracedPoints= new ArrayList<Integer>();

    //start!
    for(int i=0;i<pQueue.size()/2-1;i++){
      //start point
      int startX=pQueue.get(2*i);
      int startY=pQueue.get(2*i+1);
      //end point
      int endX=pQueue.get(2*(i+1));
      int endY=pQueue.get(2*(i+1)+1);

      //trace point
      int traceX=startX;
      int traceY=startY;
      while(traceX<endX){
        //search ymax
        int ymax=startY;
        boolean existYMax=false;
        for(int dy=0;dy<BAND_Y;dy++){
          if(traceY+dy<height && lengthMap[traceX][traceY+dy]<=0){
            ymax=traceY+dy-1;
            existYMax=true;
            break;
          }
        }
        //search ymin
        int ymin=startY;
        boolean existYMin=false;
        for(int dy=0;dy<BAND_Y;dy++){
          if(traceY-dy>0 && lengthMap[traceX][traceY-dy]<=0){
            ymin=traceY-dy+1;
            existYMin=true;
            break;
          }
        }
        //add to tracedPoints
        if(existYMax && existYMin){
          int y=(ymax+ymin)/2;
          tracedPoints.add(traceX);
          tracedPoints.add(y);
        }


        //search next position
        //線分が重なっている可能性を考慮
        boolean hasNext=false;
        do{
          traceX+=INC_X;
          for(int y=ymin-2;y<ymax+2;y++){
            if(lengthMap[traceX][y]>0){
              traceY=y;
              hasNext=true;
            }
          }
        }while(!hasNext&& traceX<endX);


      }//while

    }//i

    return tracedPoints;
  }

  public int[] setAxis(int[] org){
    int[] a={org[0],org[1],
             org[0],org[1]};
    //x end
    for(int x=org[0];x<width;x++){
      if(lengthMap[x][org[1]]<=0){
        a[0]=x-1;
        a[1]=org[1];
        break;
      }
    }
    //y end
    for(int y=org[1];y>=0;y--){
      if(lengthMap[org[0]][y]<=0){
        a[2]=org[0];
        a[3]=y+1;
        break;
      }
    }
    return a;
  }
  public int[] assistAxis(int[] org){
    int[] a={org[0],org[1],
             org[0],org[1]};
    //x end
    a[0]=width-2;
    a[1]=org[1];
    //y end
    a[2]=org[0];
    a[3]=2;
    return a;
  }

}
