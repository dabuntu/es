package com.eventshop.eventshoplinux.domain.datasource.simulator;

public class UniformGenerator {
	double A;

	public UniformGenerator(double A) {
		this.A = A;
	}

	public double getValue(double x, double y) {
		return A;
	}
}
