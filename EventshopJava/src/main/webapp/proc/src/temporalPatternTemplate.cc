/*
 * temporalPatternTemplate.cc
 *
 *  Created on: Jul 15, 2011
 *      Author: singhv
 */

#include "temporal_analysis.h"
#include <vector>

using namespace std;
using namespace cv;


inline int64 temporalPatternTemplate::getTimeBetweenFrames() const
{
	return timeBetweenFrames;
}


inline int64 temporalPatternTemplate::getTimeWindow() const
{
	return timeWindow;
}


inline void temporalPatternTemplate::setTimeBetweenFrames(int64  timeBetweenFrames)
{
	this->timeBetweenFrames = timeBetweenFrames;
}


inline void temporalPatternTemplate::setTimeWindow(int64  timeWindow)
{
	this->timeWindow = timeWindow;
}


void LinearTemporalPatternTemplate::getTemplate(cv::Mat& tempPatternVals)
{
	int numFrames=temporalPatternTemplate::getTimeWindow()/temporalPatternTemplate::getTimeBetweenFrames();
	tempPatternVals = Mat::zeros(1, numFrames, CV_32F);

	for (int i=0; i<numFrames; ++i)
	{
		tempPatternVals.at<float>(0,i)=i*slope+y_intercept;
	}
}


void ExponentialTemporalPatternTemplate::getTemplate(cv::Mat& tempPatternVals)
{
	int numFrames=temporalPatternTemplate::getTimeWindow()/temporalPatternTemplate::getTimeBetweenFrames();
	tempPatternVals = Mat::zeros(1, numFrames, CV_32F);

	for (int i=0; i<numFrames; ++i)
	{
		tempPatternVals.at<float>(0,i) = scale* pow(base, i);
	}
}


void PeriodicTemporalPatternTemplate::getTemplate(cv::Mat& tempPatternVals)
{
	int numSamplesReqd = 4*(1/frequency);
	tempPatternVals = Mat::zeros(1, numSamplesReqd, CV_32F);

	for (int i=0; i < numSamplesReqd; ++i)
	{
		tempPatternVals.at<float>(0,i) = amplitude * (sin ((i%4-phaseDelay)*3.14/2));
	}
}


void InputTemporalPatternTemplate::getTemplate(cv::Mat& tempPatternVals)
{
	ifstream patternFile(loadPath.c_str());

	char ch;
	int count = 0;
	int samplingRate = 0;
	patternFile >> count >> ch;
	patternFile >> samplingRate;

	timeBetweenFrames = samplingRate * 1000;
	timeWindow = count * samplingRate * 1000;

	tempPatternVals.create(1, count, CV_32F);
	for(int i = 0; i < count; ++i)
	{
		float value = 0;
		patternFile >> value >> ch;
		tempPatternVals.at<float>(0, i) = value;
	}
	patternFile.close();
}
