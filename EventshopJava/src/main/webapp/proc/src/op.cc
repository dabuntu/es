#include "op.h"
#include "highgui.h"
//#include "jsoncpp/json/json.h"

//#include <windows.h>
#include <fstream>

#include <sys/stat.h>
#include <time.h>

using namespace cv;
using namespace std;

bool EmageIngestor::has_next()
{
	// if next is already available, return true
	if(is_next_available) return true;

	// 08/19/2011 Mingyan
	// check modified time of the file
	struct stat attrib;
	stat(filename.c_str(), &attrib);
	long new_time = attrib.st_mtime;

	if(new_time <= last_modified_time)
		return false;
	else
	{
		last_modified_time = new_time;
		file = NULL;
		file = fopen(filename.c_str(), "rb");
		if(file == NULL)
			cerr << "File " << filename << " fails to open!" << endl;
	}

	// if not, check the current situation
	size = 0;
	int re = fread(&size, 4, 1, file);
	if(re <= 0 || size == 0) return false;

	// set next available to be true
	is_next_available = true;
	return true;
}


Emage EmageIngestor::next()
{
	// check the next available to be false
	is_next_available = false;

	// Parse the data
	char *buf = new char[size]();
	int re = fread(buf, size, 1, file);
	if(re <= 0)
	{
		cerr << "Failed to read the data of file " << filename << endl;
		if(ferror(file))
			cout << "errors in file " << filename << endl;
		else if(feof(file))
			cout << "eof is reached in " << filename << endl;
	}

	// Store the parsed message into msg
	if(!msg.ParseFromArray(buf, size))
		cerr << "Failed to parse the data of file " << filename << endl;

	Emage emage;
	// Fill in fields
	if(msg.has_theme())
	{
		emage.setTheme(msg.theme());
#ifdef DEBUG
		cout << "Theme: " << msg.theme() << endl;
#endif
	}

	if(msg.has_start_time())
	{
		emage.setStartTime(msg.start_time());
#ifdef DEBUG
		cout << "Start Time: " << msg.start_time() << endl;
#endif
	}

	if(msg.has_end_time())
	{
		emage.setEndTime(msg.end_time());
#ifdef DEBUG
		cout << "End Time: " << msg.end_time() << endl;
#endif
	}

	if(msg.has_lat_unit())
	{
		emage.setLatUnit(msg.lat_unit());
#ifdef DEBUG
		cout << "Lat Unit: " << msg.lat_unit() << endl;
#endif
	}

	if(msg.has_long_unit())
	{
		emage.setLongUnit(msg.long_unit());
#ifdef DEBUG
		cout << "Long Unit: " << msg.long_unit() << endl;
#endif
	}

	if(msg.has_sw_lat())
	{
		emage.setSwLat(msg.sw_lat());
#ifdef DEBUG
		cout << "SW Lat: " << msg.sw_lat() << endl;
#endif
	}

	if(msg.has_sw_long())
	{
		emage.setSwLong(msg.sw_long());
#ifdef DEBUG
		cout << "SW Long: " << msg.sw_long() << endl;
#endif
	}

	if(msg.has_ne_lat())
	{
		emage.setNeLat(msg.ne_lat());
#ifdef DEBUG
		cout << "NE Lat: " << msg.ne_lat() << endl;
#endif
	}

	if(msg.has_ne_long())
	{
		emage.setNeLong(msg.ne_long());
#ifdef DEBUG
		cout << "NE Long: " << msg.ne_long() << endl;
#endif
	}

	if(msg.has_num_rows())
	{
		emage.setNumRows(msg.num_rows());
#ifdef DEBUG
		cout << "Num Of Rows: " << msg.num_rows() << endl;
#endif
	}

	if(msg.has_num_cols())
	{
		emage.setNumCols(msg.num_cols());
#ifdef DEBUG
		cout << "Num Of Cols: " << msg.num_cols() << endl;
#endif
	}

	// Generate the array
	int32 rows = msg.num_rows();
	int32 cols = msg.num_cols();
	Mat array = Mat::zeros(rows, cols, CV_64F);
	if(msg.cell_size() > 0)
	{
		for(int32 i = 0; i < rows; ++i)
		{
			for(int32 j = 0; j < cols; ++j)
			{
				array.at<double>(i, j) = msg.cell(i*cols+j);
			}
		}
	}
	emage.setArray(array);
	//show_matrix(array, "inputs.jpeg");

	// 08/19/2011 Mingyan
	fclose(file);

	delete[] buf;
	return emage;
}


