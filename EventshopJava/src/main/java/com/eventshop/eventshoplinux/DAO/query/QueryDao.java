package com.eventshop.eventshoplinux.DAO.query;


import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.model.Query;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.*;

/**
 * Created by abhisekmohanty on 5/8/15.
 */
public class QueryDao extends BaseDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryDao.class);
    public int registerQuery(Query query) {
        int key = 0;
        JsonParser jsonParser = new JsonParser();
        JsonArray queryArr = jsonParser.parse(query.getQuery_esql()).getAsJsonArray();
        List<String> dataSourceList = new ArrayList<>();

        for (int i = 0; i < queryArr.size(); i++) {

            JsonObject q = queryArr.get(i).getAsJsonObject();
            if (q.get("dataSources") != null) {
                JsonArray sources = q.get("dataSources").getAsJsonArray();
                if (sources != null) {
                    for (int j = 0; j < sources.size(); j++) {
                        String source = sources.get(j).getAsString();
                        if (source.toLowerCase().startsWith("ds")) {
                            if(!dataSourceList.contains(source.toLowerCase())) {
                                dataSourceList.add(source.toLowerCase());
                            }
                        }
                    }
                }
            }
        }
        try {
            PreparedStatement ps = con.prepareStatement(INSERT_QUERY_MASTER_QRY, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, query.getQuery_creator_id());
            ps.setString(2, query.getQuery_name());
            ps.setString(3, query.getQuery_esql());
            ps.setLong(4, query.getTime_window());
            ps.setDouble(5, query.getLatitude_unit());
            ps.setDouble(6, query.getLongitude_unit());
            ps.setString(7, query.getBoundingbox());
            ps.setString(8, dataSourceList.toString());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                // Retrieve the auto generated key(s).
                key = rs.getInt(1);
                LOGGER.debug(""+key);
                return key;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean getQueryStatus(int query_id) {
        try {
            PreparedStatement ps = con.prepareStatement(QUERY_STAT_QRY);
            ps.setInt(1, query_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString(1);
                if (status.equalsIgnoreCase("1"))
                    return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String deleteQuery(int query_id) {
        int deleted = 0;
        try {
            PreparedStatement ps = con.prepareStatement(DELETE_QUERY_MASTER_QRY);
            ps.setInt(1, query_id);
            deleted = ps.executeUpdate();

            if (deleted != 0)
                return "Query deleted successfully";

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Exception in deleting Query";
    }

    public List<Integer> getEnabledQueryIds() {

        try {
            List<Integer> enabledQIds = null;
            PreparedStatement ps = con.prepareStatement("SELECT query_id FROM Query_Master WHERE query_status = 1");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                if (!rs.getString("query_id").isEmpty()) {
                    System.out.println(rs.getInt("query_id"));
                    enabledQIds.add(rs.getInt("query_id"));
                }
            }
            return enabledQIds;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Emage getQueryEmage(int Qid) {
        Emage emage = new Emage();
        String qFilePath = Config.getProperty("tempDir") + "/queries/" + "Q" + Qid + ".json";
		File tempFile = new File(qFilePath);
        if (tempFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();

            try {

                emage = mapper.readValue(new File(qFilePath), Emage.class);
                return emage;

            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }else {
            return null;
        }
        return null;
    }

}