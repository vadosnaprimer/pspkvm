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

#include <midpMountedRoots.h>
#include <jsr75/midpMountedRoots.h> // MOC-processed header

#include <midpNativeMounts.h>
#include <midpFileSystemMonitor.h>
#include <midpSupportedDisks.h>
#include <fcNotifyIsolates.h>

#include <qstringlist.h>
#include <qfile.h>

#ifdef UNIX
#include <sys/vfs.h>
#include <mntent.h>
#endif

#include <midpMalloc.h>
#include <midpStorage.h>


MidpMountedRoots* si = NULL;

/*
 *    QT for desktop (unlike QT for Zaurus) does not create file system
 *    listener so we need to create the own one.
 *
 *    Note: now we use our own file system listener for Zaurus too
 *    to avoid dependency on Qtopia library
 *    (QT file system listener uses QCopChannel and QCopEnvelope).
 */
MidpFileSystemMonitor* sm = NULL;

char* localizedNames = NULL;

#if 0
#include <stdio.h>
#define DEBUG_PRINT(s) puts(s); fflush(stdout)
#else
#define DEBUG_PRINT(s)
#endif

//-----------------------------------------------------------------------------

extern "C" void initNativeFileSystemMonitor()
{
    DEBUG_PRINT("initNativeFileSystemMonitor");

    if (sm == NULL)
    {
        sm = new MidpFileSystemMonitor();
    }

    if (si == NULL )
    {
        si = new MidpMountedRoots();
    }
}

// Note: the caller is responsible for calling 'midpFree' after use
extern "C" char* getMountedRoots()
{
   DEBUG_PRINT("getMountedRoots");

   if (si != NULL)
   {
       QString roots;
       for (QListIterator<MidpFileRoot> i(si->getFileRoots()); i.current(); ++i)
       {
           if (!roots.isEmpty())
           {
               roots += '\n';
           }
           roots += (*i)->getRoot();
       }
       char* res = (char*)midpMalloc(roots.length()+1);
       return strcpy(res, roots);
   }
   return NULL;
}

// Note: the caller is responsible for calling 'midpFree' after use
extern "C" char* getNativePathForRoot(const char* root)
{
    DEBUG_PRINT("getNativePathForRoot");

    if (si != NULL)
    {
        const QString path = si->getPathForRoot(root);
        if (!path.isEmpty())
        {
            char* res = (char*)midpMalloc(path.length()+1);
            return strcpy(res, path);
        }
    }
    return NULL;
}

//-----------------------------------------------------------------------------

/**
 * Gets localized names corresponding to roots returned by
 * FileSystemRegistry.listRoots() method. There is one localized name
 * corresponding to each root returned by method. Localized names are in the
 * same order as returned by method and are separated by ';' symbol.
 * If there is no localized name for root, non-localized (logical) name is
 * returned in the property for this root. Root names returned through this
 * property cannot contain ';' symbol.
 *
 * This method is called when the <code>fileconn.dir.roots.names</code> system
 * property is retrieved.
 *
 * @return the localized names for mounted roots separated by ';' symbol
 */
extern "C" char* getLocalizedMountedRoots()
{
    DEBUG_PRINT("getLocalizedMountedRoots")

    // it could be no calls to FileConnection API before getting this system
    // property so the subsystem could be not initialized.
    initNativeFileSystemMonitor();

    QString locals;
    for (QListIterator<MidpFileRoot> i(si->getFileRoots()); i.current(); ++i)
    {
        if (!locals.isEmpty())
        {
            locals += ';';
        }
       locals += (*i)->getLocalRoot();
    }

    if (locals.isEmpty())
    {
        return NULL;
    }

    // if it's not a first call then free previously allocated  buffer
    if (localizedNames != NULL)
    {
        midpFree(localizedNames);
    }

    localizedNames = (char*)midpMalloc(locals.length() + 1);
    return strcpy(localizedNames, locals);
}

//-----------------------------------------------------------------------------

MidpMountedRoots::MidpMountedRoots() : QObject(0)
{
    DEBUG_PRINT("MidpMountedRoots");

    mRoots.setAutoDelete(TRUE);
    update(false);
}

MidpMountedRoots::~MidpMountedRoots()
{
  DEBUG_PRINT("~MidpMountedRoots");
}

QString MidpMountedRoots::getPathForRoot(const QString &root) const
{
    DEBUG_PRINT("getPathForRoot");

    for (QListIterator<MidpFileRoot> i(mRoots); i.current(); ++i)
    {
        if (root == (*i)->getRoot())
        {
            return (*i)->getPath();
        }
    }
    return QString("");
}

