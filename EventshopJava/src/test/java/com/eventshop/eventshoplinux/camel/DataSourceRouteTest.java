// package com.eventshop.eventshoplinux.camel;

// import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
// import com.eventshop.eventshoplinux.domain.datasource.DataSource;
// import org.apache.camel.*;
// import org.apache.camel.builder.RouteBuilder;
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.test.junit4.CamelTestSupport;
// import org.junit.Test;

// import java.util.HashMap;
// import java.util.Map;

// /**
//  * Created by nandhiniv on 6/4/15.
//  */
// public class DataSourceRouteTest extends CamelTestSupport {

//     @EndpointInject(uri = "mock:twitterStreaming")
//     protected MockEndpoint resultTwitterStreaming;

//     @EndpointInject(uri = "mock:twitterSearch")
//     protected MockEndpoint resultTwitterSearch;

//     @Produce(uri = "direct:dataSourceRouter")
//     protected ProducerTemplate template;

//     @Produce(uri = "direct:toTwitterSearchAndStreaming")
//     protected ProducerTemplate twitterSearchAndStreamingTemplate;

//     DataSource dataSource;

//     @Override
//     public void setUp() throws Exception {
//         super.setUp();
//         DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
//         dataSource = dataSourceManagementDAO.getDataSource(1);


//         context.addRoutes(new RouteBuilder() {
//             @Override
//             public void configure() throws Exception {
//                 interceptSendToEndpoint("direct:twitterStreaming")
//                         .skipSendToOriginalEndpoint()
//                         .to("mock:twitterStreaming");

//                 interceptSendToEndpoint("direct:toCSV")
//                         .skipSendToOriginalEndpoint()
//                         .to("mock:resultCsv");

//                 from("direct:twitterSearch").to("mock:twitterSearch");

//             }
//         });
//         context().addRoutes(new DataSourceRoute());
//     }

//     @Test
//     public void testTwitterSerachAndStreaming() throws Exception {
//         resultTwitterStreaming.expectedMessageCount(1);
//         resultTwitterStreaming.expectedBodiesReceived("test");
//         resultTwitterSearch.expectedMessageCount(1);
//         resultTwitterSearch.expectedBodiesReceived("test");

//         twitterSearchAndStreamingTemplate.sendBody("test");

//         resultTwitterStreaming.assertIsSatisfied();
//         resultTwitterSearch.assertIsSatisfied();
//     }


//     @Test
//     public void testDataSourceRouter() throws Exception {

//         resultTwitterStreaming.expectedMessageCount(1);
//         resultTwitterStreaming.expectedHeaderReceived("dsType", "Twitter");
//         resultTwitterStreaming.expectedMessagesMatches(new Predicate() {
//             @Override
//             public boolean matches(Exchange exchange) {

//                 boolean result;
//                 DataSource dsHeader = exchange.getIn().getHeader("datasource", DataSource.class);
//                 DataSource dsBody = exchange.getIn().getBody(DataSource.class);
//                 if (dataSource.getSrcID().equals(dsHeader.getSrcID()) && dataSource.getSrcID().equals(dsBody.getSrcID())) {
//                     result = true;
//                 } else {
//                     result = false;
//                 }
//                 return result;
//             }
//         });

//         resultTwitterSearch.expectedMessageCount(1);
//         resultTwitterSearch.expectedHeaderReceived("dsType", "Twitter");
//         resultTwitterSearch.expectedMessagesMatches(new Predicate() {
//             @Override
//             public boolean matches(Exchange exchange) {

//                 boolean result;
//                 DataSource dsHeader = exchange.getIn().getHeader("datasource", DataSource.class);
//                 DataSource dsBody = exchange.getIn().getBody(DataSource.class);
//                 if (dataSource.getSrcID().equals(dsHeader.getSrcID()) && dataSource.getSrcID().equals(dsBody.getSrcID())) {
//                     result = true;
//                 } else {
//                     result = false;
//                 }
//                 return result;
//             }
//         });

//         Map<String, Object> headerMap = new HashMap<String, Object>();
//         headerMap.put("datasource", dataSource);

//         template.sendBodyAndHeaders(1, headerMap);
//         resultTwitterStreaming.assertIsSatisfied();
//         resultTwitterSearch.assertIsSatisfied();
//     }

// }