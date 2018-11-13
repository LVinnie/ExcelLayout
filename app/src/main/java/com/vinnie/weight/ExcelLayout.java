package com.vinnie.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 表格布局
 *
 * 注意：第一个子View的高度决定了所有行的高度。
 *
 * @author Vinnie
 */
public class ExcelLayout extends ViewGroup {

    final static String TAG = "ExcelLayout";

    int maxSpanCountX = 0;
    int maxSpanCountY = 0;
    float maxItemSizeX = 0f;
    int maxItemSizeY = 0;

    int borderColor;
    Paint borderPaint;

    public ExcelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ExcelLayout);
        borderColor = array.getColor(R.styleable.ExcelLayout_ELBorderColor, Color.BLACK);

        array.recycle();
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(borderColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        int cCount = getChildCount();

        int childWidthMeasureSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, LayoutParams.WRAP_CONTENT);
        int childHeightMeasureSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < cCount; i++) {
            View childFirst = getChildAt(i);
            if (i == 0) {
                measureChild(childFirst, childWidthMeasureSpec, childHeightMeasureSpec);
                maxItemSizeY = Math.max(maxItemSizeY, childFirst.getMeasuredHeight());
            }
            ExcelLayoutParam paramFirst = (ExcelLayoutParam) childFirst.getLayoutParams();
            maxSpanCountX = Math.max(maxSpanCountX, paramFirst.startX + paramFirst.spanX);
            maxSpanCountY = Math.max(maxSpanCountY, paramFirst.startY + paramFirst.spanY);
        }

        height = maxSpanCountY * maxItemSizeY;
        if (modeWidth == MeasureSpec.EXACTLY) {
            maxItemSizeX = 1f * sizeWidth / maxSpanCountX;
        }

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            ExcelLayoutParam param = (ExcelLayoutParam) child.getLayoutParams();
            childWidthMeasureSpec = getChildMeasureSpec(MeasureSpec.EXACTLY, 0, Math.round(param.spanX * maxItemSizeX));
            childHeightMeasureSpec = getChildMeasureSpec(MeasureSpec.EXACTLY, 0, param.spanY * maxItemSizeY);
            measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
        }

        setMeasuredDimension(
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        for (int i=0; i<cCount; i++) {
            View child = getChildAt(i);
            ExcelLayoutParam param = (ExcelLayoutParam) child.getLayoutParams();
            child.layout(Math.round(param.startX * maxItemSizeX), param.startY * maxItemSizeY, Math.round((param.startX + param.spanX) * maxItemSizeX), (param.startY + param.spanY) * maxItemSizeY);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        float paintHalfWidth = 0.5f;
        borderPaint.setStrokeWidth(paintHalfWidth * 2);
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            ExcelLayoutParam param = (ExcelLayoutParam) child.getLayoutParams();

            if (param.startX == 0) {
                canvas.drawLine(param.startX * maxItemSizeX + paintHalfWidth, param.startY * maxItemSizeY, (param.startX + param.spanX) * maxItemSizeX + paintHalfWidth, param.startY * maxItemSizeY, borderPaint);
            } else {
                canvas.drawLine(param.startX * maxItemSizeX, param.startY * maxItemSizeY, (param.startX + param.spanX) * maxItemSizeX, param.startY * maxItemSizeY, borderPaint);
            }

            if (param.startX + param.spanX >= maxSpanCountX) {
                canvas.drawLine((param.startX + param.spanX) * maxItemSizeX - paintHalfWidth, param.startY * maxItemSizeY, (param.startX + param.spanX) * maxItemSizeX - paintHalfWidth, (param.startY + param.spanY) * maxItemSizeY, borderPaint);
            } else {
                canvas.drawLine((param.startX + param.spanX) * maxItemSizeX, param.startY * maxItemSizeY, (param.startX + param.spanX) * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY, borderPaint);
            }

            if (param.startY + param.spanY >= maxSpanCountY) {
                canvas.drawLine((param.startX + param.spanX) * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY - paintHalfWidth, param.startX * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY - paintHalfWidth, borderPaint);
            } else {
                canvas.drawLine((param.startX + param.spanX) * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY, param.startX * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY, borderPaint);
            }

            canvas.drawLine(param.startX * maxItemSizeX, (param.startY + param.spanY) * maxItemSizeY, param.startX * maxItemSizeX, param.startY * maxItemSizeY, borderPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new ExcelLayoutParam(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ExcelLayoutParam(getContext(), attrs);
    }

    public class ExcelLayoutParam extends LayoutParams {

        int startX = 0;
        int startY = 0;

        int spanX = 0;
        int spanY = 0;

        public ExcelLayoutParam(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.ExcelLayout);
            startX = array.getInt(R.styleable.ExcelLayout_ELStartX, 0);
            startY = array.getInt(R.styleable.ExcelLayout_ELStartY, 0);
            spanX = array.getInt(R.styleable.ExcelLayout_ELSpanX, 1);
            spanY = array.getInt(R.styleable.ExcelLayout_ELSpanY, 1);

            array.recycle();
        }

        public ExcelLayoutParam(LayoutParams source) {
            super(source);
            if (source instanceof ExcelLayoutParam) {
                ExcelLayoutParam e = (ExcelLayoutParam) source;
                this.startX = e.startX;
                this.startY = e.startY;
                this.spanX = e.spanX;
                this.spanY = e.spanY;
            }
        }
    }
}
