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

package org.photonvision.vision.aruco;

import java.util.Arrays;
import java.util.Comparator;

import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_core.UMatVector;
import org.bytedeco.opencv.opencv_objdetect.ArucoDetector;
import org.bytedeco.opencv.opencv_objdetect.DetectorParameters;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.vision.opencv.Releasable;

/** This class wraps an {@link ArucoDetector} for convenience. */
public class PhotonArucoOCLDetector implements Releasable {
    private static final Logger logger = new Logger(PhotonArucoOCLDetector.class, LogGroup.VisionModule);

    // private static class ArucoDetectorHack extends ArucoDetector {
    //     public ArucoDetectorHack(Dictionary predefinedDictionary) {
    //         super(predefinedDictionary);
    //     }

    //     // avoid double-free by keeping track of this ourselves (ew)
    //     private boolean freed = false;

    //     @Override
    //     public void finalize() throws Throwable {
    //         if (freed) {
    //             return;
    //         }

    //         super.finalize();
    //         freed = true;
    //     }
    // }

    private final ArucoDetector detector =
            new ArucoDetector(opencv_objdetect.getPredefinedDictionary(opencv_objdetect.DICT_APRILTAG_16h5));

    private final UMat ids = new UMat();
    private final UMatVector cornerUMats = new UMatVector();

    public ArucoDetector getDetector() {
        return detector;
    }

    /**
     * Get a copy of the current parameters being used. Must next call setParams to update the
     * underlying detector object!
     */
    public DetectorParameters getParams() {
        return detector.getDetectorParameters();
    }

    public void setParams(DetectorParameters params) {
        detector.setDetectorParameters(params);
    }

    /**
     * Detect fiducial tags in the grayscaled image using the {@link ArucoDetector} in this class.
     * Parameters for detection can be modified with {@link #setParams(DetectorParameters)}.
     *
     * @param grayscaleImg A grayscaled image
     * @return An array of ArucoDetectionResult, which contain tag corners and id.
     */
    public ArucoDetectionResult[] detect(UMat grayscaleImg) {
        // detect tags
        detector.detectMarkers(grayscaleImg, cornerUMats, ids);

        // Not sure if this will cause issues unless there's more than 2^31-1 number of corners
        ArucoDetectionResult[] results = new ArucoDetectionResult[Math.toIntExact(cornerUMats.size())];
        for (int i = 0; i < cornerUMats.size(); i++) {
            // each detection has a Mat of corners
            UMat cornerUMat = cornerUMats.get(i);

            // Aruco detection returns corners (BR, BL, TL, TR).
            // For parity with AprilTags and photonlib, we want (BL, BR, TR, TL).
            double[] xCorners = {
                cornerUMat.row(0).col(1).checkVector(0),
                cornerUMat.row(0).col(0).checkVector(0),
                cornerUMat.row(0).col(3).checkVector(0),
                cornerUMat.row(0).col(2).checkVector(0),
                // cornerMat.get(0, 1)[0],
                // cornerMat.get(0, 0)[0],
                // cornerMat.get(0, 3)[0],
                // cornerMat.get(0, 2)[0]
            };
            double[] yCorners = {
                cornerUMat.row(0).col(1).checkVector(1),
                cornerUMat.row(0).col(0).checkVector(1),
                cornerUMat.row(0).col(3).checkVector(1),
                cornerUMat.row(0).col(2).checkVector(1),
                // cornerMat.get(0, 1)[1],
                // cornerMat.get(0, 0)[1],
                // cornerMat.get(0, 3)[1],
                // cornerMat.get(0, 2)[1]
            };
            cornerUMat.release();

            results[i] = new ArucoDetectionResult(xCorners, yCorners, (int) ids.row(i).checkVector(0));
            // results[i] = new ArucoDetectionResult(xCorners, yCorners, (int) ids.get(i, 0)[0]);
        }

        ids.release();

        // sort tags by ID
        Arrays.sort(results, Comparator.comparingInt(ArucoDetectionResult::getId));

        return results;
    }

    @Override
    public void release() {
        ids.release();
        cornerUMats.clear();
    }
}
