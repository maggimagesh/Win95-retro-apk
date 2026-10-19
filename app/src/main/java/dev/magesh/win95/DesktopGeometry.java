package dev.magesh.win95;

import android.graphics.Rect;
import java.util.*;

/** Selects one contiguous control area. Coordinates are physical pixels relative to root. */
final class DesktopGeometry {
    private DesktopGeometry() {}
    static Rect largestPane(Rect safe, List<Rect> barriers, int clearance) {
        List<Rect> panes=new ArrayList<>();panes.add(new Rect(safe));
        for(Rect fold:barriers){
            List<Rect> next=new ArrayList<>();
            for(Rect pane:panes){
                // Rect.intersects rejects zero-width creases, which still separate a display.
                if(fold.right<pane.left||fold.left>pane.right||fold.bottom<pane.top||fold.top>pane.bottom){next.add(pane);continue;}
                if(fold.left>pane.left)add(next,new Rect(pane.left,pane.top,Math.min(pane.right,fold.left-clearance),pane.bottom));
                if(fold.right<pane.right)add(next,new Rect(Math.max(pane.left,fold.right+clearance),pane.top,pane.right,pane.bottom));
                if(fold.top>pane.top)add(next,new Rect(pane.left,pane.top,pane.right,Math.min(pane.bottom,fold.top-clearance)));
                if(fold.bottom<pane.bottom)add(next,new Rect(pane.left,Math.max(pane.top,fold.bottom+clearance),pane.right,pane.bottom));
            }
            panes=next;
        }
        Rect best=new Rect(safe.left,safe.top,safe.left,safe.top);long area=-1;
        for(Rect pane:panes){long candidate=(long)pane.width()*pane.height();if(candidate>area){best=pane;area=candidate;}}
        return best;
    }
    private static void add(List<Rect> panes,Rect pane){if(pane.width()>0&&pane.height()>0)panes.add(pane);}
}
