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

import org.w3c.dom.svg.SVGPath;

/**
 * Class for Path Data.
 *
 * @version $Id: Path.java,v 1.7 2006/04/21 06:35:11 st125089 Exp $
 */
public class Path implements SVGPath {
    /**
     * The event-odd winding rule.
     */
    public static final int WIND_EVEN_ODD = 0;

    /**
     * The non-zero winding rule.
     */
    public static final int WIND_NON_ZERO = 1;

    /**
     * The default initial number of commands.
     */
    static final int INITIAL_COMMAND_CAPACITY = 10;

    /**
     * The default initial number of data entries.
     */
    static final int INITIAL_DATA_CAPACITY = 40;

    /**
     * The internal value for moveTo commands.
     */
    public static final byte MOVE_TO_IMPL = 0;
    
    /**
     * The internal value for lineTo commands
     */
    public static final byte LINE_TO_IMPL = 1;
    
    /**
     * The internal value for quadTo commands
     */
    public static final byte QUAD_TO_IMPL = 2;
    
    /**
     * The internal value for curveTo commands.
     */
    public static final byte CURVE_TO_IMPL = 3;
    
    /**
     * The internal value for close commands.
     */
    public static final int CLOSE_IMPL = 4;

    /**
     * Delta offset for each command type.
     * 2 for moveto and lineto.
     * 4 for quadto.
     * 6 for curveto.
     * 0 for close.
     */
    public static final int[] COMMAND_OFFSET = {2, 2, 4, 6, 0};
    
    /**
     * An array storing the path command types. One of MOVE_TO_IMPL,
     * LINE_TO_IMPL, QUAD_TO_IMPL, CURVE_TO_IMPL.
     */
    protected byte[] commands;

    /**
     * An array storing the path data. The array is a list of even
     * length made of consecutive x/y coordinate pairs.
     */
    protected float[] data;

    /**
     * The current number of segments.
     */
    protected int nSegments;

    /**
     * The number of used entries in the data array.
     */
    protected int nData;

    /**
     * Keeps track of the last requested command index.
     */
    protected int lastCmdIndex;

    /**
     * The offset in the data array for the last requested command.
     * This is used to minimize the computation of the offset 
     * in getSegmentParam.
     */
    protected int lastOffset;

    /**
     *
     */
    public int getNumberOfSegments() {
        return nSegments;
    }

    /**
     * Default constructor. The initial capacity is defined by 
     * the INITIAL_COMMAND_CAPACITY and INITIAL_DATA_CAPACITY
     * static variables.
     */
    public Path() {
        this(INITIAL_COMMAND_CAPACITY, INITIAL_DATA_CAPACITY);
    }

    /**
     * Initial constructor. The initial capacity is defined by
     * the capacity parameters.
     *
     * @param commandCapacity the number of intended commands for 
     *        this object. Must be positive or zero.
     * @param dataCapacity the number of intended data parameters
     *        for this object. Must be positive or zero.
     */
    public Path(final int commandCapacity,
                final int dataCapacity) {
        commands = new byte[commandCapacity];
        data = new float[dataCapacity];
    }

    /**
     * Copy constructor.
     *
     * @param model the Path object to duplicate.
     */
    public Path(final Path model) {
        if (model != null) {
            // Copy path data.
            this.data = new float[model.data.length];
            System.arraycopy(model.data, 0, data, 0, model.data.length);
            
            // Copy command data
            this.commands = new byte[model.commands.length];
            System.arraycopy(model.commands, 0, commands, 0, model.commands.length);
            
            this.nData = model.nData;
            this.nSegments = model.nSegments;
        } // else, use default values
    }

    /**
     * @return this path's commands data.
     */
    public byte[] getCommands() {
        return commands;
    }

    /**
     * @return this path's data.
     */
    public float[] getData() {
        return data;
    }

    /**
     * @param newData the new path data. The Path is only guaranteed to work after
     *        this method is invoked if the input data array has at least as many
     *        entries as the current path data array and if each entry is a float 
     *        array of at least two values.
     */
    public void setData(final float[] newData) {
        if (nData == 0) {
            return;
        }

        this.data = newData;
    }

    /*
     * Converts the internal command value to the SVGPath command type.
     *
     * @param command the internal command value, one of the XYZ_IMPL
     *        values.
     * @return one of the SVGPath command constants.
     */
    static short toSVGPathCommand(final int command) {
        switch (command) {
        case MOVE_TO_IMPL:
            return SVGPath.MOVE_TO;
        case LINE_TO_IMPL:
            return SVGPath.LINE_TO;
        case QUAD_TO_IMPL:
            return SVGPath.QUAD_TO;
        case CURVE_TO_IMPL:
            return SVGPath.CURVE_TO;
        case CLOSE_IMPL:
        default:
            return SVGPath.CLOSE;
        }
    }

