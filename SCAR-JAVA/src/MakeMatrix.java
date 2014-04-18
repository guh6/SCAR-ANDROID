import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.jlinalg.IRingElement;
import org.jlinalg.IRingElementFactory;
import org.jlinalg.Matrix;
import org.jlinalg.RingElement;
import org.jlinalg.Vector;
import org.jlinalg.complex.Complex;
import org.jlinalg.field_p.FieldP;
import org.jlinalg.field_p.FieldPFactoryMap;

public class MakeMatrix {

	String file_name;
	int k, f;
	long file_size;

	MakeMatrix(String file_name, int k,  int f)
	{
		this.file_name = file_name;
		this.k = k;
		this.f = f;
	}

		
	public Matrix fileToByteArray() throws IOException
	{
		System.out.println("======== in fileToByteArray() ========");
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
		
		byte[] buffer = new byte[(int) file_size];
		int read = inputStream.read(buffer, 0, (int) file_size);

		if(read <= 0)
		{
			System.out.println("ERROR READ");
			System.exit(0);
		}

		byte [] base64_buffer = Base64.encodeBase64(buffer);
		System.out.println("base64_buffer:");
		for(Byte aByte : base64_buffer)
			System.out.print(aByte);
		byte[] base64_decode_arr = Base64.decodeBase64(base64_buffer);
		System.out.println("\nbase64_decode_arr:");
		for(Byte aByte : base64_decode_arr)
			System.out.print(aByte);
		System.out.println();


		//System.out.println(file_size);
		step = (base64_buffer.length)/k;
		step = step + 1;
		
		padding = (step * k) - base64_buffer.length;

		byte [] buffer_64 = Arrays.copyOf(base64_buffer, (int) (base64_buffer.length + padding));

		this.file_size = base64_buffer.length;
		
		for(int i = 0; i < padding; i++)
		{
			buffer_64[base64_buffer.length + i] = 0;		
		}
		
		/*
		for(int i = 0; i < buffer.length; i++)
		{
			System.out.print(buffer[i]);
		}*/
				
		//send byte array to create Data Matrix
		return FiniteFilledMatrix(buffer_64, this.k);
	}

	
	public long getFileSize()
	{
		return this.file_size;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Matrix FiniteFilledMatrix(byte[] byte_arr, int num_rows) 
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
		//StoreData(final_Matrix, this.f);
		
		return final_Matrix;

	}


	@SuppressWarnings("unchecked")
	public Matrix makeEncodingMatrix(int k, int f)
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

}
