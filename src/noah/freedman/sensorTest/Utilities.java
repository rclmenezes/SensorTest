package noah.freedman.sensorTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class Utilities {//stores static global variables and functions
	//Threshold Levels
	final static public float hammingThreshold = 20; //best match: 0, highest possible value:infinite
	final static public float hammingSquaredThreshold = 1; //best match: 0, highest possible value:infinite
	final static public float crossCorrelationThreshold = (float) 0.6; //Best match: 1, lowest possible value: 0
	final static public float validityThreshold = (float) 0.5; //
	//Global Vars
	final static private String rootFolderPath = "sensorData/";
	final static public int maxRecordLength = 5000; //maximum length of sensor data file, in lines 
	final static public int columns = 3; //3 columns for x,y,z sensors
	static public int appState = 0; //appState. 0 = enrolling; 1 = verify; 2 = identify;
	
	static public String selectedFile;
	static public ArrayList<float[]> testSignature;
	
	public static ArrayList<float[]> errorData = new ArrayList<float[]>(); //hamming, hammingSquared, Cross-Correlation, valid
	
	public static File rootDirectory() {
		File f = new File(Environment.getExternalStorageDirectory(), rootFolderPath);
		return f;		
	}
	public static String arrayListFloatToString(ArrayList<float[]> al, int floatSizes, String seperator) {
		String s = "";
		for (int i = 0; i < al.size(); i++) {
			for (int j = 0; j < floatSizes; j++) {
				s += String.valueOf(al.get(i)[j]);
				if (j != floatSizes - 1) s += seperator;
			}         
			if (i != al.size() - 1) s += "\n";
		}
		return s;
	}
	public static String arrayToString(float[] a, String separator) {
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0]);
	        for (int i=1; i<a.length; i++) {
	            result.append(separator);
	            result.append(a[i]);
	        }
	    }
	    return result.toString();
	}
	public static String arrayToString(String[] a, String separator) {
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0]);
	        for (int i=1; i<a.length; i++) {
	            result.append(separator);
	            result.append(a[i]);
	        }
	    }
	    return result.toString();
	}
	public static String[] getFileNamesList() {
		File root = rootDirectory();
        root.mkdir();
        if (root.canRead()) {
        	return root.list();
        } else {
        	String[] error = {"Cannot read from SD card"};
        	return error;
        }
	}
	public static void saveFiletoSD(String filename, String output) {
        File file;
        try {
            File root = rootDirectory();
            root.mkdir();
            if (root.canWrite()){
                file = new File(root, filename);
                FileWriter writer = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(writer);
                out.write(output);
                out.close();
            }
        } catch (IOException e) {
            Log.e("Could not write file " + e.getMessage(), null);
        } 
	}
	//saves a file to SD if file doesn't exist. If file does exist, creates a new filename with a NONCE at end.
	//returns unique id of file. returns -1 for error.
	public static String saveUniqueFiletoSD(String filename, String output) {
        File file;
        String filePrefix = filename.substring(0, filename.lastIndexOf("."));
        String extension = filename.substring(filename.lastIndexOf("."), filename.length());
        int nonce = -1;
        try {
            File root = rootDirectory();
            root.mkdir();
            if (root.canWrite()) {
		        file = new File(root, filename);
		        if (!file.exists()) {
		        	writeFile(file, output);
		        	return String.valueOf("");
		        } else {
		        	while(file.exists()) { //find unique file
		        		file = new File(root, filePrefix + ++nonce + extension);
		        	}
		        	writeFile(new File(root, filePrefix + nonce + extension), output);
		        	return String.valueOf(nonce);
		        }
            } else {
            	return "error";
            }
        } catch (Exception e) {
        	Log.e("Could not read file " + e.getMessage(), null);
        	return (String)filePrefix + nonce + extension;
        }
	}
	private static void writeFile(File file, String output) {
		try {
	        FileWriter writer = new FileWriter(file);
	        BufferedWriter out = new BufferedWriter(writer);
	        out.write(output);
	        out.close();
        } catch (IOException e) {
            Log.e("Could not write file " + e.getMessage(), null);
        }
	}
	public static  ArrayList<float[]> loadFile(String fileName) {
		File file;
		BufferedReader reader;
		int i = 0;
		String line;
		ArrayList<float[]> data = new ArrayList<float[]>();
		
		try {
			file = new File(Utilities.rootDirectory(), fileName);
			if (file.canRead()) {
				//read file
				reader = new BufferedReader(new FileReader(file));
				i = 0;
				line = reader.readLine(); //throw out first line, header data
				while (line != null) {
					line = reader.readLine();
					if(line != null) {
						String[] lineData = line.split("\\,");
						float[] floatData = {Float.valueOf(lineData[0]), Float.valueOf(lineData[1]), Float.valueOf(lineData[2])};
						data.add(floatData);
						i++;
					}
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}
	public static String deleteAllData() {        
		//Delete all files
		File rootDir = Utilities.rootDirectory();
		try {
			if (!rootDir.canRead()) return("Cannot read root directory.");
			if (!rootDir.canWrite()) return("Cannot write root directory.");
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!files[i].delete()) return("Cannot delete file.");
			}
			return("Data deleted.");
		} catch (Exception e) {
			e.printStackTrace();
			return("Error: " + e);
		}
	}
}