EmageIngestor::~EmageIngestor()
{

}


void show_matrix(const Mat& in, const string& filename)
{

	if(in.type() == CV_64F || in.type() == CV_8U)
		imwrite(filename, in);
	else if(in.type() == DataType<int>::type)
	{
		Mat out(in.rows, in.cols, CV_8UC3);
		for(int i = 0; i < in.rows; ++i)
			for(int j = 0; j < in.cols; ++j)
			{
				unsigned char r = (in.at<int>(i, j) >> 16) & 0xff;
				unsigned char g = (in.at<int>(i, j) >> 8) & 0xff;
				unsigned char b = in.at<int>(i, j) & 0xff;
				out.at<Vec3b>(i, j)[0] = b;
				out.at<Vec3b>(i, j)[1] = g;
				out.at<Vec3b>(i, j)[2] = r;
				out.at<Vec3b>(i, j)[3] = 0x99;
			}
		imwrite(filename, out);
	}

	//	IplImage *img = cvLoadImage(filename.c_str());
	//	cvNamedWindow(filename.c_str(), 1);
	//	cvShowImage(filename.c_str(), img);
	//	cvWaitKey();
	//	cvDestroyWindow(filename.c_str());
	//	cvReleaseImage(&img);
}

bool isnan_int (int i) {
	return (i != i);
}

bool isnan_double (double d) {
	return (d != d);
}

bool isnan_uchar (uchar d) {
	return (d != d);
}

/*

void create_geojson(Emage& e, const double& minVal, const double& maxVal, const string& filepath){
	// creating .json in geoJson format to display on the UI
	// by: Siripen
	string geojsonfile = filepath + ".json";
	ofstream out(geojsonfile.c_str());

	long starttime = e.getStartTime()/1000;
	long endtime = e.getEndTime()/1000;
	string start = asctime(localtime(&starttime));
	start = start.substr(0, start.length()- 1);
	string end = asctime(localtime(&endtime));
	end = end.substr(0, end.length() - 1);

	out << "{\"startTime\":" << starttime << ", "
			<< "\"endTime\":" << endtime << ", "
			<< "\"ts_start\":\"" << start << "\", "
			<< "\"ts_end\":\"" << end << "\", "
			<< "\"latUnit\":" << e.getLatUnit() << ", "
			<< "\"longUnit\":" << e.getLongUnit() << ", "
			<< "\"swLat\":" << e.getSwLat() << ", "
			<< "\"swLong\":" << e.getSwLong() << ", "
			<< "\"neLat\":" << e.getNeLat() << ", "
			<< "\"neLong\":" << e.getNeLong() << ", "
			<< "\"row\":" << e.getNumRows() << ", "
			<< "\"col\":" << e.getNumCols() << ", "
			<< "\"min\":" << minVal << ", "
			<< "\"max\":" << maxVal << ", "
			<< "\"sttBoxGeoJson\":{\"type\":\"FeatureCollection\",\"features\":[";

	double swLat = e.getSwLat();
	double swLon = e.getSwLong();
	double latUnit = e.getLatUnit();
	double lonUnit = e.getLongUnit();
	Mat img = e.getArray();
	double stelSwLat, stelSwLon, stelNeLat, stelNeLon;
	string theme = e.getTheme();

	for(int i = 0; i < img.rows; i++){
		for(int j = 0; j < img.cols; j++){
			stelSwLat = swLat + (i*latUnit);
			stelSwLon = swLon + (j*lonUnit);
			stelNeLat = stelSwLat + latUnit;
			stelNeLon = stelSwLon + lonUnit;
			if(img.type() == DataType<int>::type)
			{
				if(img.at<int>(i,j) != 0 && !isnan_int(img.at<int>(i,j))){
					out << "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
						<< "[[[" << stelSwLon << ", " << stelSwLat << "],["
						<< stelSwLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelSwLat << "],["
						<< stelSwLon << "," << stelSwLat << "]]]},\"properties\":{\"theme\":\""
						<< theme << "\",\"value\":" << img.at<int>(i,j) << "},\"type\":\"Feature\"},";
				}
			}

			if(img.type()  == CV_64F)
			{
				if(img.at<double>(i,j) != 0.0 && !isnan_double(img.at<double>(i,j))){
					out << "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
						<< "[[[" << stelSwLon << ", " << stelSwLat << "],["
						<< stelSwLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelSwLat << "],["
						<< stelSwLon << "," << stelSwLat << "]]]},\"properties\":{\"theme\":\""
						<< theme << "\",\"value\":" << img.at<double>(i,j) << "},\"type\":\"Feature\"},";
				}
			}

			if(img.type() == CV_8U)
			{
				if(img.at<uchar>(i,j) != 0.0 && !isnan_uchar(img.at<uchar>(i,j))){
					out << "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
						<< "[[[" << stelSwLon << ", " << stelSwLat << "],["
						<< stelSwLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelNeLat << "],["
						<< stelNeLon << "," << stelSwLat << "],["
						<< stelSwLon << "," << stelSwLat << "]]]},\"properties\":{\"theme\":\""
						<< theme << "\",\"value\":" << img.at<uchar>(i,j) << "},\"type\":\"Feature\"},";
				}
			}
		}
	}
	out << "{}]}}" << endl;
	out.close();
}

*/

