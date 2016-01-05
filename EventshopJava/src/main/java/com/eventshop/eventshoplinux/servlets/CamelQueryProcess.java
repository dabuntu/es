package com.eventshop.eventshoplinux.servlets;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nandhiniv on 5/29/15.
 */
public class CamelQueryProcess implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CamelQueryProcess.class);
    protected Log log = LogFactory.getLog(this.getClass().getName());
    private CamelContext camelContext;
    private String qID;
    private boolean isRunning;

    public CamelQueryProcess(String qID, CamelContext camelContext, boolean running) {
        this.qID = qID;
        this.camelContext = camelContext;
        this.isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startProcess() {
       LOGGER.debug("Going to send message to masterActor");
        Endpoint endpoint = camelContext.getEndpoint("direct:queryInit");
        ProducerTemplate producerTemplate = new DefaultProducerTemplate(camelContext, endpoint);
        try {
            producerTemplate.start();
            producerTemplate.sendBody(endpoint, qID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            LOGGER.debug("Starting Query Process");
            startProcess();
            try {
                Thread.currentThread().sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public boolean stop() {

        isRunning = false;
        try {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
