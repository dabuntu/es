package com.eventshop.eventshoplinux.domain.datasource.simulator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Simulator implements Runnable {
	// Bounding area of incoming data points
	SpatialPoint swPoint;
	SpatialPoint nePoint;

	// Resolution
	double latUnit;
	double longUnit;

	// Time Window
	long timeWindow;

	// Theme
	String theme;

	// An enumeration of kernels
	Kernel k;

	// A list of distribution generators
	ArrayList<DistributionGenerator> generators;

	// Count array
	int rows, cols;
	int[][] count;

	// boolean
	boolean isRunning;

	// the blocking queue to store data
	LinkedBlockingQueue<DataPoint> q;
	// Data producer that puts new data points into the q
	DataProducer producer;

	// // Thread of producer
	// Thread producerThread = null;

	public enum Kernel {
		gaussian, uniform;
	}

	public Simulator(
			SpatialPoint sw,
			SpatialPoint ne,
			double latUnit,
			double longUnit,
			long timeWindow,
			String theme,
			com.eventshop.eventshoplinux.domain.datasource.simulator.Simulator.Kernel kernel) {
		// Initialize the spatial bounding box
		// Temporal box is assumed to be 1 sec
		this.swPoint = sw;
		this.nePoint = ne;
		this.latUnit = latUnit;
		this.longUnit = longUnit;

		// Setting up time window
		this.timeWindow = timeWindow;

		// Initialize the theme
		this.theme = theme;

		// Specify the kernel
		this.k = kernel;

		// Store the distribution generators
		generators = new ArrayList<DistributionGenerator>();

		// Store the size of matrix
		MathContext context = new MathContext(5);
		rows = (int) Math.ceil((BigDecimal.valueOf(nePoint.lat))
				.subtract(BigDecimal.valueOf(swPoint.lat), context)
				.divide(BigDecimal.valueOf(latUnit), context).doubleValue());
		cols = (int) Math.ceil((BigDecimal.valueOf(nePoint.lon))
				.subtract(BigDecimal.valueOf(swPoint.lon), context)
				.divide(BigDecimal.valueOf(longUnit), context).doubleValue());
		count = new int[rows][cols];

		// Initialize the value
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				count[i][j] = 0;

		isRunning = true;

		// Initialize the BlockingQueue to get data
		q = new LinkedBlockingQueue<DataPoint>();
	}

	public void setParameters(ArrayList<DistParameters> params) {
		if (k == Kernel.gaussian) {
			for (DistParameters param : params)
				generators.add(new GaussianGenerator2D(
						(GaussianParameters2D) param));
		}

		int i = 0, j = 0;
		for (double lat = swPoint.lat; lat < nePoint.lat && i < rows; lat = lat
				+ latUnit) {
			for (double lon = swPoint.lon; lon < nePoint.lon && j < cols; lon = lon
					+ longUnit) {
				double value = 0;
				for (DistributionGenerator gen : generators)
					value += ((GaussianGenerator2D) gen).getValue(lat, lon);
				count[i][j] = (int) Math.ceil(value);
				j++;
			}
			i++;
			j = 0;
		}

		producer = new DataProducer(swPoint, nePoint, latUnit, longUnit,
				timeWindow, theme, count, rows, cols, q);
	}

	public boolean hasNext() {
		return (q.peek() != null);
	}

	public DataPoint next() {
		try {
			return q.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void run() {
		new Thread(producer).start();
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {
		producer.stop();
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	public static void main(String[] args) {
		SpatialPoint swPoint = new SpatialPoint(25.0, -120.0);
		SpatialPoint nePoint = new SpatialPoint(50.0, -65.0);
		double latUnit = 0.1;
		double longUnit = 0.1;

		Kernel k = Kernel.gaussian;

		// Create the simulator
		Simulator sim = new Simulator(swPoint, nePoint, latUnit, longUnit,
				30 * 1000, "flu", k);

		// Generate the parameters
		ArrayList<DistParameters> params = new ArrayList<DistParameters>();

		// LA
		GaussianParameters2D gParam = new GaussianParameters2D(34.1, -118.2,
				3.0, 3.0, 50);
		params.add(gParam);
		// SF
		gParam = new GaussianParameters2D(37.8, -122.4, 3.0, 3.0, 50);
		params.add(gParam);
		// Seattle
		gParam = new GaussianParameters2D(47.6, -122.3, 3.0, 3.0, 50);
		params.add(gParam);
		// NYC
		gParam = new GaussianParameters2D(40.8, -74.0, 3.0, 3.0, 50);
		params.add(gParam);

		sim.setParameters(params);

		new Thread(sim).start();

	}
}
