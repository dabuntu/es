package com.eventshop.eventshoplinux.performance;

import com.eventshop.eventshoplinux.model.Alert;
import com.eventshop.eventshoplinux.model.DataSource;
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
 * Created by Abhisek on 8/4/15.
 */
public class AlertTest extends CamelTestSupport {

    ClassLoader classLoader = getClass().getClassLoader();
    Properties properties = new Properties();
    List<Integer> createdAlertIDs = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    Logger LOGGER = LoggerFactory.getLogger(AlertTest.class);


    String host;
    String port;
    String createAlertPath, enableAlertPath, disableAlertPath, deleteAlertPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //Load the properties file
        InputStream fileInputStream = classLoader.getResourceAsStream("test.properties");
        properties.load(fileInputStream);

        host = properties.getProperty("host");
        port = properties.getProperty("port");
        createAlertPath = "http://" + host + ":" + port + "/eventshoplinux/rest/alertwebservice/registerAlert";
        enableAlertPath = "http://" + host + ":" + port + "/eventshoplinux/rest/alertwebservice/enableAlert";
        disableAlertPath = "http://" + host + ":" + port + "/eventshoplinux/rest/alertwebservice/disableAlert";
        deleteAlertPath = "http://" + host + ":" + port + "/eventshoplinux/rest/alertwebservice/deleteAlert";
    }

    @Override
    public void tearDown() throws Exception {
        LOGGER.info("In tear down");
        cleanUpAlert();
        super.tearDown();

    }

    @Test
    public void testCreateAlert() throws Exception {

        //Add Rest post route

        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new CreateAlert());
        camelContext.setTracing(true);
        camelContext.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    cleanUpAlert();
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
        LOGGER.info("Making REST call to {}. Content is {}", path, content);
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
        LOGGER.info("HTTP Response code from server is {}", conn.getResponseCode());
        LOGGER.info("Response from server is {}", s);
        conn.disconnect();
        return s;
    }

    public void cleanUpAlert() throws Exception {
        for (int aID : createdAlertIDs) {
            Alert alert = new Alert();
            alert.setaID(aID);
            String content = objectMapper.writeValueAsString(alert);
            makePostRestCall(disableAlertPath, content, "POST");
            makePostRestCall(deleteAlertPath, content, "DELETE");
        }
    }

    // Route to create Datasource using POST
    private class CreateAlert extends RouteBuilder {

        @Override
        public void configure() throws Exception {

            from("timer://createAlert?fixedRate=true&period=" + properties.get("alertCreationTimeInterval"))
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Inside route");
                        }
                    })
                    .to("direct:createAlert")
            ;

            from("direct:createAlert")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {

                            List<Integer> currentAlertIDs = new ArrayList<Integer>();
                            File dir = new File(classLoader.getResource("alert/").getFile());
                            File[] fList = dir.listFiles();
                            String content = "";
                            for (File file : fList) {
                                if (file.isFile()) {
                                    content = new Scanner(file).useDelimiter("\\Z").next();
                                    String result = makePostRestCall(createAlertPath, content, "POST");

                                    //Add the created ID output from the server to the list of created IDs
                                    createdAlertIDs.add(Integer.parseInt(result));
                                    currentAlertIDs.add(Integer.parseInt(result));
                                }
                            }

                            exchange.getOut().setBody(currentAlertIDs);

                        }
                    })
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            List<Integer> currentAlertIDs = exchange.getIn().getBody(ArrayList.class);
                            for (Integer aID : currentAlertIDs) {
                                //Enable the current DSIDs
                                Alert alert = new Alert();
                                alert.setaID(aID);

                                String content = objectMapper.writeValueAsString(alert);
                                String result = makePostRestCall(enableAlertPath, content, "POST");
                            }
                        }
                    })
            ;
        }
    }
}