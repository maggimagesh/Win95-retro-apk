package dev.magesh.win95;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.*;

final class RetroStyle {
    static final int SILVER=0xffc0c0c0, NAVY=0xff000080, TEAL=0xff008080;
    static int dp(Context c, float n) { return Math.round(n*c.getResources().getDisplayMetrics().density); }
    static TextView text(Context c, String value, int size, int color) {
        TextView t=new TextView(c); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setFontFeatureSettings("kern"); t.setGravity(android.view.Gravity.CENTER_VERTICAL);
        return t;
    }
    static Button button(Context c, String label) {
        Button b=new Button(c); b.setText(label); b.setTextSize(14); b.setTextColor(Color.BLACK);
        b.setAllCaps(false); b.setMinWidth(0); b.setMinimumWidth(0); b.setMinHeight(dp(c,48));
        b.setMinimumHeight(dp(c,48)); b.setPadding(dp(c,8),0,dp(c,8),0);
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new Bevel(true,SILVER));
        states.addState(new int[]{},new Bevel(false,SILVER)); b.setBackground(states);
        return b;
    }
    static final class Bevel extends Drawable {
        private final boolean inset; private final int fill; private final Paint p=new Paint();
        Bevel(boolean inset,int fill) { this.inset=inset; this.fill=fill; }
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); p.setColor(fill); c.drawRect(b,p);
            int light=inset?0xff404040:Color.WHITE, dark=inset?Color.WHITE:0xff404040;
            int edge=Math.max(1,Math.round(android.content.res.Resources.getSystem().getDisplayMetrics().density));
            for(int i=0;i<2*edge;i++) {
                p.setColor(i<edge?light:(inset?0xff808080:0xffdfdfdf));
                c.drawRect(b.left+i,b.top+i,b.right-i,b.top+i+1,p); c.drawRect(b.left+i,b.top+i,b.left+i+1,b.bottom-i,p);
                p.setColor(i<edge?dark:(inset?0xffdfdfdf:0xff808080));
                c.drawRect(b.left+i,b.bottom-i-1,b.right-i,b.bottom-i,p); c.drawRect(b.right-i-1,b.top+i,b.right-i,b.bottom-i,p);
            }
        }
        @Override public void setAlpha(int a) { p.setAlpha(a); }
        @Override public void setColorFilter(ColorFilter f) { p.setColorFilter(f); }
        @Override public int getOpacity() { return PixelFormat.OPAQUE; }
    }
    /** Original low-resolution artwork, scaled without bitmap filtering. */
    static final class PixelIcon extends View {
        private final String kind; private final Paint p=new Paint();
        PixelIcon(Context c,String kind) { super(c); this.kind=kind; setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
        private void box(Canvas c,int color,int x,int y,int w,int h) { p.setColor(color); c.drawRect(x,y,x+w,y+h,p); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas); canvas.save(); float s=Math.min(getWidth(),getHeight())/32f;
            canvas.translate((getWidth()-32*s)/2,(getHeight()-32*s)/2); canvas.scale(s,s);
            switch(kind) {
                case "apps": case "files":
                    box(canvas,0xff595000,2,9,28,20); box(canvas,0xffffff80,3,8,12,5);
                    box(canvas,0xff808000,3,12,27,17); box(canvas,0xffffff00,2,13,26,14); box(canvas,0xffffff80,3,14,24,2); break;
                case "notes":
                    box(canvas,0xff404040,7,3,21,27); box(canvas,Color.WHITE,5,1,21,27);
                    for(int y=7;y<24;y+=4) box(canvas,0xff000080,8,y,14,1);
                    box(canvas,0xff808080,5,1,2,27); break;
                case "calc":
                    box(canvas,0xff404040,5,2,23,29); box(canvas,SILVER,3,1,23,28); box(canvas,Color.WHITE,6,4,17,7);
                    for(int y=14;y<26;y+=5) for(int x=7;x<24;x+=6) box(canvas,0xff000080,x,y,4,3); break;
                case "settings":
                    box(canvas,0xff404040,3,4,26,24); box(canvas,SILVER,2,2,26,24);
                    for(int x=7;x<26;x+=7) { box(canvas,0xff808080,x,6,2,16); box(canvas,Color.WHITE,x-2,x%3==0?9:16,6,4); } break;
                case "bin":
                    box(canvas,Color.WHITE,8,7,17,23); box(canvas,0xff808080,9,8,15,21);
                    for(int x=11;x<24;x+=4) box(canvas,SILVER,x,10,2,16);
                    box(canvas,Color.WHITE,6,5,21,3); box(canvas,SILVER,12,2,10,3); break;
                case "clock":
                    p.setColor(Color.BLACK); canvas.drawCircle(16,16,14,p); p.setColor(Color.WHITE); canvas.drawCircle(15,15,12,p);
                    box(canvas,NAVY,14,7,2,10); box(canvas,NAVY,15,15,8,2); break;
                default:
                    box(canvas,0xff404040,2,3,28,21); box(canvas,Color.WHITE,1,2,28,21); box(canvas,0xff808080,3,4,24,17);
                    box(canvas,NAVY,5,6,20,13); box(canvas,0xff00ffff,7,8,7,3); box(canvas,SILVER,12,23,7,4);
                    box(canvas,Color.WHITE,7,27,18,3); break;
            }
            canvas.restore();
        }
    }
}
