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
package com.sun.perseus.j2d;

import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;

import com.sun.perseus.platform.MathSupport;

/**
 * Class for 2D transforms.
 *
 * @version $Id: Transform.java,v 1.8 2006/04/21 06:35:16 st125089 Exp $
 */
public class Transform implements SVGMatrix {
    /**
     * The (0, 0) component of this 3x3 transformation matrix.
     */
    float m0;

    /**
     * The (1, 0) component of this 3x3 transformation matrix.
     */
    float m1;

    /**
     * The (0, 1) component of this 3x3 transformation matrix.
     */
    float m2;

    /**
     * The (1, 1) component of this 3x3 transformation matrix.
     */
    float m3;

    /**
     * The (0, 2) component of this 3x3 transformation matrix.
     */
    float m4;

    /**
     * The (1, 2) component of this 3x3 transformation matrix.
     */
    float m5;

    /**
     * Constructs an <code>Transform</code> with a given set of
     * components, representing the transform:
     *
     * <pre>
     * x' = m0*x + m2*y + m4
     * y' = m1*x + m3*y + m5
     * </pre>
     *
     * or, representing the matrix:
     *
     * <pre>
     *    [ m0 m2 m4 ]
     *    [ m1 m3 m5 ]
     *    [ 0  0  1  ]
     * </pre>
     *
     * @param m0 (0, 0) matrix value
     * @param m1 (1, 0) matrix value
     * @param m2 (0, 1) matrix value
     * @param m3 (1, 1) matrix value
     * @param m4 (0, 2) matrix value
     * @param m5 (1, 2) matrix value
     */
    public Transform(final float m0, final float m1, final float m2,
                     final float m3, final float m4, final float m5) {
        setTransform(m0, m1, m2, m3, m4, m5);
    }

    /**
     * Sets the transform to the matrix defined by the input parameters:
     * 
     * <pre>
     *    [ m0 m2 m4 ]
     *    [ m1 m3 m5 ]
     *    [ 0  0  1  ]
     * </pre>
     *
     * @param m0 (0, 0) matrix value
     * @param m1 (1, 0) matrix value
     * @param m2 (0, 1) matrix value
     * @param m3 (1, 1) matrix value
     * @param m4 (0, 2) matrix value
     * @param m5 (1, 2) matrix value
     */
    public void setTransform(final float m0, final float m1, final float m2,
                             final float m3, final float m4, final float m5) {
        this.m0 = m0;
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.m4 = m4;
        this.m5 = m5;
    }

    /**
     * Sets the transform to the value defined by the input matrix. 
     *
     * @param transform the transform whose value should be copied. If null,
     * this transform is set to identity.
     */
    public void setTransform(final SVGMatrix transform) {
        Transform txf = (Transform) transform;

        if (txf == null) {
            m0 = 1;  // create IDENTITY transformation
            m1 = 0;
            m2 = 0;
            m3 = 1;
            m4 = 0;
            m5 = 0;
            return;
        }

        this.m0 = txf.m0;
        this.m1 = txf.m1;
        this.m2 = txf.m2;
        this.m3 = txf.m3;
        this.m4 = txf.m4;
        this.m5 = txf.m5;
    }

    /**
     * Transforms a point's coordinates.
     *
     * @param pt the point to transform
     * @param opt the transformed point coordinates.
     */
    public void transformPoint(final float[] pt,
                               final float[] opt) {
        opt[0] = pt[0] * m0 + pt[1] * m2 + m4;
        opt[1] = pt[0] * m1 + pt[1] * m3 + m5;
    }

    /**
     * Constructs an <code>Transform</code> representing the same
     * transform as the given <code>Transform</code>.  Future changes
     * to the object supplied as the <code>transform</code> parameter
     * will not affect the newly constructed <code>Transform</code>.
     * If <code>transform</code> is null, the matrix is initialized
     * to be the identity matrix.
     *
     * @param transform defines the initial
     *        state for the newly constructed <code>Transform</code>.
     */
    public Transform(final SVGMatrix transform) {
        setTransform(transform);
    }

