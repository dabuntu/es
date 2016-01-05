package com.eventshop.eventshoplinux.Misc;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by aravindh on 9/16/15.
 */
public class TweetCount {
    private static final Logger logger = LoggerFactory.getLogger(TweetCount.class);
    public static void main(String args[]){
    try{
        HashMap<String, Integer>  wordsNCount = new HashMap<String, Integer>();
        Mongo mongoConn = new Mongo(Config.getProperty("mongoHost"), Integer.parseInt(Config.getProperty("mongoPort")));
        DB mongoDb = mongoConn.getDB("events");
        DBCollection collection = mongoDb.getCollection("ds37");

        BasicDBObject allQuery = new BasicDBObject();
        BasicDBObject fields = new BasicDBObject();
        fields.put("rawData", 1);
        fields.put("_id",0);

        DBCursor cursor = collection.find(allQuery, fields);

        File fout = new File("/home/aravindh/tweets.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));




        while (cursor.hasNext()) {
         //   Json
            String tweet=cursor.next().toString();
            tweet=tweet.substring(15,tweet.length()-2);
            logger.debug(tweet);
            bw.write(tweet);
            bw.newLine();
            String words [] = tweet.split(" ");
            for(String word: words){
                // Single
//                if(wordsNCount.containsKey(word)){
//                    int count=wordsNCount.get(word);
//                    wordsNCount.put(word,count+1);
//                }
//                else{
//                    wordsNCount.put(word,1);
//                }

                // Dual
//                for (String wrd:words){
//                    if (wrd!=word){
//                        if(wordsNCount.containsKey(wrd+" "+word)){
//
//                           int count=wordsNCount.get(wrd+" "+word);
//                            wordsNCount.put(wrd+" "+word,count+1);
//                        }
//                        else{
//                            wordsNCount.put(wrd+" "+word,1);
//                        }
//                    }
//                }

            }
        }
        Map<String, Integer> sortedMapDesc = sortByComparator(wordsNCount, false);

        Iterator it = sortedMapDesc.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            bw.write(pair.getKey() + " = " + pair.getValue());
//            bw.newLine();
//            System.out.println(pair.getKey() + " = " + pair.getValue());
//            it.remove(); // avoids a ConcurrentModificationException
//        }

        bw.close();
    }catch(Exception ex){
        ex.printStackTrace();
    }finally {

    }

    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
