package dev.magesh.win95;

import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;
import static dev.magesh.win95.RetroStyle.*;

public class DesktopActivity extends Activity {
    private FrameLayout workspace, root;
    private LinearLayout shell, taskbar, window, startMenu, windowBody;
    private GridLayout desktop;
    private Button taskButton, startButton;
    private TextView clock;
    private SharedPreferences prefs;
    private LauncherApps launcher;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService loader=Executors.newSingleThreadExecutor();
    private List<AppEntry> apps=new ArrayList<>();
    private String active="", query="", calculation="";
    private boolean minimized=false, maximized=false, loading=true;
    private boolean callbackRegistered=false;
    private final Runnable ticker=new Runnable() { @Override public void run() { if(clock!=null) clock.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())); handler.postDelayed(this,15000); } };
    private final LauncherApps.Callback appCallback=new LauncherApps.Callback() {
        @Override public void onPackageRemoved(String p,UserHandle u){loadApps();}
        @Override public void onPackageAdded(String p,UserHandle u){loadApps();}
        @Override public void onPackageChanged(String p,UserHandle u){loadApps();}
        @Override public void onPackagesAvailable(String[] p,UserHandle u,boolean r){loadApps();}
        @Override public void onPackagesUnavailable(String[] p,UserHandle u,boolean r){loadApps();}
    };
    static final class AppEntry {
        final LauncherActivityInfo info; final String label, key;
        AppEntry(LauncherActivityInfo info,long serial) { this.info=info; label=info.getLabel().toString(); key=info.getComponentName().flattenToString()+"@"+serial; }
    }
    int d(float n){return dp(this,n);}
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("desktop",MODE_PRIVATE); launcher=getSystemService(LauncherApps.class);
        getWindow().setDecorFitsSystemWindows(false);
        getWindow().setNavigationBarContrastEnforced(false);
        if(state!=null) { active=state.getString("active",""); query=state.getString("query",""); calculation=state.getString("calculation",""); minimized=state.getBoolean("minimized"); maximized=state.getBoolean("maximized"); }
        buildDesktop();
        if(Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::back);
        loadApps();
        try { launcher.registerCallback(appCallback,handler); callbackRegistered=true; } catch(SecurityException ignored) { }
    }
    @Override protected void onResume(){super.onResume(); handler.removeCallbacks(ticker); ticker.run(); loadApps();}
    @Override protected void onPause(){super.onPause();handler.removeCallbacks(ticker);}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null); if(callbackRegistered) launcher.unregisterCallback(appCallback); loader.shutdownNow();super.onDestroy();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);closeStart(); hideKeyboard(); if(window!=null) { minimized=true; window.setVisibility(View.GONE); }}
    @Override protected void onSaveInstanceState(Bundle b){b.putString("active",active);b.putString("query",query);b.putString("calculation",calculation);b.putBoolean("minimized",minimized);b.putBoolean("maximized",maximized);super.onSaveInstanceState(b);}
    // API 33+ uses the platform OnBackInvokedDispatcher registered above.
    // This override supports Android 11/12 without requiring AndroidX at runtime.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed(){back();}
    private void back(){if(startMenu!=null)closeStart();else if(!active.isEmpty())closeWindow();}
    private void hideKeyboard(){getSystemService(InputMethodManager.class).hideSoftInputFromWindow(root.getWindowToken(),0); root.clearFocus();}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void buildDesktop(){
        root=new FrameLayout(this);root.setBackgroundColor(prefs.getInt("wallpaper",TEAL));root.setFocusableInTouchMode(true);
        shell=vertical();root.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        root.setOnApplyWindowInsetsListener((v,i)->{
            android.graphics.Insets safe=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
            android.graphics.Insets ime=i.getInsets(WindowInsets.Type.ime());
            shell.setPadding(safe.left,safe.top,safe.right,Math.max(safe.bottom,ime.bottom));return i;
        });
        workspace=new FrameLayout(this); workspace.setTag("workspace");shell.addView(workspace,new LinearLayout.LayoutParams(-1,0,1));
        workspace.setOnClickListener(v->closeStart());
        workspace.setOnLongClickListener(v->{open("settings");return true;});
        ScrollView desktopScroll=new ScrollView(this); desktopScroll.setFillViewport(true);workspace.addView(desktopScroll,new FrameLayout.LayoutParams(-1,-1));
        desktop=new GridLayout(this);desktop.setTag("desktop"); desktop.setPadding(d(6),d(10),d(6),d(12));desktopScroll.addView(desktop);
        desktop.setOnClickListener(v->closeStart()); desktop.setOnLongClickListener(v->{open("settings");return true;});
        workspace.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol || b-t!=ob-ot){renderIcons();layoutWindow();layoutStart();}});
        taskbar=horizontal();taskbar.setBackground(new Bevel(false,SILVER));taskbar.setPadding(d(4),d(3),d(4),d(3));
        shell.addView(taskbar,new LinearLayout.LayoutParams(-1,-2));
        startButton=button(this,"▦ Start");startButton.setTypeface(null,android.graphics.Typeface.BOLD);startButton.setTag("start");startButton.setOnClickListener(v->toggleStart());taskbar.addView(startButton,new LinearLayout.LayoutParams(-2,-2));
        taskButton=button(this,"");taskButton.setTag("task");taskButton.setSingleLine(true);taskButton.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams taskParams=new LinearLayout.LayoutParams(0,-2,1); taskParams.setMargins(d(6),0,d(6),0);taskbar.addView(taskButton,taskParams);
        taskButton.setOnClickListener(v->{closeStart();if(window!=null){minimized=!minimized;window.setVisibility(minimized?View.GONE:View.VISIBLE);}});
        clock=text(this,"",13,Color.BLACK);clock.setGravity(Gravity.CENTER);clock.setPadding(d(8),0,d(8),0);clock.setBackground(new Bevel(true,SILVER));clock.setTag("clock");clock.setContentDescription("Date and time");clock.setOnClickListener(v->open("clock"));taskbar.addView(clock,new LinearLayout.LayoutParams(-2,d(48)));
        setContentView(root);root.requestApplyInsets();renderIcons();if(!active.isEmpty()){String saved=active;boolean savedMinimized=minimized;active="";open(saved);minimized=savedMinimized;window.setVisibility(minimized?View.GONE:View.VISIBLE);}else taskButton.setVisibility(View.INVISIBLE);
    }
    private void renderIcons(){
        if(desktop==null || workspace.getWidth()==0)return;
        int count=Math.max(2,Math.min(8,(workspace.getWidth()-d(12))/d(prefs.getBoolean("large",false)?116:96)));
        desktop.removeAllViews();desktop.setOrientation(GridLayout.VERTICAL);
        int iconHeight=d(prefs.getBoolean("large",false)?116:100);
        int total=6;Set<String> savedPins=prefs.getStringSet("pinned",Collections.emptySet());for(AppEntry app:apps)if(savedPins.contains(app.key))total++;
        desktop.setRowCount(Math.max(Math.max(1,workspace.getHeight()/iconHeight),(total+count-1)/count));desktop.setColumnCount(count);
        addDesktopIcon("computer","My Computer",()->open("apps"),count,null);
        addDesktopIcon("files","My Documents",()->openFiles(),count,null);
        addDesktopIcon("notes","Notepad",()->open("notes"),count,null);
        addDesktopIcon("settings","Control Panel",()->open("settings"),count,null);
        addDesktopIcon("calc","Calculator",()->open("calc"),count,null);
        addDesktopIcon("bin","Recycle Bin",()->open("bin"),count,null);
        Set<String> pinned=prefs.getStringSet("pinned",Collections.emptySet());
        for(AppEntry entry:apps)if(pinned.contains(entry.key))addDesktopIcon("apps",entry.label,()->launch(entry),count,entry);
    }
    private void addDesktopIcon(String kind,String title,Runnable action,int count,AppEntry app){
        LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);tile.setPadding(d(4),d(5),d(4),d(5));tile.setContentDescription(title);tile.setFocusable(true);
        if(app==null)tile.addView(new PixelIcon(this,kind),new LinearLayout.LayoutParams(d(38),d(38)));
        else { ImageView icon=new ImageView(this);try{icon.setImageDrawable(app.info.getBadgedIcon(0));}catch(RuntimeException ignored){}tile.addView(icon,new LinearLayout.LayoutParams(d(38),d(38))); }
        TextView label=text(this,title,13,Color.WHITE);label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(TextUtils.TruncateAt.END);label.setShadowLayer(1,1,1,Color.BLACK);tile.addView(label,new LinearLayout.LayoutParams(-1,-2));
        GridLayout.LayoutParams params=new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED),GridLayout.spec(GridLayout.UNDEFINED));
        params.width=(workspace.getWidth()-d(12))/count;params.height=d(prefs.getBoolean("large",false)?116:100);desktop.addView(tile,params);
        tile.setOnClickListener(v->{closeStart();action.run();});tile.setOnLongClickListener(v->{if(app==null)open("settings");else appMenu(app);return true;});
    }
    private void loadApps(){
        if(loader.isShutdown())return;
        loader.execute(()->{
            List<AppEntry> result=new ArrayList<>();
            try {
                for(UserHandle profile:launcher.getProfiles()) {
                    try { for(LauncherActivityInfo info:launcher.getActivityList(null,profile))if(!info.getComponentName().getPackageName().equals(getPackageName()))result.add(new AppEntry(info,getSystemService(UserManager.class).getSerialNumberForUser(info.getUser()))); }catch(SecurityException ignored) { }
                }
                result.sort(Comparator.comparing(a->a.label.toLowerCase(Locale.ROOT)));
            } catch(RuntimeException ignored) { }
            handler.post(()->{if(isDestroyed())return; apps=result;loading=false;renderIcons();if(active.equals("apps") && windowBody!=null)refreshAppList();});
        });
    }
    private void launch(AppEntry entry){
        try{launcher.startMainActivity(entry.info.getComponentName(),entry.info.getUser(),null,null);closeStart();hideKeyboard();}
        catch(RuntimeException e){toast("This app is unavailable. Check whether its profile is paused.");loadApps();}
    }
    private void appMenu(AppEntry entry){
        Set<String> pins=new HashSet<>(prefs.getStringSet("pinned",Collections.emptySet()));boolean pinned=pins.contains(entry.key);
        open("app-options");bodyText(entry.label,18);bodyText(entry.info.getComponentName().getPackageName(),12);
        bodyButton(pinned?"Remove desktop shortcut":"Pin to desktop",()->{if(pinned)pins.remove(entry.key);else pins.add(entry.key);prefs.edit().putStringSet("pinned",pins).apply();renderIcons();open("apps");});
        bodyButton("App information",()->{try{launcher.startAppDetailsActivity(entry.info.getComponentName(),entry.info.getUser(),null,null);}catch(RuntimeException e){toast("App information is unavailable.");}});
        bodyButton("Open",()->launch(entry));
    }
    private void toggleStart(){if(startMenu!=null){closeStart();return;}hideKeyboard();
        startMenu=vertical();startMenu.setTag("start-menu");startMenu.setBackground(new Bevel(false,SILVER));startMenu.setPadding(d(4),d(4),d(4),d(4));
        TextView brand=text(this,"Retro 95",24,Color.WHITE);brand.setTypeface(null,android.graphics.Typeface.BOLD);brand.setBackgroundColor(NAVY);brand.setPadding(d(12),d(10),d(12),d(10));startMenu.addView(brand);
        ScrollView scroll=new ScrollView(this);LinearLayout list=vertical();scroll.addView(list);startMenu.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        menuRow(list,"apps","Programs",()->open("apps"));menuRow(list,"notes","Documents",()->open("notes"));menuRow(list,"settings","Settings",()->open("settings"));menuRow(list,"apps","Find apps…",()->open("apps"));menuRow(list,"computer","Run…",()->open("run"));menuRow(list,"computer","Help",()->open("help"));
        menuRow(list,"settings","Choose home app…",this::chooseHome);menuRow(list,"computer","About Retro 95",()->open("about"));
        workspace.addView(startMenu);layoutStart();startMenu.bringToFront();
    }
    private void menuRow(LinearLayout list,String kind,String label,Runnable action){
        LinearLayout row=horizontal();row.setPadding(d(10),0,d(10),0);row.addView(new PixelIcon(this,kind),new LinearLayout.LayoutParams(d(30),d(30)));
        Button b=button(this,label);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->{closeStart();action.run();});row.addView(b,new LinearLayout.LayoutParams(0,d(52),1));list.addView(row);row.setOnClickListener(v->{closeStart();action.run();});
    }
    private void layoutStart(){if(startMenu==null)return; FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Math.min(d(310),Math.max(d(100),workspace.getWidth()-d(8))),Math.min(d(530),Math.max(d(120),workspace.getHeight()-d(6))),Gravity.BOTTOM|Gravity.START);p.setMargins(d(2),0,0,d(2));startMenu.setLayoutParams(p);}
    private void closeStart(){if(startMenu!=null){workspace.removeView(startMenu);startMenu=null;}}
    void open(String type){
        closeStart();closeWindow();active=type;minimized=false;
        window=vertical();window.setTag("window");window.setPadding(d(3),d(3),d(3),d(3));window.setBackground(new Bevel(false,SILVER));
        LinearLayout title=horizontal();title.setBackgroundColor(NAVY);
        TextView name=text(this,titleFor(type),14,Color.WHITE);name.setTypeface(null,android.graphics.Typeface.BOLD);name.setSingleLine(true);name.setEllipsize(TextUtils.TruncateAt.END);name.setPadding(d(7),0,d(3),0);title.addView(name,new LinearLayout.LayoutParams(0,d(48),1));
        Button min=button(this,"_");min.setContentDescription("Minimize window");min.setTag("minimize");min.setOnClickListener(v->{minimized=true;window.setVisibility(View.GONE);hideKeyboard();});title.addView(min,new LinearLayout.LayoutParams(d(48),d(44)));
        Button max=button(this,"□");max.setContentDescription("Maximize or restore window");max.setOnClickListener(v->{maximized=!maximized;layoutWindow();});title.addView(max,new LinearLayout.LayoutParams(d(48),d(44)));
        Button close=button(this,"×");close.setContentDescription("Close window");close.setTag("close");close.setOnClickListener(v->closeWindow());title.addView(close,new LinearLayout.LayoutParams(d(48),d(44)));window.addView(title);
        windowBody=vertical();windowBody.setPadding(d(8),d(6),d(8),d(6));window.addView(windowBody,new LinearLayout.LayoutParams(-1,0,1));
        workspace.addView(window);layoutWindow();taskButton.setVisibility(View.VISIBLE);taskButton.setText(titleFor(type));
        switch(type){
            case "apps":buildApps();break;case "notes":buildNotes();break;case "calc":buildCalculator();break;case "settings":buildSettings();break;
            case "clock": buildClock();break;case "run":buildRun();break;case "bin":bodyText("Recycle Bin is empty.",18);bodyText("Removing a shortcut does not uninstall its app. To uninstall apps, use Android Settings.",14);break;
            case "about":bodyText("Retro 95",28);bodyText("Version 1.0.0\nA classic desktop for your Android phone.\nCreated for Magesh.",16);bodyText("Original drawn artwork. Independent project; not affiliated with Microsoft or Samsung. No Windows software or system files are included.",13);break;
            case "help":buildHelp();break;default:break;
        }
    }
    private String titleFor(String type){switch(type){case "apps":return "My Computer";case "notes":return "Untitled - Notepad";case "calc":return "Calculator";case "settings":return "Control Panel";case "clock":return "Date / Time";case "bin":return "Recycle Bin";case "run":return "Run";case "help":return "Help";case "app-options":return "Shortcut Properties";default:return "About Retro 95";}}
    private void layoutWindow(){if(window==null)return;int w=workspace.getWidth(),h=workspace.getHeight();if(w<1||h<1)return;
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(maximized?w:Math.min(d(620),w-d(12)),maximized?h:Math.min(d(640),h-d(16)),Gravity.CENTER);window.setLayoutParams(p);
    }
    private void closeWindow(){hideKeyboardIfReady();if(window!=null){workspace.removeView(window);window=null;windowBody=null;}active="";minimized=false;if(taskButton!=null){taskButton.setVisibility(View.INVISIBLE);taskButton.setText("");}}
    private void hideKeyboardIfReady(){if(root!=null)hideKeyboard();}
    private void bodyText(String value,int size){TextView t=text(this,value,size,Color.BLACK);t.setPadding(0,d(6),0,d(10));windowBody.addView(t,new LinearLayout.LayoutParams(-1,-2));}
    private void bodyButton(String label,Runnable action){Button b=button(this,label);b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=d(6);windowBody.addView(b,p);}
    private void makeScrollable(){LinearLayout content=windowBody;window.removeView(content);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(content);window.addView(sc,new LinearLayout.LayoutParams(-1,0,1));}
    private EditText input(String hint){EditText edit=new EditText(this);edit.setTextColor(Color.BLACK);edit.setHintTextColor(0xff606060);edit.setTextSize(16);edit.setBackground(new Bevel(true,Color.WHITE));edit.setPadding(d(8),d(8),d(8),d(8));edit.setHint(hint);edit.setMinimumHeight(d(48));return edit;}
    private void watch(EditText e,java.util.function.Consumer<String> changed){e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){changed.accept(s.toString());}public void afterTextChanged(Editable e){}});}
    private void buildApps(){
        EditText search=input("Search programs");search.setSingleLine(true);search.setTag("app-search");search.setText(query);windowBody.addView(search);
        TextView tip=text(this,"Tap to open · Hold to pin or view info",12,0xff404040);tip.setPadding(0,d(6),0,d(6));windowBody.addView(tip);
        ScrollView scroll=new ScrollView(this);LinearLayout list=vertical();list.setTag("app-list");scroll.addView(list);windowBody.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        watch(search,s->{query=s;refreshAppList();});refreshAppList();
    }
    private void refreshAppList(){
        if(windowBody==null)return;LinearLayout list=windowBody.findViewWithTag("app-list");if(list==null)return;list.removeAllViews();int count=0;
        String filter=query.trim().toLowerCase(Locale.ROOT);
        for(AppEntry entry:apps){if(!entry.label.toLowerCase(Locale.ROOT).contains(filter) && !entry.info.getComponentName().getPackageName().toLowerCase(Locale.ROOT).contains(filter))continue;
            LinearLayout row=horizontal();row.setPadding(d(4),d(2),d(4),d(2));row.setBackgroundColor(Color.WHITE);row.setMinimumHeight(d(56));row.setFocusable(true);row.setContentDescription(entry.label);
            ImageView icon=new ImageView(this);try{icon.setImageDrawable(entry.info.getBadgedIcon(0));}catch(RuntimeException ignored){}row.addView(icon,new LinearLayout.LayoutParams(d(32),d(32)));
            String label=entry.label+(entry.info.getUser().equals(android.os.Process.myUserHandle())?"":" [work]");TextView t=text(this,label,16,Color.BLACK);t.setPadding(d(10),0,0,0);t.setMaxLines(2);row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            row.setOnClickListener(v->launch(entry));row.setOnLongClickListener(v->{appMenu(entry);return true;});list.addView(row,new LinearLayout.LayoutParams(-1,-2));count++;
        }
        if(count==0){TextView empty=text(this,loading?"Loading programs…":"No programs found.",16,Color.BLACK);empty.setPadding(d(8),d(16),d(8),d(16));list.addView(empty);}
    }
    private void buildNotes(){
        TextView menu=text(this,"File     Edit     View",14,Color.BLACK);windowBody.addView(menu);
        EditText note=input("Write something…");note.setTag("note");note.setGravity(Gravity.TOP|Gravity.START);note.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);note.setText(prefs.getString("note","Welcome to Retro 95!\n\nYour notes save automatically on this device."));
        windowBody.addView(note,new LinearLayout.LayoutParams(-1,0,1));TextView status=text(this,"Saved on this device",12,0xff404040);windowBody.addView(status);
        watch(note,s->{prefs.edit().putString("note",s).apply();status.setText("Saved on this device · "+s.length()+" characters");});
        bodyButton("Share note…",()->{Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,note.getText().toString());safeStart(Intent.createChooser(send,"Share note"));});
    }
    private void buildCalculator(){
        makeScrollable();EditText display=input("0");display.setTag("calc-display");display.setSingleLine(true);display.setText(calculation);display.setInputType(android.text.InputType.TYPE_CLASS_TEXT);windowBody.addView(display);watch(display,s->calculation=s);
        TextView error=text(this,"",13,0xff800000);error.setTag("calc-error");windowBody.addView(error);
        String[][] keys={{"C","(",")","⌫"},{"7","8","9","/"},{"4","5","6","*"},{"1","2","3","-"},{"0",".","=","+"}};
        for(String[] line:keys){LinearLayout row=horizontal();windowBody.addView(row);for(String key:line){Button b=button(this,key);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(52),1);p.setMargins(d(2),d(2),d(2),d(2));row.addView(b,p);b.setOnClickListener(v->{String val=display.getText().toString();error.setText("");switch(key){case "C":display.setText("");break;case "⌫":display.setText(val.isEmpty()?"":val.substring(0,val.length()-1));break;case "=":try{display.setText(Calculator.evaluate(val));}catch(RuntimeException e){error.setText("Invalid calculation or division by zero");}break;default:if(val.length()<200)display.setText(val+key);}display.setSelection(display.length());});}}
    }
    private void buildSettings(){
        makeScrollable();bodyText("Display Properties",22);bodyText("Desktop color",14);
        LinearLayout colors=horizontal();windowBody.addView(colors);int[] palette={TEAL,0xff3a6ea5,0xff008000,0xff800080,0xff000000};
        for(int color:palette){Button b=button(this," ");b.setBackground(new Bevel(false,color));b.setContentDescription(String.format("Desktop color #%06X",color&0xffffff));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(48),1);p.setMargins(d(3),0,d(3),0);colors.addView(b,p);b.setOnClickListener(v->{prefs.edit().putInt("wallpaper",color).apply();root.setBackgroundColor(color);});}
        bodyButton(prefs.getBoolean("large",false)?"Desktop icons: Large (tap for standard)":"Desktop icons: Standard (tap for large)",()->{prefs.edit().putBoolean("large",!prefs.getBoolean("large",false)).apply();renderIcons();open("settings");});
        bodyButton("Set Retro 95 as default launcher",this::chooseHome);bodyButton("Android settings",()->safeStart(new Intent(Settings.ACTION_SETTINGS)));bodyButton("Choose another home app",()->safeStart(new Intent(Settings.ACTION_HOME_SETTINGS)));
        bodyText("System",18);bodyText("Android "+Build.VERSION.RELEASE+" · API "+Build.VERSION.SDK_INT+"\n"+Build.MANUFACTURER+" "+Build.MODEL+"\nUsable workspace: "+Math.round(workspace.getWidth()/getResources().getDisplayMetrics().density)+" × "+Math.round(workspace.getHeight()/getResources().getDisplayMetrics().density)+" dp",14);
        bodyText("Home screen and built-in tools use the classic theme. Android settings, the lock screen, recents, notifications and installed apps keep their own appearance.",14);bodyButton("Help & installation",()->open("help"));
    }
    private void buildClock(){
        makeScrollable();bodyText(DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT).format(new Date()),18);
        Calendar selected=Calendar.getInstance(); selected.set(Calendar.DAY_OF_MONTH,1);
        LinearLayout nav=horizontal();Button previous=button(this,"‹");previous.setContentDescription("Previous month");Button next=button(this,"›");next.setContentDescription("Next month");TextView month=text(this,"",16,Color.BLACK);month.setGravity(Gravity.CENTER);
        nav.addView(previous,new LinearLayout.LayoutParams(d(48),d(48)));nav.addView(month,new LinearLayout.LayoutParams(0,d(48),1));nav.addView(next,new LinearLayout.LayoutParams(d(48),d(48)));windowBody.addView(nav);
        GridLayout calendar=new GridLayout(this);calendar.setColumnCount(7);calendar.setBackground(new Bevel(true,Color.WHITE));calendar.setPadding(d(4),d(4),d(4),d(4));windowBody.addView(calendar,new LinearLayout.LayoutParams(-1,-2));
        Runnable render=()->{calendar.removeAllViews();month.setText(new java.text.SimpleDateFormat("MMMM yyyy",Locale.getDefault()).format(selected.getTime()));
            String[] days={"Su","Mo","Tu","We","Th","Fr","Sa"};for(String day:days)calendarCell(calendar,day,true);
            int offset=selected.get(Calendar.DAY_OF_WEEK)-Calendar.SUNDAY;for(int i=0;i<offset;i++)calendarCell(calendar,"",false);
            for(int i=1;i<=selected.getActualMaximum(Calendar.DAY_OF_MONTH);i++)calendarCell(calendar,Integer.toString(i),false);
        };
        previous.setOnClickListener(v->{selected.add(Calendar.MONTH,-1);render.run();});next.setOnClickListener(v->{selected.add(Calendar.MONTH,1);render.run();});render.run();
        bodyButton("Android date & time settings",()->safeStart(new Intent(Settings.ACTION_DATE_SETTINGS)));
    }
    private void calendarCell(GridLayout grid,String value,boolean header){TextView cell=text(this,value,14,header?NAVY:Color.BLACK);cell.setGravity(Gravity.CENTER);if(header)cell.setTypeface(null,android.graphics.Typeface.BOLD);GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED),GridLayout.spec(GridLayout.UNDEFINED,1f));p.width=0;p.height=d(36);grid.addView(cell,p);}
    private void buildRun(){makeScrollable();bodyText("Type a program name, package name, or web address.",16);EditText command=input("e.g. camera or https://example.com");command.setTag("run-input");command.setSingleLine(true);windowBody.addView(command);bodyButton("OK",()->{String s=command.getText().toString().trim();if(s.isEmpty()){toast("Enter a program or web address.");return;}if(s.startsWith("https://")||s.startsWith("http://")){safeStart(new Intent(Intent.ACTION_VIEW,Uri.parse(s)));return;}for(AppEntry app:apps)if(app.label.equalsIgnoreCase(s)||app.info.getComponentName().getPackageName().equalsIgnoreCase(s)){launch(app);return;}query=s;open("apps");});}
    private void buildHelp(){makeScrollable();bodyText("Welcome to Retro 95",22);bodyText("1. Open Control Panel and select ‘Set Retro 95 as default launcher’. Confirm in Android’s home-app chooser.\n\n2. Tap My Computer or Start → Programs to browse your apps. Hold an app to pin it to the desktop or open its Android information.\n\n3. Use _ to minimize a window, □ to maximize, and × to close. The taskbar restores minimized windows. Back closes the menu or window.\n\n4. Notepad saves automatically. Calculator supports +, −, *, / and brackets. Run finds apps or opens http/https addresses. My Documents opens Android’s file browser.\n\n5. To return to One UI: Control Panel → Choose another home app → One UI Home.\n\nThe layout adapts to rotation, split screen, font size, display cutouts, the keyboard and navigation insets. Only one built-in tool window is active at a time.\n\nNo ads, account, analytics, network permission or broad storage permission. Work-profile apps appear when Android allows access; Samsung Secure Folder and Android Private Space are not supported.",15);}
    private void chooseHome(){try{RoleManager roles=getSystemService(RoleManager.class);if(roles.isRoleAvailable(RoleManager.ROLE_HOME)&&!roles.isRoleHeld(RoleManager.ROLE_HOME)){startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME),95);}else safeStart(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(RuntimeException e){safeStart(new Intent(Settings.ACTION_HOME_SETTINGS));}}
    private void openFiles(){Intent files=new Intent(Intent.ACTION_OPEN_DOCUMENT);files.setType("*/*");files.addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(files,96);}catch(ActivityNotFoundException e){toast("No document picker is installed.");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==96&&result==RESULT_OK&&data!=null&&data.getData()!=null){Intent view=new Intent(Intent.ACTION_VIEW);view.setDataAndType(data.getData(),getContentResolver().getType(data.getData()));view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);safeStart(view);}}
    private void safeStart(Intent intent){try{startActivity(intent);}catch(RuntimeException e){toast("No app is available for this action.");}}
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
}
