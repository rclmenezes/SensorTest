package noah.freedman.sensorTest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class Record extends Activity {
	private TextView result;
	private EditText editText;
	private SensorManager sensorManager;
	private Sensor accelSensor, gravitySensor;
	private float x, y, z, gravityX, gravityY, gravityZ;
	private int appState = 0; //0 = recording; 1 = reset and waiting for start; 2 = completed and waiting for reset
	
	final int recordLength = Utilities.maxRecordLength;
	final int recordCutoff = recordLength - 1;
	private Button startButton, saveButton, cancelButton;
	private Chronometer chronometer;
	//vars initialized in initVars()
	private ArrayList<float[]> sensorData;
	private int recordCounter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_layout);
		
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(startButtonPress);
		result = (TextView) findViewById(R.id.result);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
	  	gravitySensor = sensorManager.getSensorList(Sensor.TYPE_GRAVITY).get(0);
		
		//start in stopped state
		resetRecording();
	}
	private void initVars() {
		recordCounter = 1;
	}
	@Override
	protected void onResume() {
		super.onResume();
		//Change Sensor Delay variable to change rate at which sensor data is recorded

		resetRecording();
		sensorManager.registerListener(accelerationListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	@Override
	protected void onStop() {
		chronometer.stop(); //stop chronometer to prevent resource leaks
		sensorManager.unregisterListener(accelerationListener);
		sensorManager.unregisterListener(gravityListener);
		super.onStop();
	}
	
	private SensorEventListener accelerationListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int acc) {
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (appState == 0) {
				x = event.values[0] - gravityX;
				y = event.values[1] - gravityY;
				z = event.values[2] - gravityZ;
				
				sensorData.add(new float[]{x, y, z});
				recordCounter++;
				if (recordCounter > recordCutoff) {
					stopRecording();
				}
			}
		}
	};
	private SensorEventListener gravityListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int acc) {
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			//while values are updated on orientationListener events, their values are logged with acceleration values
			gravityX = event.values[0];
			gravityY = event.values[1];
			gravityZ = event.values[2];
		}
	};
	private OnClickListener startButtonPress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (appState == 0) {
				stopRecording();
			}
			else if (appState == 2) {
				resetRecording();
			} else {
				startRecording();
			}
		}
	};
	private OnClickListener saveButtonPress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String filename = editText.getText().toString();
			//check if filename is valid  \ / : * ? " < > | 
			if (filename.contains("\\") | filename.contains("/") | filename.contains(":") 
					| filename.contains("*") | filename.contains("?") | filename.contains("\"") 
					| filename.contains("<") | filename.contains(">") | filename.contains("|")) {
				Toast toast = Toast.makeText(getApplicationContext(), filename + " is an invalid username. Usernames cannot contain  \\ / : * ? \" < > |", Toast.LENGTH_SHORT);
				toast.show();
			} else if (filename.length() == 0) {
				Toast.makeText(getApplicationContext(), "Enter a username", Toast.LENGTH_SHORT).show();
			} else { //save
				saveRecording(filename);
			}
			//toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
		}
	};

	private OnClickListener gotoMainMenu = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Record.this, MainMenu.class);
			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(intent);		
		}
	};
	public void startRecording() {
		sensorData = new ArrayList<float[]>();
		appState = 0;
		result.setText("Recording");
		startButton.setText("Stop");
		chronometer.setVisibility(View.VISIBLE);
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
	}
	public void resetRecording() {
		result.setText("Ready to Record 3d Signature.");
		startButton.setText("Start");
		startButton.setEnabled(true);
		result.setText("Press start button to record motion. Press stop button when complete.");
		initVars();
		appState = 1;
		chronometer.setVisibility(View.INVISIBLE);
		chronometer.setBase(SystemClock.elapsedRealtime());
	}
	public void stopRecording() {
		result.setText("");
		//disable start and stop button
		appState = 2;
		startButton.setEnabled(false);
		chronometer.setVisibility(View.INVISIBLE);
		chronometer.stop();

		if (Utilities.appState == 0) { //if enrolling, open save data window
			setContentView(R.layout.save_file);
			
			saveButton = (Button) findViewById(R.id.saveButton);
			saveButton.setOnClickListener(saveButtonPress);
			cancelButton = (Button) findViewById(R.id.cancelButton0);
			cancelButton.setOnClickListener(gotoMainMenu);
			editText = (EditText) findViewById(R.id.editInput);
		} else if (Utilities.appState == 1 | Utilities.appState == 2) { //if verify or identify, set Utilities.testSignature
			Utilities.testSignature = sensorData;
			Intent intent = new Intent(Record.this, Results.class);
			startActivity(intent);		
		}
	}
	public void saveRecording(String filename) {
		if (filename.length() < 1) {
			filename = "no_name";
		}
		//generate string of data
		String output = generateFileOutput();
		//save data
		String results = Utilities.saveUniqueFiletoSD(filename + ".csv", output);
		if (results == "error") {
			Toast.makeText(getApplicationContext(), "Error saving signature for " + filename, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getBaseContext(), filename + "'s signature was successfully enrolled.\n", Toast.LENGTH_LONG).show();
			gotoMainMenu.onClick(null);
		}
	}
	private String generateFileOutput() {
		String separator = ",";
		String linebreak = "\n";
		StringBuffer result = new StringBuffer();
		//variables to store xVals, yVals, zVals
		//header
		result.append("x");
		result.append(separator);
		result.append("y");
		result.append(separator);
		result.append("z");
		result.append(linebreak);
		//POST-PROCESSING
		
	    for (int i = 1; i < recordCounter - 1; i++) { //Don't save last line of data, since using delta rotation values
	    	//start at 1, not 0, as first recorded output is all zeroes.
	    	result.append(sensorData.get(i)[0]); //x
			result.append(separator);
			result.append(sensorData.get(i)[1]); //y
			result.append(separator);
			result.append(sensorData.get(i)[2]); //z
			result.append(linebreak);
	    }
	    
	    return result.toString();
	}
}
