/*
 *
 *
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

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.sun.perseus.util;

/**
 * Define SVG constants, such as tag names, attribute names and URI. This
 * interface contains constants for:
 * <ul>
 *  <li>XML Generic constants, such as <code>XML_NAMESPACE_URI</code></li>
 *  <li>CSS constants, such as </code>CSS_FILL_PROPERTY</code> and corresponding
 *      values (e.g., <code>CSS_INHERIT_VALUE</code>)</li>
 *  <li>SVG specific constants, such as <code>SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE</code></li>
 * </ul>
 *
 * @version $Id: SVGConstants.java,v 1.4 2006/04/21 06:35:52 st125089 Exp $
 */
public interface SVGConstants {
    //
    // XML Constants
    // =========================================================================

    /**
     * The XML namespace URI.
     */
    String XML_NAMESPACE_URI =
        "http://www.w3.org/XML/1998/namespace";

    /**
     * The xmlns namespace URI.
     */
    String XMLNS_NAMESPACE_URI =
        "http://www.w3.org/2000/xmlns/";

    /**
     * The xmlns prefix
     */
    String XMLNS_PREFIX = "xmlns";

    /**
     * The xlink namespace URI
     */
    String XLINK_NAMESPACE_URI
        = "http://www.w3.org/1999/xlink";

    /**
     * The Perseus namespace URI
     */
    String PERSEUS_NAMESPACE_URI
        = "http://www.sun.com/svg/perseus";
    
    /**
     * Namespace prefix property for XML parsers
     */
    String XMLNS_ATTRIBUTES_NEEDED = "http://xml.org/sax/features/namespace-prefixes";

    /**
     * The xlink prefix
     */
    String XLINK_PREFIX = "xlink";

    String XML_PREFIX = "xml";
    String XML_LANG_ATTRIBUTE  = XML_PREFIX + ":lang";
    String XML_SPACE_ATTRIBUTE_LOCAL_NAME = "space";
    String XML_SPACE_ATTRIBUTE = XML_PREFIX + ":" + XML_SPACE_ATTRIBUTE_LOCAL_NAME;
    String XML_BASE_ATTRIBUTE_LOCAL_NAME = "base";
    String XML_BASE_ATTRIBUTE = XML_PREFIX + ":" + XML_BASE_ATTRIBUTE_LOCAL_NAME;

    String XML_DEFAULT_VALUE = "default";
    String XML_PRESERVE_VALUE = "preserve";

    String ATTR_ID = "id";

    //
    // Common Constants
    // =========================================================================
    char COMMA = ',';
    String COMMA_STR = ",";
    String EMPTY = "";
    char SPACE = ' ';
    String SPACE_STR = " ";  
    String COMMA_SPACE = ", ";
    char SEMI_COLON = ';';

    //
    // Path commands
    // =========================================================================
    char PATH_MOVE_TO_COMMAND = 'M';
    char PATH_LINE_TO_COMMAND = 'L';
    char PATH_QUAD_TO_COMMAND = 'Q';
    char PATH_CUBIC_TO_COMMAND = 'C';
    char PATH_CLOSE_COMMAND = 'Z';

    //
    // Perseus Constants
    // =========================================================================
    String PERSEUS_CHILDREN_REQUIRED_ATTRIBUTE = "externalResourcesRequired";

    //
    // CSS Constants
    // =========================================================================

    //
    // The CSS property names.
    //
    String CSS_COLOR_PROPERTY = "color";
    String CSS_COLOR_RENDERING_PROPERTY = "color-rendering";
    String CSS_DISPLAY_PROPERTY = "display";
    String CSS_FILL_PROPERTY = "fill";
    String CSS_FILL_OPACITY_PROPERTY = "fill-opacity";
    String CSS_FILL_RULE_PROPERTY = "fill-rule";
    String CSS_FONT_PROPERTY = "font";
    String CSS_FONT_FAMILY_PROPERTY = "font-family";
    String CSS_FONT_SIZE_PROPERTY = "font-size";
    String CSS_FONT_SIZE_ADJUST_PROPERTY = "font-size-adjust";
    String CSS_FONT_STRETCH_PROPERTY = "font-stretch";
    String CSS_FONT_STYLE_PROPERTY = "font-style";
    String CSS_FONT_VARIANT_PROPERTY = "font-variant";
    String CSS_FONT_WEIGHT_PROPERTY = "font-weight";
    String CSS_OPACITY_PROPERTY = "opacity";
    String CSS_OVERFLOW_PROPERTY = "overflow";
    String CSS_STROKE_PROPERTY = "stroke";
    String CSS_STROKE_DASHARRAY_PROPERTY = "stroke-dasharray";
    String CSS_STROKE_DASHOFFSET_PROPERTY = "stroke-dashoffset";
    String CSS_STROKE_LINECAP_PROPERTY = "stroke-linecap";
    String CSS_STROKE_LINEJOIN_PROPERTY = "stroke-linejoin";
    String CSS_STROKE_MITERLIMIT_PROPERTY = "stroke-miterlimit";
    String CSS_STROKE_OPACITY_PROPERTY = "stroke-opacity";
    String CSS_STROKE_WIDTH_PROPERTY = "stroke-width";
    String CSS_TEXT_ANCHOR_PROPERTY = "text-anchor";
    String CSS_VISIBILITY_PROPERTY = "visibility";