bool MidpMountedRoots::isValidDisk(const QString& disk) const
{
    DEBUG_PRINT("isValidDisk");

    QString root, local;
    return getDiskInfo(disk, root, local);
}

bool MidpMountedRoots::getDiskInfo(const QString& disk,
    /*OUT*/ QString& root, /*OUT*/ QString& localRoot) const
{
    DEBUG_PRINT("getDiskInfo");

    const int size = getValidDisksSize();
    for (int i = 0; i < size; i++)
    {
        if (!strcmp(disk, validDisks[i][0]))
        {
            root = QString(validDisks[i][1]);
           localRoot = QString(validDisks[i][2]);
           return true;
        }
    }

    return false;
}

bool MidpMountedRoots::isCachedDisk(const QString& disk)
{
    for (QListIterator<MidpFileRoot> i(mRoots); i.current(); ++i)
    {
        if ((*i)->getDisk() == disk)
        {
            return true;
        }
    }
    return false;
}

void MidpMountedRoots::update(bool notify)
{
    DEBUG_PRINT("update");

#ifdef UNIX
    QValueList<QString> disks;
    QValueList<QString> paths;
    bool rebuild = false;
    int  count   = 0;

    // Read system mounts
    struct mntent *me;
    FILE *mntfp = setmntent("/etc/mtab", "r");
    if (mntfp)
    {
        while ((me = getmntent( mntfp )) != 0)
        {
            QString disk = me->mnt_fsname;
            if (isValidDisk(disk))
            {
                count++;

                disks.append(disk);

                QString path = QString(me->mnt_dir) + '/';
                paths.append(path);

                if (!isCachedDisk(disk))
                {
                    rebuild = true;
                }
            }
        }
        endmntent(mntfp);
    }

    // Add special roots for PIM, FC and Private
    const pcsl_string* storage = storage_get_root(INTERNAL_STORAGE_ID);
    if (pcsl_string_length(storage) >= 0)
    {
        const char* cstorage = (const char*)pcsl_string_get_utf8_data(storage);
        QString qstorage = cstorage;
        pcsl_string_release_utf8_data((const jbyte*)cstorage, storage);

        QString fc = qstorage + "storage/";
        QFile ffc(fc);
        // determine whether the path exists
        // Note: the path is created by the build system.
        if (ffc.exists())
        {
            count++;
            const QString name = "storage";

            disks.append(name);
            paths.append(fc);

            if (!isCachedDisk(name))
            {
                rebuild = true;
            }
        }

        QString pim = qstorage + "pimdb/";
        QFile fpim(pim);
        // determine whether the path exists
        // Note: the path is created by the build system.
        if (fpim.exists())
        {
            count++;
            const QString name = "pimdb";

            disks.append(name);
            paths.append(pim);

            if (!isCachedDisk(name))
            {
                rebuild = true;
            }
        }

        QString prv = qstorage + "private/";
        QFile fprv(prv);
        // determine whether the path exists
        // Note: the path is created by the build system.
        if (fprv.exists())
        {
            count++;
            const QString name = "private";

            disks.append(name);
            paths.append(prv);

            if (!isCachedDisk(name))
            {
                rebuild = true;
            }
        }
    }

    if (rebuild || count != (int)mRoots.count())
    {
        mRoots.clear();
        QStringList::ConstIterator it  = disks.begin();
        QStringList::ConstIterator pit = paths.begin();
        for (; it != disks.end(); ++it, ++pit)
        {
            QString disk = *it;
            QString path = *pit;
            QString root, local;
            if (getDiskInfo(disk, root, local))
            {
                MidpFileRoot* fs = new MidpFileRoot(disk, path, root, local);
                mRoots.append(fs);
            }
        }
        // Call the handler if required
        if (notify) {
            disksChanged();
        }
    }
#endif
}

void MidpMountedRoots::disksChanged()
{
    DEBUG_PRINT("disksChanged");

    notifyDisksChanged();
}

const QList<MidpFileRoot>& MidpMountedRoots::getFileRoots() const
{
    return mRoots;
}

void MidpMountedRoots::update()
{
    DEBUG_PRINT("update");

    if (si != NULL )
    {
        si->update(true);
    }
}

//-----------------------------------------------------------------------------

MidpFileRoot::MidpFileRoot(const QString &disk, const QString &path,
    const QString &root, const QString &localRoot)
        : mDisk(disk), mPath(path), mRoot(root), mLocalRoot(localRoot)
{
}

const QString& MidpFileRoot::getDisk() const
{
    return mDisk;
}

const QString& MidpFileRoot::getPath() const
{
    return mPath;
}

const QString& MidpFileRoot::getRoot() const
{
    return mRoot;
}

const QString& MidpFileRoot::getLocalRoot() const
{
    return mLocalRoot;
}
