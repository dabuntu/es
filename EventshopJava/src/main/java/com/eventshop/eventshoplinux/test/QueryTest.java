package com.eventshop.eventshoplinux.test;

import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.servlets.QueryJSONParser;
import com.eventshop.eventshoplinux.servlets.QueryProcess;
import com.eventshop.eventshoplinux.servlets.RegisterServlet;

public class QueryTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// FrameParameters fpAqdm = new FrameParameters(1*60*1000, 0, 2, 2, 24,
		// -125, 50, -66); // fp for array of data 4x4 matrix size
		// String queryText =
		// "[{\"qID\":16,\"type\":\"filter_ds6\",\"query\":{\"dataSrcID\":\"ds04\",\"maskMethod\":\"map\",\"coords\":[24,-125,50,-66],\"placename\":\"New York City\",\"filePath\":\"/home/sln/ESProjects/es-auge/Temp/q0_filterFile\",\"valRange\":[\"-99999999\",\"99999999\"],\"timeRange\":[\"0\",\"9223372036854775807\"],\"normMode\":true,\"normVals\":[\"0\",\"100\"]}}]";
		// String qStr =
		// "[{\"qID\":1,\"patternType\":\"filter\",\"dataSources\":[\"ds04\"],\"maskMethod\":\"\",\"coords\":[24,-125,50,-66],\"placename\":\"New York City\",\"filePath\":\"//home/ing/eventshop/temp/ds04_avg_test\",\"valRange\":[\"0\",\"100\"],\"timeRange\":[1,1],\"normMode\":\"true\",\"normVals\":[\"0\",\"100\"],\"queryName\":\"Test2\","
		// +
		// "\"timeWindow\":\"60000\",\"latitudeUnit\":\"2\",\"longitudeUnit\":\"2\",\"queryStatus\":\"S\",\"qryCreatorId\":\"77\",\"boundingBox\":\"-125,50,24,-66\"}]";
		// String queryTree =
		// "[{\"qID\":1,\"patternType\":\"filter\",\"dataSources\":[\"ds346\"],\"maskMethod\":\"\",\"coords\":[23.563987128451217,-127.61718750000001,49.439556958940855,-61.083984375],\"placename\":\"New York City\",\"filePath\":\"\",\"valRange\":[-99999,99999],\"timeRange\":[1,1],\"normMode\":\"true\",\"normVals\":[0,100],\"queryName\":\"TestFilter\",\"timeWindow\":300,\"latitudeUnit\":2,\"longitudeUnit\":2,\"queryStatus\":\"S\",\"qryCreatorId\":\"77\",\"boundingBox\":\"-125,50,24,-66\"}]";
		// List<String> qList = new ArrayList();
		// qList.add(queryTree);
		Long tw = 60000L;
		RegisterServlet servlet = new RegisterServlet();
		QueryJSONParser parser = new QueryJSONParser(servlet);
		QueryListDAO qdao = new QueryListDAO();
		int qMasterId = 21;
		QueryProcess qp = parser.parseQuery(qMasterId,
				qdao.getQueryTree(qMasterId),
				qdao.getFrameParameterQry(qMasterId));
		new Thread(qp).start();
		// then, wait for the result to be generated
		try {
			Thread.sleep(tw.intValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Thread.sleep(1);
	}

}