/*
void create_json(Emage& e, const string& filepath){
	// creating .json in geoJson format to display on the UI
	// by: Siripen

	string geojsonfile = filepath + ".json";
	ofstream out(geojsonfile.c_str());
	Json::Value imageArr(Json::arrayValue);
	double swLat = e.getSwLat();
	double swLon = e.getSwLat();
	double latUnit = e.getLatUnit();
	double lonUnit = e.getLongUnit();
	Mat img = e.getArray();



	//{"ts_start":"Fri Aug 15 18:10:00 2014", "ts_end":"Fri Aug 15 18:15:00 2014", "latitude":"24", "longitude":"-125", "minVal":"0", "maxVal":"100", "value":"0","map_enabled":"true"}

	for(int i = 0; i < img.rows; i++){
		for(int j = 0; j < img.cols; j++){
			Json::Value stel;
			if(img.type() == DataType<int>::type)
			{
				if(img.at<int>(i,j) != 0){
					stel["latitude"] = swLat + (i*latUnit);
					stel["longitude"] = swLon + (j*lonUnit);
					stel["result1"] = img.at<int>(i,j);
					imageArr.append(stel);
				}
			}

			if(img.type()  == CV_64F)
			{
				if(img.at<double>(i,j) != 0.0){
					stel["latitude"] = swLat + (i*latUnit);
					stel["longitude"] = swLon + (j*lonUnit);
					stel["result1"] = img.at<double>(i,j);
					imageArr.append(stel);
				}
			}

			if(img.type() == CV_8U)
			{
				if(img.at<uchar>(i,j) != 0.0){
					stel["latitude"] = swLat + (i*latUnit);
					stel["longitude"] = swLon + (j*lonUnit);
					stel["result1"] = img.at<uchar>(i,j);
					imageArr.append(stel);
				}
			}
		}
	}
	out << imageArr << endl;
	out.close();
	std::cout << img.rows << img.cols << imageArr << std::endl;

}

*/

