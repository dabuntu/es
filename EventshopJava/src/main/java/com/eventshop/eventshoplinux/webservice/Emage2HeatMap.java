package com.eventshop.eventshoplinux.webservice;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceDao;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.model.*;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravindh on 11/26/15.
 */
@Path("/Emage2HeatMap")
public class Emage2HeatMap {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getHeatMapData")
    public List<STT> getHeadMapData(Query query) {
        int id = query.getQuery_id();
        ObjectMapper mapper = new ObjectMapper();
        List<STT> sttList = new ArrayList<STT>();

        try {
            Emage emage = mapper.readValue(new File(Config.getProperty("queryJsonLoc")+"Q"+id+".json"), Emage.class);

            double nelat = emage.getNeLat();
            double nelong = emage.getNeLong();
            double swlat= emage.getSwLat();
            double swlong= emage.getSwLong();
            double latUnit = emage.getLatUnit();
            double lonUnit = emage.getLongUnit();
            int cnt=0;
            double value=0, lat=0, lng=0;
             for(double i=nelat;i>swlat;i=i-latUnit){
                for(double j=swlong;j<nelong;j=j+lonUnit){
                    value=emage.getImage()[cnt];
                    lat=i;
                    lng=j;
                    STT stt = new STT();
                    stt.setLoc(new ELocation(lng,lat));
                    stt.setValue(value);
                    sttList.add(stt);
                    cnt++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sttList;
    }

    public static void main(String[] args){
        Query query = new Query();
        query.setQuery_id(36);
        new Emage2HeatMap().getHeadMapData(query);
    }

}
