package filter;

public class Skeltonization{
  /** 周辺８ブロックのうち，極大値の場合だけ残す */
  static void localMax(short[][] lengthMap, int width, int height){
    //System.out.println("local max ");
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

  /**
   * skeltonization
   * reference: http://codezine.jp/article/detail/98
   */
  static void simpleMask(short[][] lengthMap, int width, int height){

    short[][] newMap=new short[width][height];
    short[][] oldMap=new short[width][height];

    //copy lengthMap to newMap
    for(int y=0;y<height;y++){
      for(int x=0;x<width;x++){
        if(lengthMap[x][y]>0)
          newMap[x][y]=1;
        else
          newMap[x][y]=0;
      }
    }


    //direction
    int UPPER_LEFT=2,LOWER_RIGHT=6,UPPER_RIGHT=0,LOWER_LEFT=4;

    //flag
    int change_flag=1;
    while(change_flag==1){
      change_flag=0;

      //copy
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)oldMap[x][y]=newMap[x][y];
      //左上から細線化
      for(int j=1;j<height-1;j++)
        for(int i=1;i<width-1;i++)
          if(oldMap[i][j]==1)change_flag=thinImage(i,j,UPPER_LEFT,newMap,oldMap);

      //copy
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)oldMap[x][y]=newMap[x][y];
      //右下から細線化
      for(int j=height-2;j>=1;j--)
        for(int i=width-2;i>=1;i--)
          if(newMap[i][j]==1)change_flag=thinImage(i,j,LOWER_RIGHT,newMap,oldMap);

      //copy
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)oldMap[x][y]=newMap[x][y];
      //右上から細線化
      for(int j=1;j<height-1;j++)
        for(int i=width-2;i>=1;i--)
          if(newMap[i][j]==1)change_flag=thinImage(i,j,UPPER_RIGHT,newMap,oldMap);

      //copy
      for(int y=0;y<height;y++)for(int x=0;x<width;x++)oldMap[x][y]=newMap[x][y];
      //左下から細線化
      for(int j=height-2;j>=1;j--)
        for(int i=1;i<width-1;i++)
          if(newMap[i][j]==1)change_flag=thinImage(i,j,LOWER_LEFT,newMap,oldMap);

    }

    //copy to lengthMap
    for(int y=0;y<height;y++)for(int x=0;x<width;x++)lengthMap[x][y]=newMap[x][y];
  }

  /**
   * for simpleMask
   */
  private static int thinImage(int i,int j,int start,short[][] newMap,short[][] oldMap){
    short[] p=new short[8];
    int product,sum;
    p[0]=oldMap[i-1][j-1];
    p[1]=oldMap[i-1][j];
    p[2]=oldMap[i-1][j+1];
    p[3]=oldMap[i][j+1];
    p[4]=oldMap[i+1][j+1];
    p[5]=oldMap[i+1][j];
    p[6]=oldMap[i+1][j-1];
    p[7]=oldMap[i][j-1];

    int flag=0;
    for(int k=start;k<start+3;k++){
      product=p[k % 8]*p[(k+1) % 8]*p[(k+2) % 8];
      sum=p[(k+4) % 8]+p[(k+5) % 8]+p[(k+6) % 8];
      if(product==1 && sum==0){
        newMap[i][j]=0;   //消去するs
        flag=1;
        break;//exit k-loop
      }
    }
    return flag;
  }

}
