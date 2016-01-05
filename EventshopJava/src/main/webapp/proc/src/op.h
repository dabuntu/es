#ifndef OP_H_
#define OP_H_

#include <cassert>
#include <stdint.h>

#include <limits>
#include <vector>
#include <iostream>
#include <fstream>

#include "cv.h"
#include "message.pb.h"

using namespace std;

enum CompOp{LT, LE, EQ, GT, GE, RG};

enum AggregateOp {AggMAX, AggMIN, AggSUM, AggAVG, AggSUB, AggMUL, AggDIV, AggAND, AggOR, AggNOT, AggXOR, AggCOV};

enum GroupingOp{KMEANS, AFFINITY};

enum SpatialCharOp{SPTLMAX, SPTLMIN, SPTLAVG, SPTLSUM, EPICENTER, COVERAGE, CIRCULARITY};

enum TemporalCharOp{DISPLACEMENT, VELOCITY, ACCELERATION, GROWTHRATE, PERIODICITY};

enum SpatialPatternOp {Input, Gaussian, Linear2D};

enum grptype{gt_absolute, gt_relative};

enum GroupingColors{red = 0xff0000, green = 0x00c000, yellow = 0xffff00, blue = 0x0000ff, orange=0xff9933,
	purple=0xcc0099, brown = 0x996600, white = 0xffffff, black = 0x000000, grey = 0x999999};

typedef int32_t int32;
typedef int64_t int64;


// Implementation of template function need to be put
// in header files
template <typename T>
void print_matrix(const cv::Mat& in, const string& name)
{
	cout << name << endl;
	for(int32 i = 0; i < in.rows; ++i)
	{
		for (int32 j = 0; j < in.cols; ++j)
		{
			cout << in.at<T>(i, j) << " ";
		}
		cout << endl;
	}
}

class Emage;

void show_matrix(const cv::Mat& in, const string& filename);
void create_output(Emage& emage, const string& filepath);
void create_output(Emage& emage, const string& filepath, const string& colors);
void invert_matrix(Emage& e);
void cutoff_matrix(Emage& e);

Emage createProjectedOverlay (Emage& e, double & minVal, double& maxVal);
double GudermannianInv(double latitude);
double Gudermannian(double y);

class Emage
{
	string theme;

	int64 start_time;
	int64 end_time;

	double lat_unit;
	double long_unit;

	double sw_lat;
	double sw_long;
	double ne_lat;
	double ne_long;

	int32 num_rows;
	int32 num_cols;

	cv::Mat array;

public:
	Emage();	// no image constructed
	Emage(const Emage& emage);	// shallow copy constructor
	Emage(const Emage& emage, const cv::Mat& m);	// deep constructor

	// GetArray returns reference
	cv::Mat& getArray() { return array; }

	// SetArray does shallow copy
	void setArray(cv::Mat& m) { array = m; }
	// CopyArray does deep copy
	void copyArray(const cv::Mat& m) { array = m.clone(); }

    int64 getEndTime() const;
    double getLatUnit() const;
    double getLongUnit() const;
    double getNeLat() const;
    double getNeLong() const;
    int32 getNumCols() const;
    int32 getNumRows() const;
    int64 getStartTime() const;
    double getSwLat() const;
    double getSwLong() const;
    string getTheme() const;

    void setEndTime(const int64 end_time);
    void setLatUnit(const double lat_unit);
    void setLongUnit(const double long_unit);
    void setNeLat(const double ne_lat);
    void setNeLong(const double ne_long);
    void setNumCols(const int32 num_cols);
    void setNumRows(const int32 num_rows);
    void setStartTime(const int64 start_time);
    void setSwLat(const double sw_lat);
    void setSwLong(const double sw_long);
    void setTheme(const string& theme);
};


class ProcEmageIterator
{
protected:
	string theme;
public:
	ProcEmageIterator() {};

	void setTheme(const string& th) { this->theme = string(th); }

	virtual bool has_next() = 0;
	virtual Emage next() = 0;
	virtual ~ProcEmageIterator() {};
};


class EmageIngestor : public ProcEmageIterator
{
	// file stream to get input from Java
	FILE *file;
	string filename;

	// EmageMsg object
	EmageMsg msg;
	// Store if the next is available
	bool is_next_available;

	// Store last modified time of file
	long last_modified_time;

	int32 size;

public:
	EmageIngestor(const string& filename)
	{
		size = 0;
		this->filename = string(filename);
		is_next_available = false;
		last_modified_time = -1l;

		GOOGLE_PROTOBUF_VERIFY_VERSION;

		// Need to open with "rb" mode in windows!!!
		// 08/19/2011 Mingyan
		// moving opening file in has_next();
//		file = NULL;
//		file = fopen(filename.c_str(), "rb");
//		if(file == NULL)
//			cerr << "File " << filename << " fails to open!" << endl;
	}

	virtual bool has_next();
	virtual Emage next();
	virtual ~EmageIngestor();
};


class FilterCondition
{
	// Users have three options to form spatial filter, which will finally be
	// mapped to masks
	// 1. Semantic expr: California
	// 2. A set of bounding boxes
	// 3. Mask
	cv::Mat mask;
	CompOp op;

	double val_min;
	double val_max;

	int64 tm_min;
	int64 tm_max;

	bool norm_mode;
	double norm_min;
	double norm_max;

public:
	cv::Mat& get_mask() { return mask; }
	CompOp get_op() const { return op; }

	double get_val_min() const { return val_min; }
	double get_val_max() const { return val_max; }
	int64  get_tm_min()  const { return tm_min; }
	int64  get_tm_max()  const { return tm_max; }

	bool get_norm_mode() const {return norm_mode;}
	double get_norm_min() const { return norm_min; }
	double get_norm_max() const { return norm_max; }

