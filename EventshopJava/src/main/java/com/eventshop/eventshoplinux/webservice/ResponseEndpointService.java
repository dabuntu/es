package com.eventshop.eventshoplinux.webservice;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/**
 * Created by abhisekmohanty on 9/6/15.
 */

@Path("/queryRenderServices")
public class ResponseEndpointService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseEndpointService.class);
    @POST
    @Path("/uploadFile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@JsonProperty("image") String imageData) {
        LOGGER.debug("inside uploadFile");
        try {
            JSONObject obj = new JSONObject(imageData);
            imageData = obj.get("image").toString();
           LOGGER.debug("This IS THe Image " + imageData);
            byte[] data = Base64.decodeBase64(imageData);
            OutputStream stream = new FileOutputStream(new File("test.png"));
            stream.write(data);
            stream.close();
            String success = "Image File Sent Successfully";
            return Response.ok(success, MediaType.APPLICATION_JSON).build();
        } catch (JSONException e) {
            e.printStackTrace();
            return Response.serverError().entity(e.toString()).build();
        } catch (IOException ex) {
            ex.printStackTrace();
            return Response.serverError().entity(ex.toString()).build();
        }

    }

}

