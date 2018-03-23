package com.yinghe.whiteboardlib.utils;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Desc:数据计算工具
 *
 * @author Administrator
 * @time 2017/3/31.
 */
public class MathUtils {
    /**
     * 计算距离
     *
     * @param event
     * @return
     */
    public static float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取p1到p2的线段的长度
     *
     * @return
     */
    public static double getVectorLength(PointF vector) {
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    /**
     * 获取p1到p2的的角度
     *
     * @return
     */
    public static double cosAlpha(PointF preVector, PointF curVector) {
        //计算向量长度
        double preVectorLen = MathUtils.getVectorLength(preVector);
        double curVectorLen = MathUtils.getVectorLength(curVector);

        //计算两个向量的夹角.
        double cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                / (preVectorLen * curVectorLen);

        //由于计算误差，可能会带来略大于1的cos
        if (cosAlpha > 1.0f) {
            cosAlpha = 1.0f;
        }

        return cosAlpha;
    }

    /**
     * cos值转换为角度值
     * @param cosAlpha
     * @return
     */
    public static double cosAlphaToAngle(double cosAlpha){
        return Math.acos(cosAlpha) * 180.0 / Math.PI;
    }

    /**
     * 获得角度
     * @param pointPre
     * @param pointCur
     * @param pointCenter
     * @return
     */
    public static double getDAngle(PointF pointPre, PointF pointCur, PointF pointCenter){
        //根据移动坐标的变化构建两个向量，以便计算两个向量角度.
        PointF preVector = new PointF();
        PointF curVector = new PointF();
        preVector.set(pointPre.x - pointCenter.x, pointPre.y - pointCenter.y);//旋转后向量
        curVector.set(pointCur.x - pointCenter.x, pointCur.y - pointCenter.y);//旋转前向量
        //计算向量长度
        double preVectorLen = MathUtils.getVectorLength(preVector);
        double curVectorLen = MathUtils.getVectorLength(curVector);

        //计算两个向量的夹角.
        double cosAlpha = (preVector.x * curVector.x + preVector.y * curVector.y)
                / (preVectorLen * curVectorLen);
        //由于计算误差，可能会带来略大于1的cos，例如
        if (cosAlpha > 1.0f) {
            cosAlpha = 1.0f;
        }
        //本次的角度已经计算出来。
        double dAngle = Math.acos(cosAlpha) * 180.0 / Math.PI;
        // 判断顺时针和逆时针.
        //判断方法其实很简单，这里的v1v2其实相差角度很小的。
        //先转换成单位向量
        preVector.x /= preVectorLen;
        preVector.y /= preVectorLen;
        curVector.x /= curVectorLen;
        curVector.y /= curVectorLen;
        //作curVector的逆时针垂直向量。
        PointF verticalVec = new PointF(curVector.y, -curVector.x);

        //判断这个垂直向量和v1的点积，点积>0表示俩向量夹角锐角。=0表示垂直，<0表示钝角
        float vDot = preVector.x * verticalVec.x + preVector.y * verticalVec.y;
        if (vDot > 0) {
            //v2的逆时针垂直向量和v1是锐角关系，说明v1在v2的逆时针方向。
        } else {
            dAngle = -dAngle;
        }

        return dAngle;
    }

    /**
     * 取手势中心点
     * @param point
     * @param event
     */
    public static void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 取旋转角度
     * @param event
     * @return
     */
    public static float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

}
