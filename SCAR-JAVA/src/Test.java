import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.jlinalg.IRingElement;
import org.jlinalg.IRingElementFactory;
import org.jlinalg.Matrix;
import org.jlinalg.Vector;
import org.jlinalg.field_p.FieldPFactoryMap;



public class Test {

	public static ArrayList<Byte> testingarray = new ArrayList<Byte>();
	
	public static void main(String args[]) throws IOException
	{

		String filename = "pic.png";
		//file splitter class takes in a file name/path and k
		//MakeMatrix splitter = new MakeMatrix(filename, 7, 10);
		//gives us back the full matrix to send
	//	Matrix final_Matrix = splitter.fileToByteArray();
		//System.out.println(final_Matrix.toString());

//		long file_size = splitter.getFileSize();
//		System.out.println("FILE_SIZE = " + file_size);
		//store data matrix
		StoreData store;// = new StoreData(final_Matrix, 10, 7);
		//store.storeData("KEY-", "" );
		
		//retrieve data matrix
		RetrieveData retrieve;// = new RetrieveData(10);
		Matrix serverMatrix; // = retrieve.getMatrixfromServer("KEY-", "");
		
		//System.out.println("Server Matrix: " +serverMatrix);
		
		/*
		 * Begin hashed version
		 */
		System.out.println("\n\n$$$$$$$$$$$$$$$$$$$$ HASH $$$$$$$$$$$$$$$$$$$$$$$");

		Hash hash = new Hash();	
		
	//	store = new StoreData(final_Matrix, 10, 7,  filename, file_size);
	//	store.storeHash(filename, "aaa");
		
		retrieve = new RetrieveData(10, 7);
		serverMatrix = retrieve.getMatrixFromServerHashed("save", "save");
		System.out.println("Hashed Matrix: " +serverMatrix);
				
		
		String matrix_string = serverMatrix.toString();
		
		
		
		
		testingarray = new ArrayList();
		for(int i = 0; i < serverMatrix.getRows(); i++){
			VectorToArray(serverMatrix.getRow(i+1).toString());
		}
		
		System.out.println("testingarray" + testingarray.size());
		
		
		String _CONTENT = matrix_string.replaceAll("\\[" , "").replaceAll("\\]", ""); // Getting rid of parenthesis
		String[] _CONTENT_SPLITS  = _CONTENT.split(","); // Splitting the data with commas
		
		byte [] vectorData = new byte[_CONTENT_SPLITS.length]; // This contains the data for the vector

		for(int i = 0; i < _CONTENT_SPLITS.length; i++){
			String[] _M_SPLITS = _CONTENT_SPLITS[i].split("m"); // Splitting between xxmxx

			int _VALUE = (byte)Integer.parseInt(_M_SPLITS[0]);
			vectorData[i] = (byte) _VALUE; // adding the value into vectorData
		}
		
		System.out.println("vector" +vectorData.length);
		/*
		System.out.println("======== BACK TO BYTE ARRAY ========");
		for(int i = 0; i < vectorData.length; i++)
		{
			System.out.print(vectorData[i]);
		}
		
		System.out.println();
		
		*/
		
		//Conveert to image
		long file_size1 = retrieve.getSize();
	//	System.out.println("FILE_SIZE = " + file_size);
		
		/*
		ByteBuffer byteBuffer = ByteBuffer.allocate(vectorData.length *4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(vectorData);
		*/
		
		System.out.println("======VECTORDATA+++++++++");
		for(int i = 0; i < vectorData.length; i++)
		{
			System.out.print(vectorData[i]);
		}
		
		System.out.println("\n======testingarray+++++++++");

		byte [] temp_test = new byte[testingarray.size()-2];
		for(int i = 0; i < testingarray.size()-2; i++)
		{
			temp_test[i] = testingarray.get(i);
			System.out.print(testingarray.get(i));
		}
		
		System.out.println();
		
		
		
		//byte[]array = byteBuffer.array();
		byte [] arr = Arrays.copyOf(vectorData, (int)file_size1);
		System.out.println("Arr.length = "+ arr.length);
		byte[] base64_decode_arr = Base64.decodeBase64(arr);
		System.out.println("DECODED");
		for(byte aByte : base64_decode_arr)
			System.out.print(aByte);
		
		//BufferedImage img = ImageIO.read(new ByteArrayInputStream(base64_decode_arr));
		OutputStream out = new BufferedOutputStream(new FileOutputStream("testfinal.txt"));
		out.write(base64_decode_arr);
		out.close();
		
		arr = Arrays.copyOf(temp_test, (int)file_size1);
		System.out.println("Arr.length = "+ arr.length);
		base64_decode_arr = Base64.decodeBase64(arr);
		System.out.println("DECODED");
		for(byte aByte : base64_decode_arr)
			System.out.print(aByte);
		
		//BufferedImage img = ImageIO.read(new ByteArrayInputStream(base64_decode_arr));
		out = new BufferedOutputStream(new FileOutputStream("imagefina2.png"));
		out.write(base64_decode_arr);
		out.close();
		
		
		
		
		
	}
	
	private static void VectorToArray(String _CONTENT){
		System.out.println("==== in toVector ====");
		/*
		 * Doing string splits because of the data is returned as a string
		 */
		if(_CONTENT == null){
			//No result is found from the server!
		}

		else{
			
			_CONTENT = _CONTENT.replaceAll("\\(" , "").replaceAll("\\(", ""); // Getting rid of parenthesis
			String[] _CONTENT_SPLITS  = _CONTENT.split(", "); // Splitting the data with commas
			//System.out.println(_CONTENT_SPLITS.length); 
			int[] vectorData = new int[_CONTENT_SPLITS.length]; // This contains the data for the vector

			for(int i = 0; i < _CONTENT_SPLITS.length; i++){
				String[] _M_SPLITS = _CONTENT_SPLITS[i].split("m"); // Splitting between xxmxx

				int _VALUE = (byte)Integer.parseInt(_M_SPLITS[0]);
				testingarray.add((byte) _VALUE);
			}
			
		}
	}
			
	
	
}


