package com.eventshop.eventshoplinux.DAO.rule;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.rule.DSRuleElement;
import com.eventshop.eventshoplinux.model.RuleOperator;
import com.eventshop.eventshoplinux.model.RuleResponse;
import com.eventshop.eventshoplinux.ruleEngine.EventshopUtils;
import com.eventshop.eventshoplinux.ruleEngine.Rule;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.eventshop.eventshoplinux.constant.Constant.*;

/**
 * Created by aravindh on 8/25/15.
 */
public class RuleDao extends BaseDAO {

    private final static Logger LOGGER = LoggerFactory.getLogger(RuleDao.class);
    public static void main(String args[]) {
        RuleDao rd = new RuleDao();
        User user = new User();
        user.setId(78);
    rd.getAllRules(user);
    }

    public int registerRule(Rules rules) {
        int key = 0;

        try {
            PreparedStatement ps = con.prepareStatement(INSERT_RULE_QRY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, rules.getRuleName());
            ps.setString(2, rules.getSource());
            JsonArray ruleArray = new JsonArray();
            List<Rule> rule = rules.getRules();
            for (Rule rul : rule) {
                JsonObject ruleObj = new JsonObject();
                ruleObj.addProperty("dataField", rul.getDataField());
                ruleObj.addProperty("ruleOperator", rul.getRuleOperator());
                ruleObj.addProperty("ruleParameters", rul.getRuleParameters());
                ruleArray.add(ruleObj);
            }
            ps.setString(3, "{\"rules\":"+ruleArray.toString()+"}");
            ps.setString(4, rules.getExtractFields());
            ps.setInt(5,rules.getUserId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                // Retrieve the auto generated key(s).
                key = rs.getInt(1);
            }
            return key;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int registerDefaultRule(DataSource ds) {
        int key = 0;

        Rules rules = new Rules();
        rules.setRuleName(DEFAULT);
        rules.setSource("ds"+ds.getSrcID());
        rules.setUserId(Integer.parseInt(ds.getUserId()));
        rules.setExtractFields(DEFAULT_EXTRACT_FIELD);

        //Sample Rule Insert {"source":"ds42","extractFields":"loc,theme,value,timestamp,","rules":[{"dataField":"loc","ruleOperator":"coordinates","ruleParameters":"33,33,33,33"}]}

        try{
            PreparedStatement ps = con.prepareStatement(INSERT_DEFAULT_RULE_QRY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,ds.getSrcID()+"_"+rules.getRuleName());
            String ruleQuery="{\"rules\":[{\"ruleOperator\":\"coordinates\",\"dataField\":\"loc\",\"ruleParameters\":\""+ds.getInitParam().getSwLat()+","+ds.getInitParam().getSwLong()+","+ds.getInitParam().getNeLat()+","+ds.getInitParam().getNeLong()+"\"}]}";

            ps.setString(2, ruleQuery);
            ps.setString(3,rules.getSource());
            ps.setString(4, rules.getExtractFields());
            ps.setInt(5, rules.getUserId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                // Retrieve the auto generated key(s).
                key = rs.getInt(1);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }

        return key;
    }

    public List<DSRuleElement> getDSRules(User user){
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DSRuleElement> ruleElements = new ArrayList<DSRuleElement>();
        try {
            con.prepareStatement(SELECT_RULE_DS_QRY);
            ps.setInt(1,user.getId());
            rs=ps.executeQuery();
            while(rs.next()){
                DSRuleElement dsRuleElement = new DSRuleElement();
                dsRuleElement.setDataSourceID("ds"+rs.getInt("dsmaster_id"));
                dsRuleElement.setDataSourceName(rs.getString("dsmaster_title"));
                dsRuleElement.setRuleID(rs.getInt("RuleID"));
                dsRuleElement.setRuleName(rs.getString("Rule_Name"));
                ruleElements.add(dsRuleElement);
            }
            return ruleElements;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Rules getRules(int ruleId) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Rules rules = new Rules();

        try {
            ps = con.prepareStatement(SELECT_RULE_QRY);
            ps.setInt(1, ruleId);
            rs = ps.executeQuery();

            String sourceID="";
            String ruleString="";
            String sourceFields="";
            String ruleName="";
            int userId=0;
            if (rs.next()) {
                ruleId=rs.getInt("RuleID");
                ruleName= rs.getString(RULE_NAME);
                sourceID = rs.getString(SOURCE_ID);
                ruleString = rs.getString(RULE_QUERIES);
                sourceFields = rs.getString(SOURCE_FIELDS);
                userId=rs.getInt("user_id");
                LOGGER.debug(ruleString);

            }
            rules.setRuleID(ruleId);
            rules.setRuleName(ruleName);
            rules.setSource(sourceID);
            rules.setUserId(userId);
            rules.setExtractFields(sourceFields);
//            objectMapper.convertValue(ruleString, Rule.class);
            List<Rule> ruless = new ArrayList<Rule>();

            JsonObject rulesObj = (new JsonParser()).parse(ruleString).getAsJsonObject();

            JsonArray rulesJson = rulesObj.get("rules").getAsJsonArray();
            for(int i=0;i<rulesJson.size();i++){
                JsonObject tempObj =rulesJson.get(i).getAsJsonObject();
                Rule rule  = new Rule();
                rule.setRuleOperator(tempObj.get("ruleOperator").getAsString());
                rule.setDataField(tempObj.get("dataField").getAsString());
                rule.setRuleParameters(tempObj.get("ruleParameters").getAsString());
                ruless.add(rule);
            }

            rules.setRules(ruless);
            //rulesObj.get("")
            //{"source":"ds59","rules":[{"ruleOperator":"coordinates","dataField":"loc","ruleParameters":"-120.2,40.1,-118.2,32.4"},{"ruleOperator":">","dataField":"value","ruleParameters":"100"}],"extractFields":"loc,value,timestamp"}
            LOGGER.debug(rules.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return rules;

    }

    public int updateRule(Rules rules, int ruleID) {
        int key = 0;

        try {
            PreparedStatement ps = con.prepareStatement(UPDATE_RULE_QRY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, rules.getRuleName());
            ps.setString(2, rules.getSource());
            JsonArray ruleArray = new JsonArray();
            List<Rule> rule = rules.getRules();
            for (Rule rul : rule) {
                JsonObject ruleObj = new JsonObject();
                ruleObj.addProperty("dataField", rul.getDataField());
                ruleObj.addProperty("ruleOperator", rul.getRuleOperator());
                ruleObj.addProperty("ruleParameters", rul.getRuleParameters());
                ruleArray.add(ruleObj);
            }
            ps.setString(3, "{\"rules\":"+ruleArray.toString()+"}");
            ps.setString(4, rules.getExtractFields());
            ps.setInt(5, ruleID);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                // Retrieve the auto generated key(s).
                key = rs.getInt(1);
            }
            return key;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }




    public List<RuleOperator> getAllEnabledRuleOperators() {

//        PreparedStatement ps = null;
//        ResultSet rs = null;

        List<RuleOperator> ruleOperatorList = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(SELECT_ENABLED_RULE_OPERATOR_QRY);
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                RuleOperator ruleOperator = new RuleOperator();

                ruleOperator.setId(rs.getInt("Operator_id"));
                ruleOperator.setDataType(rs.getString("DataType"));
                ruleOperator.setOperator(rs.getString("Operators"));
                ruleOperator.setEnabled(rs.getInt("status"));

                ruleOperatorList.add(ruleOperator);

            }
            return ruleOperatorList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ruleOperatorList;
    }

    public List<RuleResponse> getAllDataSourceWithSyntaxAndOperators() {
        DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
        ArrayList<String> allDataSource = dataSourceManagementDAO.getAllDsIds();
        List<RuleResponse> ruleResponseList = new ArrayList<>();
        for (String dataSource : allDataSource) {
            String syntax = dataSourceManagementDAO.getDataSource(Integer.parseInt(dataSource)).getSyntax();
            Map<String, Object> result = EventshopUtils.getSyntaxAndOperators(syntax);

            RuleResponse ruleResponse = new RuleResponse(dataSource, result);
            ruleResponseList.add(ruleResponse);
        }

        return ruleResponseList;

    }

    public List<String> getAllRuleIds(){
        List<String> ruleIds = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<RuleOperator> ruleOperatorList = new ArrayList<>();
        try {
            ps = con.prepareStatement(SELECT_ALL_RULE_ID_QRY);
            rs = ps.executeQuery();

            String sourceId;
            String ruleId;
            while (rs.next()) {

                sourceId=rs.getString(RULE_ID);
                ruleId=rs.getString(SOURCE_ID);
                ruleIds.add(sourceId+":"+ruleId);
            }
            return ruleIds;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Rules> getAllRules(User user) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Rules> rulesList = new ArrayList<>();

        try {
            ps = con.prepareStatement(SELECT_ALL_RULE_QRY);
            ps.setInt(1,user.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                Rules rules = new Rules();
                int ruleID;
                String ruleName;
                String sourceID;
                String ruleString;
                String sourceFields;
                int userId;

                ruleID= rs.getInt(RULE_ID);
                ruleName=rs.getString(RULE_NAME);
                if(ruleName == null){
                    ruleName="";
                }
                sourceID = rs.getString(SOURCE_ID);
                ruleString = rs.getString(RULE_QUERIES);
                if (ruleString==null){
                    ruleString="";
                }
                sourceFields = rs.getString(SOURCE_FIELDS);
                if(sourceFields==null){
                    sourceFields="";
                }
                userId=rs.getInt(USER_ID);

                rules.setRuleID(ruleID);
                rules.setRuleName(ruleName);
                rules.setUserId(userId);
                rules.setSource(sourceID);
                rules.setExtractFields(sourceFields);
                List<Rule> ruless = new ArrayList<Rule>();
                if(!ruleString.equals("")){
                    JsonObject rulesObj = (new JsonParser()).parse(ruleString).getAsJsonObject();
                    JsonArray rulesJson = rulesObj.get("rules").getAsJsonArray();
                    for (int i = 0; i < rulesJson.size(); i++) {
                        Rule rule = new Rule();
                        JsonObject tempObj = rulesJson.get(i).getAsJsonObject();
                        rule.setRuleOperator(tempObj.get("ruleOperator").getAsString());
                        rule.setDataField(tempObj.get("dataField").getAsString());
                        rule.setRuleParameters(tempObj.get("ruleParameters").getAsString());
                        ruless.add(rule);
                    }
                }

                rules.setRules(ruless);
                rulesList.add(rules);

            }
            for(int i=0;i<rulesList.size();i++){
                LOGGER.debug(rulesList.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return rulesList;

    }


//    public Rules getRuleById(int ruleID) throws Exception {
//
//        try {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            Rules rules = new Rules();
//            String sourceID = "";
//            String ruleString = "";
//            String sourceFields = "";
//            ps = con.prepareStatement(SELECT_RULE_QRY);
//            ps.setInt(1, ruleID);
//            rs = ps.executeQuery();
//
//            while (rs.next()) {
//                sourceID = rs.getString(SOURCE_ID);
//                ruleString = rs.getString(RULE_QUERIES);
//                sourceFields = rs.getString(SOURCE_FIELDS);
//
//            }
//            rules.setSource(sourceID);
//            rules.setExtractFields(sourceFields);
//            List<Rule> ruless = new ArrayList<Rule>();
//            JsonObject rulesObj = (new JsonParser()).parse(ruleString).getAsJsonObject();
//            JsonArray rulesJson = rulesObj.get("rules").getAsJsonArray();
//            for (int i = 0; i < rulesJson.size(); i++) {
//                JsonObject tempObj = rulesJson.get(i).getAsJsonObject();
//                Rule rule = new Rule();
//                rule.setRuleOperator(tempObj.get("ruleOperator").getAsString());
//                rule.setDataField(tempObj.get("dataField").getAsString());
//                rule.setRuleParameters(tempObj.get("ruleParameters").getAsString());
//                ruless.add(rule);
//            }
//            rules.setRules(ruless);
//
//            return rules;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


}