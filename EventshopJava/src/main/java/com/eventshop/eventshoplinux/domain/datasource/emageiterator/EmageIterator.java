package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.TwitterWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.Wrapper;

public class EmageIterator implements Iterator<Emage>, Runnable {
	// Type of the input STTPoint iterator
	// 0. Wrapper
	// 1. DBSTTPointIterator
	protected static Log log = LogFactory.getLog(EmageIterator.class);
	int streamType = -1;

	// The input STTPointIterator
	STTPointIterator iter;
	// The frame parameters used to form frames
	public FrameParameters params;

	// The theme
	public String theme;

	// The carry-over Emage
	Emage lastEmage = null;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;

	public String IteratorID;

	long srcID;

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

	public void setSrcID(long srcID) {
		this.srcID = srcID;
	}

	@Override
	public boolean hasNext() {
		return queue.iterator().hasNext();
	}

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

	public void runFromWrapper() {
		// Is an emage generated?
		boolean emageGenerated = false;

		// Is this the first entry into the following while loop?
		boolean first;

		// To store when the last window ends
		long lastWindowEnd = 0;

		Emage emage = null;
		MathContext context = new MathContext(5);
		while (isRunning) {
			first = true;
			// If there is a carry over Emage?
			// Copy it and set it to be null
			if (lastEmage != null) {
				emage = lastEmage;
				lastEmage = null;
				first = false;
			}
			double total = 0;

			while (!emageGenerated) {
				if (!isRunning)
					break;
				STTPoint point = iter.next();

				// If this is the first time to enter the loop
				// create an Emage and set parameters accordingly
				if (first) {
					if (point.start.getTime() >= lastWindowEnd) {
						// an Emage is generated
						emage = new Emage(params, iter.theme);
						emage.setStart(point.start.getTime());
						emage.setEnd(point.end.getTime());

						first = false;

						lastWindowEnd = point.end.getTime();
					} else
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
								- (int) Math.floor(Math.abs((BigDecimal
										.valueOf(point.latitude))
										.subtract(
												BigDecimal
														.valueOf(params.swLat),
												context)
										.divide(BigDecimal
												.valueOf(params.latUnit),
												context).doubleValue()));
						int col = (int) Math.floor(Math.abs((BigDecimal
								.valueOf(point.longitude))
								.subtract(BigDecimal.valueOf(params.swLong),
										context)
								.divide(BigDecimal.valueOf(params.longUnit),
										context).doubleValue()));
						lastEmage.setValue(col, row, point.value);

						emageGenerated = true;
						break;
					}
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
				// log.info("set value from point to emage " + col + ", " + row
				// + ", " + point.value);
				total += point.value;
			}
			// find and set min max value for each emage
			// by: Siripen
			emage.setMinMax();

			queue.add(emage);
			emageGenerated = false;
			log.info("the total number of point value: " + total);
		}
	}

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

	public void stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	public static void main(String[] args) {
		long timeWindow = 1000 * 60;// *60*24*2;//the last 2 days
		double latUnit = 2;
		double longUnit = 2;
		double swLat = 24;
		double swLong = -125;
		double neLat = 50;
		double neLong = -66;

		FrameParameters fp = new FrameParameters(timeWindow, 1000, latUnit,
				longUnit, swLat, swLong, neLat, neLong);
		String url = Config.getProperty("twtrURL");

		TwitterWrapper wrapper = new TwitterWrapper(url, "Flu", fp, true);
		wrapper.setBagOfWords(new String[] { "Allergy", "Flu" });

		EmageIterator EIter = new EmageIterator();
		EIter.setSTTPointIterator(wrapper);

		(new Thread(wrapper)).start();
		(new Thread(EIter)).start();

		while (EIter.hasNext()) {
			Emage emage = EIter.next();
			for (int i = 0; i < emage.numOfRows; i++)
				for (int j = 0; j < emage.numOfColumns; j++)
					log.info(i + " " + j + " " + emage.image[i][j]);
		}
	}
}
