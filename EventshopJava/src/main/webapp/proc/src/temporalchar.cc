/*
 * temporalchar.cc
 *
 *  Created on: Jul 8, 2011
 *      Author: singhv
 */

#include "temporal_analysis.h"

#include <vector>
#include <cmath>

using namespace std;
using namespace cv;


bool TemporalChar::has_next()
{
	// If no new emage is available
//	if(!eit.has_next())
//	{
//		// check if the last one is available
//		if(is_last_available) return true;
//		else return false;
//	}
//	is_last_available = false;
//	return true;
	if(!eit.has_next()) return false;
	return true;
}


void TemporalChar::manage_buffer(const Emage& emage)
{
	// Remove the last element when necessary
	if(buffer.size() > 1)
	{
		while(emage.getEndTime() - buffer[0].getEndTime() >= overall_window)
			buffer.erase(buffer.begin());
	}
	// Add the new element
	buffer.push_back(emage);
}


Emage TemporalChar::next()
{
	// If only the last is available
//	if(is_last_available) return cur_emage;

	// Get the new emage
	Emage emage = eit.next();
	manage_buffer(emage);

	int size = buffer.size();
	cout<< "Buffer size is :" << size <<endl;

	// Do the computation
	Mat out = Mat::zeros(1, 1, CV_64F);
	if(temporal_operation == DISPLACEMENT)
	{
		if(size > 1)
			out.at<double>(0, 0) = displacement(buffer[0], buffer[size-1]);
		else
			out.at<double>(0, 0) = 0;
	}
	else if(temporal_operation == VELOCITY)
	{
		if(size > 1)
			out.at<double>(0, 0) = velocity(buffer[0], buffer[size-1]);
		else
			out.at<double>(0, 0) = 0;
	}
	else if(temporal_operation == ACCELERATION)
	{
		if(size < 3) out.at<double>(0, 0) = 0;
		else
		{
			double velocity_begin = velocity(buffer[0], buffer[1]);
			double velocity_end = velocity(buffer[size-2], buffer[size-1]);
			out.at<double>(0, 0)=(velocity_begin - velocity_end)
					/ (buffer[size-1].getEndTime() - buffer[0].getEndTime());
		}
	}
	else if(temporal_operation == GROWTHRATE)
	{
		if(size < 2) out.at<double>(0, 0) = 0;
		else
		{
			double first_sum = sum(buffer[0].getArray())[0];
			double last_sum = sum(buffer[size-1].getArray())[0];
			out.at<double>(0, 0) = (last_sum - first_sum) / (buffer[size-1].getEndTime() - buffer[0].getEndTime());
		}
	}

	Emage out_emage(buffer[size-1], out);
	out_emage.setNumRows(1);
	out_emage.setNumCols(1);
	out_emage.setSwLat(-999);
	out_emage.setSwLong(-999);
	out_emage.setArray(out);

	// Save the cur_emage for furture query when next is not available
	cur_emage = out_emage;
	cur_emage.copyArray(out);
	is_last_available = true;
	return out_emage;
}


void TemporalChar::getEpicenter(const Mat& in, Point_<double>& point)
{
	double sum_val_w = 0;
	double sum_val_h = 0;
	double sum_val = 0;

	for(int h = 0; h < in.rows; h++)
	{
		for(int w = 0; w < in.cols; w++)
		{
			sum_val += in.at<double>(h,w);
			sum_val_w += in.at<double>(h,w) * w;
			sum_val_h += in.at<double>(h,w) * h;
		}
	}
	double lat_long_val_w = (sum_val_w / sum_val);
	double lat_long_val_h = (sum_val_h / sum_val);
	point.y = lat_long_val_h;
	point.x = lat_long_val_w;
}


double TemporalChar::displacement(Emage& first_emage, Emage& last_emage)
{
	Point_<double> first, last;
	first.x = first_emage.getSwLong();
	first.y = first_emage.getSwLat();
	last.x = last_emage.getSwLong();
	last.y = last_emage.getSwLat();

	if(first_emage.getNumRows() > 1 || first_emage.getNumCols() > 1)
		getEpicenter(first_emage.getArray(), first);
	if(last_emage.getNumRows() > 1 || last_emage.getNumCols() > 1)
		getEpicenter(last_emage.getArray(), last);
	return sqrt(pow((first.y - last.y), 2) + pow((first.x - last.x), 2));
}


double TemporalChar::velocity(Emage& first_emage, Emage& last_emage)
{
	double disp = displacement(first_emage, last_emage);
	return disp / (last_emage.getEndTime() - first_emage.getEndTime()) * 1000;
}


TemporalChar::~TemporalChar()
{
}


//int main()
//{
//	EmageIngestor in("C:\\Project\\emage1");
//	TemporalChar tpchar(in, DISPLACEMENT, 1000*30);
//
//	int count = 0;
//	while(true)
//	{
//		while(tpchar.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//
//			Emage e = tpchar.next();
//			cout << e.getSwLat()<< " " << e.getSwLong()<< endl;
//
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
