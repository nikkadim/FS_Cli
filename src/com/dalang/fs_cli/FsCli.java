package com.dalang.fs_cli;

import android.text.Html;
import java.util.ArrayList;

import com.dalang.fs_cli.EventSocketManager;
import com.dalang.fs_cli.FsPreferences;
import com.dalang.fs_cli.FsSetting;
import com.dalang.fs_cli.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class FsCli extends Activity implements OnSharedPreferenceChangeListener {
    /** Called when the activity is first created. */
	
    private SharedPreferences mPrefs;
    private FsSetting mSettings;
    private static final int REQ_FS_SETTINGS = 0;   
    
	private static final String SERVER_KEY = "server";
	private static final String PASSWORD_KEY = "password";
	private static final String PORT_KEY = "port";
	
	EditText termOut;
	EditText termIn;
	
	Button enter_button;
	EditText prompt_box;
	
	ArrayList<String> localHistory = new ArrayList<String>();
	int localHistoryIndex=0;
	

	EventSocketManager evtsock;
	boolean connected = false;
	
	static String []banner ={
			"            _____ ____     ____ _     ___            \n",
			"           |  ___/ ___|   / ___| |   |_ _|           \n",
			"           | |_  \\___ \\  | |   | |    | |          \n",
			"           |  _|  ___) | | |___| |___ | |            \n",
			"           |_|   |____/   \\____|_____|___|          \n",
			"\n",
			"*****************************************************\n",
			"* Anthony Minessale II, Ken Rice,                   *\n",
			"* Michael Jerris, Travis Cross                      *\n",
			"* FreeSWITCH (http://www.freeswitch.org)            *\n",
			"* Brought to you by ClueCon http://www.cluecon.com/ *\n",
			"*****************************************************\n",
			"* Dalang @ CRSCY                                    *\n",
			"*****************************************************\n",
			"\n",
			"Type /help <enter> to see a list of commands\n\n\n"};
	
	public void print_banner()
	{
		for (String strline : banner) {
			handle.sendMessage(Message.obtain(this.handle,0,strline));
		}
	}
	
    public Handler handle = new Handler(){
    	public void handleMessage(Message msg){
    		if (msg.what == 1){
        		termOut.append(Html.fromHtml("<font color='yellow'>" + (String)msg.obj + "</font>"));
        		termOut.append("\n");
    		} else {
    			termOut.append((String)msg.obj);
    		}
    		scrollDown();
    		prompt_box.requestFocusFromTouch();
    	}
    };
	
    /** Called when the activity is first created. */	 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_preferences) {
            doPreferences();
        } else if (id == R.id.menu_close_window) {
            doCloseWindow();
        } else if (id == R.id.menu_reset) {
            doResetFsCli();
        } else if (id == R.id.menu_send_email) {
            doEmailTranscript();
        } else if (id == R.id.menu_toggle_soft_keyboard) {
            doToggleSoftKeyboard();
        } else if (id == R.id.menu_toggle_wakelock) {
            doToggleWakeLock();
        } else if (id == R.id.menu_toggle_wifilock) {
            doToggleWifiLock();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void doPreferences() {
        startActivityForResult(new Intent(this, FsPreferences.class), REQ_FS_SETTINGS);
    }
    
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	 if(requestCode == REQ_FS_SETTINGS)  
         { 
         }
    	 else {
         }
    }
    
    private void doCloseWindow() {
        if(evtsock != null && evtsock.client != null) {	
        	if( evtsock.client.canSend()) {
        		evtsock.client.close();
        	}
        }
        connected = false;
        finish();
    }
    
    private void doEmailTranscript() {
        // Don't really want to supply an address, but
        // currently it's required, otherwise we get an
        // exception.
        String addr = "dongguoxing@crscy.com.cn";
        Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + addr));

        intent.putExtra("body", termOut.getText().toString().trim());
        startActivity(intent);
    }
    
    private void doToggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        
        termOut.setFocusable(false);
        prompt_box.setFocusableInTouchMode(true);
        prompt_box.setFocusable(true);
		prompt_box.requestFocus();
		prompt_box.setSelection(prompt_box.getText().length());

    }

    private void doToggleWakeLock() {
//        if (mWakeLock.isHeld()) {
//            mWakeLock.release();
//        } else {
//            mWakeLock.acquire();
//        }
    }
    
    private void doResetFsCli() {
    	SharedPreferences.Editor editor = mPrefs.edit();
    	editor.putString(SERVER_KEY, "127.0.0.1");  
    	editor.putString(PASSWORD_KEY, "ClueCon");
    	editor.putString(PORT_KEY, "8021");
    	editor.commit();
    	//mSettings.readPrefs(mPrefs);
    	/*
    	termOut.setText("");
    	
    	connected = evtsock.reset(mSettings);
		if(connected)
		{
			print_banner();
		}
		else
			termOut.append("Error Connecting\n");
		*/
    }

    private void doToggleWifiLock() {
//        if (mWifiLock.isHeld()) {
//            mWifiLock.release();
//        } else {
//            mWifiLock.acquire();
//        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        termOut = (EditText)findViewById(R.id.termOutput);
        termOut.setFocusable(false);
        termOut.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				termOut.setFocusable(true);
				prompt_box.setFocusable(false);
				termOut.requestFocus();
				return true;
			}
        });
        
        //termOut.setOnClickListener(termOut_onClicklsn);
        
        prompt_box = (EditText)findViewById(R.id.Input); 
        prompt_box.setSelection(prompt_box.getText().length());
        prompt_box.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				termOut.setFocusable(false);
		        prompt_box.setFocusableInTouchMode(true);
				prompt_box.setFocusable(true);
				prompt_box.requestFocus();
				//prompt_box.setSelection(prompt_box.getText().length());
				if(prompt_box.getSelectionStart() <= 2)
					prompt_box.setSelection(2);
				return true;
			}
        });        
        
        prompt_box.setOnKeyListener(new OnKeyListener(){
        	public boolean onKey(View v, int keyCode, KeyEvent event) { 
		        if (keyCode == KeyEvent.KEYCODE_DEL) { 
		    		if(prompt_box.getText().length() <= 2 || prompt_box.getSelectionStart() <= 2)
		    			return true;
		        }
		        
		        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
		        	if(prompt_box.getSelectionStart() <= 2)
		        		return true;
				}
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						enter_pressed();
						return true;
					} else {
						return false;
					}
				}
		        return false;
		    } 
		});
        
        enter_button = (Button)findViewById(R.id.enter);
        enter_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				enter_pressed();
			}
        });
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mSettings = new FsSetting(mPrefs);
        //mSettings.readPrefs(mPrefs);
        startTest(mSettings);
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
    	//super.onKeyDown(keyCode, event);
    	//prompt_box.setText(""+event.);
    	switch(keyCode){
	    	case KeyEvent.KEYCODE_DPAD_DOWN:
	    		retriveHistory(false);
	    		return true;
	    	case KeyEvent.KEYCODE_DPAD_UP:
	    		retriveHistory(true);
	    		prompt_box.setSelected(true);
	    		termOut.setSelected(false);	
	    		return true;
	    	case KeyEvent.KEYCODE_DPAD_CENTER:
	    		prompt_box.setText("DPAD_CENTER");
	    		return true;
    	}
    	//return true;
    	return super.onKeyDown(keyCode, event);
    }
    
    public void enter_pressed(){
		String cmd = prompt_box.getText().toString();
		//Spannable s = prompt_box.getText();
		//s.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		termOut.append(cmd+"\n");
		if(cmd.length() > 2 )
		{
			addHistory(cmd.substring(2));
			//term.exec(cmd+"\n");
			if(null != evtsock)
				evtsock.SendApiCommand(cmd.substring(2));
		}
		
		scrollDown();
		//prompt_box.selectAll();
		prompt_box.setText(R.string.Prompt);
        prompt_box.setSelection(prompt_box.getText().length());
		prompt_box.requestFocusFromTouch();
    }
    
    public void addHistory(String cmd){
    	localHistory.add(cmd);
    	localHistoryIndex=localHistory.size();
    }
    
    public void retriveHistory(boolean up){
    	if(up){
    		if(localHistoryIndex > 0){
    			localHistoryIndex--;
    			prompt_box.setText(">_"+localHistory.get(localHistoryIndex));
    			prompt_box.setSelection(prompt_box.getText().length());
    		}
    	}else{
    		if(localHistoryIndex < localHistory.size()){    			
    			prompt_box.setText(">_"+localHistory.get(localHistoryIndex));
    			prompt_box.setSelection(prompt_box.getText().length());
    			localHistoryIndex++;
    		}    		
    	}
    	//prompt_box.setText("Got me here!!!! motherfucker");
    }
    
    public void startTest(FsSetting setting){
    	//boolean lastconnect = connected;
    	if(connected == false) {
    		evtsock = new EventSocketManager(this, setting);
    		try{
    			connected = evtsock.do_connect();
    		}
    		catch (InterruptedException e)
    		{
    			Log.e("Connect failed", e.toString() );
    		}

    		if(connected)
    		{
    			// switch screen mode wouldn't print banner
    			//    		if(!lastconnect)
    			print_banner();
    		}
    		else
    			termOut.append("Error Connecting\n");

    	}
    }
    
    
    public void scrollDown(){
		final ScrollView sv = (ScrollView)findViewById(R.id.ScrollView01);
		sv.post(new Runnable(){ // ugly but works!!!!
			public void run(){
				sv.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		sv.setFocusable(false);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	mSettings.readPrefs(mPrefs);
    	if(evtsock != null)
    		connected = evtsock.changesetting(mSettings);

    	termOut.setText("");
		if(connected)
		{
			print_banner();
		}
		else
			termOut.append("Error Connecting\n");
    } 
}