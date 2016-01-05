package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.EmageIterator;

public class ResolutionMapper {

	protected Log log = LogFactory.getLog(this.getClass().getName());
	FrameParameters initResolution;
	FrameParameters finalResolution;

	/*
	 * //FrameParameter's fields long start; // Start time of this frame (single
	 * frame) long end; // End time of this frame
	 * 
	 * long timeWindow; // the size of time window for creating a frame long
	 * syncAtMilSec; // Offset to do synchronization
	 * 
	 * double latUnit; // Resolution of latitude double longUnit; // Resolution
	 * of longitude
	 * 
	 * double swLat; // SW corner of the bounding box double swLong; double
	 * neLat; // NE corner of the bounding box double neLong;
	 * 
	 * int numOfColumns; // The derived number of columns int numOfRows; // The
	 * derived number of rows
	 */

	long mSecsToSinkAt;

	ArrayList<Emage> EL;
	Emage lastKnownEmage;

	String theme;

	public enum SpatialMapper {
		sum, max, min, average, majority, linear_interpolation, repeat, split_uniform
	}

	public enum TemporalMapper {
		sum, max, min, average, latest, repeat, split_uniform
	}

	public ResolutionMapper(FrameParameters initP, FrameParameters finalP,
			String theme) {
		this.initResolution = initP;
		this.finalResolution = finalP;
		this.theme = theme;

		lastKnownEmage = new Emage(initP, theme);
	}

	public FrameParameters getFinalResolution() {
		return finalResolution;
	}

	public void setFinalResolution(FrameParameters finalResolution) {
		this.finalResolution = finalResolution;
	}

	public FrameParameters getInitResolution() {
		return initResolution;
	}

	public void setInitResolution(FrameParameters initResolution) {
		this.initResolution = initResolution;
	}

	public Emage doTemporalTranslation(EmageIterator Eitr, TemporalMapper TM) {
		Emage e;

		long tw1 = initResolution.timeWindow;
		long tw2 = finalResolution.timeWindow;

		// If one initFrame needs to be split into many
		if (tw1 > tw2) {
			if (Eitr.hasNext())
				lastKnownEmage = Eitr.next();
			e = splitEmage(lastKnownEmage, tw1, tw2, TM);
		} else {
			// If Many initFrames needs to be combined into one
			if (tw1 < tw2) {
				long endtime = finalResolution.end;
				boolean thisWindowDone = false;

				while (Eitr.hasNext() && !thisWindowDone) {
					Emage emage = Eitr.peek();

					if (emage.endTime.getTime() <= endtime) {
						Emage e1 = Eitr.next();
						EL.add(e1);
					} else
						thisWindowDone = true;
				}
				e = aggregateEmages(EL, tw1, tw2, TM);
			} else // the timewindows are the same
			{
				e = Eitr.next();
			}
		}
		lastKnownEmage = e;
		return e;

	}