    /**
     * The components of the matrix denoted a to f in the API are :
     *
     *    [ a c e ]
     *    [ b d f ]
     *    [ 0 0 1 ]
     *
     * @param index component index for this matrix
     * @return the component of the matrix corresponding to the index.
     * @throws DOMException  - INDEX_SIZE_ERR if the <code>index</code> is 
     * invalid.
     */
    public float getComponent(final int index) throws DOMException {
        switch (index) {
          case 0:
              return m0;
          case 1:
              return m1;
          case 2:
              return m2;
          case 3:
              return m3;
          case 4:
              return m4;
          case 5:
              return m5;
          default:
              throw new DOMException
                  (DOMException.INDEX_SIZE_ERR, 
                   Messages.formatMessage
                   (Messages.ERROR_OUT_OF_BOUND_PARAMETER_VALUE,
                    new String[] {"SVGMatrix",
                                  "getComponent",
                                  "index",
                                  "" + index}));
        }
    }

    /**
     *
     */
    public SVGMatrix mMultiply(final SVGMatrix secondMatrix) 
                 throws NullPointerException {
        if (secondMatrix == null) {
            throw new NullPointerException();
        }

        Transform sm = (Transform) secondMatrix;

        float t0 = sm.m0;
        float t1 = sm.m1;
        float t2 = sm.m2;
        float t3 = sm.m3;
        float t4 = sm.m4;
        float t5 = sm.m5;

        float mM0 = m0;
        float mM1 = m2;
        m0  = t0 * mM0 + t1 * mM1;
        m2  = t2 * mM0 + t3 * mM1;
        m4 += t4 * mM0 + t5 * mM1;

        mM0 = m1;
        mM1 = m3;
        m1  = t0 * mM0 + t1 * mM1;
        m3  = t2 * mM0 + t3 * mM1;
        m5 += t4 * mM0 + t5 * mM1;

        return this;
    }

    /**
     * Returns the inverse matrix.
     *
     * @return the inverted matrix.
     * @throws SVGException  - SVG_MATRIX_NOT_INVERTABLE when determinant 
     * of this matrix is zero.
     */
    public SVGMatrix inverse() throws SVGException {
        Transform svm = new Transform(1, 0, 0, 1, 0, 0);
        inverse(svm);
        return svm;
    }

    /**
     * Returns true if the matrix is invertible.
     *
     * @return true if the matrix is invertible.
     */
    public boolean isInvertible() {
        return (m0 * m3 - m1 * m2) != 0;
    }

    /**
     * Inverses this transform and stores the resulting transform into 
     * the input transform.
     *
     * @param txf the SVGMatrix into which the result should be stored.
     * @throws SVGException  - SVG_MATRIX_NOT_INVERTABLE when determinant 
     * of this matrix is zero.
     */
    public SVGMatrix inverse(SVGMatrix txf) throws SVGException {
        Transform svm = (Transform) txf;

        float det = m0 * m3 - m2 * m1;
        if (MathSupport.abs(det) <= Float.MIN_VALUE) {
            throw new SVGException(SVGException.SVG_MATRIX_NOT_INVERTABLE, 
                                   this.toString());
        }

        if (svm == null) {
            svm = new Transform(1, 0, 0, 1, 0, 0);
        }
        
        if (isIdentity()) {
            svm.setTransform(1, 0, 0, 1, 0, 0);
            return svm;
        }

        svm.m0 = m3 / det;
        svm.m2 = -m2 / det;
        svm.m4 = (m2 * m5 - m3 * m4) / det;
        svm.m1 = -m1 / det;
        svm.m3 = m0 / det;
        svm.m5 = (m1 * m4 - m0 * m5) / det;

        return svm;
    }


