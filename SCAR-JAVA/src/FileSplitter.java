import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import org.jlinalg.IRingElement;
import org.jlinalg.IRingElementFactory;
import org.jlinalg.Matrix;
import org.jlinalg.RingElement;
import org.jlinalg.Vector;
import org.jlinalg.complex.Complex;
import org.jlinalg.field_p.FieldP;
import org.jlinalg.field_p.FieldPFactoryMap;

public class FileSplitter {

	String file_name;
	int k, f;

	FileSplitter(String file_name, int k,  int f)
	{
		this.file_name = file_name;
		this.k = k;
		this.f = f;
	}

	public void fileToByteArray() throws IOException
	{
		File file;
		byte [] byte_arr = null;
		long step, padding;
		long file_size = 0;
		FileInputStream fstream;
		InputStream inputStream = null;

		try {
			//take file name for reading
			file = new File(this.file_name);
			file_size = file.length();
			fstream = new FileInputStream(this.file_name);
			inputStream = fstream;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//System.out.println(file_size);
		step = file_size/k;
		step = step + 1;

		byte[] buffer = new byte[(int) (step*k)];
		int read = inputStream.read(buffer, 0, (int) file_size);

		if(read <= 0)
		{
			System.out.println("ERROR READ");
			System.exit(0);
		}

		padding = (step * k) - file_size;

		for(int i = 0; i < padding; i++)
		{
			buffer[i] = 0;		
		}

		//send byte array to create Data Matrix
		FiniteFilledMatrix(buffer, this.k);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void FiniteFilledMatrix(byte[] byte_arr, int num_rows) 
	{

		//get byte buffer and turn it into Finite Filled matrix		
		IRingElement[] theEntries = new IRingElement[byte_arr.length];
		IRingElementFactory elements = FieldPFactoryMap.getFactory(new Long(257));

		for(int i = 0; i < byte_arr.length; i++)
		{
			theEntries[i] = elements.get((int)byte_arr[i]);
		} 

		//System.out.println(theEntries.toString());
		//System.out.println("byte arr entry = " + byte_arr[0]);
		//System.out.println("entry arr = " +theEntries[0]);

		Matrix data_Matrix = new Matrix(theEntries, num_rows);
		Matrix encoding_Matrix = makeEncodingMatrix(this.k, this.f);		
		Matrix final_Matrix = encoding_Matrix.multiply(data_Matrix);

		System.out.println(final_Matrix.toString());
		
		//store this matrix
		StoreData(final_Matrix, this.f);

	}


	@SuppressWarnings("unchecked")
	private Matrix makeEncodingMatrix(int k, int f)
	{

		//this function makes the encoding matrix

		Vector[] a = new Vector[(f-k)];
		IRingElementFactory elements = FieldPFactoryMap.getFactory(new Long(257));

		for(int i = 0; i < f-k; i++)
		{
			IRingElement[] theEntries = new IRingElement[k];

			for(int j = 0; j < k; j++)
			{
				theEntries[j] = this.power(this.power(elements.get(2), elements.get(f).subtract(elements.get(k)).add(elements.get(i))), elements.get(j));
			}

			a[i] = new Vector(theEntries);

			//System.out.println(a.toString());
		} 

		Vector [] b = new Vector[k];

		for ( int i = 0; i < k; i++)
		{
			IRingElement[] theEntries = new IRingElement[k];

			for ( int j = 0; j < k; j++)
			{
				if( (i == 0) && (j == 0) )
				{
					theEntries[j] = elements.get(1);
				}
				else if ( i == 0 )
				{
					theEntries[j] = elements.get(0);
				}
				else
				{
					theEntries[j] = this.power(this.power(elements.get(2), elements.get(i).subtract(elements.get(1))), elements.get(j));
				}
			}

			b[i] = new Vector(theEntries);
		}

		Matrix A_Matrix = new Matrix(a);
		Matrix B_Matrix = new Matrix(b);
		Matrix C_Matrix = A_Matrix.multiply(B_Matrix.inverse());

		Vector[] d = new Vector[f];

		for(int i = 0; i < k; i++)
		{
			IRingElement[] theEntries = new IRingElement[k];

			for( int j = 0; j < k; j++)
			{
				if(i == j)
				{
					theEntries[j] = elements.get(1);
				}
				else
				{
					theEntries[j] = elements.get(0);
				}
			}

			d[i] = new Vector(theEntries);
		}

		for(int i = k; i < f; i++)
		{
			IRingElement[] theEntries = new IRingElement[k];

			for(int j = 0; j < k; j++)
			{
				theEntries[j] = C_Matrix.get((i-k)+1, (j + 1));
			}

			d[i] = new Vector(theEntries);
		}

		Matrix D_Matrix = new Matrix(d);

		//D matrix is the encoded matrix
		return D_Matrix;
	}


	@SuppressWarnings("unchecked")
	private IRingElement power(IRingElement x, IRingElement n)
	{
		IRingElementFactory elements = FieldPFactoryMap.getFactory(new Long(257));

		IRingElement return_elem = x;

		if(n.compareTo(elements.get(0)) == 0)
		{
			return elements.get(1);
		}
		for(int i = 1; n.compareTo(elements.get(i)) > 0; i++)
		{
			return_elem = return_elem.multiply(x);
		}

		return return_elem;
	}


	/**
	 * @description takes in a matrix that is stored into the server using f splits
	 * @param final_Matrix
	 * @param f
	 */	
	private void StoreData( Matrix final_Matrix, int f){
		System.out.println("===== In StoreData =====");		
		System.out.println("storing data with rows: "+final_Matrix.getRows());
		 
		/*
		 * Establishing server connection
		 */
		JedisDriver jstore = new JedisDriver("ra.cs.pitt.edu",8084, "username",	"password");
		System.out.println(jstore); // Checking if connection is good. 

		String _KEY = "KEY-";
		String _DATA = "";
		/*
		 * Storing data into server with _KEY, _DATA
		 */
		System.out.println("\n=============== Storage ===============");
		System.out.println("How many rows in matrix? " +final_Matrix.getRows());
		for(int i = 0; i < f ; i++ ){	
			_DATA = final_Matrix.getRow(i+1).toString();
			storeRow(_KEY + (i+1) +"", _DATA , jstore ); // parameters - key, value, server			
		}


		System.out.println("\n=============== Retrieval ===============");

		/*
		 * Retrieving the data from server
		 */
		String _DATABACK;
		Vector[] _VECTORS = new Vector[f];
		for(int i = 0; i < f ; i++ ){	
			_DATABACK = (getRow(_KEY + (i+1) +"",jstore)); // Raw data back from the server. Now I want to convert the String into a matrix?
			//System.out.println(_DATABACK);
			_VECTORS[i] = toVector(_DATABACK);
			//System.out.println("Constructed vector: "+toVector(_DATABACK));	
		}

		/*
		 * Now Constructing the Matrix from the Vector[]
		 */

		Matrix<?> serverMatrix = new Matrix(_VECTORS);

		System.out.println("server Matrix: " +serverMatrix);


	}
	/**
	 * @description stores the key and value into the server. This is String, String based. Can be byte[] , byte[]. 
	 * @param key
	 * @param value
	 * @param server
	 */
	private void storeRow(String key, String value, JedisDriver server){
		System.out.println("==== in storeRow ====");
		try {
			server.store(key, value );
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * @description returns the data as a string with key and server params
	 * @param key
	 * @param server
	 * @return
	 */
	private String getRow(String key, JedisDriver server){
		System.out.println("==== in getRow ====");

		String _DATABACK="";

		try{
			_DATABACK =server.getValue(key);
		}catch(Exception e){


		}


		return _DATABACK;
	}

	private Vector toVector(String _CONTENT){
		System.out.println("==== in toVector ====");

		/*
		 * Doing string splits because of the data is returned as a string
		 */
		if(_CONTENT == null){
			//No result is found from the server!
			return null;
			
		}
		
		else{
		_CONTENT = _CONTENT.replaceAll("\\(" , "").replaceAll("\\(", ""); // Getting rid of parenthesis
		String[] _CONTENT_SPLITS  = _CONTENT.split(", "); // Splitting the data with commas
		//System.out.println(_CONTENT_SPLITS.length); 
		int[] vectorData = new int[_CONTENT_SPLITS.length]; // This contains the data for the vector

		for(int i = 0; i < _CONTENT_SPLITS.length; i++){
			String[] _M_SPLITS = _CONTENT_SPLITS[i].split("m"); // Splitting between xxmxx

			int _VALUE = Integer.parseInt(_M_SPLITS[0]);
			vectorData[i] = _VALUE; // adding the value into vectorData

		}
		/*
		for( int num : vectorData)
			System.out.print(num +" "); 
		 */

		/*
		 * Now doing the Vector conversion
		 */
		IRingElement[] theEntries = new IRingElement[vectorData.length];
		IRingElementFactory elements = FieldPFactoryMap.getFactory(new Long(257));

		for(int i = 0; i < vectorData.length; i++)
		{
			theEntries[i] = elements.get((int)vectorData[i]);

		} 

		Vector <?> vector = new Vector(theEntries);

		return vector;
		}
	}




}
