/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package java.lang;

/**
 * The class <code>Math</code> contains methods for performing basic
 * numeric operations.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */

public final strictfp class Math {

    /**
     * Don't let anyone instantiate this class.
     */
    private Math() {}

    /**
     * The <code>double</code> value that is closer than any other to
     * <code>e</code>, the base of the natural logarithms.
     * @since CLDC 1.1
     */
    public static final double E = 2.7182818284590452354;

    /**
     * The <code>double</code> value that is closer than any other to
     * <i>pi</i>, the ratio of the circumference of a circle to its diameter.
     * @since CLDC 1.1
     */
    public static final double PI = 3.14159265358979323846;

    /**
     * Returns the trigonometric sine of an angle.  Special cases:
     * <ul><li>If the argument is NaN or an infinity, then the
     * result is NaN.
     * <li>If the argument is positive zero, then the result is
     * positive zero; if the argument is negative zero, then the
     * result is negative zero.</ul>
     *
     * @param   a   an angle, in radians.
     * @return  the sine of the argument.
     * @since   CLDC 1.1
     */
    public static native double sin(double a);

    /**
     * Returns the trigonometric cosine of an angle. Special case:
     * <ul><li>If the argument is NaN or an infinity, then the
     * result is NaN.</ul>
     *
     * @param   a   an angle, in radians.
     * @return  the cosine of the argument.
     * @since   CLDC 1.1
     */
    public static native double cos(double a);

    /**
     * Returns the trigonometric tangent of an angle.  Special cases:
     * <ul><li>If the argument is NaN or an infinity, then the result
     * is NaN.
     * <li>If the argument is positive zero, then the result is
     * positive zero; if the argument is negative zero, then the
     * result is negative zero</ul>
     *
     * @param   a   an angle, in radians.
     * @return  the tangent of the argument.
     * @since   CLDC 1.1
     */
    public static native double tan(double a);

    /**
     * Converts an angle measured in degrees to the equivalent angle
     * measured in radians.
     *
     * @param   angdeg   an angle, in degrees
     * @return  the measurement of the angle <code>angdeg</code>
     *          in radians.
     * @since   CLDC 1.1
     */
    public static double toRadians(double angdeg) {
        return angdeg / 180.0 * PI;
    }

    /**
     * Converts an angle measured in radians to the equivalent angle
     * measured in degrees.
     *
     * @param   angrad   an angle, in radians
     * @return  the measurement of the angle <code>angrad</code>
     *          in degrees.
     * @since   CLDC 1.1
     */
    public static double toDegrees(double angrad) {
        return angrad * 180.0 / PI;
    }

    /**
     * Returns the correctly rounded positive square root of a
     * <code>double</code> value.
     * Special cases:
     * <ul><li>If the argument is NaN or less than zero, then the result
     * is NaN.
     * <li>If the argument is positive infinity, then the result is positive
     * infinity.
     * <li>If the argument is positive zero or negative zero, then the
     * result is the same as the argument.</ul>
     *
     * @param   a   a <code>double</code> value.
     * @return  the positive square root of <code>a</code>.
     *          If the argument is NaN or less than zero, the result is NaN.
     * @since   CLDC 1.1
     */
    public static native double sqrt(double a);

    /**
     * Returns the smallest (closest to negative infinity)
     * <code>double</code> value that is not less than the argument and is
     * equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical
     * integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or negative
     * zero, then the result is the same as the argument.
     * <li>If the argument value is less than zero but greater than -1.0,
     * then the result is negative zero.</ul>
     * Note that the value of <code>Math.ceil(x)</code> is exactly the
     * value of <code>-Math.floor(-x)</code>.
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value &lceil;&nbsp;<code>a</code>&nbsp;&rceil;.-->
     * @return  the smallest (closest to negative infinity)
     *          <code>double</code> value that is not less than the argument
     *          and is equal to a mathematical integer.
     * @since   CLDC 1.1
     */
    public static native double ceil(double a);

    /**
     * Returns the largest (closest to positive infinity)
     * <code>double</code> value that is not greater than the argument and
     * is equal to a mathematical integer. Special cases:
     * <ul><li>If the argument value is already equal to a mathematical
     * integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or
     * negative zero, then the result is the same as the argument.</ul>
     *
     * @param   a   a <code>double</code> value.
     * <!--@return  the value &lfloor;&nbsp;<code>a</code>&nbsp;&rfloor;.-->
     * @return  the largest (closest to positive infinity)
     *          <code>double</code> value that is not greater than the argument
     *          and is equal to a mathematical integer.
     * @since   CLDC 1.1
     */
    public static native double floor(double a);

    /**
     * Returns the absolute value of an <code>int</code> value.
     * If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * <p>
     * Note that if the argument is equal to the value of
     * <code>Integer.MIN_VALUE</code>, the most negative representable
     * <code>int</code> value, the result is that same value, which is
     * negative.
     *
     * @param   a   an <code>int</code> value.
     * @return  the absolute value of the argument.
     * @see     java.lang.Integer#MIN_VALUE
     */
    public static int abs(int a) {
        return (a < 0) ? -a : a;
    }

    /**
     * Returns the absolute value of a <code>long</code> value.
     * If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * <p>
     * Note that if the argument is equal to the value of
     * <code>Long.MIN_VALUE</code>, the most negative representable
     * <code>long</code> value, the result is that same value, which is
     * negative.
     *
     * @param   a   a <code>long</code> value.
     * @return  the absolute value of the argument.
     * @see     java.lang.Long#MIN_VALUE
     */
    public static long abs(long a) {
        return (a < 0) ? -a : a;
    }

    /**
     * Returns the absolute value of a <code>float</code> value.
     * If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * Special cases:
     * <ul><li>If the argument is positive zero or negative zero, the
     * result is positive zero.
     * <li>If the argument is infinite, the result is positive infinity.
     * <li>If the argument is NaN, the result is NaN.</ul>
     * In other words, the result is equal to the value of the expression:
     * <p><pre>Float.intBitsToFloat(0x7fffffff & Float.floatToIntBits(a))</pre>
     *
     * @param   a   a <code>float</code> value.
     * @return  the absolute value of the argument.
     * @since   CLDC 1.1
     */
    public static float abs(float a) {
        return (a <= 0.0F) ? 0.0F - a : a;
    }

    /**
     * Returns the absolute value of a <code>double</code> value.
     * If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * Special cases:
     * <ul><li>If the argument is positive zero or negative zero, the result
     * is positive zero.
     * <li>If the argument is infinite, the result is positive infinity.
     * <li>If the argument is NaN, the result is NaN.</ul>
     * In other words, the result is equal to the value of the expression:
     * <p><pre>Double.longBitsToDouble((Double.doubleToLongBits(a)<<1)>>>1)</pre>
     *
     * @param   a   a <code>double</code> value.
     * @return  the absolute value of the argument.
     * @since   CLDC 1.1
     */
    public static double abs(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    /**
     * Returns the greater of two <code>int</code> values. That is, the
     * result is the argument closer to the value of
     * <code>Integer.MAX_VALUE</code>. If the arguments have the same value,
     * the result is that same value.
     *
     * @param   a   an <code>int</code> value.
     * @param   b   an <code>int</code> value.
     * @return  the larger of <code>a</code> and <code>b</code>.
     * @see     java.lang.Long#MAX_VALUE
     */
    public static int max(int a, int b) {
        return (a >= b) ? a : b;
    }

    /**
     * Returns the greater of two <code>long</code> values. That is, the
     * result is the argument closer to the value of
     * <code>Long.MAX_VALUE</code>. If the arguments have the same value,
     * the result is that same value.
     *
     * @param   a   a <code>long</code> value.
     * @param   b   a <code>long</code> value.
     * @return  the larger of <code>a</code> and <code>b</code>.
     * @see     java.lang.Long#MAX_VALUE
     */
    public static long max(long a, long b) {
        return (a >= b) ? a : b;
    }

    private static long negativeZeroFloatBits = Float.floatToIntBits(-0.0f);
    private static long negativeZeroDoubleBits = Double.doubleToLongBits(-0.0d);

    /**
     * Returns the greater of two <code>float</code> values.  That is, the
     * result is the argument closer to positive infinity. If the
     * arguments have the same value, the result is that same value. If
     * either value is <code>NaN</code>, then the result is <code>NaN</code>.
     * Unlike the the numerical comparison operators, this method considers
     * negative zero to be strictly smaller than positive zero. If one
     * argument is positive zero and the other negative zero, the result
     * is positive zero.
     *
     * @param   a   a <code>float</code> value.
     * @param   b   a <code>float</code> value.
     * @return  the larger of <code>a</code> and <code>b</code>.
     */
    public static float max(float a, float b) {
        if (a != a) return a; // a is NaN
        if ((a == 0.0f) && (b == 0.0f)
            && (Float.floatToIntBits(a) == negativeZeroFloatBits)) {
            return b;
        }
        return (a >= b) ? a : b;
    }

    /**
     * Returns the greater of two <code>double</code> values.  That is, the
     * result is the argument closer to positive infinity. If the
     * arguments have the same value, the result is that same value. If
     * either value is <code>NaN</code>, then the result is <code>NaN</code>.
     * Unlike the the numerical comparison operators, this method considers
     * negative zero to be strictly smaller than positive zero. If one
     * argument is positive zero and the other negative zero, the result
     * is positive zero.
     *
     * @param   a   a <code>double</code> value.
     * @param   b   a <code>double</code> value.
     * @return  the larger of <code>a</code> and <code>b</code>.
     */
    public static double max(double a, double b) {
        if (a != a) return a; // a is NaN
        if ((a == 0.0d) && (b == 0.0d)
            && (Double.doubleToLongBits(a) == negativeZeroDoubleBits)) {
            return b;
        }
        return (a >= b) ? a : b;
    }

    /**
     * Returns the smaller of two <code>int</code> values. That is, the
     * result the argument closer to the value of <code>Integer.MIN_VALUE</code>.
     * If the arguments have the same value, the result is that same value.
     *
     * @param   a   an <code>int</code> value.
     * @param   b   an <code>int</code> value.
     * @return  the smaller of <code>a</code> and <code>b</code>.
     * @see     java.lang.Long#MIN_VALUE
     */
    public static int min(int a, int b) {
        return (a <= b) ? a : b;
    }

    /**
     * Returns the smaller of two <code>long</code> values. That is, the
     * result is the argument closer to the value of
     * <code>Long.MIN_VALUE</code>. If the arguments have the same value,
     * the result is that same value.
     *
     * @param   a   a <code>long</code> value.
     * @param   b   a <code>long</code> value.
     * @return  the smaller of <code>a</code> and <code>b</code>.
     * @see     java.lang.Long#MIN_VALUE
     */
    public static long min(long a, long b) {
        return (a <= b) ? a : b;
    }

    /**
     * Returns the smaller of two <code>float</code> values.  That is, the
     * result is the value closer to negative infinity. If the arguments
     * have the same value, the result is that same value. If either value
     * is <code>NaN</code>, then the result is <code>NaN</code>.  Unlike the
     * the numerical comparison operators, this method considers negative zero
     * to be strictly smaller than positive zero.  If one argument is
     * positive zero and the other is negative zero, the result is negative
     * zero.
     *
     * @param   a   a <code>float</code> value.
     * @param   b   a <code>float</code> value.
     * @return  the smaller of <code>a</code> and <code>b.</code>
     * @since   CLDC 1.1
     */
    public static float min(float a, float b) {
        if (a != a) return a; // a is NaN
        if ((a == 0.0f) && (b == 0.0f)
            && (Float.floatToIntBits(b) == negativeZeroFloatBits)) {
            return b;
        }
        return (a <= b) ? a : b;
    }

    /**
     * Returns the smaller of two <code>double</code> values.  That is, the
     * result is the value closer to negative infinity. If the arguments have
     * the same value, the result is that same value. If either value
     * is <code>NaN</code>, then the result is <code>NaN</code>.  Unlike the
     * the numerical comparison operators, this method considers negative zero
     * to be strictly smaller than positive zero. If one argument is
     * positive zero and the other is negative zero, the result is negative
     * zero.
     *
     * @param   a   a <code>double</code> value.
     * @param   b   a <code>double</code> value.
     * @return  the smaller of <code>a</code> and <code>b</code>.
     * @since   CLDC 1.1
     */
    public static double min(double a, double b) {
        if (a != a) return a; // a is NaN
        if ((a == 0.0d) && (b == 0.0d)
            && (Double.doubleToLongBits(b) == negativeZeroDoubleBits)) {
            return b;
        }
        return (a <= b) ? a : b;
    }

    /**
     * Returns the arc sine of an angle, in the range of -<i>pi</i>/2 through
     * <i>pi</i>/2. Special cases: 
     * <ul><li>If the argument is NaN or its absolute value is greater 
     * than 1, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the
     * same sign as the argument.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   the value whose arc sine is to be returned.
     * @return  the arc sine of the argument.
     */
    public static native double asin(double a);

    /**
     * Returns the arc cosine of an angle, in the range of 0.0 through
     * <i>pi</i>.  Special case:
     * <ul><li>If the argument is NaN or its absolute value is greater 
     * than 1, then the result is NaN.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results 
     * must be semi-monotonic.
     *
     * @param   a   the value whose arc cosine is to be returned.
     * @return  the arc cosine of the argument.
     */
    public static native double acos(double a);

    /**
     * Returns the arc tangent of an angle, in the range of -<i>pi</i>/2
     * through <i>pi</i>/2.  Special cases: 
     * <ul><li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the
     * same sign as the argument.</ul>
     * <p>
     * A result must be within 1 ulp of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   a   the value whose arc tangent is to be returned.
     * @return  the arc tangent of the argument.
     */
    public static native double atan(double a);

    /**
     * Converts rectangular coordinates (<code>x</code>,&nbsp;<code>y</code>)
     * to polar (r,&nbsp;<i>theta</i>).
     * This method computes the phase <i>theta</i> by computing an arc tangent
     * of <code>y/x</code> in the range of -<i>pi</i> to <i>pi</i>. Special 
     * cases:
     * <ul><li>If either argument is NaN, then the result is NaN. 
     * <li>If the first argument is positive zero and the second argument 
     * is positive, or the first argument is positive and finite and the 
     * second argument is positive infinity, then the result is positive 
     * zero. 
     * <li>If the first argument is negative zero and the second argument 
     * is positive, or the first argument is negative and finite and the 
     * second argument is positive infinity, then the result is negative zero. 
     * <li>If the first argument is positive zero and the second argument 
     * is negative, or the first argument is positive and finite and the 
     * second argument is negative infinity, then the result is the 
     * <code>double</code> value closest to <i>pi</i>. 
     * <li>If the first argument is negative zero and the second argument 
     * is negative, or the first argument is negative and finite and the 
     * second argument is negative infinity, then the result is the 
     * <code>double</code> value closest to -<i>pi</i>. 
     * <li>If the first argument is positive and the second argument is 
     * positive zero or negative zero, or the first argument is positive 
     * infinity and the second argument is finite, then the result is the 
     * <code>double</code> value closest to <i>pi</i>/2. 
     * <li>If the first argument is negative and the second argument is 
     * positive zero or negative zero, or the first argument is negative 
     * infinity and the second argument is finite, then the result is the 
     * <code>double</code> value closest to -<i>pi</i>/2. 
     * <li>If both arguments are positive infinity, then the result is the 
     * <code>double</code> value closest to <i>pi</i>/4. 
     * <li>If the first argument is positive infinity and the second argument 
     * is negative infinity, then the result is the <code>double</code> 
     * value closest to 3*<i>pi</i>/4. 
     * <li>If the first argument is negative infinity and the second argument 
     * is positive infinity, then the result is the <code>double</code> value 
     * closest to -<i>pi</i>/4. 
     * <li>If both arguments are negative infinity, then the result is the 
     * <code>double</code> value closest to -3*<i>pi</i>/4.</ul>
     * <p>
     * A result must be within 2 ulps of the correctly rounded result.  Results
     * must be semi-monotonic.
     *
     * @param   y   the ordinate coordinate
     * @param   x   the abscissa coordinate
     * @return  the <i>theta</i> component of the point
     *          (<i>r</i>,&nbsp;<i>theta</i>)
     *          in polar coordinates that corresponds to the point
     *          (<i>x</i>,&nbsp;<i>y</i>) in Cartesian coordinates.
     */
    public static native double atan2(double y, double x);
}