void create_output(Emage& e, const string& filepath)
{
	Emage projE;
	bool singleValue = false;
	double minVal;
	double maxVal;
	if(e.getNumRows() > 1 || e.getNumCols() > 1)
	{
		show_matrix(e.getArray(), filepath+"_before.png");
		projE = createProjectedOverlay(e, minVal, maxVal);
		show_matrix(projE.getArray(), filepath+".png");
	}
	else
	{
		projE = e;
		singleValue = true;
	}

	string jsonfile = filepath + ".json";
	ofstream out(jsonfile.c_str());

	long starttime = projE.getStartTime()/1000;
	long endtime = projE.getEndTime() / 1000;
	string start = asctime(localtime(&starttime));
	start = start.substr(0, start.length()- 1);
	string end = asctime(localtime(&endtime));
	end = end.substr(0, end.length() - 1);

	out << "{\"theme\":\"" << projE.getTheme() << "\", "
			<< "\"startTime\":\"" << starttime << "\", "
			<< "\"endTime\":\"" << endtime << "\", "
			<< "\"startTimeStr\":\"" << end << "\", "
			<< "\"endTimeStr\":\"" << end << "\", "
			<< "\"min\":\"" << minVal << "\", "
			<< "\"max\":\"" << maxVal << "\", "
						// additional output
			<< "\"latUnit\":" << projE.getLatUnit() << ", "
			<< "\"longUnit\":" << projE.getLongUnit() << ", "
			<< "\"swLat\":" << projE.getSwLat() << ", "
			<< "\"swLong\":" << projE.getSwLong() << ", "
			<< "\"neLat\":" << projE.getNeLat() << ", "
			<< "\"neLong\":" << projE.getNeLong() << ", "
			<< "\"row\":" << e.getNumRows() << ", "			// for row, col, and image array we use original emage not projected emage
			<< "\"col\":" << e.getNumCols() << ", "
			//<< "\"value_array\":\"" << projE.getArray() << "\","
			// end additional output
			<< "\"value\":\"" << projE.getArray().at<double>(0,0) << "\",";

	std::ostringstream stream;

	for (int i = 0; i < e.getArray().rows; ++i){
		for(int j = 0; j < e.getArray().cols; ++j){
			stream << e.getArray().at<double>(i,j) << ',';
		}
	}
	out << "\"image\":[" << stream.str().substr(0, stream.str().length() -1) << "],";
	if(singleValue)
		out << "\"mapEnabled\":\"false\"}" << endl;
	else
		out << "\"mapEnabled\":\"true\"}" << endl;

	out.close();
	/*
	for (int i = 0; i < e.getArray().rows; ++i)
		for(int j = 0; j < e.getArray().cols; ++j){
			if(!isnan_double(e.getArray().at<double>(i,j)))
					cout << i << ", " << j << " " << e.getArray().at<double>(i,j) << endl;
		}
	*/
	// note:
	// the query name in the filepath is in this following format: Q + qID + _1
	// for json output, we want filepath in Q + qId only (no _1)
	//create_json(projE, filepath.substr(0, filepath.length() - 2));
	//create_geojson(e, minVal, maxVal, filepath.substr(0, filepath.length() - 2));
}

void create_output(Emage& e, const string& filepath, const string& colors)
{
	Emage projE;
	bool singleValue = false;
	double minVal;
	double maxVal;
	if(e.getNumRows() > 1 || e.getNumCols() > 1)
	{
		show_matrix(e.getArray(), filepath+"_before.png");
		projE = createProjectedOverlay(e, minVal, maxVal);
		show_matrix(projE.getArray(), filepath+".png");
	}
	else
	{
		projE = e;
		singleValue = true;
	}

	string jsonfile = filepath + ".json";
	ofstream out(jsonfile.c_str());

	long starttime = projE.getStartTime()/1000;
	long endtime = projE.getEndTime() / 1000;
	string start = asctime(localtime(&starttime));
	start = start.substr(0, start.length()- 1);
	string end = asctime(localtime(&endtime));
	end = end.substr(0, end.length() - 1);

	out << "{\"theme\":\"" << projE.getTheme() << "\", "
			<< "\"startTime\":\"" << starttime << "\", "
			<< "\"endTime\":\"" << endtime << "\", "
			<< "\"startTimeStr\":\"" << end << "\", "
			<< "\"endTimeStr\":\"" << end << "\", "
			<< "\"min\":\"" << minVal << "\", "
			<< "\"max\":\"" << maxVal << "\", "
						// additional output
			<< "\"latUnit\":" << projE.getLatUnit() << ", "
			<< "\"longUnit\":" << projE.getLongUnit() << ", "
			<< "\"swLat\":" << projE.getSwLat() << ", "
			<< "\"swLong\":" << projE.getSwLong() << ", "
			<< "\"neLat\":" << projE.getNeLat() << ", "
			<< "\"neLong\":" << projE.getNeLong() << ", "
			<< "\"row\":" << e.getNumRows() << ", "			// for row, col, and image array we use original emage not projected emage
			<< "\"col\":" << e.getNumCols() << ", "
			//<< "\"value_array\":\"" << projE.getArray() << "\","
			// end additional output
			<< "\"value\":\"" << projE.getArray().at<double>(0,0) << "\","
			<< "\"colors\":[" << colors << "],";

	std::ostringstream stream;

	for (int i = 0; i < e.getArray().rows; ++i){
		for(int j = 0; j < e.getArray().cols; ++j){
			stream << e.getArray().at<double>(i,j) << ',';
		}
	}
	out << "\"image\":[" << stream.str().substr(0, stream.str().length() -1) << "],";
	if(singleValue)
		out << "\"mapEnabled\":\"false\"}" << endl;
	else
		out << "\"mapEnabled\":\"true\"}" << endl;

	out.close();
	/*
	for (int i = 0; i < e.getArray().rows; ++i)
		for(int j = 0; j < e.getArray().cols; ++j){
			if(!isnan_double(e.getArray().at<double>(i,j)))
					cout << i << ", " << j << " " << e.getArray().at<double>(i,j) << endl;
		}
	*/
	// note:
	// the query name in the filepath is in this following format: Q + qID + _1
	// for json output, we want filepath in Q + qId only (no _1)
	//create_json(projE, filepath.substr(0, filepath.length() - 2));
	//create_geojson(e, minVal, maxVal, filepath.substr(0, filepath.length() - 2));
}

