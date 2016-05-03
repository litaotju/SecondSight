package com.tju.secondsight.filters.convolution;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import com.tju.secondsight.filters.Filter;

public class StrokeEdgesFilter implements Filter {

	private final Mat mKernel = new MatOfInt(
		0, 0, 1, 0, 0,
		0, 1, 2, 1, 0,
		1, 2, -16, 2, 1,
		0, 1, 2, 1, 0,
		0, 0, 1, 0, 0
		);
	private Mat mEdges = new Mat();
	@Override
	public void apply(Mat src, Mat dst) {
		//先找出图像的边缘
		Imgproc.filter2D(src, mEdges, -1, mKernel);
		//将边缘反转成黑色
		Core.bitwise_not(mEdges, mEdges);
		//两个图像相乘
		Core.multiply(src, mEdges, dst, 1.0/255.0);
	}

}
