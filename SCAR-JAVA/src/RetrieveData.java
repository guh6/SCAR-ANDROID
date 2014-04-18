import java.util.ArrayList;

import org.jlinalg.IRingElement;
import org.jlinalg.IRingElementFactory;
import org.jlinalg.Matrix;
import org.jlinalg.Vector;
import org.jlinalg.field_p.FieldPFactoryMap;


public class RetrieveData 
{
	int f, k;
	long f_size;

	RetrieveData(int f, int k)
	{
		this.f = f;
		this.k = k;
	}

	/**
	 * @description does the hashing algo from Hash.java. key is returned in a list (ArrayList)
	 * @param filename
	 * @param password
	 * @return
	 */
	public Matrix getMatrixFromServerHashed(String filename, String password){
		JedisDriver jstore = new JedisDriver("ra.cs.pitt.edu",8084, "username",	"password");
		MongoDriver mdb = new MongoDriver("ra.cs.pitt.edu",9084, "username","password", "scar_db", "scar_collection");

		String _DATABACK;
		Vector[] _VECTORS = new Vector[k];
		System.out.println("\n=============== Retrieval ===============");

		Hash hash = new Hash(); // Creating a Hashing object
		hash.setArr(); // Call this to initialize the array keys		
		hash.recursiveKey(f, filename, password); // Call this to create the keys
		ArrayList<String> tempKeys = (hash.getArr()); // Call this to retrieve it. Index 0 - 1st key


		int [] arr = new int [k];
		long f_size = 0;
		int row = 0;
		
		for(int i = 0; i < f; i++){
			System.out.println("Key: "+tempKeys.get(i));

			/*
			 * If odd, store redis. Else even store MongoDB
			 */
			
			if(i%2==0){
				_DATABACK = (getRow(tempKeys.get(i),jstore));
				//arr[i] = get header row index
			}
			else{
				_DATABACK = (getRow(tempKeys.get(i),mdb));
				//arr[i] = get header row index
			}

			try{
				System.out.println(_DATABACK);
				row = getRowHeader(_DATABACK);
				arr[i] = row;
				this.f_size = getFileSize(_DATABACK);
				_VECTORS[i] = toVector(_DATABACK);
			}catch(Exception e)
			{
				System.out.println("Skipped");
			}

		}

		/*
		for(int i = 0; i < arr.length; i++)
		{
			System.out.println(arr[i]);
		}
		*/
		
		Matrix serverMatrix = new Matrix(_VECTORS);
		int counter = 0;
		Vector v[] = new Vector[serverMatrix.getRows()];
		//reversing matrix we get back
		for(int i =serverMatrix.getRows() - 1; i >= 0; i--)
		{
			v[counter] = serverMatrix.getRow(i+1);
			
			counter++;
			
		}
	
		serverMatrix = new Matrix(v);
		
		Matrix decoding_Matrix = this.makeDecodingMatrix(this.k, this.f);
		
		//System.out.println(decoding_Matrix);
		
		decoding_Matrix = decode(decoding_Matrix, arr);
		
		//System.out.println(decoding_Matrix);
		
		Matrix finalMatrix = (decoding_Matrix.inverse()).multiply(serverMatrix);
		
		
		return finalMatrix;

	}

	/**
	 * @description returns the data as a string with key and server params
	 * @param key
	 * @param server (Redis)
	 * @return
	 */
	private String getRow(String key, JedisDriver server){
		System.out.println("==== in getRow (Redis) ====");

		String _DATABACK="";

		try{
			_DATABACK =server.getValue(key);
		}catch(Exception e){

		}

		return _DATABACK;
	}

	/**
	 * @description returns the data as a string with key and server params
	 * @param key
	 * @param server (MongoDB)
	 * @return
	 */
	private String getRow(String key, MongoDriver server){
		System.out.println("==== in getRow (MongoDB) ====");

		String _DATABACK="";

		try{
			_DATABACK =server.getValue(key);
		}catch(Exception e){


		}


		return _DATABACK;
	}

	private Vector toVector(String _CONTENT){
		System.out.println("==== in toVector ====");
		String [] header;
		String [] header_1;
		
		/*
		 * Doing string splits because of the data is returned as a string
		 */
		if(_CONTENT == null){
			//No result is found from the server!
			return null;

		}

		else{

			header = _CONTENT.split("\\:\\$\\:"); //uses \\ to escape special characters
			String header_data = header[0]; // gets the header information from the [0] index
			header_1  = header_data.split(" , ");
			/*
			System.out.println(header[1]);
			
			for(String s : header_1)
			{
				System.out.println("header:" + s);
			}
			*/
			_CONTENT = header[1].replaceAll("\\(" , "").replaceAll("\\(", ""); // Getting rid of parenthesis
			String[] _CONTENT_SPLITS  = _CONTENT.split(", "); // Splitting the data with commas
			//System.out.println(_CONTENT_SPLITS.length); 
			int[] vectorData = new int[_CONTENT_SPLITS.length]; // This contains the data for the vector

			for(int i = 0; i < _CONTENT_SPLITS.length; i++){
				String[] _M_SPLITS = _CONTENT_SPLITS[i].split("m"); // Splitting between xxmxx

				int _VALUE = Integer.parseInt(_M_SPLITS[0]);
				vectorData[i] = _VALUE; // adding the value into vectorData
			}
			
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

	
	private int getRowHeader(String _CONTENT)
	{
		System.out.println("==== in getRowHeader ====");
		String [] header;
		String [] header_1;
		
		header = _CONTENT.split("\\:\\$\\:"); //uses \\ to escape special characters
		String header_data = header[0]; // gets the header information from the [0] index
		header_1  = header_data.split(" , ");
			
		System.out.println(header[1]);
		
		/*
		for(String s : header_1)
		{
			System.out.println("header:" + s);
		}
		
		System.out.println("RETURNING: "+ Integer.parseInt(header_1[2]));
		*/	
		return Integer.parseInt(header_1[2]);
	}
	
	
	private long getFileSize(String _CONTENT)
	{
		System.out.println("==== in getfilesize ====");
		String [] header;
		String [] header_1;
		
		header = _CONTENT.split("\\:\\$\\:"); //uses \\ to escape special characters
		String header_data = header[0]; // gets the header information from the [0] index
		header_1  = header_data.split(" , ");
			
		System.out.println(header[1]);
		/*	
		for(String s : header_1)
		{
			System.out.println("header:" + s);
		}
		*/
		System.out.println("RETURNING: "+ Long.parseLong(header_1[5]));
		return Long.parseLong(header_1[5]);
	}

	
	@SuppressWarnings("unchecked")
	public Matrix makeDecodingMatrix(int k, int f)
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
	

	public Matrix decode(Matrix decoding_Matrix, int[] arr)
	{
		System.out.println("======= IN DECODE ======");
		
		Matrix decoded_Matrix = null;
		int counter = 0;
		Vector [] v = new Vector[arr.length]; 
		
		System.out.println("matrixLen = " + decoding_Matrix.getRows());
		
		for(int i = arr.length-1; i >= 0; i--)
		{
			//System.out.println("i: " + i);
			//System.out.println("counter: " + counter);
			v[counter] = decoding_Matrix.getRow(i+1);
			
			//System.out.println(v[counter]);

			counter++;
			
		}

		decoded_Matrix = new Matrix(v);
		
		return decoded_Matrix;
		
	}

	
	
	public long getSize()
	{
		return this.f_size;
	}
	
	
}
