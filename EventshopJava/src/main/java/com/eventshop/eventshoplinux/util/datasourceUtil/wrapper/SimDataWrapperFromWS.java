package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.domain.datasource.simulator.DataPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class SimDataWrapperFromWS extends Wrapper implements Runnable {
	protected static Log log = LogFactory.getLog(SimDataWrapperFromWS.class);
	long dsid;
	long curWindowEnd;
	boolean isRunning;
	LinkedBlockingQueue<STTPoint> sttStream;

	int[][] count;

	public SimDataWrapperFromWS(String url, String theme,
			FrameParameters params, int dsid) {
		super(url, theme, params);
		this.dsid = dsid;
		int numOfRows = params.getNumOfRows();
		int numOfColumns = params.getNumOfColumns();

		count = new int[numOfRows][numOfColumns];
		for (int i = 0; i < numOfRows; i++)
			for (int j = 0; j < numOfColumns; j++)
				count[i][j] = 0;
		curWindowEnd = 0;
		sttStream = new LinkedBlockingQueue<STTPoint>();
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(Config.getProperty("esuri")).build();
	}

	@Override
	public void run() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());

		isRunning = true;
		boolean firstEntry = true;

		Gson gson = new Gson();
		JsonParser jparser = new JsonParser();

		while (isRunning) {
			String json = service.path("rest").path("datasource/id/" + dsid)
					.accept(MediaType.APPLICATION_JSON).get(String.class);

			if (json.equals("null")) {

				try {
					Thread.sleep(Math.max(curWindowEnd + params.timeWindow
							- System.currentTimeMillis(), 1000));
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				continue;
			}

			JsonObject datapoints = jparser.parse(json).getAsJsonObject();
			JsonArray dataArray = datapoints.getAsJsonArray("dataPoint");
			ArrayList<DataPoint> points = gson.fromJson(dataArray,
					new TypeToken<ArrayList<DataPoint>>() {
					}.getType());

			log.info("Number of points is " + points.size());
			for (int i = 0; i < points.size(); ++i) {
				double lat = points.get(i).spatial.lat;
				double lon = points.get(i).spatial.lon;

				// if the first element arrives, update the end time of the
				// current frame
				if (firstEntry) {
					long now = Long.valueOf(points.get(i).timevalue);
					curWindowEnd = (long) Math.ceil(now / params.timeWindow)
							* params.timeWindow + params.syncAtMilSec;
					firstEntry = false;
				}

				MathContext context = new MathContext(5);
				int r = (int) Math.floor(Math.abs((BigDecimal.valueOf(lat))
						.subtract(BigDecimal.valueOf(params.swLat), context)
						.divide(BigDecimal.valueOf(params.latUnit), context)
						.doubleValue()));
				int c = (int) Math.floor(Math.abs((BigDecimal.valueOf(lon))
						.subtract(BigDecimal.valueOf(params.swLong), context)
						.divide(BigDecimal.valueOf(params.longUnit), context)
						.doubleValue()));

				// update the value in the corresponding cell
				count[r][c]++;
			}

			// Create the corresponding STT points
			for (int i = 0; i < params.getNumOfRows(); i++) {
				for (int j = 0; j < params.getNumOfColumns(); j++) {
					double lat = params.swLat + (i + 0.5) * params.latUnit; // pick
																			// the
																			// center
					double lon = params.swLong + (j + 0.5) * params.longUnit;

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
		}
	}

	@Override
	public boolean stop() {
		isRunning = false;
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
			long timeWindow = 1000 * 30;
			long sync = 0;

			double latUnit = 1;
			double longUnit = 1;
			double swLat = 25;
			double swLong = -125;
			double neLat = 50;
			double neLong = -65;

			FrameParameters params = new FrameParameters(timeWindow, sync,
					latUnit, longUnit, swLat, swLong, neLat, neLong);
			String url = Config.getProperty("simURL");
			SimDataWrapperFromWS wrapper = new SimDataWrapperFromWS(url, "flu",
					params, 0);

			// Start the wrapper
			new Thread(wrapper).start();

			while (true) {
				while (wrapper.hasNext()) {
					STTPoint point = wrapper.next();
					SimDataWrapperFromWS.log.info(point.latitude + " " + point.longitude
							+ " " + point.value + " , ");
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		System.exit(0);
	}
}
