package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.SpatialMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.TemporalMapper;

import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.TwitterWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.Wrapper;

public class STTEmageIterator extends EmageIterator {
	// Type of the input STTPoint iterator
	// 0. Wrapper
	// 1. DBSTTPointIterator
	protected Log log = LogFactory.getLog(this.getClass().getName());
	int streamType = -1;

	// The input STTPointIterator
	STTPointIterator iter;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;

	public String IteratorID;

	@Override
	public void setSTTPointIterator(STTPointIterator iter) {
		if (Wrapper.class.isAssignableFrom(iter.getClass())) {
			this.streamType = 0;
		}
		;

		this.iter = iter;
		this.theme = iter.theme;
		this.params = iter.getParams();

		isRunning = true;
		queue = new LinkedBlockingQueue<Emage>();
	}

	@Override
	public boolean hasNext() {
		return (queue.peek() != null);
	}

	@Override
	public Emage peek() {
		return queue.peek();
	}

	@Override
	public Emage next() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public void run() {
		if (streamType == 0) {
			runFromWrapper();
		} else if (streamType == 1) {
			runFromDB();
		}
	}

	@Override
	public void runFromWrapper() {
		// Has an emage been generated?
		boolean emageGenerated = false;
		// Is this the first entry into the following while loop?
		boolean first;
		// To store when the last window ends
		long lastWindowEnd = 0;
		// The emage to be added to queue
		Emage emage = null;
		// The carry-over Emage
		Emage lastEmage = null;

		while (isRunning) {
			// If there is a carry over Emage
			// Copy it to emage and set it to be null
			if (lastEmage != null) {
				emage = lastEmage;
				lastEmage = null;
				first = false;
			}

			first = true;

			// Set the timeout value based on registered or new data source
			// status
			long timeout;
			// int preRegisteredSrcCount = 16;
			int preRegisteredSrcCount = Integer.parseInt(Config
					.getProperty("preRegisteredSrcCount"));
			if (srcID >= preRegisteredSrcCount)
				timeout = 2 * 60 * 1000;
			else
				timeout = 80 * 60 * 1000;

			MathContext context = new MathContext(5);
			boolean roundFinish = false;
			while (!roundFinish) {
				if (!isRunning)
					break;

				while (iter.hasNext()) {
					STTPoint point = iter.next();

					// If this is the first time to enter the loop
					// create an Emage and set parameters accordingly
					if (first) {
						// If this is a point with new start time
						if (point.start.getTime() >= lastWindowEnd) {
							// an Emage is generated
							emage = new Emage(params, iter.theme);
							emage.setStart(point.start.getTime());
							emage.setEnd(point.end.getTime());

							emageGenerated = true;
							first = false;

							lastWindowEnd = point.end.getTime();
						}
						// Else, continue until a new point is found
						else
							continue;
					} else {
						// If the STTPoint of a new Emage is found
						// Create a new Emage, and update the value
						if (point.start.getTime() >= emage.endTime.getTime()) {
							lastEmage = new Emage(params, iter.theme);
							lastEmage.setStart(point.start.getTime());
							lastEmage.setEnd(point.end.getTime());

							int row = params.getNumOfRows()
									- 1
									- (int) Math
											.floor(Math.abs((BigDecimal
													.valueOf(point.latitude))
													.subtract(
															BigDecimal
																	.valueOf(params.swLat),
															context)
													.divide(BigDecimal
															.valueOf(params.latUnit),
															context)
													.doubleValue()));
							int col = (int) Math
									.floor(Math.abs((BigDecimal
											.valueOf(point.longitude))
											.subtract(
													BigDecimal
															.valueOf(params.swLong),
													context)
											.divide(BigDecimal
													.valueOf(params.longUnit),
													context).doubleValue()));
							lastEmage.setValue(col, row, point.value);

							roundFinish = true;
							break;
						}
					}

					int col = (int) Math.floor(Math.abs((BigDecimal
							.valueOf(point.longitude))
							.subtract(BigDecimal.valueOf(params.swLong),
									context)
							.divide(BigDecimal.valueOf(params.longUnit),
									context).doubleValue()));
					int row = params.getNumOfRows()
							- 1
							- (int) Math.floor(Math.abs((BigDecimal
									.valueOf(point.latitude))
									.subtract(BigDecimal.valueOf(params.swLat),
											context)
									.divide(BigDecimal.valueOf(params.latUnit),
											context).doubleValue()));

					emage.setValue(col, row, point.value);
				}

				if (roundFinish)
					break;

				// when the next point is still not coming
				// wait for the timeout or when iter has next
				long now = System.currentTimeMillis();
				while (!iter.hasNext()
						&& (now + timeout) > System.currentTimeMillis()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}
				// if the point is still not here after timeout
				// end this round
				if (!iter.hasNext())
					roundFinish = true;
			}

			// If emage is generated, add the emage to queue
			if (emageGenerated) {
				queue.add(emage);
				emageGenerated = false;
			}

			// Try sleeping
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}

	@Override
	public void runFromDB() {
		int imageNum = 0;

		MathContext context = new MathContext(5);
		while (isRunning) {
			// This rounds start and end time
			long start = params.start + imageNum * params.timeWindow;
			long end = start + params.timeWindow;

			Emage emage = new Emage(params, iter.theme);
			emage.setStart(start);
			emage.setEnd(end);

			// If there is a carry-over Emage
			// Test whether this Emage should be returned in this round
			if (lastEmage != null) {
				// return an empty Emage
				if (lastEmage.startTime.getTime() > end) {
					queue.add(emage);
					continue;
				}
				emage = lastEmage;
				lastEmage = null;
			}

			STTPoint point;
			while (iter.hasNext()) {
				if (!isRunning)
					break;

				point = iter.next();

				// If the STTPoint of a new Emage is found
				// Create a new Emage, and update the value
				if (point.start.getTime() >= emage.endTime.getTime()) {
					lastEmage = new Emage(params, iter.theme);
					lastEmage.setStart(point.start.getTime());
					lastEmage.setEnd(point.end.getTime());

					int row = params.getNumOfRows()
							- 1
							- (int) Math.floor(Math.abs((BigDecimal
									.valueOf(point.latitude))
									.subtract(BigDecimal.valueOf(params.swLat),
											context)
									.divide(BigDecimal.valueOf(params.latUnit),
											context).doubleValue()));
					int col = (int) Math.floor(Math.abs((BigDecimal
							.valueOf(point.longitude))
							.subtract(BigDecimal.valueOf(params.swLong),
									context)
							.divide(BigDecimal.valueOf(params.longUnit),
									context).doubleValue()));

					lastEmage.setValue(col, row, point.value);
					break;
				}

				int col = (int) Math.floor(Math.abs((BigDecimal
						.valueOf(point.longitude))
						.subtract(BigDecimal.valueOf(params.swLong), context)
						.divide(BigDecimal.valueOf(params.longUnit), context)
						.doubleValue()));
				int row = params.getNumOfRows()
						- 1
						- (int) Math.floor(Math.abs((BigDecimal
								.valueOf(point.latitude))
								.subtract(BigDecimal.valueOf(params.swLat),
										context)
								.divide(BigDecimal.valueOf(params.latUnit),
										context).doubleValue()));
				emage.setValue(col, row, point.value);
			}

			imageNum++;
		}
	}

	@Override
	public void remove() {
		queue.remove();
	}

	@Override
	public void stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	public static void main(String[] args) {
		// String imgBasePath =
		// "C:\\Program Files (x86)\\Apache Software Foundation\\Tomcat 7.0\\webapps\\eventshop\\temp\\ds\\";
		String imgBasePath = Config.getProperty("context") + Constant.RESULT_DS;
		String tempDir = Config.getProperty("tempDir");
		String url = Config.getProperty("twtrURL");
		FrameParameters fp = new FrameParameters(6 * 3600 * 1000, 0, 2, 2, 24,
				-125, 50, -66);// CHANGED TO 6 MINUTES for testing...change back
								// !!!!*****
		FrameParameters fpFinal = new FrameParameters(5 * 60 * 1000, 0, 0.1,
				0.1, 24, -125, 50, -66);

		// 1. Twitter-Obama
		TwitterWrapper wrapper = new TwitterWrapper(url, "Obama", fp, true);
		wrapper.setBagOfWords(new String[] { "obama", "president", "barack" });
		STTEmageIterator EIterObama = new STTEmageIterator();
		EIterObama.setSTTPointIterator(wrapper);

		STMerger mergerObama = new STMerger(fpFinal);
		SpatialMapper sp = SpatialMapper.repeat;
		TemporalMapper tp = TemporalMapper.repeat;
		mergerObama.addIterator(EIterObama, sp, tp);
		mergerObama.setMergingExpression("mulED(R0,1)");
		DataProcess process = new DataProcess(mergerObama, EIterObama, wrapper,
				tempDir + "ds0_Twitter-Obama", imgBasePath + "ds0", "ds0");
		new Thread(process).start();

		// 2. Twitter Happy
		TwitterWrapper wrapperHappy = new TwitterWrapper(url, "Happiness", fp,
				true);
		wrapperHappy.setBagOfWords(new String[] { "happy", "fun", "excited",
				":)", "glad", "enjoy" });
		STTEmageIterator EIterHappy = new STTEmageIterator();
		EIterHappy.setSTTPointIterator(wrapperHappy);

		STMerger mergerHappy = new STMerger(fpFinal);
		mergerHappy.addIterator(EIterHappy, SpatialMapper.repeat,
				TemporalMapper.repeat);
		mergerHappy.setMergingExpression("mulED(R0,1)");
		DataProcess processHappy = new DataProcess(mergerHappy, EIterHappy,
				wrapperHappy, tempDir + "ds1_Twitter-Happy", imgBasePath
						+ "ds1", "ds1");
		new Thread(processHappy).start();

		// 3. Twitter Sad
		TwitterWrapper wrapperSad = new TwitterWrapper(url, "Sad", fp, true);
		wrapperSad.setBagOfWords(new String[] { "sad", "unhappy",
				"disappointed", "angry", "mad", "shit", ":(" });
		STTEmageIterator EIterSad = new STTEmageIterator();
		EIterSad.setSTTPointIterator(wrapperSad);

		STMerger mergerSad = new STMerger(fpFinal);
		mergerSad.addIterator(EIterSad, sp, tp);
		mergerSad.setMergingExpression("mulED(R0,1)");
		DataProcess processSad = new DataProcess(mergerSad, EIterSad,
				wrapperSad, tempDir + "ds2_Twitter-Sad", imgBasePath + "ds2",
				"ds2");
		new Thread(processSad).start();

		// 4. population
		FrameParameters fpPop = new FrameParameters(24 * 3600 * 1000, 0, 0.1,
				0.1, 24, -125, 50, -66);
		CSVEmageIterator iterPop = new CSVEmageIterator(fpPop, "Population",
				tempDir + "population.txt");
		STMerger mergerPop = new STMerger(fpFinal);
		mergerPop
				.addIterator(iterPop, SpatialMapper.sum, TemporalMapper.repeat);
		mergerPop.setMergingExpression("mulED(R0,1)");
		DataProcess processPop = new DataProcess(mergerPop, iterPop, null,
				tempDir + "ds3_CSV-Population", imgBasePath + "ds3", "ds3");
		new Thread(processPop).start();

		// 5. Pollen
		FrameParameters fpPollen = new FrameParameters(12 * 3600 * 1000, 0,
				0.1, 0.1, 24, -125, 50, -66);
		double[][] TrMat = {
				{ 54.169184929, -5.542199557 }, // 52.169184929
				{ 0.496176521, 0.203104483 }, // 0.506176521, .196
				{ -0.328315814, 0.868318964 },// , .87
				{ 0.001014807, 0.000358314 }, { 0.000245955, -0.000381097 },
				{ 0.000422211, -0.000662529 },
				{ -0.00000001, -0.000000585 }, // -0.000000121, -0.000000785
				{ 0.000000180, 0.000000555 }, { -0.000000270, 0.000000078 },
				{ -0.000001276, 0.000001363 }, };

		double[][] ColMat = { { 2.04, 138.45, 2.21 },
				{ 155.65, 252.84, 50.32 }, { 254.37, 254.27, 2.78 },
				{ 254.30, 152.94, 0.09 }, { 253.93, 0.08, 0.00 },
				{ 190.85, 190.35, 147.51 }, { 254.67, 254.98, 254.58 }, };

		VisualEmageIterator iteratorPollen = null;
		iteratorPollen = new VisualEmageIterator(fpPollen, "Pollen",
				"http://pollen.com/images/usa_map.gif", TrMat, ColMat, tempDir
						+ "mask.png", 5);
		STMerger mergerPollen = new STMerger(fpFinal);
		mergerPollen.addIterator(iteratorPollen, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerPollen.setMergingExpression("mulED(R0,1)");
		DataProcess processPollen = new DataProcess(mergerPollen,
				iteratorPollen, null, tempDir + "ds4_Visual-Pollen",
				imgBasePath + "ds4", "ds4");
		new Thread(processPollen).start();

		// 6. Infrared image
		double[][] TrMatInfrared = {
				{ -0.5499418160371790, -0.8405625013791700 },
				{ 1.3922308173657200, -0.0056820561835960 },
				{ -0.0344354857974046, 1.7326167496144500 },
				{ -0.0000016363211234, 0.0000735026415935 },
				{ -0.0000284684689729, -0.0000436190226989 },
				{ 0.0002912631781378, -0.0008557492797333 },
				{ -0.0000003483359801, -0.0000001740636921 },
				{ 0.0000007368743793, 0.0000001617461713 },
				{ 0.0000000868279898, 0.0000000908638039 },
				{ -0.0000010981079577, 0.0000004877718002 }, };

		FrameParameters fpInfraredInit = new FrameParameters(30 * 60 * 1000, 0,
				0.1, 0.1, 7, -99, 39, -47.5);
		VisualEmageIterator iteratorInfrared = new VisualEmageIterator(
				fpInfraredInit, "Infrared",
				"http://www.goes.noaa.gov/GIFS/HUIR.JPG", TrMatInfrared,
				ColMat, null, -1);
		STMerger mergerInfrared = new STMerger(fpFinal);
		mergerInfrared.addIterator(iteratorInfrared, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerInfrared.setMergingExpression("mulED(R0,1)");
		DataProcess processInfrared = new DataProcess(mergerInfrared,
				iteratorInfrared, null, tempDir + "ds5_Visual-Infrared",
				imgBasePath + "ds5", "ds5");
		new Thread(processInfrared).start();

		// 7. AQI
		FrameParameters fpAQIInit = new FrameParameters(12 * 3600 * 1000, 0,
				0.1, 0.1, 24, -125, 50, -66);
		double[][] TrMatAQI = { { 40.462234530, 36.608742073 },
				{ 0.748263373, 0.222251869 }, { -0.489894685, 1.208570277 },
				{ 0.001507530, 0.000538853 }, { 0.000193739, -0.000330355 },
				{ 0.000799529, -0.000634850 }, { -0.000000258, -0.000000979 },
				{ 0.000000080, 0.000000062 }, { -0.000000198, -0.000000100 },
				{ -0.000002263, 0.000001772 }, };

		double[][] ColMatAQI = { { 46.22, 113.17, 64.82 },
				{ 221.88, 232.50, 32.62 }, { 249.71, 102.66, 4.23 },
				{ 248.93, 2.89, 3.44 }, { 153.07, 1.88, 76.04 },
				{ 130.20, 8.82, 43.29 }, { 178.00, 178.00, 178.00 },
				{ 191.00, 231.00, 255.00 }, { 0, 0, 0 }, };

		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		String dateNow = formatter.format(currentDate.getTime());
		VisualEmageIterator iteratorAQI = new VisualEmageIterator(fpAQIInit,
				"AQI", "http://www.epa.gov/airnow/today/forecast_aqi_20"
						+ dateNow + "_usa.jpg", TrMatAQI, ColMatAQI, tempDir
						+ "mask.png", 6);

		STMerger mergerAQI = new STMerger(fpFinal);
		mergerAQI.addIterator(iteratorAQI, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerAQI.setMergingExpression("mulED(R0,1)");
		DataProcess processAQI = new DataProcess(mergerAQI, iteratorAQI, null,
				tempDir + "ds6_Visual-AQI", imgBasePath + "ds6", "ds6");
		new Thread(processAQI).start();

		// 8. Twitter Asthma
		TwitterWrapper wrapperAsthma = new TwitterWrapper(url, "Asthma", fp,
				true);
		wrapperAsthma.setBagOfWords(new String[] { "asthma", "allergy" });
		STTEmageIterator EIterAsthma = new STTEmageIterator();
		EIterAsthma.setSTTPointIterator(wrapperAsthma);

		STMerger mergerAsthma = new STMerger(fpFinal);
		mergerAsthma.addIterator(EIterAsthma, SpatialMapper.repeat,
				TemporalMapper.repeat);
		mergerAsthma.setMergingExpression("mulED(R0,1)");
		DataProcess processAsthma = new DataProcess(mergerAsthma, EIterAsthma,
				wrapperAsthma, tempDir + "ds7_Twitter-Asthma", imgBasePath
						+ "ds7", "ds7");
		new Thread(processAsthma).start();

		// 9. hurricane cone

		FrameParameters fpHCone = new FrameParameters(7 * 24 * 3600 * 1000, 0,
				0.1, 0.1, 24, -125, 50, -66);
		KMLIterator iterHCone = new KMLIterator(fpHCone, "HCone",
				"http://web.media.mit.edu/~singhv/Hurricane_Sandy_5day22.kml");

		STMerger mergerHCone = new STMerger(fpFinal);
		mergerHCone.addIterator(iterHCone, SpatialMapper.sum,
				TemporalMapper.repeat);
		mergerHCone.setMergingExpression("mulED(R0,1)");
		DataProcess processHCone = new DataProcess(mergerHCone, iterHCone,
				null, tempDir + "ds8_KML-HurForecast", imgBasePath + "ds8",
				"ds8");
		new Thread(processHCone).start();

		// 10. open shelters
		FrameParameters fpShelters = new FrameParameters(1 * 3600 * 1000, 0,
				0.1, 0.1, 24, -125, 50, -66);
		String shelterUrl = Config.getProperty("shelterURL");
		KMLIterator iterShelters = new KMLIterator(fpShelters, "Shelters",
				shelterUrl);
		// Sandy
		// String shelterUrl =
		// "https://www.google.com/fusiontables/exporttable?query=select+col0,col1,col11+from+1Ny1ShO0ZH2T0gKMWRuUEYEkKvZ66YOh7UdyNQMQ&amp;o=kmllink&amp;g=col0";
		// CSVFieldEmageIterator iterShelters = new
		// CSVFieldEmageIterator(fpShelters , "Shelters", shelterUrl);
		STMerger mergerShelters = new STMerger(fpFinal);
		mergerShelters.addIterator(iterShelters, SpatialMapper.sum,
				TemporalMapper.repeat);
		mergerShelters.setMergingExpression("mulED(R0,1)");
		DataProcess processShelters = new DataProcess(mergerShelters,
				iterShelters, null, tempDir + "ds9_KML-Shelters", imgBasePath
						+ "ds9", "ds9");
		new Thread(processShelters).start();

		// 11. Twitter Hurricane
		TwitterWrapper wrapperHurricane = new TwitterWrapper(url, "Hurricane",
				fp, true);
		wrapperHurricane.setBagOfWords(new String[] { "Hurricane", "storm",
				"sandy" });
		STTEmageIterator EIterHurricane = new STTEmageIterator();
		EIterHurricane.setSTTPointIterator(wrapperHurricane);

		STMerger mergerHurricane = new STMerger(fpFinal);
		mergerHurricane.addIterator(EIterHurricane, SpatialMapper.repeat,
				TemporalMapper.repeat);
		mergerHurricane.setMergingExpression("mulED(R0,1)");
		DataProcess processHurricane = new DataProcess(mergerHurricane,
				EIterHurricane, wrapperHurricane, tempDir
						+ "ds10_Twitter-Hurricane", imgBasePath + "ds10",
				"ds10");
		new Thread(processHurricane).start();

		// 12. Flood
		FrameParameters fpFlood = new FrameParameters(12 * 3600 * 1000, 0, 0.1,
				0.1, 24, -125, 50, -66);
		double[][] TrMatSevere = { { 107.572933001236, -7.248905562794 },
				{ 1.002534820850, 0.576263830841 },
				{ -0.883930187195, 1.532091340713 },
				{ 0.003471730292, 0.000100432258 },
				{ 0.000051236733, -0.001047379391 },
				{ -0.000215562360, 0.001125942557 },
				{ 0.000000000000, 0.000000000000 },
				{ 0.000000000000, 0.000000000000 },
				{ 0.000000000000, 0.000000000000 },
				{ 0.000000000000, 0.000000000000 }, };

		double[][] ColMatFlood = { { 0, 166, 0 }, { 0, 255, 0 },
				{ 46, 49, 146 }, { 255, 0, 255 }, { 0, 0, 255 },
				{ 237, 0, 140 }, { 255, 158, 158 }, { 255, 127, 0 },
				{ 255, 0, 0 }, { 255, 255, 255 }, { 255, 255, 0 },
				{ 255, 255, 255 }, { 255, 255, 255 }, { 0, 204, 255 },
				{ 158, 0, 255 }, { 153, 153, 153 }, { 239, 239, 239 },
				{ 255, 255, 255 }, { 130, 120, 80 }, { 0, 0, 0 }, };

		VisualEmageIterator iteratorFlood = new VisualEmageIterator(fpFlood,
				"Flood",
				"http://icons-ak.wxug.com/data/640x480/2xus_severe.gif",
				TrMatSevere, ColMatFlood, tempDir + "mask.png", 2);
		STMerger mergerFlood = new STMerger(fpFinal);
		mergerFlood.addIterator(iteratorFlood, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerFlood.setMergingExpression("mulED(R0,1)");
		DataProcess processFlood = new DataProcess(mergerFlood, iteratorFlood,
				null, tempDir + "ds11_Visual-Flood", imgBasePath + "ds11",
				"ds11");
		new Thread(processFlood).start();

		// 13) for hurricane or storm warnings
		FrameParameters fpHurStorm = new FrameParameters(300 * 1000, 0, 0.1,
				0.1, 24, -125, 50, -66);
		double[][] ColMatHurStorm = { { 46, 49, 146 }, { 255, 0, 255 },
				{ 0, 0, 255 }, { 237, 0, 140 }, { 255, 158, 158 },
				{ 255, 127, 0 }, { 255, 0, 0 }, { 255, 255, 255 },
				{ 255, 255, 0 }, { 255, 255, 255 }, { 255, 255, 255 },
				{ 0, 204, 255 }, { 158, 0, 255 }, { 153, 153, 153 },
				{ 239, 239, 239 }, { 255, 255, 255 }, { 130, 120, 80 },
				{ 0, 0, 0 }, { 0, 166, 0 }, { 0, 255, 0 }, };

		VisualEmageIterator iteratorHurStorm = null;
		iteratorHurStorm = new VisualEmageIterator(fpHurStorm, "HurStorm",
				"http://icons-ak.wxug.com/data/640x480/2xus_severe.gif",
				TrMatSevere, ColMatHurStorm, tempDir + "mask.png", 4);
		STMerger mergerHurStorm = new STMerger(fpFinal);
		mergerHurStorm.addIterator(iteratorHurStorm, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerHurStorm.setMergingExpression("mulED(R0,1)");
		DataProcess processHurStorm = new DataProcess(mergerHurStorm,
				iteratorHurStorm, null, tempDir + "ds12_Visual-HurStorm",
				imgBasePath + "ds12", "ds12");
		new Thread(processHurStorm).start();

		// 14) for fire warnings
		FrameParameters fpFire = new FrameParameters(300 * 1000, 0, 0.1, 0.1,
				24, -125, 50, -66);
		double[][] ColMatFire = { { 255, 158, 158 }, { 255, 127, 0 },
				{ 255, 0, 0 }, { 255, 255, 255 }, { 255, 255, 0 },
				{ 255, 255, 255 }, { 255, 255, 255 }, { 0, 204, 255 },
				{ 158, 0, 255 }, { 153, 153, 153 }, { 239, 239, 239 },
				{ 255, 255, 255 }, { 130, 120, 80 }, { 0, 0, 0 },
				{ 0, 166, 0 }, { 0, 255, 0 }, { 46, 49, 146 }, { 255, 0, 255 },
				{ 0, 0, 255 }, { 237, 0, 140 }, };

		VisualEmageIterator iteratorFire = null;
		iteratorFire = new VisualEmageIterator(fpFire, "Fire",
				"http://icons-ak.wxug.com/data/640x480/2xus_severe.gif",
				TrMatSevere, ColMatFire, tempDir + "mask.png", 4);
		STMerger mergerFire = new STMerger(fpFinal);
		mergerFire.addIterator(iteratorFire, SpatialMapper.average,
				TemporalMapper.repeat);
		mergerFire.setMergingExpression("mulED(R0,1)");
		DataProcess processFire = new DataProcess(mergerFire, iteratorFire,
				null, tempDir + "ds13_Visual-Fire", imgBasePath + "ds13",
				"ds13");
		new Thread(processFire).start();

		// 15. open shelters for sandy hurricane
		String shelterSandyUrl = Config.getProperty("shelterSandyURL");
		CSVFieldEmageIterator iterSheltersSandy = new CSVFieldEmageIterator(
				fpShelters, "SheltersSandy", shelterSandyUrl);
		STMerger mergerSheltersSandy = new STMerger(fpFinal);
		mergerSheltersSandy.addIterator(iterSheltersSandy, SpatialMapper.sum,
				TemporalMapper.repeat);
		mergerSheltersSandy.setMergingExpression("mulED(R0,1)");
		DataProcess processSheltersSandy = new DataProcess(mergerSheltersSandy,
				iterSheltersSandy, null, tempDir + "ds14_CSV-Shelters-Sandy",
				imgBasePath + "ds14", "ds14");
		new Thread(processSheltersSandy).start();

		/*
		 * // 15. Twitter-Cain TwitterWrapper wrapperCain = new
		 * TwitterWrapper(url, "Cain", fp, true); wrapperCain.setBagOfWords(new
		 * String[]{"Cain"}); STTEmageIterator EIterCain = new
		 * STTEmageIterator(); EIterCain.setSTTPointIterator(wrapperCain);
		 * 
		 * STMerger mergerCain = new STMerger(fpFinal);
		 * mergerCain.addIterator(EIterCain, SpatialMapper.repeat,
		 * TemporalMapper.repeat);
		 * mergerCain.setMergingExpression("mulED(R0,1)"); DataProcess
		 * processCain = new DataProcess(mergerCain, EIterCain, wrapperCain,
		 * tempDir + "ds14_Twitter-Cain", imgBasePath + "ds14"); new
		 * Thread(processCain).start();
		 * 
		 * // 16. Twitter-Romney TwitterWrapper wrapperRomney = new
		 * TwitterWrapper(url, "Romney", fp, true);
		 * wrapperRomney.setBagOfWords(new String[]{"Romney"}); STTEmageIterator
		 * EIterRomney = new STTEmageIterator();
		 * EIterRomney.setSTTPointIterator(wrapperRomney);
		 * 
		 * STMerger mergerRomney = new STMerger(fpFinal);
		 * mergerRomney.addIterator(EIterRomney, SpatialMapper.repeat,
		 * TemporalMapper.repeat);
		 * mergerRomney.setMergingExpression("mulED(R0,1)"); DataProcess
		 * processRomney = new DataProcess(mergerRomney, EIterRomney,
		 * wrapperRomney, tempDir + "ds15_Twitter-Romney", imgBasePath +
		 * "ds15"); new Thread(processRomney).start();
		 * 
		 * // 17. Twitter Perry TwitterWrapper wrapperPerry = new
		 * TwitterWrapper(url, "Perry", fp, true);
		 * wrapperPerry.setBagOfWords(new String[]{"Perry"}); STTEmageIterator
		 * EIterPerry = new STTEmageIterator();
		 * EIterPerry.setSTTPointIterator(wrapperPerry);
		 * 
		 * STMerger mergerPerry = new STMerger(fpFinal);
		 * mergerPerry.addIterator(EIterPerry,SpatialMapper.repeat,
		 * TemporalMapper.repeat);
		 * mergerPerry.setMergingExpression("mulED(R0,1)"); DataProcess
		 * processPerry = new DataProcess(mergerPerry, EIterPerry, wrapperPerry,
		 * tempDir + "ds16_Twitter-Perry", imgBasePath + "ds16"); new
		 * Thread(processPerry).start();
		 */
	}
}
