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

#ifndef _MIDP_MOUNTED_ROOTS_H_
#define _MIDP_MOUNTED_ROOTS_H_

class MidpFileRoot;

/**
 * This is a proxy class used to get up-to-date list of mounted roots.
 * Additionally the class notifies MIDP stack about root mounts/unmounts.
 * It also converts human-friendly root names to native file paths.
 */
class MidpMountedRoots
{

public:
    /**
     * The constructor creates an instance of the class.
     * It also fills the roots array with currently mounted roots.
     */
    MidpMountedRoots();

    /**
     * Frees allocated memory.
     */
    virtual ~MidpMountedRoots();

    /**
     * The method converts human root to corresponding native file path.
     *
     * @param root The name of the root. The name must conform to JSR-75
     *             specification, i.e. to end with '/'.
     * @return The native  path that is used to access the root.
     */
    const char* getPathForRoot(const char* root) const;

    /**
     * The method retrieves the number of mounted roots.
     *
     * @return the number of mounted roots.
     */
    int getSize() const;

    /**
     * The method returns the mounted file root with specified index.
     *
     * Valid index value range is from <code>0</code>
     * to <code>getSize()-1</code>.
     * The method returns <code>NULL</code> if the index is invalid.
     *
     * @return the mounted root.
     */
    const MidpFileRoot* getFileRoot(int index) const;

    /**
     * The method is called upon every mount/unmount notification.
     *
     * It checks whether new root is really  mounted or unmounted.
     * If so, update the root array and calls <cide>disksChanged</code> method.
     */
    void update() { update(true); }

protected:
    /**
     * The method is called by <code>update()</code> method.
     * It stores <code>FC_DISKS_CHANGED_EVENT</code> event
     * into MIDP event queue.
     */
    void disksChanged();

    /**
     * The method is called from constructor to fill the root array
     * and also upon every mount/unmount notification.
     *
     * It checks whether new root really  mounted or unmounted. If so,
     * update the root array.
     *
     * It also calls <code>disksChanged</code> method if <code>notify</code>
     * parameter equals <code>true</code>.
     *
     * @param notify determines whether it's necessary to call
     *               <code>disksChanged</code> method upon
     *               mount/unmount detection
     */
    void update(bool notify);

private:
    /**
     * The method returns index of the disk in the internal disk table.
     *
     * @param disk The device (the left column in 'mtab' file).
     * @return The index of the disk.
     */
    int getDiskIndex(const char* disk) const;

    /**
     * Determines whether the disk is already in the cache.
     *
     * @param diskIndex Index of disk in the internal disk table.
     * @return <code>true</code> if the disk is already in the cache,
     *         <code>false</code> otherwise.
     */
    bool isCachedDisk(int diskIndex);

    /**
     * Empty internal root cache.
     */
    void resetCache();

    /**
     * Adds the file root into the cache.
     *
     * @param a pointer to the file root object
     */
    void addFileRoot(MidpFileRoot* fr);


    /**
     * Root cache.
     * This is an array of pointers to file root objects.
     */
    MidpFileRoot** mRoots;

    /** The maximum possible root number. */
    int mMaxRootsSize;

    /** The current cache size. */
    int mRootsSize;
};

/**
 * This is a helper class used by <code>MidpMountedRoots</code> class to store
 * information about particular root.
 */
class MidpFileRoot
{
public:
    /**
     * The constructor creates an instance of the class.
     * The constructor does not copy the path strings, just stores the
     * pointers, so this class is responsible for deleting of the strings.
     *
     * @param diskIndex Index of disk in the internal disk table.
     * @param path The native path for the root.
     */
    MidpFileRoot(int diskIndex, char* path);

    /**
     * Frees memory allocated for the strings.
     */
    virtual ~MidpFileRoot();


    /**
     * The method returns the index of the disk in the internal disk table.
     *
     * @return the index of the disk.
     */
    const int getDiskIndex() const;

    /**
     * The method returns disk (device) for the root.
     *
     * @return The disk for the root.
     */
    const char* getDisk() const;

    /**
     * The method returns native path for the root.
     *
     * @return The native path for the root.
     */
    const char* getPath() const;

    /**
     * The method returns human name of the root.
     *
     * @return The name of the root.
     */
    const char* getRoot() const;

    /**
     * The method returns localized human name of the root.
     *
     * @return The localized name of the root.
     */
    const char* getLocalRoot() const;

private:
    /** The index of the disk in the internal disk table (validDisks). */
    int   mDiskIndex;

    /** The native path for the root. */
    char* mPath;
};

#endif /* _MIDP_MOUNTED_ROOTS_H_ */
