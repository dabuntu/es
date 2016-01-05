package com.eventshop.eventshoplinux.akka.dataSource;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nandhiniv on 6/15/15.
 */

/**
 * This class is an Actor which will schedule the Data Sources on startup. It adds Akka Schedulars to the actor system.
 */
public class DataSourceSchedular extends UntypedActor {

    private final static Logger LOG = LoggerFactory.getLogger(DataSourceSchedular.class);
    private final DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
    private Cancellable startDSSchedule;
    private Map<Integer, Cancellable> dataSourceSchedulars = new HashMap<Integer, Cancellable>();
    private ActorRef dataProcessRouteProducer;
    private ActorRef twitterActor;


    DataSourceSchedular(ActorRef dataProcessRouteProducer, ActorRef twitterActor) {
        this.dataProcessRouteProducer = dataProcessRouteProducer;
        this.twitterActor = twitterActor;
    }

    /**
     * Used to create the DataSouceSchedular actor
     *
     * @param dataProcessRouteProducer
     * @param twitterActor
     * @return
     */
    public static Props props(final ActorRef dataProcessRouteProducer, final ActorRef twitterActor) {
        return Props.create(new Creator<DataSourceSchedular>() {
            @Override
            public DataSourceSchedular create() throws Exception {
                return new DataSourceSchedular(dataProcessRouteProducer, twitterActor);
            }
        });
    }

    /**
     * @throws Exception
     */
    @Override
    public void preStart() throws Exception {
        super.preStart();
        startDSSchedule = getContext().system().scheduler().scheduleOnce(
                Duration.create(5, TimeUnit.SECONDS),
                getSelf(), "startDS", getContext().dispatcher(), null);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    /**
     * Used to add the initial schedular to the actorSystem, which in turn adds schedul
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {

        if (message.equals("startDS")) {
            DataCache.updateRegisteredSources();

            ArrayList<String> enabledDSIds = dataSourceManagementDAO.getAllEnabledDsIds();
            LOG.info("Enabled dsIDs {} ", enabledDSIds);
            for (String dsID : enabledDSIds) {
                //Get the data source from cache
                DataSource dataSource = dataSourceManagementDAO.getDataSource(Integer.parseInt(dsID));
                LOG.info("Sync Time {}", dataSource.getInitParam().getSyncAtMilSec());
                Cancellable cancellable = getContext().system().scheduler().schedule(
                        Duration.create(0, TimeUnit.MILLISECONDS),
                        Duration.create(dataSource.getInitParam().getSyncAtMilSec(), TimeUnit.MILLISECONDS),
                        dataProcessRouteProducer, dsID, getContext().dispatcher(), null);
                dataSourceSchedulars.put(Integer.parseInt(dsID), cancellable);
                if (!cancellable.isCancelled()) {
                    LOG.info("Added scheduler for DataSource {}", dsID);
                }
                if (dataSource.getUrl().contains("www.twitter.com")) {
                    LOG.debug("Going to send message to TwitterActor");
                    twitterActor.tell(new TwitterActor.StartTwitterProducer(Integer.parseInt(dsID)), getSelf());
                }

            }
        } else if (message instanceof StartDataSource) {
            StartDataSource startDataSource = (StartDataSource) message;
            //Enable ds in database
            int dsID = startDataSource.getId();
            dataSourceManagementDAO.enableDataSource(dsID);
            DataSource dataSource = dataSourceManagementDAO.getDataSource(dsID);
            DataCache.updateRegisteredSources();

            //create a cancellable
            Cancellable cancellable = getContext().system().scheduler().schedule(
                    Duration.create(0, TimeUnit.MILLISECONDS),
                    Duration.create(dataSource.getInitParam().getSyncAtMilSec(), TimeUnit.MILLISECONDS),
                    dataProcessRouteProducer, dsID, getContext().dispatcher(), null);
            dataSourceSchedulars.put(dsID, cancellable);
            if (!cancellable.isCancelled()) {
                LOG.info("Added scheduler for DataSource {}", dsID);
            }
            //Send message to Twitter Actor
            if (dataSource.getUrl().contains("www.twitter.com")) {
                twitterActor.tell(new TwitterActor.StartTwitterProducer(dsID), getSelf());
            }
        } else if (message instanceof StopDataSource) {
            StopDataSource stopDataSource = (StopDataSource) message;
            final int stopDataSourceId = stopDataSource.getId();
            LOG.info("Stopping scheduler for DataSource " + stopDataSourceId);

            if (dataSourceSchedulars.containsKey(stopDataSourceId)) {
                Cancellable cancellable = dataSourceSchedulars.get(stopDataSourceId);
                cancellable.cancel();
                dataSourceSchedulars.remove(stopDataSourceId);

            if (cancellable.isCancelled()) {
                LOG.info("Cancelled Data Source ");
            }
            } else {
                LOG.info("No data source schedular for " + stopDataSourceId);
            }
            DataCache.updateRegisteredSources();
            DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
            dataSourceManagementDAO.disableDataSource(stopDataSourceId);

            //Delete files
            File file = new File(Config.getProperty("tempDir") + "ds/" + stopDataSourceId + ".json");
            if (file.exists()) {
                file.delete();
            }
            file = new File(Config.getProperty("tempDir") + "ds" + stopDataSourceId);

            if (file.exists()) {
                file.delete();
            }

        }
    }

    /**
     * This class is used by the DataSourceSchedualar to stop the datasource by removing the schedular and updating DB.
     */
    public static class StopDataSource {

        private int id;

        public StopDataSource(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * This class is used by the DataSourceSchedualar to start the datasource by adding the schedular and updating DB.
     */
    public static class StartDataSource {

        private int id;

        public StartDataSource(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