	Emage doSpatialTranslation(Emage e, SpatialMapper SM) {
		Emage mappedEmage = new Emage(finalResolution, theme);

		// lets do the spatial mapping now
		// the desired output image's template
		int nRowsFinal = (int) Math
				.ceil((finalResolution.neLat - finalResolution.swLat)
						/ finalResolution.latUnit);
		int nColsFinal = (int) Math
				.ceil((finalResolution.neLong - finalResolution.swLong)
						/ finalResolution.longUnit);
		int nRowsInit = (int) Math
				.ceil((initResolution.neLat - initResolution.swLat)
						/ initResolution.latUnit);
		int nColsInit = (int) Math
				.ceil((initResolution.neLong - initResolution.swLong)
						/ initResolution.longUnit);

		double[][] dummyOut = new double[nRowsFinal][nColsFinal];

		// initialize the image data grid to zeros
		for (int i = 0; i < nRowsFinal; i++) {
			for (int j = 0; j < nColsFinal; j++) {
				dummyOut[i][j] = 0;

			}
		}
		double latScale = finalResolution.latUnit / initResolution.latUnit;
		double longScale = finalResolution.longUnit / initResolution.longUnit;

		// the image needs to be mapped between [mapped2minRow, mapped2minCol]
		// to [mapped2MaxRow, mapped2MaxCol]
		int mapped2minRow = (int) ((finalResolution.neLat - initResolution.neLat) / finalResolution.latUnit);
		int mapped2minCol = (int) ((initResolution.swLong - finalResolution.swLong) / finalResolution.latUnit);

		int mapped2MaxRow = (int) ((-initResolution.swLat + finalResolution.neLat) / finalResolution.latUnit);
		int mapped2MaxCol = (int) ((initResolution.neLong - finalResolution.swLong) / finalResolution.latUnit);

		// checking to make sure they are not mapping outside the acceptable
		// final resolution boundaries
		mapped2minRow = (mapped2minRow < 0) ? 0 : mapped2minRow;
		mapped2minRow = (mapped2minRow > nRowsFinal) ? nRowsFinal
				: mapped2minRow;

		mapped2minCol = (mapped2minCol < 0) ? 0 : mapped2minCol;
		mapped2minCol = (mapped2minCol > nColsFinal) ? nColsFinal
				: mapped2minCol;

		mapped2MaxRow = (mapped2MaxRow < 0) ? 0 : mapped2MaxRow;
		mapped2MaxRow = (mapped2MaxRow > nRowsFinal) ? nRowsFinal
				: mapped2MaxRow;

		mapped2MaxCol = (mapped2MaxCol < 0) ? 0 : mapped2MaxCol;
		mapped2MaxCol = (mapped2MaxCol > nColsFinal) ? nColsFinal
				: mapped2MaxCol;

		// the data for this mapping needs to come from [mappedFromminRow,
		// mappedFromminCol] to [mappedFromMaxRow, mappedFromMaxCol]
		int mappedFromMinRow = (int) ((initResolution.neLat - finalResolution.neLat) / initResolution.latUnit);
		int mappedFromMinCol = (int) ((finalResolution.swLong - initResolution.swLong) / initResolution.latUnit);

		int mappedFromMaxRow = (int) ((initResolution.neLat - finalResolution.swLat) / initResolution.latUnit);
		int mappedFromMaxCol = (int) ((finalResolution.neLong - initResolution.swLong) / initResolution.latUnit);

		// checking to make sure they are not mapping outside the acceptable
		// final resolution boundaries
		mappedFromMinRow = (mappedFromMinRow < 0) ? 0 : mappedFromMinRow;
		mappedFromMinRow = (mappedFromMinRow > nRowsFinal) ? nRowsInit
				: mappedFromMinRow;

		mappedFromMinCol = (mappedFromMinCol < 0) ? 0 : mappedFromMinCol;
		mappedFromMinCol = (mappedFromMinCol > mappedFromMinCol) ? nColsInit
				: mappedFromMinCol;

		mappedFromMaxRow = (mappedFromMaxRow < 0) ? 0 : mappedFromMaxRow;
		mappedFromMaxRow = (mappedFromMaxRow > nRowsFinal) ? nRowsInit
				: mappedFromMaxRow;

		mappedFromMaxCol = (mappedFromMaxCol < 0) ? 0 : mappedFromMaxCol;
		mappedFromMaxCol = (mappedFromMaxCol > nColsFinal) ? nColsInit
				: mappedFromMaxCol;

		// filling up the values
		for (int i = mapped2minRow; i < mapped2MaxRow; i++) {
			for (int j = mapped2minCol; j < mapped2MaxCol; j++) {
				// lets assume that you cannot stretch on one dimension and
				// shrink on another
				if (latScale > 1 || longScale > 1)// latScale=finalResolution.latUnit/initResolution.latUnit;
				// i.e. fine2Coarse... many initialArray values will map to 1
				{
					double[][] vals = new double[(int) Math.ceil(latScale)][(int) Math
							.ceil(longScale)];
					for (int k = 0; k < latScale; k++) {
						for (int l = 0; l < longScale; l++) {
							vals[k][l] = e.image[mappedFromMinRow
									+ (int) ((i - mapped2minRow) * latScale)
									+ k][mappedFromMinCol
									+ (int) ((j - mapped2minCol) * longScale)
									+ l];
						}
					}

					dummyOut[i][j] = fine2Coarse(vals, SM);
				} else {
					if (latScale < 1 || longScale < 1) // i.e. coarse2Fine...1
														// initialArray value
														// will map to many in
														// finalArray
					{
						int nRows2StretchTo = (int) (1 / latScale);
						int nCols2StretchTo = (int) (1 / longScale);

						double pInCols = ((double) ((j - mapped2minCol) % nCols2StretchTo) / (double) nCols2StretchTo);
						double pInRows = (double) ((i - mapped2minRow) % nRows2StretchTo)
								/ (double) nRows2StretchTo;

						int initsRow2ReadFrom = (int) ((i - mapped2minRow) * latScale);
						int initsCol2ReadFrom = (int) ((j - mapped2minCol) * longScale);

						double valCur = e.image[initsRow2ReadFrom][initsCol2ReadFrom];
						double valDiagonal = valCur;
						if (initsRow2ReadFrom + 1 < nRowsInit
								&& initsCol2ReadFrom + 1 < nColsInit)
							valDiagonal = e.image[initsRow2ReadFrom + 1][initsCol2ReadFrom + 1];

						if (SM.equals(SpatialMapper.split_uniform))
							dummyOut[i][j] = coarse2Fine(valCur, valDiagonal,
									nRows2StretchTo, nCols2StretchTo, SM);
						else
							dummyOut[i][j] = coarse2Fine(valCur, valDiagonal,
									pInRows, pInCols, SM);
					} else { // no resolution change
						int initsRow2ReadFrom = (int) ((i - mapped2minRow) * latScale);
						int initsCol2ReadFrom = (int) ((j - mapped2minCol) * longScale);

						if (initsCol2ReadFrom >= 0
								&& initsCol2ReadFrom < nColsInit
								&& initsRow2ReadFrom >= 0
								&& initsRow2ReadFrom < nRowsInit)
							dummyOut[i][j] = e.image[initsRow2ReadFrom][initsCol2ReadFrom];
						else
							log.info("No data found for " + i + " , " + j);
					}
				}
			}
		}

		mappedEmage.image = dummyOut;

		return mappedEmage;

	}

