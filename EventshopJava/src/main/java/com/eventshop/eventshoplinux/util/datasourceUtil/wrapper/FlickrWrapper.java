package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;

import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.Photo;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class FlickrWrapper extends Wrapper {
	protected static Log log = LogFactory.getLog(FlickrWrapper.class);
	private String[] bagOfWords;

	private ArrayList<STTPoint> ls;
	private Iterator<STTPoint> it = null;

	private boolean isRunning = false;

	public FlickrWrapper(String url, String theme, FrameParameters params) {
		super(url, theme, params);
		ls = new ArrayList<STTPoint>();
	}

	public void setBagOfWords(String[] words) {
		bagOfWords = words;
	}

	@Override
	public STTPoint next() {
		STTPoint point = null;
		if (it.hasNext()) {
			point = it.next();
		}
		return point;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public void remove() {
		it.remove();
	}

	@Override
	public void run() {
		isRunning = true;

		// Set API key
		String key = "e30af378fb58c62d8254f02cfbd3bde8";
		String svr = "flickr.com";
		REST rest = null;
		try {
			rest = new REST();
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
		}
		rest.setHost(svr);

		// initialize Flickr object with key and rest
		Flickr flickr = new Flickr(key, rest);
		Flickr.debugStream = false;

		// initializing the class attributes which need to be set
		STTPoint point;

		// initialize SearchParameter object, this object stores the search
		// keyword
		SearchParameters searchParams = new SearchParameters();

		// 1. Bag of Words
		searchParams.setTags(bagOfWords);

		// 2. startTime
		Calendar startTime = Calendar.getInstance();
		Date d = startTime.getTime();
		d.setTime(d.getTime() - params.timeWindow); // timewindow unit should be
													// seconds
		searchParams.setMinTakenDate(d);

		// 3. Loop for Lat Long blocks
		for (double i = params.swLat; i < params.neLat; i = i + params.latUnit) {
			for (double j = params.swLong; j < params.neLong && isRunning; j = j
					+ params.longUnit) {
				if (!isRunning)
					break;

				log.info("<BR>" + i + " , " + j + "<BR>");

				searchParams.setBBox(Double.toString(j), Double.toString(i),
						Double.toString(j + params.longUnit),
						Double.toString(i + params.latUnit));
				// Initialize PhotosInterface object
				PhotosInterface photosInterface = flickr.getPhotosInterface();

				// Execute search with entered tags
				PhotoList photoList = null;
				try {
					photoList = photosInterface.search(searchParams, 100, 1);
				} catch (IOException e) {
					log.error(e.getMessage());
				} catch (SAXException e) {
					log.error(e.getMessage());
				} catch (FlickrException e) {
					log.error(e.getMessage());
				}

				// Get search result and fetch the photo object and get small
				// square imag's url
				if (photoList != null) {
					// Get search result and check the size of photo result
					point = new STTPoint(photoList.size(), d,
							startTime.getTime(), params.latUnit,
							params.longUnit, i, j, theme);
					ls.add(point);
					for (int k = 0; k < photoList.size(); k++) {
						// get photo object
						Photo photo = (Photo) photoList.get(k);
						log.info("<a href=" + photo.getUrl()
								+ "><img border=\"0\" src=\""
								+ photo.getSmallUrl() + "\"></a>");
					}
				}
			}
		}
		it = ls.iterator();
	}

	@Override
	public boolean stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
		return true;
	}

	public static void main(String[] args) {
		try {
			long timeWindow = 1000 * 60 * 60 * 24 * 7; // the last 7 days
			long sync = 1000;
			double latUnit = 5;
			double longUnit = 5;
			double swLat = 40;
			double swLong = -125;
			double neLat = 50;
			double neLong = -65;

			FrameParameters params = new FrameParameters(timeWindow, sync,
					latUnit, longUnit, swLat, swLong, neLat, neLong);
			String url = Config.getProperty("flkrURL");

			FlickrWrapper wrapper = new FlickrWrapper(url, "Flu", params);
			wrapper.setBagOfWords(new String[] { "Allergy", "Flu", "Dogs",
					"Dog", "Nature" });
			wrapper.run();

			while (wrapper.hasNext()) {
				STTPoint point = wrapper.next();
				FlickrWrapper.log.info(point.value + " , ");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		System.exit(0);
	}
}
