package com.eventshop.eventshoplinux.util.datasourceUtil;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.Message;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.EmageIterator;
import com.eventshop.eventshoplinux.domain.datasource.simulator.DistParameters;
import com.eventshop.eventshoplinux.domain.datasource.simulator.GaussianParameters2D;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.simulator.Simulator.Kernel;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.SimDataWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.TwitterWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.Wrapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Iterator;

//import com.eventshop.eventshoplinux.domain.datasource.DataSourceHelper;

public class DataProcess implements Runnable {
	public boolean isRunning;
	protected Log log = LogFactory.getLog(this.getClass().getName());
	STMerger merger;
	EmageIterator eit;
	Wrapper wrapper;
	String filepath;
	String imgpath; // context+"results"+"dsID.jpg"
	String dsID;
	DB db = CommonUtil.connectMongoDB();

	public DataProcess(STMerger merger, EmageIterator eit, Wrapper wrapper,
			String filepath, String imgpath, String dsID) {
		this.merger = merger;
		this.eit = eit;
		this.wrapper = wrapper;
		this.filepath = filepath;
		this.imgpath = imgpath;
		this.dsID = dsID;
	}

	public static void main(String[] args) {
		String imgBasePath = Config.getProperty("context") + Constant.RESULT_DS;
		String tempDir = Config.getProperty("tempDir");

		FrameParameters fp = new FrameParameters(1 * 6 * 60 * 1000, 0, 2, 2,
				24, -125, 50, -66);// CHANGED TO 6 MINUTES for testing...change
		// back to 60 MINUTES !!!!*****

		// Test TwitterWrapper
		// 99. Twitter-Flu
		String url = Config.getProperty("twtrURL");
		TwitterWrapper wrapper = new TwitterWrapper(url, "Flu", fp, true);
		wrapper.setBagOfWords(new String[]{"Allergy", "Flu"});

		EmageIterator EIter = new EmageIterator();
		EIter.setSTTPointIterator(wrapper);

		DataProcess process = new DataProcess(null, EIter, wrapper, tempDir
				+ "99_Twitter-test", imgBasePath + "99", "99");
		// new Thread(process).start();

		// Test SimDataWrapper
		// 98. Simulator
		String simURL = Config.getProperty("simURL");
		SimDataWrapper simWrapper = new SimDataWrapper(simURL, "Flu", fp);

		// Generate the parameters
		ArrayList<DistParameters> dParams = new ArrayList<DistParameters>();
		GaussianParameters2D gParam = new GaussianParameters2D(28, -118, 3.0,
				3.0, 2);
		dParams.add(gParam);

		// set up the wrapper with appropriate parameters
		simWrapper.setUpSimulator(Kernel.gaussian, dParams);

		EmageIterator EIter2 = new EmageIterator();
		EIter2.setSTTPointIterator(simWrapper);

		DataProcess process2 = new DataProcess(null, EIter2, simWrapper,
				tempDir + "98_Sim", imgBasePath + "98", "98");
		new Thread(process2).start();

		// DataSourceParser dsParser = new DataSourceParser();
		// FrameParameters fp = new FrameParameters();
		// fp.setDefaultValue();
		// fp.setLatUnit(2.0);
		// fp.setLongUnit(2.0);
		// DataSource src0 = new DataSource();
		// src0.srcID = "0";
		// src0.srcName = "Twitter-Obama";
		// src0.srcTheme = "Obama";
		// src0.initParam = fp;
		// src0.url = "www.twitter.com";
		// src0.srcFormat = DataFormat.stream;
		// src0.supportedWrapper = "Twitter";
		// src0.bagOfWords = new ArrayList();
		// src0.bagOfWords.add("happy");
		// src0.bagOfWords.add("joy");
		// DataSourceHelper dsHelper = new DataSourceHelper();
		// dsHelper.startDataProcess(src0, "R");
		// src0.finalParam = fp;

		// String strSim =
		// "{\"srcID\":14,\"source\":"
		// + "{\"theme\":\"Asthma\","
		// + "\"name\":\"Twitter-Asthma7\","
		// + "\"url\":\"www.twitter.com\","
		// + "\"format\":\"stream\",\"supportedWrapper\":\"sim\","
		// + "\"saveTweets\":true,"
		// + "\"bagOfWords\":[\"\"],"
		// + "\"visualParam\":null,"
		// + "\"archiveParam\":null,"
		// +
		// "\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\","
		// +
		// "\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},"
		// +
		// "\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\","
		// +
		// "\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// servlet.startDataProcess(servlet.parseDataSource(strSim));

		// String strDb =
		// "{\"srcID\":9,\"source\":{\"theme\":\"Asthma\",\"name\":\"election\",\"url\":\"www.twitter.com\",\"format\":\"archive\",\"supportedWrapper\":\"raw\",\"bagOfWords\":[\"123\"],\"visualParam\":null,\"archiveParam\":{\"beginTime\":1328170721637,\"endTime\":1328257121637,\"refreshRate\":\"60\"},"
		// +
		// "\"initRes\":{\"timeWindow\":\"300000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		String strDb = "{\"srcID\":14,\"source\":{\"theme\":\"Asthma\",\"name\":\"obama\",\"url\":\"www.twitter.com\",\"format\":\"archive\",\"supportedWrapper\":\"raw\",\"saveTweets\":true,\"bagOfWords\":[\"obama\"],\"visualParam\":null,\"archiveParam\":{\"beginTime\":1328053417803,\"endTime\":1328399017803,\"refreshRate\":60000},\"initRes\":{\"timeWindow\":\"86400\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"60\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// strDb="{\"srcID\":14,\"source\":{\"theme\":\"Asthma\",\"name\":\"obama\",\"url\":\"www.twitter.com\",\"format\":\"archive\",\"supportedWrapper\":\"state\",\"saveTweets\":true,\"bagOfWords\":[\"obama\"],\"visualParam\":null,\"archiveParam\":{\"beginTime\":1328053417803,\"endTime\":1328399017803,\"refreshRate\":60000},\"initRes\":{\"timeWindow\":\"86400\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"60\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// String strDb =
		// "{\"srcID\":0,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma0\",\"url\":\"www.twitter.com\",\"format\":\"archive\",\"supportedWrapper\":\"state\",\"saveTweets\":true,\"bagOfWords\":null,\"visualParam\":null,\"archiveParam\":{\"beginTime\":481336271397,\"endTime\":1333413071397,\"refreshRate\":60000},\"initRes\":{\"timeWindow\":\"126230400000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"126230400000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// String strDb =
		// "{\"srcID\":5,\"source\":{\"theme\":\"election\",\"name\":\"obama\",\"url\":\"www.twitter.com\","
		// +
		// "\"format\":\"archive\",\"supportedWrapper\":\"raw\",\"saveTweets\":true,\"bagOfWords\":[\"obama\",\"president\",\"barack\"],\"visualParam\":null,"
		// +
		// "\"archiveParam\":{\"beginTime\":1327942857791,\"endTime\":1328010597791,\"refreshRate\":\"10000\"},"
		// +
		// "\"initRes\":{\"timeWindow\":\"300000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},"
		// +
		// "\"finalRes\":{\"timeWindow\":\"300000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// String strDb2 =
		// "{\"srcID\":5,\"source\":{\"theme\":\"election\",\"name\":\"twitter.tbl_obama_tweet\",\"url\":\"www.twitter.com\",\"format\":\"archive\",\"supportedWrapper\":\"raw\",\"bagOfWords\":[\"obama\",\"president\",\"barack\"],\"visualParam\":null,\"archiveParam\":{\"beginTime\":1327840237962,\"endTime\":1328185837962,\"refreshRate\":10000},\"initRes\":{\"timeWindow\":\"3000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"3000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// String strTweet =
		// "{\"srcID\":6,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma6\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"Twitter\",\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"archiveParam\":null,\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"1\",\"longUnit\":\"1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"1\",\"longUnit\":\"1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// String strSim =
		// "{\"srcID\":8,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma8\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"sim\",\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"archiveParam\":null,\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// servlet.startDataProcess(servlet.parseDataSource(strDb));

		String strTweet = "{\"srcID\":8,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma8\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"Twitter\",\"saveTweets\":true,\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"archiveParam\":null,\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// servlet.startDataProcess(servlet.parseDataSource(strTweet));

		String test = "{\"srcID\":17,\"source\":{\"theme\":\"Asthma17\",\"name\":\"Twitter-Asthma17\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"Twitter\",\"saveTweets\":true,\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// String strSim
		// ="{\"srcID\":7,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma7\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"sim\",\"saveTweets\":true,\"bagOfWords\":[\"\"],\"visualParam\":null,\"archiveParam\":null,\"initRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// {"srcID":17,"source":{"theme":"Asthma17","name":"Twitter-Asthma17","url":"www.twitter.com","format":"stream","supportedWrapper":"Twitter","saveTweets":true,"bagOfWords":["flu"," asthma"," allergy"],"visualParam":null,"initRes":{"timeWindow":"20","syncAtMilSec":"0","latUnit":"2","longUnit":"2","swLat":"24","swLong":"-125","neLat":"50","neLong":"-66"},"finalRes":{"timeWindow":"20","syncAtMilSec":"0","latUnit":"2","longUnit":"2","swLat":"24","swLong":"-125","neLat":"50","neLong":"-66"}}}

		// servlet.startDataProcess(servlet.parseDataSource(test));

		// new Thread(servlet.queries.get(1)).start();

		// String srcJSONText =
		// "{\"srcID\":0,\"source\":{\"theme\":\"Flu\",\"name\":\"Twitter\",\"url\":\"twitter.com\",\""
		// +
		// "format\":\"stream\",\"supportedWrapper\":\"sim\",\"bagOfWords\":[\"flu\",\"cough\",\"fever\"],\"visualParam\":null,\"initRes\":{\"timeWindow\":\""
		// +
		// "1\",\"syncAtMilSec\":\"1\",\"latUnit\":\"1\",\"longUnit\":\"1\",\"swLat\":\"24\",\"swLong\":\"-125.0\",\"neLat\":\"50.0\",\"neLong\""
		// +
		// ":\"-65.0\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"1\",\"latUnit\":\"1\",\"longUnit\":\"1\",\"swLat\":\"24.0\",\"swLong\":\"-125.0\",\"neLat\":\"50.0\",\"neLong\":\"-66.0\"}}}";

		// srcJSONText =
		// "{\"srcID\":3,\"source\":{\"theme\":\"Asthma\",\"name\":\"Twitter-Asthma3\",\"url\":\""
		// + tempDir +
		// "ds3_csvFile\",\"format\":\"file\",\"supportedWrapper\":null,\"bagOfWords\":[],\"visualParam\":null,\"initRes\":{\"timeWindow\":\"1\",\"syncAtMilSec\":\"1\",\"latUnit\":\"0.1\",\"longUnit\":\"0.1\",\"swLat\":\"24.0\",\"swLong\":\"-125.0\",\"neLat\":\"50.0\",\"neLong\":\"-66.0\"},\"finalRes\":{\"timeWindow\":\"10\",\"syncAtMilSec\":\"1\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// String
		// apple="{\"srcID\":4,\"source\":{\"theme\":\"Asthma\",\"name\":\"CVCVCVC\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"Twitter\",\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"initRes\":{\"timeWindow\":\"600\",\"syncAtMilSec\":\"1\",\"latUnit\":\"2.0\",\"longUnit\":\"2.0\",\"swLat\":\"24.0\",\"swLong\":\"-125.0\",\"neLat\":\"50.0\",\"neLong\":\"-65.0\"},\"finalRes\":{\"timeWindow\":\"600\",\"syncAtMilSec\":\"1\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";
		// String
		// yahoo="{\"srcID\":3,\"source\":{\"theme\":\"Asthma\",\"name\":\"ghhhhg\",\"url\":\"www.twitter.com\",\"format\":\"stream\",\"supportedWrapper\":\"Twitter\",\"bagOfWords\":[\"flu\",\" asthma\",\" allergy\"],\"visualParam\":null,\"initRes\":{\"timeWindow\":\"6000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"},\"finalRes\":{\"timeWindow\":\"6000\",\"syncAtMilSec\":\"0\",\"latUnit\":\"2\",\"longUnit\":\"2\",\"swLat\":\"24\",\"swLong\":\"-125\",\"neLat\":\"50\",\"neLong\":\"-66\"}}}";

		// String alertJSONText =
		// "{\"userDS\":\"q15\",\"sourceQuery\":\"ds2:Twitter-Sad\",\"qRange\":\"12.025316455696203:50\",\"nearestDs\":\"ds4:Visual-Pollen\",\"aRange\":\"8.227848101265822:50\",\"msg\":\"dfaf\"}";

		// test query
		// [{"qID":14,"type":"filter_ds14","query":{"dataSrcID":"ds14","maskMethod":"map","coords":["24","-125","50","-66"],"placename":"New York City","filePath":"/home/ingwei/spongpai/temp-archived/\\q0_filterFile","valRange":["-99999999","99999999"],"timeRange":["0","9223372036854775807"],"normMode":true,"normVals":["0","100"]}}]

		// [{"qID":15,"type":"grouping_ds14","query":{"dataSrcID":"ds14","method":"KMeans","numGroup":"3","thresholds":["30"," 70"],"split":"False","doColoring":"True","colorCodes":["Green"," Yellow"," Red"]}}]
	}

