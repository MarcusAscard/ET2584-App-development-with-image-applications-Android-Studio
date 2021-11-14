package com.softa.imageenhancer;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class TestEnhancer implements ImageEnhancer {

	private static final int ACTION_1 = 1;
	private static final int ACTION_0 = 0;
	private int progress;

	public TestEnhancer() {

	}
	
	public Bitmap enhanceImageHSV(Bitmap theImage, int action) {

		// Set progress
		progress = 0;
		
		// Get the image pixels
		int height = theImage.getHeight();
		int width = theImage.getWidth();
		Log.d("DEBUG", "Image size is " + width + "px by " + height + "px." );
		int[] pixels = new int[height * width];
		theImage.getPixels(pixels, 0, width,0,0, width, height);
		
		progress = 5;

		Log.d("DEBUG", "pixels length = " + pixels.length);
		
		//Convert pixels to brightness values;
		float[][] hsvPixels = convertToHSV(pixels);
		
		progress = 40;
		
		Log.d("DEBUG", "hsvPixels length = " + hsvPixels.length);

		// Here below some manipulations of the image is made as examples.
		// This should be changed to your image enhancement algorithms.
 		 if(action == ACTION_0){ // Normal histogram equalization
			float[] im_hist = imhist_calc(hsvPixels); // Get histogram of V-component
			float[] pdf = pdf_calc(im_hist, height, width); // Calculate probability density function
			float[] cdf = cdf_calc(pdf); // Calculate cumulative distribution function
			float[] cdf_transform = cdf_transform_calc(cdf); // Transform values
			float[] histeq_v = histeq_v_calc(cdf_transform, getV_comp(hsvPixels)); // Equalized V-component

			for (int i = 0; i < hsvPixels.length; i++) { // replace V-component in image with the equalized version
				hsvPixels[i][2] = histeq_v[i];
				pixels[i] = Color.HSVToColor(hsvPixels[i]);
			}
		}
		else if(action == ACTION_1){ // Histogram equalization on greyscale image
			float[] im_hist = imhist_calc(hsvPixels);
			float[] pdf = pdf_calc(im_hist, height, width);
			float[] cdf = cdf_calc(pdf);
			float[] cdf_transform = cdf_transform_calc(cdf);
			float[] histeq_v = histeq_v_calc(cdf_transform, getV_comp(hsvPixels));

			for (int i = 0; i < hsvPixels.length; i++) {
				hsvPixels[i][0] = 0;
				hsvPixels[i][1] = 0;
				hsvPixels[i][2] = histeq_v[i];
				pixels[i] = Color.HSVToColor(hsvPixels[i]);
			}
		}

		progress = 80;
		Log.d("DEBUG","creating BITMAP,width x height "+width+" "+height);
        Bitmap modifiedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		modifiedImage.setPixels(pixels, 0, width, 0, 0, width, height);

		progress = 100;
		return modifiedImage;
	}

	private float[] getV_comp(float[][] hsvPixels){
		float[] v_comp = new float[hsvPixels.length];

			for(int i = 0; i < hsvPixels.length; i++){
				v_comp[i] = hsvPixels[i][2]*256; // Extract and scale V-component from [0,1] to [0,256]
		}
		return v_comp;
	}

	private float[] imhist_calc(float[][] hsvPixels){
		float[] im_hist = new float[256];
		float[] v_comp = getV_comp(hsvPixels);

			for(int i = 0; i < v_comp.length; i++){
				int hist_val = (int) v_comp[i];
				if(hist_val == 0) hist_val = 1;
				im_hist[hist_val-1]++;
			}
		return im_hist;
	}

	private float[] pdf_calc(float[] imhist, int height, int width){
		float[] pdf = new float[imhist.length];

		for(int i = 0; i < imhist.length; i++){
			pdf[i] = imhist[i]/(height*width); // Calculate probability density function
		}
		return pdf;
	}

	private float[] cdf_calc(float[] pdf){
		float[] cdf = new float[pdf.length];
		float cumsum = 0;

		for(int i = 0; i < cdf.length; i++){
			cumsum += pdf[i]; // Save current value which is to be added to the next iteration's CDF-value
			cdf[i] = cumsum; // Calculate cumulative distribution function
		}
		return cdf;
	}

	private float[] cdf_transform_calc(float[] cdf){
		float[] cdf_transform = new float[cdf.length];

		for(int i = 0; i < cdf_transform.length; i++){
			cdf_transform[i] = (float) Math.floor(cdf[i]*255);
		}
		return cdf_transform;
	}

	private float[] histeq_v_calc(float[] cdf_transform, float[] v_comp){
		float[] histeq_v = new float[v_comp.length];

		for(int i = 0; i < v_comp.length; i++){
			if(v_comp[i] == 0) v_comp[i] = 1; // To avoid that index in cdf_transform[index] isn't -1
				histeq_v[i] = cdf_transform[(int) v_comp[i]-1];
		}

		for(int i = 0; i < histeq_v.length; i++){
			histeq_v[i] = histeq_v[i]/256; // Convert back to [0,1]
		}
		return histeq_v;
	}


	private float[][] convertToHSV(int[] pixels) {
		float[][] hsvPixels = new float[pixels.length][3];
		for (int i = 0; i < pixels.length; i++) {
			Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsvPixels[i]);
			
		}
		return hsvPixels;
	}

	public int getProgress() {
		// Log.d("DEBUG", "Progress: "+progress);
		return progress;
	}

	@Override
	public Bitmap enhanceImage(Bitmap bitmap, int configuration) {
		switch (configuration) {
		case ACTION_0:
			return enhanceImageHSV(bitmap, 0);
		case ACTION_1:
			return enhanceImageHSV(bitmap, 1);
		default:
			return enhanceImageHSV(bitmap, 0);
		}
	}

	@Override
	public String[] getConfigurationOptions() {
		return new String[]{ "Magic", "Magic in Grey"};
	}

}
