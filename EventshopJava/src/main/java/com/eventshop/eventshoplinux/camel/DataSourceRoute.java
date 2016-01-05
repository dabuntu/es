package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nandhiniv on 5/13/15.
 */
public class DataSourceRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceRoute.class);

    @Override
    public void configure() throws Exception {


        from("direct:dataSourceRouter")
                .threads(Integer.parseInt(Config.getProperty("threadPoolSize")), Integer.parseInt(Config.getProperty("maxthreadPoolSize")))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.info("Inside datasource router");
                        int dataSourceId = exchange.getIn().getBody(Integer.class);

                        DataSourceManagementDAO datasourceDAO = new DataSourceManagementDAO();
                        DataSource dataSource = datasourceDAO.getDataSource(dataSourceId);
                        DataSource.DataFormat dataFormat = dataSource.getSrcFormat();

                        exchange.getOut().setHeader("dsFormat", dataFormat);
                        exchange.getOut().setHeader("dsType", dataSource.getSupportedWrapper());
                        exchange.getOut().setHeader("datasource", dataSource);
                    }
                })
                .choice()
                .when(header("dsFormat").isEqualTo(DataSource.DataFormat.stream))
                    .to("direct:toTwitterSearchAndStreaming")
                .when(header("dsFormat").isEqualTo(DataSource.DataFormat.file))
                .to("direct:readFromFile")
                .when(header("dsFormat").isEqualTo(DataSource.DataFormat.rest))
                .to("direct:readFromRest")
        ;

    }
}
