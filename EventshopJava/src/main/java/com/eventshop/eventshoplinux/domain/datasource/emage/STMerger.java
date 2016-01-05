package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.SpatialMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.TemporalMapper;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.EmageIterator;

public class STMerger implements Iterator<Emage>, Runnable {

	protected static Log log = LogFactory.getLog(STMerger.class);
	int resCnt = 0;
	int emageCnt = 0;

	String expr;

	// Final Resolution
	FrameParameters fp;

	ArrayList<EmageIterator> emageIters;
	ArrayList<SpatialMapper> spMappers;
	ArrayList<TemporalMapper> tempMappers;

	ArrayList<ResolutionMapper> resMappers;

	long curWindowEnd;
	ArrayList<Emage> emageList;
	LinkedBlockingQueue<Emage> queue;
	boolean isRunning;

	enum MergingOp {
		addEE, subEE, maxEE, minEE, mulED, divED
	};

	public STMerger(FrameParameters params) {
		emageIters = new ArrayList<EmageIterator>();
		spMappers = new ArrayList<SpatialMapper>();
		tempMappers = new ArrayList<TemporalMapper>();

		resMappers = new ArrayList<ResolutionMapper>();

		emageList = new ArrayList<Emage>();

		fp = params;
		queue = new LinkedBlockingQueue<Emage>();

		isRunning = true;
	}

	public void setFinalFrameParameters(FrameParameters params) {
		fp = params;
	}

	public void setMergingExpression(String expr) {
		this.expr = expr;

	}

	@Override
	public boolean hasNext() {
		return queue.iterator().hasNext();
	}

	@Override
	public Emage next() {
		try {
			emageList.clear();
			return queue.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public void remove() {
		queue.remove();
	}

	@Override
	public void run() {
		long now = System.currentTimeMillis();
		curWindowEnd = (long) Math.ceil(now / fp.timeWindow) * fp.timeWindow
				+ fp.syncAtMilSec;

		while (isRunning) {
			fp.start = curWindowEnd - fp.timeWindow;
			fp.end = curWindowEnd;

			while (now < curWindowEnd) {
				if (!isRunning)
					break;

				try {
					Thread.sleep(curWindowEnd - now);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				now = System.currentTimeMillis();
			}

			String outEmageID = EvaluateExpr(expr);
			Emage eOut = getEmageFromID(outEmageID);

			eOut.setStart(fp.start);
			eOut.setEnd(fp.end);
			queue.add(eOut);

			curWindowEnd = curWindowEnd + fp.timeWindow;
		}
	}

	public void stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	public String addIterator(EmageIterator Eiter, SpatialMapper sp,
			TemporalMapper tp) {
		String iterID = "R" + resCnt;
		Eiter.IteratorID = iterID;

		// Add the iterator and spatial and temporal mapper
		emageIters.add(Eiter);
		spMappers.add(sp);
		tempMappers.add(tp);

		// Add the reslution mapper
		ResolutionMapper rm = new ResolutionMapper(Eiter.params, fp,
				Eiter.theme);
		resMappers.add(rm);

		resCnt++;
		return iterID;
	}

	String addEmage2List(Emage e) {

		String eID = "E" + emageCnt;
		e.emageID = eID;
		emageList.add(e);
		emageCnt++;
		return eID;
	}

	Emage getEmageFromID(String eID) {
		for (int i = 0; i < emageList.size(); i++) {
			if (emageList.get(i).emageID.compareToIgnoreCase(eID) == 0)
				return emageList.get(i);
		}
		log.info("No emage found corresponspoding to ID: " + eID);
		return null;
	}

	String EvaluateExpr(String expr) {
		String LCType = "";
		String RCType = "";

		String opStr = expr.substring(0, expr.indexOf('('));
		MergingOp.valueOf(opStr);

		int branchSplitPoint = getCommaWNoPendingBracketsPos(expr.substring(
				expr.indexOf('(') + 1, expr.length() - 1));

		String p1 = "";
		String p2 = "";

		String LChild = expr.substring(expr.indexOf('(') + 1, expr.indexOf('(')
				+ branchSplitPoint);
		try {
			String LCopStr = LChild.substring(0, expr.indexOf('('));
			MergingOp.valueOf(LCopStr);
			LCType = "Expression";
		} catch (Exception e) {
			LCType = "NonExpression";
		}

		if (LCType.compareTo("Expression") == 0)
			p1 = EvaluateExpr(LChild);
		else
			p1 = LChild;

		String RChild = expr.substring(
				expr.indexOf('(') + branchSplitPoint + 1, expr.length() - 1);

		try {
			String RCopStr = RChild.substring(0, expr.indexOf('('));
			MergingOp.valueOf(RCopStr);
			RCType = "Expression";
		} catch (Exception e) {
			RCType = "NonExpression";
		}

		if (RCType.compareTo("Expression") == 0)
			p2 = EvaluateExpr(RChild);
		else
			p2 = RChild;

		return computeABlock(MergingOp.valueOf(opStr), p1, p2);
	}

	public int getCommaWNoPendingBracketsPos(String str) {
		int openCount = 0;
		int closeCount = 0;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ',' && openCount == closeCount) {
				return i + 1;
			}

			if (str.charAt(i) == '(') {
				openCount++;
			}
			if (str.charAt(i) == ')') {
				closeCount++;
			}
		}
		log.info("The string does not have a proper ','  separation into LHS and RHS for evaluation.");
		return -1;
	}

	public String computeABlock(MergingOp MO, String p1, String p2) {
		Emage e1;
		Emage e2;
		Emage e3 = null;
		double d;

		switch (MO) {
		case addEE: {
			e1 = getEmageByResID(p1);
			e2 = getEmageByResID(p2);
			e3 = addEE(e1, e2);
			break;
		}
		case subEE: {
			e1 = getEmageByResID(p1);
			e2 = getEmageByResID(p2);
			e3 = subEE(e1, e2);
			break;
		}
		case minEE: {
			e1 = getEmageByResID(p1);
			e2 = getEmageByResID(p2);
			e3 = minEE(e1, e2);
			break;
		}
		case maxEE: {
			e1 = getEmageByResID(p1);
			e2 = getEmageByResID(p2);
			e3 = maxEE(e1, e2);
			break;
		}
		case mulED: {
			e1 = getEmageByResID(p1);
			d = Double.parseDouble(p2);
			e3 = mulED(e1, d);
			break;
		}
		case divED: {
			e1 = getEmageByResID(p1);
			d = Double.parseDouble(p2);
			e3 = divED(e1, d);
			break;
		}
		default:
			log.info("Illegal parameter for ST MERGER");
		}

		String out = addEmage2List(e3);
		return out;
	}

	public int countOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}