    /**
     *
     */
    public short getSegment(final int cmdIndex) throws DOMException {
        if (cmdIndex >= nSegments || cmdIndex < 0) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        }

        return toSVGPathCommand(commands[cmdIndex]);
    }

    /**
     *
     */
    public float getSegmentParam(final int cmdIndex, final int paramIndex)
                   throws DOMException {
        if (cmdIndex >= nSegments || cmdIndex < 0 || paramIndex < 0) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        }

        switch (commands[cmdIndex]) {
        case CLOSE_IMPL:
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        case LINE_TO_IMPL:
        case MOVE_TO_IMPL:
            if (paramIndex > 1) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            break;
        case QUAD_TO_IMPL:
            if (paramIndex > 3) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            break;
        case CURVE_TO_IMPL:
            if (paramIndex > 5) {
                throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
            }
            break;
        default:
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        }

        return data[checkOffset(cmdIndex) + paramIndex];
    }

    /**
     * Implementation helper. Sets the lastCmdIndex to the requested index
     * after computing the offset corresponding to that index.
     *
     * @param cmdIndex the new index for which the offset should be computed.
     */
    final int checkOffset(final int cmdIndex) {
        if (cmdIndex == lastCmdIndex) {
            return lastOffset;
        }

        if (cmdIndex > lastCmdIndex) {
            // The new index is _after_ the previous one. Add offsets for all
            // commands between the two indices.
            for (int ci = lastCmdIndex; ci < cmdIndex; ci++) {
                lastOffset += COMMAND_OFFSET[commands[ci]];
            }
        } else {
            // The next index is _before_ the previous one. Remove offsets for
            // all commands, between the two indices.
            for (int ci = lastCmdIndex - 1; ci >= cmdIndex; ci--) {
                lastOffset -= COMMAND_OFFSET[commands[ci]];
            }
        }

        lastCmdIndex = cmdIndex;
        return lastOffset;
    }

    /**
     * Adjust the internal arrays for the requested number of points.
     *
     * @param nParams the number of points to add to the array.
     */
    void newCommand(final int nPoints) {
        // Adjust the length of the command array if needed.
        if (nSegments == commands.length) {
            byte[] tmpCommands = new byte[commands.length * 2 + 1];
            System.arraycopy(commands, 0, tmpCommands, 0, commands.length);
            commands = tmpCommands;
        }

        // Adjust the length of the data array if needed.
        if (nData + nPoints * 2 > data.length) {
            float[] tmpData = new float[(nData + nPoints * 2) * 2];
            System.arraycopy(data, 0, tmpData, 0, data.length);
            data = tmpData;
        }
    }

    /**
     *
     */
    public void moveTo(final float x, final float y) {
        newCommand(1);
        commands[nSegments] = MOVE_TO_IMPL;
        data[nData] = x;
        data[nData + 1] = y;

        nSegments++;
        nData += 2;
    }

    /**
     *
     */
    public void lineTo(final float x, final float y) {
        newCommand(1);
        commands[nSegments] = LINE_TO_IMPL;
        data[nData] = x;
        data[nData + 1] = y;

        nSegments++;
        nData += 2;
    }

    /**
     *
     */
     public void quadTo(final float x1, final float y1, 
                        final float x2, final float y2) {
        newCommand(2);
        commands[nSegments] = QUAD_TO_IMPL;
        data[nData] = x1;
        data[nData + 1] = y1;
        data[nData + 2] = x2;
        data[nData + 3] = y2;

        nSegments++;
        nData += 4;
     }

    /**
     *
     */
    public void curveTo(final float x1, final float y1, 
                        final float x2, final float y2, 
                        final float x3, final float y3) {
        newCommand(3);
        commands[nSegments] = CURVE_TO_IMPL;

        data[nData] = x1;
        data[nData + 1] = y1;
        data[nData + 2] = x2;
        data[nData + 3] = y2;
        data[nData + 4] = x3;
        data[nData + 5] = y3;

        nSegments++;
        nData += 6;
    }

    /**
     *
     */
    public void close() {
        newCommand(0);
        commands[nSegments] = CLOSE_IMPL;

        nSegments++;
    }

    /**
     * @return a String representation of this path, using the SVG notation.
     */
    public String toString() {
        return toString(data);
    }

    /**
     * @param d the set of data values to use for the string conversion. This
     *     will fail if the input data array does not have at least nData 
     *     entries of at least 2 values.
     * @return a String representation of this path, using the SVG notation.
     */
    public String toString(final float[] d) {
        StringBuffer sb = new StringBuffer();
        int offset = 0;
        for (int i = 0; i < nSegments; i++) {
            switch (commands[i]) {
            case Path.MOVE_TO_IMPL:
                sb.append('M');
                sb.append(d[offset]);
                sb.append(',');
                sb.append(d[offset + 1]);
                offset += 2;
                break;
            case Path.LINE_TO_IMPL:
                sb.append('L');
                sb.append(d[offset]);
                sb.append(',');
                sb.append(d[offset + 1]);
                offset += 2;
                break;
            case Path.QUAD_TO_IMPL:
                sb.append('Q');
                sb.append(d[offset]);
                sb.append(',');
                sb.append(d[offset + 1]);
                sb.append(',');
                sb.append(d[offset + 2]);
                sb.append(',');
                sb.append(d[offset + 3]);
                offset += 4;
                break;
            case Path.CURVE_TO_IMPL:
                sb.append('C');
                sb.append(d[offset]);
                sb.append(',');
                sb.append(d[offset + 1]);
                sb.append(',');
                sb.append(d[offset + 2]);
                sb.append(',');
                sb.append(d[offset + 3]);
                sb.append(',');
                sb.append(d[offset + 4]);
                sb.append(',');
                sb.append(d[offset + 5]);
                offset += 6;
                break;
            case Path.CLOSE_IMPL:
                sb.append('Z');
                break;
            default:
                throw new Error();
            }
        }

        return sb.toString();
    }

    /**
     * @return a string descriton of this Path using the points syntax. This
     * methods throws an IllegalStateException if the path does not have the 
     * commands corresponding to the points syntax (initial move to followed
     * by linetos and closes
     */
    public String toPointsString() {
        return toPointsString(data);
    }

    /**
     * @param d the set of data values to use for the string conversion. This
     * will fail if the input data array does not have an even number of values.
     * @return a string descriton of this Path using the points syntax. This
     * methods throws an IllegalStateException if the path does not have the
     * commands corresponding to the points syntax (initial move to followed by
     * linetos and closes
     */
    public String toPointsString(final float[] d) {
        StringBuffer sb = new StringBuffer();
        int curSegment = 0;
        int off = 0, cmd = 0;

        while (curSegment < nSegments) {
            switch(commands[curSegment]) {
            case Path.MOVE_TO_IMPL:
                if (curSegment > 0) {
                    throw new IllegalArgumentException();
                }
                sb.append(d[off]);
                sb.append(',');
                sb.append(d[off + 1]);
                sb.append(' ');
                off += 2;
                break;
            case Path.LINE_TO_IMPL:
                sb.append(d[off]);
                sb.append(',');
                sb.append(d[off + 1]);
                sb.append(' ');
                off += 2;
                break;
            case Path.CLOSE_IMPL:
                break;
            default:
            case Path.QUAD_TO_IMPL:
            case Path.CURVE_TO_IMPL:
                throw new IllegalArgumentException();
            }
            
            curSegment++;
        }
        return sb.toString().trim();
    }

    /**
     * Compute the path's bounds.
     *
     * @return the path's bounds.
     */
    public Box getBounds() {
        float x1, y1, x2, y2;
        int i = nData - 2;
        if (nData > 0) {
            x1 = x2 = data[i];
            y1 = y2 = data[i + 1];
            i -= 2;
            while (i >= 0) {
                float x = data[i];
                float y = data[i + 1];
                i -= 2;
                if (x < x1) x1 = x;
                if (y < y1) y1 = y;
                if (x > x2) x2 = x;
                if (y > y2) y2 = y;
            }
        } else {
            x1 = y1 = x2 = y2 = 0.0f;
        }
        
        return new Box(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Compute the path's bounds in the transformed coordinate system.
     */
    public Box getTransformedBounds(final Transform t) {
        float x1, y1, x2, y2;
        int i = nData - 2;
        float x0, y0, x, y;
        if (nData > 0) {
            x0 = data[i];
            y0 = data[i + 1];
            x1 = x2 = x0 * t.m0 + y0 * t.m2 + t.m4;
            y1 = y2 = x0 * t.m1 + y0 * t.m3 + t.m5;
            i -= 2;
            while (i >= 0) {
                x0 = data[i];
                y0 = data[i + 1];
                i -= 2;
                x = x0 * t.m0 + y0 * t.m2 + t.m4;
                y = x0 * t.m1 + y0 * t.m3 + t.m5;
                if (x < x1) x1 = x;
                if (y < y1) y1 = y;
                if (x > x2) x2 = x;
                if (y > y2) y2 = y;
            }
        } else {
            x1 = y1 = x2 = y2 = 0.0f;
        }
        
        return new Box(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * @return true if obj is a path and all its commands are the 
     *         same as this instance.
     */
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !(obj instanceof Path)) {
            return false;
        }

        Path p = (Path) obj;
        if (nSegments != p.nSegments || nData != nData) {
            return false;
        }

        // Compare each command type and offset
        for (int ci = 0; ci < nSegments; ci++) {
            // Check we are dealing with the same command type
            if (commands[ci] != p.commands[ci]) {
                return false;
            }
        }

        // Now, compare each command data
        for (int di = 0; di < nData; di++) {
            if (data[di] != p.data[di]) {
                return false;
            }
        }

        return true;
    }
}
