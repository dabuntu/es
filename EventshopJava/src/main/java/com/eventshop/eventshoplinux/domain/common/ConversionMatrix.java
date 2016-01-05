package com.eventshop.eventshoplinux.domain.common;

public class ConversionMatrix {

	int row;
	int column;

	double matrix[][];

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public int getRow() {
		row = matrix.length;
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		column = matrix[0].length;
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

}
