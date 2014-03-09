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
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesRenderer;


public class CustomPointRenderer<FormatterType extends LineAndPointFormatter> extends XYSeriesRenderer<FormatterType> {

    private float circleWidth = 1;
    private Bitmap mWindArrow = null;

    public CustomPointRenderer(XYPlot plot, Bitmap windArrow) {
        super(plot);
        mWindArrow = windArrow;
    }

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {
        for(XYSeries series : getPlot().getSeriesListForRenderer(this.getClass())) {
            drawSeries(canvas, plotArea, series, getFormatter(series));
        }
    }
    @Override
    protected void doDrawLegendIcon(Canvas canvas, RectF rect, FormatterType formatter) {
        // horizontal icon:
        float centerY = rect.centerY();
        float centerX = rect.centerX();

        if(formatter.getFillPaint() != null) {
            canvas.drawRect(rect, formatter.getFillPaint());
        }
        if(formatter.getLinePaint() != null) {
            canvas.drawLine(rect.left, rect.bottom, rect.right, rect.top, formatter.getLinePaint());
        }

        if(formatter.getVertexPaint() != null) {
            canvas.drawPoint(centerX, centerY, formatter.getVertexPaint());
        }
    }

    private void drawSeries(Canvas canvas, RectF plotArea, XYSeries series, LineAndPointFormatter formatter) throws PlotRenderException {

        PointF p = null;
        XYPlot plot = getPlot();
        int size = series.size();

        for (int i = 0; i < size; i++) {
            Number y = series.getY(i);
            Number x = series.getX(i);

            if (y != null && x != null) {
                p = ValPixConverter.valToPix(x, y, plotArea,
                        plot.getCalculatedMinX(),
                        plot.getCalculatedMaxX(),
                        plot.getCalculatedMinY(),
                        plot.getCalculatedMaxY());

                if (formatter.getVertexPaint() != null) {
                    boolean offScreen = p.x > plotArea.right || p.y > plotArea.bottom || p.x < plotArea.left || p.y < plotArea.top;
                    if(!offScreen)
                    {

                        Matrix arrow = new Matrix();
                        arrow.preScale(0.5f, 0.5f);

                        final float rotateToNorth = 90.f;
                        arrow.preRotate(rotateToNorth ); // clockwise
                        arrow.postTranslate(p.x, p.y);

                        canvas.drawBitmap(mWindArrow, arrow, new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                        //canvas.drawCircle(p.x, p.y - circleWidth, circleWidth, formatter.getVertexPaint());
                    }
                }
            }
        }
    }

    public void setWidth(float width){
        circleWidth = width;
    }
}
