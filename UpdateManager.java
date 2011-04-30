import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.*;
import java.util.regex.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;
import java.text.*;

//launch browser
//http://www.centerkey.com/java/browser/
import com.centerkey.utils.BareBonesBrowserLaunch;


public class UpdateManager{
  private double thisVersion=1.001;
  private double wwwVersion=thisVersion;
  private String downloadURL="http://code.google.com/p/gtracer/downloads/list";

  //constructor
  public UpdateManager(){
    showDialog();
  }

  public void showDialog(){
    //show dialog
    String str;
    if(checkUpdate()){
      str="Available new version "+wwwVersion;
      showDialog(str);
    }else
      str="The current version "+thisVersion+" is up to date";

    System.out.println(str);
  }

  public boolean isStrongUpdate(){
    int newWWW=(int)wwwVersion;
    int newThis=(int)thisVersion;
    if(newWWW>newThis)
      return true;
    else
      return false;
  }

  public boolean checkUpdate(){
    try{
      //get url
      URL u = new URL(downloadURL);
      InputStream is = u.openStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      //parse and check
      MyParserCallback cb = new MyParserCallback();
      ParserDelegator pd = new ParserDelegator();
      pd.parse(br, cb, true);
    } catch (Exception e) {
      wwwVersion=thisVersion;
    }

    if(wwwVersion > thisVersion)
      return true;
    else
      return false;

  }

  private class MyParserCallback extends HTMLEditorKit.ParserCallback {
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet attr, int pos){
      if(tag.equals(HTML.Tag.A)){
        String ret = (String)attr.getAttribute(HTML.Attribute.HREF);

        //if(ret.matches(".*Akira.*.zip.*"))System.out.println(ret);

        Pattern pattern = Pattern.compile("(.*)GTracer-(.*).zip(.*)");
        Matcher matcher = pattern.matcher(ret);
        if(matcher.find()) wwwVersion=Double.valueOf(matcher.group(2));
      }
    }
  }

  /////////
  private ImageIcon icon=new ImageIcon(this.getClass().getResource("/icon/GT_icons/1_Desktop_Icons/icon_512.png"));

  private void showDialog(String query){
    JFrame frame = new JFrame();
    String option[] = { "WWW page","Later"};
    int ans = JOptionPane.showOptionDialog(frame,
                                           query,
                                           "update notify",
                                           JOptionPane.DEFAULT_OPTION,
                                           JOptionPane.QUESTION_MESSAGE,
                                           icon,
                                           option,
                                           option[1] );

    if(ans==0)BareBonesBrowserLaunch.openURL(downloadURL);

  }

}
