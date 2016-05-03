package com.tju.secondsight.filters;

import org.opencv.core.Mat;

public class NoneFilter implements Filter {
	@Override
	public void apply(final Mat source, final Mat dest){
		return;
	}
	
}
