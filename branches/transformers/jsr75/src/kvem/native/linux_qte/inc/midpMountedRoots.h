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
 * 
 * This source file is specific for Qt-based configurations.
 */

#ifndef _MIDP_MOUNTED_ROOTS_H_
#define _MIDP_MOUNTED_ROOTS_H_

#include <qobject.h>
#include <qlist.h>

class MidpFileRoot;

/**
 * This is a proxy class used to get up-to-date list of mounted roots.
 * Additionally the class notifies MIDP stack about root mounts/unmounts.
 * It also converts human-friendly root names to native file paths.
 */
class MidpMountedRoots : public QObject
{
    Q_OBJECT

public:
    /**
     * The constructor creates an instance of the class.
     * It fills the roots cache with currently mounted roots.
     */
    MidpMountedRoots();

    /**
     * The destructor.
     */
    virtual ~MidpMountedRoots();

    /**
     * The method converts human root to corresponding native file path.
     *
     * @param root The name of the root. The name must conform to JSR-75
     *             specification, i.e. to end with '/'.
     * @return The native path that is used to access the root.
     */
    QString getPathForRoot(const QString &root) const;

    /**
     * The method returns the list of currently mounted roots.
     *
     * @return the list of mounted roots.
     */
    const QList<MidpFileRoot>& getFileRoots() const;
    
    /** 
     * The method is called upon every mount/unmount notification.
     *
     * It checks whether a root is really mounted or unmounted.
     * If so, update the root cache and calls <cide>disksChanged</code> method.
     */
    static void update();

protected:
    /**
     * The method is called by <code>update()</code> method.
     * It stores <code>FC_DISKS_CHANGED_EVENT</code> event into 
     * MIDP event queue.
     */
    void disksChanged();

    /**
     * The method is called from constructor to fill the root cache
     * and also upon every mount/unmount notification.
     *
     * It checks whether new root really  mounted or unmounted. If so, 
     * update the root cache.
     *
     * It also calls <code>disksChanged</code> method if <code>notify</code> 
     * parameter equals <code>true</code>.
     *
     * @param notify determines whether it's necessary to call
     *               <code>disksChanged</code> method upon mount/unmount
     */
    void update(bool notify);

private:
    /**
     * The method checks whether the specified disk is supported
     * i.e. accessible via JSR-75 FileConnection API.
     *
     * @param disk the device (the left column in 'mtab' file).
     * @return <code>true</code> if the disk is accessible,
     *         <code>false</code> otherwise.
     */
    bool isValidDisk(const QString& disk) const;
    
    /**
     * Determines whether the disk is already in the cache.
     *
     * @param disk the device (the left column in 'mtab' file).
     * @return <code>true</code> if the disk is already in the cache,
     *         <code>false</code> otherwise.
     */
    bool isCachedDisk(const QString& disk);

    /**
     * The method returns the root name and localized root name for
     *  the specified disk.
     *
     * @param disk The device (the left column in 'mtab' file).
     * @param root The returned root name.
     * @param localRoot The returned localized root name.
     * @return <code>true</code> if the disk is supported,
     *         <code>false</code> otherwise
     */
    bool getDiskInfo(const QString& disk,
        /*OUT*/ QString& root, /*OUT*/ QString& localRoot) const;


    /** Root cache. */
    QList<MidpFileRoot> mRoots;
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
     *
     * @param disk The device (the left column in 'mtab' file).
     * @param path The native path for the root.
     * @param root The human name of the root.
     * @param localRoot The localized human name of the root.
     */
    MidpFileRoot(const QString &disk, const QString &path,
        const QString &root, const QString &localRoot);

    /**
     * The method returns disk (device) for the root.
     *
     * @return The disk for the root.
     */
    const QString& getDisk() const;

    /**
     * The method returns native path for the root.
     *
     * @return The native path for the root.
     */
    const QString& getPath() const;

    /**
     * The method returns human name of the root.
     *
     * @return The name of the root.
     */
    const QString& getRoot() const;
    
    /**
     * The method returns localized human name of the root.
     *
     * @return The localized name of the root.
     */
    const QString& getLocalRoot() const;

private:
    /** The disk for the root. */    
    QString mDisk;

    /** The native path for the root. */
    QString mPath;

    /** The name of the root. */
    QString mRoot;
    
    /** The localized name of the root. */
    QString mLocalRoot;
};

#endif /* _MIDP_MOUNTED_ROOTS_H_ */
