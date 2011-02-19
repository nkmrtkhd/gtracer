package filter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;


public class Boner{

  static void delete(short[][] lengthMap, int width, int height){
    System.out.println("boner");
    //ignore 1pixcel on border
    int xstart=1;
    int xend=width-2;
    int ystart=1;
    int yend=height-2;

    for(int x=xstart;x<xend;x++){
      for( int y=ystart;y<yend;y++){
        int d=lengthMap[x][y];
        for(int xx=x-1;xx<=x+1;xx++){
          for(int yy=y-1;yy<=y+1;yy++){
            int d1=lengthMap[xx][yy];
            if(d<d1)lengthMap[x][y]=0;
          }
        }
      }
    }

  }

}
