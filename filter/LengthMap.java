package filter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;


public class LengthMap{

  /** set length map according to chess board distance*/
  static void setChessBoard(short[][] lengthMap, int width, int height){
    //System.out.println("chess board length filter");
    //ignore 1pixcel on border
    int xstart=1;
    int xend=width-2;
    int ystart=1;
    int yend=height-2;

    int[] k=new int[4];

    //upward left to right
    for(int x=xstart;x<xend;x++){
      for( int y=ystart;y<yend;y++){
        int d=lengthMap[x][y];
        if(d>0){
          k[0]=lengthMap[x-1][y]+1;
          k[1]=lengthMap[x-1][y-1]+1;
          k[2]=lengthMap[x][y-1]+1;
          k[3]=lengthMap[x+1][y-1]+1;
          int nd=getMin(k);
          if(nd<d)lengthMap[x][y]=(short)nd;
        }
      }
    }

    //downward right to left
    for(int x=xend;x>=xstart;x--){
      for( int y=yend;y>=ystart;y--){
        int d=lengthMap[x][y];
        if(d>0){
          k[0]=lengthMap[x+1][y]+1;
          k[1]=lengthMap[x+1][y+1]+1;
          k[2]=lengthMap[x][y+1]+1;
          k[3]=lengthMap[x-1][y+1]+1;
          int nd=getMin(k);
          if(nd<d)lengthMap[x][y]=(short)nd;
        }
      }
    }

  }

  /** set length map according to city block distance*/
  static void setCityBlock(short[][] lengthMap, int width, int height){
    //System.out.println("city block length filter");
    //ignore 1pixcel on border
    int xstart=1;
    int xend=width-2;
    int ystart=1;
    int yend=height-2;

    int[] k=new int[2];

    //upward left to right
    for(int x=xstart;x<xend;x++){
      for( int y=ystart;y<yend;y++){
        int d=lengthMap[x][y];
        if(d>0){
          k[0]=lengthMap[x-1][y]+1;
          k[1]=lengthMap[x][y-1]+1;
          int nd=getMin(k);
          if(nd<d)lengthMap[x][y]=(short)nd;
        }
      }
    }

    //downward right to left
    for(int x=xend;x>=xstart;x--){
      for( int y=yend;y>=ystart;y--){
        int d=lengthMap[x][y];
        if(d>0){
          k[0]=lengthMap[x+1][y]+1;
          k[1]=lengthMap[x][y+1]+1;
          int nd=getMin(k);
          if(nd<d)lengthMap[x][y]=(short)nd;
        }
      }
    }

  }

  /** search min in k*/
  private static int getMin(int[] k){
    int a=k[0];
    for(int i=1;i<k.length;i++){
      if(k[i]<a)a=k[i];
    }
    return a;
  }


}
