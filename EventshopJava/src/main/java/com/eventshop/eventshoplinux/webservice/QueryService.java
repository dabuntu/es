package com.eventshop.eventshoplinux.webservice;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.eventshop.eventshoplinux.DAO.query.QueryDao;
import com.eventshop.eventshoplinux.akka.query.MainQueryActor;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.model.Query;
import com.eventshop.eventshoplinux.servlets.ReturnEmage;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by abhisekmohanty on 5/8/15.
 */

@Path("/queryService")
public class QueryService {

    @Context
    private ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createQuery")
    public String createQuery(Query query) {
        QueryDao queryDao = new QueryDao();
        int id = queryDao.registerQuery(query);
        return String.valueOf(id);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteQuery")
    public String deletQuery(Query query) {
        QueryDao queryDao = new QueryDao();
        boolean enabled = queryDao.getQueryStatus(query.getQuery_id());
        if (enabled)
            disableQuery(query);
        String result = queryDao.deleteQuery(query.getQuery_id());
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/enableQuery")
    public String enableQuery(Query query) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        ActorSelection mainQueryActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/mainQueryActor");
        mainQueryActor.tell(new MainQueryActor.EnableAndRunQuery((query.getQuery_id())), null);
        return "Enabled Query with ID " + query.getQuery_id();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/disableQuery")
    public String disableQuery(Query query) {
        ActorSystem actorSystem = (ActorSystem) context.getAttribute("AkkaActorSystem");
        ActorSelection mainQueryActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/mainQueryActor");
        mainQueryActor.tell(new MainQueryActor.DisableQuery((query.getQuery_id())), null);
        return "Disabled Query with ID " + query.getQuery_id();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getQueryEmage")
    public Response getQueryEmage(Query query) {
        QueryDao queryDao = new QueryDao();
        Emage emage = queryDao.getQueryEmage(query.getQuery_id());
        if (emage != null) {
            return Response.ok(emage, MediaType.APPLICATION_JSON).build();
        }else {
            return Response.status(Response.Status.NO_CONTENT).entity("Emage not found for QueryID: " + query.getQuery_id()).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getQueryEmageLayer")
    public Response getQueryEmage(@QueryParam("sourceId") String sourceId, @QueryParam("layerId") int layerId) {
        ReturnEmage re = new ReturnEmage();
        Emage emage = re.getEmage(sourceId,layerId);
        if (emage != null) {
            return Response.ok(emage, MediaType.APPLICATION_JSON).build();
        }else {
            return Response.status(Response.Status.NO_CONTENT).entity("Emage not found for sourceID: " +sourceId).build();
        }

    }


}
