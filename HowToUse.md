# ダウンロード #
[Download](http://code.google.com/p/gtracer/downloads/list)から最新版のzipファイルをダウンロードしてください．


zipファイルには以下が含まれています．
  * GTracer.jar
  * HowToUse.pdf
  * sample\_Rutile\_O\_K.png



# 使い方 #
GTracerは，コマンドラインから
```
java -jar GTracer.jar
```
として起動します．
なお，Macintosh, WindowsではGTracer.jarをダブルクリックするだけで起動できます．



グラフをトレースする流れは以下です．
  1. 画像を開く
  1. 軸の設定
  1. フィルタ処理
  1. Assist Pointsの設定
  1. トレース
  1. 数値ファイルを出力


_**以下では，**_sample\_Rutile\_O\_K.png_**を例に，GTracerの使い方を説明します．**_

## 1. 画像を開く ##
openボタンを押すとダイアログが開くので，トレースしたい画像を選択してください．

![![](http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/open.png)](http://wiki.gtracer.googlecode.com/hg/img/screenshot/open.png)
_Fig. sample\_Rutile\_O\_K.pngを開いたとき_


読み込み可能な画像フォーマットは，png, jpg, bmp, gifです．

また，
```
java -jar GTracer.jar sample_Rutile_O_K.png
```
のように，コマンドラインの引数として与えることも可能です．

## 2. 軸の設定 ##
次に，軸の設定とレンジを設定します．トレースした点の座標は，ここで設定された軸とレンジを用いて決定されます．

現在は、線形軸のみで、対数軸には対応していません。

軸の設定には**自動**と**手動**の二つの方法があります．

### 2.1 自動設定 ###
選択された原点から自動的に軸を認識します．

(i)   原点を選択 <br>
(ii)  軸の自動探索 <br>
(iii) 軸のレンジを設定 <br>

<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/axis.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/axis.png' /></a>
<i>Fig. 軸の自動設定</i>

現在のアルゴリズムは，原点から水平，垂直方向に，連続する線分を1pxの幅で探索します．<br>
よって，軸が途切れている場合や画像が回転している場合は失敗します．<br>
<br>
<h3>2.2 手動設定</h3>
軸の自動設定がうまく行かない場合は，以下のように手動で軸を選択して下さい．<br>
<br>
(i)   x軸の始点と終点を手動で選択 <br>
(ii)  y軸の始点と終点を手動で選択 <br>
(iii) 軸のレンジを設定 <br>

<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/axis-manual.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/axis-manual.png' /></a>
<i>Fig. 軸の手動設定</i>

<h2>3．フィルタ処理</h2>
画像をフィルタ処理することで，トレースしやすい画像へ変換します．<br>
現在，<br>
<ul><li>二値化フィルタ<br>
</li><li>色フィルタ<br>
が内蔵されています．</li></ul>

<h3>3.1 二値化フィルタ</h3>
二値化とは，通常RGBで表される色情報を，白と黒のみのdigitalな情報へ変換することを言います．このフィルタにより，アンチエイリアスやグラデーションがかかっている画像をデジタルに扱うことが出来ます．<br>
<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/bin1.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/bin1.png' /></a>
<i>Fig. 二値化フィルタ適用前</i>


<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/bin2.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/bin2.png' /></a>
<i>Fig. 二値化フィルタ適用後</i>

<h3>3.2 色フィルタ</h3>
特定の色のみを抽出しその他の色を消去するフィルタです．<br>
<br>
<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/setcolor.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/setcolor.png' /></a>
<i>Fig. 残したい色の選択＆色フィルタ適用前</i>


<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/colorcut.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/colorcut.png' /></a>
<i>Fig. 色フィルタ適用後</i>


<h2>4. Assist Points</h2>
トレースする曲線の始点と終点を指定します．<br>
<br>
<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/setAP.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/setAP.png' /></a>
<i>Fig. Assist Pointsの指定</i>


曲線が途切れている部分や急峻な部分の場合，トレースが失敗する可能性があります．その時は，不連続な部分にAssist Pointを追加してみてください．<br>
<br>
<br>
<h2>5. トレース</h2>
Traceボタンを押すと，Assist Pointsの情報をもとにトレースします．<br>
<br>
<br>
<a href='http://wiki.gtracer.googlecode.com/hg/img/screenshot/traced.png'><img src='http://wiki.gtracer.googlecode.com/hg/img/screenshot/small/traced.png' /></a>
<i>Fig. トレースした様子</i>

<h2>6. 数値ファイルを出力</h2>

Exportボタンを押すと，追跡した点を軸のレンジに合わせて変換し，テキストファイルとして出力します．<br>
このテキストファイルのフォーマットは<br>
<pre><code>x1, y1<br>
x2, y2<br>
x3, y3<br>
...<br>
</code></pre>
のようにトレースした点のx, y座標が羅列されているのみです．