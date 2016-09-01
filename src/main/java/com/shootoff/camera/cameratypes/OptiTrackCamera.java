package com.shootoff.camera.cameratypes;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shootoff.camera.CameraManager;
import com.shootoff.camera.CameraView;
import com.shootoff.camera.shotdetection.JavaShotDetector;
import com.shootoff.camera.shotdetection.NativeShotDetector;
import com.shootoff.camera.shotdetection.OptiTrackShotDetector;
import com.shootoff.camera.shotdetection.ShotDetector;
import com.shootoff.config.Configuration;

public class OptiTrackCamera extends Camera {
	private static final Logger logger = LoggerFactory.getLogger(OptiTrackCamera.class);
	
	private static boolean initialized = false;
	
	static {
		init();
		
		if (cameraAvailable())
		{
			Camera.registerCamera(new OptiTrackCamera());
		}
	}
	
	public OptiTrackCamera()
	{
		if (initialized)
			return;

		init();
	}
	
	public static void init()
	{
		if (initialized)
			return;
		
		try {
			File lib = new File(System.mapLibraryName("OptiTrackCamera"));
			System.load(lib.getAbsolutePath());
			initialize();
			initialized = true;
		} catch (UnsatisfiedLinkError exception)
		{
			initialized = false;
		}
		
		logger.warn("init");
	}
	
	public String getName()
	{
		return "OptiTrack";
	}
	
	private native static void initialize();
	
	public static boolean cameraAvailable()
	{
		if (!initialized)
			init();
		return cameraAvailableNative();
	}
	
	public native static boolean cameraAvailableNative();

	
	public native boolean open();
	
	public void setViewSize(final Dimension size) {
		// Not supported
		return;
	}
	
	public Dimension getViewSize()
	{
		int width = getViewWidth();
		int height = getViewHeight();
		return new Dimension(width, height);
	}

	private native int getViewWidth();
	private native int getViewHeight();
	
	public native boolean isOpen();

	public Mat translateCameraArrayToMat(byte[] imageBuffer)
	{
		Mat mat = new Mat(getViewHeight(), getViewWidth(), CvType.CV_8UC1);
		Mat dst = new Mat(getViewHeight(), getViewWidth(), CvType.CV_8UC3);
		
		mat.put(0,0, imageBuffer);
		Imgproc.cvtColor(mat, dst, Imgproc.COLOR_GRAY2BGR);
		return dst;
	}
	
	
	public Mat getMatFrame()
	{
		byte[] frame = getImageNative();
		Mat mat = translateCameraArrayToMat(frame);
		return mat;
	}
	
	private native byte[] getImageNative();
	
	public native boolean close();

	@Override
	public BufferedImage getBufferedImage() {
		return Camera.matToBufferedImage(getMatFrame());
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShotDetector getPreferredShotDetector(final CameraManager cameraManager, final Configuration config, final CameraView cameraView)
	{
		if (OptiTrackShotDetector.isSystemSupported())
			return new OptiTrackShotDetector(cameraManager, config, cameraView);
		else if (NativeShotDetector.isSystemSupported())
			return new NativeShotDetector(cameraManager, config, cameraView);
		else if (JavaShotDetector.isSystemSupported())
			return new JavaShotDetector(cameraManager, config, cameraView);
		else
			return null;
	}
	

	public static boolean initialized() {
		return initialized;
	}

}