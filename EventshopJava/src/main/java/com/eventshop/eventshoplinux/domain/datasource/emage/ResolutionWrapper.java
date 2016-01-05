package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;

//This class is used for resolution mapping from the datasource to emage
//This is different from ResolutionMapper.java which used to map the resolution between emages
public class ResolutionWrapper {

	protected Log log = LogFactory.getLog(this.getClass().getName());

	// public enum SpatialWrapper {
	// sum, max, min, average, majority, linear_interpolation, repeat,
	// split_uniform, nearest
	// }
	// public enum TemporalWrapper {
	// sum, max, min, average, latest, repeat, split_uniform
	// }
	public enum DatasourceType {
		point, region, grid, category// , path, overlapRegion, overlapGrid,
										// nonUniformGrid
	}

	public enum SpatialWrapper {
		sum, min, max, average, count, most_freq, majority, // for point to grid
		linear_interpolation, repeat, split_uniform, nearest
	}

	// The method to determine how the cell will be assigned a value when more
	// than one feature falls within a cell.
	//
	// MOST_FREQUENT If there is more than one feature within the cell, the one
	// with the most common attribute, in <field>, is assigned to the cell. If
	// they have the same number of common attributes, the one with the lowest
	// FID is used.
	// SUM The sum of the attributes of all the points within the cell (not
	// valid for string data).
	// MEAN The mean of the attributes of all the points within the cell (not
	// valid for string data).
	// STANDARD_DEVIATION The standard deviation of attributes of all the points
	// within the cell. If there are less than two points in the cell, the
	// cellis assigned NoData (not valid for string data).
	// MAXIMUM The maximum value of the attributes of the points within the cell
	// (not valid for string data).
	// MINIMUM The minimum value of the attributes of the points within the cell
	// (not valid for string data).
	// RANGE The range of the attributes of the points within the cell (not
	// valid for string data).
	// COUNT The number of points within the cell.

	private DatasourceType dsType;
	// Emage emage;
	private FrameParameters emageResolution;
	private SpatialWrapper SW;
	// private SpatialWrapper stw;

	private double[][] emageGrid;
	private int numRow, numCol;

	// For point data type
	private ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

	// For grid data type
	private DataGrid dataGrid;

	public ResolutionWrapper(DatasourceType ds, FrameParameters fp,
			SpatialWrapper SW) {
		this.dsType = ds;
		this.emageResolution = fp;
		this.SW = SW;
		this.numRow = fp.numOfRows;
		this.numCol = fp.numOfColumns;
		this.emageGrid = createEmptyDataGrid(numRow, numCol);

	}

	public void setDataPoints(ArrayList<DataPoint> ds) {
		dataPoints = ds;
	}

	public ArrayList<DataPoint> getDataPoints() {
		return dataPoints;
	}

	public void setDataGrid(DataGrid grid) {
		dataGrid = grid;
	}

	public DataGrid getDataGrid() {
		return dataGrid;
	}

	public double[][] getEmageGrid() {
		return emageGrid;
	}

	public FrameParameters getEmageResolution() {
		return emageResolution;
	}

	public void setEmageResolution(FrameParameters emageResolution) {
		this.emageResolution = emageResolution;
	}

