package com.eventshop.eventshoplinux.webservice;


import akka.actor.ActorSystem;
import akka.camel.CamelExtension;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.model.PopulateData;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by nandhiniv on 10/12/15.
 */
@Path("/populateDataService")

public class PopulateDataWebService {

    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDataWebService.class);
    @Context
    private ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/populateData")
    public PopulateData populateData(PopulateData populateData) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        DefaultCamelContext camelContext = CamelExtension.get(actorSystem).context();
        Endpoint endpoint = camelContext.getEndpoint("direct:populateKafka");
        ProducerTemplate producerTemplate = new DefaultProducerTemplate(camelContext, endpoint);
        try {
            producerTemplate.start();
            producerTemplate.sendBodyAndHeader(endpoint, populateData.getData(), "dataSource"
                    , new DataSourceManagementDAO().getDataSource(populateData.getDsID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return populateData;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/populateListData")
    public PopulateData populateListData(PopulateData populateData) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        DefaultCamelContext camelContext = CamelExtension.get(actorSystem).context();
        Endpoint endpoint = camelContext.getEndpoint("direct:toJsonPath");
        ProducerTemplate producerTemplate = new DefaultProducerTemplate(camelContext, endpoint);
        try {
            producerTemplate.start();
            producerTemplate.sendBodyAndHeader(endpoint, populateData.getData(), "datasource"
                    , new DataSourceManagementDAO().getDataSource(populateData.getDsID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return populateData;
    }

    @POST
    @Path("/getData")
    public String populateData(String code) {
        System.out.println(code);
        return code;
    }
}