	FilterCondition(const int32 num_rows, const int32 num_cols)
	: mask(cv::Mat::ones(num_rows, num_cols, CV_8U))
	{
		val_min = -1 * numeric_limits<double>::max();
		val_max = numeric_limits<double>::max();
		tm_min = 0;
		tm_max = numeric_limits<int64>::max();
	}

	FilterCondition(const cv::Mat& m,
			const CompOp cop = LT,
			const double v_min = -1 * numeric_limits<double>::max(),
			const double v_max = numeric_limits<double>::max(),
			const int64 t_min = 0,
			const int64 t_max = numeric_limits<int64>::max(),
			const bool nMode=false,
			const double nMin=-999,
			const double nMax=-999)
	: mask(m), op(cop), val_min(v_min), val_max(v_max), tm_min(t_min), tm_max(t_max), norm_mode(nMode), norm_min(nMin), norm_max(nMax)
	{}

	FilterCondition(const string& maskPath,
			const CompOp cop = LT,
			const double v_min = -1 * numeric_limits<double>::max(),
			const double v_max = numeric_limits<double>::max(),
			const int64 t_min = 0,
			const int64 t_max = numeric_limits<int64>::max(),
			const bool nMode=false,
			const double nMin=-999,
			const double nMax=-999)
	: op(cop), val_min(v_min), val_max(v_max), tm_min(t_min), tm_max(t_max), norm_mode(nMode), norm_min(nMin), norm_max(nMax)
	{
		ifstream maskFile(maskPath.c_str());

		char ch;
		int rows = 0, cols = 0;
		maskFile >> rows >> ch;
		maskFile >> cols;
		mask.create(rows, cols, CV_8U);

		for(int i = 0; i < rows; ++i)
			for(int j = 0; j < cols; ++j)
			{
				unsigned char bit = 0;
				maskFile >> bit;
				mask.at<unsigned char>(i, j) = bit;
				if(j < cols-1)
					maskFile >> ch;
			}
		maskFile.close();
	}
};


class Filter: public ProcEmageIterator
{
	Emage cur_emage;

	ProcEmageIterator& eit;
	FilterCondition fcond;

	bool check_time(const Emage& emage);
	bool select_location(Emage& emage);
	bool select_value(Emage& emage);
	void normalizeEmage(Emage& e);

public:
	Filter(ProcEmageIterator& it, const FilterCondition& cond)
	: eit(it),
	  fcond(cond)
	{}

	virtual bool has_next();
	virtual Emage next();
	virtual ~Filter();
};


class Aggregate: public ProcEmageIterator
{
	vector<ProcEmageIterator*>& eits;
	vector<double> scalarVec;
	AggregateOp aop;
	bool scalar1stParam;
	bool normValue;
	double normMin;
	double normMax;

	void normalizeEmage(Emage& e);

public:
	Aggregate(vector<ProcEmageIterator*>& its, AggregateOp op, bool normVal = false, double min = 0.0, double max = 100)
	: ProcEmageIterator(),
	  eits(its),
	  aop(op),
	  normValue(normVal),
	  normMin(min),
	  normMax(max)
	{
//		if(aop == AggSUB || aop == AggDIV || aop == AggAND || aop == AggOR || aop == AggXOR || aop == AggCOV)
//		{
//			assert(eits.size() == 2);
//		}
//		else if(aop == AggNOT)
//		{
//			assert(eits.size() == 1);
//		}
	}


	Aggregate(vector<ProcEmageIterator*>& its, vector<double>scVec, AggregateOp op, bool scalarFirst,
			bool normVal = false, double min = 0.0, double max = 100)
	: ProcEmageIterator(),
	  eits(its),
	  scalarVec(scVec),
	  aop(op),
	  scalar1stParam(scalarFirst),
	  normValue(normVal),
	  normMin(min),
	  normMax(max)
	{}


	virtual bool has_next();
	virtual Emage next();
	virtual ~Aggregate();
};


class GroupingCriteria
{
	vector<double> thresholds;
public:
	grptype critGrpType;
    const vector<double>& getThresholds() const;
    void setThresholds(const vector<double>& thresholds);
};


class Grouping : public ProcEmageIterator
{
	ProcEmageIterator& eit;
	int32 num_groups_;
	GroupingOp gop;

	// Store the index of cluster that should be returned next
	int next_index_;
	// Store the next emage
	Emage next_emage_;
	// Store the cluster index matrix
	cv::Mat cluster_index_;

	int num_rows;
	int num_cols;

	GroupingCriteria criteria;
	bool split_groups;
	bool do_coloring;
	vector<GroupingColors> color_codes;


public:
	Grouping(ProcEmageIterator&it, int32 num_grp, GroupingOp op, bool varSplitGroups, bool coloring,
			const vector<GroupingColors>& colors = vector<GroupingColors>())
	: eit(it),
	  num_groups_(num_grp),
	  gop(op),
	  next_index_(-1),
	  split_groups(varSplitGroups),
	  do_coloring(coloring),
	  color_codes(colors)
	{
		if(do_coloring)
		{
			assert((unsigned)num_grp == colors.size());
		}
	}


	Grouping(ProcEmageIterator& it, GroupingCriteria crit, bool varSplitGroups, bool coloring,
			const vector<GroupingColors>& colors = vector<GroupingColors>()):
		eit(it),
		next_index_(-1),
		criteria (crit),
		split_groups(varSplitGroups),
		do_coloring(coloring),
		color_codes(colors)
	{
		num_groups_ = crit.getThresholds().size()+1;
	}

	virtual bool has_next();
	virtual Emage next();
	virtual ~Grouping();
};

#endif /* OP_H_ */
