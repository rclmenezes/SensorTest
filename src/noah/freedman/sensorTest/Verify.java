package noah.freedman.sensorTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//2.3 functionality commented out for now
//2.3//

public class Verify extends Activity {
	private Button OKButton;
	private Button cancelButton;
	private TextView textview;
	private Spinner spinner;
	private String[] fileNameArray;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_layout);
		textview = (TextView) findViewById(R.id.verifyText);
        OKButton = (Button) findViewById(R.id.OKButtonV);
        OKButton.setOnClickListener(OKButtonPress);
        cancelButton = (Button) findViewById(R.id.cancelButtonV);
        cancelButton.setOnClickListener(gotoMainMenu);
        
       	spinner = (Spinner) findViewById(R.id.spinner0);
       	updateSpinnerLists();
       	
       	textview.setText("Select username:");
	}
	private void updateSpinnerLists() { //initializations that are repeated on resume.
        //get a list of saved sensor files.
		if (!Utilities.rootDirectory().canRead()) {
			OKButton.setEnabled(false);
		} else {
			OKButton.setEnabled(true);
		}
		fileNameArray = Utilities.getFileNamesList();
		if (fileNameArray.length == 0) {
			fileNameArray = new String[]{"Error: Cannot read from SD Card."};
		} else {
			for (int i = 0; i < fileNameArray.length; i++) {
				if (fileNameArray[i].lastIndexOf(".") > 0) {
					fileNameArray[i] = fileNameArray[i].substring(0, fileNameArray[i].lastIndexOf("."));
				}
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, fileNameArray);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
	}
	@Override
	protected void onResume() {
		super.onResume();
		updateSpinnerLists();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	private OnClickListener gotoMainMenu = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Verify.this, MainMenu.class);
			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(intent);		
		}
	};
	private OnClickListener OKButtonPress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String fileName = (String)  Utilities.getFileNamesList()[spinner.getSelectedItemPosition()];
			Utilities.selectedFile = fileName;
			if (Utilities.selectedFile == null) {
				Toast.makeText(getApplicationContext(), "Error loading user file.", Toast.LENGTH_SHORT).show();
			} else {
				gotoRecord();
			}
		}
	};
	private void gotoRecord() {
		Utilities.appState = 1;
		Intent intent = new Intent(Verify.this, Record.class);
		startActivity(intent);	
	}
}
