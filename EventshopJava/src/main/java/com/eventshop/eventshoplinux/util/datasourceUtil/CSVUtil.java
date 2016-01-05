package com.eventshop.eventshoplinux.util.datasourceUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;

public class CSVUtil {

	protected Log log = LogFactory.getLog(this.getClass().getName());

	public double[][] readDataFromCSVFile(FrameParameters params, String fileURL)
			throws IOException {
		MathContext context = new MathContext(5);
		Double expectedCols = (BigDecimal.valueOf(params.neLong))
				.subtract(BigDecimal.valueOf(params.swLong), context)
				.divide(BigDecimal.valueOf(params.longUnit), context)
				.doubleValue();
		Double expectedRows = (BigDecimal.valueOf(params.neLat))
				.subtract(BigDecimal.valueOf(params.swLat), context)
				.divide(BigDecimal.valueOf(params.latUnit), context)
				.doubleValue();

		double[][] tempdataGrid = new double[expectedRows.intValue()][expectedCols
				.intValue()];

		URL url = new URL(fileURL);
		InputStream stream = url.openStream();

		BufferedReader reader = null;
		try {
			// This is for file reader locally
			// reader = new BufferedReader(new FileReader(fileURL));
			reader = new BufferedReader(new InputStreamReader(stream));
			String myline = "";
			StringTokenizer vals;

			// Loop for Lat Long blocks
			int row = 0;
			int col = 0;
			for (double i = params.swLat; i < params.neLat; i = i
					+ params.latUnit) {
				myline = reader.readLine();
				vals = new StringTokenizer(myline, " ");
				// col = 0;
				if (vals.countTokens() != expectedCols)
					log.info("ERROR !! There should be " + expectedCols
							+ "Columns  in data file");
				else {
					// for(double j = params.swLong; j <
					// params.neLong-params.longUnit; j = j+params.longUnit) //I
					// DONT UNDERSTAND WHY THERE SHOULD BE A -1... but it seems
					// to be needed
					for (col = 0; col < expectedCols; col++) {
						String val = vals.nextToken();
						log.info("val:" + val);
						if (val.contains("e")) { // if the number is presented
													// in exponential notion
							int splitPoint = val.indexOf('e');
							double num = Double.parseDouble(val.substring(0,
									splitPoint - 1));
							double pw = Double.parseDouble(val
									.substring(splitPoint + 1));

							tempdataGrid[row][col] = num * Math.pow(10, pw);
						} else {
							tempdataGrid[row][col] = Double.parseDouble(val);
						}
						col++;
					}
				}
				row++;
			}
			log.info("number of rows: " + row + " number of cols: " + col);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} finally {
			reader.close();
		}
		return tempdataGrid;
	}

}