    //
    // The CSS property values.
    //
    String CSS_100_VALUE = "100";
    String CSS_200_VALUE = "200";
    String CSS_300_VALUE = "300";
    String CSS_400_VALUE = "400";
    String CSS_500_VALUE = "500";
    String CSS_600_VALUE = "600";
    String CSS_700_VALUE = "700";
    String CSS_800_VALUE = "800";
    String CSS_900_VALUE = "900";
    String CSS_ACCUMULATE_VALUE = "accumulate";
    String CSS_ACTIVEBORDER_VALUE = "activeborder";
    String CSS_ACTIVECAPTION_VALUE = "activecaption";
    String CSS_AFTER_EDGE_VALUE = "after-edge";
    String CSS_ALL_VALUE = "all";
    String CSS_ALPHABETIC_VALUE = "alphabetic";
    String CSS_AQUA_VALUE = "aqua";
    String CSS_AUTO_VALUE = "auto";
    String CSS_BACKGROUND_VALUE = "background";
    String CSS_BEVEL_VALUE = "bevel";
    String CSS_BLACK_VALUE = "black";
    String CSS_BLOCK_VALUE = "block";
    String CSS_BLUE_VALUE = "blue";
    String CSS_BOLDER_VALUE = "bolder";
    String CSS_BOLD_VALUE = "bold";
    String CSS_BUTT_VALUE = "butt";
    String CSS_CENTER_VALUE = "center";
    String CSS_COLLAPSE_VALUE = "collapse";
    String CSS_COMPACT_VALUE = "compact";
    String CSS_CURRENTCOLOR_VALUE = "currentColor";
    String CSS_DEFAULT_VALUE = "default";
    String CSS_END_VALUE = "end";
    String CSS_EVENODD_VALUE = "evenodd";
    String CSS_EXPANDED_VALUE = "expanded";
    String CSS_FILL_VALUE = "fill";
    String CSS_FUCHSIA_VALUE = "fuchsia";
    String CSS_GRAY_VALUE = "gray";
    String CSS_GREEN_VALUE = "green";
    String CSS_HIDDEN_VALUE = "hidden";
    String CSS_INHERIT_VALUE = "inherit";
    String CSS_INLINE_TABLE_VALUE = "inline-table";
    String CSS_INLINE_VALUE = "inline";
    String CSS_ITALIC_VALUE = "italic";
    String CSS_LIGHTER_VALUE = "lighter";
    String CSS_LIME_VALUE = "lime";
    String CSS_LIST_ITEM_VALUE = "list-item";
    String CSS_MARKER_VALUE = "marker";
    String CSS_MAROON_VALUE = "maroon";
    String CSS_MIDDLE_VALUE = "middle";
    String CSS_MITER_VALUE = "miter";
    String CSS_MONOSPACE_VALUE = "monospace";
    String CSS_NAVY_VALUE = "navy";
    String CSS_NONE_VALUE = "none";
    String CSS_NONZERO_VALUE = "nonzero";
    String CSS_NORMAL_VALUE = "normal";
    String CSS_OBLIQUE_VALUE = "oblique";
    String CSS_OLIVE_VALUE = "olive";
    String CSS_ORANGE_VALUE = "orange";
    String CSS_PINK_VALUE = "pink";
    String CSS_PURPLE_VALUE = "purple";
    String CSS_RED_VALUE = "red";
    String CSS_ROUND_VALUE = "round";
    String CSS_RUN_IN_VALUE = "run-in";
    String CSS_SANS_SERIF_VALUE = "sans-serif";
    String CSS_SERIF_VALUE = "serif";
    String CSS_SILVER_VALUE = "silver";
    String CSS_SQUARE_VALUE = "square";
    String CSS_START_VALUE = "start";
    String CSS_TABLE_CAPTION_VALUE = "table-caption";
    String CSS_TABLE_CELL_VALUE = "table-cell";
    String CSS_TABLE_COLUMN_GROUP_VALUE = "table-column-group";
    String CSS_TABLE_COLUMN_VALUE = "table-column";
    String CSS_TABLE_FOOTER_GROUP_VALUE = "table-footer-group";
    String CSS_TABLE_HEADER_GROUP_VALUE = "table-header-group";
    String CSS_TABLE_ROW_GROUP_VALUE = "table-row-group";
    String CSS_TABLE_ROW_VALUE = "table-row";
    String CSS_TABLE_VALUE = "table";
    String CSS_TEAL_VALUE = "teal";
    String CSS_VISIBLE_VALUE = "visible";
    String CSS_WHITE_VALUE = "white";
    String CSS_YELLOW_VALUE = "yellow";

