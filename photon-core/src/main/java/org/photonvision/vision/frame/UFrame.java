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

package org.photonvision.vision.frame;

import org.photonvision.common.util.math.MathUtils;
import org.photonvision.vision.opencv.CVUMat;
import org.photonvision.vision.opencv.Releasable;

/**
 * Alternative implementation of {@link org.photonvision.vision.frame} that uses
 * {@link org.photonvision.vision.opencv.CVUMat} to utilize OpenCL processing.
 */
public class UFrame implements Releasable {
    public final long sequenceID;
    public final long timestampNanos;

    // Frame should at _least_ contain the thresholded frame, and sometimes the color image
    public final CVUMat colorImage;
    public final CVUMat processedImage;
    public final FrameThresholdType type;

    public final FrameStaticProperties frameStaticProperties;

    public UFrame(
            long sequenceID,
            CVUMat color,
            CVUMat processed,
            FrameThresholdType type,
            long timestampNanos,
            FrameStaticProperties frameStaticProperties) {
        this.sequenceID = sequenceID;
        this.colorImage = color;
        this.processedImage = processed;
        this.type = type;
        this.timestampNanos = timestampNanos;
        this.frameStaticProperties = frameStaticProperties;
    }

    public UFrame(
            long sequenceID,
            CVUMat color,
            CVUMat processed,
            FrameThresholdType processType,
            FrameStaticProperties frameStaticProperties) {
        this(sequenceID, color, processed, processType, MathUtils.wpiNanoTime(), frameStaticProperties);
    }

    public UFrame() {
        this(
                -1,
                new CVUMat(),
                new CVUMat(),
                FrameThresholdType.NONE,
                MathUtils.wpiNanoTime(),
                new FrameStaticProperties(0, 0, 0, null));
    }

    public void copyTo(UFrame destFrame) {
        colorImage.getUMat().copyTo(destFrame.colorImage.getUMat());
        processedImage.getUMat().copyTo(destFrame.processedImage.getUMat());
    }

    @Override
    public void release() {
        colorImage.release();
        processedImage.release();
    }
}