	Emage doTheTranslation(EmageIterator Eitr, SpatialMapper SM,
			TemporalMapper TM) {
		Emage e;
		EL = new ArrayList<Emage>();

		e = doTemporalTranslation(Eitr, TM);

		Emage mappedEmage = new Emage(finalResolution, theme);
		mappedEmage = doSpatialTranslation(e, SM);

		return mappedEmage;
	}

	private Emage splitEmage(Emage lastKnownEmage2, long tw1, long tw2,
			TemporalMapper TM) {
		int num2SplitTo = (int) (tw1 / tw2);
		double[][] tempImage = new double[lastKnownEmage2.image.length][lastKnownEmage2.image[0].length];

		Emage e = new Emage(initResolution, lastKnownEmage2.theme);

		switch (TM) {
		case repeat: {
			return lastKnownEmage2;
		}
		case split_uniform: {
			for (int i = 0; i < lastKnownEmage2.image.length; i++) {
				for (int j = 0; j < lastKnownEmage2.image[0].length; j++) {
					tempImage[i][j] = lastKnownEmage2.image[i][j] / num2SplitTo;
				}

			}
			e.image = tempImage;
			break;
		}
		default: {
			log.info("Supplied Temporal Mapper not applicable to aggregation of Emages");
			return null;
		}
		}
		return e;
	}

	public Emage aggregateEmages(ArrayList<Emage> EL, long tw1, long tw2,
			TemporalMapper TM) {
		Emage e = new Emage(finalResolution, theme);
		log.info("size: " + EL.size());

		if (EL.size() > 0) {
			double[][] tempImage = null;

			for (int i = 0; i < EL.size(); i++) {
				if ((EL.get(i).image.length != EL.get(0).image.length)
						|| (EL.get(i).image[0].length != EL.get(0).image[0].length)) {
					log.info("Supplied Emages for aggregation are at different resolution. Error.");
					return null;
				} else {
					tempImage = new double[EL.get(0).image.length][EL.get(0).image[0].length];
				}
			}

			switch (TM) {
			case sum: {
				for (int i = 0; i < EL.get(0).image.length; i++) {
					for (int j = 0; j < EL.get(0).image[0].length; j++) {
						for (int k = 0; k < EL.size(); k++) {

							tempImage[i][j] += EL.get(k).image[i][j];
						}
					}
				}
				break;
			}
			case max: {
				for (int i = 0; i < EL.get(0).image.length; i++) {
					for (int j = 0; j < EL.get(0).image[0].length; j++) {
						double curMax = 0.0;
						for (int k = 0; k < EL.size(); k++) {
							if (EL.get(k).image[i][j] > curMax)
								curMax = EL.get(k).image[i][j];
						}
						tempImage[i][j] += curMax;
					}
				}
				break;
			}
			case min: {
				for (int i = 0; i < EL.get(0).image.length; i++) {
					for (int j = 0; j < EL.get(0).image[0].length; j++) {
						double curMin = 99999999.0;
						for (int k = 0; k < EL.size(); k++) {
							if (EL.get(k).image[i][j] < curMin)
								curMin = EL.get(k).image[i][j];
						}
						tempImage[i][j] += curMin;
					}
				}
				break;
			}
			case average: {
				for (int i = 0; i < EL.get(0).image.length; i++) {
					for (int j = 0; j < EL.get(0).image[0].length; j++) {
						for (int k = 0; k < EL.size(); k++) {
							tempImage[i][j] += EL.get(k).image[i][j];
						}
						tempImage[i][j] = tempImage[i][j] / EL.size();
					}
				}

				break;
			}
			case latest: {
				tempImage = EL.get(EL.size()).image;
				break;
			}
			default: {
				log.info("Supplied Temporal Mapper not applicable to Aggregate fn of Emages");
				return null;
			}
			}

			e = new Emage(initResolution, EL.get(0).theme);
			e.image = tempImage;
		} else {
			log.info("EmageList is empty...returning a zero valued image");
			e = new Emage(initResolution, theme);
			for (int i = 0; i < e.image.length; i++) {
				for (int j = 0; j < e.image[0].length; j++)
					e.image[i][j] = 0;
			}

		}
		return e;
	}

