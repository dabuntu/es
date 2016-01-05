package com.eventshop.eventshoplinux.test;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.servlets.RegisterServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataSourceTest {
	protected static Log log = LogFactory.getLog(STMerger.class);

	public static void main(String[] args) {
		log.info("test data source");
		RegisterServlet myServlet = new RegisterServlet();
		DataSourceManagementDAO datasourceDAO = new DataSourceManagementDAO();
		// String context = Config.getProperty("context");
		// int[] dsIDList = {350, 357, 361, 382, 383, 384, 385, 386}; // Pollen,
		// AQI, Tweets(Asthma)
		int[] dsIDList = { 350, 357, 361 }; // Pollen, AQI, Tweets(Asthma)
		// int[] dsIDList = {382, 383, 386}; // Pollen, AQI, Tweets(Asthma)

		for (int i = 0; i < dsIDList.length; i++) {
			int dsID = dsIDList[i];
			DataSource dataSource = datasourceDAO.getDataSource(dsID);
			if (dataSource != null) {
//				myServlet.startDataProcess(dataSource);
				// start data process and add to the list
				dataSource.setControl(1);
				// try {
				// Thread.sleep(2*dataSource.initParam.timeWindow);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// } // so that the file gets generated before we push back to
				// UI or do you think this wont happen -- sanjukta
			} else {
				System.exit(-1);
			}
		}

		// Test Servlet
		// RegisterServlet myServlet = new RegisterServlet();
		// System.out.println(myServlet.getServletConfig());
		//
		// System.exit(0);

		// long timeWindow = 1000*60*60*24*7; // the last 7 days
		// long sync = 1000;
		// double latUnit = 0.1;
		// double longUnit = 0.1;
		// double swLat = 24;
		// double swLong = -125;
		// double neLat = 50;
		// double neLong = -45;
		// FrameParameters fp = new FrameParameters(timeWindow, sync,
		// latUnit,longUnit,
		// swLat,swLong , neLat, neLong);
		// String theme = "test";
		//
		/*
		 * String dsJsonPath = Config.getProperty("tempDir") + "ds/";
		 * //Config.getProperty("context") + "temp/"; String tempDir =
		 * Config.getProperty("tempDir");
		 * 
		 * String url = "http://www.twitter.com"; FrameParameters fp = new
		 * FrameParameters(6*3600*1000, 0, 2, 2, 24, -125, 50, -66);//CHANGED TO
		 * 6 MINUTES for testing...change back !!!!***** FrameParameters
		 * fpFinal= new FrameParameters(5*60*1000, 0, 1, 1, 24, -125, 50, -66);
		 * 
		 * FrameParameters highRes = new FrameParameters(1*60*1000, 0, 0.01,
		 * 0.01, 24, -125, 50, -66); // Every one minute FrameParameters lowRes
		 * = new FrameParameters(1*60*1000, 0, 2, 2, 24, -125, 50, -66); //
		 * Every one minute FrameParameters testRes = new
		 * FrameParameters(1*60*1000, 0, 2, 2, 20, 30, 25, 35);
		 * 
		 * Double x = Double.parseDouble("1"); System.out.println("x: " + x);
		 * String theme = "test";
		 * 
		 * // Testing CSVEmageIterator String tempFile =
		 * "http://eventshop.ics.uci.edu:8004/sln/datasource/csv_array_usa";
		 * //String tempFile =
		 * "file:////home/ingwei/spongpai/temp/ds/csvarray.txt";
		 * //FrameParameters fpArray = new FrameParameters(1*60*1000, 0, 1, 1,
		 * 20, 20, 28, 28); // fp for array of data 4x4 matrix size
		 * CSVEmageIterator itArray = new CSVEmageIterator(lowRes, theme+"c2f",
		 * tempFile); // Test coarse to fine // If the number of rows/columns
		 * are not match with the emage rows/columns, the resolution wrapper is
		 * required.
		 * itArray.setResolutionWrapper(SpatialWrapper.linear_interpolation);
		 * itArray.setResolutionWrapper(SpatialWrapper.repeat); DataProcess
		 * dpArray = new DataProcess(null, itArray, null, tempDir + "ds03_" +
		 * theme+"c2f", dsJsonPath + "ds03_" + theme + "c2f", "ds03"); new
		 * Thread(dpArray).start();
		 * 
		 * /* //FrameParameters fpAqdm = new FrameParameters(1*60*1000, 0, 1, 1,
		 * 30, -125, 40, -113); // fp for array of data 4x4 matrix size
		 * FrameParameters fpAqdm = new FrameParameters(1*60*1000, 0, 1, 1, 24,
		 * -125, 50, -66); // fp for array of data 4x4 matrix size //String aqdm
		 * = "file:////home/ingwei/spongpai/temp/ds/aqdm"; String aqdm =
		 * "file://///home/ing/eventshop/temp/aqdm"; //String aqdm =
		 * "http://eventshop.ics.uci.edu:8080/sln/datasource/adqm.txt";
		 * CSVFieldEmageIterator itAvgIndex = new CSVFieldEmageIterator(fpAqdm,
		 * theme, aqdm); itAvgIndex.setLatLongValIndex(11, 12, 7);
		 * itAvgIndex.setResolutionWrapper(DatasourceType.point,
		 * SpatialWrapper.sum); DataProcess processFieldAvg = new
		 * DataProcess(null, itAvgIndex, null, tempDir + "ds04_avg_" + theme,
		 * dsJsonPath + "ds04_avg", "ds04_avg"); new
		 * Thread(processFieldAvg).start();
		 */

		/*
		 * this class is working fine as of 02/06/2014 String tempFile =
		 * "http://eventshop.ics.uci.edu:8080/sln/datasource/csvfield";
		 * CSVFieldEmageIterator itTest = new CSVFieldEmageIterator(testRes,
		 * theme, tempFile); DataProcess processTestField = new
		 * DataProcess(null, itTest, null, tempDir + "ds00_" + theme, dsJsonPath
		 * + "ds00"); new Thread(processTestField).start();
		 * 
		 * CSVFieldEmageIterator itAvg = new CSVFieldEmageIterator(testRes,
		 * theme, tempFile); itAvg.setResolutionWrapper(DatasourceType.point,
		 * SpatialWrapper.average); DataProcess processFieldAvg = new
		 * DataProcess(null, itAvg, null, tempDir + "ds01_avg_" + theme,
		 * dsJsonPath + "ds01_avg"); new Thread(processFieldAvg).start();
		 * 
		 * tempFile =
		 * "http://eventshop.ics.uci.edu:8080/sln/datasource/csvfieldDup";
		 * CSVFieldEmageIterator itSum = new CSVFieldEmageIterator(testRes,
		 * theme, tempFile); itSum.setResolutionWrapper(DatasourceType.point,
		 * SpatialWrapper.most_freq); DataProcess processFieldSum = new
		 * DataProcess(null, itSum, null, tempDir + "ds01_most_" + theme,
		 * dsJsonPath + "ds01_most"); new Thread(processFieldSum).start();
		 * 
		 * CSVFieldEmageIterator itMaj = new CSVFieldEmageIterator(testRes,
		 * theme, tempFile); itSum.setResolutionWrapper(DatasourceType.point,
		 * SpatialWrapper.majority); DataProcess processFieldMaj = new
		 * DataProcess(null, itMaj, null, tempDir + "ds01_most_" + theme,
		 * dsJsonPath + "ds01_most"); new Thread(processFieldMaj).start();
		 */

		/*
		 * This class is working fine as of 02/06/2014 TwitterWrapper
		 * wrapperAsthma = new TwitterWrapper(url, "Asthma", fp, true);
		 * wrapperAsthma.setBagOfWords(new String[]{"asthma", "allergy"});
		 * wrapperAsthma.setMaskFile(tempDir + "visual/population.txt");
		 * STTEmageIterator EIterAsthma = new STTEmageIterator();
		 * EIterAsthma.setSTTPointIterator(wrapperAsthma); DataProcess
		 * processAsthma = new DataProcess(null, EIterAsthma, wrapperAsthma,
		 * tempDir + "ds02_Twitter-Asthma", dsJsonPath + "ds02"); new
		 * Thread(processAsthma).start();
		 */

		// FrameParameters fpCrime = new FrameParameters(1*5*60*1000, 0, 0.1,
		// 0.1, 24, -125, 50, -66);
		// String crimeuUrl00 =
		// "http://localhost:8282/test/crimereport_la_00.csv";
		// CSVFieldEmageIterator iterCrime00 = new CSVFieldEmageIterator(fpCrime
		// , "crimereport00", crimeuUrl00);
		// STMerger mergerCrime00 = new STMerger(fpFinal);
		// mergerCrime00.addIterator(iterCrime00, SpatialMapper.sum,
		// TemporalMapper.repeat);
		// mergerCrime00.setMergingExpression("mulED(R0,1)");
		// DataProcess processSheltersSandy = new DataProcess(mergerCrime00,
		// iterCrime00, null, tempDir + "ds14_CSV-Crime-Report-00",
		// imgBasePath+"ds14" );
		// new Thread(processSheltersSandy).start();
		//
		// // 8. Twitter Asthma
		// TwitterWrapper wrapperAsthma = new TwitterWrapper(url, "Asthma", fp,
		// true);
		// wrapperAsthma.setBagOfWords(new String[]{"asthma", "allergy"});
		// STTEmageIterator EIterAsthma = new STTEmageIterator();
		// EIterAsthma.setSTTPointIterator(wrapperAsthma);
		//
		// STMerger mergerAsthma = new STMerger(fpFinal);
		// mergerAsthma.addIterator(EIterAsthma, SpatialMapper.repeat,
		// TemporalMapper.repeat);
		// mergerAsthma.setMergingExpression("mulED(R0,1)");
		// DataProcess processAsthma = new DataProcess(mergerAsthma,
		// EIterAsthma, wrapperAsthma, tempDir + "ds7_Twitter-Asthma",
		// imgBasePath + "ds7");
		// new Thread(processAsthma).start();

		// try{
		// Emage e1 = new Emage(fp, theme);
		// CSVFieldEmageIterator csvEit = new CSVFieldEmageIterator(fp, theme,
		// "");
		// csvEit.run();
		// } catch (Exception e){
		// e.printStackTrace();
		// }
	}
}
