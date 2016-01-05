/*
 * filter.cc
 *
 *  Created on: Jul 8, 2011
 *      Author: singhv
 */

#include "op.h"

#include <stdio.h>

using namespace std;
using namespace cv;


bool Filter::check_time(const Emage& emage)
{
	int64 time = emage.getEndTime();

	// time range [tm_min, tm_max)
	cout << "check_time: emage end time: " << cur_emage.getEndTime() << endl;
	//cout << "check_time: fcond tm_min: " << fcond.get_tm_min() << endl;
	//cout << "check_time: fcond tm_max: " << fcond.get_tm_max() << endl;

	if(time >= fcond.get_tm_min() && time < fcond.get_tm_max())
		return true;
	else
		return false;
}


bool Filter::select_location(Emage& emage)
{
	Mat in = emage.getArray().clone();
	Mat out = emage.getArray();
	out = Mat::zeros(in.rows, in.cols, CV_64F);
	cout << "rows" << in.rows << ", cols: " << in.cols << endl;
	add(in, Scalar(0), out, fcond.get_mask());
	return true;
}


bool Filter::select_value(Emage& emage)
{
	// If the min value and max value are different
	double min = fcond.get_val_min();
	double max = fcond.get_val_max();

	// If min > max, condition is ill formatted
	if(min > max) return false;
	// If min < max, check if cell values fall into the range
	else if(min < max)
	{
		Mat in = emage.getArray().clone();
		Mat out = emage.getArray();
		out = Mat::zeros(in.rows, in.cols, CV_64F);

		// Compute the mask
		Mat mask = Mat::zeros(in.rows, in.cols, CV_8U);
		inRange(in, Scalar(min), Scalar(max), mask);
		// Compute the result
		add(in, Scalar(0), out, mask);
		return true;
	}
	// otherwise, check the value together with the operators
	else
	{
		Mat m = emage.getArray();
		int rows = emage.getNumRows();
		int cols = emage.getNumCols();

		if(fcond.get_op() == LT)
		{
			for(int i = 0; i < rows; ++i)
				for(int j = 0; j < cols; ++j)
				{
					if(m.at<double>(i, j) >= min)
						m.at<double>(i, j) = 0;
				}
			return true;
		}
		else if(fcond.get_op() == LE)
		{
			for(int i = 0; i < rows; ++i)
				for(int j = 0; j < cols; ++j)
				{
					if(m.at<double>(i, j) > min)
						m.at<double>(i, j) = 0;
				}
			return true;
		}
		else if(fcond.get_op() == EQ)
		{
			for(int i = 0; i < rows; ++i)
				for(int j = 0; j < cols; ++j)
				{
					if(m.at<double>(i, j) != min)
						m.at<double>(i, j) = 0;
				}
			return true;
		}
		else if(fcond.get_op() == GT)
		{
			for(int i = 0; i < rows; ++i)
				for(int j = 0; j < cols; ++j)
				{
					if(m.at<double>(i, j) <= min)
						m.at<double>(i, j) = 0;
				}
			return true;
		}
		else if(fcond.get_op() == GE)
		{
			for(int i = 0; i < rows; ++i)
				for(int j = 0; j < cols; ++j)
				{
					if(m.at<double>(i, j) < min)
						m.at<double>(i, j) = 0;
				}
			return true;
		}
	}
	return false;
}


bool Filter::has_next()
{
	if(!eit.has_next()) return false;

	cur_emage = eit.next();

	cout << "time: " << cur_emage.getEndTime() << endl;

//	if(!check_time(cur_emage)) return false;
//	if(!select_location(cur_emage)) return false;
//	if(!select_value(cur_emage)) return false;

	return true;
}


Emage Filter::next()
{
	normalizeEmage(cur_emage);
	return cur_emage;
}

void Filter::normalizeEmage(Emage& e)
{
	if (fcond.get_norm_mode())
	{
		Mat inMat = e.getArray();
		Mat outMat = e.getArray();

		double arrayMax = -999999;
		double arrayMin = 999999;

		for (int i=0; i<e.getNumRows(); i++)
		{
			for (int j=0; j<e.getNumCols(); j++)
			{
				if (inMat.at<double>(i, j) > arrayMax) arrayMax = inMat.at<double>(i, j);
				if (inMat.at<double>(i, j) < arrayMin) arrayMin = inMat.at<double>(i, j);
			}
		}

		if (arrayMax>0)
		{
			for (int i=0; i<e.getNumRows(); i++)
			{
				for (int j=0; j<e.getNumCols(); j++)
				{
					outMat.at<double>(i, j)=fcond.get_norm_min()+((inMat.at<double>(i, j)-arrayMin)/(arrayMax-arrayMin))*(fcond.get_norm_max()-fcond.get_norm_min());
				}
			}
		}
		e.setArray(outMat);
	}
}


Filter::~Filter()
{
}


//int main()
//{
//	EmageIngestor in1("C:\\Project\\emage1");
//
//	int rows = 10;
//	int cols = 10;
//
////	Mat mask(rows, cols, CV_8U);
////	for(int i = 0; i < rows; ++i)
////	{
////		for(int j = 0; j < cols; ++j)
////		{
////			if(j <= i)
////				mask.at<unsigned char>(i, j) = 1;
////			else
////				mask.at<unsigned char>(i, j) = 0;
////			printf("%x ", mask.at<unsigned char>(i, j));
////		}
////		cout << endl;
////	}
////
////	FilterCondition fcond(mask, LT, 0, 100, 1310527370000, 1310529000000);
//	FilterCondition fcond(rows, cols);
//	Filter filter(in1, fcond);
//
//	int count = 0;
//	while(count < 1000)
//	{
//		++count;
//		cout << "count: " << count << endl;
//		while(filter.has_next())
//		{
//			Emage e = filter.next();
//
//			cout << "time: " << e.getEndTime() << endl;
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
