

import java.util.Iterator;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class MongoDBConnector {

	private static DB m_db = null;

	private static void createInstance() throws Exception {

		if (m_db == null) {
			Mongo mongo = new Mongo("localhost", 27017);
			m_db = mongo.getDB("test");
			//boolean auth = m_db.authenticate(myUserName, myPassword);
		}

	}

	public static void insert(String content, String name) throws Exception {

		if (m_db == null) {
			createInstance();
		}
		DBCollection collection = m_db.getCollection(name);
		m_db.requestStart();
		try {
			m_db.requestEnsureConnection();
			DBObject dbObject = (DBObject) JSON.parse(content);

			// save it into collection named "yourCollection"
			collection.insert(dbObject);

		} finally {
			m_db.requestDone();
		}

	}

	public static void retrieveData(String name) throws Exception {

		DBCursor cursor = null;

		if (m_db == null) {
			createInstance();
		}
		DBCollection collection = m_db.getCollection(name);
		m_db.requestStart();
		try {
			m_db.requestEnsureConnection();
			// query it
			cursor = collection.find();

			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}

		} finally {
			cursor.close();
			m_db.requestDone();
			// collection.drop();
		}

	}

	public static void dropCollection(String name) throws Exception {

		if (m_db == null) {
			createInstance();
		}
		DBCollection collection = m_db.getCollection(name);
		m_db.requestStart();
		try {
			m_db.requestEnsureConnection();

			collection.drop();
			System.out.println("Successfully dropped collection");

		} finally {

			m_db.requestDone();

		}

	}
	
	public static void select()throws Exception{
		if (m_db == null) {
			createInstance();
		}
		
		Iterator itr = m_db.getCollectionNames().iterator();
		
		while(itr.hasNext()){
			System.out.println((String)itr.next());
		}
	}

	public static void main(String[] arg) throws Exception {
		MongoDBConnector db = new MongoDBConnector();
		// db.insert();
	}

}
