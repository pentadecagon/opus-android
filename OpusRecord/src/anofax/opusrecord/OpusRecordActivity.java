package anofax.opusrecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class OpusRecordActivity extends Activity {
	boolean _isRunning = false;	
	String _fname = null;
	
	class EncThread extends Thread {
		@Override		
		public void run(){
			Native.encode(_fname);
		}
	}
	EncThread _encoder = null;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opus_record);
	}
	
	void doStop(){
		Native.stopRecorder();
		_isRunning=false;
		try {
			_encoder.join();
		} catch (InterruptedException e) {}
		_encoder = null;
		MediaScannerConnection.scanFile(this, new String[]{_fname}, null, null );		  		
	}
	
	@Override	
	protected void onDestroy(){
		super.onDestroy();
		if( _isRunning ){
			doStop();
		}
		
	} 
	
	void showError(String text){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Error");
		alertDialog.setMessage(text);
		alertDialog.setPositiveButton("OK", null);
		alertDialog.show();
		
	}

	boolean createFileName() {		
		File path = Environment.getExternalStorageDirectory ();
		try {
			String s = path.getCanonicalPath() + "/OpusRecord/";
			_fname = s+new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US).format(new Date())+".opus";
			new File(s).mkdirs();
			Log.i("URI", _fname);
			new FileWriter(_fname).close();
			return true;
		} catch (IOException e) {
			showError("Could not Open File");
		}
		return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_opus_record, menu);
		return true;
	}

	public void buttonClicked( View v ){
		Button b = (Button) v;
		if( _isRunning ){
			doStop();
			b.setText( "Start Recording");			
		} else {
			if( !createFileName())
				return;
			_encoder = new EncThread();
			_encoder.start();
			Native.startRecorder();
			b.setText( "Stop Recording");
			_isRunning=true;
			
		}
	}

}