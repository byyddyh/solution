package cn.byyddyh.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 工具类
 */
public class MathUtils {
    /**
     * 对应matlab中sign
     */
    public static int sign(float num) {
        return num == 0 ? 0 : (num > 0 ? 1 : -1);
    }

    /**
     * 对应matlab中sign
     */
    public static int sign(double num) {
        return num == 0 ? 0 : (num > 0 ? 1 : -1);
    }

    /**
     * 对应matlab中sign
     */
    public static int sign(int num) {
        return Integer.compare(num, 0);
    }

    /**
     * 对应matlab中sign
     */
    public static int[] sign(int[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Integer.compare(array[i], 0);
        }
        return result;
    }

    /**
     * 对应matlab中sign
     */
    public static double[] sign(double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] == 0 ? 0 : (array[i] > 0 ? 1 : -1);
        }
        return result;
    }

    /**
     * 两个数组相加
     */
    public static int[] arraySum(int[] array1, int[] array2) {
        int[] result = new int[Math.max(array1.length, array2.length)];
        for (int i = 0; i < result.length; i++) {
            if (i < array1.length && i < array2.length) {
                result[i] = array1[i] + array2[i];
            } else {
                result[i] = i < array1.length ? array1[i] : array2[i];
            }
        }
        return result;
    }

    /**
     * 两个数组相加
     */
    public static double[] arraySum(double[] array1, double[] array2) {
        double[] result = new double[Math.max(array1.length, array2.length)];
        for (int i = 0; i < result.length; i++) {
            if (i < array1.length && i < array2.length) {
                result[i] = array1[i] + array2[i];
            } else {
                result[i] = i < array1.length ? array1[i] : array2[i];
            }
        }
        return result;
    }

    /**
     * 两个数组相加
     */
    public static float[] arraySum(float[] array1, float[] array2) {
        float[] result = new float[Math.max(array1.length, array2.length)];
        for (int i = 0; i < result.length; i++) {
            if (i < array1.length && i < array2.length) {
                result[i] = array1[i] + array2[i];
            } else {
                result[i] = i < array1.length ? array1[i] : array2[i];
            }
        }
        return result;

    }

    /**
     * 数组一定区间元素和
     */
    public static double arraySingleSumRange(double[] array, int start, int end) {
        double result = 0;
        for (int i = start; i < end; i++) {
            result += array[i];
        }
        return result;
    }

    /**
     * 数组元素和
     */
    public static double arraySingleSum(double[] array) {
        double result = 0;
        for (double v : array) {
            result += v;
        }
        return result;
    }

    /**
     * 数组元素和
     */
    public static int arraySingleSum(int[] array) {
        int result = 0;
        for (int j : array) {
            result += j;
        }
        return result;
    }

    /**
     * 两个List相减
     */
    public static List<Integer> listSub(List<Integer> array1, List<Integer> array2) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < array1.size(); i++) {
            result.add(array1.get(i) - array2.get(i));
        }
        return result;
    }

    /**
     * 两个数组相减
     */
    public static int[] arraySub(int[] array1, int[] array2) {
        int[] result = new int[Math.max(array1.length, array2.length)];
        for (int i = 0; i < result.length; i++) {
            if (i < array1.length && i < array2.length) {
                result[i] = array1[i] - array2[i];
            } else {
                result[i] = i < array1.length ? array1[i] : -array2[i];
            }
        }
        return result;
    }

    /**
     * 两个数组相减
     */
    public static double[] arraySub(double[] array1, double[] array2) {
        double[] result = new double[Math.max(array1.length, array2.length)];
        for (int i = 0; i < result.length; i++) {
            if (i < array1.length && i < array2.length) {
                result[i] = array1[i] - array2[i];
            } else {
                result[i] = i < array1.length ? array1[i] : -array2[i];
            }
        }
        return result;
    }

    /**
     * 两个数组相减
     */
    public static double[] arraySub(int num, double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = num - array[i];
        }
        return result;
    }

    /**
     * 两个数组相减
     */
    public static double[] arraySubAbs(double num, double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Math.abs(num - array[i]);
        }
        return result;
    }

    /**
     * 单个数组绝对值
     */
    public static double[] arraySingleAbs(double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Math.abs(array[i]);
        }
        return result;
    }

    /**
     * 数组乘法后求和
     */
    public static double arrayMultipleSum(double[] array1, double[] array2) {
        double result = 0;
        if (array1.length != array2.length) {
            return result;
        }
        for (int i = 0; i < array1.length; i++) {
            result += array1[i] * array2[i];
        }
        return result;
    }

    /**
     * 数组乘法
     */
    public static float[] arrayMultiple(float[] array, int num) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] * num;
        }
        return result;
    }

    /**
     * 数组乘法
     */
    public static double[] arrayMultiple(double[] array, double num) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] * num;
        }
        return result;
    }

    /**
     * 数组乘法
     */
    public static float[] arrayMultiple(float[] array, double num) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) (array[i] * num);
        }
        return result;
    }

    /**
     * 数组乘法
     */
    public static int[] arrayMultiple(int[] array, int num) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] * num;
        }
        return result;
    }

    /**
     * 数组除法
     */
    public static double[] arrayDivide(int num, int[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = num * 1.0 / array[i];
        }
        return result;
    }

    /**
     * 数组除法
     * <p>
     * 这个方法是如果array中存储的是别的数组下标，matlab中数组是以1为起始点的，而java中是以0开始
     */
    public static double[] arrayDividePos(int num, int[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = num * 1.0 / (array[i] + 1);
        }
        return result;
    }

    /**
     * 一个数组平方和
     */
    public static double arraySquareSum(double[] array) {
        double result = 0;
        for (int i = 0; i < array.length; i++) {
            result += array[i] * array[i];
        }
        return result;
    }

    /**
     * 一个数组中的最小值
     */
    public static int arrayMin(int[] array) {
        int result = array[0];
        for (int num : array) {
            if (num < result) {
                result = num;
            }
        }
        return result;
    }

    /**
     * 一个数组中的最小值
     */
    public static double arrayMin(double[] array) {
        double result = array[0];
        for (double num : array) {
            if (num < result) {
                result = num;
            }
        }
        return result;
    }

    /**
     * 一个数组中的最小值的位置
     */
    public static int arrayIndexMin(int[] array) {
        int index = 0;
        int result = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] < result) {
                result = array[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * 一个数组中的最小值的位置
     */
    public static int arrayIndexMin(double[] array) {
        int index = 0;
        double result = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (array[i] < result) {
                result = array[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * 生成一个数组
     */
    public static double[] createArray(double start, double step, double end) {
        int length = (int) ((end - start) / step);
        if (length * step != end - start) {
            length += 1;
        }
        double[] result = new double[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = start + step * i;
        }
        return result;
    }

    /**
     * 获取一定范围内的数组
     */
    public static double[] rangeArray(double[] array, int start, int end) {
        double[] newArray = new double[end - start];
        for (int i = start; i < end; i++) {
            newArray[i - start] = array[i];
        }
        return newArray;
    }

    /**
     * 初始化数组
     */

    public static void initArray(int[] array) {
        Arrays.fill(array, 0);
    }

    /**
     * 初始化数组
     */
    public static void initArray(double[] array) {
        Arrays.fill(array, 0);
    }

    /**
     * 初始化数组
     */
    public static void initArray(float[] array) {
        Arrays.fill(array, 0);
    }

    /**
     * 初始化数组
     */

    public static void initArray(double[] array, int start, int end) {
        for (int i = start; i < end; i++) {
            array[i] = 0;
        }
    }

    /**
     * 初始化数组
     */
    public static void initArray(double[] array, int start, int end, double defaultValue) {
        for (int i = start; i < end; i++) {
            array[i] = defaultValue;
        }
    }

    /**
     * 求数组平均值
     */
    public static double arrayAverage(double[] array) {
        double total = 0;
        for (double v : array) {
            total += v;
        }
        return total / array.length;
    }

    /**
     * 求数组平均值
     */
    public static float arrayAverage(float[] array) {
        double total = 0;
        for (float v : array) {
            total += v;
        }
        return (float) (total / array.length);
    }

    /**
     * 根据起始位置，并且步长生成一个数组
     */
    public static float[] initArrayByStep(float start, float step, float end) {
        int length = (int) Math.floor(MathUtils.divide((float) (end - start), step)) + 1;
        float[] result = new float[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    /**
     * 根据起始位置，并且步长生成一个数组
     */
    public static float[] initArrayByStepF(float start, float step, float end) {
        int length = (int) Math.floor((end - start) / step) + 1;
        float[] result = new float[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    /**
     * 产生一个正态分布的随机数
     */
    public static float randomGauss(Random random) {
        return (float) random.nextGaussian();
    }

    /**
     * 两个float数相除
     */
    public static float divide(float num1, float num2) {
        BigDecimal decimal1 = new BigDecimal(String.valueOf(num1));

        BigDecimal decimal2 = new BigDecimal(String.valueOf(num2));

        return decimal1.divide(decimal2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 连接数组，生成一个新的数组
     */

    public static float[] concatArray(float[]... arrays) {
        int length = 0;
        for (float[] floats : arrays) {
            length += floats.length;
        }

        float[] array = new float[length];
        int start = 0;
        for (float[] tempArr : arrays) {
            System.arraycopy(tempArr, 0, array, start, tempArr.length);
            start += tempArr.length;

        }
        return array;
    }
}
