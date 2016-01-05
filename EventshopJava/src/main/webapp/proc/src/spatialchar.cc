#include "spatial_analysis.h"

using namespace std;
using namespace cv;


bool SpatialChar::has_next()
{
	if(!eit.has_next()) return false;
	return true;
}


Emage SpatialChar::next()
{
	Emage emage = eit.next();

	double min_val;
	double max_val;
	Point min_loc;
	Point max_loc;

	Mat in = emage.getArray();
	Mat out = Mat::zeros(1,1, CV_64F);
	Emage out_emage(emage, out);

	//double sw_lat = emage.getSwLat();
	double sw_long = emage.getSwLong();
	double ne_lat = emage.getNeLat();
	double lat_unit = emage.getLatUnit();
	double long_unit = emage.getLongUnit();

	if (in.rows > 0)
	{
		out_emage.setNumCols(1);
		out_emage.setNumRows(1);

		if(scop == SPTLMAX || scop == SPTLMIN )
		{
			minMaxLoc(in, &min_val, &max_val, &min_loc, &max_loc);
			if(scop == SPTLMAX)
			{
				out.at<double>(0,0) = max_val;
				out_emage.setSwLat(ne_lat - lat_unit*(max_loc.y+1));
				out_emage.setSwLong(sw_long + long_unit*max_loc.x);
			}
			if(scop == SPTLMIN )
			{
				out.at<double>(0,0) = min_val;
				out_emage.setSwLat(ne_lat - lat_unit*(min_loc.y+1));
				out_emage.setSwLong(sw_long + long_unit*min_loc.x);
			}
		}
		else if(scop == SPTLAVG)
		{
			out.at<double>(0,0) = (mean(in))[0];


			int numNonZeroPixels;
			int pixelVal;
				double sum_val = 0;
				for(int h = 0; h < in.rows; h++)
				{
					for(int w = 0; w < in.cols; w++)
					{
						pixelVal=in.at<double>(h,w);
						if (pixelVal>0)
						{
							sum_val += in.at<double>(h,w);
							numNonZeroPixels++;
						}
					}
				}
				out.at<double>(0,0) = (sum_val/numNonZeroPixels) +30;
				out_emage.setSwLat(-999);
				out_emage.setSwLong(-999);
		}
		else if(scop == SPTLSUM)
		{
			out.at<double>(0,0) = (sum(in))[0];
			out_emage.setSwLat(-999);
			out_emage.setSwLong(-999);
		}
		else if(scop == EPICENTER)
		{
			double sum_val_w = 0;
			double sum_val_h = 0;
			double sum_val = 0;
			for(int h = 0; h < in.rows; h++)
			{
				for(int w = 0; w < in.cols; w++)
				{
					sum_val += in.at<double>(h,w);
					sum_val_w += in.at<double>(h,w) * (w+1);
					sum_val_h += in.at<double>(h,w) * (h+1);
				}
			}

			double lat_long_val_w = (sum_val_w / sum_val) - 1;
			double lat_long_val_h = (sum_val_h / sum_val) - 1;
			out_emage.setSwLat(ne_lat - lat_unit*(lat_long_val_h+1));
			out_emage.setSwLong(sw_long + long_unit*lat_long_val_w);
		}
		else if(scop == COVERAGE)
		{
			out.at<double>(0,0) =  countNonZero(in);
			out_emage.setSwLat(-999);
			out_emage.setSwLong(-999);
		}
		else if(scop == CIRCULARITY)
		{
			out.at<double>(0,0) = countNonZero(in);

			Mat circle = Mat::zeros(in.rows, in.cols, CV_32F);
			cvCircle(new IplImage(circle), Point(in.rows/2, in.cols/2),
					min(in.rows/2, in.cols/2),
					CV_RGB(255,255,255), -1, 8, 0);

			Mat output;
			in.convertTo(in, CV_32F, 1);
			matchTemplate(in, circle, output, CV_TM_SQDIFF_NORMED);
			minMaxLoc(output, &min_val, &max_val, &min_loc, &max_loc);
			out.at<double>(0,0) = (double)1 - min_val;
			out_emage.setSwLat(ne_lat - lat_unit*(min_loc.y+in.rows/2+1));
			out_emage.setSwLong(sw_long + long_unit*(min_loc.x+in.cols/2));
		}
	}
	else
	{
		cerr << "Error: Input Emage is blank!";
	}

	out_emage.setArray(out);
	return out_emage;
}


SpatialChar::~SpatialChar()
{
}


//int main()
//{
//	EmageIngestor in("C:\\Project\\emage1");
//	SpatialChar sit(in, SPTLMAX);
//
//	int count = 0;
//	while(true)
//	{
//		while(sit.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//			Emage e = sit.next();
//
//			cout << e.getSwLat()<< ", " <<e.getSwLong()<< endl;
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
