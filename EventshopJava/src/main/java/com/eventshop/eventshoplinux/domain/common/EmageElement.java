package com.eventshop.eventshoplinux.domain.common;

import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class EmageElement {

	public String theme;
	public long startTime, endTime;
	public String startTimeStr, endTimeStr;
	public double min, max, latUnit, longUnit, swLat, swLong, neLat, neLong;
	public int row, col;
	public double[] image;
	public double value;
	public String mapEnabled;
	public String[] colors;

	public String getTheme() {
		return this.theme;
	}

	public void setTheme(String th) {
		this.theme = th;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(long st) {
		this.startTime = st;
	}

	public long getEndTime() {
		return this.endTime;
	}

	public void setEndTime(long et) {
		this.endTime = et;
	}

	public void reduceSize(int level) {
		System.out.println("Inside emageelement.reducesize");
		double powLevel = Math.pow(2.0, level);
		double oriCol = this.col;
		this.row = (int) Math.ceil(this.row / powLevel);
		this.col = (int) Math.ceil(this.col / powLevel);
		this.latUnit = this.latUnit * powLevel;
		this.longUnit = this.longUnit * powLevel;
		double[] temp = new double[this.row * this.col];
		int count = 0;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				temp[count] = this.image[(int) ((powLevel * i * oriCol) + (powLevel * j))];
//				System.out.println(count + ", "
//						+ ((powLevel * i * oriCol) + (powLevel * j)));
				count++;
			}
		}
		this.image = temp;
	}

	// public double[] getCaliBoundary(){
	// System.out.println("Inside emageelement.getCaliBoundary");
	// String fileName = "/opt/CA_index_grid.file";
	// BufferedReader reader = null;
	// double[] caImage = null;
	// StringBuilder sb = new StringBuilder();
	// try {
	// reader = new BufferedReader(new FileReader(fileName));
	// if(reader != null){
	// String dataRow = reader.readLine();
	// while(dataRow != null){
	// //String[] coord = dataRow.split(",");
	// //caIndex = new int[coord.length];
	// //for(int i = 0; i < coord.length; i++){
	// // caIndex[i] = Integer.parseInt(coord[i]);
	// //}
	// sb.append(dataRow);
	// dataRow = reader.readLine();
	// }
	// }
	// String[] value = sb.toString().split(", ");
	// caImage = new double[value.length];
	// for(int i = 0; i < value.length; i++){
	// caImage[i] = Double.parseDouble(value[i]);
	// }
	//
	// } catch (FileNotFoundException e) {
	// System.out.println(e.getMessage());
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// reader.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// System.out.println("caImage is " + caImage.toString());
	// return caImage;
	// }

	public void selectState(String state) {
		if (state.equalsIgnoreCase("ca")) {
			// double[] caImage = this.getCaliBoundary();
			double[] caImage = { 0, 9, 8 };
			if (caImage != null) {
				System.out.println("image length: " + image.length + ", " + row
						+ ", " + col);

				for (int i = 0; i < image.length; i++) {
					if (caImage[i] == 0.0)
						image[i] = Double.NaN;
				}
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < image.length; i++) {
					sb.append(image[i] + ",");
				}
				CommonUtil.writeToFile("/home/eventshop/eventshopdata/CA_" + theme + "_nan.file",
						sb.toString(), false);

			}

			Double llLat = 32.0, llLon = -125.0, urLat = 43.0, urLon = -112.0;
			Point llPoint = latLong2Pixel(llLat, llLon);
			Point urPoint = latLong2Pixel(urLat, urLon);
			System.out.println("llPoint, urPoint: " + llPoint.x + ", "
					+ llPoint.y + ", " + urPoint.x + ", " + urPoint.y);
			int newRow = llPoint.x - urPoint.x;
			int newCol = urPoint.y - llPoint.y;
			double newMin = max;
			double newMax = min;
			double[] selectedImage = new double[newRow * newCol];
			int count = 0;

			for (int i = 0; i < this.image.length; i++) {

				int imageX = i / this.col;
				int imageY = i % this.col;
				if (imageX > urPoint.x && imageX <= llPoint.x
						&& imageY > llPoint.y && imageY <= urPoint.y) {
					selectedImage[count] = image[i];
					// System.out.println( "i,imageX,Y: " + i + ", " + imageX +
					// ", " + imageY + ", ["+count+"] value" + image[i]);
					count++;
					if (image[i] > newMax)
						newMax = image[i];
					if (image[i] < newMin)
						newMin = image[i];
				}
			}

			this.row = newRow;
			this.col = newCol;
			this.min = newMin;
			this.max = newMax;
			this.image = selectedImage;
			this.swLat = llLat;
			this.swLong = llLon;
			this.neLat = urLat;
			this.neLong = urLon;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < image.length; i++) {
				sb.append(image[i] + ",");
			}
			CommonUtil.writeToFile("/home/eventshop/eventshopdata/CA_" + theme + ".file", sb.toString(),
					false);

			// get ca post code
			String fileName = "/home/eventshop/eventshopdata/CA_postcode.file";
			BufferedReader reader = null;
			StringBuilder str = new StringBuilder();
			try {
				reader = new BufferedReader(new FileReader(fileName));
				if (reader != null) {
					String dataRow = reader.readLine();
					while (dataRow != null) {
						String[] coord = dataRow.split(",");
						Point p = this.latLong2Pixel(
								Double.parseDouble(coord[0]),
								Double.parseDouble(coord[1]));
						int index = p.x * row + p.y;
						str.append(this.image[index] + ",");
						dataRow = reader.readLine(); // read next line;
						System.out.println("xyindex: " + p.x + "," + p.y + ","
								+ index + "," + image[index]);
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			CommonUtil.writeToFile("/home/eventshop/eventshopdata/CA_postcode_" + theme + ".file",
					str.toString(), false);

		}
	}

	public Point latLong2Pixel(Double latV, Double longV) {
		int nRows = this.row;

		MathContext context = new MathContext(5);
		int x = (nRows - 1)
				- (int) ((BigDecimal.valueOf(latV)).subtract(
						BigDecimal.valueOf(this.swLat), context).divide(
						BigDecimal.valueOf(this.latUnit), context)
						.doubleValue());
		int y = (int) ((BigDecimal.valueOf(longV)).subtract(
				BigDecimal.valueOf(this.swLong), context).divide(
				BigDecimal.valueOf(this.longUnit), context).doubleValue());

		return new Point(x, y);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("theme: " + theme);
		sb.append("\nrow: " + row);
		sb.append("\ncol: " + col);
		sb.append("\nlatUnit: " + latUnit);
		sb.append("\nlongUnit: " + longUnit);
		sb.append("\ncolors: " + Arrays.toString(colors));
		sb.append("\nimage: " + Arrays.toString(image));
		return sb.toString();
	}
}
