package noah.freedman.sensorTest;


import java.util.ArrayList;

/*****************************************************************************/
/* DataCompare.java                                                          */
/* Author: Rodrigo Menezes, Vladimir Costescu, Noah Freedman                 */
/* ELE 386 Final Project                                                     */
/*                                                                           */
/* This DataCompare object takes in two ArrayList data sets and compares     */
/* them. It assumes the ArrayLists have arrays of floats inside them. It     */
/* compares the data using hamming distances, cross correlation and R^2      */
/* analysis.                                                                 */
/*                                                                           */
/* Warning: this does get a compiler error that CANNOT be avoided.           */
/*****************************************************************************/

public class DataCompare
{
	private static final double VALIDITY_CONSTANT = 0.5;
	private static final int XACCEL = 0;
	private static final int YACCEL = 1;
	private static final int ZACCEL = 2;
	private static final int THRESHOLD = 3;
	private ArrayList<float[]> input;
	private ArrayList<float[]> enroll;
	private int length;
	private float hamming;
	private float hammingSquared;
	private float[] correlation;
	private float validity;
	
	/***************************************************************/
	// Constructor for DataCompare object
	/***************************************************************/
	public DataCompare(ArrayList<float[]> inputData, ArrayList<float[]> enrollData)
	{
		int enrollOrigSize = enrollData.size();
		
		// Cut filler
		input = cutFiller(inputData);
		enroll = cutFiller(enrollData);
		//input = inputData;
		//enroll = enrollData;
		
		/*System.out.println(input.size());
		System.out.println(enroll.size());*/
		
		// Set data to same length
		if (input.size() < enroll.size())
			reduceArrayList(enroll, input.size(), enroll.size());
		else if (input.size() > enroll.size())
			reduceArrayList(input, enroll.size(), input.size());

		length = enroll.size();
		
		/*System.out.println(input.size());
		System.out.println(enroll.size());*/
			
		// Validity dependent on whether data was cut too much
		if (input.size() / enrollOrigSize <= 1) {
			validity = input.size() / enrollOrigSize;
		} else {
			validity = enrollOrigSize / input.size();
		}
		
		// Finds the hamming distance
		hamming = 0;	
		for (int i = 0; i < length; i++)
		{
			hamming += hamming(input.get(i), enroll.get(i));
			hammingSquared += hamming(input.get(i), enroll.get(i))
			                * hamming(input.get(i), enroll.get(i));
		}
		hamming = hamming/input.size(); // Dividing by time
		hammingSquared = hammingSquared/(input.size() * input.size());
		
		// Finds the cross correlation
		this.correlation = crossCorrelation(input, enroll);
	}
	
	/***************************************************************/
	// Returns the size of the data sets
	/***************************************************************/
	public float valid()
	{
		return validity;
	}
	
	/***************************************************************/
	// Returns the hamming distance as float
	/***************************************************************/
	public float hamming()
	{
		return hamming;
	}
	
	/***************************************************************/
	// Returns the hamming distance squared
	/***************************************************************/
	public float hammingSquared()
	{
		return hammingSquared;
	}
	
	/***************************************************************/
	// Returns the crossCorrelation as float[]
	/***************************************************************/
	public float[] crossCorrelation()
	{
		return correlation;
	}
	
	/***************************************************************/
	// Returns the crossCorrelation as float[]
	/***************************************************************/
	private void reduceArrayList(ArrayList<float[]> a, int start, int end)
	{
		for (int i = end; i > start; i--) {
			a.remove(start);
		}
	}
	
	/***************************************************************/
    // Eliminates "filler" when data starts by making sure hamming
    // when compared to 0 is above a certain threshold. Returns
    // ArrayList (out of convenience, really).
    /***************************************************************/
    private ArrayList<float[]> cutFiller(ArrayList<float[]> data)
    {
        // Temporary variables
        float[] zero = new float[3];
        zero[XACCEL] = 0;
        zero[YACCEL] = 0;
        zero[ZACCEL] = 0;
       
        // Moves along until hamming distance is above threshold
        // Makes sure there's enough movement
        int dataStart = 0;
        if (data.size() > 0) {
	        while (hamming(data.get(dataStart), zero) < THRESHOLD)
	        {
	            dataStart++;
	            if (dataStart >= data.size() - 1) {
	                break;
	            }
	        }
        }
           
        // Cuts out all the filler
        //if (data.size() > 0) {
        reduceArrayList(data, 0, dataStart);
        //}
        return data;
    }
	
	/***************************************************************/
	// Finds the hamming distance of two float arrays,
	// assuming arrays are of length 3. Returns float.
	/***************************************************************/
	private float hamming(float[] a, float[] b)
	{
		float hamming = 0;
		hamming += Math.abs(a[XACCEL] - b[XACCEL]);
		hamming += Math.abs(a[YACCEL] - b[YACCEL]);
		hamming += Math.abs(a[ZACCEL] - b[ZACCEL]);
		return hamming;
	}
	
