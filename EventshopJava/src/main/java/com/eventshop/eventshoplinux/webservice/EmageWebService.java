package com.eventshop.eventshoplinux.webservice;

import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.model.EmageLayer;
import com.eventshop.eventshoplinux.ruleEngine.ApplyRule;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;

/**
 * Created by aravindh on 10/8/15.
 */
@Path("/emagewebservice")
public class EmageWebService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EmageWebService.class);
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getEmageLayer/")
    public String getEmageLayer(EmageLayer emageLayer) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Emage emage = mapper.readValue(new File(Config.getProperty("datasourceJsonLoc")+emageLayer.getDatasourceId()+"_layer"+emageLayer.getLayer()+".json"), Emage.class);
           // return emage.toString();
            double swlat=emage.getSwLat();
            double swlong =emage.getSwLong();
            double nelat = emage.getNeLat();
            double nelong =emage.getNeLong();
            double latUnit = emage.getLatUnit();
            double longUnit = emage.getLongUnit();
            int count = 0;
            JsonArray jArray = new JsonArray();
            for (double i = swlat; i < nelat; i = i+latUnit) {
                for (double j = swlong; j < nelong; j = j + longUnit) {
                    double val = emage.getImage()[count];
                    double lat = i;
                    double lng = j;
                    JsonObject jObj = new JsonObject();
                    jObj.addProperty("lat", lat);
                    jObj.addProperty("long", lng);
                    jObj.addProperty("val", val);
                    jArray.add(jObj);

                    count++;
                }
            }
            System.out.println(jArray.toString());
            return jArray.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        EmageLayer emageLayer = new EmageLayer();
        emageLayer.setDatasourceId("16");
        emageLayer.setLayer(1);
        EmageWebService emageWebService = new EmageWebService();
        emageWebService.getEmageLayer(emageLayer);
    }

}
