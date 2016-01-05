package com.eventshop.eventshoplinux.service;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class MongoDB {
	public SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	public static String DBHOST = "emme.ics.uci.edu";
	public static int DBPORT = 27017;
	public DB db;
	public DBCollection collection;

	public MongoDB(String dbHost, int dbPort, String dbName) {
		// Connect to mongodb
		MongoClient mongo;
		try {
			mongo = new MongoClient(dbHost, dbPort);
			// get database, if database doesn't exists, mongodb will create it
			// for you
			db = mongo.getDB(dbName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public MongoDB(String dbName) {
		// Connect to mongodb
		MongoClient mongo;
		try {
			mongo = new MongoClient(DBHOST, DBPORT);
			// get database, if database doesn't exists, mongodb will create it
			// for you
			db = mongo.getDB(dbName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getCollection() {
		Set<String> collections = db.getCollectionNames();
		// JsonArray col = new JsonArray();
		for (String collectionName : collections) {
			// col.add(new JsonPrimitive(collectionName));
			System.out.println(collectionName);
		}
		return collections;
	}

	public void setCollection(String colName) {
		collection = db.getCollection(colName);
	}

	// public JsonObject findOneJson(){
	// DBObject dbObject = collection.findOne();
	// JsonObject result = new JsonObject();
	// String s = String.format("%s",dbObject);
	// System.out.println(s);
	// result = (JsonObject) JSON.parse(s); // ERROR: com.mongodb.BasicDBObject
	// cannot be cast to com.google.gson.JsonObject
	// return result;
	// }
	//
	public DBObject findOne() {
		return collection.findOne();
	}

	public List<DBObject> find(BasicDBObject query, BasicDBObject fields) {
		DBCursor cursor2 = collection.find(query, fields);
		List<DBObject> result = new ArrayList<DBObject>();
		int count = 0;
		while (cursor2.hasNext()) {
			DBObject temp = cursor2.next();
			System.out.println(temp);
			result.add(temp);
			count++;
		}
		if (count != 0)
			System.out.println("total found: " + count);
		return result;
	}

	public List<DBObject> find(String qStr, String fieldStr) {
		BasicDBObject query = (BasicDBObject) JSON.parse(qStr);
		BasicDBObject fields = new BasicDBObject();
		String[] fList = fieldStr.split(",");
		for (String f : fList) {
			fields.put(f, 1);
		}
		DBCursor cursor2 = collection.find(query, fields);
		List<DBObject> result = new ArrayList<DBObject>();
		int count = 0;
		while (cursor2.hasNext()) {
			DBObject temp = cursor2.next();
			// System.out.println(temp);
			result.add(temp);
			count++;
		}
		if (count != 0)
			System.out.println("total found: " + count);
		return result;
	}

	public List<DBObject> find(String qStr) {
		BasicDBObject query = (BasicDBObject) JSON.parse(qStr);
		DBCursor cursor2 = collection.find(query);
		List<DBObject> result = new ArrayList<DBObject>();
		int count = 0;
		while (cursor2.hasNext()) {
			DBObject temp = cursor2.next();
			// System.out.println(temp);
			result.add(temp);
			count++;
		}
		if (count != 0)
			System.out.println("total found: " + count);
		return result;
	}

	public static void main(String[] args) {
		String dbName = "evimdb2";
		String colName = "PM2_5_daily";
		MongoDB myDB = new MongoDB(dbName);
		myDB.setCollection(colName);
		BasicDBObject dateQuery = new BasicDBObject();
		dateQuery.put("date", new BasicDBObject("$gt", "2013-08-05").append(
				"$lt", "2013-08-10"));
		// BasicDBObject latQuery = new BasicDBObject();
		// latQuery.put("latitude", new BasicDBObject("$gt", 24).append("$lt",
		// 40));
		// BasicDBObject lngQuery = new BasicDBObject();
		// lngQuery.put("longitude", new BasicDBObject("$gt",
		// -125).append("$lt", -66));
		//

		Double radious = 10.0 / 3959.0;
		// BasicDBObject geoQuery = (BasicDBObject)
		// JSON.parse("{'loc':{$geoWithin:{$centerSphere:[[-120.680278,38.201944],"+radious+"]}}}");
		BasicDBObject geoQuery = (BasicDBObject) JSON
				.parse("{'loc':{$geoWithin:{$centerSphere:[[-120.6,38.2],"
						+ radious + "]}}}");

		BasicDBObject andQuery = new BasicDBObject();
		List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		obj.add(dateQuery);
		// obj.add(latQuery);
		// obj.add(lngQuery);
		obj.add(geoQuery);
		andQuery.put("$and", obj);
		System.out.println(andQuery);

		BasicDBObject fields = new BasicDBObject();
		fields.put("loc", 1);
		fields.put("date", 1);
		fields.put("value", 1);
		myDB.find(andQuery, fields);

		String qstr = "{$and:[{date:{$gt:'2013-08-05',$lt:'2013-08-10'}},{'loc':{$geoWithin:{$centerSphere:[[-120.6,38.2],"
				+ radious + "]}}}]}";
		// BasicDBObject query = (BasicDBObject) JSON.parse(qstr);
		String fStr = "loc,date,value";
		List<DBObject> result = myDB.find(qstr, fStr);

		for (DBObject r : result) {
			BasicDBList loc = ((BasicDBList) ((DBObject) r.get("loc"))
					.get("coordinates"));
			System.out.println(r.get("date") + "," + r.get("value") + ","
					+ loc.get(0) + "," + loc.get(1));
		}
	}
}
