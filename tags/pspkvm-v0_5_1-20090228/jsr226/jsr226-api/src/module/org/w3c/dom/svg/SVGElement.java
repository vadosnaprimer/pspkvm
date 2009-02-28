/*
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

package org.w3c.dom.svg;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

/**
 *
 */
public interface SVGElement extends Element, EventTarget
{

    /**
     *
     */
    public void setId(String Id)
		throws DOMException;

    /**
     *
     */
    public String getId();

    /**
     *
     */
    public Element getFirstElementChild();

    /**
     *
     */
    public Element getNextElementSibling();

    /**
     *
     */
    public String getTrait(String name)
		throws DOMException;

    /**
     *
     */
    public String getTraitNS(String namespaceURI, String name)
		throws DOMException;

    /**
     *
     */
    public float getFloatTrait(String name)
		throws DOMException;

    /**
     *
     */
    public SVGMatrix getMatrixTrait(String name)
		throws DOMException;

    /**
     *
     */
    public SVGRect getRectTrait(String name)
		throws DOMException;

    /**
     *
     */
    public SVGPath getPathTrait(String name)
		throws DOMException;

    /**
     *
     */
    public SVGRGBColor getRGBColorTrait(String name)
		throws DOMException;

    /**
     *
     */
    public void setTrait(String name, String value)
		throws DOMException;

    /**
     *
     */
    public void setTraitNS(String namespaceURI, String name, String value)
		throws DOMException;

    /**
     *
     */
    public void setFloatTrait(String name, float value)
		throws DOMException;

    /**
     *
     */
    public void setMatrixTrait(String name, SVGMatrix matrix)
		throws DOMException;

    /**
     *
     */
    public void setRectTrait(String name, SVGRect rect)
		throws DOMException;

    /**
     *
     */
    public void setPathTrait(String name, SVGPath path)
		throws DOMException;

    /**
     *
     */
    public void setRGBColorTrait(String name, SVGRGBColor color)
		throws DOMException;

}
