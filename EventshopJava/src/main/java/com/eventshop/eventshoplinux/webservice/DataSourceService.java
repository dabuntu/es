package com.eventshop.eventshoplinux.webservice;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceDao;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.akka.dataSource.DataSourceSchedular;
import com.eventshop.eventshoplinux.model.DataSource;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by abhisekmohanty on 4/8/15.
 */

@Path("/dataSourceService")
public class DataSourceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);
    @Context
    private ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createDataSource")
    public String createDataSource(DataSource dataSource) {
        DataSourceDao dataSourceDao = new DataSourceDao();
        int id = dataSourceDao.registerDatasource(dataSource);
        //Update Data Cache
        DataCache.updateRegisteredSources();
        return String.valueOf(id);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteDataSource")
    public String deleteDataSource(DataSource dataSource) {

        DataSourceDao dataSourceDao = new DataSourceDao();
        boolean linked = dataSourceDao.checkLinkedQuery(dataSource.getID());
        if (!linked) {
            boolean enabled = dataSourceDao.getDsStatus(dataSource.getID());
            if (enabled)
                disableDataSource(dataSource);
            ZkClient zkClient = new ZkClient("localhost:2181", 10000);
            zkClient.deleteRecursive(ZkUtils.getTopicPath("ds" + dataSource.getID()));
            String result = dataSourceDao.deleteDatasource(dataSource.getID());
            //Update Data Cache
            DataCache.updateRegisteredSources();
            return result;
        } else {
            return "Datasource linked with query so can't be deleted";
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/enableDataSource")
    public String enableDataSource(DataSource dataSource) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        ActorSelection dataSourceSchedularActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/dataSourceSchedularActor");
        LOGGER.info("datasource id is "  + dataSource.getID());
        dataSourceSchedularActor.tell(new DataSourceSchedular.StartDataSource((dataSource.getID())), null);
        return "Enabled Data source with ID " + dataSource.getID();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/disableDataSource")
    public String disableDataSource(DataSource dataSource) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        ActorSelection dataSourceSchedularActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/dataSourceSchedularActor");
        dataSourceSchedularActor.tell(new DataSourceSchedular.StopDataSource((dataSource.getID())), null);
        return "Disabled Data source with ID " + dataSource.getID();
    }
}