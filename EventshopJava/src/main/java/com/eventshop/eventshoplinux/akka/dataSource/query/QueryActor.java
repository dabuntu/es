package com.eventshop.eventshoplinux.akka.dataSource.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.akka.MasterActor;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by nandhiniv on 6/18/15.
 */

/**
 * The QueryActor class enables, runs and disables the query
 */
public class QueryActor extends UntypedActor {

    private final static Logger LOG = LoggerFactory.getLogger(QueryActor.class);
    private final QueryListDAO queryListDAO = new QueryListDAO();

    private ActorRef masterActor;

    QueryActor(ActorRef masterActor) {
        this.masterActor = masterActor;
    }

    public static Props props(final ActorRef masterActor) {
        return Props.create(new Creator<QueryActor>() {
            @Override
            public QueryActor create() throws Exception {
                return new QueryActor(masterActor);
            }
        });
    }

    /**
     * Receives EnableAndRunQuery or DisableQuery as a parameter and enables/executes or disable the query.
     *
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof EnableAndRunQuery) {
            EnableAndRunQuery enableAndRunQuery = (EnableAndRunQuery) message;
            int qID = enableAndRunQuery.getId();
            //Update status of query in DB
            queryListDAO.enableQuery(qID);

            //Send message to master actor
            masterActor.tell(new MasterActor.ExecuteQuery(qID), getSelf());
        } else if (message instanceof DisableQuery) {
            //Disable query in DB
            int masterQueryID = ((DisableQuery) message).getId();
            queryListDAO.disableQuery(masterQueryID);
            // Delete files
            String dirToSearchAndDelete = Config.getProperty("context") + "temp/queries/";
            File file = new File(dirToSearchAndDelete);
            if (file.isDirectory()) {
                String[] subNote = file.list();
                for (String filename : subNote) {
                    if (filename.startsWith("Q" + masterQueryID)) {
                        LOG.info("Deleting file " + filename);
                        new File(dirToSearchAndDelete + filename).delete();
                    }
                }
            }
        }

    }

    /**
     * This class is passed as a message to the Query actor to enable the query in DB and execute it
     */
    public static class EnableAndRunQuery {

        private int id;

        public EnableAndRunQuery(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    /**
     * This class is passed as a message to Query actor to disable the query.
     */
    public static class DisableQuery {

        private int id;

        public DisableQuery(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
