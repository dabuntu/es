#include "op.h"


Emage::Emage()
{
	theme = "";

	start_time = end_time = 0;
	lat_unit = long_unit = 0;
	sw_lat = sw_long = ne_lat = ne_long = 0;
	num_rows = num_cols = 0;
}


// Shallow Initialization
Emage::Emage(const Emage& emage) : array(emage.array)
{
	theme = emage.theme;

	start_time = emage.start_time;
	end_time = emage.end_time;

	lat_unit = emage.lat_unit;
	long_unit = emage.long_unit;
	sw_lat = emage.sw_lat;
	sw_long = emage.sw_long;
	ne_lat = emage.ne_lat;
	ne_long = emage.ne_long;

	num_rows = emage.num_rows;
	num_cols = emage.num_cols;
}


// Deep Initialization
Emage::Emage(const Emage& emage, const cv::Mat& m)
{
	theme = emage.theme;

	start_time = emage.start_time;
	end_time = emage.end_time;

	lat_unit = emage.lat_unit;
	long_unit = emage.long_unit;
	sw_lat = emage.sw_lat;
	sw_long = emage.sw_long;
	ne_lat = emage.ne_lat;
	ne_long = emage.ne_long;

	num_rows = emage.num_rows;
	num_cols = emage.num_cols;

	array = m.clone();
}


int64 Emage::getEndTime() const
{
    return end_time;
}

double Emage::getLatUnit() const
{
    return lat_unit;
}

double Emage::getLongUnit() const
{
    return long_unit;
}

double Emage::getNeLat() const
{
    return ne_lat;
}

double Emage::getNeLong() const
{
    return ne_long;
}

int32 Emage::getNumCols() const
{
    return num_cols;
}

int32 Emage::getNumRows() const
{
    return num_rows;
}

int64 Emage::getStartTime() const
{
    return start_time;
}

double Emage::getSwLat() const
{
    return sw_lat;
}

double Emage::getSwLong() const
{
    return sw_long;
}

string Emage::getTheme() const
{
    return theme;
}

void Emage::setEndTime(const int64 end_time)
{
    this->end_time = end_time;
}

void Emage::setLatUnit(const double lat_unit)
{
    this->lat_unit = lat_unit;
}

void Emage::setLongUnit(const double long_unit)
{
    this->long_unit = long_unit;
}

void Emage::setNeLat(const double ne_lat)
{
    this->ne_lat = ne_lat;
}

void Emage::setNeLong(const double ne_long)
{
    this->ne_long = ne_long;
}

void Emage::setNumCols(const int32 num_cols)
{
    this->num_cols = num_cols;
}

void Emage::setNumRows(const int32 num_rows)
{
    this->num_rows = num_rows;
}

void Emage::setStartTime(const int64 start_time)
{
    this->start_time = start_time;
}

void Emage::setSwLat(const double sw_lat)
{
    this->sw_lat = sw_lat;
}

void Emage::setSwLong(const double sw_long)
{
    this->sw_long = sw_long;
}

void Emage::setTheme(const string& theme)
{
    this->theme = string(theme);
}

