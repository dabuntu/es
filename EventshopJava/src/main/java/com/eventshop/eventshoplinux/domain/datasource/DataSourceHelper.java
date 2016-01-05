package com.eventshop.eventshoplinux.domain.datasource;

import com.eventshop.eventshoplinux.domain.datasource.DataSource.DataFormat;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.CSVEmageIterator;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.EmageIterator;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.STTEmageIterator;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.VisualEmageIterator;
import com.eventshop.eventshoplinux.domain.datasource.simulator.DistParameters;
import com.eventshop.eventshoplinux.domain.datasource.simulator.GaussianParameters2D;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;
import com.eventshop.eventshoplinux.util.datasourceUtil.simulator.Simulator.Kernel;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.FlickrWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.SimDataWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.TwitterWrapper;
import com.eventshop.eventshoplinux.util.datasourceUtil.wrapper.Wrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.eventshop.eventshoplinux.constant.Constant.*;

public class DataSourceHelper {

	protected Log log = LogFactory.getLog(this.getClass().getName());

	ArrayList<DataProcess> dataProcesses = new ArrayList<DataProcess>();

	public void runDatasources(List<DataSource> datasrcList, String runflag) {

		ListIterator listIterator = datasrcList.listIterator();

		while (listIterator.hasNext()) {

			DataSource dsrc = (DataSource) listIterator.next();
			startDataProcess(dsrc, runflag);

		}

	}

	private void preRegisterDataSources(ArrayList<DataSource> dsList) {

		int preRegisteredSrcCount = dsList.size();
	}

