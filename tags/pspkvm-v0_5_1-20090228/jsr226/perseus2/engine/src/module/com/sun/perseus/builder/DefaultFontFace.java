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

import com.sun.perseus.model.FontFace;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.UpdateAdapter;
import com.sun.perseus.model.DocumentNode;

import com.sun.perseus.platform.ResourceHandler;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

/**
 * This helper class encapsulates the creation of a default <tt>FontFace</tt>
 * which a renderer can use. <br />
 *
 * This class tries to load an SVG file containing a font. The resource
 * identifier for the default font is: <br />
 * <tt>com/sun/perseus/renderer/resources/defaultFont.svg</tt><br />
 *
 * <b>Note</b>: the <tt>defaultFont.svg</tt> is loaded and its first
 * <tt>&lt;font&gt;</tt> element is loaded. The first <tt>&lt;font&gt;</tt> 
 * element must be the first child of the root <tt>&lt;svg&gt;</tt> element. Any
 * other structure for the <tt>defaultFont.svg</tt> file will result in an 
 * <tt>Error</tt>.
 * 
 * This helper class also encapsulates the creation and loading of a set 
 * of <tt>FontFaces</tt> for the initial font-family value. The fonts are 
 * expected to all be in a single initial font file: <br />
 * <tt>com/sun/perseus/renderer/resources/initialFont.svg</tt>
 *
 *
 * <b>Note</b>: the <tt>initialFont.svg</tt> is loaded and all the 
 * children of the root <tt>&lt;svg&gt;</tt> element are expected to
 * be <tt>&lt;font&gt;</tt> elements. Any
 * other structure for the <tt>initialFont.svg</tt> file will result in an 
 * <tt>Error</tt>.
 * 
 * @see com.sun.perseus.render.RenderGraphics
 *
 * @version $Id: DefaultFontFace.java,v 1.6 2006/07/17 00:35:43 st125089 Exp $
 */
public final class DefaultFontFace {

    /**
     * The default font face is cached so that it is loaded only
     * once
     */
    protected static FontFace defaultFontFace;

    /**
     * The set of initial font faces is cached so that it is loaded
     * only once.
     */
    protected static FontFace[] initialFontFaces;


    /**
     * @return the default font face. This font face is loaded from the
     *         {@link #ResourceHandler.DEFAULT_FONT_FACE_FILE 
     *          ResourceHandler.DEFAULT_FONT_FACE} resource.
     */
    public static FontFace getDefaultFontFace() {
        if (defaultFontFace == null) {
            loadDefaultFontFace();
        }
        return defaultFontFace;
    }

    /**
     * @return the initial font faces. The initial font faces are
     *         loaded from the 
     *         {@link #ResourceHandler.INITIAL_FONT_FACE_FILE 
     *          ResourceHandler.INITIAL_FONT_FACE_FILE} resource.
     */
    public static FontFace[] getInitialFontFaces() {
        if (initialFontFaces == null) {
            loadInitialFontFaces();
        }
        return initialFontFaces;
    }

    /**
     * Loads the initial font faces. This assumes that the font face file
     * has not been loaded already. If the load of the font face fails, the 
     * application throws an error.
     *
     * @throws Error if there is an error while loading the file. An error 
     *         is generated.
     * @see #getInitialFontFaces
     */
    static void loadInitialFontFaces() throws Error {
        try {
            InputStream is = 
                ResourceHandler.getInitialFontResource();
            if (is == null) {
                throw new Exception
                    (Messages.formatMessage
                     (Messages.ERROR_CANNOT_LOAD_RESOURCE,
                      new Object[] {"Initial Font"}));
            }

            ModelNode svg = loadSVGFontResource(is);

            // Now, get the FontFace from the tree. 
            // vp -> svg -> font[i] -> font-face
            
            // Count the font children
            int n = 0;
            ModelNode c = svg.getFirstChildNode();
            while (c != null) {
                n++;
                c = c.getNextSiblingNode();
            }
            initialFontFaces = new FontFace[n];
            ModelNode font = svg.getFirstChildNode();
            for (int i = 0; i < initialFontFaces.length; i++) {
                initialFontFaces[i] = (FontFace) font.getFirstChildNode();
                font = font.getNextSiblingNode();
            }
        } catch (Exception e) {
            // Do not tolerate any error as this is critical for the operation
            // of the toolkit.
            e.printStackTrace();
            throw new Error();
        }
    }

    /**
     * Loads the default font face. This assumes that the font face file
     * has not been loaded already. If the load of the font face fails, the 
     * application throws an error.
     *
     * @throws Error if there is an error while loading the default font 
     *         face file. 
     * @see #getDefaultFontFace
     */
    static void loadDefaultFontFace() throws Error {
        try {
            InputStream is = 
                ResourceHandler.getDefaultFontResource();
            if (is == null) {
                throw new Exception
                    (Messages.formatMessage
                     (Messages.ERROR_CANNOT_LOAD_RESOURCE,
                      new Object[] {"Default Font"}));
            }

            ModelNode svg = loadSVGFontResource(is);

            // Now, get the FontFace from the tree. 
            // svg -> font -> font-face
            defaultFontFace = (FontFace)
                svg.getFirstChildNode().getFirstChildNode();
            
        } catch (Exception e) {
            // Do not tolerate any error as this is critical for the operation
            // of the toolkit.
            e.printStackTrace();
            throw new Error();
        }
    }

    /**
     * Helper method: loads an SVG Font resource file
     * @param svgResource the SVG Font resource file
     * @return The root <tt>&lt;svg&gt;</tt> element representing the 
     *         resource file.
     * @throws Exception if an error happens while loading the resource
     */
    protected static ModelNode loadSVGFontResource(final InputStream is) 
        throws Exception {
        // We use JAXP to get a SAX 2 Parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        
        // Configure a ModelBuilder to turn an SVG document 
        // into a ModelNode tree
        DocumentNode root = new DocumentNode();
        UpdateAdapter ul = new UpdateAdapter();
        root.setUpdateListener(ul);
        ModelBuilder modelBuilder = new ModelBuilder(null, root);
        
        ul.loadStarting(root, is);

        try {
            parser.parse(is, modelBuilder);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
            }

            int n = modelBuilder.entityStreams.size();
            for (int i = 0; i < n; i++) {
                Reader r = (Reader) modelBuilder.entityStreams.elementAt(i);
                try {
                    r.close();
                } catch (IOException ioe) {
                    // Do nothing: this means the stream was 
                    // closed by the SAX parser.
                }
            }
        }

        if (!ul.loadSuccess()) {
            throw new Exception
                (Messages.formatMessage
                    (Messages.ERROR_CANNOT_LOAD_RESOURCE,
                     new Object[] {"SVG System Font Resource"}));
        }

        // trace(modelBuilder.getModelRoot(), "");
        return modelBuilder.getModelRoot().getFirstChildNode();
    }

    /**
     * Private default constructor to prevent instantiation of this 
     * utility class
     */
    private DefaultFontFace() {
    }
}
