package com.eventshop.eventshoplinux.performance;

import com.eventshop.eventshoplinux.model.DataSource;
import com.eventshop.eventshoplinux.ruleEngine.Rule;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by aravindh on 1/14/16.
 */

public class RuleTest extends CamelTestSupport {

    ClassLoader classLoader = getClass().getClassLoader();
    Properties properties = new Properties();
    List<Integer> createdRuleIDs = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    Logger LOGGER = LoggerFactory.getLogger(RuleTest.class);


    String host;
    String port;
    String createRulePath, enableRulePath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //Load the properties file
        InputStream fileInputStream = classLoader.getResourceAsStream("test.properties");
        properties.load(fileInputStream);

        host = properties.getProperty("host");
        port = properties.getProperty("port");
        createRulePath = "http://" + host + ":" + port + "/eventshoplinux/rest/rulewebservice/rule";
        enableRulePath = "http://" + host + ":" + port + "/eventshoplinux/rest/rulewebservice/enableRule";
        //  disableRulePath = "http://" + host + ":" + port + "/eventshoplinux/rest/rulewebservice/disableDataSource";
        //  deleteRulePath = "http://" + host + ":" + port + "/eventshoplinux/rest/rulewebservice/deleteDataSource";
    }

    @Override
    public void tearDown() throws Exception {
        LOGGER.info("In tear down");
        //   cleanUpDatasource();
        super.tearDown();

    }

    @Test
    public void testCreateRule() throws Exception {

        //Add Rest post route

        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new CreateRule());
        camelContext.setTracing(true);
        camelContext.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    //      cleanUpDatasource();
                    camelContext.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        waitForStop();

    }

    void waitForStop() {
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public String makePostRestCall(String path, String content, String method) throws Exception {
        System.out.println("Making REST call to "+path+" Content is "+ content);
        HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");

        OutputStream os = conn.getOutputStream();
        os.write(content.getBytes());
        os.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        String s = "";
        while ((output = br.readLine()) != null) {
            s += output;
        }
        System.out.println("HTTP Response code from server is "+ conn.getResponseCode());
        System.out.println("Response from server is "+ s);
        conn.disconnect();
        return s;
    }

//    public void cleanUpDatasource() throws Exception {
//        for (int ruleID : createdRuleIDs) {
//            DataSource dataSource = new DataSource();
//            dataSource.setID(ruleID);
//            String content = objectMapper.writeValueAsString(dataSource);
//            makePostRestCall(disableRulePath, content, "POST");
//            makePostRestCall(deleteRulePath, content, "DELETE");
//        }
//    }

    private class CreateRule extends RouteBuilder {

        @Override
        public void configure() throws Exception {

            from("timer://createRule?fixedRate=true&period=" + properties.get("ruleCreationTimeInterval"))
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Inside route");
                        }
                    })
                    .to("direct:createRule")
            ;

            from("direct:createRule")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Insert create Rule");
                            List<Integer> currentRuleIDs = new ArrayList<Integer>();
                            File dir = new File(classLoader.getResource("rule/").getFile());
                            File[] fList = dir.listFiles();
                            String content = "";
                            for (File file : fList) {
                                System.out.println("Inside for loop"+file.getName());
                                if (file.isFile()) {
                                    System.out.println("Inside If");
                                    content = new Scanner(file).useDelimiter("\\Z").next();
                                    String result = makePostRestCall(createRulePath, content, "POST");
                                    String enabled = makePostRestCall(enableRulePath, content, "POST");
                                    //Add the created ID output from the server to the list of created IDs
                                    createdRuleIDs.add(Integer.parseInt(result));
                                    currentRuleIDs.add(Integer.parseInt(result));
                                }
                            }

                            exchange.getOut().setBody(currentRuleIDs);

                        }
                    })
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            List<Integer> currentRuleIDs = exchange.getIn().getBody(ArrayList.class);
                            for (Integer ruleID : currentRuleIDs) {
                                //Enable the current DSIDs
                                Rule rule = new Rule();
                                rule.setRuleID(ruleID.toString());

                                String content = objectMapper.writeValueAsString(rule);
//                                String result = makePostRestCall(enableDSPath, content, "POST");
                            }
                        }
                    })
            ;
        }
    }
}