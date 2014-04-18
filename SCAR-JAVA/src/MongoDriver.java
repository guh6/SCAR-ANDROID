

import java.util.ArrayList;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDriver {

	private String hostname;
	private String username;
	private String password;
	private String database_name; 
	private String collection_name;
	private DB db;
	private int port;
	private Mongo mongoClient;
	DBCollection collection;

	
	/**
	 * 
	 * @param parm_hostname - hostname
	 * @param parm_port - port 
	 * @param parm_username - username
	 * @param parm_password - password
	 * @param parm_db - database name
	 * @param parm_collection - collection name
	 * @example MongoDriver mdb = new MongoDriver("thot.cs.pitt.edu","Tom","Smith","scar_db","scar_collec");
	 */
	public MongoDriver(String parm_hostname, int parm_port, String parm_username,
			String parm_password, String parm_db, String parm_collection) {

		this.hostname = parm_hostname;
		this.username = parm_username;
		this.password = parm_password;
		this.database_name = parm_db;
		this.collection_name = parm_collection;
		this.port = parm_port;

		setNewConnection();
	}

	/**
	 * 
	 * @param key
	 * @return String
	 */
	public String getValue(String key) {

		String value = "";

		DBObject query = new BasicDBObject(key, new BasicDBObject("$exists",
				true));
		DBCursor result = this.collection.find(query);
		if (result.iterator().hasNext()) { // If something is returned

			System.out.println("Found: ");
			value = result.iterator().next().get(key).toString();
			

		}
		else{
			System.out.println("Found nothing");
		}

		return value;

	}
	
	
	public String getValue64(String key) {

		String value = "";

		DBObject query = new BasicDBObject(key, new BasicDBObject("$exists",
				true));
		DBCursor result = this.collection.find(query);
		if (result.iterator().hasNext()) { // If something is returned

			System.out.println("Found: ");
			value = result.iterator().next().get(key).toString();
			
			
		}
		else{
			System.out.println("Found nothing");
		}

		return value;

	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @description stores primitive key value pairs
	 */
	public void store(String key, String value) {

		try {
			BasicDBObject doc = new BasicDBObject(key, value);
			// Now put it into the set.
			this.collection.insert(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @param keys - array of keys
	 * @param values - array of values
	 */
	public void pipelineStore(String[] keys, ArrayList<byte[]> values) {

		
		
	}
	/**
	 * 
	 * @param key
	 * @param image
	 * @description converts the byte[] into a base64 string for storage
	 */
	/*
	public void storeImage(String key, byte[] image) {

		
		String base64 = Base64.encodeToString(image, Base64.NO_PADDING);
		BasicDBObject doc = new BasicDBObject("type", "image").append(
				key, base64);
		this.collection.insert(doc);
		
		
	}
	 */
	// Setters
	public void setCollection() {
		this.collection = this.db.getCollection(this.collection_name);
	}

	public void setHostname(String parm_hostname) {
		this.hostname = parm_hostname;
	}

	public void setUsername(String parm_username) {
		this.username = parm_username;
	}

	public void setPassword(String parm_password) {
		this.password = parm_password;
	}

	public void setCollectionName(String parm_coll) {
		this.collection_name = parm_coll;
	}

	public void setDatabase(String parm_db) {
		this.database_name = parm_db;
	}
	public void setPort(int parm_port){
		this.port = parm_port;
	}
	
	public void setNewConnection(){
		try {
			this.mongoClient = new Mongo(this.hostname,this.port);
			this.db = mongoClient.getDB(this.database_name);
			this.collection = db.getCollection(this.collection_name);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void setDB(){
		this.db = mongoClient.getDB(this.database_name);
		
	}
	// toString
	public String toString() {

		String result = "";

		result = "Server information:\n" + "Hostname: " + this.hostname
				+"\nPort: " +this.port 				
				+ "\nUsername: " + this.username + "\nPassword: "
				+ this.password + "\nCollection: " + this.collection_name
				+ "\nDB_name: " + this.db;

		return result;
	}

}
