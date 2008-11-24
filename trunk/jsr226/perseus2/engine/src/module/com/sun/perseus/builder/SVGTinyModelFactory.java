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
package com.sun.perseus.builder;

import com.sun.perseus.model.Anchor;
import com.sun.perseus.model.Animate;
import com.sun.perseus.model.AnimateMotion;
import com.sun.perseus.model.AnimateTransform;
// import com.sun.perseus.model.Audio;
import com.sun.perseus.model.Defs;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.Ellipse;
import com.sun.perseus.model.Font;
import com.sun.perseus.model.FontFace;
import com.sun.perseus.model.Glyph;
import com.sun.perseus.model.Group;
import com.sun.perseus.model.HKern;
import com.sun.perseus.model.ImageNode;
import com.sun.perseus.model.Line;
import com.sun.perseus.model.LinearGradient;
import com.sun.perseus.model.RadialGradient;
import com.sun.perseus.model.Rect;
import com.sun.perseus.model.Set;
import com.sun.perseus.model.SolidColor;
import com.sun.perseus.model.SVG;
import com.sun.perseus.model.ShapeNode;
import com.sun.perseus.model.Stop;
import com.sun.perseus.model.StrictElement;
import com.sun.perseus.model.Switch;
import com.sun.perseus.model.Symbol;
import com.sun.perseus.model.Text;
import com.sun.perseus.model.TimedElementNode;
// import com.sun.perseus.model.TSpan;
import com.sun.perseus.model.Use;

import com.sun.perseus.util.SVGConstants;

import java.util.Vector;

/**
 * This ModelFactory implementation is initialized with all the handlers
 * necessary to handle SVG Tiny 1.1 content.
 * 
 * @version $Id: SVGTinyModelFactory.java,v 1.5 2006/04/21 06:36:12 st125089 Exp $
 */
public class SVGTinyModelFactory {
    /**
     * List of required attributes for foreignObject.
     */
    public static final String[] FOREIGN_OBJECT_REQUIRED_ATTRIBUTES = 
    {SVGConstants.SVG_WIDTH_ATTRIBUTE, SVGConstants.SVG_HEIGHT_ATTRIBUTE};

    /**
     * @param doc the document for which the prototypes are built.
     * @return a Vector with all the prototypes for SVG Tiny content.
     */
    public static Vector getPrototypes(final DocumentNode doc) {
        Vector v = new Vector();

        //
        // == Structure Module =================================================
        //
        v.addElement(new SVG(doc));
        v.addElement(new Group(doc));
        v.addElement(new Use(doc));
        v.addElement(new Defs(doc));
        v.addElement(new ImageNode(doc));
        v.addElement(new Switch(doc));
        v.addElement(new Symbol(doc));

        // 
        // == Shape Module =====================================================
        //
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_PATH_TAG));
        v.addElement(new Rect(doc));
        v.addElement(new Line(doc));
        v.addElement(new Ellipse(doc));
        v.addElement(new Ellipse(doc, true)); // <circle>
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_POLYGON_TAG));
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_POLYLINE_TAG));

        // 
        // == Text Module ======================================================
        //
        v.addElement(new Text(doc));
        // v.addElement(new TSpan(doc));

        //
        // == Font Module ======================================================
        //
        v.addElement(new Font(doc));
        v.addElement(new FontFace(doc));
        v.addElement(new Glyph(doc));
        v.addElement(new Glyph(doc, SVGConstants.SVG_MISSING_GLYPH_TAG));
        v.addElement(new HKern(doc)); 

        // 
        // == Hyperlinking Module ==============================================
        //
        v.addElement(new Anchor(doc));

        // 
        // == Animation Module =================================================
        //
        v.addElement(new Animate(doc));
        v.addElement(new AnimateMotion(doc));
        v.addElement(new Set(doc));
        v.addElement(new AnimateTransform(doc));
        v.addElement(new Animate(doc, SVGConstants.SVG_ANIMATE_COLOR_TAG));
        
        //
        // == SolidColor Module ================================================
        //
        v.addElement(new SolidColor(doc));

        //
        // == Gradient Module ================================================
        //
        v.addElement(new LinearGradient(doc));
        v.addElement(new RadialGradient(doc));
        v.addElement(new Stop(doc));

        //
        // == Extensibility Module =========================================
        //
        v.addElement(new StrictElement(doc, 
                                       SVGConstants.SVG_FOREIGN_OBJECT_TAG,
                                       SVGConstants.SVG_NAMESPACE_URI,
                                       FOREIGN_OBJECT_REQUIRED_ATTRIBUTES,
                                       null));
        //
        // == Medial Module ================================================
        //
        // v.addElement(new Audio(doc));

        return v;
    }
}
