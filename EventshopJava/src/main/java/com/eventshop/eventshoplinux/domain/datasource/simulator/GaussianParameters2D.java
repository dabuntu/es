package com.eventshop.eventshoplinux.domain.datasource.simulator;

public class GaussianParameters2D extends DistParameters {
	double mu_x;
	double mu_y;
	double sigma_x;
	double sigma_y;

	double A;

	public GaussianParameters2D(double x, double y, double sigma_x,
			double sigma_y, double A) {
		this.mu_x = x;
		this.mu_y = y;
		this.sigma_x = sigma_x;
		this.sigma_y = sigma_y;
		this.A = A;
	}

	public GaussianParameters2D(GaussianParameters2D gParams) {
		mu_x = gParams.mu_x;
		mu_y = gParams.mu_y;
		sigma_x = gParams.sigma_x;
		sigma_y = gParams.sigma_y;
		A = gParams.A;
	}
}