void invert_matrix(Emage & e)
{
	Emage outputEmage(e);
	Mat inMat = e.getArray();

	Mat outMat(inMat.rows, inMat.cols, inMat.type());
	for(int i = 0; i < inMat.rows; ++i)
		for(int j = 0; j < inMat.cols; ++j)
		{
			if(inMat.type() == DataType<int>::type)
			{
				outMat.at<int>(i,j)=inMat.at<int>((inMat.rows-1)-i, j);
			}

			if(inMat.type()  == CV_64F )
			{
				outMat.at<double>(i,j)=inMat.at<double>((inMat.rows-1)-i, j);
			}

			if(inMat.type() == CV_8U)
			{
				outMat.at<uchar>(i,j)=inMat.at<uchar>((inMat.rows-1)-i, j);
			}
		}
	e.setArray(outMat);
}


void cutoff_matrix(Emage & e)
{
	Emage outputEmage(e);
	Mat inMat = e.getArray();

	Mat outMat(inMat.rows, inMat.cols, inMat.type());
	for(int i = 0; i < inMat.rows; ++i)
		for(int j = 0; j < inMat.cols; ++j)
		{
			if(inMat.type() == DataType<int>::type)
			{
				if (inMat.at<int>(i, j)>0 &&  inMat.at<int>(i, j)<255)
					outMat.at<int>(i,j)=inMat.at<int>(i, j);
				else if(inMat.at<int>(i, j)>255)
					outMat.at<int>(i,j)=255;
				else if (inMat.at<int>(i, j)< 0 )
					outMat.at<int>(i,j)=0;
			}

			if(inMat.type()  == CV_64F )
			{
				if (inMat.at<double>(i, j)>0 &&  inMat.at<double>(i, j)<255)
					outMat.at<double>(i,j)=inMat.at<double>(i, j);
				else if(inMat.at<double>(i, j)>255)
					outMat.at<double>(i,j)=255;
				else if (inMat.at<double>(i, j)< 0 )
					outMat.at<double>(i,j)=0;
			}

			if(inMat.type() == CV_8U)
			{
				if (inMat.at<uchar>(i, j)>0 &&  inMat.at<uchar>(i, j)<255)
					outMat.at<uchar>(i,j)=inMat.at<uchar>(i, j);
				else if(inMat.at<uchar>(i, j)>255)
					outMat.at<uchar>(i,j)=255;
				else if (inMat.at<uchar>(i, j)< 0 )
					outMat.at<uchar>(i,j)=0;
			}
		}
	e.setArray(outMat);
}