    //
    // SVG Constants
    // =========================================================================

    /////////////////////////////////////////////////////////////////////////
    // SVG general
    /////////////////////////////////////////////////////////////////////////

    /**
     * The SVG namespace URI. Note that the SVG namespace is constant accross
     * SVG versions.
     */
    String SVG_NAMESPACE_URI =
        "http://www.w3.org/2000/svg";

    //////////////////////////////////////////////////////////////////////////
    // Events type and attributes
    //////////////////////////////////////////////////////////////////////////

    /**
     * The event type for MouseEvent.
     */
    String SVG_MOUSEEVENTS_EVENT_TYPE = "MouseEvents";

    /**
     * The event type for UIEvent.
     */
    String SVG_UIEVENTS_EVENT_TYPE = "UIEvents";

    /**
     * The event type for SVGEvent.
     */
    String SVG_SVGEVENTS_EVENT_TYPE = "SVGEvents";

    /**
     * The event type for KeyEvent.
     */
    String SVG_KEYEVENTS_EVENT_TYPE = "KeyEvents";

    // ---------------------------------------------------------------------

    /**
     * The event type for 'keydown' KeyEvent.
     */
    String SVG_KEYDOWN_EVENT_TYPE = "keydown";

    /**
     * The event type for 'keypress' KeyEvent.
     */
    String SVG_KEYPRESS_EVENT_TYPE = "keypress";

    /**
     * The event type for 'keyup' KeyEvent.
     */
    String SVG_KEYUP_EVENT_TYPE = "keyup";

    /**
     * The event type for 'click' MouseEvent.
     */
    String SVG_CLICK_EVENT_TYPE = "click";

    /**
     * The event type for 'mouseup' MouseEvent.
     */
    String SVG_MOUSEUP_EVENT_TYPE = "mouseup";

    /**
     * The event type for 'mousedown' MouseEvent.
     */
    String SVG_MOUSEDOWN_EVENT_TYPE = "mousedown";

    /**
     * The event type for 'mousemove' MouseEvent.
     */
    String SVG_MOUSEMOVE_EVENT_TYPE = "mousemove";

    /**
     * The event type for 'mouseout' MouseEvent.
     */
    String SVG_MOUSEOUT_EVENT_TYPE = "mouseout";

    /**
     * The event type for 'mouseover' MouseEvent.
     */
    String SVG_MOUSEOVER_EVENT_TYPE = "mouseover";

    /**
     * The event type for 'DOMFocusIn' UIEvent.
     */
    String SVG_DOMFOCUSIN_EVENT_TYPE = "DOMFocusIn";

    /**
     * The event type for 'DOMFocusOut' UIEvent.
     */
    String SVG_DOMFOCUSOUT_EVENT_TYPE = "DOMFocusOut";

    /**
     * The event type for 'DOMActivate' UIEvent.
     */
    String SVG_DOMACTIVATE_EVENT_TYPE = "DOMActivate";

    /**
     * The event type for 'SVGLoad' SVGEvent.
     */
    String SVG_SVGLOAD_EVENT_TYPE = "SVGLoad";

    /**
     * The event type for 'SVGUnload' SVGEvent.
     */
    String SVG_SVGUNLOAD_EVENT_TYPE = "SVGUnload";

    /**
     * The event type for 'SVGAbort' SVGEvent.
     */
    String SVG_SVGABORT_EVENT_TYPE = "SVGAbort";

