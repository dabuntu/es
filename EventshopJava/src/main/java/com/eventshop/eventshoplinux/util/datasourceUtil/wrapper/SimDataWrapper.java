package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.domain.datasource.simulator.DataPoint;
import com.eventshop.eventshoplinux.domain.datasource.simulator.DistParameters;
import com.eventshop.eventshoplinux.domain.datasource.simulator.GaussianParameters2D;
import com.eventshop.eventshoplinux.domain.datasource.simulator.SpatialPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.simulator.*;
import com.eventshop.eventshoplinux.util.datasourceUtil.simulator.Simulator.Kernel;

public class SimDataWrapper extends Wrapper implements Runnable {
	protected static Log log = LogFactory.getLog(SimDataWrapper.class);
	Simulator sim;

	int[][] count;
	long curWindowEnd;

	boolean isRunning;

	LinkedBlockingQueue<STTPoint> sttStream;

	public SimDataWrapper(String url, String theme, FrameParameters params) {
		super(url, theme, params);

		int numOfRows = params.getNumOfRows();
		int numOfColumns = params.getNumOfColumns();

		count = new int[numOfRows][numOfColumns];
		for (int i = 0; i < numOfRows; i++)
			for (int j = 0; j < numOfColumns; j++)
				count[i][j] = 0;

		curWindowEnd = 0;

		sttStream = new LinkedBlockingQueue<STTPoint>();
	}

	public SimDataWrapper(String url, String theme, FrameParameters params,
			Kernel gaussian, ArrayList<DistParameters> dParams) {
		super(url, theme, params);

		int numOfRows = params.getNumOfRows();
		int numOfColumns = params.getNumOfColumns();

		count = new int[numOfRows][numOfColumns];
		for (int i = 0; i < numOfRows; i++)
			for (int j = 0; j < numOfColumns; j++)
				count[i][j] = 0;

		curWindowEnd = 0;

		sttStream = new LinkedBlockingQueue<STTPoint>();

		// Create the simulator
		SpatialPoint swPoint = new SpatialPoint(params.swLat, params.swLong);
		SpatialPoint nePoint = new SpatialPoint(params.neLat, params.neLong);
		double latUnit = params.latUnit;
		double longUnit = params.longUnit;

		sim = new Simulator(swPoint, nePoint, latUnit, longUnit,
				params.timeWindow, theme, Kernel.gaussian);
		sim.setParameters(dParams);
	}

	public void setUpSimulator(Kernel k, ArrayList<DistParameters> dParams) {
		SpatialPoint swPoint = new SpatialPoint(params.swLat, params.swLong);
		SpatialPoint nePoint = new SpatialPoint(params.neLat, params.neLong);
		double latUnit = params.latUnit;
		double longUnit = params.longUnit;

		// Create the simulator
		sim = new Simulator(swPoint, nePoint, latUnit, longUnit, 1000, "flu", k);
		sim.setParameters(dParams);
	}

	@Override
	public void run() {
		(new Thread(sim)).start();

		boolean firstEntry = true;

		while (sim.isRunning()) {
			DataPoint point = sim.next();

			// if the first element arrives, update the end time of the current
			// frame
			if (firstEntry) {
				long now = point.getTime().getTime();
				curWindowEnd = (long) Math.ceil(now / params.timeWindow)
						* params.timeWindow + params.syncAtMilSec;

				firstEntry = false;
			}
			// if the data from next window has arrived
			if (point.getTime().getTime() > curWindowEnd) {
				// Create the corresponding STT points
				for (int i = 0; i < params.getNumOfRows(); i++) {
					for (int j = 0; j < params.getNumOfColumns(); j++) {
						double lat = params.swLat + (i + 0.5) * params.latUnit; // pick
																				// the
																				// center
						double lon = params.swLong + (j + 0.5)
								* params.longUnit;

						STTPoint stt = new STTPoint(count[i][j], new Date(
								curWindowEnd - params.timeWindow), new Date(
								curWindowEnd), params.latUnit, params.longUnit,
								lat, lon, theme);
						sttStream.add(stt);

						// clear the count
						count[i][j] = 0;
					}
				}

				// Update the new window
				curWindowEnd += params.timeWindow;
			} else {
				// compute the row and col of the point
				MathContext context = new MathContext(5);
				int r = (int) Math.floor(Math.abs((BigDecimal.valueOf(point
						.getSpatial().getLat()))
						.subtract(BigDecimal.valueOf(params.swLat), context)
						.divide(BigDecimal.valueOf(params.latUnit), context)
						.doubleValue()));
				int c = (int) Math.floor(Math.abs((BigDecimal.valueOf(point
						.getSpatial().getLong()))
						.subtract(BigDecimal.valueOf(params.swLong), context)
						.divide(BigDecimal.valueOf(params.longUnit), context)
						.doubleValue()));

				// update the value in the corresponding cell
				count[r][c]++;
			}
		}
	}

	@Override
	public boolean stop() {
		sim.stop();
		Thread.currentThread().interrupt();
		return true;
	}

	@Override
	public boolean hasNext() {
		return (sttStream.peek() != null);
	}

	@Override
	public STTPoint next() {
		try {
			return sttStream.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public void remove() {
		sttStream.remove();
	}

	public static void main(String[] args) {
		try {
			long timeWindow = 1000 * 1;
			long sync = 10;

			double latUnit = 1;
			double longUnit = 1;
			double swLat = 25;
			double swLong = -120;
			double neLat = 30;
			double neLong = -115;

			FrameParameters params = new FrameParameters(timeWindow, sync,
					latUnit, longUnit, swLat, swLong, neLat, neLong);
			String url = Config.getProperty("simURL");
			SimDataWrapper wrapper = new SimDataWrapper(url, "Flu", params);

			// Generate the parameters
			ArrayList<DistParameters> dParams = new ArrayList<DistParameters>();
			GaussianParameters2D gParam = new GaussianParameters2D(28, -118,
					3.0, 3.0, 2);
			dParams.add(gParam);

			// set up the wrapper with appropriate parameters
			wrapper.setUpSimulator(Kernel.gaussian, dParams);

			// Start the wrapper
			new Thread(wrapper).start();

			while (true) {
				while (wrapper.hasNext()) {
					STTPoint point = wrapper.next();
					SimDataWrapper.log.info(point.latitude + " " + point.longitude
							+ " " + point.value + " , ");
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		System.exit(0);
	}
}
