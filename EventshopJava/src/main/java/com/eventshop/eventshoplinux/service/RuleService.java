package com.eventshop.eventshoplinux.service;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.rule.DSRuleElement;
import com.eventshop.eventshoplinux.model.RuleResponse;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nandhiniv on 8/27/15.
 */
@Path("uiRuleService")
public class RuleService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RuleService.class);
    RuleDao ruleDao = new RuleDao();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allDSSyntax")
    public String getAllSyntaxAndOperators() throws Exception {

        List<String> finalResult = new ArrayList<>();
        List<RuleResponse> result = ruleDao.getAllDataSourceWithSyntaxAndOperators();
        for (RuleResponse ruleResponse : result) {
            finalResult.add(ruleResponse.toString());
        }
        String test = new Gson().toJson(result);
        test = "{\"result\":" + test + "}";
        LOGGER.debug("test is ::::" +test);
        return test;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allRules")
    public Rules[] getAllRules(@QueryParam("userId") int uId) {

        User user = new User();
        user.setId(uId);
        Rules[] ruleListArray = null;
        List<Rules> ruleList = ruleDao.getAllRules(user);
        ruleListArray = ruleList.toArray(new Rules[ruleList.size()]);
        return ruleListArray;


    }


	/*
	 * This method returns datasource list for Admin/based on userid
	 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getUserDatasourceAndRules")
    public DSRuleElement[] getDataSourceAndRuleList(
            @QueryParam("userId") int userId) {
        User user = new User();
        user.setId(userId);
        RuleDao rd = new RuleDao();
        List<Rules> rulesList = rd.getAllRules(user);
        DSRuleElement[] ruleElements = new DSRuleElement[rulesList.size()];
        int cnt=0;

        for(Rules rules :rulesList ){
            DSRuleElement dsr= new DSRuleElement();
            dsr.setRuleID(rules.getRuleID());
            dsr.setRuleName(rules.getRuleName());
            dsr.setDataSourceID(rules.getSource().replace("ds", ""));
            System.out.println("rules.getSource() :::  " + rules.getSource());
            dsr.setDataSourceName(DataCache.registeredDataSources.get(rules.getSource().substring(2, rules.getSource().length())).getSrcName());
            ruleElements[cnt]=dsr;
            cnt++;
        }
        cnt=0;
        return ruleElements;
    }


    public static void main(String[] args) {
        RuleService rs = new RuleService();
        DSRuleElement[] ruleElements = rs.getDataSourceAndRuleList(78);
        for (DSRuleElement dsRuleElement : ruleElements) {
            LOGGER.debug(dsRuleElement.getDataSourceID() + dsRuleElement.getDataSourceName()
            + dsRuleElement.getRuleID() + dsRuleElement.getRuleName());
            LOGGER.debug(dsRuleElement.toString());
        }
    }

}
