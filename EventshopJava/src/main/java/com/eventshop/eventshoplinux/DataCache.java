package com.eventshop.eventshoplinux;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nandhiniv on 6/17/15.
 */
public class DataCache {

    private final static Logger log = LoggerFactory.getLogger(DataCache.class);
    public static HashMap<String, DataSource> registeredDataSources = new HashMap<String, DataSource>();
    public static HashMap<Integer, DataSource> twitterSources = new HashMap<Integer, DataSource>();

    public static void updateRegisteredSources() {
        registeredDataSources.clear();
        DataSourceManagementDAO dsDao = new DataSourceManagementDAO();
        ArrayList<String> keys = dsDao.getAllDsIds();
        for (String key : keys) {
            int dsId = Integer.parseInt(key);
            DataSource ds = dsDao.getDataSource(dsId);
            registeredDataSources.put(key, ds);
        }
        log.info("Updated all the data sources in cache.");
        getTwitterSources();
    }

    private static HashMap<Integer, DataSource> getTwitterSources() {
        twitterSources.clear();
        for (Map.Entry<String, DataSource> entry : registeredDataSources.entrySet()) {
            int dsId = Integer.parseInt(entry.getKey());
            DataSource ds = entry.getValue();
            if (ds.getUrl().contains("www.twitter.com")) {
                twitterSources.put(dsId, ds);
                log.debug("dsID is " + dsId + "and bagOFWords are " + ds.getBagOfWords().toString());
            }
        }
        return twitterSources;
    }

}
