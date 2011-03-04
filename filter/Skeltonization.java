package filter;

public class Skeltonization{
  /** 周辺８ブロックのうち，極大値の場合だけ残す */
  static void localMax(short[][] lengthMap, int width, int height){
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

  /** マスクを使った細線化 */
  static void simpelMask(short[][] lengthMap, int width, int height){
    short[][] mask={//[8][9]
      {0, 0,-1,
       0, 1, 1,
       -1, 1,-1},
      {0, 0, 0,
       -1, 1,-1,
       1, 1,-1},
      {-1, 0, 0,
       1, 1, 0,
       -1, 1,-1},
      {1,-1, 0,
       1, 1, 0,
       -1,-1, 0},
      {-1, 1,-1,
       1, 1, 0,
       -1, 0, 0},
      {-1, 1, 1,
       -1, 1,-1,
       0, 0, 0},
      {-1, 1,-1,
       0, 1, 1,
       0, 0,-1},
      {0,-1,-1,
       0, 1, 1,
       0,-1, 1}
    };

    int xstart=1;
    int ystart=1;
    int xend=width-2;
    int yend=height-2;

    short[][] lengthMap1=new short[width][height];
    short[][] lengthMap2=new short[width][height];

    //copy img to img1
    for(int y=ystart;y<=yend;y++) {
      for(int x=xstart;x<=xend;x++) {
        if(lengthMap[x][y]>0){
          lengthMap1[x-xstart][y-ystart]=1;
        }
      }
    }

    //
    int del=1;
    while(del>0) {
      del=0;
      for(int n=0;n<8;n++) {
        for(int y=ystart;y<yend;y++) {
          for(int x=xstart;x<xstart;x++) {
            short val=lengthMap1[x][y];
            if(val>0 && delPoint(lengthMap1,x,y,mask[n])) {
              val=0;
              del++;
            }
            lengthMap2[x][y]=val;
          }//x
        }//y

        //copy
        for(int y=0;y<height;y++) {
          for(int x=0;x<width;x++) {
            lengthMap1[x][y]=lengthMap2[x][y];
            lengthMap2[x][y]=0;
          }
        }

      }//n
    }//while

    //set output image
    for(int y=0;y<height;y++) {
      for(int x=0;x<width;x++) {
        if(lengthMap1[x][y]>0)lengthMap[x][y]=1;
      }
    }
  }//end of mask
  /** for mask */
  static boolean delPoint(short[][] lengthMap,int x,int y,short[] mask){
    int sum=0;
    for(int yy=0;yy<=2;yy++) {
      for(int xx=0;xx<=2;xx++) {
        short val=lengthMap[x+xx-1][y+yy-1];
        short p=mask[xx+yy*3];
        if(p>=0 && val!=p) sum++;
      }
    }
    if(sum>0) return false;
    return true;
  }


  /** hildthマスク */
  static void hildthMask(short[][] lengthMap, int width, int height){
    int xstart=1;
    int ystart=1;
    int xend=width-2;
    int yend=height-2;

    short[][] lengthMap1=new short[width][height];

    for(int y=0;y<height;y++) {
      for(int x=0;x<width;x++) {
        if(lengthMap[x][y]>0){
          lengthMap1[x][y]=1;
        }
      }
    }

    int del=1;
    while(del>0) {
      del=0;
      for(int y=ystart;y<=yend;y++) {
        for(int x=xstart;x<=xend;x++) {
          short val=lengthMap1[x][y];
          if(val==1){
            if(delPoint(lengthMap1,x,y)){
              val=-1;
              del++;
            }
          }
          lengthMap1[x][y]=val;
        }
      }

      for(int y=0;y<height;y++) {
        for(int x=0;x<width;x++) {
          if(lengthMap1[x][y]==-1)lengthMap1[x][y]=0;
        }
      }

    }//while

    for(int y=ystart;y<=yend;y++) {
      for(int x=xstart;x<=xend;x++) {
        if(lengthMap1[x][y]>0)lengthMap[x][y]=1;
      }
    }
  }//end of hildth
  /** for hildth */
  static boolean delPoint(short[][] lengthMap ,int x,int y){
    short[] n=new short[9];
    short[] sn=new short[9];

    n[1]=lengthMap[x+1][y  ];
    n[2]=lengthMap[x+1][y-1];
    n[3]=lengthMap[x  ][y-1];
    n[4]=lengthMap[x-1][y-1];
    n[5]=lengthMap[x-1][y  ];
    n[6]=lengthMap[x-1][y+1];
    n[7]=lengthMap[x  ][y+1];
    n[8]=lengthMap[x+1][y+1];

    for(int i=1;i<9;i++) {
      if(n[i]<0)
        sn[i] = (short)-n[i];
      else
        sn[i]=n[i];
    }
    /* 境界であるか */
    if(n[1]+n[3]+n[5]+n[7]==4) return false;

    int sum=0;
    int psum=0;
    for(int i=1;i<9;i++) {
      psum+=sn[i];
      if(n[i]>0) sum+=n[i];
    }
    /* 端点か */
    if(psum<2) return false;

    /* 孤立点か */
    if(sum<1) return false;

    /* 連結性を保持できるか */
    sum=getConnect(sn);
    if(sum!=1) return false;

    /* 連結性を保持できるか2 */
    for(int i=1;i<9;i++) {
      int tmp;
      if(n[i]<0) sn[i]=0;
    }
    sum=getConnect(sn);
    if(sum!=1) return false;
    return true;
  }
  /** for hildth */
  static int getConnect(short[] sn){
    int sum=0;
    for(int i=1;i<9;i+=2) {
      int j=i+1;
      int k=i+2;
      if(j>8) j-=8;
      if(k>8) k-=8;
      sum+=(sn[i]-sn[i]*sn[j]*sn[k]);
    }
    return sum;
  }

}
