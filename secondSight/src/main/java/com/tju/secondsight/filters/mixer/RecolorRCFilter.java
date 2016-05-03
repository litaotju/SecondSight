package com.tju.secondsight.filters.mixer;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.tju.secondsight.filters.Filter;

public class RecolorRCFilter implements Filter {
	private final ArrayList<Mat> mChannels = new ArrayList<Mat>(4); 
	@Override
	public void apply(Mat source, Mat dst) {
		Core.split(source, mChannels);
		Mat g = this.mChannels.get(1);
		Mat b = this.mChannels.get(1);
		Core.addWeighted(g, 0.5, b, 0.5, 0, g);
		mChannels.set(2, g);
		Core.merge(mChannels, dst);
	}

}