Emage createProjectedOverlay (Emage& e, double & minVal, double & maxVal)
{
	double swLat = e.getSwLat();
	double swLong = e.getSwLong();
	double neLat = e.getNeLat();
	double neLong = e.getNeLong();
	double DEGREES_PER_RADIAN = 57.2957795;
	double sizeStretchRatio = 1000;
	double incomingLatUnit = e.getLatUnit();
	double incomingLongUnit = e.getLongUnit();

	Point rectifiedPoint;
	double Ymax = GudermannianInv(neLat);
	double Ymin = GudermannianInv(swLat);
	int nRows = (int)floor((Ymax-Ymin)*sizeStretchRatio);
	int nCols = abs((int)floor(sizeStretchRatio*((swLong-neLong)/DEGREES_PER_RADIAN)));

	Emage outputEmage(e);
	outputEmage.setNumCols(nCols);
	outputEmage.setNumRows(nRows);

	// Get the image dimensions of the unrectified image
	int width = e.getNumCols();
	int height = e.getNumRows();

	Mat inMat = e.getArray();
	Mat outMat(nRows, nCols, inMat.type());

	maxVal = -999999999;
	minVal =  999999999;

	for (int i=0; i<nRows; i++)
	{
		for (int j=0; j<nCols; j++)
		{
			double corrLa = Gudermannian(Ymax- i/sizeStretchRatio);//
			int corrLat = (int)floor((neLat-corrLa)/incomingLatUnit);

			// int curLat=neLat-j/sizeStretchRatio;
			double curLong = swLong+j/sizeStretchRatio*DEGREES_PER_RADIAN;
			int corrLong =(int)floor((curLong-swLong)/incomingLongUnit);

			if ( corrLat>=0 && corrLat<height && corrLong>=0 && corrLong<width)
			{
				if(inMat.type() == DataType<int>::type)
				{
					outMat.at<int>(i,j) = inMat.at<int>(corrLat, corrLong);
				}

				if(inMat.type()  == CV_64F )
				{
					outMat.at<double>(i,j) = inMat.at<double>(corrLat, corrLong);
					if (outMat.at<double>(i,j) > maxVal) maxVal=outMat.at<double>(i,j);
					if (outMat.at<double>(i,j) < minVal) minVal=outMat.at<double>(i,j);
				}

				if(inMat.type() == CV_8U)
				{
					outMat.at<uchar>(i,j) = inMat.at<uchar>(corrLat, corrLong);

					if (outMat.at<uchar>(i,j) > maxVal) maxVal=outMat.at<uchar>(i,j);
					if (outMat.at<uchar>(i,j) < minVal) minVal=outMat.at<uchar>(i,j);
				}
			}
			else
			{
				if(inMat.type() == DataType<int>::type)
				{
					outMat.at<int>(i,j) = inMat.at<int>(0, 0);
				}
				if(inMat.type()  == CV_64F )
				{
					outMat.at<double>(i,j)=0;

					if (outMat.at<double>(i,j) > maxVal) maxVal = outMat.at<double>(i,j);
					if (outMat.at<double>(i,j) < minVal) minVal = outMat.at<double>(i,j);
				}

				if(inMat.type() == CV_8U)
				{
					outMat.at<uchar>(i,j)=0;
					if (outMat.at<uchar>(i,j) > maxVal) maxVal = outMat.at<uchar>(i,j);
					if (outMat.at<uchar>(i,j) < minVal) minVal = outMat.at<uchar>(i,j);
				}
			}
		}
	}

	//normalizing the vals to show between 0 to 255
	if(maxVal - minVal > 0)
	{
		for (int i=0; i<nRows; i++)
		{
			for (int j=0; j<nCols; j++)
			{
				if(outMat.type()  == CV_64F )
				{
					outMat.at<double>(i,j) =(outMat.at<double>(i,j) - minVal)*255/(maxVal-minVal);
				}

				if(outMat.type() == CV_8U)
				{
					outMat.at<uchar>(i,j) =(outMat.at<uchar>(i,j) - minVal)*255/(maxVal-minVal);
				}
			}
		}
	}

	outputEmage.setArray(outMat);
	return outputEmage;
}


/// <summary>
/// Calculates the Y-value (inverse Gudermannian function) for a latitude.
/// <para><see cref="http://en.wikipedia.org/wiki/Gudermannian_function"/></para>
/// </summary>
/// <param name="latitude">The latitude in degrees to use for calculating the Y-value.</param>
/// <returns>The Y-value for the given latitude.</returns>
double GudermannianInv(double latitude)
{
	double RADIANS_PER_DEGREE=1/57.2957795;
	double sign =+1;
	if (latitude<0) sign=-1;
	double sinv = sin(latitude * RADIANS_PER_DEGREE * sign);
	return sign * (log((1.0 + sinv) / (1.0 - sinv)) / 2.0);
}


/// <summary>
/// Returns the Latitude in degrees for a given Y.
/// </summary>
/// <param name="y">Y is in the range of +PI to -PI.</param>
/// <returns>Latitude in degrees.</returns>
double Gudermannian(double y)
{
	double DEGREES_PER_RADIAN=57.2957795;
	return atan(sinh(y)) * DEGREES_PER_RADIAN;
}


//int32 main()
//{
//	EmageIngestor *ingestor = new EmageIngestor("C:\\Programs\\emage1");
//
//	int32 count = 0;
//	while(true)
//	{
//		while(ingestor->has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//			Emage e = ingestor->next();
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
