/*
 * temporal_analysis.h
 *
 *  Created on: Jul 15, 2011
 *      Author: gaom
 */

#ifndef TEMPORAL_ANALYSIS_H_
#define TEMPORAL_ANALYSIS_H_

#include "op.h"
#include "spatial_analysis.h"

enum TemporalPatternOp{tpo_INPUT, tpo_LINEAR, tpo_EXPONENTIAL, tpo_PERIODIC};


class temporalPatternTemplate
{
protected:
	int64 timeBetweenFrames;
	int64 timeWindow;

public:
	temporalPatternTemplate(int64 vartimeBetweenFrames, int64 vartimeWindow ):
		timeBetweenFrames( vartimeBetweenFrames), timeWindow(vartimeWindow) {};

	virtual void getTemplate(cv::Mat& tempPatternVals ) = 0;
    int64 getTimeBetweenFrames() const;
    int64 getTimeWindow() const;
    void setTimeBetweenFrames(int64  timeBetweenFrames);
    void setTimeWindow(int64  timeWindow);
};


class InputTemporalPatternTemplate: public temporalPatternTemplate
{
	string loadPath;
public:
	InputTemporalPatternTemplate(const string& filePath)
	: temporalPatternTemplate(10*1000, 30*1000),
	  loadPath(filePath)
	{}
	virtual void getTemplate(cv::Mat& tempPatternVals );
};


class LinearTemporalPatternTemplate: public temporalPatternTemplate
{
	double slope;
	double y_intercept;
public:
	LinearTemporalPatternTemplate(double varslope, double vary_intercept, int64 vartimeBetweenFrames, int64 vartimeWindow)
	: temporalPatternTemplate(vartimeBetweenFrames, vartimeWindow),
	  slope(varslope), y_intercept(vary_intercept)
	{}
	virtual void getTemplate(cv::Mat& tempPatternVals );
};


class ExponentialTemporalPatternTemplate: public temporalPatternTemplate
{
	double base;
	double scale;

public:
	ExponentialTemporalPatternTemplate(double varbase, double varscale, int64 vartimeBetweenFrames, int64 vartimeWindow)
	: temporalPatternTemplate(vartimeBetweenFrames, vartimeWindow),
	  base(varbase), scale(varscale)
	{}
	virtual void getTemplate(cv::Mat& tempPatternVals );

};


class PeriodicTemporalPatternTemplate: public temporalPatternTemplate
{
	double frequency;
	double amplitude;
	double phaseDelay;

public:
	PeriodicTemporalPatternTemplate(double varfrequency, double varamplitude, double varphaseDelay,  int64 vartimeBetweenFrames, int64 vartimeWindow )
	: temporalPatternTemplate(vartimeBetweenFrames, vartimeWindow),
	  frequency(varfrequency),
	  amplitude(varamplitude),
	  phaseDelay(varphaseDelay)
	{}
	virtual void getTemplate(cv::Mat& tempPatternVals );

};


class TemporalChar : public ProcEmageIterator
{
	ProcEmageIterator& eit;
	TemporalCharOp temporal_operation;
	int64 overall_window;

	bool is_last_available;
	Emage cur_emage;
	vector<Emage> buffer;

	void manage_buffer(const Emage& emage);
	double displacement(Emage& first_emage, Emage& last_emage);
	double velocity(Emage& first_emage, Emage& last_emage);
	void getEpicenter(const cv::Mat& in, cv::Point_<double>& point);

public:
	TemporalChar(ProcEmageIterator& it, TemporalCharOp op, int64 timeWindowToConsider)
	: ProcEmageIterator(),
	  eit(it), temporal_operation(op),
	  overall_window(timeWindowToConsider),
	  is_last_available(false)
	{}

	virtual bool has_next();
	virtual Emage next();
	virtual ~TemporalChar();
};


class TemporalPatternMatching: public ProcEmageIterator
{
	ProcEmageIterator& eit;
	TemporalPatternOp temporal_operation;
	bool normAmplitude;
	bool normDuration;
	int64 overall_window;
	temporalPatternTemplate *temporal_pattern;
	string loadPath;

	vector<Emage> buffer;
	cv::Mat pattern;

	void manageBuffer();

public:
	TemporalPatternMatching(ProcEmageIterator& it, TemporalPatternOp tpop, bool multi_amp, bool multi_dur,
			int64 window, temporalPatternTemplate *tpt)
	: ProcEmageIterator(), eit(it),
	  temporal_operation(tpop),
	  normAmplitude(multi_amp),
	  normDuration(multi_dur),
	  overall_window(window),
	  temporal_pattern(tpt)
	{
		temporal_pattern->getTemplate(pattern);
	}

	virtual bool has_next();
	virtual Emage next();
	virtual ~TemporalPatternMatching();
};

#endif /* TEMPORAL_ANALYSIS_H_ */