	public double[][] replaceValue(double[][] grid, double oldVal, double newVal) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++)
				if (grid[i][j] == oldVal)
					grid[i][j] = newVal;
		}
		return grid;
	}

	public double[][] setDataGrid2Zero(double[][] grid) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++)
				grid[i][j] = 0.0;
		}
		return grid;
	}

	public double[][] setDataGrid2Value(double[][] grid, double val) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++)
				grid[i][j] = val;
		}
		return grid;
	}

	public double[][] createEmptyDataGrid(int row, int col) {
		double[][] grid = new double[row][col];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++)
				grid[i][j] = Double.NaN;
		}
		return grid;
	}

	public double[][] createZeroDataGrid(int row, int col) {
		double[][] grid = new double[row][col];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++)
				grid[i][j] = 0.0;
		}
		return grid;
	}

	public void reset() {

	}

	public void pointAggregation() {
		// CommonUtil.printNonZeroCell(datagrid);
		int x = 0;
		if (dataPoints != null) {
			switch (SW) {
			case sum: {
				// emageGrid = setDataGrid2Zero(emageGrid);
				setDataGrid2Value(emageGrid, Double.NaN);
				for (DataPoint p : dataPoints) {
					System.out.println("emagegrid xIndex = " + p.xEmageIndex);
					System.out.println("emagegrid yIndex = " + p.yEmageIndex);
					if (Double.isNaN(emageGrid[p.xEmageIndex][p.yEmageIndex])) {
						emageGrid[p.xEmageIndex][p.yEmageIndex] = p.value;
						x++;
					}

					else {
						emageGrid[p.xEmageIndex][p.yEmageIndex] += p.value;
						x++;
					}
					System.out.println("point agg count is " + x);
					if (x == 103)
						System.out.println("this is it");
				}
				break;
			} // end sum
			case min: {
				// setDataGrid2Value(emageGrid, Double.POSITIVE_INFINITY);
				setDataGrid2Value(emageGrid, Double.NaN);

				for (DataPoint p : dataPoints) {
					// System.out.println("data point value: " + p.value + "," +
					// p.xEmageIndex + "," + p.yEmageIndex);
					if (Double.isNaN(emageGrid[p.xEmageIndex][p.yEmageIndex])
							|| (emageGrid[p.xEmageIndex][p.yEmageIndex] > p.value))
						emageGrid[p.xEmageIndex][p.yEmageIndex] = p.value;
				}
				// replace min value of unknown data with NaN
				replaceValue(emageGrid, Double.POSITIVE_INFINITY, Double.NaN);
				break;
			} // end min
			case max: {
				setDataGrid2Value(emageGrid, Double.NaN);
				for (DataPoint p : dataPoints) {
					if (Double.isNaN(emageGrid[p.xEmageIndex][p.yEmageIndex])
							|| (emageGrid[p.xEmageIndex][p.yEmageIndex] < p.value))
						emageGrid[p.xEmageIndex][p.yEmageIndex] = p.value;
				}
				break;
			} // end max
			case average: {
				// emageGrid = setDataGrid2Zero(emageGrid);
				setDataGrid2Value(emageGrid, Double.NaN);
				double[][] count = this.createZeroDataGrid(numRow, numCol);
				for (DataPoint p : dataPoints) {
					log.info(emageGrid[p.xEmageIndex][p.yEmageIndex] + ", "
							+ count[p.xEmageIndex][p.yEmageIndex] + ", "
							+ p.value);
					if (Double.isNaN(emageGrid[p.xEmageIndex][p.yEmageIndex])) {
						emageGrid[p.xEmageIndex][p.yEmageIndex] = p.value;
						count[p.xEmageIndex][p.yEmageIndex] = 1.0;
					} else {
						emageGrid[p.xEmageIndex][p.yEmageIndex] += p.value;
						count[p.xEmageIndex][p.yEmageIndex] += 1.0;
					}
					// log.info("after +++"
					// +emageGrid[p.xEmageIndex][p.yEmageIndex] + ", " +
					// count[p.xEmageIndex][p.yEmageIndex] + ", " + p.value);

				}
				for (int i = 0; i < numRow; i++) {
					for (int j = 0; j < numCol; j++) {
						if (!Double.isNaN(count[i][j]) && count[i][j] > 0.0) {
							emageGrid[i][j] = emageGrid[i][j] / count[i][j];
							log.info("---" + emageGrid[i][j]);

						}
						// else
						// emageGrid[i][j] = Double.NaN; // for missing value
					}
				}
				break;
			} // end average
			case count: {
				emageGrid = setDataGrid2Zero(emageGrid);
				for (DataPoint p : dataPoints) {
					emageGrid[p.xEmageIndex][p.yEmageIndex] += 1.0;
				}
				break;
			} // end count
			case most_freq: {
				List<Double>[] dataArray = new List[numRow * numCol];
				for (int i = 0; i < dataArray.length; i++) {
					dataArray[i] = new ArrayList<Double>();
				}
				for (DataPoint p : dataPoints) {
					int index = (p.xEmageIndex * numCol) + p.yEmageIndex;
					dataArray[index].add(p.value);
				}
				for (int i = 0; i < dataArray.length; i++) {
					Collections.sort(dataArray[i]);
					emageGrid[i / numCol][i % numCol] = getMostFreq(dataArray[i]);
				}
				break;
			} // end most_freq
			case majority: {
				List<Double>[] dataArray = new List[numRow * numCol];
				for (int i = 0; i < dataArray.length; i++) {
					dataArray[i] = new ArrayList<Double>();
				}
				for (DataPoint p : dataPoints) {
					int index = (p.xEmageIndex * numCol) + p.yEmageIndex;
					dataArray[index].add(p.value);
				}
				for (int i = 0; i < dataArray.length; i++) {
					Collections.sort(dataArray[i]);
					emageGrid[i / numCol][i % numCol] = getMajority(dataArray[i]);
				}
				break;
			} // end majority
				// case nearest:{
				// for(int i = 0; i < numRow; i++){
				// for(int j = 0; j < numCol; j++){
				// emageGrid[i][j]= 255/(Math.pow(2,0.1* nearestLoc(i,j)));
				// }
				// }
				// break;
				// }
			default: {
				log.error("invalid spatial-temporal wrapper");
			}
			}
		} else {
			log.info("There is no data points");
		}
	}

	// return the most frequency value in the the array
	// list has to be sorted
	public double getMostFreq(List<Double> list) {
		double pop = 0;
		if (!list.isEmpty()) {
			pop = list.get(0);
			int count = 1;
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i) == pop) {
					count++;
				} else {
					if (i + count < list.size()
							&& list.get(i).equals(list.get(i + count))) {
						pop = list.get(i);
						i = i + count;
						count++;
					}
				}
			}
		}
		return pop;
	}

	// return the majority value in the array (this value has to appear more
	// than half times)
	// list has to be sorted
	public double getMajority(List<Double> list) {
		double pop = 0;
		if (!list.isEmpty()) {
			pop = list.get(0);
			int count = 1;
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i) == pop) {
					count++;
					if (count > list.size() / 2)
						break;
				} else {
					if (i + count < list.size()
							&& list.get(i).equals(list.get(i + count))) {
						pop = list.get(i);
						i = i + count;
						count++;
					}
				}
			}
			if (count <= list.size() / 2)
				pop = 0;
		}
		return pop;
	}

	public double coarse2Fine(double valCur, double valDiagonal,
			double pInRows, double pInCols, SpatialWrapper sw) {
		double out = 0;
		switch (sw) {
		case linear_interpolation: {

			out = (((pInRows) * valDiagonal + (1 - pInRows) * valCur) + ((pInCols)
					* valDiagonal + (1 - pInCols) * valCur)) * 0.5;

			break;
		}
		case split_uniform: {

			out = valCur / (pInRows * pInCols);// pInRows in this case will
												// simply be the stretch factor
			log.info("-------------" + sw.name() + ": valCur" + valCur
					+ ", pInRows:" + pInRows + ", pInCols:" + pInCols
					+ ", output: " + out);
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

	public double fine2Coarse(double vals[][], SpatialWrapper SM) {
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

	public void doTransformation() {
		switch (dsType) {

		case point: {
			if (!dataPoints.isEmpty())
				pointAggregation();
			break;
		} // point

		case grid: {
			if (dataGrid != null)
				gridTransformation();
			break;
		} // end grid

		case region: {
			break;
		} // region

		case category: {
			break;
		} // end category

		default: {
			System.out.println("invalid datasource type");
		}
		}
		// CommonUtil.printNonZeroCell(emageGrid);

		// return emageGrid;
	}

	public double[][] gridTransformation() {
		// lets do the spatial mapping now
		// the desired output image's template
		int nRowsFinal = (int) Math
				.ceil((emageResolution.neLat - emageResolution.swLat)
						/ emageResolution.latUnit);
		int nColsFinal = (int) Math
				.ceil((emageResolution.neLong - emageResolution.swLong)
						/ emageResolution.longUnit);
		int nRowsInit = dataGrid.row;
		int nColsInit = dataGrid.col;

		emageGrid = createEmptyDataGrid(nRowsFinal, nColsFinal);
		// // If scale > 1, fine2coarse
		// // If scale < 1, coarse2fine
		double rowScale = (double) nRowsInit / (double) nRowsFinal;
		double colScale = (double) nColsInit / (double) nColsFinal;
		log.info("iRow,iCol,fRow,fCol: " + nRowsInit + "," + nColsInit + ","
				+ nRowsFinal + "," + nColsFinal + "  rowScale, colScale: "
				+ rowScale + "," + colScale);

		// the image needs to be mapped between [mapped2minRow, mapped2minCol]
		// to [mapped2MaxRow, mapped2MaxCol]
		int mapped2minRow = (int) ((emageResolution.neLat - dataGrid.neLat) / emageResolution.latUnit);
		int mapped2minCol = (int) ((dataGrid.swLong - emageResolution.swLong) / emageResolution.latUnit);

		int mapped2MaxRow = (int) ((-dataGrid.swLat + emageResolution.neLat) / emageResolution.latUnit);
		int mapped2MaxCol = (int) ((dataGrid.neLong - emageResolution.swLong) / emageResolution.latUnit);

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
		int mappedFromMinRow = (int) ((dataGrid.neLat - emageResolution.neLat) / dataGrid.latUnit);
		int mappedFromMinCol = (int) ((emageResolution.swLong - dataGrid.swLong) / dataGrid.latUnit);

		int mappedFromMaxRow = (int) ((dataGrid.neLat - emageResolution.swLat) / dataGrid.latUnit);
		int mappedFromMaxCol = (int) ((emageResolution.neLong - dataGrid.swLong) / dataGrid.latUnit);

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
				if (rowScale > 1 || colScale > 1)// latScale=finalResolution.latUnit/initResolution.latUnit;
				// i.e. fine2Coarse... many initialArray values will map to 1
				{
					double[][] vals = new double[(int) Math.ceil(rowScale)][(int) Math
							.ceil(colScale)];
					for (int k = 0; k < rowScale; k++) {
						for (int l = 0; l < colScale; l++) {
							vals[k][l] = dataGrid.data[mappedFromMinRow
									+ (int) ((i - mapped2minRow) * rowScale)
									+ k][mappedFromMinCol
									+ (int) ((j - mapped2minCol) * colScale)
									+ l];
						}
					}

					emageGrid[i][j] = fine2Coarse(vals, SW);
				} else {
					if (rowScale < 1 || colScale < 1) // i.e. coarse2Fine...1
														// initialArray value
														// will map to many in
														// finalArray
					{
						int nRows2StretchTo = (int) (1 / rowScale);
						int nCols2StretchTo = (int) (1 / colScale);

						double pInCols = ((double) ((j - mapped2minCol) % nCols2StretchTo) / (double) nCols2StretchTo);
						double pInRows = (double) ((i - mapped2minRow) % nRows2StretchTo)
								/ (double) nRows2StretchTo;

						int initsRow2ReadFrom = (int) ((i - mapped2minRow) * rowScale);
						int initsCol2ReadFrom = (int) ((j - mapped2minCol) * colScale);

						double valCur = dataGrid.data[initsRow2ReadFrom][initsCol2ReadFrom];
						double valDiagonal = valCur;
						if (initsRow2ReadFrom + 1 < nRowsInit
								&& initsCol2ReadFrom + 1 < nColsInit)
							valDiagonal = dataGrid.data[initsRow2ReadFrom + 1][initsCol2ReadFrom + 1];

						if (SW.equals(SpatialWrapper.split_uniform)) {
							emageGrid[i][j] = coarse2Fine(valCur, valDiagonal,
									nRows2StretchTo, nCols2StretchTo, SW);
							log.info("+++++++++++++" + SW.name() + ": valCur"
									+ valCur + ", nRows2StretchTo:"
									+ nRows2StretchTo + ", nCols2StretchTo:"
									+ nCols2StretchTo);

						} else {
							emageGrid[i][j] = coarse2Fine(valCur, valDiagonal,
									pInRows, pInCols, SW);
						}
					} else { // no resolution change
						int initsRow2ReadFrom = (int) ((i - mapped2minRow) * rowScale);
						int initsCol2ReadFrom = (int) ((j - mapped2minCol) * colScale);

						if (initsCol2ReadFrom >= 0
								&& initsCol2ReadFrom < nColsInit
								&& initsRow2ReadFrom >= 0
								&& initsRow2ReadFrom < nRowsInit)
							emageGrid[i][j] = dataGrid.data[initsRow2ReadFrom][initsCol2ReadFrom];
						else
							log.info("No data found for " + i + " , " + j);
					}
				}
			}
		}
		CommonUtil.printAllCellGrid(emageGrid);
		log.info("===============================================");
		return emageGrid;

	}

	// public double[][] gridTransformation(){
	// //lets do the spatial mapping now
	// //the desired output image's template
	// int nRowsFinal = emageResolution.numOfRows;
	// int nColsFinal = emageResolution.numOfColumns;
	// int nRowsInit = dataGrid.length;
	// int nColsInit = dataGrid[0].length;
	//
	// emageGrid = dataGrid.clone();
	// // If scale > 1, fine2coarse
	// // If scale < 1, coarse2fine
	// double rowScale = nRowsInit / nRowsFinal;
	// double colScale = nColsInit / nColsFinal;
	//
	// // filling up the value
	// // lets assume that you cannot stretch on one dimension and shrink on
	// another
	// if(rowScale > 1 || colScale >1){ // fine2coarse: many initialArray values
	// will map to 1
	// for(int i = 0; i < nRowsFinal; i++){
	// for(int j = 0; j < nColsFinal; j++){
	//
	// }
	// }
	// } else if(rowScale < 1 || colScale <1){ // coarse2fine: 1 initialArray
	// value will map to many in finalArray
	// for(int i = 0; i < nRowsFinal; i++){
	// for(int j = 0; j < nColsFinal; j++){
	// int nRows2StretchTo = (int) (1/rowScale);
	// int nCols2StretchTo = (int) (1/colScale);
	//
	// double pInCols = ((double)((j)%nCols2StretchTo)/(double)nCols2StretchTo);
	// double pInRows = (double)((i)%nRows2StretchTo)/(double)nRows2StretchTo;
	//
	// int initsRow2ReadFrom = (int)((i)*rowScale);
	// int initsCol2ReadFrom = (int)((j)*colScale);
	//
	// double valCur = dataGrid[initsRow2ReadFrom][initsCol2ReadFrom];
	// double valDiagonal = valCur;
	// if (initsRow2ReadFrom + 1 < nRowsInit && initsCol2ReadFrom + 1
	// <nColsInit)
	// valDiagonal = dataGrid[initsRow2ReadFrom+1][initsCol2ReadFrom+1];
	//
	// if(SW.equals(SpatialWrapper.split_uniform))
	// emageGrid[i][j] = coarse2Fine ( valCur, valDiagonal, nRows2StretchTo,
	// nCols2StretchTo, SW );
	// else
	// emageGrid[i][j] = coarse2Fine ( valCur, valDiagonal, pInRows, pInCols, SW
	// );
	// }
	// }
	// } else { // no change
	// }
	// return emageGrid;
	// }
	//
	// private double nearestLoc(int i, int j){
	// Point inPoint =new Point(i,j);
	// double minDist=9999999;
	// for (int s=0; s< dataPoints.size(); s++)
	// {
	// if (inPoint.distance(dataPoints.get(s).point)<minDist)
	// minDist=inPoint.distance(dataPoints.get(s).point);
	// }
	// return minDist;
	// }
}