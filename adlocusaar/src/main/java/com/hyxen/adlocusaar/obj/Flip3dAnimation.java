package com.hyxen.adlocusaar.obj;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Flip3dAnimation  extends Animation
{
	private final float mFromXDegrees;
	private final float mToXDegrees;
	private final float mFromYDegrees;
	private final float mToYDegrees;
//	private final float mCenterX;
//	private final float mCenterY;
	private final View mView;
	private Camera mCamera;
	
	public Flip3dAnimation(View view, float fromXDegrees, float toXDegrees, float fromYDegrees, float toYDegrees)
	{
		mFromXDegrees = fromXDegrees;
		mToXDegrees = toXDegrees;
		mFromYDegrees = fromYDegrees;
		mToYDegrees = toYDegrees;
		mView = view;
//		mCenterX = centerX;
//		mCenterY = centerY;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight)
	{
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		final float fromXDegrees = mFromXDegrees;
		float xDegrees = fromXDegrees + ((mToXDegrees - fromXDegrees) * interpolatedTime);
		final float fromYDegrees = mFromYDegrees;
		float yDegrees = fromYDegrees + ((mToYDegrees - fromYDegrees) * interpolatedTime);
		
		final float centerX = mView.getWidth() / 2;
		final float centerY = mView.getHeight() / 2;
		final Camera camera = mCamera;

		final Matrix matrix = t.getMatrix();

		camera.save();

		camera.rotateY(xDegrees);
		camera.rotateX(yDegrees);

		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}

}
