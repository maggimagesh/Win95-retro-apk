package dev.magesh.win95;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.view.*;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class LauncherDeviceTest {
 Instrumentation instrument;DesktopActivity activity;
 @Before public void launch(){instrument=InstrumentationRegistry.getInstrumentation();Context c=instrument.getTargetContext();Intent i=new Intent(c,DesktopActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);activity=(DesktopActivity)instrument.startActivitySync(i);instrument.waitForIdleSync();}
 @After public void finish(){instrument.runOnMainSync(()->activity.finish());}
 @Test public void homeIntentIsDeclared(){Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(activity.getPackageName());assertFalse(activity.getPackageManager().queryIntentActivities(i,0).isEmpty());}
 @Test public void startAndEveryToolWork(){instrument.runOnMainSync(()->{View root=activity.getWindow().getDecorView();root.findViewWithTag("start").performClick();assertNotNull(root.findViewWithTag("start-menu"));for(String tool:new String[]{"apps","notes","calc","settings","clock","run","bin","about","help"}){activity.open(tool);assertNotNull(root.findViewWithTag("window"));root.findViewWithTag("close").performClick();assertNull(root.findViewWithTag("window"));}});}
 @Test public void notesSaveAndTaskRestores(){instrument.runOnMainSync(()->{activity.open("notes");View root=activity.getWindow().getDecorView();((EditText)root.findViewWithTag("note")).setText("Device persistence check");root.findViewWithTag("minimize").performClick();assertEquals(View.GONE,root.findViewWithTag("window").getVisibility());root.findViewWithTag("task").performClick();assertEquals(View.VISIBLE,root.findViewWithTag("window").getVisibility());root.findViewWithTag("close").performClick();activity.open("notes");assertEquals("Device persistence check",((EditText)root.findViewWithTag("note")).getText().toString());});}
 @Test public void windowFitsWorkspace(){instrument.runOnMainSync(()->{activity.open("settings");});instrument.waitForIdleSync();instrument.runOnMainSync(()->{View root=activity.getWindow().getDecorView(),w=root.findViewWithTag("window"),space=root.findViewWithTag("workspace");assertTrue(w.getWidth()>0);assertTrue(w.getHeight()>0);assertTrue(w.getWidth()<=space.getWidth());assertTrue(w.getHeight()<=space.getHeight());assertTrue(w.getTop()>=0);assertTrue(w.getLeft()>=0);});}
}
