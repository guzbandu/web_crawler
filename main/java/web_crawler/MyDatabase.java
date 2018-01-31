package web_crawler;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class MyDatabase {
	private static MyDatabase instance;	
    private static MongoClient dbClient;
    private static MongoDatabase database;
    private static String db_url = "localhost";
    private static Integer db_port = 27017;
    private static String db_name = "crawler";
    
	public MyDatabase() {
		dbClient = new MongoClient(db_url, db_port);
		database = dbClient.getDatabase(db_name);		
	}
	
	public synchronized MongoCollection<Document> getCollection(String collectionName) {
		MongoCollection<Document> exists = database.getCollection(collectionName);
		if(exists==null) {
			database.createCollection(collectionName, null);
		}
		return database.getCollection(collectionName);
	}
	
	public synchronized boolean addToCollection(String collectionName, Document document) {
		if(getCollection(collectionName).find(document)==null) {
			getCollection(collectionName).insertOne(document);
			return true;
		}
		return false;
	}
	
	public synchronized void updateInCollection (String collectionName, Document documentToFind, Document documentWithUpdates) {
		getCollection(collectionName).replaceOne(documentToFind, documentWithUpdates, new UpdateOptions().upsert( true ));
	}
	
	public static void setInstance(MyDatabase instance) {
		MyDatabase.instance = instance;
	}
	
	public static MyDatabase getInstance() {
		if (instance == null)
			instance = new MyDatabase();
		return instance;
	}
	
}
