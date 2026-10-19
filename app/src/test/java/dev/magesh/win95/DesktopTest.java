package dev.magesh.win95;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class DesktopTest {
 ActivityController<DesktopActivity> controller;DesktopActivity activity;
 @Before public void setup(){controller=Robolectric.buildActivity(DesktopActivity.class).setup();activity=controller.get();}
 @After public void stop(){controller.pause().stop().destroy();}
 View tagged(String tag){return activity.getWindow().getDecorView().findViewWithTag(tag);}
 @Test public void startMenuToggles(){tagged("start").performClick();assertNotNull(tagged("start-menu"));tagged("start").performClick();assertNull(tagged("start-menu"));}
 @Test public void notesPersistAcrossClose(){activity.open("notes");((EditText)tagged("note")).setText("Magesh's persistent note");tagged("close").performClick();activity.open("notes");assertEquals("Magesh's persistent note",((EditText)tagged("note")).getText().toString());}
 @Test public void minimizeAndTaskRestore(){activity.open("calc");tagged("minimize").performClick();assertEquals(View.GONE,tagged("window").getVisibility());tagged("task").performClick();assertEquals(View.VISIBLE,tagged("window").getVisibility());}
 @Test public void searchSurvivesRecreation(){activity.open("apps");((EditText)tagged("app-search")).setText("camera");controller.recreate();activity=controller.get();assertEquals("camera",((EditText)tagged("app-search")).getText().toString());}
 @Test public void allToolsCanOpenAndClose(){for(String tool:new String[]{"apps","notes","calc","settings","clock","run","bin","about","help"}){activity.open(tool);assertNotNull(tool,tagged("window"));tagged("close").performClick();assertNull(tool,tagged("window"));}}
 @Test public void minimizedWindowSurvivesRecreation(){activity.open("notes");tagged("minimize").performClick();controller.recreate();activity=controller.get();assertEquals(View.GONE,tagged("window").getVisibility());tagged("task").performClick();assertEquals(View.VISIBLE,tagged("window").getVisibility());}
 @Test public void calculatorErrorsDontCrash(){activity.open("calc");((EditText)tagged("calc-display")).setText("1/0");findButton(tagged("window"),"=").performClick();assertTrue(((TextView)tagged("calc-error")).getText().length()>0);}
 private Button findButton(View v,String text){if(v instanceof Button&&((Button)v).getText().toString().equals(text))return (Button)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){Button result=findButton(((ViewGroup)v).getChildAt(i),text);if(result!=null)return result;}return null;}
}
