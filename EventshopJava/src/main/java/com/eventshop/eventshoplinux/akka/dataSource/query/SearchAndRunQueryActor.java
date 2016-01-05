package com.eventshop.eventshoplinux.akka.dataSource.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.camel.Ack;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.akka.query.MasterQueryActor;
import com.eventshop.eventshoplinux.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nandhiniv on 6/18/15.
 */

/**
 * This class is used to get the list of enabled Queries and execute them.
 * It is used to send a message from a Camel Endpoint
 */
public class SearchAndRunQueryActor extends UntypedConsumerActor {

    private final static Logger LOG = LoggerFactory.getLogger(SearchAndRunQueryActor.class);

    private QueryListDAO queryListDAO = new QueryListDAO();
    private ActorRef masterQueryActor;

    SearchAndRunQueryActor(ActorRef masterQueryActor) {
        this.masterQueryActor = masterQueryActor;
    }

    /**
     * Used to create the SearchAndRunQueryActor
     *
     * @param masterQueryActor
     * @return
     */
    public static Props props(final ActorRef masterQueryActor) {
        return Props.create(new Creator<SearchAndRunQueryActor>() {
            @Override
            public SearchAndRunQueryActor create() throws Exception {
                return new SearchAndRunQueryActor(masterQueryActor);
            }
        });
    }

    /**
     * Camel endpoint configured for the actor
     *
     * @return
     */
    public String getEndpointUri() {
        return "direct:searchAndRunQuery";
    }

    @Override
    public boolean autoAck() {
        return false;
    }

    /**
     * Used to get the list of enabled queries from DB and execute them.
     * @param message, of type CamelMessage
     */
    public void onReceive(Object message) {
        LOG.info("In SearchAndRunQuery Actor");
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            int dsID = camelMessage.getHeaderAs("dsID", Integer.class, getCamelContext());
            getEnabledQueriesWithDs(dsID);
            getSender().tell(Ack.getInstance(), getSelf());


        }
    }

    /**
     * Get the list of enabled queries from DB.
     * @param dsID
     */
    private void getEnabledQueriesWithDs(int dsID) {
        List<Query> enabledQueryList = queryListDAO.getEnabledQueriesWithDS(dsID);
        for (Query query : enabledQueryList) {
            LOG.info("In SearchAndRunQuery Actor" + query.getQuery_id());
            masterQueryActor.tell(new MasterQueryActor.ExecuteQuery(query.getQuery_id()), getSelf());
        }
    }
}