	// newly added to fetch for registerServlet 21/07/14-- sanjukta
	public Wrapper getWrapper() {
		return wrapper;
	}

	public String getDsID() {
		return dsID;
	}

	@Override
	public void run() {
		isRunning = true;

		if (wrapper != null)
			new Thread(wrapper).start();
		if (eit != null)
			new Thread(eit).start();
		if (merger != null)
			new Thread(merger).start();
		log.info("in DataProcess run()");
		// // Start the output pipe to output the emage
		// try moving the code into each loop so that
		// the data file is overwritten each time

		int count = 0;

		// Start the process of collecting emages
		Iterator<Emage> it;
		if (merger != null)
			it = merger;
		else
			it = eit;
		while (isRunning) {
			// log.info("count:" + count + ", " + isRunning + ", " +
			// it.hasNext());
			while (it.hasNext()) {
				System.out.println("it hasnext elements works.");
				if (!isRunning)
					break;
				Emage e = it.next();
				e.emageID = this.dsID;

				// Formulate the emage message to be passed to operators
				Message.EmageMsg.Builder builder = Message.EmageMsg
						.newBuilder().setTheme(e.theme)
						.setStartTime(e.startTime.getTime())
						.setEndTime(e.endTime.getTime()).setLatUnit(e.latUnit)
						.setLongUnit(e.longUnit).setSwLat(e.swLat)
						.setSwLong(e.swLong).setNeLat(e.neLat)
						.setNeLong(e.neLong).setNumRows(e.numOfRows)
						.setNumCols(e.numOfColumns);
				log.info("inside message.emagemsg");

				// Get the cell values and add it to emage
				for (int i = 0; i < e.numOfRows; i++) {
					for (int j = 0; j < e.numOfColumns; j++) {
						builder.addCell(e.image[i][j]);

					}

				}

				// Build the Message
				Message.EmageMsg msg = builder.build();

				// Output the data
				byte[] data = msg.toByteArray();
				byte[] size = ByteBuffer.allocate(4)
						.order(ByteOrder.LITTLE_ENDIAN).putInt(data.length)
						.array();
				ByteArrayOutputStream result = new ByteArrayOutputStream(
						data.length + 4);
				try {
					result.write(size);
					result.write(data);
				} catch (IOException e1) {
					log.error(e1.getMessage());
				}

				// moved here to rewrite the output file
				// 08/19/2011 Mingyan
				FileOutputStream output = null;
				try {
					output = new FileOutputStream(filepath);
				} catch (FileNotFoundException e1) {
					log.error(e1.getMessage());
				}

				// Lock the file for writing
				FileLock lock = null;
				try {
					while (lock == null)
						lock = output.getChannel().tryLock();
					output.write(result.toByteArray());
					output.flush();
					lock.release();

					// 08/19/2011 Mingyan
					output.close();
					log.info("[" + e.emageID
							+ "]  successfully write the (binary) output file "
							+ filepath);
				} catch (IOException e1) {
					log.error(e1.getMessage());
					// add by Siripen, to solve runnning method calling twice
					// from the front UI, cause this locking throw an error
					// will need to resolve this issue later
					this.stop();
				}

				// to create a json file of the datasource
				writeJsonEmage(e, imgpath);
				log.info("[" + e.emageID + "]  wrote json file " + count
						+ " emage(s)! " + imgpath);

				// to store in mongodb
				/*
				 * if(db != null) { insertToMongoDB(db, e);
				 * log.info("["+e.emageID+"] store in mongodb "); } else {
				 * log.error("["+e.emageID+"] cannot connect to mongodb "); }
				 */
				// to create a viewable version of the datasource
				// gridToEmage(e.image, imgpath+"_before" );
				// double[][] overlay = createProjectedOverlay (e);
				// gridToEmage(overlay, imgpath);
				// gridToEmage(overlay, imgpath+ "_" + e.startTime);
				// log.info("["+e.emageID+"]  wrote png overlay " + count +
				// " emage(s)! " + imgpath);

				count++;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}

	public boolean stop() {
		if (wrapper != null)
			wrapper.stop();
		if (eit != null)
			eit.stop();
		if (merger != null)
			merger.stop();

		isRunning = false;
		Thread.currentThread().interrupt();
		return true;
	}

	public void writeJsonEmage(Emage e, String fileName) {
		CommonUtil
				.writeToFile(fileName + ".json", e.toJson().toString(), false);
		CommonUtil.writeToFile(fileName + "_geo.json",
				e.toGeoJson().toString(), false);
		CommonUtil.writeToFile(fileName + "_stt.json", e.getSTTPointList()
				.toString(), false);
		// CommonUtil.writeToFile(fileName + "_"+e.startTime.getTime()+".json",
		// e.getAllFormat().toString(), false);
		// log.info("create ds json file");
	}

	public void insertToMongoDB(DB db, Emage e) {
		DBCollection collection = db.getCollection(e.emageID);
		// convert JSON to DBObject directly
		DBObject dbObject = (DBObject) JSON.parse(e.getAllFormat().toString());
		collection.insert(dbObject);
	}

	public PlanarImage gridToEmage(double[][] dataGrid, String imageName) {
		int width = dataGrid[0].length;
		int height = dataGrid.length;

		double maxVal = -999999999;// a large NEGATIVE number which should get
									// overwritten
		double minVal = 999999999;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (dataGrid[i][j] > maxVal)
					maxVal = dataGrid[i][j];
				if (dataGrid[i][j] < minVal)
					minVal = dataGrid[i][j];
			}
		}
		if (minVal == -1)
			minVal = 0;

		// Get the number of bands on the image.
		int nbands = 3;
		// We assume that we can get the pixels values in a integer array.
		double[] pixel = new double[nbands];
		// Get an iterator for the image.
		WritableRaster rasterData = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_BYTE, width, height, nbands, new Point(0, 0));

