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
package com.hojoai.common.asserts;

/**
 * The common class for asserts.
 *
 * @author kevanwong
 * @version 1.0, 2018/6/27
 * @since hojoai-common 1.0.0
 */
public class Asserts {

    // Short less
    /**
     * 断言 {@link Short} 值小于 0。
     *
     * @param value 待校验的值，为 null 或 ≥0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void ltZero(Short value, String name) {
        lt(value, (short) 0, name);
    }

    /**
     * 断言 {@link Short} 值小于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≥{@code threshold} 时失败
     * @param threshold 上界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lt(Short value, short threshold, String name) {
        if (value == null || value >= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than [%d]", name, threshold));
        }
    }

    // Short less or equal
    /**
     * 断言 {@link Short} 值小于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &gt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lteZero(Short value, String name) {
        lte(value, (short) 0, name);
    }

    /**
     * 断言 {@link Short} 值小于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &gt;{@code threshold} 时失败
     * @param threshold 上界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lte(Short value, short threshold, String name) {
        if (value == null || value > threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than or equal to [%d]", name, threshold));
        }
    }

    // Short greater
    /**
     * 断言 {@link Short} 值大于 0。
     *
     * @param value 待校验的值，为 null 或 ≤0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gtZero(Short value, String name) {
        gt(value, (short) 0, name);
    }

    /**
     * 断言 {@link Short} 值大于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≤{@code threshold} 时失败
     * @param threshold 下界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gt(Short value, short threshold, String name) {
        if (value == null || value <= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than [%d]", name, threshold));
        }
    }

    // Short greater or equal
    /**
     * 断言 {@link Short} 值大于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &lt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gteZero(Short value, String name) {
        gte(value, (short) 0, name);
    }

    /**
     * 断言 {@link Short} 值大于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &lt;{@code threshold} 时失败
     * @param threshold 下界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gte(Short value, short threshold, String name) {
        if (value == null || value < threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than or equal to [%d]", name, threshold));
        }
    }

    // Integer less
    /**
     * 断言 {@link Integer} 值小于 0。
     *
     * @param value 待校验的值，为 null 或 ≥0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void ltZero(Integer value, String name) {
        lt(value, 0, name);
    }

    /**
     * 断言 {@link Integer} 值小于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≥{@code threshold} 时失败
     * @param threshold 上界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lt(Integer value, int threshold, String name) {
        if (value == null || value >= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than [%d]", name, threshold));
        }
    }

    // Integer less or equal
    /**
     * 断言 {@link Integer} 值小于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &gt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lteZero(Integer value, String name) {
        lte(value, 0, name);
    }

    /**
     * 断言 {@link Integer} 值小于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &gt;{@code threshold} 时失败
     * @param threshold 上界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lte(Integer value, int threshold, String name) {
        if (value == null || value > threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than or equal to [%d]", name, threshold));
        }
    }

    // Integer greater
    /**
     * 断言 {@link Integer} 值大于 0。
     *
     * @param value 待校验的值，为 null 或 ≤0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gtZero(Integer value, String name) {
        gt(value, 0, name);
    }

    /**
     * 断言 {@link Integer} 值大于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≤{@code threshold} 时失败
     * @param threshold 下界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gt(Integer value, int threshold, String name) {
        if (value == null || value <= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than [%d]", name, threshold));
        }
    }

    // Integer greater or equal
    /**
     * 断言 {@link Integer} 值大于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &lt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gteZero(Integer value, String name) {
        gte(value, 0, name);
    }

    /**
     * 断言 {@link Integer} 值大于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &lt;{@code threshold} 时失败
     * @param threshold 下界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gte(Integer value, int threshold, String name) {
        if (value == null || value < threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than or equal to [%d]", name, threshold));
        }
    }

    // Long less
    /**
     * 断言 {@link Long} 值小于 0。
     *
     * @param value 待校验的值，为 null 或 ≥0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void ltZero(Long value, String name) {
        lt(value, 0, name);
    }

    /**
     * 断言 {@link Long} 值小于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≥{@code threshold} 时失败
     * @param threshold 上界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lt(Long value, long threshold, String name) {
        if (value == null || value >= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than [%d]", name, threshold));
        }
    }

    // Long less or equal
    /**
     * 断言 {@link Long} 值小于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &gt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lteZero(Long value, String name) {
        lte(value, 0, name);
    }

    /**
     * 断言 {@link Long} 值小于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &gt;{@code threshold} 时失败
     * @param threshold 上界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lte(Long value, long threshold, String name) {
        if (value == null || value > threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than or equal to [%d]", name, threshold));
        }
    }

    // Long greater
    /**
     * 断言 {@link Long} 值大于 0。
     *
     * @param value 待校验的值，为 null 或 ≤0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gtZero(Long value, String name) {
        gt(value, 0, name);
    }

    /**
     * 断言 {@link Long} 值大于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≤{@code threshold} 时失败
     * @param threshold 下界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gt(Long value, long threshold, String name) {
        if (value == null || value <= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than [%d]", name, threshold));
        }
    }

    // Long greater or equal
    /**
     * 断言 {@link Long} 值大于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &lt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gteZero(Long value, String name) {
        gte(value, 0, name);
    }

    /**
     * 断言 {@link Long} 值大于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &lt;{@code threshold} 时失败
     * @param threshold 下界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gte(Long value, long threshold, String name) {
        if (value == null || value < threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than or equal to [%d]", name, threshold));
        }
    }

    // Float less
    /**
     * 断言 {@link Float} 值小于 0。
     *
     * @param value 待校验的值，为 null 或 ≥0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void ltZero(Float value, String name) {
        lt(value, 0, name);
    }

    /**
     * 断言 {@link Float} 值小于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≥{@code threshold} 时失败
     * @param threshold 上界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lt(Float value, float threshold, String name) {
        if (value == null || value >= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than [%f]", name, threshold));
        }
    }

    // Float less or equal
    /**
     * 断言 {@link Float} 值小于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &gt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lteZero(Float value, String name) {
        lte(value, 0, name);
    }

    /**
     * 断言 {@link Float} 值小于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &gt;{@code threshold} 时失败
     * @param threshold 上界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lte(Float value, float threshold, String name) {
        if (value == null || value > threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than or equal to [%f]", name, threshold));
        }
    }

    // Float greater
    /**
     * 断言 {@link Float} 值大于 0。
     *
     * @param value 待校验的值，为 null 或 ≤0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gtZero(Float value, String name) {
        gt(value, 0, name);
    }

    /**
     * 断言 {@link Float} 值大于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≤{@code threshold} 时失败
     * @param threshold 下界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gt(Float value, float threshold, String name) {
        if (value == null || value <= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than [%f]", name, threshold));
        }
    }

    // Float greater or equal
    /**
     * 断言 {@link Float} 值大于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &lt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gteZero(Float value, String name) {
        gte(value, 0, name);
    }

    /**
     * 断言 {@link Float} 值大于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &lt;{@code threshold} 时失败
     * @param threshold 下界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gte(Float value, float threshold, String name) {
        if (value == null || value < threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than or equal to [%f]", name, threshold));
        }
    }

    // Double less
    /**
     * 断言 {@link Double} 值小于 0。
     *
     * @param value 待校验的值，为 null 或 ≥0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void ltZero(Double value, String name) {
        lt(value, 0, name);
    }

    /**
     * 断言 {@link Double} 值小于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≥{@code threshold} 时失败
     * @param threshold 上界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lt(Double value, double threshold, String name) {
        if (value == null || value >= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than [%f]", name, threshold));
        }
    }

    // Double less or equal
    /**
     * 断言 {@link Double} 值小于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &gt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lteZero(Double value, String name) {
        lte(value, 0, name);
    }

    /**
     * 断言 {@link Double} 值小于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &gt;{@code threshold} 时失败
     * @param threshold 上界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void lte(Double value, double threshold, String name) {
        if (value == null || value > threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be less than or equal to [%f]", name, threshold));
        }
    }

    // Double greater
    /**
     * 断言 {@link Double} 值大于 0。
     *
     * @param value 待校验的值，为 null 或 ≤0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gtZero(Double value, String name) {
        gt(value, 0, name);
    }

    /**
     * 断言 {@link Double} 值大于指定阈值。
     *
     * @param value     待校验的值，为 null 或 ≤{@code threshold} 时失败
     * @param threshold 下界（不包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gt(Double value, double threshold, String name) {
        if (value == null || value <= threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than [%f]", name, threshold));
        }
    }

    // Double greater or equal
    /**
     * 断言 {@link Double} 值大于或等于 0。
     *
     * @param value 待校验的值，为 null 或 &lt;0 时失败
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gteZero(Double value, String name) {
        gte(value, 0, name);
    }

    /**
     * 断言 {@link Double} 值大于或等于指定阈值。
     *
     * @param value     待校验的值，为 null 或 &lt;{@code threshold} 时失败
     * @param threshold 下界（包含）
     * @param name      参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当断言不成立时
     */
    public static void gte(Double value, double threshold, String name) {
        if (value == null || value < threshold) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should be greater than or equal to [%f]", name, threshold));
        }
    }

    // Not false
    /**
     * 断言布尔表达式为 {@code true}（且非 null）。
     *
     * @param expression 待校验的 {@link Boolean}，为 null 或为 {@code false} 时失败
     * @param name       参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当值为 null、为 false 时
     */
    public static void notFalse(Boolean expression, String name) {
        if (expression == null) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be null", name));
        }

        if (!expression) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be false", name));
        }
    }

    // Not true
    /**
     * 断言布尔表达式必须为 {@code false}（不可为 null）。
     *
     * @param expression 待校验的 {@link Boolean}，为 null 或为 {@code true} 时失败
     * @param name       参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当值为 null 或为 true 时
     */
    public static void notTrue(Boolean expression, String name) {
        if (expression == null) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be null", name));
        }

        if (expression) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be true", name));
        }
    }

    // Not null
    /**
     * 断言对象引用非 null。
     *
     * @param value 待校验的引用
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当 {@code value == null} 时
     */
    public static void notNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be null", name));
        }
    }

    // Not empty
    /**
     * 断言字符串非 null 且长度不为 0（允许仅空白字符，若需去空白请用 {@link #notBlank(String, String)}）。
     *
     * @param value 待校验的字符串
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当为 null 或 {@link String#isEmpty()} 为 true 时
     */
    public static void notEmpty(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be empty", name));
        }
    }

    // Not blank
    /**
     * 断言字符串非 null 且去空白后非空。
     *
     * @param value 待校验的字符串
     * @param name  参数或字段名称，用于异常信息
     * @throws IllegalArgumentException 当为 null 或仅含空白时
     */
    public static void notBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("has an illegal argument, [%s] should not be blank", name));
        }
    }
}
