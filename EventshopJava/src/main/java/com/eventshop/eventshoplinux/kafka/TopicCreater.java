package com.eventshop.eventshoplinux.kafka;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;

import java.util.Properties;

/**
 * Created by aravindh on 26/5/15.
 */
public class TopicCreater {

    public boolean topicCreator(String topicName) {
        try{
            Properties topicConfig = new Properties();
            ZkClient zkClient = new ZkClient(Config.getProperty("zkURI"), Integer.parseInt(Config.getProperty("sessionTimeoutMs")), Integer.parseInt(Config.getProperty("connectionTimeoutMs")), ZKStringSerializer$.MODULE$);
            AdminUtils.createTopic(zkClient,topicName,Integer.parseInt(Config.getProperty("numOfPartitions")),Integer.parseInt(Config.getProperty("replicationFactor")), topicConfig);
            return true;
        }catch(Exception ex){
            return false;
        }

    }

}