	public void startDataProcess(DataSource src, String runflag) {
		DataFormat format = src.srcFormat;

		String imgBasePath = Config.getProperty(CONTEXT) + RESULT_DS;
		String tempDir = Config.getProperty(TEMPDIR);
		if (format == DataFormat.stream) {
			Wrapper wrapper = null;
			if (src.supportedWrapper.equals(TWITTER)) {
				log.info("before creation");
				wrapper = new TwitterWrapper(src.url, src.srcTheme,
						src.initParam, true);
				log.info("wrapper created");

				String[] words = new String[src.bagOfWords.size()];
				src.bagOfWords.toArray(words);
				((TwitterWrapper) wrapper).setBagOfWords(words);
			} else if (src.supportedWrapper.equals(FLICKER)) {
				wrapper = new FlickrWrapper(src.url, src.srcTheme,
						src.initParam);
				String[] words = new String[src.bagOfWords.size()];
				src.bagOfWords.toArray(words);
				((FlickrWrapper) wrapper).setBagOfWords(words);
			} else if (src.supportedWrapper.equals(SIM)) {
				// Create Distribution Parameters and Generators
				ArrayList<DistParameters> dParams = new ArrayList<DistParameters>();
				// LA
				GaussianParameters2D gParam = new GaussianParameters2D(34.1,
						-118.2, 3.0, 3.0, 200);
				dParams.add(gParam);
				// SF
				gParam = new GaussianParameters2D(37.8, -122.4, 2.0, 2.0, 200);
				dParams.add(gParam);
				// Seattle
				gParam = new GaussianParameters2D(47.6, -122.3, 1.0, 1.0, 100);
				dParams.add(gParam);
				// NYC
				gParam = new GaussianParameters2D(40.8, -74.0, 5.0, 5.0, 200);
				dParams.add(gParam);

				// Wrappers
				wrapper = new SimDataWrapper(src.url, src.srcTheme,
						src.initParam, Kernel.gaussian, dParams);// changed to
																	// initParam...vks:Aug30,2011
			}

			// Add EmageIterator
			EmageIterator eit = new STTEmageIterator();
			eit.setSTTPointIterator(wrapper);
			eit.setSrcID(Integer.parseInt(src.srcID));

			// Add output filename

			// change it to be query independent: 08/19/2011 Mingyan
			String filepath = tempDir + PATH_DS + src.srcID + UNDERSCORE
					+ src.srcName;

			// Create st merger
			STMerger merger = null;
			/*
			 * if(!src.finalParam.equals(src.initParam)) {
			 * log.info("The initial and final frame parameters are different");
			 * 
			 * merger = new STMerger(src.finalParam); SpatialMapper sp = null;
			 * TemporalMapper tp = null; if(src.initParam.latUnit <
			 * src.finalParam.latUnit || src.initParam.longUnit <
			 * src.finalParam.longUnit) sp = SpatialMapper.sum; else
			 * if(src.initParam.latUnit > src.finalParam.latUnit ||
			 * src.initParam.longUnit > src.finalParam.longUnit) sp =
			 * SpatialMapper.repeat;
			 * 
			 * if(src.initParam.timeWindow < src.finalParam.timeWindow) tp =
			 * TemporalMapper.sum; else if(src.initParam.timeWindow >
			 * src.finalParam.timeWindow) tp = TemporalMapper.repeat;
			 * 
			 * merger.addIterator(eit, sp, tp);
			 * merger.setMergingExpression("mulED(R0,1)"); }
			 */

			// Add data processors
			DataProcess process = new DataProcess(merger, eit, wrapper,
					filepath, imgBasePath + DS_PATH + src.srcID, src.srcID);
			dataProcesses.add(process);
			log.info("Add new DataProcess to the list");

			// Stop data processes
			if (!runflag.equals(CONTROLFLAG)) {
				stopDatasources(dataProcesses);
			} else {
				// Start the data collecting process
				new Thread(process).start();
			}

		} else if (format == DataFormat.visual) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						src.visualParam.tranMatPath));
				String line = reader.readLine();
				String[] dim = line.split(COMMA);
				double[][] tranMat = new double[Integer.parseInt(dim[0])][Integer
						.parseInt(dim[1])];
				int row = 0;
				String tranMatStr = TRANS_MATRX_NWLINE;
				while ((line = reader.readLine()) != null) {
					String[] numbers = line.split(COMMA);
					for (int col = 0; col < numbers.length; ++col)
						tranMat[row][col] = Double.parseDouble(numbers[col]
								.trim());
					row++;
				}
				reader.close();

				for (int x = 0; x < tranMat.length; x++) {
					for (int y = 0; y < tranMat[x].length; y++)
						tranMatStr += tranMat[x][y] + COMMA_BLANK;
					tranMatStr += NWLINE;
				}
				log.info(tranMatStr);

				reader = new BufferedReader(new FileReader(
						src.visualParam.colorMatPath));
				line = reader.readLine();
				dim = line.split(COMMA);
				double[][] colorMat = new double[Integer.parseInt(dim[0])][Integer
						.parseInt(dim[1])];
				row = 0;
				while ((line = reader.readLine()) != null) {
					String[] numbers = line.split(COMMA);
					for (int col = 0; col < numbers.length; ++col)
						colorMat[row][col] = Double.parseDouble(numbers[col]
								.trim());
					row++;
				}
				reader.close();

				String colorMatStr = COLOR_MATRX_NWLINE;
				for (int x = 0; x < colorMat.length; x++) {
					for (int y = 0; y < colorMat[x].length; y++)
						colorMatStr += colorMat[x][y] + COMMA_BLANK;
					colorMatStr += NWLINE;
				}
				log.info(colorMatStr);

				VisualEmageIterator veit = new VisualEmageIterator(
						src.initParam, src.srcTheme, src.url, tranMat,
						colorMat, src.visualParam.maskPath,
						src.visualParam.ignoreSinceNumber);
				veit.setSrcID(Integer.parseInt(src.srcID));

				// Add output filename
				// Change it to be query independent: 08/19/2011 Mingyan
				String filepath = tempDir + PATH_DS + src.srcID + UNDERSCORE
						+ src.srcName;

				// Create st merger
				STMerger merger = null;
				/*
				 * if(!src.finalParam.equals(src.initParam)) { merger = new
				 * STMerger(src.finalParam); SpatialMapper sp = null;
				 * TemporalMapper tp = null; if(src.initParam.latUnit <
				 * src.finalParam.latUnit || src.initParam.longUnit <
				 * src.finalParam.longUnit) sp = SpatialMapper.average; else
				 * if(src.initParam.latUnit > src.finalParam.latUnit ||
				 * src.initParam.longUnit > src.finalParam.longUnit) sp =
				 * SpatialMapper.repeat;
				 * 
				 * if(src.initParam.timeWindow < src.finalParam.timeWindow) tp =
				 * TemporalMapper.sum; else if(src.initParam.timeWindow >
				 * src.finalParam.timeWindow) tp = TemporalMapper.repeat;
				 * 
				 * merger.addIterator(veit, sp, tp);
				 * merger.setMergingExpression("mulED(R0,1)"); }
				 */

				// Add data processors
				DataProcess process = new DataProcess(merger, veit, null,
						filepath, imgBasePath + DS_PATH + src.srcID, src.srcID);
				dataProcesses.add(process);

				// Stop data processes
				if (!runflag.equals(CONTROLFLAG)) {
					stopDatasources(dataProcesses);
				} else {
					// Start the data collecting process
					new Thread(process).start();
				}

			} catch (NumberFormatException e) {
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		} else if (format == DataFormat.file) {
			String filepath = tempDir + PATH_DS + src.srcID + UNDERSCORE
					+ src.srcName;
			CSVEmageIterator csvEIter = new CSVEmageIterator(src.initParam,
					src.srcTheme, src.url);
			csvEIter.setSrcID(Integer.parseInt(src.srcID));

			// Create st merger
			STMerger merger = null;
			/*
			 * if(!src.finalParam.equals(src.initParam)) { merger = new
			 * STMerger(src.finalParam); SpatialMapper sp = null; TemporalMapper
			 * tp = null; if(src.initParam.latUnit < src.finalParam.latUnit ||
			 * src.initParam.longUnit < src.finalParam.longUnit) sp =
			 * SpatialMapper.average; else if(src.initParam.latUnit >
			 * src.finalParam.latUnit || src.initParam.longUnit >
			 * src.finalParam.longUnit) sp = SpatialMapper.repeat;
			 * 
			 * if(src.initParam.timeWindow < src.finalParam.timeWindow) tp =
			 * TemporalMapper.sum; else if(src.initParam.timeWindow >
			 * src.finalParam.timeWindow) tp = TemporalMapper.repeat;
			 * 
			 * merger.addIterator(csvEIter, sp, tp);
			 * merger.setMergingExpression("mulED(R0,1)"); }
			 */

			DataProcess process = new DataProcess(merger, csvEIter, null,
					filepath, imgBasePath + DS_PATH + src.srcID, src.srcID);
			dataProcesses.add(process);

			// Stop data processes
			if (!runflag.equals(CONTROLFLAG)) {
				stopDatasources(dataProcesses);
			} else {
				// Start the data collecting process
				new Thread(process).start();
			}
		}

	}

	// Stopping the Datasources
	// Checking the flag and making the thread Sleep
	public void stopDatasources(List<DataProcess> dataProcesses) {

		for (int i = 0; i < dataProcesses.size(); i++) {
			dataProcesses.get(i).stop();
			while (dataProcesses.get(i).isRunning)
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}

		}

	}
}