	public Point getRectifiedCoords(Point p, int offsetLat, int offsetLong,
			double latScale, double longScale, SpatialMapper SM) {
		Point rectPoint = new Point();

		rectPoint.x = (int) ((p.x - offsetLat) / latScale);
		rectPoint.y = (int) ((p.y - offsetLong) / longScale);

		return rectPoint;
	}

	// move to Emage.java
	// public SpatialBound pixel2geoBound(Point pp)
	// {
	// SpatialBound sb=new SpatialBound();
	// sb.sw.x=(int)((pp.x-finalResolution.swLat)*finalResolution.latUnit);
	// sb.sw.y=(int)((pp.y-finalResolution.swLong)*finalResolution.longUnit);
	//
	// sb.ne.x=(int)((pp.x+1-finalResolution.swLat)*finalResolution.latUnit);
	// sb.ne.y=(int)((pp.y+1-finalResolution.swLong)*finalResolution.longUnit);
	//
	// return sb;
	// }

	public double coarse2Fine(double valCur, double valDiagonal,
			double pInRows, double pInCols, SpatialMapper SM) {
		double out = 0;
		// Types considered: sum, max, min, average, majority
		switch (SM) {
		case linear_interpolation: {

			out = (((pInRows) * valDiagonal + (1 - pInRows) * valCur) + ((pInCols)
					* valDiagonal + (1 - pInCols) * valCur)) * 0.5;

			break;
		}
		case split_uniform: {

			out = valCur / (pInRows * pInCols);// pInRows in this case will
												// simply be the stretch factor

			break;
		}
		case repeat: {

			out = valCur;

			break;
		}
		default: {
			log.info("Illegal Spatial Mapper specified for coarse2Fine translation");
		}

		}
		return out;
	}

	public double fine2Coarse(double vals[][], SpatialMapper SM) {
		double out = 0;

		// Types considered: sum, max, min, average, majority
		switch (SM) {
		case sum: {
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals[0].length; j++) {
					out += vals[i][j];
				}
			}
			break;
		}

		case max: {
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals[0].length; j++) {
					if (vals[i][j] > out)
						out = vals[i][j];
				}
			}
			break;
		}
		case min: {
			out = 999999999.0;
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals[0].length; j++) {
					if (vals[i][j] < out)
						out = vals[i][j];
				}
			}
			break;
		}
		case average: {
			int count = 0;
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals[0].length; j++) {
					out += vals[i][j];
					count++;
				}
			}
			out = out / count;
			break;
		}
		case majority: {
			double tempArray[] = new double[vals.length * vals[0].length];
			int count = 0;
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals[0].length; j++) {

					tempArray[count] = vals[i][j];
					count++;
				}
			}
			out = mode(tempArray);
			break;
		}
		default: {
			log.info("Illegal Spatial Mapper specified for fine2Coarse translation");
		}

		}
		return out;
	}

	public double mode(double values[]) {
		int i = 0;
		int next = 0;
		double hold;
		double longest = 0;
		double cur = 0;
		double m = 0;
		boolean switched = true;

		// sort the array of integrs
		while (switched && next < values.length) {
			switched = false;

			for (i = next++; i < values.length - 1; i++)
				if (values[i] > values[i + 1]) {
					switched = true;
					hold = values[i];
					values[i] = values[i + 1];
					values[i + 1] = hold;
				}
		}

		// find the longest run of the same number
		for (i = 0; i < values.length - 1; i++) {
			if (values[i] == values[i + 1] && i < values.length - 2)
				cur++;
			else {
				if (cur > longest) {
					m = values[i];
					longest = cur;
					cur = 0;
				}
			}

		}

		return m;
	}

	public Point geoBound2MatchingPixels() {
		return null;
	}
}
