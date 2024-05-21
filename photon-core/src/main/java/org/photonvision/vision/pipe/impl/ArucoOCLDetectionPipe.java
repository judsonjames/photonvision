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

package org.photonvision.vision.pipe.impl;

import java.util.List;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Point2d;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_core.TermCriteria;
import org.bytedeco.opencv.opencv_core.UMat;
import org.photonvision.vision.aruco.ArucoDetectionResult;
import org.photonvision.vision.aruco.PhotonArucoOCLDetector;
import org.photonvision.vision.opencv.CVUMat;
import org.photonvision.vision.opencv.Releasable;
import org.photonvision.vision.pipe.CVPipe;

public class ArucoOCLDetectionPipe
        extends CVPipe<CVUMat, List<ArucoDetectionResult>, ArucoDetectionPipeParams>
        implements Releasable {
    // ArucoDetector wrapper class
    private final PhotonArucoOCLDetector photonDetector = new PhotonArucoOCLDetector();

    // Ratio multiplied with image size and added to refinement window size
    private static final double kRefineWindowImageRatio = 0.004;
    // Ratio multiplied with max marker diagonal length and added to refinement window size
    private static final double kRefineWindowMarkerRatio = 0.03;

    @Override
    protected List<ArucoDetectionResult> process(CVUMat in) {
        var imgMat = in.getUMat();

        // Sanity check -- image should not be empty
        if (imgMat.empty()) {
            // give up is best we can do here
            return List.of();
        }

        var detections = photonDetector.detect(imgMat);
        // manually do corner refinement ourselves
        if (params.useCornerRefinement) {
            for (var detection : detections) {
                double[] xCorners = detection.getXCorners();
                double[] yCorners = detection.getYCorners();
                Point[] cornerPoints =
                        new Point[] {
                            new Point((int) xCorners[0], (int) yCorners[0]),
                            new Point((int) xCorners[1], (int) yCorners[1]),
                            new Point((int) xCorners[2], (int) yCorners[2]),
                            new Point((int) xCorners[3], (int) yCorners[3]),
                            // new Point2d(xCorners[1], yCorners[1]),
                            // new Point2d(xCorners[2], yCorners[2]),
                            // new Point2d(xCorners[3], yCorners[3])
                        };
                double bltr =
                        Math.hypot(
                                cornerPoints[2].x() - cornerPoints[0].x(), cornerPoints[2].y() - cornerPoints[0].y());
                double brtl =
                        Math.hypot(
                                cornerPoints[3].x() - cornerPoints[1].x(), cornerPoints[3].y() - cornerPoints[1].y());
                double minDiag = Math.min(bltr, brtl);
                int halfWindowLength =
                        (int) Math.ceil(kRefineWindowImageRatio * Math.min(imgMat.rows(), imgMat.cols()));
                halfWindowLength += (int) (minDiag * kRefineWindowMarkerRatio);
                // dont do refinement on small markers
                if (halfWindowLength < 4) continue;
                var halfWindowSize = new Size(halfWindowLength, halfWindowLength);

                // var ptsMat = new MatOfPoint2f(cornerPoints);
                
                // Create new UMat of Corner Points
                // UMatOfPoint2f ptsUMat = new UMatOfPoint2f(cornerPoints);

                // MatOfPoint2f ptsMat = new MatOfPoint2f(cornerPoints);

                // org.bytedeco.opencv.opencv_core.Mat m = new Mat()

                // m.colRange(new Range());
                
                // TODO Please fix this. UMat doesn't have a native MatOfPoint2f conversion factor.
                
                var criteria =
                        new TermCriteria(3, params.refinementMaxIterations, params.refinementMinErrorPx);
                opencv_imgproc.cornerSubPix(imgMat, new UMat(), halfWindowSize, new Size(-1, -1), criteria);
                cornerPoints = cornerPoints.toArray();
                for (int i = 0; i < cornerPoints.length; i++) {
                    var pt = cornerPoints[i];
                    xCorners[i] = pt.x();
                    yCorners[i] = pt.y();
                    // If we want to debug the refinement window, draw a rectangle on the image
                    if (params.debugRefineWindow) {
                        drawCornerRefineWindow(imgMat, pt, halfWindowLength);
                    }
                }
            }
        }
        return List.of(detections);
    }

    @Override
    public void setParams(ArucoDetectionPipeParams newParams) {
        if (this.params == null || !this.params.equals(newParams)) {
            photonDetector
                    .getDetector()
                    .setDictionary(opencv_objdetect.getPredefinedDictionary(newParams.tagFamily));
            var detectParams = photonDetector.getParams();
            detectParams.adaptiveThreshWinSizeMin(newParams.threshMinSize);
            detectParams.adaptiveThreshWinSizeStep(newParams.threshStepSize);
            detectParams.adaptiveThreshWinSizeMax(newParams.threshMaxSize);
            detectParams.adaptiveThreshConstant(newParams.threshConstant);

            detectParams.errorCorrectionRate(newParams.errorCorrectionRate);

            detectParams.useAruco3Detection(newParams.useAruco3);
            detectParams.minSideLengthCanonicalImg(newParams.aruco3MinCanonicalImgSide);
            detectParams.minMarkerLengthRatioOriginalImg((float) newParams.aruco3MinMarkerSideRatio);

            photonDetector.setParams(detectParams);
        }

        super.setParams(newParams);
    }

    public PhotonArucoOCLDetector getPhotonDetector() {
        return photonDetector;
    }

    private void drawCornerRefineWindow(UMat outputMat, Point2d corner, int windowSize) {
        int thickness = (int) (Math.ceil(Math.max(outputMat.cols(), outputMat.rows()) * 0.003));
        var pt1 = new Point((int) corner.x() - windowSize, (int) corner.y() - windowSize);
        var pt2 = new Point((int) corner.x() + windowSize, (int) corner.y() + windowSize);
        opencv_imgproc.rectangle(outputMat, pt1, pt2, new Scalar(0, 0, 255, 0), thickness, opencv_imgproc.LINE_8, 0);
        // Imgproc.rectangle(outputMat, pt1, pt2, new Scalar(0, 0, 255), thickness);
    }

    @Override
    public void release() {
        photonDetector.release();
    }
}
