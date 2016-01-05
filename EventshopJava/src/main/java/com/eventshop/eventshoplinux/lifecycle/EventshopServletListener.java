package com.eventshop.eventshoplinux.lifecycle;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.camel.CamelExtension;
import com.eventshop.eventshoplinux.akka.AlertRouteProducer;
import com.eventshop.eventshoplinux.akka.dataSource.DataSourceSchedular;
import com.eventshop.eventshoplinux.akka.dataSource.TwitterActor;
import com.eventshop.eventshoplinux.akka.dataSource.query.SearchAndRunQueryActor;
import com.eventshop.eventshoplinux.akka.query.*;
import com.eventshop.eventshoplinux.akka.settings.DataCacheSettings;
import com.eventshop.eventshoplinux.akka.settings.schedular.DataProcessRouteProducer;
import com.eventshop.eventshoplinux.akka.settings.schedular.QueryProcessRouteProducer;
import com.eventshop.eventshoplinux.camel.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.TimeUnit;


/**
 * Created by nandhiniv on 5/26/15.
 */

/**
 * This class is loaded after the Servlet Context is loaded. The akka actor system is initialized and all the actors are
 * created and the camel routes are initialized and added to Akka's Camel Context.
 */
public class EventshopServletListener implements ServletContextListener {

    private final static Logger LOG = LoggerFactory.getLogger(EventshopServletListener.class);

    // Static import of OpenCV
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    ActorSystem actorSystem;

    /**
     * Actor System is initialized, actors are created and the references are maintained and the camel routes are added
     * to the Akka's Camel context.
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        LOG.debug("Servlet Context Loaded");

        Config config = ConfigFactory.load();
        actorSystem = ActorSystem.create("eventshop-actorSystem", config);
        final DataCacheSettings schedulingSettings = new DataCacheSettings(config.getObject("dataCache"));

        servletContextEvent.getServletContext().setAttribute("AkkaActorSystem", actorSystem);

        try {
            CamelExtension.get(actorSystem).context().addRoutes(new DataFormatRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new DataSourceRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new AlertRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new CsvRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new EmageRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new JsonRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new KafkaToMongoRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new MongoRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new PopulateKafkaRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new QueryRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new TwitterRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new XmlRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new VisualRoute());
            CamelExtension.get(actorSystem).context().addRoutes(new RuleRoute());


        } catch (Exception e) {
            e.printStackTrace();
        }
        final ActorRef alertRouteProducer = actorSystem.actorOf(Props.create(AlertRouteProducer.class));

        final ActorRef mongoQueryRouteProducerActor = actorSystem.actorOf(Props.create(MongoQueryRouteProducerActor.class));
        final ActorRef ruleRouteProducerActor = actorSystem.actorOf(Props.create(RuleRouteProducerActor.class));


        // New Actors
        final ActorRef groupingQueryActor = actorSystem.actorOf(Props.create(GroupingQueryActor.class));
        final ActorRef filterQueryActor = actorSystem.actorOf(FilterQueryActor.props(mongoQueryRouteProducerActor));
        final ActorRef spatialCharQueryActor = actorSystem.actorOf(Props.create(SpatialCharQueryActor.class));
        final ActorRef spatialPatternQueryActor = actorSystem.actorOf(Props.create(SpatialPatternQueryActor.class));
        final ActorRef aggregationQueryActor = actorSystem.actorOf(Props.create(AggregationQueryActor.class));
        final ActorRef temporalCharQueryActor = actorSystem.actorOf(TemporalCharQueryActor.props(mongoQueryRouteProducerActor));
        final ActorRef temporalPatternQueryActor = actorSystem.actorOf(TemporalPatternQueryActor.props(mongoQueryRouteProducerActor));
        
        final ActorRef masterQueryActor = actorSystem.actorOf(MasterQueryActor.props(filterQueryActor, groupingQueryActor
                , spatialCharQueryActor, spatialPatternQueryActor, aggregationQueryActor, temporalCharQueryActor
                , mongoQueryRouteProducerActor, temporalPatternQueryActor, ruleRouteProducerActor, alertRouteProducer));
        final ActorRef queryProcessRouteProducer = actorSystem.actorOf(Props.create(QueryProcessRouteProducer.class));
        final ActorRef dataProcessRouteProducer = actorSystem.actorOf(Props.create(DataProcessRouteProducer.class));
        final ActorRef twitterActor = actorSystem.actorOf(Props.create(TwitterActor.class));
        final ActorRef mainQueryActor = actorSystem.actorOf(MainQueryActor.props(masterQueryActor), "mainQueryActor");

        final ActorRef dataSourceSchedularActor = actorSystem.actorOf(DataSourceSchedular.props(
                dataProcessRouteProducer, twitterActor)
                , "dataSourceSchedularActor");

        final ActorRef searchAndRunQueryActor = actorSystem.actorOf(SearchAndRunQueryActor.props(masterQueryActor));


    }

    /**
     * This method is called when the Servlet context shuts down. Shutdown hook is registered for the Akka actor system.
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    actorSystem.shutdown();
                    actorSystem.awaitTermination(Duration.create(10, TimeUnit.SECONDS));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
