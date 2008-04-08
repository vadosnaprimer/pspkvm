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

package vm;
import jcc.Const;
import util.DataFormatException;
import components.*;
import jcc.Util;

/*
 * A primitive class
 *
 */

public
class PrimitiveClassInfo extends ClassInfo {

    public char signature;
    public int slotsize;
    public int elementsize;
    public int typecode;

    private PrimitiveClassInfo(String name, char signature, int typecode,
			       int slotsize, int elementsize, 
			       boolean verbosity, ConstantPool p) 
    {
        super(verbosity);
        UnicodeConstant uc = (UnicodeConstant)p.add(new UnicodeConstant(name));
	className = name;
	thisClass = new ClassConstant(uc);
	superClassInfo = lookupClass("java/lang/Object");
	superClass = superClassInfo.thisClass;
	this.signature = signature;
	this.typecode = typecode;
	this.slotsize = slotsize;
	this.elementsize = elementsize;
	access = Const.ACC_FINAL | Const.ACC_ABSTRACT | Const.ACC_PUBLIC; 
	constants = new ConstantObject[0];
	methods = new MethodInfo[0];
	fields  = new FieldInfo[0];
	enterClass(name);
    }

    public static void init(boolean v, ConstantPool p) { 

        (new PrimitiveClassInfo("void",    
	       Const.SIGC_VOID,    Const.T_VOID,    0, 0, v, p)).countReferences(false);
	(new PrimitiveClassInfo("boolean", 
	       Const.SIGC_BOOLEAN, Const.T_BOOLEAN, 4, 1, v, p)).countReferences(false);
	(new PrimitiveClassInfo("byte",    
	       Const.SIGC_BYTE,    Const.T_BYTE,    4, 1, v, p)).countReferences(false);
	(new PrimitiveClassInfo("char",
	       Const.SIGC_CHAR,    Const.T_CHAR,    4, 2, v, p)).countReferences(false);
	(new PrimitiveClassInfo("short",
	       Const.SIGC_SHORT,   Const.T_SHORT,   4, 2, v, p)).countReferences(false);
	(new PrimitiveClassInfo("int",  
	       Const.SIGC_INT,     Const.T_INT,     4, 4, v, p)).countReferences(false);
	(new PrimitiveClassInfo("long",   
	       Const.SIGC_LONG,    Const.T_LONG,    8, 8, v, p)).countReferences(false);
	(new PrimitiveClassInfo("float",
	       Const.SIGC_FLOAT,   Const.T_FLOAT,   4, 4, v, p)).countReferences(false);
	(new PrimitiveClassInfo("double", 
	       Const.SIGC_DOUBLE,  Const.T_DOUBLE,  8, 8, v, p)).countReferences(false);
    };

    protected String createGenericNativeName() { 
        return /*NOI18N*/"primitiveClass_" + 
	     Util.convertToClassName(className);
    }
}
