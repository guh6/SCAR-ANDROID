import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;


public class Hash {

	private ArrayList<String>keys;
	
	public Hash(){
		
	}
	
	/**
	 * @description returns the hashed verison of the string in Hex String form
	 * @param content
	 * @return
	 */
	public String getHashKey(String content){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(content.getBytes("UTF-8"));
			
			return new String(Hex.encodeHex(DigestUtils.md5((hash))));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Hashing failed");
			return "";
		}
	}
	
	/**
	 * @description returns the last known value of the recurisve thing. use getArr to the a list. 
	 * @param n - "f"
	 * @param key - filename
	 * @param password - password
	 * @return
	 */
	public String recursiveKey(int n, String key, String password){
		
		if ( n== 0) return "";
		else if( n== 1) {
			String newKey = getHashKey(key+password);
			//System.out.println("first is: " +newKey);
			this.keys.add(newKey);
			return newKey;
		}
		else{
			String newKey = getHashKey( recursiveKey(n-1, key, password) + key + password);
			this.keys.add(newKey);
			return newKey;
		}
	}
	
	/**
	 * initliaze the arraylist
	 */
	public void setArr(){
		this.keys = new ArrayList();
	}
	
	/**
	 * returns the keys
	 * @return
	 */
	public ArrayList<String> getArr(){
		return this.keys;
	}
}
