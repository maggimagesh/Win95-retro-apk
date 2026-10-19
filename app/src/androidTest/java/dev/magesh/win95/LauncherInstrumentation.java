package dev.magesh.win95;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/** SDK-only device regression suite; also runnable without external test dependencies. */
public class LauncherInstrumentation extends Instrumentation {
 private DesktopActivity activity; private int passed=0; private final List<String> failures=new ArrayList<>();
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 private void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 private View tag(String name){return activity.getWindow().getDecorView().findViewWithTag(name);}
 private void test(String name,Runnable action){try{runOnMainSync(action);passed++;}catch(Throwable e){failures.add(name+": "+e);} }
 @Override public void onStart(){
  try {
   Intent intent=new Intent(getTargetContext(),DesktopActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
   activity=(DesktopActivity)startActivitySync(intent);waitForIdleSync();
   test("HOME declaration",()->check(!activity.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(activity.getPackageName()),0).isEmpty(),"HOME intent missing"));
   test("Start menu",()->{tag("start").performClick();check(tag("start-menu")!=null,"menu missing");tag("start").performClick();check(tag("start-menu")==null,"menu not closed");});
   for(String tool:new String[]{"apps","notes","calc","settings","clock","run","bin","about","help"})test("Tool "+tool,()->{activity.open(tool);check(tag("window")!=null,"window missing");tag("close").performClick();check(tag("window")==null,"window not closed");});
   test("Note persistence",()->{activity.open("notes");((EditText)tag("note")).setText("Regression note 95");tag("close").performClick();activity.open("notes");check(((EditText)tag("note")).getText().toString().equals("Regression note 95"),"note lost");});
   test("Minimize and restore",()->{tag("minimize").performClick();check(tag("window").getVisibility()==View.GONE,"not minimized");tag("task").performClick();check(tag("window").getVisibility()==View.VISIBLE,"not restored");});
   test("Calculator",()->{activity.open("calc");((EditText)tag("calc-display")).setText("(2+3)*4");findButton(tag("window"),"=").performClick();check(((EditText)tag("calc-display")).getText().toString().equals("20"),"wrong result");((EditText)tag("calc-display")).setText("1/0");findButton(tag("window"),"=").performClick();check(((TextView)tag("calc-error")).getText().length()>0,"error missing");});
   test("Search empty state",()->{activity.open("apps");((EditText)tag("app-search")).setText("__no_such_app_95__");check(((LinearLayout)tag("app-list")).getChildCount()==1,"unexpected search rows");((EditText)tag("app-search")).setText("");});
   runOnMainSync(()->activity.open("settings"));waitForIdleSync();
   test("Window geometry",()->{View win=tag("window"),space=tag("workspace");check(win.getWidth()>0&&win.getHeight()>0,"zero window");check(win.getLeft()>=0&&win.getTop()>=0,"negative origin");check(win.getRight()<=space.getWidth()&&win.getBottom()<=space.getHeight(),"window clipped");});
   runOnMainSync(()->{tag("close").performClick();tag("start").performClick();});waitForIdleSync();
   test("Menu geometry",()->{View menu=tag("start-menu"),space=tag("workspace");check(menu.getWidth()>0&&menu.getHeight()>0,"zero menu");check(menu.getTop()>=0&&menu.getBottom()<=space.getHeight(),"menu clipped");});
   capture("start-menu.png");runOnMainSync(()->tag("start").performClick());waitForIdleSync();capture("desktop.png");
   runOnMainSync(()->activity.open("calc"));waitForIdleSync();capture("calculator.png");
  }catch(Throwable e){failures.add("Suite: "+e);}
  finally {
   Bundle result=new Bundle();String output="Retro95 device tests: "+passed+" passed, "+failures.size()+" failed\n"+String.join("\n",failures)+"\n";
   result.putString("stream",output);result.putInt("passed",passed);result.putInt("failed",failures.size());if(activity!=null)runOnMainSync(()->activity.finish());finish(failures.isEmpty()?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
  }
 }
 private Button findButton(View v,String text){if(v instanceof Button&&((Button)v).getText().toString().equals(text))return (Button)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){Button b=findButton(((ViewGroup)v).getChildAt(i),text);if(b!=null)return b;}return null;}
 private void capture(String name){try{File folder=new File(getTargetContext().getExternalFilesDir(null),"test-screenshots");folder.mkdirs();Bitmap image=getUiAutomation().takeScreenshot();if(image!=null){try(FileOutputStream out=new FileOutputStream(new File(folder,name))){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();}}catch(IOException ignored){}}
}
