/*
 * grouping.cc
 *
 *  Created on: Jul 8, 2011
 *      Author: singhv
 */

#include "spatial_analysis.h"
#include "highgui.h"

using namespace std;
using namespace cv;


void SpatialPatternMatching::preparePattern()
{
	// Load or create pattern matrix
	if (spatial_operation == Input)
	{
		pattern = imread(loadPath.c_str(), 0);
		pattern.convertTo(pattern, CV_8U, 1);
		pattern.convertTo(pattern, CV_32F, 1);
	}
	else if(spatial_operation == Gaussian || spatial_operation == Linear2D)
	{
		pattern = spatial_pattern->createPattern();
	}
}


bool SpatialPatternMatching::has_next()
{
	if (!eit.has_next()) return false;
	return true;
}


Emage SpatialPatternMatching::next()
{
	Emage emage = eit.next();
	Mat in = emage.getArray().clone();
	in.convertTo(in, CV_32F, 1);

	if (normSize)
	{
		// We will resize the kernel to match up with the input image and do ONLY one comparison
		Mat pattern_copy = pattern.clone();
		resize(pattern_copy, pattern, Size(in.cols,in.rows), 0, 0, INTER_LINEAR);
	}

	double sizeRatio = (in.rows * in.cols) / ((pattern.rows * pattern.cols));
	if (sizeRatio < 1)
	{
		cerr << "ERROR: input image is too small for pattern matching" << endl;
	}
	if (normAmplitude)
	{
		// do not care about the difference in amplitude.
		// hence we will normalize so that the sum is 1 for the inputEmage
		in = in / sum(in)[0];
		// and we will normalize so that the sum is 1/size ratio to the emage
		pattern = pattern / (sum(pattern)[0] * sizeRatio);
	}

	Mat matching_result;

	matchTemplate(in, pattern, matching_result, CV_TM_SQDIFF_NORMED);
	double min_val, max_val;
	Point minLoc, maxLoc;
	minMaxLoc(matching_result, &min_val, &max_val, &minLoc, &maxLoc);

	Mat out = Mat::zeros(1, 1, CV_64F);
	out.at<double>(0, 0) = 1 - (min_val);

	Emage output_emage(emage, out);
	output_emage.setNumRows(1);
	output_emage.setNumCols(1);
	//double sw_lat = emage.getSwLat();
	double sw_long = emage.getSwLong();
	double ne_lat = emage.getNeLat();
	double lat_unit = emage.getLatUnit();
	double long_unit = emage.getLongUnit();
	output_emage.setSwLat(ne_lat - lat_unit*(minLoc.y+pattern.rows/2+1));
	output_emage.setSwLong(sw_long + long_unit*(minLoc.x+pattern.cols/2));
	output_emage.setArray(out);
	return output_emage;
}


SpatialPatternMatching::~SpatialPatternMatching()
{
}



//int main()
//{
//	EmageIngestor in("C:\\Project\\emage1");
//	GaussianPattern pattern(5, 5, 2.0, 2.0, 3.0, 3.0, 240.0);
////	LinearPattern pattern(10, 10);
//	Mat m = pattern.createPattern();
//	for(int i = 0; i < m.rows; ++i)
//	{
//		for(int j = 0; j < m.cols; ++j)
//			cout << m.at<float>(i, j) << " ";
//		cout << endl;
//	}
//
//	SpatialPatternMatching matcher(in, Gaussian, true, false, &pattern);
//
//	int count = 0;
//	cout << "in main" << endl;
//	while (true)
//	{
//		while (matcher.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//			Emage e = matcher.next();
//			cout << "Most similar at: " << e.getSwLat() << " " << e.getSwLong() << endl;
//
//			int32 rows = e.getNumRows();
//			int32 cols = e.getNumCols();
//			for (int32 i = 0; i < rows; ++i) {
//				for (int32 j = 0; j < cols; ++j)
//					cout << e.getArray().at<float>(i, j) << " ";
//				cout << endl;
//			}
//		}
//		Sleep(100);
//	}
//	return 0;
//}
