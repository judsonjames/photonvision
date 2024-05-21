/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.vision.opencv;

import org.bytedeco.opencv.opencv_core.UMat;
import org.opencv.core.CvType;

/**
 * Helper class derived from {@link org.opencv.core.MatOfPoint2f} to support
 * UMat from JavaCV.
 * 
 * TODO this may not be necessary but it might be for ArucoOCL Detection
 */
public class UMatOfPoint2f extends UMat {
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 2;

    public UMatOfPoint2f() {
        super();
    }

    // public UMatOfPoint2f(Point... a) {
    //     super();
    //     fromArray(a);
    // }

    // public UMatOfPoint2f(Point2d... a) {
    //     super();
    //     fromArray(a);
    // }

    // public void alloc(int elemNumber) {
    //     if (elemNumber > 0) {
    //         super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    //     }
    // }

    // public void fromArray(Point... a) {
    //     if (a == null || a.length == 0) {
    //         return;
    //     }

    //     int num = a.length;
    //     alloc(num);

    //     float buff[] = new float[num * _channels];
    //     for (int i = 0; i < num; ++i) {
    //         Point p = a[i];
    //         buff[_channels * i + 0] = (float) p.x();
    //         buff[_channels * i + 1] = (float) p.y();
    //     }

    //     put(0, 0, buff);
    // }


    // public void fromArray(Point2d... a) {
    //     if (a == null || a.length == 0) {
    //         return;
    //     }

    //     int num = a.length;
    //     alloc(num);

    //     float buff[] = new float[num * _channels];
    //     for (int i = 0; i < num; ++i) {
    //         Point2d p = a[i];
    //         buff[_channels * i + 0] = (float) p.x();
    //         buff[_channels * i + 1] = (float) p.y();
    //     }

    //     put(0, 0, buff);
    // }

    // public Point[] toArray() {
    //     int num = (int) total();
    //     Point[] ap = new Point[num];
    //     if (num == 0) {
    //         return ap;
    //     }
    //     float buff[] = new float[num * _channels];
    //     get(0, 0, buff); // TODO: check ret val!
    //     for (int i = 0; i < num; i++) {
    //         ap[i] = new Point(buff[i * _channels], buff[i * _channels + 1]);
    //     }
    //     return ap;
    // }

    // public void fromList(List<Point> a) {
    //     fromArray(a.toArray(new Point[0]));
    // }
}