    /**
     * The event type for 'SVGError' SVGEvent.
     */
    String SVG_SVGERROR_EVENT_TYPE = "SVGError";

    /**
     * The event type for 'SVGResize' SVGEvent.
     */
    String SVG_SVGRESIZE_EVENT_TYPE = "SVGResize";

    /**
     * The event type for 'SVGScroll' SVGEvent.
     */
    String SVG_SVGSCROLL_EVENT_TYPE = "SVGScroll";

    /**
     * The event type for 'SVGZoom' SVGEvent.
     */
    String SVG_SVGZOOM_EVENT_TYPE = "SVGZoom";

    /**
     * The event type for focus in used in SMIL animation attributes.
     */
    String SVG_SMIL_FOCUS_IN_EVENT_TYPE = "focusin";

    /**
     * The event type for focusout used in SMIL animation attributes.
     */
    String SVG_SMIL_FOCUS_OUT_EVENT_TYPE = "focusout";

    /**
     * The event type for activate used in SMIL animation attributes.
     */
    String SVG_SMIL_ACTIVATE_EVENT_TYPE = "activate";


    // ---------------------------------------------------------------------

    /**
     * The 'onkeyup' attribute name of type KeyEvents.
     */
    // Perseus 1.5 String SVG_ONKEYUP_ATTRIBUTE = "onkeyup";

    /**
     * The 'onkeydown' attribute name of type KeyEvents.
     */
    // Perseus 1.5 String SVG_ONKEYDOWN_ATTRIBUTE = "onkeydown";

    /**
     * The 'onkeypress' attribute name of type KeyEvents.
     */
    // Perseus 1.5 String SVG_ONKEYPRESS_ATTRIBUTE = "onkeypress";

    /**
     * The 'onabort' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONABORT_ATTRIBUTE = "onabort";

    /**
     * The 'onabort' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONACTIVATE_ATTRIBUTE = "onactivate";

    /**
     * The 'onbegin' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONBEGIN_ATTRIBUTE = "onbegin";

    /**
     * The 'onclick' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONCLICK_ATTRIBUTE = "onclick";

    /**
     * The 'onend' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONEND_ATTRIBUTE = "onend";

    /**
     * The 'onerror' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONERROR_ATTRIBUTE = "onerror";

    /**
     * The 'onfocusin' attribute name of type UIEvents.
     */
    // Perseus 1.5 String SVG_ONFOCUSIN_ATTRIBUTE = "onfocusin";

    /**
     * The 'onfocusout' attribute name of type UIEvents.
     */
    // Perseus 1.5 String SVG_ONFOCUSOUT_ATTRIBUTE = "onfocusout";

    /**
     * The 'onload' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONLOAD_ATTRIBUTE = "onload";

    /**
     * The 'onmousedown' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONMOUSEDOWN_ATTRIBUTE = "onmousedown";

    /**
     * The 'onmousemove' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONMOUSEMOVE_ATTRIBUTE = "onmousemove";

    /**
     * The 'onmouseout' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONMOUSEOUT_ATTRIBUTE = "onmouseout";

    /**
     * The 'onmouseover' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONMOUSEOVER_ATTRIBUTE = "onmouseover";

    /**
     * The 'onmouseup' attribute name of type MouseEvents.
     */
    // Perseus 1.5 String SVG_ONMOUSEUP_ATTRIBUTE = "onmouseup";

    /**
     * The 'onrepeat' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONREPEAT_ATTRIBUTE = "onrepeat";

    /**
     * The 'onresize' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONRESIZE_ATTRIBUTE = "onresize";

    /**
     * The 'onscroll' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONSCROLL_ATTRIBUTE = "onscroll";
 
    /**
     * The 'onunload' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONUNLOAD_ATTRIBUTE = "onunload";

    /**
     * The 'onzoom' attribute name of type SVGEvents.
     */
    // Perseus 1.5 String SVG_ONZOOM_ATTRIBUTE = "onzoom";

    /////////////////////////////////////////////////////////////////////////
    // SVG features
    /////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////
    // SVG tags
    /////////////////////////////////////////////////////////////////////////

