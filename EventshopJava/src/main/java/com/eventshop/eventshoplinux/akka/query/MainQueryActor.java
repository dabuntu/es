package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by abhisekmohanty on 29/6/15.
 */
public class MainQueryActor extends UntypedActor{

    private final static Logger LOG = LoggerFactory.getLogger(MainQueryActor.class);


    private QueryListDAO queryListDAO = new QueryListDAO();

    private ActorRef masterQueryActor;

    MainQueryActor(ActorRef masterQueryActor) {
        this.masterQueryActor = masterQueryActor;
    }

    public static Props props(final ActorRef masterQueryActor) {
        return Props.create(new Creator<MainQueryActor>() {
            @Override
            public MainQueryActor create() throws Exception {
                return new MainQueryActor(masterQueryActor);
            }
        });
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof EnableAndRunQuery) {
            EnableAndRunQuery enableAndRunQuery = (EnableAndRunQuery) message;
            int qID = enableAndRunQuery.getId();
            //Update status of query in DB
            queryListDAO.enableQuery(qID);

            //Send message to master actor
            LOG.info("Enable and run Query : Sending to MasterQueryActor");
            masterQueryActor.tell(new MasterQueryActor.ExecuteQuery(qID), getSelf());
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

    public static class EnableAndRunQuery {

        private int id;

        public EnableAndRunQuery(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

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
