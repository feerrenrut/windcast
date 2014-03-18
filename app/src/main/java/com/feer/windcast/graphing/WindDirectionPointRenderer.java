package com.feer.windcast.graphing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;


public class WindDirectionPointRenderer<FormatterType extends LineAndPointFormatter> extends LineAndPointRenderer<FormatterType> {

    private Bitmap mWindArrow = null;
    private ArrayList<Float> mWindDirections;

    public WindDirectionPointRenderer(XYPlot plot, Bitmap windArrow) {
        super(plot);
        mWindArrow = windArrow;
    }

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException
    {
        super.onRender(canvas, plotArea);

        if(mWindDirections != null)
        {
            for(XYSeries series : getPlot().getSeriesListForRenderer(this.getClass())) {
                drawSeries(canvas, plotArea, series, getFormatter(series));
            }
        }
    }

    private void drawSeries(Canvas canvas, RectF plotArea, XYSeries series, LineAndPointFormatter formatter) throws PlotRenderException
    {
        PointF p;
        XYPlot plot = getPlot();
        int size = series.size();

        final float arrowImageWidth = mWindArrow.getScaledWidth(canvas);
        final float arrowImageHeight = mWindArrow.getScaledHeight(canvas);

        for (int i = 0; i < size; i++)
        {
            Number y = series.getY(i);
            Number x = series.getX(i);
            Float directionForPoint = mWindDirections.get(i);

            //TODO Some times there is NO direction for wind.. (ie if the speed is very low. We should use a different icon!
            if (y != null && x != null && directionForPoint != null)
            {
                p = ValPixConverter.valToPix(
                        x, y, plotArea,
                        plot.getCalculatedMinX(),
                        plot.getCalculatedMaxX(),
                        plot.getCalculatedMinY(),
                        plot.getCalculatedMaxY()
                                            );

                if (formatter.getVertexPaint() != null &&
                        p.x > plotArea.right ||
                        p.y > plotArea.bottom ||
                        p.x < plotArea.left ||
                        p.y < plotArea.top)
                {

                    Matrix arrowMatrix = calculateMatrix(p, arrowImageWidth, arrowImageHeight, directionForPoint);

                    canvas.drawBitmap(
                            mWindArrow,
                            arrowMatrix,
                            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG)
                                     );
                }
            }
        }
    }

    private Matrix calculateMatrix(PointF p, float arrowImageWidth, float arrowImageHeight, Float directionForPoint)
    {
        Matrix arrowMatrix = new Matrix();

        // Our image points left, we need to rotate it to point up.
        final float rotateToNorth = 90.f; // degrees

        // the directions are the bearing for the "direction the wind comes from"
        // we want to show "the direction the wind is going
        final float rotateToWindsDirection = 180.f; //degrees

        final float arrowRotation =
                rotateToNorth +
                rotateToWindsDirection +
                directionForPoint;

        // center the arrow image
        arrowMatrix.preTranslate(
                -0.5f * arrowImageWidth,
                -0.5f * arrowImageHeight);

        // rotate it to the correct orientation
        arrowMatrix.postRotate(arrowRotation);

        final float scaleFactor = 0.25f;
        arrowMatrix.postScale(scaleFactor, scaleFactor);

        arrowMatrix.postTranslate(p.x, p.y );
        return arrowMatrix;
    }

    public void SetWindDirections(ArrayList<Float> windDirections)
    {
        mWindDirections = windDirections;
    }
}
