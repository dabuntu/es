/*
 * temporalchar.cc
 *
 *  Created on: Jul 8, 2011
 *      Author: singhv
 */

#include "temporal_analysis.h"

using namespace std;
using namespace cv;


bool TemporalPatternMatching::has_next()
{
	if(!eit.has_next()) return false;
	return true;
}


void TemporalPatternMatching::manageBuffer()
{
	Emage emage = eit.next();

//	cout << "emage end time " << emage.getEndTime() << endl;

	buffer.push_back(emage);
//	cout << "buffer 0 end time " << buffer[0].getEndTime() << endl;
	while(emage.getEndTime() - buffer[0].getEndTime() >= overall_window)
	{
		buffer.erase(buffer.begin());
	}
//	cout<< "Buf size is :" << buffer.size() <<endl;
}


Emage TemporalPatternMatching::next()
{
	manageBuffer();
	int size = buffer.size();

	Mat out = Mat::zeros(1, 1, CV_64F);
	Emage out_emage(buffer[size-1], out);

	if(size < 2 || size < (temporal_pattern->temporalPatternTemplate::getTimeWindow() / temporal_pattern->temporalPatternTemplate::getTimeBetweenFrames()))
	{
		cerr << "The number of emage is too small to matching pattern." << endl;
		out_emage.getArray().at<double>(0, 0) = 0;
		out_emage.setSwLat(-999);
		out_emage.setSwLong(-999);
		return out_emage;
	}

	// Getting the input values
	if (buffer[0].getArray().cols > 1 || buffer[0].getArray().rows > 1 )
	{
		cerr << "ERROR:The incoming data for temporal pattern matching contains more than 1 pixel. Do spatial/temporal characterization first! "<< endl ;
		cerr << "Continuing with just the first pixel for now." << endl;
	}

	// Forming the 1D image
	Mat in = Mat::zeros(1, size, CV_32F);
	for (int i=0; i < size; ++i)
	{
		in.at<float>(0, i) = (float)buffer[i].getArray().at<double>(0,0);
	}

	if (normDuration || temporal_operation == tpo_PERIODIC )
	{
		// so we will resize the kernel to match up with the input image and do ONLY one comparison
		Mat pattern_copy = pattern.clone();
		pattern = Mat::zeros(1, size, CV_32F);
		float match = (float)in.cols / (float)pattern_copy.cols;
		for (int i=0; i < in.cols; i++)
		{
			pattern.at<float>(0,i) = pattern_copy.at<float>(0, floor((float)i / match));
		}
	}

	double sizeRatio = (in.rows * in.cols)/ ((pattern.rows * pattern.cols));
	if (sizeRatio < 1)
	{
		cerr << "ERROR: input duration is too small for pattern matching" << endl;
	}

	if (normAmplitude)
	{
		in = in / sum(abs(in))[0];
		pattern = pattern /(sum(abs(pattern))[0] * sizeRatio);
	}

	Mat matching_result;
	matchTemplate(in, pattern, matching_result, CV_TM_SQDIFF_NORMED);

	double min_val;
	double max_val;
	Point min_loc;
	Point max_loc;

	minMaxLoc(matching_result, &min_val, &max_val, &min_loc, &max_loc);
	out.at<double>(0, 0) = 1 - (min_val);
	out_emage.setSwLat(-999);
	out_emage.setSwLong(-999);
	out_emage.setArray(out);
	return out_emage;
}


TemporalPatternMatching::~TemporalPatternMatching()
{
}


//int main()
//{
//	EmageIngestor in("C:\\Project\\emage1");
//	SpatialChar spchar(in, SPTLSUM);
//	LinearTemporalPatternTemplate* ltptemplate = new LinearTemporalPatternTemplate(1, 1, 10000, 3*10000);
//	TemporalPatternMatching matching(spchar, tpo_LINEAR, false, true, 3*10000, ltptemplate);
//
//	int count = 0;
//	while(true)
//	{
//		while(matching.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//
//			Emage e = matching.next();
//			int32 rows = e.getNumRows();
//			int32 cols = e.getNumCols();
//			for(int32 i = 0; i < rows; ++i)
//			{
//				for(int32 j = 0; j < cols; ++j)
//					cout << e.getArray().at<double>(i, j) << " ";
//				cout << endl;
//			}
//		}
//		Sleep(100);
//	}
//	return 0;
//}
