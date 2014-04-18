

import java.util.ArrayList;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

/*
 * 	Driver class for Redis server storage functions.
 * 	-To store an item, create a Jedis_store(hostname, username, password) object.
 * 	-Then, setHostname() follow by setConnectionObj() -> those two are optional if you're using the same DB when you initialize the server
 * 	-Then, use the store_image or store_primitive for put.
 *  -Or, use getValue() for get. getValue() returns a byte[]. 
 */
public class JedisDriver {

	private String hostname;
	private String username;
	private String password;
	private Jedis connection_obj;
	boolean DEBUG = true;
	private int port;

	/**
	 * 
	 * @param parm_hostname
	 * @param port 
	 * @param parm_username
	 * @param parm_password
	 * @description constructor for Jedis object. input the above. e.g.
	 *              parm_hostname = "thot.cs.pitt.edu" or "10.0.2.2"
	 */
	public JedisDriver(String parm_hostname, int parm_port, String parm_username,
			String parm_password) {

		this.hostname = parm_hostname;
		this.username = parm_username;
		this.password = parm_password;
		this.port = parm_port;
		setNewConnection();
	}

	/**
	 * 
	 * @param key
	 *            - byte[] array. Convert string to byte[], then store
	 * @param value
	 * @description stores key-value pair
	 */
	public void store(byte[] key, byte[] value) {

		if (DEBUG)
			System.out.println("Storing: " + key + "\t" + value);

		try {
			this.connection_obj.set(key, value);
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param key
	 *            - byte[] array. Convert string to byte[], then store
	 * @param value
	 * @description stores key-value pair
	 */
	public void store(String key, String value) {

		if (DEBUG)
			System.out.println("Storing: " + key + "\t" + value);

		try {
			this.connection_obj.set(key, value);
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param key
	 * @return byte[]
	 * @description returns the value (if exists) of the key. Convert byte[] to
	 *              bitmap for image or byte[] to string for raw output
	 */
	public byte[] getValue(byte[] key) {

		if(key == null){
			System.out.println("Found nothing");
			
		}else{
			System.out.println("Found");
		}
		return connection_obj.get(key);

	}

	
	/**
	 * 
	 * @param key
	 * @return String
	 * @description returns the value (if exists) of the key. Convert byte[] to
	 *              bitmap for image or byte[] to string for raw output
	 */
	public String getValue(String key) {

		if(key == null){
			System.out.println("Found nothing");
			
		}else{
			System.out.println("Found");
		}
		return connection_obj.get(key);

	}
	/**
	 * 
	 * @param key
	 * @param image
	 *            : byte[]
	 * @description: stores an image specified by the image byte[] data
	 */
	public void storeImage(byte[] key, byte[] image) {
		if (DEBUG)
			System.out.println("Storing image: " + key);

		try {
			this.connection_obj.set(key, image);
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param keys
	 * @param values
	 * @description input in an array list of byte[] key value pairs in pipeline
	 *              fashion
	 */
	public void pipelineStore(ArrayList<byte[]> keys, ArrayList<byte[]> values) {

		if (DEBUG)
			System.out.println("In pipe_line");

		//Starting pipeline connection
		Pipeline p = connection_obj.pipelined();

		//Storing the key-value pairs
		for (int i = 0; i < 3; i++) {
			p.set(keys.get(i), values.get(i));

		}

		//Sync, close. 
		p.sync();

		if (DEBUG)
			System.out.println("Done with pipe_line");

	}

	// Setters
	public void setHostname(String parm_hostname) {
		this.hostname = parm_hostname;
	}

	public void setUsername(String parm_username) {
		this.username = parm_username;
	}

	public void setPassword(String parm_password) {
		this.password = parm_password;
	}
	public void setPort(int port){
		this.port = port;
	}

	/**
	 * description: reset the connection object with a new hostname. Must use
	 * setHostname prior.
	 */
	public void setNewConnection() {
		if (DEBUG)
			System.out.println("Trying to connect to : " + this.hostname);
		try {
			this.connection_obj = new Jedis(this.hostname,this.port);
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * @param value
	 * @description: for debugging purposes
	 */
	public void setDebug(boolean value) {
		this.DEBUG = value;
	}

	/**
	 * toString
	 */
	public String toString() {

		String result = "";

		result = "Server information:\n" + "Hostname: " + this.hostname
				+"\nPort: " +this.port 	+ "\nUsername: " + this.username + "\nPassword: "
				+ this.password;

		return result;
	}
	
	

}
