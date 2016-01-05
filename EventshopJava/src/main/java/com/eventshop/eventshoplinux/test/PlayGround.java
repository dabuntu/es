package com.eventshop.eventshoplinux.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayGround {

	public static void main(String[] args) {
		try {
			// DB db = CommonUtil.connectMongoDB();

			// System.out.println(Config.getProperty("mongoHost") + ":" +
			// Config.getProperty("mongoDB"));
			// MongoClient mongo = new
			// MongoClient(Config.getProperty("mongoHost"));
			// DB db = mongo.getDB(Config.getProperty("mongoDB"));
			// DBCollection collection = db.getCollection("ds111");
			// // convert JSON to DBObject directly
			//
			// DBObject dbObject = (DBObject) JSON.parse("{\"test\":\"1234\"}");
			// collection.insert(dbObject);

			// /**** Insert ****/
			// // create a document to store key and value
			// BasicDBObject document = new BasicDBObject();
			// document.put("name", "mkyong");
			// document.put("age", 30);
			// document.put("createdDate", new Date());
			// collection.insert(document);
			//
			// /**** Find and display ****/
			// BasicDBObject searchQuery = new BasicDBObject();
			// searchQuery.put("name", "mkyong");
			//
			// DBCursor cursor = collection.find(searchQuery);
			//
			// while (cursor.hasNext()) {
			// System.out.println(cursor.next());
			// }
			//
			System.out.println("done");
		} catch (Exception e) {
			System.out.println("error");
		}
		System.exit(0);

		double[] data = { 1, 1, 1, 2, 2, 2, 3, 3, 5, 5 };
		double pop = data[0];
		int count = 1;
		Arrays.sort(data);

		for (int i = 1; i < data.length; i++) {
			if (data[i] == pop) {
				count++;
			} else {
				if (i + count < data.length && data[i] == data[i + count]) {
					pop = data[i];
					count++;
					i = i + count;
				}
			}
		}
		PlayGround.print("test " + pop + ", " + count);

		List<Double>[] dataList = new List[4];
		dataList[0] = new ArrayList<Double>();
		dataList[0].add(1.0);
		dataList[0].add(2.0);
		dataList[0].add(2.0);
		dataList[0].add(2.0);

		dataList[1] = new ArrayList<Double>();
		dataList[1].add(1.0);
		dataList[1].add(1.0);
		dataList[1].add(3.0);
		dataList[1].add(3.0);
		dataList[1].add(3.0);
		dataList[1].add(4.0);

		dataList[2] = new ArrayList<Double>();
		dataList[2].add(0.0);
		dataList[2].add(1.0);
		dataList[2].add(5.0);
		dataList[2].add(5.0);

		dataList[3] = new ArrayList<Double>();

		for (int k = 0; k < dataList.length; k++) {
			double p = getMostFreq((ArrayList<Double>) dataList[k]);
			PlayGround.print("test pop [" + k + "] " + p);
		}
		for (int k = 0; k < dataList.length; k++) {
			double p = getMajority(dataList[k]);
			PlayGround.print("test major [" + k + "] " + p);
		}

		PlayGround.print("test2 " + pop + ", " + count);

		List<Double> list = new ArrayList<Double>();
		list.add(1.0);
		list.add(5.0);
		list.add(2.0);
		list.add(3.0);
		list.add(4.0);
		list.add(4.0);
		list.add(5.0);
		list.add(4.0);
		list.add(4.0);
		list.add(1.0);
		list.add(2.0);
		Collections.sort(list);

		dataList[0] = list;
		dataList[0] = list;
		dataList[0] = list;

		pop = list.get(0);
		count = 1;
		for (int i = 1; i < list.size(); i++) {
			PlayGround.print("list[" + i + "] " + list.get(i));
			if (list.get(i) == pop) {
				count++;
			} else {
				// PlayGround.print("p:" + pop + ", c:" + count + ", i:" + i);
				if (i + count < list.size()
						&& list.get(i).equals(list.get(i + count))) {
					pop = list.get(i);
					i = i + count;
					count++;
				}
			}
		}
		PlayGround.print("test2 " + pop + ", " + count);

	}

	public static double getMajority(List<Double> list) {
		double pop = 0;
		if (!list.isEmpty()) {
			pop = list.get(0);
			int count = 1;
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i) == pop) {
					count++;
					if (count > list.size() / 2)
						break;
				} else {
					if (i + count < list.size()
							&& list.get(i).equals(list.get(i + count))) {
						pop = list.get(i);
						i = i + count;
						count++;
					}
				}
			}
			if (count <= list.size() / 2)
				pop = 0;
		}
		return pop;
	}

	public static double getMostFreq(ArrayList<Double> list) {
		double pop = 0;
		if (!list.isEmpty()) {
			pop = list.get(0);
			int count = 1;
			for (int i = 1; i < list.size(); i++) {
				// PlayGround.print("list["+i+"] "+list.get(i));
				if (list.get(i) == pop) {
					count++;
				} else {
					// PlayGround.print("p:" + pop + ", c:" + count + ", i:" +
					// i);
					if (i + count < list.size()
							&& list.get(i).equals(list.get(i + count))) {
						pop = list.get(i);
						i = i + count;
						count++;
					}
				}
			}
		}
		return pop;
	}

	public static void print(String txt) {
		System.out.println(txt);
	}
}
