package dev.magesh.win95;

import android.graphics.Rect;
import android.view.*;
import android.widget.EditText;
import androidx.window.layout.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=31,qualifiers="w840dp-h900dp-mdpi")
public class AdaptiveLayoutTest {
    @Test public void noHingeUsesWholeArea(){Rect safe=new Rect(12,24,828,880);assertEquals(safe,DesktopGeometry.largestPane(safe,Collections.emptyList(),8));}
    @Test public void verticalHingeAvoided(){assertEquals(new Rect(0,0,392,900),DesktopGeometry.largestPane(new Rect(0,0,840,900),Arrays.asList(new Rect(400,0,440,900)),8));}
    @Test public void zeroHeightTabletopAvoided(){assertEquals(new Rect(0,0,840,442),DesktopGeometry.largestPane(new Rect(0,0,840,900),Arrays.asList(new Rect(0,450,840,450)),8));}
    @Test public void multipleHingesAvoided(){assertEquals(new Rect(818,0,1240,900),DesktopGeometry.largestPane(new Rect(0,0,1240,900),Arrays.asList(new Rect(390,0,410,900),new Rect(790,0,810,900)),8));}
    @Test public void offscreenHingeIgnored(){Rect r=new Rect(0,0,300,600);assertEquals(r,DesktopGeometry.largestPane(r,Arrays.asList(new Rect(900,0,900,600)),8));}
    @Test public void keyboardAreaBeforeHingeIsKept(){assertEquals(new Rect(0,24,840,350),DesktopGeometry.largestPane(new Rect(0,24,840,350),Arrays.asList(new Rect(0,450,840,450)),8));}
    @Test public void postureAndResizePreserveOpenNote(){
        ActivityController<DesktopActivity> c=Robolectric.buildActivity(DesktopActivity.class).setup();
        try{
            DesktopActivity a=c.get();View decor=a.getWindow().getDecorView();layout(decor,840,900);a.open("notes");layout(decor,840,900);
            ((EditText)decor.findViewWithTag("note")).setText("Fold transition note");
            a.onWindowLayout(info(new Rect(400,0,440,900),true));layout(decor,840,900);
            View shell=decor.findViewWithTag("desktop-shell");assertTrue(shell.getRight()<=400);assertTrue(shell.getWidth()>0);
            assertEquals("Fold transition note",((EditText)decor.findViewWithTag("note")).getText().toString());
            a.onWindowLayout(new WindowLayoutInfo(Collections.emptyList()));layout(decor,840,900);assertEquals(840,shell.getWidth());
            layout(decor,280,600);View win=decor.findViewWithTag("window"),space=decor.findViewWithTag("workspace");assertTrue(win.getRight()<=space.getWidth());assertTrue(win.getBottom()<=space.getHeight());
            assertEquals("Fold transition note",((EditText)decor.findViewWithTag("note")).getText().toString());
        }finally{c.pause().stop().destroy();}
    }
    @Test public void systemInsetsAndKeyboardKeepControlsVisible(){
        ActivityController<DesktopActivity> c=Robolectric.buildActivity(DesktopActivity.class).setup();
        try{
            View decor=c.get().getWindow().getDecorView();layout(decor,840,900);
            View shell=decor.findViewWithTag("desktop-shell");View root=(View)shell.getParent();
            root.dispatchApplyWindowInsets(new WindowInsets.Builder().setInsets(WindowInsets.Type.systemBars(),android.graphics.Insets.of(20,24,30,40)).setInsets(WindowInsets.Type.ime(),android.graphics.Insets.of(0,0,0,300)).setVisible(WindowInsets.Type.ime(),true).build());
            layout(decor,840,900);assertEquals(20,shell.getLeft());assertEquals(24,shell.getTop());assertEquals(810,shell.getRight());assertEquals(600,shell.getBottom());
            root.dispatchApplyWindowInsets(new WindowInsets.Builder().setInsets(WindowInsets.Type.systemBars(),android.graphics.Insets.of(20,24,30,40)).build());layout(decor,840,900);assertEquals(860,shell.getBottom());
        }finally{c.pause().stop().destroy();}
    }
    @Test public void flatCreaseDoesNotShrinkDesktop(){ActivityController<DesktopActivity> c=Robolectric.buildActivity(DesktopActivity.class).setup();try{View decor=c.get().getWindow().getDecorView();layout(decor,840,900);c.get().onWindowLayout(info(new Rect(420,0,420,900),false));layout(decor,840,900);assertEquals(840,decor.findViewWithTag("desktop-shell").getWidth());}finally{c.pause().stop().destroy();}}
    private WindowLayoutInfo info(Rect bounds,boolean separating){
        FoldingFeature fold=new FoldingFeature(){
            public Rect getBounds(){return new Rect(bounds);}public boolean isSeparating(){return separating;}
            public OcclusionType getOcclusionType(){return bounds.width()>0?OcclusionType.FULL:OcclusionType.NONE;}
            public Orientation getOrientation(){return Orientation.VERTICAL;}public State getState(){return separating?State.HALF_OPENED:State.FLAT;}
        };
        return new WindowLayoutInfo(Arrays.asList(fold));
    }
    private void layout(View v,int w,int h){for(int i=0;i<5;i++){v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));v.layout(0,0,w,h);}}
}