    String SVG_A_TAG = "a";
    String SVG_ANIMATE_TAG = "animate";
    String SVG_ANIMATE_COLOR_TAG = "animateColor";
    String SVG_ANIMATE_MOTION_TAG = "animateMotion";
    String SVG_ANIMATE_TRANSFORM_TAG = "animateTransform";
    String SVG_AUDIO_TAG = "audio";
    String SVG_CIRCLE_TAG = "circle";
    String SVG_DEFS_TAG = "defs";
    String SVG_DESC_TAG = "desc";
    String SVG_ELLIPSE_TAG = "ellipse";
    String SVG_FILTER_TAG = "filter";
    String SVG_FONT_TAG = "font";
    String SVG_FONT_FACE_TAG = "font-face";
    String SVG_FONT_FACE_NAME_TAG = "font-face-name";
    String SVG_FONT_FACE_SRC_TAG = "font-face-src";
    String SVG_FOREIGN_OBJECT_TAG = "foreignObject";
    String SVG_G_TAG = "g";
    String SVG_GLYPH_TAG = "glyph";
    String SVG_HKERN_TAG = "hkern";
    String SVG_IMAGE_TAG = "image";
    String SVG_LINE_TAG = "line";
    String SVG_LINEAR_GRADIENT_TAG = "linearGradient";
    String SVG_METADATA_TAG = "metadata";
    String SVG_MISSING_GLYPH_TAG = "missing-glyph";
    String SVG_MPATH_TAG = "mpath";
    String SVG_PAR_TAG = "par";
    String SVG_PATH_TAG = "path";
    String SVG_POLYGON_TAG = "polygon";
    String SVG_POLYLINE_TAG = "polyline";
    String SVG_RECT_TAG = "rect";
    String SVG_SET_TAG = "set";
    String SVG_SOLID_COLOR_TAG = "solidColor";
    String SVG_STOP_TAG = "stop";
    String SVG_SVG_TAG = "svg";
    String SVG_SWITCH_TAG = "switch";
    String SVG_SYMBOL_TAG = "symbol";
    String SVG_TEXT_TAG = "text";
    String SVG_TITLE_TAG = "title";
    String SVG_TREF_TAG = "tref";
    String SVG_TSPAN_TAG = "tspan";
    String SVG_USE_TAG = "use";

    /////////////////////////////////////////////////////////////////////////
    // SVG attributes
    /////////////////////////////////////////////////////////////////////////