	/***************************************************************/
	// Finds crossCorrelation of x, y and z. Returns as float[].
	// More information at: http://paulbourke.net/miscellaneous/correlate
	/***************************************************************/
	private float[] crossCorrelation(ArrayList<float[]> input, ArrayList<float[]> enroll)
	{
		// Finds mean
		float[] mean1 = new float[3];
		float[] mean2 = new float[3];
		for (int i = 0; i < input.size(); i++)
		{
			mean1[XACCEL] += input.get(i)[XACCEL];
			mean1[YACCEL] += input.get(i)[YACCEL];
			mean1[ZACCEL] += input.get(i)[ZACCEL];
			mean2[XACCEL] += enroll.get(i)[XACCEL];
			mean2[YACCEL] += enroll.get(i)[YACCEL];
			mean2[ZACCEL] += enroll.get(i)[ZACCEL];
		}
		
		// Size doesn't matter! Either input or enroll!
		mean1[XACCEL] = mean1[XACCEL]/input.size();
		mean1[YACCEL] = mean1[YACCEL]/input.size();
		mean1[ZACCEL] = mean1[ZACCEL]/input.size();
		mean2[XACCEL] = mean2[XACCEL]/input.size();
		mean2[YACCEL] = mean2[YACCEL]/input.size();
		mean2[ZACCEL] = mean2[ZACCEL]/input.size();
		
		// Finds numerator
		float[] cCorrelation = new float[3];
		for (int i = 0; i < input.size(); i++)
		{
			cCorrelation[0] += (input.get(i)[XACCEL] - mean1[XACCEL]) * 
							   (enroll.get(i)[XACCEL] - mean2[XACCEL]);
			cCorrelation[1] += (input.get(i)[YACCEL] - mean1[YACCEL]) * 
							   (enroll.get(i)[YACCEL] - mean2[YACCEL]);
			cCorrelation[2] += (input.get(i)[ZACCEL] - mean1[ZACCEL]) * 
							   (enroll.get(i)[ZACCEL] - mean2[ZACCEL]);
		}
		
		// Finds stddev
		float[] stdDev1 = new float[3];
		float[] stdDev2 = new float[3];
		for (int i = 0; i < input.size(); i++)
		{
			stdDev1[XACCEL] += (input.get(i)[XACCEL] - mean1[XACCEL]) * (input.get(i)[XACCEL] - mean1[XACCEL]);
			stdDev1[YACCEL] += (input.get(i)[YACCEL] - mean1[YACCEL]) * (input.get(i)[YACCEL] - mean1[YACCEL]);
			stdDev1[ZACCEL] += (input.get(i)[ZACCEL] - mean1[ZACCEL]) * (input.get(i)[ZACCEL] - mean1[ZACCEL]);
			stdDev2[XACCEL] += (enroll.get(i)[XACCEL] - mean2[XACCEL]) * (enroll.get(i)[XACCEL] - mean2[XACCEL]);
			stdDev2[YACCEL] += (enroll.get(i)[YACCEL] - mean2[YACCEL]) * (enroll.get(i)[YACCEL] - mean2[YACCEL]);
			stdDev2[ZACCEL] += (enroll.get(i)[ZACCEL] - mean2[ZACCEL]) * (enroll.get(i)[ZACCEL] - mean2[ZACCEL]);
		}
		stdDev1[XACCEL] = (float) Math.sqrt(stdDev1[XACCEL]);
		stdDev1[YACCEL] = (float) Math.sqrt(stdDev1[YACCEL]);
		stdDev1[ZACCEL] = (float) Math.sqrt(stdDev1[ZACCEL]);
		stdDev2[XACCEL] = (float) Math.sqrt(stdDev2[XACCEL]);
		stdDev2[YACCEL] = (float) Math.sqrt(stdDev2[YACCEL]);
		stdDev2[ZACCEL] = (float) Math.sqrt(stdDev2[ZACCEL]);
		
		// Calculates cross correlation
		cCorrelation[XACCEL] = cCorrelation[XACCEL]/(stdDev1[XACCEL] * stdDev2[XACCEL]);
		cCorrelation[YACCEL] = cCorrelation[YACCEL]/(stdDev1[YACCEL] * stdDev2[YACCEL]);
		cCorrelation[ZACCEL] = cCorrelation[ZACCEL]/(stdDev1[ZACCEL] * stdDev2[ZACCEL]);
		
		return cCorrelation;
	}
	/*
	// For stand-alone testing
	public static  ArrayList<float[]> loadFile(String fileName) {
		File file;
		BufferedReader reader;
		int i = 0;
		String line;
		ArrayList<float[]> data = new ArrayList<float[]>();

		try {
			file = new File(fileName);
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
	
	public static void main(String[] args)
	{
		ArrayList d1 = loadFile(args[0]);
		ArrayList d2 = loadFile(args[1]);
		DataCompare dc = new DataCompare(d1, d2);
		System.out.println("Hamming: " + dc.hamming());
		System.out.println("Hamming Squared: " + dc.hammingSquared());
		System.out.println("Validity: " + dc.valid());
		System.out.println("CCorrelation X: " + dc.crossCorrelation()[0]);
		System.out.println("CCorrelation Y: " + dc.crossCorrelation()[1]);
		System.out.println("CCorrelation Z: " + dc.crossCorrelation()[2]);
		System.out.println();
	}
	*/
}
	




