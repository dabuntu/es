/*
 * spatial_analysis.h
 *
 *  Created on: Jul 15, 2011
 *      Author: gaom
 */

#ifndef SPATIAL_ANALYSIS_H_
#define SPATIAL_ANALYSIS_H_

#include "op.h"

#include <cmath>



class SpatialChar : public ProcEmageIterator
{
	ProcEmageIterator& eit;
	SpatialCharOp scop;

public:
	SpatialChar(ProcEmageIterator &it, SpatialCharOp op)
	: ProcEmageIterator(),
	  eit(it), scop(op)
	{}

	virtual bool has_next();
	virtual Emage  next();
	virtual ~SpatialChar();
};


class SpatialPattern
{
protected:
	int32 num_rows;
	int32 num_cols;

public:
	SpatialPattern(int32 rows, int32 cols)
	: num_rows(rows), num_cols(cols)
	{}
	virtual cv::Mat createPattern() = 0;
};


class GaussianPattern: public SpatialPattern
{
	float mu_x;
	float mu_y;
	float sigma_x;
	float sigma_y;
	float amplitude;

public:
	GaussianPattern(int32 rows, int32 cols) : SpatialPattern(rows, cols)
	{
		mu_x = mu_y = 0;
		sigma_x = sigma_y = 1;
		amplitude = 1;
	}

	GaussianPattern(int32 rows, int32 cols, float x, float y, float sx, float sy, float amp)
	: SpatialPattern(rows, cols), mu_x(x), mu_y(y), sigma_x(sx), sigma_y(sy), amplitude(amp)
	{}

	virtual cv::Mat createPattern()
	{
		cv::Mat pattern(num_rows, num_cols, CV_32F);
		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
				pattern.at<float>(i, j) = amplitude * exp( -0.5 * (pow((i - mu_x)/sigma_x ,2)
																 +  pow((j - mu_y)/sigma_y, 2)));
		return pattern;
	}

	void setCenterX(const float x)     { this->mu_x = x; }
	void setCenterY(const float y)     { this->mu_y = y; }
	void setVarianceX(const float x)   { this->sigma_x = x; }
	void setVarianceY(const float y)   { this->sigma_y = y; }
	void resize(const float amp) { this->amplitude = amp; }
};


class LinearPattern : public SpatialPattern
{
	float start_x;
	float start_y;
	float start_value;
	float direction_gradient;	// the slope along which values vary
	float value_gradient;		// how fast is the value changing with respect to the distance

public:
	LinearPattern(int32 rows, int32 cols) : SpatialPattern(rows, cols)
	{
		start_x = start_y = start_value = 0;
		direction_gradient = value_gradient = 1;
	}

	LinearPattern(int32 rows, int32 cols, float x, float y, float value, float dg, float vg)
	: SpatialPattern(rows, cols), start_x(x), start_y(y), start_value(value),
	  direction_gradient(dg), value_gradient(vg)
	{}

	virtual cv::Mat createPattern()
	{
		cv::Mat pattern(num_rows, num_cols, CV_32F);

		float arc_direction = atan(direction_gradient);
		float arc_point = 0;

		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
			{
				float inter_y = i - start_y;
				float inter_x = j - start_x;
				if(inter_x != 0)
				{
					arc_point = arc(inter_y / inter_x);
				}
				else
				{
					if(inter_y == 0) arc_point = arc_direction + 0.5*pi;
					else arc_point = 0.5*pi;
				}

				float radius = pow((pow(inter_y, 2) + pow(inter_x, 2)), (float)0.5);
				float distance = radius * cos(arc_point - arc_direction);
				pattern.at<float>(i, j) = distance * value_gradient + start_value;
			}
		return pattern;
	}

	void setStartX(const float x)              { start_x = x; }
	void setStartY(const float y)              { start_y = y; }
	void setStartValue(const float val)        { start_value = val; }
	void setDirectionGradient(const float dg)  { direction_gradient = dg; }
	void setValueGradient(const float vg)      { value_gradient = vg; }
};


class SpatialPatternMatching : public ProcEmageIterator
{
	ProcEmageIterator& eit;
	SpatialPatternOp spatial_operation;
	bool normSize;
	bool normAmplitude;

	string loadPath;
	SpatialPattern* spatial_pattern;

	cv::Mat pattern;

	void preparePattern();

public:
	SpatialPatternMatching(ProcEmageIterator& it, SpatialPatternOp op, bool size, bool amplitude, SpatialPattern * const pat)
	: ProcEmageIterator(), eit(it), spatial_pattern(pat)
	{
		// For generated patterns
		spatial_operation = op;
		normSize = size;
		normAmplitude = amplitude;

		// create pattern
		preparePattern();
	}

	SpatialPatternMatching(ProcEmageIterator& it, SpatialPatternOp op, bool size, bool amplitude, const string& path)
	: ProcEmageIterator(), eit(it), loadPath(path)
	{
		// For loaded pattern
		spatial_operation = op;
		normSize = size;
		normAmplitude = amplitude;

		// create pattern
		preparePattern();
	}

	virtual bool has_next();
	virtual Emage next();
	virtual ~SpatialPatternMatching();
};

#endif /* SPATIAL_ANALYSIS_H_ */