		if (maxVal > 0) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					for (int k = 0; k < nbands; k++) {
						double value = (dataGrid[i][j] - minVal) * 255
								/ (maxVal - minVal);
						if (value > 255)
							pixel[k] = 255;
						else
							pixel[k] = value;
					}
					rasterData.setPixel(j, i, pixel);
				}
			}
		}

		SampleModel sModel2 = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, width, height, nbands);
		// Try to create a compatible ColorModel - if the number of bands is
		// larger than 4, it will be null.
		ColorModel cModel2 = PlanarImage.createColorModel(sModel2);
		// Create a TiledImage using the sample and color models.
		TiledImage rectPollenImage = new TiledImage(0, 0, width, height, 0, 0,
				sModel2, cModel2);
		// Set the data of the tiled image to be the raster.
		rectPollenImage.setData(rasterData);
		// Save the image on a file.

		try {
			ImageIO.write(rectPollenImage, "png", new File(imageName + ".png"));
			// FileWriter fstream = new FileWriter(imageName+".json");
			// BufferedWriter out = new BufferedWriter(fstream);
			// out.write("{\"minVal\":\""+minVal+"\","+
			// "\"maxVal\":\""+maxVal+"\"}");
			// out.close();
			// log.info("create ds image (png) for visulization");
		} catch (IOException e) {
			log.error(e.getMessage());
			log.error("ERROr in rectifying AQI image");
		}
		return rectPollenImage;
	}

	// / <summary>
	// / Calculates the Y-value (inverse Gudermannian function) for a latitude.

	double[][] createProjectedOverlay(Emage e) {

		double swLong = e.swLong;
		double neLat = e.neLat;

		double DEGREES_PER_RADIAN = 57.2957795;
		double sizeStretchRatio = 1000;
		double incomingLatUnit = e.latUnit;
		double incomingLongUnit = e.longUnit;

		double Ymax = GudermannianInv(e.neLat);
		double Ymin = GudermannianInv(e.swLat);
		int nRows = (int) Math.floor((Ymax - Ymin) * sizeStretchRatio);
		int nCols = Math.abs((int) Math.floor(sizeStretchRatio
				* ((e.swLong - e.neLong) / DEGREES_PER_RADIAN)));

		// Get the image dimensions of the unrectified image
		int width = e.numOfColumns;
		int height = e.numOfRows;

		double[][] inMat = e.image;
		double[][] outMat = new double[nRows][nCols];

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				double corrLa = Gudermannian(Ymax - i / sizeStretchRatio);//
				int corrLat = (int) Math.round((neLat - corrLa)
						/ incomingLatUnit);

				double curLong = swLong + j / sizeStretchRatio
						* DEGREES_PER_RADIAN;
				int corrLong = (int) Math.round((curLong - swLong)
						/ incomingLongUnit);

				if (corrLat >= 0 && corrLat < height && corrLong >= 0
						&& corrLong < width) {
					outMat[i][j] = inMat[corrLat][corrLong];
				} else {
					outMat[i][j] = 0;

				}
			}
		}

		return outMat;
	}

	double GudermannianInv(double latitude) {
		double RADIANS_PER_DEGREE = 1 / 57.2957795;
		double sign = +1;
		if (latitude < 0)
			sign = -1;
		double sinv = Math.sin(latitude * RADIANS_PER_DEGREE * sign);
		return sign * (Math.log((1.0 + sinv) / (1.0 - sinv)) / 2.0);
	}

	// / <summary>
	// / Returns the Latitude in degrees for a given Y.
	// / </summary>
	// / <param name="y">Y is in the range of +PI to -PI.</param>
	// / <returns>Latitude in degrees.</returns>
	double Gudermannian(double y) {
		double DEGREES_PER_RADIAN = 57.2957795;
		return Math.atan((Math.exp(y) - Math.exp(-y)) / 2.0)
				* DEGREES_PER_RADIAN;
	}
}