    String SVG_ACCENT_HEIGHT_ATTRIBUTE = "accent-height";
    String SVG_ACCUMULATE_ATTRIBUTE = "accumulate";
    String SVG_ADDITIVE_ATTRIBUTE = "additive";
    String SVG_ALPHABETIC_ATTRIBUTE = "alphabetic";
    String SVG_AMPLITUDE_ATTRIBUTE = "amplitude";
    String SVG_ARABIC_FORM_ATTRIBUTE = "arabic-form";
    String SVG_ASCENT_ATTRIBUTE = "ascent";
    String SVG_ATTRIBUTE_NAME_ATTRIBUTE = "attributeName";
    String SVG_AUDIO_LEVEL_ATTRIBUTE = "audio-level";
    String SVG_BASE_PROFILE_ATTRIBUTE = "baseProfile";
    String SVG_BEGIN_ATTRIBUTE = "begin";
    String SVG_BBOX_ATTRIBUTE = "bbox";
    String SVG_BIAS_ATTRIBUTE = "bias";
    String SVG_BY_ATTRIBUTE = "by";
    String SVG_CALC_MODE_ATTRIBUTE = "calcMode";
    String SVG_CAP_HEIGHT_ATTRIBUTE = "cap-height";
    String SVG_COLOR_ATTRIBUTE = CSS_COLOR_PROPERTY;
    String SVG_COLOR_RENDERING_ATTRIBUTE = CSS_COLOR_RENDERING_PROPERTY;
    String SVG_CX_ATTRIBUTE = "cx";
    String SVG_CY_ATTRIBUTE = "cy";
    String SVG_DESCENT_ATTRIBUTE = "descent";
    String SVG_DISPLAY_ATTRIBUTE = "display";
    String SVG_DUR_ATTRIBUTE = "dur";
    String SVG_DX_ATTRIBUTE = "dx";
    String SVG_DY_ATTRIBUTE = "dy";
    String SVG_D_ATTRIBUTE = "d";
    String SVG_END_ATTRIBUTE = "end";
    String SVG_FILL_ATTRIBUTE = CSS_FILL_PROPERTY;
    String SVG_FILL_OPACITY_ATTRIBUTE = CSS_FILL_OPACITY_PROPERTY;
    String SVG_FILL_RULE_ATTRIBUTE = CSS_FILL_RULE_PROPERTY;
    String SVG_FONT_FAMILY_ATTRIBUTE = CSS_FONT_FAMILY_PROPERTY;
    String SVG_FONT_SIZE_ATTRIBUTE = CSS_FONT_SIZE_PROPERTY;
    String SVG_FONT_STRETCH_ATTRIBUTE = CSS_FONT_STRETCH_PROPERTY;
    String SVG_FONT_STYLE_ATTRIBUTE = CSS_FONT_STYLE_PROPERTY;
    String SVG_FONT_VARIANT_ATTRIBUTE = CSS_FONT_VARIANT_PROPERTY;
    String SVG_FONT_WEIGHT_ATTRIBUTE = CSS_FONT_WEIGHT_PROPERTY;
    String SVG_FROM_ATTRIBUTE = "from";
    String SVG_FX_ATTRIBUTE = "fx";
    String SVG_FY_ATTRIBUTE = "fy";
    String SVG_G1_ATTRIBUTE = "g1";
    String SVG_G2_ATTRIBUTE = "g2";
    String SVG_GLYPH_NAME_ATTRIBUTE = "glyph-name";
    String SVG_GRADIENT_TRANSFORM_ATTRIBUTE = "gradientTransform";
    String SVG_GRADIENT_UNITS_ATTRIBUTE = "gradientUnits";
    String SVG_OPACITY_ATTRIBUTE = CSS_OPACITY_PROPERTY;
    String SVG_HANGING_ATTRIBUTE = "hanging";
    String SVG_HEIGHT_ATTRIBUTE = "height";
    String SVG_HORIZ_ADV_X_ATTRIBUTE = "horiz-adv-x";
    String SVG_HORIZ_ORIGIN_X_ATTRIBUTE = "horiz-origin-x";
    String SVG_HREF_ATTRIBUTE = "href";
    String SVG_ID_ATTRIBUTE = "id";
    String SVG_K_ATTRIBUTE = "k";
    String SVG_KEY_POINTS_ATTRIBUTE = "keyPoints";
    String SVG_KEY_SPLINES_ATTRIBUTE = "keySplines";
    String SVG_KEY_TIMES_ATTRIBUTE = "keyTimes";
    String SVG_LANG_ATTRIBUTE = "lang";
    String SVG_MAX_ATTRIBUTE = "max";
    String SVG_MIN_ATTRIBUTE = "min";
    String SVG_MOTION_PSEUDO_ATTRIBUTE = "#motion";
    String SVG_NAME_ATTRIBUTE = "name";
    String SVG_OFFSET_ATTRIBUTE = "offset";
    String SVG_PANOSE_1_ATTRIBUTE = "panose-1";
    String SVG_PATH_ATTRIBUTE = "path";
    String SVG_POINTS_ATTRIBUTE = "points";
    String SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE = "preserveAspectRatio";
    String SVG_RADIAL_GRADIENT_TAG = "radialGradient";
    String SVG_REPEAT_COUNT_ATTRIBUTE = "repeatCount";
    String SVG_REPEAT_DUR_ATTRIBUTE = "repeatDur";
    String SVG_REQUIRED_FEATURES_ATTRIBUTE = "requiredFeatures";
    String SVG_REQUIRED_EXTENSIONS_ATTRIBUTE = "requiredExtensions";
    String SVG_RESTART_ATTRIBUTE = "restart";
    String SVG_RX_ATTRIBUTE = "rx";
    String SVG_RY_ATTRIBUTE = "ry";
    String SVG_R_ATTRIBUTE = "r";
    String SVG_ROTATE_ATTRIBUTE = "rotate";
    String SVG_SLOPE_ATTRIBUTE = "slope";
    String SVG_SOLID_COLOR_ATTRIBUTE = "solid-color";
    String SVG_SOLID_OPACITY_ATTRIBUTE = "solid-opacity";
    String SVG_SPACE_ATTRIBUTE = "space";
    String SVG_STEMH_ATTRIBUTE = "stemh";
    String SVG_STEMV_ATTRIBUTE = "stemv";
    String SVG_STOP_COLOR_ATTRIBUTE = "stop-color";
    String SVG_STOP_OPACITY_ATTRIBUTE = "stop-opacity";
    String SVG_STROKE_ATTRIBUTE = CSS_STROKE_PROPERTY;
    String SVG_STROKE_DASHARRAY_ATTRIBUTE = CSS_STROKE_DASHARRAY_PROPERTY;
    String SVG_STROKE_DASHOFFSET_ATTRIBUTE = CSS_STROKE_DASHOFFSET_PROPERTY;
    String SVG_STROKE_LINECAP_ATTRIBUTE = CSS_STROKE_LINECAP_PROPERTY;
    String SVG_STROKE_LINEJOIN_ATTRIBUTE = CSS_STROKE_LINEJOIN_PROPERTY;
    String SVG_STROKE_MITERLIMIT_ATTRIBUTE = CSS_STROKE_MITERLIMIT_PROPERTY;
    String SVG_STROKE_OPACITY_ATTRIBUTE = CSS_STROKE_OPACITY_PROPERTY;
    String SVG_STROKE_WIDTH_ATTRIBUTE = CSS_STROKE_WIDTH_PROPERTY;
    String SVG_STYLE_ATTRIBUTE = "style";
    String SVG_SYSTEM_LANGUAGE_ATTRIBUTE = "systemLanguage";
    String SVG_TARGET_ATTRIBUTE = "target";
    String SVG_TEXT_ANCHOR_ATTRIBUTE = CSS_TEXT_ANCHOR_PROPERTY;
    String SVG_TEXT_LENGTH_ATTRIBUTE = "textLength";
    String SVG_TEXT_PSEUDO_ATTRIBUTE = "#text";
    String SVG_TITLE_ATTRIBUTE = "title";
    String SVG_TO_ATTRIBUTE = "to";
    String SVG_TRANSFORM_ATTRIBUTE = "transform";
    String SVG_TYPE_ATTRIBUTE = "type";
    String SVG_U1_ATTRIBUTE = "u1";
    String SVG_U2_ATTRIBUTE = "u2";
    String SVG_UNICODE_ATTRIBUTE = "unicode";
    String SVG_UNICODE_RANGE_ATTRIBUTE = "unicode-range";
    String SVG_UNITS_PER_EM_ATTRIBUTE = "units-per-em";
    String SVG_VALUES_ATTRIBUTE = "values";
    String SVG_VERSION_ATTRIBUTE = "version";
    String SVG_VIEW_BOX_ATTRIBUTE = "viewBox";
    String SVG_VISIBILITY_ATTRIBUTE = "visibility";
    String SVG_WIDTH_ATTRIBUTE = "width";
    String SVG_WIDTHS_ATTRIBUTE = "widths";
    String SVG_X1_ATTRIBUTE = "x1";
    String SVG_X2_ATTRIBUTE = "x2";
    String SVG_X_ATTRIBUTE = "x";
    String SVG_X_HEIGHT_ATTRIBUTE = "xHeight";
    String SVG_Y1_ATTRIBUTE = "y1";
    String SVG_Y2_ATTRIBUTE = "y2";
    String SVG_Y_ATTRIBUTE = "y";
    String SVG_ZOOM_AND_PAN_ATTRIBUTE = "zoomAndPan";

