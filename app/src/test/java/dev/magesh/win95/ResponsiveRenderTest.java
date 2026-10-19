package dev.magesh.win95;

import android.graphics.*;
import android.view.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=36)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ResponsiveRenderTest {
 @Test @Config(qualifiers="w411dp-h891dp-port-xxxhdpi") public void s24Portrait() throws Exception { verify(411,891,"portrait"); }
 @Test @Config(qualifiers="w891dp-h411dp-land-xxxhdpi") public void landscape() throws Exception { verify(891,411,"landscape"); }
 @Test @Config(qualifiers="w360dp-h640dp-port-xxhdpi") public void compact() throws Exception { verify(360,640,"compact"); }
 @Test @Config(qualifiers="w411dp-h891dp-port-xxxhdpi") public void largeFont() throws Exception { RuntimeEnvironment.setFontScale(1.5f); try { verify(411,891,"large-font"); } finally { RuntimeEnvironment.setFontScale(1f); } }
 @Test @Config(sdk=31,qualifiers="w280dp-h653dp-port-mdpi") public void coverScreenAndroid12() throws Exception { verify(280,653,"cover-android12"); }
 @Test @Config(sdk=32,qualifiers="w600dp-h960dp-port-mdpi") public void tabletAndroid12L() throws Exception { verify(600,960,"tablet-android12l"); }
 @Test @Config(qualifiers="w1280dp-h800dp-land-mdpi") public void largeTablet() throws Exception { verify(1280,800,"large-tablet"); }
 @Test @Config(qualifiers="w320dp-h320dp-land-mdpi") public void shortWindow() throws Exception { verify(320,320,"short-window"); }
 private void layout(View decor,int w,int h){int width=RetroStyle.dp(decor.getContext(),w),height=RetroStyle.dp(decor.getContext(),h);for(int i=0;i<3;i++){decor.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));decor.layout(0,0,width,height);}}
 private void verify(int w,int h,String name) throws Exception {
  ActivityController<DesktopActivity> control=Robolectric.buildActivity(DesktopActivity.class).setup();
  try {
   DesktopActivity a=control.get();View decor=a.getWindow().getDecorView();layout(decor,w,h);
   capture(decor,name+"-desktop");decor.findViewWithTag("start").performClick();layout(decor,w,h);capture(decor,name+"-start");
   View menu=decor.findViewWithTag("start-menu"),space=decor.findViewWithTag("workspace");assertTrue(menu.getWidth()>0);assertTrue(menu.getTop()>=0);assertTrue(menu.getBottom()<=space.getHeight());
   for(String tool:new String[]{"apps","notes","calc","settings","clock","run","about","help"}){
    a.open(tool);layout(decor,w,h);View window=decor.findViewWithTag("window");assertTrue(tool,window.getWidth()>0&&window.getHeight()>0);assertTrue(tool,window.getRight()<=space.getWidth()&&window.getBottom()<=space.getHeight());assertTrue(tool,window.getTop()>=0&&window.getLeft()>=0);
    if(tool.equals("calc")||tool.equals("clock"))capture(decor,name+"-"+tool);
   }
  }finally{control.pause().stop().destroy();}
 }
 private void capture(View view,String name) throws IOException {
  File folder=new File("build/test-screenshots");folder.mkdirs();Bitmap bitmap=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);view.draw(new Canvas(bitmap));
  try(FileOutputStream output=new FileOutputStream(new File(folder,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,output);}bitmap.recycle();
 }
}
