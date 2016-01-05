// package com.eventshop.eventshoplinux.akka;


// import akka.actor.ActorRef;
// import akka.actor.ActorSystem;
// import akka.actor.Props;
// import akka.pattern.Patterns;
// import akka.testkit.JavaTestKit;
// import akka.util.Timeout;
// import com.eventshop.eventshoplinux.camel.queryProcessor.CommonQueryUtil;
// import com.eventshop.eventshoplinux.model.QueryMessage;
// import com.eventshop.eventshoplinux.util.commonUtil.Config;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import org.junit.AfterClass;
// import org.junit.BeforeClass;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import scala.concurrent.Await;
// import scala.concurrent.Future;
// import scala.concurrent.duration.Duration;
// import scala.concurrent.duration.FiniteDuration;

// import java.io.File;
// import java.io.IOException;
// import java.util.Scanner;
// import java.util.concurrent.TimeUnit;

// /**
//  * Created by nandhiniv on 5/28/15.
//  */
// public class QueryOperationsAkkaTest {

//     private final static Logger LOGGER = LoggerFactory.getLogger(MasterActor.class);
//     private static final String masterQueryID = "999";
//     static ActorSystem actorSystem;
//     private final FiniteDuration duration = Duration.create(10, TimeUnit.SECONDS);
//     private final Timeout timeout = Timeout.durationToTimeout(duration);
//     ClassLoader classLoader = getClass().getClassLoader();
//     CommonQueryUtil commonQueryUtil = new CommonQueryUtil();

//     @BeforeClass
//     public static void setup() {
//         QueryOperationsAkkaTest queryOperationsAkkaTest = new QueryOperationsAkkaTest();
//         queryOperationsAkkaTest.initialize();
//         actorSystem = ActorSystem.create();
//     }

//     @AfterClass
//     public static void teardown() {
//         actorSystem.shutdown();
//     }

//     public void initialize() {
//         //Read from test resources and create json
//         File dir = new File(classLoader.getResource("input/emage/").getFile());
//         File[] fList = dir.listFiles();
//         for (File file : fList) {
//             if (file.isFile()) {
//                 try {
//                     commonQueryUtil.readEmageFileAndCreateBin(file, Config.getProperty("tempDir") + "ds"
//                             + file.getName().substring(0, file.getName().indexOf(".")));
//                 } catch (IOException e) {
//                     e.printStackTrace();
//                 }
//             }
//         }

//     }

//     @org.junit.Test
//     public void testIt() {

//         new JavaTestKit(actorSystem) {
//             {

//                 try {
//                     final ActorRef aggregationActor = actorSystem.actorOf(Props.create(AggregationActor.class));
//                     final ActorRef filterActor = actorSystem.actorOf(Props.create(FilterActor.class));
//                     final ActorRef groupingActor = actorSystem.actorOf(Props.create(GroupingActor.class));
//                     final ActorRef spatialCharActor = actorSystem.actorOf(Props.create(SpatialCharActor.class));
//                     final ActorRef spatialMatchingActor = actorSystem.actorOf(Props.create(SpatialMatchingActor.class));
//                     final ActorRef temporalCharActor = actorSystem.actorOf(Props.create(TemporalCharActor.class));
//                     final ActorRef temporalMatchingActor = actorSystem.actorOf(Props.create(TemporalMatchingActor.class));

//                     final JavaTestKit probe = new JavaTestKit(actorSystem);

//                     File dir = new File(classLoader.getResource("input/queryTree/").getFile());
//                     File[] fList = dir.listFiles();
//                     for (File file : fList) {
//                         if (file.isFile()) {
//                             String content = null;
//                             content = new Scanner(file).useDelimiter("\\Z").next();
//                             JsonParser jsonParser = new JsonParser();
//                             JsonObject query = (JsonObject) jsonParser.parse(content);

//                             //Do switching to actors
//                             String result = null;

//                             String patternType = query.get("patternType").getAsString();

//                             if (patternType.equalsIgnoreCase("filter")) {
//                                 Future<Object> future = Patterns.ask(filterActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("grouping")) {
//                                 Future<Object> future = Patterns.ask(groupingActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("aggregation")) {
//                                 Future<Object> future = Patterns.ask(aggregationActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("spchar")) {
//                                 Future<Object> future = Patterns.ask(spatialCharActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("spmatching")) {
//                                 Future<Object> future = Patterns.ask(spatialMatchingActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("tpchar")) {
//                                 Future<Object> future = Patterns.ask(temporalCharActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             } else if (patternType.equalsIgnoreCase("tpmatching")) {
//                                 Future<Object> future = Patterns.ask(temporalMatchingActor, new QueryMessage(masterQueryID, query), timeout);
//                                 result = (String) Await.result(future, duration);
//                             }

//                             LOGGER.info("Output file name is : " + result);

// //                        assert result.equals("Q1_1");

//                             String resultFilename = Config.getProperty("tempDir") + "queries/Q" + masterQueryID + ".json";
//                             File resultFile = new File(resultFilename);
//                             File outputFile = new File(classLoader.getResource("output/" + file.getName() + ".json").getFile());

//                             LOGGER.info("Going to compare " + resultFile.getName() + " and " + outputFile.getName());

//                             String resultFileContent = new Scanner(resultFile).useDelimiter("\\Z").next();
//                             String outputFileContent = new Scanner(outputFile).useDelimiter("\\Z").next();

//                             assert resultFileContent.equals(outputFileContent);


//                             cleanup();
//                         }
//                     }
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }

//         };


//     }

//     private void cleanup() {

// //        File jsonFile = new File(Config.getProperty("tempDir") + "queries/Q" + masterQueryID + ".json");
// //        jsonFile.delete();

//         File dir = new File(Config.getProperty("context") + "proc/Debug/");
//         File[] fList = dir.listFiles();
//         for (File file : fList) {
//             if (file.getName().startsWith("EmageOperators_Q" + masterQueryID)) {
//                 file.delete();
//             }
//         }

//     }


// }