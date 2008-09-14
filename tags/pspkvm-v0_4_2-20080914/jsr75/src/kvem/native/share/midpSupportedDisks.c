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

#include <midpSupportedDisks.h>

const char* validDisks [][VD_DIMENSIONS] = {
#ifndef WIN32
#ifdef ZAURUS
    {"/dev/hda1", "CFCard/", "Compact_Flash_Card/"},
    {"/dev/mmcda1", "SDCard/", "Secure_Digital_Card/"},
//    {"/dev/mtdblock1", "Home/"},
//    {"/dev/mtdblock6", "InternalMemory/"}, // openzaurus ramfs
//    {"tmpfs", "InternalMemory/" },         // ipaqs /mnt/ramfs
//    {"/dev/root", "InternalStorage/"},
//    {"/dev/mtd", "Flash/"},
#endif /* ZAURUS */

#ifdef ARM
      // Note: If sd/mmc card is used as swap partition then it's not in
      // '/etc/mtab' table since mount point for a swap partition is none.
      {"/dev/mmc/part1", "MMCCard/", "MultiMedia_Card/"},
#else /* i386 */
      {"/dev/fd0", "Floppy/", "Floppy_Disk/"},
//    {"/dev/hda1", "HardDisk1/"},
//    {"/dev/hda2", "HardDisk2/"},
//    {"/dev/hda3", "HardDisk3/"},
//    {"/dev/hda4", "HardDisk4/"},
//    {"/dev/sda1", "SCSIDisk1/"},
//    {"/dev/sda2", "SCSIDisk2/"},
#endif /* ARM */
#endif /* WIN32 */

      {"pimdb", "PIMdb/", "PIM_Database/"},    // special root for pim
      {"storage", "Storage/", "Main_Storage/"}, // special root for fc
      {"private", "Private/", "Private_Storage/"},//root for midlet private dirs
};

/**
 * Retrieves number of valid disks.
 *
 * @return total amount of valid disks.
 */
int getValidDisksSize() {
   return sizeof(validDisks) / sizeof(char*) / VD_DIMENSIONS;
}