    /////////////////////////////////////////////////////////////////////////
    // SVG attribute value
    /////////////////////////////////////////////////////////////////////////

    String SVG_ALIGN_VALUE = "align";
    String SVG_ALWAYS_VALUE = "always";
    String SVG_AUTO_VALUE = "auto";
    String SVG_AUTO_REVERSE_VALUE = "auto-reverse";
    String SVG_DISABLE_VALUE = "disable";
    String SVG_DISCRETE_VALUE = "discrete";
    String SVG_FALSE_VALUE = "false";
    String SVG_FREEZE_VALUE = "freeze";
    String SVG_INDEFINITE_VALUE = "indefinite";
    String SVG_LINEAR_VALUE = "linear";
    String SVG_MAGNIFY_VALUE = "magnify";
    String SVG_MEDIA_VALUE = "media";
    String SVG_NEVER_VALUE = "never";
    String SVG_NONE_VALUE = "none";
    String SVG_OBJECT_BOUND_BOX_VALUE = "objectBoundingBox";
    String SVG_PACED_VALUE = "paced";
    String SVG_PRESERVE_VALUE = "preserve";
    String SVG_REMOVE_VALUE = "remove";
    String SVG_REPLACE_VALUE = "replace";
    String SVG_ROTATE_VALUE = "rotate";
    String SVG_SCALE_VALUE = "scale";
    String SVG_SKEW_X = "skewX";
    String SVG_SKEW_Y = "skewY";
    String SVG_SPLINE_VALUE = "spline";
    String SVG_SUM_VALUE = "sum";
    String SVG_TRANSLATE_VALUE = "translate";
    String SVG_TRUE_VALUE = "true";
    String SVG_USER_SPACE_ON_USE_VALUE = "userSpaceOnUse";
    String SVG_WHEN_NOT_ACTIVE_VALUE = "whenNotActive";

