/*
 * Copyright 2026 HojoAI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hojoai.webmvc.omniasst.meeting.service.voiceprint.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class VectorSimilarityUtils {
    public static double cosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have same length");
        }
        RealVector v1 = new ArrayRealVector(vector1);
        RealVector v2 = new ArrayRealVector(vector2);
        return v1.cosine(v2);
    }
}
