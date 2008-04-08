/*
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

package com.sun.cldchi.tools.memoryprofiler.jdwp;

/**
 * The <code>VMReply</code> declares interface between transport 
 * layer and data providing layer of the memory profiler. It allows reading integet
 * and string values from the packet sent by the VM. 
 * 
 * The instances of this interface are provided by <code>VMConnection</code>.
 *
 * @see com.sun.cldchi.tools.memoryprofiler.jdwp.VMConnection
 *
 */

public interface VMReply {

  /**
   * Reads int value from the VM message. 
   * Could throw <code>BoundException</code> if tries to read 
   * beyond the end of the message
   *
   * @return int value written to the packet by the VM
   *
   * @see #getString()
   */
  public int getInt() throws BoundException;

  /**
   * Reads a String from the VM message. 
   * Could throw <code>BoundException</code> if tries to read 
   * beyond the end of the message
   *
   * @return String written to the packet by the VM
   *
   * @see #getInt()
   */
  public String getString() throws BoundException;    
}
