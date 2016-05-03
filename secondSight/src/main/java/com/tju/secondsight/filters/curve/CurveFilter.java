package com.tju.secondsight.filters.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

import com.tju.secondsight.filters.Filter;

public class CurveFilter implements Filter {
	private final Mat mLut =  new MatOfInt();
	
	public CurveFilter(
			final double[] vVarIn, final double [] vVarOut,
			final double[] rVarIn, final double [] rVarOut,
			final double[] gVarIn, final double [] gVarOut,
			final double[] bVarIn, final double [] bVarOut){
		//根据点，产生四个函数，对应四个通道
		UnivariateFunction vFunc = newFunc(vVarIn, vVarOut);
		UnivariateFunction rFunc = newFunc(rVarIn, rVarOut);
		UnivariateFunction gFunc = newFunc(gVarIn, gVarOut);
		UnivariateFunction bFunc = newFunc(bVarIn, bVarOut);
		//创建查找表
		mLut.create(256, 1, CvType.CV_8UC4);
		for(int i =0; i<=255; i++){
			final double v = vFunc.value(i);
			final double r = rFunc.value(v);
			final double g = gFunc.value(v);
			final double b = bFunc.value(v);
			mLut.put(i, 0, r, g, b, i); // alpha is unchanged
		}
	}
	
	@Override
	public void apply(Mat src, Mat dst) {
		Core.LUT(src, mLut, dst);
	}
	
	private UnivariateFunction newFunc(final double[] varIn, final double [] varOut){
		UnivariateInterpolator interpolator;
		if( varIn.length > 2){
			interpolator = new SplineInterpolator();
		}else{
			interpolator = new LinearInterpolator();
		}
		return interpolator.interpolate(varIn, varOut);
	}

}
