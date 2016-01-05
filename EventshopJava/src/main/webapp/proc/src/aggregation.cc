#include "op.h"

using namespace std;
using namespace cv;


bool Aggregate::has_next()
{
	unsigned size = eits.size();
	for(unsigned i = 0; i < size; ++i)
	{
		if(!eits[i]->has_next()) return false;
	}
	return true;
}


void Aggregate::normalizeEmage(Emage& e)
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
				outMat.at<double>(i, j) = normMin +((inMat.at<double>(i, j)-arrayMin)/(arrayMax-arrayMin))*(normMax - normMin);
			}
		}
	}
	e.setArray(outMat);
}


Emage Aggregate::next()
{
	Emage emage;
	Mat out;

	if(aop == AggSUM)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		unsigned size = eits.size();

		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();
			if(m.rows == 1 && m.cols == 1)
				out += m.at<double>(0, 0);
			else
				out += m;
		}

		unsigned numScalars=scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			out += scalarVec[i];
		}

		emage.setArray(out);
	}
	else if(aop == AggMAX)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		unsigned size = eits.size();
		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();
			if(m.rows == 1 && m.cols == 1)
				cv::max(out, m.at<double>(0, 0), out);
			else
				cv::max(out, m, out);
		}

		unsigned numScalars=scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			cv::max(out, scalarVec[i], out);
		}

		emage.setArray(out);
	}
	else if(aop == AggMIN)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		unsigned size = eits.size();
		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();
			if(m.rows == 1 && m.cols == 1)
				cv::min(out, m.at<double>(0, 0), out);
			else
				cv::min(out, m, out);
		}

		unsigned numScalars = scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			cv::min(out, scalarVec[i], out);
		}
		emage.setArray(out);
	}
	else if(aop == AggAVG)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		unsigned size = eits.size();
		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();
			if(m.rows == 1 && m.cols == 1)
				out += m.at<double>(0, 0);
			else
				out = out + m;
		}
		unsigned numScalars = scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			out += scalarVec[i];
		}

		out = out / (size+numScalars);
		emage.setArray(out);
	}
	else if(aop == AggMUL)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		unsigned size = eits.size();
		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();
			if(m.rows == 1 && m.cols == 1)
			{
				Mat m_array(out.rows, out.cols, CV_64F,  m.at<double>(0, 0));
				cv::multiply(out, m_array, out);
			}
			else
				cv::multiply(out, m, out);
		}
		unsigned numScalars = scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			cv::multiply(out, Mat(out.rows, out.cols, CV_64F, scalarVec[i]), out);
		}

		emage.setArray(out);
	}
	else if(aop == AggAND)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		// Check scalar first
		bool scalarTrue = true;
		unsigned numScalars = scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			// Can only be zero
			if (scalarVec[i] <= 0)
			{
				scalarTrue = false;
				out = Mat::zeros(out.rows, out.cols, CV_64F);
				break;
			}
		}

		if(scalarTrue)
		{
			unsigned size = eits.size();
			if(size == 1)
			{
				for(int h = 0; h < out.rows; h++)
					for(int w = 0; w < out.cols; w++)
					{
						if(out.at<double>(h, w) > 0) out.at<double>(h, w) = 1;
						else out.at<double>(h, w) = 0;
					}
			}

			for(unsigned i = 1; i < size; ++i)
			{
				Mat m = eits[i]->next().getArray();

				for(int h = 0; h < out.rows; h++)
				{
					for(int w = 0; w < out.cols; w++)
					{
						if (emage.getArray().at<double>(h,w)>0 && m.at<double>(h,w)>0)
						{
							out.at<double>(h,w)=1 ;
						}
						else
						{	out.at<double>(h,w)=0;
						}
					}
				}
			}
		}

		emage.setArray(out);
	}
	else if(aop == AggOR)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		// Check scalar first
		bool scalarTrue = false;
		unsigned numScalars = scalarVec.size();
		for(unsigned i = 0; i < numScalars; ++i)
		{
			// Can only be zero
			if (scalarVec[i] > 0)
			{
				scalarTrue = true;
				out = Mat(out.rows, out.cols, CV_64F, 1);
				break;
			}
		}

		if(!scalarTrue)
		{
			unsigned size = eits.size();

			if(size == 1)
			{
				for(int h = 0; h < out.rows; h++)
					for(int w = 0; w < out.cols; w++)
					{
						if(out.at<double>(h, w) > 0) out.at<double>(h, w) = 1;
						else out.at<double>(h, w) = 0;
					}
			}

			for(unsigned i = 1; i < size; ++i)
			{
				Mat m = eits[i]->next().getArray();

				for(int h = 0; h < out.rows; h++)
				{
					for(int w = 0; w < out.cols; w++)
					{
						if (emage.getArray().at<double>(h,w)>0 || m.at<double>(h,w)>0) out.at<double>(h,w)=1 ;
						else out.at<double>(h,w)=0;
					}
				}
			}
		}
		emage.setArray(out);
	}
	else if(aop == AggXOR)
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		// Check scalar first
		unsigned numScalars = scalarVec.size();
		bool scalarTrue;
		if(numScalars > 1)
			scalarTrue = scalarVec[0] > 0? true:false;
		for(unsigned i = 1; i < numScalars; ++i)
		{
			if(scalarVec[i] > 0 && scalarTrue) scalarTrue = false;
			else if(scalarVec[i] > 0 && !scalarTrue) scalarTrue = true;
			else if(scalarVec[i] <= 0 && scalarTrue) scalarTrue = true;
			else scalarTrue = false;
		}

		// XOR between emages
		unsigned size = eits.size();
		for(unsigned i = 1; i < size; ++i)
		{
			Mat m = eits[i]->next().getArray();

			for(int h = 0; h < out.rows; h++)
			{
				for(int w = 0; w < out.cols; w++)
				{
					out.at<double>(h,w)=1;
					if (emage.getArray().at<double>(h,w)>0 && m.at<double>(h,w)>0) out.at<double>(h,w)=0 ;
					if (emage.getArray().at<double>(h,w)<=0 && m.at<double>(h,w)<=0) out.at<double>(h,w)=0 ;
				}
			}
		}

		for(int h = 0; h < out.rows; h++)
			for(int w = 0; w < out.cols; w++)
			{
				if(out.at<double>(h, w) > 0 && scalarTrue) out.at<double>(h, w) = 0;
				else if(out.at<double>(h, w) > 0 && !scalarTrue) out.at<double>(h, w) = 1;
				else if(out.at<double>(h, w) <= 0 && scalarTrue) out.at<double>(h, w) = 1;
				else out.at<double>(h, w) = 0;
			}

		emage.setArray(out);
	}
	else if(aop == AggSUB)//can have only 2 inputs
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();
		if (scalar1stParam==false)
		{
			unsigned numScalars=scalarVec.size();
			if (numScalars>0)
			{
				out -= scalarVec[0];
			}
			else
			{
				Mat m = eits[1]->next().getArray();
				if(m.rows == 1 && m.cols == 1)
					out -= m.at<double>(0, 0);
				else
					out -= m;
			}
		}
		else//1st param is a number
		{
			unsigned numScalars = scalarVec.size();
			if (numScalars > 0)
			{
				out = scalarVec[0]-out;
			}
		}
		emage.setArray(out);
	}

	else if(aop == AggDIV)//can have only 2 inputs
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();
		if (scalar1stParam==false)
		{
			unsigned numScalars = scalarVec.size();
			if (numScalars>0)
			{
				out /= scalarVec[0];
			}
			else
			{
				Mat m = eits[1]->next().getArray();
				if(m.rows == 1 && m.cols == 1)
					out /= m.at<double>(0, 0);
				else
					out /= m;
			}
		}
		else
		{
			unsigned numScalars=scalarVec.size();
			if (numScalars>0)
			{
				out = scalarVec[0]/out;
			}

		}
		emage.setArray(out);
	}

	else if(aop == AggNOT)//can have only 1 input
	{
		emage = eits[0]->next();
		out = emage.getArray().clone();

		for(int h = 0; h < out.rows; ++h)
			for(int w = 0; w < out.cols; ++w)
			{
				if(out.at<double>(h, w) > 0) out.at<double>(h, w) = 0;
				else out.at<double>(h, w) = 1;
			}
		emage.setArray(out);
	}
	else if(aop == AggCOV)
	{
		emage = eits[0]->next();
		Mat src=emage.getArray();
		cout << "src: " << endl;
		for(int i = 0; i < src.rows; ++i)
		{
			for(int j = 0; j < src.cols; ++j)
				cout << src.at<double>(i, j) << " ";
			cout << endl;
		}
		Mat kernel;
		if (eits.size()==2){
			kernel=eits[1]->next().getArray();
			cout << "kernel: " << endl;
			for(int i = 0; i < kernel.rows; ++i)
			{
				for(int j = 0; j < kernel.cols; ++j)
					cout << kernel.at<double>(i, j) << " ";
				cout << endl;
			}
		}
		else
		{
			cout << "kernel of size: "<< scalarVec[0] << endl;
			kernel= Mat::zeros(scalarVec[0], scalarVec[0], CV_64F);
			for(int i = 0; i < kernel.rows; ++i)
			{
				for(int j = 0; j < kernel.cols; ++j)
				{
					kernel.at<double>(i,j)=1;
					cout << kernel.at<double>(i, j) << " ";
				}
				cout << endl;
			}
		}

		Mat convOut = Mat::zeros(src.rows, src.cols, CV_64F);
		cout << "convolution before: " << endl;
		for(int i = 0; i < convOut.rows; ++i)
		{
			for(int j = 0; j < convOut.cols; ++j)
				cout << convOut.at<double>(i, j) << " ";
			cout << endl;
		}

		filter2D(src, convOut , -1, kernel,Point(-1, -1));
		convOut=convOut/(kernel.rows * kernel.cols);
		cout << "convolution: " << endl;
		for(int i = 0; i < convOut.rows; ++i)
		{
			for(int j = 0; j < convOut.cols; ++j)
				{
				cout << convOut.at<double>(i, j) << " ";

				}cout << endl;
		}
		Mat abc = convOut.clone();


		cout<< "size of input image:"<< src.cols << " , " << src.rows << endl;
		cout<< "size of kernel:"<< kernel.cols << " , " << kernel.rows << endl;
		cout<< "size of convolved image:"<< convOut.cols << " , " << convOut.rows << endl;
		emage.setArray(abc);//.setArray(convOut);
		convOut.release();

	}

	//	show_matrix(emage.getArray(), "emage.jpeg");
	if(normValue)
		normalizeEmage(emage);

	return emage;
}


Aggregate::~Aggregate()
{
}


//int main()
//{
//	ProcEmageIterator *in1 = new EmageIngestor("C:\\Project\\emage1");
//	ProcEmageIterator *in2 = new EmageIngestor("C:\\Project\\emage2");
//
//	vector<ProcEmageIterator*> eits;
//	eits.push_back(in1);
//	eits.push_back(in2);
//
//	Aggregate agg(eits, AggCOV);
//
//	int count = 0;
//	while(true)
//	{
//		while(agg.has_next())
//		{
//			++count;
//			cout << "count: " << count << endl;
//			Emage e = agg.next();
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
