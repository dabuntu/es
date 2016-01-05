/*
 * grouping.cc
 *
 *  Created on: Jul 8, 2011
 *      Author: singhv
 */

#include "op.h"

using namespace std;
using namespace cv;


const vector<double>& GroupingCriteria::getThresholds() const
{
    return thresholds;
}


void GroupingCriteria::setThresholds(const vector<double>& thresholds)
{
    this->thresholds = thresholds;
}


bool Grouping::has_next()
{
	// If there is an un-returned clustered sub-emage
	if(split_groups)
	{
		if(next_index_ != -1 && next_index_ < num_groups_) return true;
	}

	// If there is no input
	if(!eit.has_next()) return false;

	// Else, there is a new input emage
	next_index_ = 0;

	next_emage_ = eit.next();
	Emage emage = next_emage_;

	num_rows = emage.getArray().rows;
	num_cols = emage.getArray().cols;

	if(gop == KMEANS)
	{
		// Change the value to be of CV_32F type for segmentation
		// Generate the input value as a matrix of num_rows*num_cols by 3
		Mat array = emage.getArray();
		Mat input(num_rows * num_cols, 3, CV_32F);

		// Initialize the sample matrix
		for(int i = 0; i < num_rows; ++i)
		{
			for(int j = 0; j < num_cols; ++j)
			{
				input.at<float>(i*num_cols + j, 0) = (float)array.at<double>(i, j);
				input.at<float>(i*num_cols + j, 1) = 0.2*(float)i;
				input.at<float>(i*num_cols + j, 2) = 0.2*(float)j;
			}
		}

		// Prepare the output, center and cluster index matrices
		Mat output(num_rows*num_cols, 1, DataType<int>::type);
		Mat centers = Mat::zeros(num_groups_, 3, CV_32F);
		cluster_index_ = Mat(num_rows, num_cols, DataType<int>::type);

		// Do the clustering
		CvTermCriteria term_criteria = cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3);
		kmeans(input, num_groups_, output, term_criteria, 3, KMEANS_RANDOM_CENTERS, centers);

		for(int i = 0; i < num_rows; ++i)
		{
			for(int j = 0; j < num_cols; ++j)
			{
				cluster_index_.at<int>(i, j) = output.at<int>(i*num_cols+j, 0);
			}
		}

#ifdef DEBUG
		for(int i = 0; i < num_groups; ++i)
			cout << centers.at<float>(i, 0) << " " << centers.at<float>(i, 1) << " "
			<< centers.at<float>(i, 2) << endl;
#endif
	}
	// Do clustering based on grouping criteria
	else
	{
		Mat array = emage.getArray();
		cluster_index_ = Mat(num_rows, num_cols, DataType<int>::type);

		vector<double>::const_iterator begin = criteria.getThresholds().begin();
		vector<double>::const_iterator end = criteria.getThresholds().end();
		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
			{
				vector<double>::const_iterator it = lower_bound(begin, end, array.at<double>(i, j));
				cluster_index_.at<int>(i, j) = it - begin;
			}
	}
	return true;
}


Emage Grouping::next()
{
	Mat result;

	if(!do_coloring && split_groups)
	{
        result = next_emage_.getArray().clone();
		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
			{
				if(cluster_index_.at<int>(i, j) != next_index_)
					result.at<double>(i, j) = 0;
			}
		// Increate the cluster index for next cluster
		next_index_++;
	}
	else if(!do_coloring && !split_groups)
	{
		cluster_index_.clone().convertTo(result, CV_64F, 1);
		next_index_ = num_groups_;
	}
	else if(do_coloring && split_groups)
	{
		result = Mat::zeros(num_rows, num_cols, DataType<int32>::type);
		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
			{
				if(cluster_index_.at<int>(i, j) == next_index_)
					result.at<int32>(i, j) = color_codes[next_index_];
				else
					result.at<int32>(i, j) = white;
			}
		// Increate the cluster index for next cluster
		next_index_++;
	}
	else if(do_coloring && !split_groups)
	{
		result = Mat::zeros(num_rows, num_cols, DataType<int32>::type);
		for(int i = 0; i < num_rows; ++i)
			for(int j = 0; j < num_cols; ++j)
			{
				result.at<int32>(i, j) = color_codes[cluster_index_.at<int>(i, j)];
			}
	}

	return Emage(next_emage_, result);
}


Grouping::~Grouping()
{
}


//int main()
//{
//	EmageIngestor in1("C:\\Project\\emage1");
//
//	Grouping grouping(in1, 3, KMEANS);
//
//	int count = 0;
//	while(true)
//	{
//		while(grouping.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//
//			Emage e = grouping.next();
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
