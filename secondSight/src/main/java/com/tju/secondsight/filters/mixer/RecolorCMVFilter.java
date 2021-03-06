package com.tju.secondsight.filters.mixer;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.tju.secondsight.filters.Filter;

public class RecolorCMVFilter implements Filter {

	@Override
	public void apply(Mat source, Mat dst) {
		ArrayList<Mat> mChannels = new ArrayList<Mat>(4);
		Core.split(source, mChannels);
		Mat r = mChannels.get(0);
		Mat g = mChannels.get(1);
		Mat b = mChannels.get(2);
		Core.max(b, r, b);
		Core.max(b, g, b);
		Core.merge(mChannels, dst);
		
	}

}
