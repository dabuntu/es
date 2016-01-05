#include "spatial_analysis.h"
#include "temporal_analysis.h"

#include <ctime>


using namespace cv;


/**
 * Aggregate(vector<ProcEmageIterator*>& its, AggregateOp op)
 */
void test_aggregation()
{
	ProcEmageIterator *in1 = new EmageIngestor("C:\\Project\\emage1");
	ProcEmageIterator *in2 = new EmageIngestor("C:\\Project\\emage2");

	vector<ProcEmageIterator*> eits;
	eits.push_back(in1);
	eits.push_back(in2);

//	Aggregate agg(eits, AggSUM);
	Aggregate agg(eits, AggMAX);
//	Aggregate agg(eits, AggMIN);
//	Aggregate agg(eits, AggAVG);
//	Aggregate agg(eits, AggSUB);
//	Aggregate agg(eits, AggMUL);
//	Aggregate agg(eits, AggDIV);
//	Aggregate agg(eits, AggAND);
//	Aggregate agg(eits, AggOR);
//	Aggregate agg(eits, AggXOR);
//	Aggregate agg(eits, AggNOT);
//	Aggregate agg(eits, AggCOV);

	int count = 0;
	while(true)
	{
		while(agg.has_next() && count < 10)
		{
			++count;
			cout << "count: " << count << endl;
			Emage e = agg.next();

			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();
			for(int32 i = 0; i < rows; ++i)
			{
				for(int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
		}
		sleep(100);
	}
}


/**
 * FilterCondition(const cv::Mat& m, const CompOp cop, const double v_min, const double v_max,
 *  const int64 t_min, const int64 t_max)
 * Filter(ProcEmageIterator& it, const FilterCondition& cond);
 */
void test_filter()
{
	EmageIngestor ds3("C:\\Temp\\ds3_CSV-Population");
	int rows = 26;
	int cols = 59;
	Mat mask0(rows, cols, CV_8U);
	for(int i = 0; i < rows; ++i)
	{
		for(int j = 0; j < cols; ++j)
		{
			if(j <= 50 && j> 4 && i<=19 && i> 2)
				mask0.at<unsigned char>(i, j) = 255;
			else
				mask0.at<unsigned char>(i, j) = 0;
		}
	}
	FilterCondition fcond0(mask0, LT, -99999999, 99999999, -60889584074918677, 9223372036854775807, true, 0.0, 90);
	Filter q0(ds3, fcond0);

	while(true)
	{
		while(q0.has_next())
		{
			Emage e = q0.next();
			create_output(e, "C:\\Program Files\\Apache Software Foundation\\apache-tomcat-7.0.14\\webapps\\eventshop\\results\\q0_filter");
		}
		sleep(100);
	}
}


/**
 * Grouping(ProcEmageIterator&it, int32 num_grp, GroupingOp op, bool varSplitGroups, bool coloring,
			const vector<GroupingColors>& colors);
 */
void test_grouping()
{
	EmageIngestor in("C:\\Project\\Population");
	//EmageIngestor in("C:\\Temp\\q0\\q0_ds0_Twitter-Asthma0");
	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);

	Grouping grouping(in, 3, KMEANS, false, true, grpColors);

	int count = 0;
	while(true)
	{
		while(grouping.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = grouping.next();
/*	
			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();
			for(int32 i = 0; i < rows; ++i)
			{
				for(int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
*/			create_output(e, "cluster.bmp");
		}
		sleep(100);
	}
}


/**
 * SpatialChar(ProcEmageIterator &it, SpatialCharOp op);
 */
void test_spatial_char()
{
	EmageIngestor in("C:\\Temp\\ds2_Twitter-Sad");

	SpatialChar sit(in, SPTLMAX);
	//	SpatialChar sit(in, SPTLMIN);
	//	SpatialChar sit(in, SPTLAVG);
	//	SpatialChar sit(in, SPTLSUM);
	//  SpatialChar sit(in, EPICENTER);
	//	SpatialChar sit(in, COVERAGE);
	//	SpatialChar sit(in, CIRCULARITY);

	int count = 0;
	while(true)
	{
		while(sit.has_next())
		{
			++count;
			cout << "count: " << count << endl;
			Emage e = sit.next();

			cout << e.getSwLat()<< ", " <<e.getSwLong()<< endl;

			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();
			for(int32 i = 0; i < rows; ++i)
			{
				for(int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
		}
		sleep(100);
	}
}


/**
 * SpatialPatternMatching(ProcEmageIterator& it,
 * SpatialPatternOp op, bool size, bool amplitude, SpatialPattern * const pat);
 * GaussianPattern(int32 rows, int32 cols, float x, float y, float sx, float sy, float amp);
 * LinearPattern(int32 rows, int32 cols, float x, float y, float value, float dg, float vg);
 */
void test_spatial_pattern()
{
	EmageIngestor in("C:\\Project\\emage1");
	GaussianPattern pattern(5, 5, 2.0, 2.0, 3.0, 3.0, 240.0);
	//	LinearPattern pattern(10, 10);
	Mat m = pattern.createPattern();
	for(int i = 0; i < m.rows; ++i)
	{
		for(int j = 0; j < m.cols; ++j)
			cout << m.at<float>(i, j) << " ";
		cout << endl;
	}

	SpatialPatternMatching matcher(in, Gaussian, true, true, &pattern);

	int count = 0;
	while (true)
	{
		while (matcher.has_next())
		{
			++count;
			cout << "count: " << count << endl;
			Emage e = matcher.next();
			cout << "Most similar at: " << e.getSwLat() << " " << e.getSwLong() << endl;

			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();
			for (int32 i = 0; i < rows; ++i) {
				for (int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
		}
		sleep(100);
	}
}


/**
 * TemporalChar(ProcEmageIterator& it, TemporalCharOp op, int64 timeWindowToConsider);
 */
 /*
void test_temporal_char()
{
	EmageIngestor in("C:\\Project\\emage1");

	TemporalChar tpchar(in, DISPLACEMENT, 1000*30);
	//	TemporalChar tpchar(in, VELOCITY, 1000*30);
	//	TemporalChar tpchar(in, ACCELERATION, 1000*30);
	//	TemporalChar tpchar(in, GROWTHRATE, 1000*30);

	int count = 0;
	while(true)
	{
		while(tpchar.has_next() && count < 1000)
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = tpchar.next();
			cout << e.getSwLat()<< " " << e.getSwLong()<< endl;

			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();
			for(int32 i = 0; i < rows; ++i)
			{
				for(int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
		}
		sleep(100);
	}
}
*/

/**
 * TemporalPatternMatching(ProcEmageIterator& it, TemporalPatternOp tpop, bool multi_amp, bool multi_dur,
		int64 window, temporalPatternTemplate *tpt);
	LinearTemporalPatternTemplate(double varslope, double vary_intercept, int64 vartimeBetweenFrames, int64 vartimeWindow);
	ExponentialTemporalPatternTemplate(double varbase, double varscale, int64 vartimeBetweenFrames, int64 vartimeWindow)
	PeriodicTemporalPatternTemplate(double varfrequency, double varamplitude, double varphaseDelay,
	int64 vartimeBetweenFrames, int64 vartimeWindow );
 */
void test_temporal_pattern()
{
	EmageIngestor in("C:\\Project\\emage1");

	SpatialChar spchar(in, SPTLSUM);
	LinearTemporalPatternTemplate* ltptemplate = new LinearTemporalPatternTemplate(1, 1, 10000, 3*10000);
	TemporalPatternMatching matching(spchar, tpo_LINEAR, false, false, 5*10000, ltptemplate);

	//	ExponentialTemporalPatternTemplate *ltptemplate = new ExponentialTemporalPatternTemplate(2, 2, 10000, 3*10000);
	//	TemporalPatternMatching matching(spchar, tpo_EXPONENTIAL, false, true, 5*10000, ltptemplate);

	//	PeriodicTemporalPatternTemplate *ltptemplate = new PeriodicTemporalPatternTemplate(1, 1, 0, 10000, 5*10000);
	//	TemporalPatternMatching matching(spchar, tpo_PERIODIC, false, true, 10*10000, ltptemplate);

	int count = 0;
	while(true)
	{
		while(matching.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = matching.next();
			int32 rows = e.getNumRows();
			int32 cols = e.getNumCols();

			cout << e.getSwLat() << ", " << e.getSwLong() << endl;
			for(int32 i = 0; i < rows; ++i)
			{
				for(int32 j = 0; j < cols; ++j)
					cout << e.getArray().at<double>(i, j) << " ";
				cout << endl;
			}
		}
		sleep(100);
	}
}


void test_pollen_us()
{
//	EmageIngestor *pollen = new EmageIngestor("C:\\Project\\pollen");
//	EmageIngestor *us = new EmageIngestor("C:\\Project\\us");
//
//	vector<ProcEmageIterator*> eits;
//	eits.push_back(pollen);
//	eits.push_back(us);
//
//	Aggregate aggregate(eits, AggSUM);
//	Grouping grouping(aggregate, 3, KMEANS);
//	SpatialChar spchar(grouping, EPICENTER);
//
//	int count = 0;
//	while(true)
//	{
//		while(spchar.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//
//			Emage e = spchar.next();
//
//			cout << "The epicenter is at: " << endl;
//			cout << "latitude: " << e.getSwLat() << " longitude: " << e.getSwLong() << endl;
//
//			int32 rows = e.getNumRows();
//			int32 cols = e.getNumCols();
//			for(int32 i = 0; i < rows; ++i)
//			{
//				for(int32 j = 0; j < cols; ++j)
//					cout << e.getArray().at<double>(i, j) << " ";
//				cout << endl;
//			}
//
//			//string filename = "pollen_us_aggdiv.jpeg";
//			//show_matrix(e.getArray(), filename);
//		}
//		sleep(100);
//	}
}


void test_asthma_us()
{
	EmageIngestor *AQI = new EmageIngestor("C:\\Project\\AQI");
	EmageIngestor *pollen = new EmageIngestor("C:\\Project\\pollen");
	EmageIngestor *tweets_us = new EmageIngestor("C:\\Project\\us");

	vector<ProcEmageIterator*> eits;
	eits.push_back(pollen);
	eits.push_back(AQI);
	eits.push_back(tweets_us);

	//1) add them
	Aggregate aggregate(eits, AggSUM);

	//2) threshold them
	GroupingCriteria gcrit= GroupingCriteria ();
	gcrit.critGrpType = gt_absolute;
	vector<double> thresh;
	thresh.push_back(70);
	thresh.push_back(140);
	gcrit.setThresholds(thresh);

	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);

	Grouping grouping(aggregate, gcrit, false, true, grpColors);

	int count = 0;
	while(true)
	{
		while(grouping.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = grouping.next();
			struct tm *timeinfo;
			long time = e.getEndTime()/1000;
			timeinfo = localtime(&time);

			cout << "Asthma clusters at time " << asctime(timeinfo);

			char buffer[20];
			sprintf(buffer, "Asthma_Clusters_%d.jpeg", count);
			show_matrix(e.getArray(), buffer);
		}
		sleep(100);
	}
}


void test_asthma_Irvine()
{
	EmageIngestor *pollen = new EmageIngestor("C:\\Project\\pollen");
	EmageIngestor *AQI= new EmageIngestor("C:\\Project\\AQI");
	EmageIngestor *tweets_us = new EmageIngestor("C:\\Project\\us");

	vector<ProcEmageIterator*> eits;
	eits.push_back(pollen);
	eits.push_back(AQI);
	eits.push_back(tweets_us);

	//1) add them
	Aggregate aggregate(eits, AggSUM);

	//2) threshold them
	GroupingCriteria gcrit= GroupingCriteria();
	gcrit.critGrpType = gt_absolute;
	vector<double> thresh;
	thresh.push_back(70);
	thresh.push_back(140);
	gcrit.setThresholds(thresh);

	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);
	Grouping grouping(aggregate, gcrit, false, false);

	// 3) do the filtering based on irvine
	int rows = 260;
	int cols = 590;
	Mat mask(rows, cols, CV_8U);
	for(int i = 0; i < rows; ++i)
	{
		for(int j = 0; j < cols; ++j)
		{
			if(j >= 72 && j<82 && i>=160 && i<170)// LOCATION FOR IRVINE... set as mask
				mask.at<unsigned char>(i, j) = 255;
			else
				mask.at<unsigned char>(i, j) = 0;
//			printf("%x ", mask.at<unsigned char>(i, j));
		}
//		cout << endl;
	}
//	show_matrix(mask, "mask.bmp");

	FilterCondition fcond(mask);//CHANGE THE TEMPORAL RANGE
	Filter filter(grouping, fcond);

	// 4) select the average of the irvine area
	SpatialChar spchar(filter, SPTLSUM);

	int count = 0;
	while(true)
	{
		while(spchar.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = spchar.next();

			struct tm *timeinfo;
			long time = e.getEndTime()/1000;
			timeinfo = localtime(&time);

			double rating = e.getArray().at<double>(0, 0) / countNonZero(mask);
			cout << "The average allergy rating at Irvine is " << rating << " at time " << asctime(timeinfo);

			if (rating > 1)
				cout<< "ALERT: LARGE ASTHMATIC ACTIVITY FOUND NEAR YOUR LOCALITY" << endl;
			else
				cout<< "NO SIGNIFICANT ACTIVITY FOUND NEAR YOUR LOCALITY" << endl;
		}
		sleep(100);
	}
}


void test_asthma_nationalIndex()
{
	EmageIngestor *pollen = new EmageIngestor("C:\\Project\\pollen");
	EmageIngestor *AQI = new EmageIngestor("C:\\Project\\AQI");
	EmageIngestor *tweets_us = new EmageIngestor("C:\\Project\\us");

	vector<ProcEmageIterator*> eits;
	eits.push_back(pollen);
	eits.push_back(AQI);
	eits.push_back(tweets_us);

	//1) add them
	Aggregate aggregate(eits, AggSUM);

	//2) threshold them
	GroupingCriteria gcrit = GroupingCriteria();
	gcrit.critGrpType = gt_absolute;
	vector<double> thresh;
	thresh.push_back(70);
	thresh.push_back(140);
	gcrit.setThresholds(thresh);

	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);
	Grouping *grouping = new Grouping(aggregate, gcrit, false, false, grpColors);

	EmageIngestor population("C:\\Project\\Population");
	//3)Multiply with population
	vector<ProcEmageIterator*> eits2;
	eits2.push_back(grouping);
	eits2.push_back(&population);
	Aggregate agg2(eits2, AggMUL);

	// 4) Get the weighted sum
	SpatialChar weighted_sum(agg2, SPTLSUM);

	// 5) Get the sum of Population
	EmageIngestor population1 ("C:\\Project\\Population");
	SpatialChar popSum(population1, SPTLSUM);

	//6) Normalizing by the sum of population
	vector<ProcEmageIterator*> eits4;
	eits4.push_back(&weighted_sum);
	eits4.push_back(&popSum);
	Aggregate agg4(eits4, AggDIV);

	int count = 0;
	while(true)
	{
		while(agg4.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = agg4.next();
			struct tm *timeinfo;
			long time = e.getEndTime()/1000;
			timeinfo = localtime(&time);
			cout << "National allergy Index is: " << e.getArray().at<double>(0, 0)
					<< " at " << asctime(timeinfo);
		}
		sleep(100);
	}
}


void test_linear_similarity()
{
	EmageIngestor *pollen = new EmageIngestor("C:\\Project\\pollen");
	EmageIngestor *AQI = new EmageIngestor("C:\\Project\\AQI");
	EmageIngestor *tweets_us = new EmageIngestor("C:\\Project\\us");

	vector<ProcEmageIterator*> eits;
	eits.push_back(pollen);
	eits.push_back(AQI);
	eits.push_back(tweets_us);

	//1) add them
	Aggregate aggregate(eits, AggSUM);

	//2) threshold them
	GroupingCriteria gcrit = GroupingCriteria();
	gcrit.critGrpType = gt_absolute;
	vector<double> thresh;
	thresh.push_back(70);
	thresh.push_back(140);
	gcrit.setThresholds(thresh);

	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);
	Grouping *grouping = new Grouping(aggregate, gcrit, false, false, grpColors);

	EmageIngestor population("C:\\Project\\Population");
	// 3) Multiply with population
	vector<ProcEmageIterator*> eits2;
	eits2.push_back(grouping);
	eits2.push_back(&population);
	Aggregate agg2(eits2, AggMUL);

	// 4) Get the weighted sum
	SpatialChar weighted_sum(agg2, SPTLSUM);

	// 5) Get the sum of Population
	EmageIngestor population1 ("C:\\Project\\Population");
	SpatialChar popSum(population1, SPTLSUM);

	// 6) Normalizing by the sum of population
	vector<ProcEmageIterator*> eits4;
	eits4.push_back(&weighted_sum);
	eits4.push_back(&popSum);
	Aggregate agg4(eits4, AggDIV);

	// 7) Comparing with linear pattern
	LinearTemporalPatternTemplate* ltptemplate = new LinearTemporalPatternTemplate(1, 1, 10000, 3*10000);
	TemporalPatternMatching matching(agg4, tpo_LINEAR, false, false, 5*10000, ltptemplate);


	int count = 0;
	while(true)
	{
		while(matching.has_next())
		{
			++count;
			cout << "count: " << count << endl;

			Emage e = matching.next();
			struct tm *timeinfo;
			long time = e.getEndTime()/1000;
			timeinfo = localtime(&time);
			cout << "The similarity of national index to a linear pattern of gradient " << 1 <<
					" is: " << e.getArray().at<double>(0, 0) << " at " << asctime(timeinfo);
		}
		sleep(100);
	}
}


int remote_test()
{
	EmageIngestor ds4("C:\\Temp\\ds4_Visual-Pollen");
	EmageIngestor ds6("C:\\Temp\\ds6_Visual-AQI");
	EmageIngestor ds7("C:\\Temp\\ds7_Twitter-Asthma");
	int rows0 = 260;
	int cols0 = 590;
	Mat mask0(rows0 , cols0 , CV_8U);
	for(int i = 0; i < rows0; ++i)
	{
		for(int j = 0; j < cols0; ++j)
		{
			if(j <= 640 && j> 26 && i<=256 && i> 5)
				mask0.at<unsigned char>(i, j) = 255;
			else
				mask0.at<unsigned char>(i, j) = 0;
		}
	}
	FilterCondition fcond0(mask0, LT, -99999999, 99999999, 1314403658356, 9223372036854775807, true, 0.0, 1.0);
	Filter q0(ds4, fcond0);

	int rows1 = 260;
	int cols1 = 590;
	Mat mask1(rows1 , cols1 , CV_8U);
	for(int i = 0; i < rows1; ++i)
	{
		for(int j = 0; j < cols1; ++j)
		{
			if(j <= 672 && j> 30 && i<=239 && i> 5)
				mask1.at<unsigned char>(i, j) = 255;
			else
				mask1.at<unsigned char>(i, j) = 0;
		}
	}
	FilterCondition fcond1(mask1, LT, -99999999, 99999999, 1314403658356, 9223372036854775807, true, 0.0, 1.0);
	Filter q1(ds6, fcond1);

	int rows2 = 260;
	int cols2 = 590;
	Mat mask2(rows2 , cols2 , CV_8U);
	for(int i = 0; i < rows2; ++i)
	{
		for(int j = 0; j < cols2; ++j)
		{
			if(j <= 621 && j> 27 && i<=259 && i> 7)
				mask2.at<unsigned char>(i, j) = 255;
			else
				mask2.at<unsigned char>(i, j) = 0;
		}
	}
	FilterCondition fcond2(mask2, LT, -99999999, 99999999, 1314943658356, 9223372036854775807, true, 0.0, 1.0);
	Filter q2(ds7, fcond2);


	vector<ProcEmageIterator*> eits0;
	eits0.push_back(&q0);
	eits0.push_back(&q1);
	eits0.push_back(&q2);
	Aggregate q3(eits0, AggSUM);

	vector<GroupingColors> grpColors;
	grpColors.push_back(green);
	grpColors.push_back(yellow);
	grpColors.push_back(red);

	GroupingCriteria gcrit0 = GroupingCriteria();
	gcrit0.critGrpType = gt_absolute;
	vector<double> thresh0;
	thresh0.push_back(1);
	thresh0.push_back(2);
	gcrit0.setThresholds(thresh0);
	Grouping q4(q3, gcrit0, false, true, grpColors);
	while(true)
	{
		while(q4.has_next())
		{
			Emage e = q4.next();
			create_output(e, "C:\\Program Files\\Apache Software Foundation\\apache-tomcat-7.0.14\\webapps\\eventshop\\results\\q4_grouping");
		}
		sleep(100);
	}
	return 0;
}



int main(int argc, char **argv)
{
//	test_aggregation();
//	test_filter();
//	test_grouping();
	test_spatial_char();
//	test_spatial_pattern();
//	test_temporal_char();
//	test_temporal_pattern();
//	test_pollen_us();
//	test_asthma_us();
//	test_asthma_Irvine();
//	test_asthma_nationalIndex();
//	test_linear_similarity();
//	remote_test();

	return 0;
}
