package com.eventshop.eventshoplinux.domain.datasource.simulator;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataProducer implements Runnable {

	protected Log log = LogFactory.getLog(this.getClass().getName());
	SpatialPoint swPoint;
	SpatialPoint nePoint;
	double latUnit;
	double longUnit;
	long timeWindow;

	String theme;

	int[][] count;
	int rows, cols;
	LinkedBlockingQueue<DataPoint> queue;

	long curTime;

	boolean isRunning;

	// Hurricane matrix
	double[][] hurricaneMatrix;

	public DataProducer(SpatialPoint sw, SpatialPoint ne, double latUnit,
			double longUnit, long timeWindow, String theme, int[][] cnt,
			int rows, int cols, LinkedBlockingQueue<DataPoint> q) {
		swPoint = sw;
		nePoint = ne;
		this.latUnit = latUnit;
		this.longUnit = longUnit;

		this.timeWindow = timeWindow;

		this.theme = theme;

		this.rows = rows;
		this.cols = cols;
		count = new int[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				this.count[i][j] = cnt[i][j];

		this.queue = q;

	}

	public double[][] randomGaussian() {
		// Add one random center
		Random rand = new Random(System.currentTimeMillis());
		double lat = rand.nextDouble() * 26 + 24;
		double lon = rand.nextDouble() * 59 - 125;

		GaussianParameters2D gParam = new GaussianParameters2D(lat, lon, 3.0,
				3.0, 200);
		GaussianGenerator2D gen = new GaussianGenerator2D(gParam);

		// Create the count
		double noiseCount[][] = new double[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				noiseCount[i][j] = gen.getValue(swPoint.getLat() + i * latUnit,
						swPoint.getLong() + j * longUnit);
		return noiseCount;
	}

	private double[][] createHurricaneCloud() {
		// Add one random center
		Random rand = new Random(System.currentTimeMillis());
		double lat = rand.nextDouble() * 26 + 24;
		double lon = rand.nextDouble() * 59 - 125;

		int latIndex = (int) Math.ceil((lat - swPoint.lat) / latUnit);
		int lonIndex = (int) Math.ceil((lon - swPoint.lon) / longUnit);
		// Add hurricaneMatrix
		double hurricaneCount[][] = new double[rows][cols];
		for (int i = 0; i < rows; ++i)
			for (int j = 0; j < cols; ++j) {
				if (i < latIndex - 25 || i > latIndex + 24 || j < lonIndex - 25
						|| j > lonIndex + 24)
					hurricaneCount[i][j] = 0;
				else {
					hurricaneCount[i][j] = hurricaneMatrix[25 + i - latIndex][25
							+ j - lonIndex];
				}
			}

		return hurricaneCount;
	}

	@Override
	public void run() {
		isRunning = true;

		curTime = System.currentTimeMillis();

		while (isRunning) {
			curTime = curTime + this.timeWindow;

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (!isRunning)
						break;

					int cnt = count[i][j];
					for (int k = 0; k < cnt; k++) {
						DataPoint point = new DataPoint();
						point.spatial.lat = swPoint.lat + latUnit * (i + 0.5); // pick
																				// the
																				// center!!
						point.spatial.lon = swPoint.lon + longUnit * (j + 0.5);
						point.theme = theme;
						point.time = new Date(curTime);

						try {
							queue.put(point);
						} catch (InterruptedException e) {
							log.error(e.getMessage());
						}
					}
				}

			}

			try {
				while (System.currentTimeMillis() < curTime)
					Thread.sleep(10);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
	}
}
