package com.eventshop.eventshoplinux.webservice;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.model.Alert;
import com.eventshop.eventshoplinux.ruleEngine.ApplyRule;
import com.eventshop.eventshoplinux.ruleEngine.Rule;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by aravindh on 8/26/15.
 */
@Path("/rulewebservice")
public class RuleWebService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RuleWebService.class);
    RuleDao ruleDAO = new RuleDao();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rule")
    public int createRule(Rules rule) {

        LOGGER.info("Registering Ruleee: " + rule);
        RuleDao rd = new RuleDao();
        int key = rd.registerRule(rule);
        return key;

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/enableRule/{id}")
    public StringBuffer enableRule(@PathParam(value = "id") final String ruleId) {

        RuleDao dao = new RuleDao();
        int ruleID = new Integer(ruleId).intValue();
        Rules rule = dao.getRules(ruleID);
        ApplyRule applyRule = new ApplyRule();
        StringBuffer result = applyRule.getAppliedRules(rule);
        return result;

    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createAndEnableRule")
    public StringBuffer createAndEnableRule(Rules rule) {

        ApplyRule applyRule = new ApplyRule();
        StringBuffer result = applyRule.getAppliedRules(rule);
        LOGGER.debug(result.toString());
        return result;

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rule/{id}")
    public Rules getRule(@PathParam(value = "id") final String ruleId) {
        LOGGER.debug("Getting details for rule Id: " + ruleId);
        RuleDao dao = new RuleDao();
        int ruleID = new Integer(ruleId).intValue();
        Rules rule = dao.getRules(ruleID);
        LOGGER.debug(rule.toString());
        return rule;
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rule")
    public int updateRule(Rules rule) {

        LOGGER.info("Updating Rule" + rule);

        //{"source":"ds59","rules":[{"ruleOperator":"coordinates","dataField":"loc","ruleParameters":"-120.2,40.1,-118.2,32.4"},{"ruleOperator":">","dataField":"value","ruleParameters":"100"}],"extractFields":"loc,value,timestamp"}
        RuleDao rd = new RuleDao();
        int ruleID = rule.getRuleID();
        int key = rd.updateRule(rule,ruleID);
        return key;

    }

}