    ///////////////////////////////////////////////////////////////////
    // default values for attributes
    ///////////////////////////////////////////////////////////////////

    String SVG_BASE_PROFILE_TINY_VALUE = "tiny";
    String SVG_BEGIN_DEFAULT_VALUE = "0s";
    String SVG_CIRCLE_CX_DEFAULT_VALUE = "0";
    String SVG_CIRCLE_CY_DEFAULT_VALUE = "0";
    String SVG_ELLIPSE_CX_DEFAULT_VALUE = "0";
    String SVG_ELLIPSE_CY_DEFAULT_VALUE = "0";
    String SVG_FONT_FACE_FONT_STRETCH_DEFAULT_VALUE = CSS_NORMAL_VALUE;
    String SVG_FONT_FACE_FONT_SIZE_DEFAULT_VALUE = CSS_ALL_VALUE;
    String SVG_FONT_FACE_FONT_STYLE_DEFAULT_VALUE = CSS_ALL_VALUE;
    String SVG_FONT_FACE_FONT_VARIANT_DEFAULT_VALUE = CSS_NORMAL_VALUE;
    String SVG_FONT_FACE_FONT_WEIGHT_DEFAULT_VALUE = CSS_ALL_VALUE;
    String SVG_FONT_FACE_PANOSE_1_DEFAULT_VALUE = "0 0 0 0 0 0 0 0 0 0";
    String SVG_FONT_FACE_SLOPE_DEFAULT_VALUE = "0";
    String SVG_FONT_FACE_UNITS_PER_EM_DEFAULT_VALUE = "1000";
    String SVG_FONT_HORIZ_ORIGIN_X_DEFAULT_VALUE = "0";
    String SVG_FOREIGN_OBJECT_X_DEFAULT_VALUE = "0";
    String SVG_FOREIGN_OBJECT_Y_DEFAULT_VALUE = "0";
    String SVG_IMAGE_X_DEFAULT_VALUE = "0";
    String SVG_IMAGE_Y_DEFAULT_VALUE = "0";
    String SVG_IMAGE_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE = "xMidYMid meet";
    String SVG_LINE_X1_DEFAULT_VALUE = "0";
    String SVG_LINE_X2_DEFAULT_VALUE = "0";
    String SVG_LINE_Y1_DEFAULT_VALUE = "0";
    String SVG_LINE_Y2_DEFAULT_VALUE = "0";
    String SVG_PANOSE_1_DEFAULT_VALUE = "0 0 0 0 0 0 0 0 0 0";
    String SVG_RECT_X_DEFAULT_VALUE = "0";
    String SVG_RECT_Y_DEFAULT_VALUE = "0";
    String SVG_RECT_RX_DEFAULT_VALUE = null;
    String SVG_RECT_RY_DEFAULT_VALUE = null;
    String SVG_SVG_HEIGHT_DEFAULT_VALUE = "100%";
    String SVG_SVG_WIDTH_DEFAULT_VALUE = "100%";
    String SVG_SVG_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE = "xMidYMid meet";
    String SVG_SVG_ZOOM_AND_PAN_DEFAULT_VALUE = SVG_MAGNIFY_VALUE;
    String SVG_TEXT_X_DEFAULT_VALUE = "0";
    String SVG_TEXT_Y_DEFAULT_VALUE = "0";
    String SVG_USE_X_DEFAULT_VALUE = "0";
    String SVG_USE_Y_DEFAULT_VALUE = "0";
    String SVG_USE_WIDTH_DEFAULT_VALUE = "100%";
    String SVG_USE_HEIGHT_DEFAULT_VALUE = "100%";
    String SVG_VERSION_1_1_VALUE = "1.1";
    String SVG_VERSION_1_2_VALUE = "1.2";

    ///////////////////////////////////////////////////////////////////
    // event constants
    ///////////////////////////////////////////////////////////////////
    
    // Perseus 1.5 String SVG_EVENT_CLICK = "click";
    // Perseus 1.5 String SVG_EVENT_MOUSEOVER = "mouseover";
    // Perseus 1.5 String SVG_EVENT_MOUSEOUT  = "mouseout";

}
