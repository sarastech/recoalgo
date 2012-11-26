
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.Arrays;


public class TestMongo {

	public static void main(String args[]) throws Exception{
		Mongo m = new Mongo( "localhost" , 27017 );
		DB db = m.getDB( "pingpong" );
		DBCollection coll = db.getCollection("rawDataDump");
		
		BasicDBObject query = new BasicDBObject();
		query.put("type","friendlists");
		DBCursor cursor = coll.find(query);
		
		System.out.println(cursor.next());
	}
}
