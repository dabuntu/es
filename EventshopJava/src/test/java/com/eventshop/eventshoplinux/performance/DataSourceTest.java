package com.eventshop.eventshoplinux.performance;

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
 * Created by nandhiniv on 8/4/15.
 */
public class DataSourceTest extends CamelTestSupport {

    ClassLoader classLoader = getClass().getClassLoader();
    Properties properties = new Properties();
    List<Integer> createdDSIDs = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    Logger LOGGER = LoggerFactory.getLogger(DataSourceTest.class);


    String host;
    String port;
    String createDSPath, enableDSPath, disableDSPath, deleteDSPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //Load the properties file
        InputStream fileInputStream = classLoader.getResourceAsStream("test.properties");
        properties.load(fileInputStream);

        host = properties.getProperty("host");
        port = properties.getProperty("port");
        createDSPath = "http://" + host + ":" + port + "/eventshoplinux/rest/dataSourceService/createDataSource";
        enableDSPath = "http://" + host + ":" + port + "/eventshoplinux/rest/dataSourceService/enableDataSource";
        disableDSPath = "http://" + host + ":" + port + "/eventshoplinux/rest/dataSourceService/disableDataSource";
        deleteDSPath = "http://" + host + ":" + port + "/eventshoplinux/rest/dataSourceService/deleteDataSource";
    }

    @Override
    public void tearDown() throws Exception {
        LOGGER.info("In tear down");
        cleanUpDatasource();
        super.tearDown();

    }

    @Test
    public void testCreateDataSource() throws Exception {

        //Add Rest post route

        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new CreateDataSource());
        camelContext.setTracing(true);
        camelContext.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    cleanUpDatasource();
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

    public void cleanUpDatasource() throws Exception {
        for (int dsID : createdDSIDs) {
            DataSource dataSource = new DataSource();
            dataSource.setID(dsID);
            String content = objectMapper.writeValueAsString(dataSource);
            makePostRestCall(disableDSPath, content, "POST");
            makePostRestCall(deleteDSPath, content, "DELETE");
        }
    }

    // Route to create Datasource using POST
    private class CreateDataSource extends RouteBuilder {

        @Override
        public void configure() throws Exception {

            from("timer://createDS?fixedRate=true&period=" + properties.get("dsCreationTimeInterval"))
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Inside route");
                        }
                    })
                    .to("direct:createDatasource")
            ;

            from("direct:createDatasource")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {

                            List<Integer> currentDSIDs = new ArrayList<Integer>();
                            File dir = new File(classLoader.getResource("datasource/").getFile());
                            File[] fList = dir.listFiles();
                            String content = "";
                            for (File file : fList) {
                                if (file.isFile()) {
                                    content = new Scanner(file).useDelimiter("\\Z").next();
                                    String result = makePostRestCall(createDSPath, content, "POST");

                                    //Add the created ID output from the server to the list of created IDs
                                    createdDSIDs.add(Integer.parseInt(result));
                                    currentDSIDs.add(Integer.parseInt(result));
                                }
                            }

                            exchange.getOut().setBody(currentDSIDs);

                        }
                    })
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            List<Integer> currentDSIDs = exchange.getIn().getBody(ArrayList.class);
                            for (Integer dsID : currentDSIDs) {
                                //Enable the current DSIDs
                                DataSource dataSource = new DataSource();
                                dataSource.setID(dsID);

                                String content = objectMapper.writeValueAsString(dataSource);
//                                String result = makePostRestCall(enableDSPath, content, "POST");
                            }
                        }
                    })
            ;
        }
    }
}