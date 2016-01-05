package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.model.Alert;
import com.eventshop.eventshoplinux.model.AlertResponse;
import com.eventshop.eventshoplinux.model.EnableAlert;
import com.eventshop.eventshoplinux.servlets.AlertProcess;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nandhiniv on 6/10/15.
 */
public class AlertRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(AlertRoute.class);
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    public void configure() throws Exception {

        from("direct:runAlertForDS")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        int dsID = exchange.getIn().getHeader("dsID", Integer.class);
                        LOGGER.info("Going to get Alerts for ds {}", dsID);

                        String id = "ds" + dsID;
                        List<Alert> runnableAlerts = alertDAO.getAllEnabledAlertListForID(id);
                        exchange.getOut().setBody(runnableAlerts);
                    }
                }).to("direct:runAlert")
        ;

        from("direct:runAlertForQuery")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        int queryID = exchange.getIn().getBody(Integer.class);
                        LOGGER.info("Going to get Alerts for query {}", queryID);

                        String id = "Q" + queryID;
                        List<Alert> runnableAlerts = alertDAO.getAllEnabledAlertListForID(id);
                        exchange.getOut().setBody(runnableAlerts);
                    }
                }).to("direct:runAlert")
        ;

        from("direct:runAlert")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        List<Alert> enabledAlerts = exchange.getIn().getBody(ArrayList.class);
                        List<Alert> sendAlertList = new ArrayList<Alert>();

                        for (Alert alert : enabledAlerts) {
                            LOGGER.info("Running alert for " + alert.getaID());

                            if (alert.getResultEndpoint() != null && (!alert.getResultEndpoint().isEmpty())) {
                                //Check ds files and q files
                                String alertSource = alert.getAlertSource();
                                String alertSourceFilename = "";
                                int type = Integer.parseInt(alert.getAlertType());
                                if (type == 2) {
                                    if (alertSource.startsWith("ds")) {
                                        alertSourceFilename = Config.getProperty("tempDir") + "ds/" + alertSource.substring(2) + ".json";

                                    } else if (alertSource.startsWith("Q")) {
                                        alertSourceFilename = Config.getProperty("tempDir") + "queries/" + alertSource + ".json";
                                    }
                                    if (!alertSourceFilename.isEmpty()) {
                                        if (new File(alertSourceFilename).exists()) {
                                            sendAlertList.add(alert);
                                        }
                                    }
                                } else if (type == 1) {
                                    String safeSourceFilename = "";
                                    String safeSource = alert.getSafeSource();

                                    if (alertSource.startsWith("ds")) {
                                        alertSourceFilename = Config.getProperty("tempDir") + "ds/" + alertSource.substring(2) + ".json";

                                    } else if (alertSource.startsWith("Q")) {
                                        alertSourceFilename = Config.getProperty("tempDir") + "queries/" + alertSource + ".json";
                                    }
                                    if (safeSource.startsWith("ds")) {
                                        safeSourceFilename = Config.getProperty("tempDir") + "ds/" + safeSource.substring(2) + ".json";

                                    } else if (safeSource.startsWith("Q")) {
                                        safeSourceFilename = Config.getProperty("tempDir") + "queries/" + safeSource + ".json";
                                    }

                                    if ((!alertSourceFilename.isEmpty()) && (!safeSourceFilename.isEmpty())) {
                                        if (new File(alertSourceFilename).exists()) {
                                            sendAlertList.add(alert);
                                        }
                                    }
                                }
                            }
                        }

                        exchange.getOut().setBody(sendAlertList);

                    }
                })
                .split(body())
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Alert alert = exchange.getIn().getBody(Alert.class);
                        LOGGER.info("Enabled Alerts {} ", alert);

                        EnableAlert enableAlert = new EnableAlert(alert.getaID());
                        exchange.getOut().setBody(enableAlert);
                    }
                })
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {


                        EnableAlert enableAlert = exchange.getIn().getBody(EnableAlert.class);

                        LOGGER.info("Going to run alert : " + enableAlert.getAlertID());

                        HashMap<String, String> alert = alertDAO.getAlertDetails(enableAlert.getAlertID());
                        AlertProcess alertProcess = new AlertProcess();

                        String alertSrc = alert.get("alertSrc");



                        if (alert.get("alertType").equalsIgnoreCase("1")) {

                            File file = null;
                            File solutionFile = null;

                            if (alertSrc.startsWith("Q") || alertSrc.startsWith("q")) {
                                file = new File(Config.getProperty("tempDir") + "queries/Q" + alertSrc.substring(1) + ".json");
                            } else if (alertSrc.startsWith("ds")) {
                                file = new File(Config.getProperty("tempDir") + "ds/" + alertSrc.substring(2) + ".json");

                            }
                            String safeSrc = alert.get("safeSrc");
                            if (safeSrc.startsWith("Q") || safeSrc.startsWith("q")) {
                                solutionFile = new File(Config.getProperty("tempDir") + "queries/Q" + safeSrc.substring(1) + ".json");

                            } else if (alertSrc.startsWith("ds")) {
                                solutionFile = new File(Config.getProperty("tempDir") + "ds/" + safeSrc.substring(2) + ".json");
                            }
                            try {
                                String content = new Scanner(file).useDelimiter("\\Z").next();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject jsonObject = (JsonObject) jsonParser.parse(content);

                                content = new Scanner(solutionFile).useDelimiter("\\Z").next();
                                JsonObject jsonSolutionObject = (JsonObject) jsonParser.parse(content);

                                List<AlertResponse> alertResponseList = alertProcess.checkAlertWithSolution(jsonObject, jsonSolutionObject, false
                                        , Integer.parseInt(alert.get("alertSrcMin"))
                                        , Integer.parseInt(alert.get("alertSrcMax"))
                                        , Integer.parseInt(alert.get("safeSrcMin"))
                                        , Integer.parseInt(alert.get("safeSrcMax"))
                                        , alert.get("alertMessage")
                                        , alert.get("boundingbox"));

                                ObjectMapper objectMapper = new ObjectMapper();
                                String jsonString = objectMapper.writeValueAsString(alertResponseList);

                                exchange.getOut().setBody(alertResponseList);
                                postMessageToEndpoint(jsonString, alert.get("resultEndpoint"));


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {

                            try {
                                File file = null;
                                String type = null;

                                if (alertSrc.startsWith("Q") || alertSrc.startsWith("q")) {
                                    file = new File(Config.getProperty("tempDir") + "queries/Q" + alertSrc.substring(1) + ".json");
                                    type = "Q";
                                } else if (alertSrc.startsWith("ds")) {
                                    type = "ds";
                                    file = new File(Config.getProperty("tempDir") + "ds/" + alertSrc.substring(2) + ".json");
                                }
                                String content = new Scanner(file).useDelimiter("\\Z").next();
                                JsonParser jsonParser = new JsonParser();
                                JsonObject jsonObject = (JsonObject) jsonParser.parse(content);

                                List<AlertResponse> alertResponseList = alertProcess.checkAlertWithoutSolution(jsonObject, type, false
                                        , Integer.parseInt(alert.get("alertSrcMin"))
                                        , Integer.parseInt(alert.get("alertSrcMax"))
                                        , alert.get("alertMessage"), alert.get("boundingbox"));

                                ObjectMapper objectMapper = new ObjectMapper();
                                String jsonString = objectMapper.writeValueAsString(alertResponseList);
                                exchange.getOut().setBody(alertResponseList);

                                postMessageToEndpoint(jsonString, alert.get("resultEndpoint"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        exchange.getOut().setHeader("enablePostingAlert", Config.getProperty("enablePostingAlert"));
                    }
                })
                .choice()
                .when(header("enablePostingAlert").isEqualTo(true))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        List<AlertResponse> alertResponseList = exchange.getIn().getBody(ArrayList.class);
                        List<String> twitterUpdateList = new ArrayList<String>();

                        for (AlertResponse alertResponse : alertResponseList) {

                            String status = "";
                            String message = alertResponse.getAlertMessage();

                            if (message.contains("{$coordinate}")) {
                                if (alertResponse.getLatlong() != null) {
                                    message = message.replace("{$coordinate}", alertResponse.getLatlong());
                                } else {
                                    message = message.replace("{$coordinate}", "");
                                }
                            }
                            if (message.contains("{$geoAddress}")) {
                                if (alertResponse.getGeoAddress() != null) {
                                    message = message.replace("{$geoAddress}", alertResponse.getGeoAddress());
                                } else {
                                    message = message.replace("{$geoAddress}", "");
                                }
                            }
                            if (message.contains("{$solutionCoordinate}")) {
                                if (alertResponse.getSolutionLatLong() != null) {
                                    message = message.replace("{$solutionCoordinate}", alertResponse.getSolutionLatLong());
                                } else {
                                    message = message.replace("{$solutionCoordinate}", "");
                                }
                            }
                            if (message.contains("{$solutionAddress}")) {
                                if (alertResponse.getSolutionLatLong() != null) {
                                    message = message.replace("{$solutionAddress}", alertResponse.getSolutionGeoAddress());
                                } else {
                                    message = message.replace("{$solutionAddress}", "");
                                }
                            }

                            status += message;

                           LOGGER.debug("Tweet Tweet:-   " + status);
                            if (status.length() < 140) {
                                twitterUpdateList.add(status);
                            }
                        }
                        exchange.getOut().setBody(twitterUpdateList);
                    }
                })
                .split(body())
                .to("twitter://timeline/user?" +
                        "consumerKey=" + Config.getProperty("alerttwtConsumerKey") +
                        "&consumerSecret=" + Config.getProperty("alerttwtConsumerSecret") +
                        "&accessToken=" + Config.getProperty("alerttwtAccessToken") +
                        "&accessTokenSecret=" + Config.getProperty("alerttwtAccessTokenSecret"))
                .endChoice()
        ;
    }

    public void postMessageToEndpoint(String jsonString, String resultEndpoint) {
        Client client = Client.create();
        WebResource webResource = client.resource(resultEndpoint);
        ClientResponse response = webResource.type("application/json").post(ClientResponse.class, jsonString);
        if (!String.valueOf(response.getStatus()).startsWith("2")) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        LOGGER.info("Server response : {} ", response.getEntity(String.class));
    }
}
