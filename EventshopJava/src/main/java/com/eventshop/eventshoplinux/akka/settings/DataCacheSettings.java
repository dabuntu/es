package com.eventshop.eventshoplinux.akka.settings;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * Created by nandhiniv on 6/4/15.
 */

public class DataCacheSettings {

    private final CacheSettings dataSource;
    private final CacheSettings querySource;


    public DataCacheSettings(ConfigObject configObject) {
        Config config = configObject.toConfig();

        this.dataSource = new CacheSettings(config.getObject("dataSource"));
        this.querySource = new CacheSettings(config.getObject("querySource"));
    }

    public CacheSettings getDataSource() {
        return dataSource;
    }

    public CacheSettings getQuerySource() {
        return querySource;
    }


    public static class CacheSettings {
        private final FiniteDuration refreshInterval;

        public CacheSettings(ConfigObject configObject) {
            Config config = configObject.toConfig();

            Duration duration = FiniteDuration.create(config.getString("refreshInterval"));
            this.refreshInterval = FiniteDuration.create(duration.length(), duration.unit());
        }

        public FiniteDuration getRefreshInterval() {
            return refreshInterval;
        }
    }
}

