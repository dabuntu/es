package com.eventshop.eventshoplinux.servlets;

import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.mongodb.*;

import java.net.UnknownHostException;

/**
 * Created by aravindh on 10/29/15.
 */
public class SpatialDataService {
    public void getSpatialData(String sourceID, long startTime, long endTime, String neLatLong, String swLatLong) throws UnknownHostException {
//        DataCache cache = new DataCache();
//        DataSource ds = cache.registeredDataSources.get(sourceID);
//        long timeInterval=ds.getInitParam().getTimeWindow();
        long timeInterval = 300000;
        long intermediateTime=0;
        Mongo mongoConn = new Mongo("localhost", Integer.parseInt(Config.getProperty("mongoPort")));
        DB mongoDb = mongoConn.getDB("events");
        DBCollection collection = mongoDb.getCollection(sourceID);

        for(long i=startTime;i<endTime;i=i+timeInterval){
            BasicDBObject timeQuery = new BasicDBObject();
            timeQuery.put("number", new BasicDBObject("$gt", i).append("$lt", i+timeInterval));

            DBCursor cursor = collection.find(timeQuery);
            while(cursor.hasNext()) {
                System.out.println(cursor.next());

            }

        }


    }
    public static void main(String args[]){
        SpatialDataService sds = new SpatialDataService();
        try {
            sds.getSpatialData("ds34",1446035884770l,1446118284770l,"","");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
