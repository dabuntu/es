package com.eventshop.eventshoplinux.domain.datasource.simulator;

public class GaussianGenerator2D extends DistributionGenerator {
	GaussianParameters2D gParams;

	public GaussianGenerator2D(GaussianParameters2D gParams) {
		this.gParams = gParams;
	}

	public double getValue(double x, double y) {
		double value = gParams.A
				* Math.pow(Math.E, (-0.5 * (Math.pow((x - gParams.mu_x)
						/ gParams.sigma_x, 2) + Math.pow((y - gParams.mu_y)
						/ gParams.sigma_y, 2))));

		return value;
	}
}