    /**
     *
     */
    public SVGMatrix mTranslate(final float x, final float y) {
        m4 = x * m0 + y * m2 + m4;
        m5 = x * m1 + y * m3 + m5;

        return this;
    }

    /**
     *
     */
    public SVGMatrix mScale(final float scaleFactor) {
        m0 *= scaleFactor;
        m2 *= scaleFactor;
        m1 *= scaleFactor;
        m3 *= scaleFactor;
        return this;
    }

    /**
     * Post-multiplies a scale transformation on the current matrix and
     * returns the resulting matrix.  
     * <p>
     * <pre>
     *                [   scaleFactorX      0          0   ]
     *                [   0          scaleFactorY      0   ]
     *                [   0                 0          1   ]
     * </pre>
     * </p>
     *
     * @param scaleFactorX the factor by which coordinates are scaled along the
     *                    X axis direction.
     * @param scaleFactorY the factor by which coordinates are scaled along the
     *                    Y axis direction.
     * @return this matrix scaled by the scaleFactor.
     */
    public SVGMatrix mScale(final float scaleFactorX, 
                            final float scaleFactorY) {
        m0 *= scaleFactorX;
        m2 *= scaleFactorY;
        m1 *= scaleFactorX;
        m3 *= scaleFactorY;
        return this;
    }

    /**
     *
     */
    public SVGMatrix mRotate(final float angle) {
        final float angl = MathSupport.toRadians(angle);
        final float trigTOLERANCE = 1E-7f;
        final float sin = MathSupport.sin(angl);
        final float cos = MathSupport.cos(angl);

        if (MathSupport.abs(sin) < trigTOLERANCE) {  // angle = 0 or 180 deg.
            if (cos < 0f) {         // angle = 180 deg.
                m0 = -m0;
                m3 = -m3;
                m2 = -m2;
                m1 = -m1;
            }
            return this;
        }

        if (MathSupport.abs(cos) < trigTOLERANCE) {  // angle = 90 or 270 deg.
            if (sin < 0f) {         // angle = 270 deg
                float mM0 = m0;
                m0  = -m2;
                m2  = mM0;
                mM0 = m1;
                m1  = -m3;
                m3  = mM0;
            } else {                 // angle = 90 deg
                float mM0 = m0;
                m0  = m2;
                m2  = -mM0;
                mM0 = m1;
                m1  = m3;
                m3  = -mM0;
           }
           return this;
        }

        float mM0, mM1;
        mM0 = m0;
        mM1 = m2;
        m0 =  cos * mM0 + sin * mM1;
        m2 = -sin * mM0 + cos * mM1;
        mM0 = m1;
        mM1 = m3;
        m1 =  cos * mM0 + sin * mM1;
        m3 = -sin * mM0 + cos * mM1;

        return this;
    }

    /**
     * Returns <code>true</code> if the current <code>SVGMatrix</code>
     * is equivalent to the identity matrix, <code>false</code>
     * otherwise.
     *
     * @return true if this matrix is identity, false otherwise.
     */
    public boolean isIdentity() {
        return (m0 == 1f 
                &&
                m2 == 0f 
                &&
                m4 == 0f 
                &&
                m1 == 0f 
                &&
                m3 == 1f 
                &&
                m5 == 0f);
    }

    /**
     * @return true if obj is a Transform and all its components are the 
     *         same as this instance.
     */
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Transform)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Transform t = (Transform) obj;
        return t.m0 == m0
            &&
            t.m1 == m1
            &&
            t.m2 == m2
            &&
            t.m3 == m3
            &&
            t.m4 == m4
            &&
            t.m5 == m5;
    }

    /**
     * @param a 6x1 float array containing the transform matrix.
     * @return true if this transform is equal to the input matrix.
     */
    public boolean equals(final float[][] m) {
        return m[0][0] == m0
            &&
            m[1][0] == m1
            &&
            m[2][0] == m2
            &&
            m[3][0] == m3
            &&
            m[4][0] == m4
            &&
            m[5][0] == m5;
    }
}
