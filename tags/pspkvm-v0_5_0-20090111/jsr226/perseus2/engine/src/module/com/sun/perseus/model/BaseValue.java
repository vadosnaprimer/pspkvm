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

package com.sun.perseus.model;

/**
 * The <code>BaseValue</code> interface is used to abstract either the
 * original value of a trait (like 'x' on a &lt;rect&gt;) or a pseudo-trait
 * (like '#text' on a &lt;text&gt; or '#motion' on <code>SVGLocatable</code>).
 * It is implemented by <code>TraitAnim</code> and <code>Animation</code>
 *
 * @version $Id: BaseValue.java,v 1.4 2006/06/29 10:47:29 ln156897 Exp $
 */
public interface BaseValue {
    /**
     * Returns the BaseValue as an array of objects. The dimensions of the
     * returned value depend on the number of components in the trait. There are
     * as many values as there are 'components' in the value. For example,
     * a stroke-dash array trait value has as many components as there are dask
     * lengths in the value.  An SVGMatrix trait has six components. 
     * A coordinate trait has a single component.  
     * 
     * Then each object value can be a String or a float array. In the case of
     * float arrays, each float array has as many entries as there are
     * dimensions in the component value. For example, each stroke-dash array
     * component has only one entry, because there is only on dimension for each
     * dash or gap length.  An RGB trait has 3 dimensions (one for r, one for g
     * and one for b).
     *
     * The following table summarizes the trait types and their number of
     * components and dimensions:
     * <table>
     *   <th>
     *     <td>Trait Type</td>
     *     <td># components</td>
     *     <td># dimensions</td>
     *   </th>
     *   <tr>
     *     <td>String</td>
     *     <td>1</td>
     *     <td>NA</td>
     *   </tr>
     *   <tr>
     *     <td>Number</td>
     *     <td>1</td>
     *     <td>1</td>
     *   </tr>
     *   <tr>
     *     <td>Length/Coordinate</td>
     *     <td>1</td>
     *     <td>1</td>
     *   </tr>
     *   <tr>
     *     <td>List of XXX</td>
     *     <td>Number of entries in the list</td>
     *     <td>Same number of components as list entries.</td>
     *   </tr>
     *   <tr>
     *     <td>Angle</td>
     *     <td>1</td>
     *     <td>1</td>
     *   </tr>
     *   <tr>
     *     <td>Color</td>
     *     <td>1</td>
     *     <td>3</td>
     *   </tr>
     *   <tr>
     *     <td>transform list</td>
     *     <td>1</td>
     *     <td>6</td>
     *   </tr>
     *   <tr>
     *     <td>URI</td>
     *     <td>NA</td>
     *     <td>NA</td>
     *   </tr>
     * </table>
     * @return the base value as an Object array. The dimensions of the
     *         returned array depend on the trait.
     */
    Object[] getBaseValue();
}
