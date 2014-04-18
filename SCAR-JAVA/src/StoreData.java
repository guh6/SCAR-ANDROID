import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.CRC32;

import org.jlinalg.IRingElement;
import org.jlinalg.IRingElementFactory;
import org.jlinalg.Matrix;
import org.jlinalg.Vector;
import org.jlinalg.field_p.FieldPFactoryMap;


public class StoreData {

	Matrix matrix;
	int f, k;
	String file_name;
	long file_size;
	
	StoreData(Matrix M, int f, int k, String filename, long file_size)
	{
		this.matrix = M;
		this.f = f;
		this.k = k;
		this.file_name = filename;
		this.file_size = file_size;

	}



	/**
	 * @description algo is: input(filename, password) 1st key: hash(filename + password). 2nd key: hash(f1 + filename+password),..
	 * @param filename
	 * @param password
	 */
	public void storeHash(String filename, String password){
		
		Matrix final_Matrix = this.matrix;
		JedisDriver jstore = new JedisDriver("ra.cs.pitt.edu",8084, "username",	"password");
		MongoDriver mdb = new MongoDriver("ra.cs.pitt.edu",9084, "username","password", "scar_db", "scar_collection");

		System.out.println("\n=============== Storage (HASH) ===============");
		System.out.println("How many rows in matrix? " +final_Matrix.getRows());

		/*
		 * Doing this to "f" times. 
		 */
		System.out.println("[Password]: " +password +" | "+"[filename]: " +filename);

		Hash hash = new Hash(); // Creating a Hashing object
		hash.setArr(); // Call this to initialize the array keys		
		hash.recursiveKey(f, filename, password); // Call this to create the keys
		ArrayList<String> tempKeys = (hash.getArr()); // Call this to retrieve it. Index 0 - 1st key

		for(int i = 0; i < this.f; i++){
			
			String header = makeHeader(this.k, this.f, i, this.file_name, this.file_size);

			String _DATA = final_Matrix.getRow(i+1).toString();

			if(i%2==0){
				System.out.println("even: "+i);
				storeRow(tempKeys.get(i), header + ":$:" + _DATA  , jstore );
			}
			else{
				System.out.println("odd: "+i);
				storeRow(tempKeys.get(i), header + ":$:" + _DATA , mdb );
			}

			//storeRow(tempKeys.get(i), _DATA , jstore ); Jedis
			//storeRow(tempKeys.get(i), _DATA , mdb ); // testing with mongo
		}

	}


	/**
	 * @description takes in a matrix that is stored into the server using f splits
	 * @param final_Matrix
	 * @param f
	 */	
	public void storeData(String _KEY, String _DATA)
	{

		Matrix final_Matrix = this.matrix;
		int f = this.f;

		System.out.println("===== In StoreData (not hashed) =====");		
		System.out.println("storing data with rows: "+final_Matrix.getRows());

		/*
		 * Establishing server connection
		 */
		JedisDriver jstore = new JedisDriver("ra.cs.pitt.edu",8084, "username",	"password");
		MongoDriver mdb = new MongoDriver("ra.cs.pitt.edu",9084, "username","password", "scar_db", "scar_collection");
		System.out.println(jstore); // Checking if connection is good. 

		/*
		 * Storing data into server with _KEY, _DATA
		 */
		System.out.println("\n=============== Storage ===============");
		System.out.println("How many rows in matrix? " +final_Matrix.getRows());
		for(int i = 0; i < f ; i++ ){	

			String header = makeHeader(this.k, this.f, i, this.file_name , this.file_size);
		//	System.out.println(this.k + " " + this.f + " " + i);

			_DATA = header + ":$:" + final_Matrix.getRow(i+1).toString();
			storeRow(_KEY + (i+1) +"", _DATA , jstore ); // parameters - key, value, server			

		}

	}


	/**
	 * @description stores the key and value into the server. This is String, String based. Can be byte[] , byte[]. 
	 * @param key
	 * @param value
	 * @param server
	 */
	private void storeRow(String key, String value, JedisDriver server){
		System.out.println("==== in storeRow (Redis)====");
		try {
			server.store(key, value );
		} catch (Exception e) {

			e.printStackTrace();
		}

	}


	/**
	 * @description stores the key and value into the server. This is String, String based. Can be byte[] , byte[]. 
	 * @param key
	 * @param value
	 * @param server
	 */
	private void storeRow(String key, String value, MongoDriver server){
		System.out.println("==== in storeRow (MongoDB) ====");
		try {
			server.store(key, value );
		} catch (Exception e) {

			e.printStackTrace();
		}

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


	public long calculateChecksum(byte [] b)
	{
		CRC32 checksum = new CRC32();
		checksum.update(b);	
		return checksum.getValue();
	}

	public long getunitSignature()
	{
		return 0;
	}


	public String makeHeader(int k, int f, int row, String file_name, long file_size)
	{

		String k_str = k + "";
		String f_str = f + "";
		String row_str = row + "";
		String CS = "12345";
		String f_name = file_name;
		String f_size = file_size + "";

		String header = k_str+" , "+ f_str + " , " + row_str + " , " + CS + " , " + file_name + " , " + f_size;

		return header;
	}


}
