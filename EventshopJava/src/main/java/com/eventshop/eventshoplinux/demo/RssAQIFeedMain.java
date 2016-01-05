package com.eventshop.eventshoplinux.demo;

/**
 * Created by nandhiniv on 5/22/15.
 */


import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.jsoup.Jsoup;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssAQIFeedMain {

    private Map<String, ELocation> locationMap = new HashMap<String, ELocation>();

    public static void main(String[] args) throws Exception {
        new RssAQIFeedMain().run();
    }

    void run() throws Exception {
        final CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(createRouteBuilder());
        camelContext.start();

        Endpoint endpoint = camelContext.getEndpoint("direct:initializeLocations");
        ProducerTemplate producerTemplate = new DefaultProducerTemplate(camelContext, endpoint);


        try {
            producerTemplate.start();
            producerTemplate.sendBody(endpoint, "test");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    camelContext.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        waitForStop();
    }

    RouteBuilder createRouteBuilder() {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:initializeLocations")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                File file = new File(Config.getProperty("context") + "nationalAirQuality.file");
                                String content = new Scanner(file).useDelimiter("\\Z").next();
                                exchange.getOut().setBody(content);
                            }
                        })
                        .unmarshal().csv()
                        .split(body())
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                List locationList = exchange.getIn().getBody(ArrayList.class);
                                ELocation eLocation = new ELocation(Double.valueOf(locationList.get(2).toString()), Double.valueOf(locationList.get(1).toString()));
                                locationMap.put(locationList.get(0).toString(), eLocation);
                            }
                        })
                ;

                from("timer://foo?fixedRate=true&delay=5000&period=60000")
                        .to("direct:test");


                from("direct:test")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {

                                File l_file = new File(Config.getProperty("context") + "aqi_links.txt");
                                String content = new Scanner(l_file).useDelimiter("\\Z").next();
                                String deleteRssFile = Config.getProperty("context") + "rss.file";
                                System.out.println("File name " + deleteRssFile);
                                File file = new File(deleteRssFile);
                                if (file.exists()) {
                                    file.delete();
                                    System.out.println("Deleted file");
                                } else {
                                    System.out.println("File does not exists");
                                }
                                exchange.getOut().setBody(content);
                            }
                        })
                        .unmarshal().csv()
                        .split(body())
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                ArrayList body = exchange.getIn().getBody(ArrayList.class);

                                String link = (String) body.get(0);
                                System.out.println(link);
                                String rssEndpoint = "rss:" + link + "?splitEntries=false&consumer.delay=60000";
                                if (getContext().hasEndpoint(rssEndpoint) == null) {
                                    getContext().addRoutes(new DynamicRoute(getContext(), rssEndpoint, "direct:processFeeds"));
                                } else {
                                    System.out.println("### Route already exists");
                                }
                            }
                        })
                ;

                from("direct:processFeeds")
                        .marshal().rss()
                        .setBody(xpath("//rss/channel/item/description/text()", String.class))
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                String body = exchange.getIn().getBody(String.class);
                                body = html2text(body);
                                String patternString = "AQI - Particle Pollution \\(2.5 microns\\)";
                                Pattern pattern = Pattern.compile(patternString);
                                Matcher matcher = pattern.matcher(body);
                                String patternLocation = "Location:";

                                Pattern pattern3 = Pattern.compile(patternLocation);
                                Matcher matcher3 = pattern3.matcher(body);

                                Pattern patternCA = Pattern.compile(" CA");
                                Matcher matcherCA = patternCA.matcher(body);


                                int foundIndex = 0;
                                int locationBeginIndex = 0, locationEndIndex = 0;
                                String finalLocation;

                                String finalString = "";
                                String s = "";
                                if (matcher.find()) {
                                    String subString = body.substring(0, matcher.start());
                                    Pattern pattern1 = Pattern.compile("-");
                                    Matcher matcher1 = pattern1.matcher(subString);
                                    while (matcher1.find()) {
                                        foundIndex = matcher1.start();
                                    }
                                    finalString = subString.substring(foundIndex + 1, matcher.start());
                                    finalString = finalString.replace(" ", "");

                                    if (matcher3.find()) {
                                        locationBeginIndex = matcher3.end();
                                    }
                                    if (matcherCA.find()) {
                                        locationEndIndex = matcherCA.start();
                                    }
                                    finalLocation = body.substring(locationBeginIndex + 1, locationEndIndex - 1);
                                    ELocation location = locationMap.get(finalLocation);

                                    if (location != null) {
                                        s = location.getLat() + "," + location.getLon() + "," + finalString + "\n";
                                        System.out.println(s);
                                    }

                                }

                                if (!s.isEmpty()) {
                                    exchange.getOut().setBody(s);
                                    exchange.getOut().setHeader("fileNotEmpty", true);

                                }
                                exchange.getOut().setHeader(Exchange.FILE_NAME, "rss.file");
                            }
                        })
                        .choice().when(header("fileNotEmpty").isEqualTo(true))
                        .to("file:" + Config.getProperty("context") + "?noop=true&charset=iso-8859-1&fileExist=Append")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                System.out.println("Update File " + exchange.getIn().getHeader(Exchange.FILE_NAME));
                            }
                        }).endChoice()
                ;

            }
        };
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

    public String html2text(String html) {
        return Jsoup.parse(html).text();
    }
}