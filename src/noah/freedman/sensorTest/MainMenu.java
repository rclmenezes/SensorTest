package noah.freedman.sensorTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends Activity {
	private TextView tv;
	private Button enrollButton;
	private Button verifyButton;
	private Button identifyButton;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		
		tv  = (TextView) findViewById(R.id.main_menu_textview);
		tv.setText("Welcome to 3D signature biometric test");

		enrollButton = (Button) findViewById(R.id.enrollButton);
		enrollButton.setOnClickListener(gotoEnroll);
		verifyButton = (Button) findViewById(R.id.verifyButton);
		verifyButton.setOnClickListener(gotoVerify);
		identifyButton = (Button) findViewById(R.id.identifyButton);
		identifyButton.setOnClickListener(gotoIdentify);
		
		checkButtons();
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		checkButtons();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == R.id.deleteAll) {
				displayDeleteConfirmBox();
	    }
	    return true;
	}
	private void displayDeleteConfirmBox() {   
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("Are you sure you want to clear all enrolled data?");
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
		new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			String message = Utilities.deleteAllData();
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			checkButtons();
		}
		});

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
		new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			
		}
		});
		dialog.show();

	}
	private void checkButtons() {
		if (Utilities.getFileNamesList().length < 1) {
			identifyButton.setEnabled(false);
			verifyButton.setEnabled(false);
		} else {
			identifyButton.setEnabled(true);
			verifyButton.setEnabled(true);
		}
	}
	private OnClickListener gotoEnroll = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.appState = 0;
			Intent intent = new Intent(MainMenu.this, Record.class);
			startActivity(intent);		
		}
	};
	private OnClickListener gotoVerify = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.appState = 1;
			Intent intent = new Intent(MainMenu.this, Verify.class);
			startActivity(intent);			
		}
	};
	private OnClickListener gotoIdentify = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Utilities.appState = 2;
			Intent intent = new Intent(MainMenu.this, Record.class);
			startActivity(intent);			
		}
	};
}
