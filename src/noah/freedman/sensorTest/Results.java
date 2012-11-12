package noah.freedman.sensorTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Results extends Activity {
	private Button OKButton;
	private TextView textview;
	private ProgressBar progressBar;
	private String output;
	public float hh;
	public float hs;
	public float cr;
	public float vv;
	//for progress bar
	public int mProgressStatus = 0;
	public Handler mHandler = new Handler();
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results_layout);
        
        textview = (TextView) findViewById(R.id.resultsText0);
        OKButton = (Button) findViewById(R.id.OKButtonR);
        OKButton.setOnClickListener(gotoMainMenu);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        
        textview.setText("Loading");
        //VERIFY
        if (Utilities.appState == 1) {
        	verify();
        }
        //IDENTIFY
        else if (Utilities.appState == 2) {
        	identify();
        }
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	private OnClickListener gotoMainMenu = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Results.this, MainMenu.class);
			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(intent);	
		}
	};
	
	public String identify() {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setMax(100);
		new Thread(new Runnable() {
			public void run() {
				String[] files = Utilities.getFileNamesList();
				float counter = 0;
				float countTotal = files.length;
				output = "Results:\n";
				float hammingMin = Float.POSITIVE_INFINITY;
				String hammingFileString = "";
				float hammingSquaredMin = Float.POSITIVE_INFINITY;
				String hammingSquaredFileString = "";
				float crossCorrelMax = 0;
				String crossCorrelFileString = "";
				String noMatchFound = "No match found.";
				
				for (int i = 0; i < files.length; i++) {
					counter++;
					mProgressStatus = (int) Math.round((counter/countTotal)*100);
					DataCompare dataCompare = new DataCompare(Utilities.testSignature,Utilities.loadFile(files[i]));
					float[] cc = dataCompare.crossCorrelation();
					float crossCorrelation = (cc[0] + cc[1] + cc[2]) / 3;
					float hamming = dataCompare.hamming();
					float hammingSquared = dataCompare.hammingSquared();

					boolean validity = dataCompare.valid() >= Utilities.validityThreshold;
					boolean hammingPassed = ((hamming < Utilities.hammingThreshold) & (validity));
					boolean hammingSquaredPassed = ((hammingSquared < Utilities.hammingSquaredThreshold) & (validity));
					boolean crossCorrelationPassed = ((crossCorrelation > Utilities.crossCorrelationThreshold) & (validity));
					String ff = files[i].substring(0, files[i].lastIndexOf("."));
					output += ff + ":\n"
								+ "  Thresholds passed?\n     Hamming: " + hammingPassed
							+ "\n     Hamming-Squared: " + hammingSquaredPassed
							+ "\n     Cross Correlation: " + crossCorrelationPassed
								+ "\n  Results:\n       Hamming: " + hamming
								+ "\n       Hamming-Squared: " + hammingSquared
								+ "\n       Cross Correlation: " + crossCorrelation	
								+ "\n       Validity: " + validity + "\n\n";
					//Save best fits
					if (hamming < hammingMin) {
						hammingMin = hamming;
						if (hamming < Utilities.hammingThreshold) {
							hammingFileString = ff;
						} else {
							hammingFileString = noMatchFound;
						}
					}
					if (hammingSquared < hammingSquaredMin) {
						hammingSquaredMin = hammingSquared;
						if (hammingSquared < Utilities.hammingSquaredThreshold) {
							hammingSquaredFileString = ff;
						} else {
							hammingSquaredFileString = noMatchFound;
						}
						
					}
					if (crossCorrelation > crossCorrelMax) {
						crossCorrelMax = crossCorrelation;
						if (crossCorrelation > Utilities.crossCorrelationThreshold) {
							crossCorrelFileString = ff;
						} else {
							crossCorrelFileString = noMatchFound;
						}
						
						crossCorrelFileString = ff;
					}
					//hh = hamming;
					//hs = hammingSquared;
					//cr = crossCorrelation;
					//vv = dataCompare.valid();
					//saveErrorRateData();
					// Update the progress bar
					mHandler.post(new Runnable() {
                         public void run() {
                          	textview.setText("Loading..." + String.valueOf(mProgressStatus) + "%");
                        	progressBar.setProgress(mProgressStatus);
                         }
                     });
				}
				//Add best matches to output
				output = "Best matches: \n     Hamming: " + hammingFileString + "\n     Hamming Squared: " + hammingSquaredFileString
							+ "\n     Cross Correlation: " + crossCorrelFileString + "\n\n" + output;
                mHandler.post(new Runnable() {
                    public void run() {
                    	textview.setText(output);
                    	progressBar.setVisibility(View.GONE);
                    }
                });
				//Save results
			}
		}).start();
		return null;
	}
	public String verify() {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setMax(100);
		
		new Thread(new Runnable() {
			public void run() {
				String[] files = Utilities.getFileNamesList();
				int counter = 0;
				int countTotal = files.length;
				
				mProgressStatus = Math.round((counter/countTotal)*100);	
				
				DataCompare dataCompare = new DataCompare(Utilities.testSignature, Utilities.loadFile(Utilities.selectedFile));
				float[] cc = dataCompare.crossCorrelation();
				float crossCorrelation = (cc[0] + cc[1] + cc[2]) / 3;
				float hamming = dataCompare.hamming();
				float hammingSquared = dataCompare.hammingSquared();
				boolean validity = dataCompare.valid() >= Utilities.validityThreshold;
				boolean hammingPassed = ((hamming < Utilities.hammingThreshold) & (validity));
				boolean hammingSquaredPassed = ((hammingSquared < Utilities.hammingSquaredThreshold) & (validity));
				boolean crossCorrelationPassed = ((crossCorrelation > Utilities.crossCorrelationThreshold) & (validity));
				output = "Thresholds passed?\n   Hamming: " + hammingPassed
							+ "\n   Hamming-Squared: " + hammingSquaredPassed
							+ "\n   Cross Correlation: " + crossCorrelationPassed
							+ "\n\nResults:\n";
				output += "   Hamming: " + hamming
				+ "\n   Hamming-Squared: " + hammingSquared
				+ "\n   Cross Correlation: " + crossCorrelation  	
				+ "\n   Validity: " + validity + "\n\n";
				//hh = hamming;
				//hs = hammingSquared;
				//cr = crossCorrelation;
				//vv = dataCompare.valid();
				// Update the progress bar
                mHandler.post(new Runnable() {
                	public void run() {
                    	textview.setText(output);
                    	progressBar.setVisibility(View.GONE);
        				//saveErrorRateData();
                	}
                });        		
			}
		}).start();
		return null;
	}
	/*private void saveErrorRateData() {
		Utilities.errorData.add(new float[]{hh, hs, cr, vv});
		Utilities.saveFiletoSD("errorRates.csv", Utilities.arrayListFloatToString(Utilities.errorData, 4, ","));
	}*/
}