	Emage getEmageByResID(String resID) {
		// If the resID is of the type EmageIterator.ID
		if (resID.indexOf('R') != -1) {
			int index = Integer.parseInt(resID.substring(1));

			ResolutionMapper RM = resMappers.get(index);
			Emage e = RM.doTheTranslation(emageIters.get(index),
					spMappers.get(index), tempMappers.get(index));

			return e;
		}

		// If the resID is of the type Emage.ID
		Emage e = getEmageFromID(resID);
		if (e == null)
			log.info("No Iterator or Emage found with the matching resID. returning NULL");

		return e;
	}

	Emage addEE(Emage e1, Emage e2) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		if (nRows != e2.image.length || nCols != e2.image[0].length) {
			log.info("The size of images must match for add operation");
			return null;
		}
		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				tempImage[i][j] = e1.image[i][j] + e2.image[i][j];
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	Emage subEE(Emage e1, Emage e2) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		if (nRows != e2.image.length || nCols != e2.image[0].length) {
			log.info("The size of images must match for SUBT operation");
			return null;
		}
		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				double value = e1.image[i][j] - e2.image[i][j];
				if (value >= 0)
					tempImage[i][j] = value;
				else
					tempImage[i][j] = 0;
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	Emage maxEE(Emage e1, Emage e2) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		if (nRows != e2.image.length || nCols != e2.image[0].length) {
			log.info("The size of images must match for MAX operation");
			return null;
		}
		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				tempImage[i][j] = e1.image[i][j] > e2.image[i][j] ? e1.image[i][j]
						: e2.image[i][j];
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	Emage minEE(Emage e1, Emage e2) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		if (nRows != e2.image.length || nCols != e2.image[0].length) {
			log.info("The size of images must match for MIN operation");
			return null;
		}
		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				tempImage[i][j] = e1.image[i][j] < e2.image[i][j] ? e1.image[i][j]
						: e2.image[i][j];
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	Emage mulED(Emage e1, double d) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				tempImage[i][j] = d * e1.image[i][j];
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	Emage divED(Emage e1, double d) {
		int nRows = e1.image.length;
		int nCols = e1.image[0].length;

		double[][] tempImage = new double[nRows][nCols];
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				tempImage[i][j] = e1.image[i][j] / d;
			}
		}
		Emage e = new Emage(e1.getParameters(), e1.theme);
		e.image = tempImage;
		return e;
	}

	public static void main(String args[]) {
		long mSecsOffset = 1000;
		double latUnit1 = 5;
		double longUnit1 = 5;
		double swLat1 = 30;
		double swLong1 = -100;
		double neLat1 = 40;
		double neLong1 = -90;
		long timeWindow1 = 1000 * 60 * 60 * 24 * 2;// the last 2 days
		FrameParameters fp1 = new FrameParameters(timeWindow1, mSecsOffset,
				latUnit1, longUnit1, swLat1, swLong1, neLat1, neLong1);

		STMerger STM = new STMerger(fp1);

		Emage tempEmage1 = new Emage(fp1, "Flu");
		double[][] testImage1 = { { 12, 10 }, { 1, 30 }, };
		tempEmage1.image = testImage1;
		String e1ID = STM.addEmage2List(tempEmage1);

		Emage tempEmage2 = new Emage(fp1, "Flu");
		double[][] testImage2 = { { 2, 20 }, { 3, 3 }, };
		tempEmage2.image = testImage2;
		String e2ID = STM.addEmage2List(tempEmage2);

		String expr = "subEE(addEE(" + e2ID + ",mulED(" + e1ID + "," + 2
				+ "))," + e2ID + ")";

		STM.setMergingExpression(expr);

		STM.run();

		log.info("Done");
	}
}
