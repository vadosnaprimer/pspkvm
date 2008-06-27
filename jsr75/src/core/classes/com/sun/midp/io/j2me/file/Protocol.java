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

package com.sun.midp.io.j2me.file;

import com.sun.midp.security.Permissions;
import com.sun.midp.main.Configuration;
import com.sun.midp.io.*;

import javax.microedition.io.file.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Enumeration;
import javax.microedition.io.*;
import com.sun.midp.security.*;

import java.util.Vector;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;

/**
 * This class implements the necessary functionality
 * for a File connection.
 */
public class Protocol extends ConnectionBaseAdapter implements FileConnection {

    /** Security token for using FileConnection API from PIM */
    private SecurityToken classSecurityToken;

    /** Stores file connection mode */
    private int mode;

    /** File name string */
    private String fileName;

    /** File path string including root filesystem */
    private String filePath;

    /** Root filesystem for the file */
    private String fileRoot;

    /** File original URL */
    private String fileURL;

    /** A peer to the native file */
    private BaseFileHandler fileHandler;

    /** Indicates if there is a need to try to load alternative file handler */
    private static boolean hasOtherFileHandler = true;

    /** Input stream associated with this connection */
    InputStream fis;

    /** Output stream associated with this connection */
    OutputStream fos;

    /** Separator for file path components */
    private static String sep;

    /** Static initialization of file separator */
    static {
        char[] t = new char[1];
        t[0]= DefaultFileHandler.getFileSeparator();
        sep = new String(t);
        if (sep == null) {
            throw new
                NullPointerException("Undefined \"file.separator\" property");
        }
    }

    /**
     * Constructor for file connection implementation.
     */
    public Protocol() {
        connectionOpen = false;
        fileHandler = null;
    }

    /**
     * Opens the file connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        return openPrimImpl(name, mode, timeouts, true);
    }

    /**
     * Opens the file connection and receive security token.
     * @param token security token from PIM
     * @param name URL path fragment
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(SecurityToken token, String name)
            throws IOException {
        return openPrim(token, name, Connector.READ_WRITE);
    }

    /**
     * Opens the file connection and receive security token.
     * @param token security token from PIM
     * @param name URL path fragment
     * @param mode access mode
     * @return an opened Connection
     *  @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(SecurityToken token, String name, int mode)
            throws IOException {
        classSecurityToken = token;
        return openPrim(name, mode, false);
    }

    // JAVADOC COMMENT ELIDED
    public boolean isOpen() {
        return connectionOpen;
    }

    /**
     * Open and return an input stream for a connection.  The connection's
     * target must already exist and be accessible for the input stream to be
     * created.
     *
     * @return  An open input stream
     * @exception  IOException if an I/O error occurs, if the method is invoked
     *          on a directory, if the connection's target does not
     *          yet exist, or the connection's target is not accessible.
     * @exception  IllegalModeException if the application does have read
     *             access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @exception  SecurityException   If the application is not granted read
     *          access to the connection's target.
     *
     */
    public InputStream openInputStream() throws IOException {

        checkReadPermission();

        try {
            ensureOpenAndConnected();
        } catch (ConnectionClosedException e) {
            throw new IOException(e.getMessage());
        }

        // IOException when target file doesn't exist
        if (!fileHandler.exists()) {
            throw new IOException("Target file doesn't exist");
        }

        if (!fileHandler.canRead()) { // no read access
            throw new SecurityException("No read access");
        }

        fileHandler.openForRead();

        super.openInputStream();

        fis = new FileConnectionInputStream(this);

        return fis;
    }

    /**
     * Open and return an output stream for a connection. The output stream
     * is positioned at the start of the file. Writing data to the output
     * stream overwrites the contents of the files (i.e. does not insert data).
     * Writing data to output streams beyond the current end of file
     * automatically extends the file size.  The connection's target must
     * already exist and be accessible for the output stream to be created.
     * {@link #openOutputStream(long)} should be used to position an output
     * stream to a different position in the file.
     * <P>
     * Changes made to a file through an output stream may not be immediately
     * made to the actual file residing on the file system because
     * platform and implementation specific use of caching and buffering of the
     * data.  Stream contents and file length extensions are not necessarily
     * visible outside of the application immediately unless
     * <code>flush()</code> is called on the stream.? The returned output
     *  stream is automatically and synchronously flushed when it is closed.
     * </P>
     *
     * @return  An open output stream
     * @exception  IOException  If an I/O error occurs, if the method is
     *             invoked on
     *          a directory, the file does not yet exist, or the connection's
     *          target is not accessible.
     * @exception  IllegalModeException if the application does have write
     *             access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @exception  SecurityException    If the application is not granted write
     *          access to the connection's target.
     * @see #openOutputStream(long)
     *
     */
    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(0);
    }

    // JAVADOC COMMENT ELIDED
    public OutputStream openOutputStream(long byteOffset) throws IOException {
        if (byteOffset < 0) {
            throw new IllegalArgumentException("Offset has a negative value");
        }

        checkWritePermission();

        try {
            ensureOpenAndConnected();
        } catch (ConnectionClosedException e) {
            throw new IOException(e.getMessage());
        }

        // IOException when target file doesn't exist
        if (!fileHandler.exists()) {
            throw new IOException("Target file doesn't exist");
        }

        if (!fileHandler.canWrite()) {
            // no write access
            throw new SecurityException("No write access");
        }

        fileHandler.openForWrite();
        fileHandler.positionForWrite(byteOffset);

        fos = super.openOutputStream();

        return fos;
    }

    // JAVADOC COMMENT ELIDED
    public long totalSize() {
        long size = -1;

        try {
            checkRootReadPermission();

            ensureOpenAndConnected();

            size = fileHandler.totalSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long availableSize() {
        long size = -1;

        try {
            checkRootReadPermission();

            ensureOpenAndConnected();

            size = fileHandler.availableSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long usedSize() {
        long size = -1;

        try {
            checkRootReadPermission();

            ensureOpenAndConnected();

            size = fileHandler.usedSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long directorySize(boolean includeSubDirs) throws IOException {
        long size = 0;

        // Permissions and ensureOpenAndConnected called by exists()
        if (exists()) {
            if (!isDirectory()) {
                throw new
                    IOException("directorySize is not invoked on directory");
            }
        } else {
            return -1L;
        }

        try {
            size = fileHandler.directorySize(includeSubDirs);
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long fileSize() throws IOException {
        long size = -1;

        checkReadPermission();

        if (isDirectory()) {
            throw new IOException("fileSize invoked on a directory");
        }

        try {
            ensureOpenAndConnected();

            size = fileHandler.fileSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public boolean canRead() {
        boolean res = false;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res = fileHandler.canRead();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean canWrite() {
        boolean res = false;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res = fileHandler.canWrite();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isHidden() {
        boolean res = false;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res = fileHandler.isHidden();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public void setReadable(boolean readable) throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        fileHandler.setReadable(readable);
    }

    // JAVADOC COMMENT ELIDED
    public void setWritable(boolean writable) throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        fileHandler.setWritable(writable);
    }

    // JAVADOC COMMENT ELIDED
    public void setHidden(boolean hidden) throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        fileHandler.setHidden(hidden);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration list() throws IOException {
        return listInternal(null, false);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration list(String filter, boolean includeHidden)
        throws IOException {

        if (filter == null) {
            throw new NullPointerException("List filter is null");
        }

        return listInternal(EscapedUtil.getUnescapedString(filter),
            includeHidden);
    }

    // JAVADOC COMMENT ELIDED
    public void create() throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        if (fileName.charAt(fileName.length() - 1) == '/') {
            throw new IOException("Can not create directory");
        }

        fileHandler.create();
    }

    // JAVADOC COMMENT ELIDED
    public void mkdir() throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        fileHandler.mkdir();
    }

    // JAVADOC COMMENT ELIDED
    public boolean exists() {
        boolean res = false;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res = fileHandler.exists();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isDirectory() {
        boolean res = false;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res = fileHandler.isDirectory();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public void delete() throws java.io.IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        try {
            if (fis != null) {
                fis.close();
                fis = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            fileHandler.closeForReadWrite();
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.delete();
    }


    // JAVADOC COMMENT ELIDED
    public void rename(String newName) throws IOException {
        checkWritePermission();

        newName = EscapedUtil.getUnescapedString(newName);
        // Following line will throw NullPointerException if newName is null
        int dirindex = newName.indexOf('/');
        if (dirindex != -1 && dirindex != (newName.length() - 1)) {
            throw new
              IllegalArgumentException("New name contains path specification");
        }

        if (!"/".equals(sep) && newName.indexOf(sep) != -1) {
            throw new
              IllegalArgumentException("New name contains path specification");
        }

        ensureOpenAndConnected();
        checkIllegalChars(newName);

        try {
            if (fis != null) {
                fis.close();
                fis = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            fileHandler.closeForReadWrite();
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.rename(filePath + newName);

        fileName = newName;
        fileURL = "file://" + filePath + fileName;
    }

    // JAVADOC COMMENT ELIDED
    public void truncate(long byteOffset) throws IOException {
        checkWritePermission();

        ensureOpenAndConnected();

        if (byteOffset < 0) {
            throw new IllegalArgumentException("offset is negative");
        }

        try {
            if (fos != null) {
                fos.flush();
            }
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.truncate(byteOffset);
    }

    // JAVADOC COMMENT ELIDED
    public void setFileConnection(String fileName) throws IOException {
        ensureOpenAndConnected();

        // Note: permissions are checked by openPrim method

        // Following line will throw NullPointerException if fileName is null
        int dirindex = fileName.indexOf('/');
        if (dirindex != -1 && dirindex != (fileName.length() - 1)) {
            throw new IllegalArgumentException(
                "Contains any path specification");
        }

        if (fileName.equals("..") && this.fileName.length() == 0) {
            throw new IOException(
                "Cannot set FileConnection to '..' from a file system root");
        }

        if (!"/".equals(sep) && fileName.indexOf(sep) != -1) {
            throw new
            IllegalArgumentException("Contains any path specification");
        }

        checkIllegalChars(fileName);

        // According to the spec, the current FileConnection object must refer
        // to a directory.
        // Check this right here in order to avoid IllegalModeException instead
        // of IOException.
        if (!fileHandler.isDirectory()) {
            throw new IOException("Not a directory");
        }

        String origPath = filePath, origName = this.fileName;

        String tmp_sep;
        // Note: security checks are performed before any object state changes
        if (fileName.equals("..")) {
            // go one directory up
            openPrim("//" + filePath, mode, false);
        } else {
            int fileNameLen = this.fileName.length();
            if (fileNameLen == 0 || this.fileName.charAt(fileNameLen - 1) == '/') {
                tmp_sep = "";
            } else {
                tmp_sep = "/";
            }
            // go deeper in directory structure
            openPrimImpl("//" + filePath 
                     + this.fileName + tmp_sep + fileName,
                     mode, false, false);
        }

        // Old file connection must be a directory. It can not have open
        // streams so no need to close it. Just reset it to null
        fileHandler = null;

        // Reconnect to the new target
        ensureOpenAndConnected();

        // At this point we are already refer to the new file
        if (!fileHandler.exists()) {
            // Revert to an old file
            openPrim("//" + origPath + origName, mode, false);
            fileHandler = null;

            throw new IllegalArgumentException("New target does not exists");
        }
    }

    /**
     * Spec is not consistent: sometimes it requires IOException
     * and sometimes IllegalArgumentException in case of illegal chars
     * in the filename
     * @param name URL path fragment
     * @throws IOException if name contains unsupported characters
     */
    private void checkIllegalChars(String name) throws IOException {

        String illegalChars = fileHandler.illegalFileNameChars();
        for (int i = 0; i < illegalChars.length(); i++) {
            if (name.indexOf(illegalChars.charAt(i)) != -1) {
                throw new
                    IOException("Contains characters invalid for a filename");
            }
        }
    }

    // JAVADOC COMMENT ELIDED
    public String getName() {
        String name = fileName;

        try {
            if (exists()) {
                int lastPos = name.length() - 1;
                if (isDirectory()) {
                    if (!name.equals("") && name.charAt(lastPos) != '/') 
                        name += '/';
                } else {
                    if (name.charAt(lastPos) == '/')
                        name = name.substring(0, lastPos);
                }
            }
        } catch (SecurityException e) {
            // According to spec should silently ignore any exceptions
        } catch (IllegalModeException e) {
            // According to spec should silently ignore any exceptions
        } catch (ConnectionClosedException e) {
            // According to spec should silently ignore any exceptions
        }

        return name;
    }

    // JAVADOC COMMENT ELIDED
    public String getPath() {
        return filePath;
    }

    // JAVADOC COMMENT ELIDED
    public String getURL() {
        String url = EscapedUtil.getEscapedString(fileURL);

        try {
            if (exists()) {
                int lastPos = url.length() - 1;
                if (isDirectory()) {
                    if (url.charAt(lastPos) != '/') {
                        url += '/';
                    }
                } else {
                    if (url.charAt(lastPos) == '/') {
                        url = url.substring(0, lastPos);
                    }
                }
            }
        } catch (SecurityException e) {
            // According to spec should silently ignore any exceptions
        } catch (IllegalModeException e) {
            // According to spec should silently ignore any exceptions
        } catch (ConnectionClosedException e) {
            // According to spec should silently ignore any exceptions
        }

        return url;
    }

    // JAVADOC COMMENT ELIDED
    public long lastModified() {
        long res = 0;

        try {
            checkReadPermission();

            ensureOpenAndConnected();

            res =  fileHandler.lastModified();
        } catch (IOException e) {
            res = 0;
        }

        return res;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    protected int readBytes(byte b[], int off, int len)
        throws IOException {

        checkReadPermission();

        ensureConnected();

        int readBytes = fileHandler.read(b, off, len);
        // return '-1' instead of '0' as stream specification requires
        // in case the end of the stream has been reached
        return (readBytes > 0) ? readBytes : -1;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread. This classes implementation always returns
     * <code>0</code>. It is up to subclasses to override this method.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     *
     *     public int available() throws IOException {
     *    return 0;
     *}
     */
    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    protected int writeBytes(byte b[], int off, int len)
        throws IOException {
        checkWritePermission();

        ensureConnected();

        return fileHandler.write(b, off, len);
    }

    /**
     * Forces any buffered output bytes to be written out.
     * The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written that have been buffered by the connection,
     * should immediately be written to their intended destination.
     * <p>
     * The <code>flush</code> method of <code>ConnectionBaseAdapter</code>
     * does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    protected void flush() throws IOException {
        checkWritePermission();

        ensureConnected();

        fileHandler.flush();
    }

    /**
     * Called once by each child input stream.
     * If the input stream is marked open, it will be marked closed and
     * the if the connection and output stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeInputStream() throws IOException {
        maxIStreams++;
        fileHandler.closeForRead();
        super.closeInputStream();
    }

    /**
     * Called once by each child output stream.
     * If the output stream is marked open, it will be marked closed and
     * the if the connection and input stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeOutputStream() throws IOException {
        maxOStreams++;
        flush();
        fileHandler.closeForWrite();
        super.closeOutputStream();
    }

    /**
     * Free up the connection resources.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    protected void disconnect() throws IOException {
        try {
            if (fileHandler != null) {
                fileHandler.close();
            }
        } finally {
            fileHandler = null;
        }
    }

    // In order to compile against MIDP's ConnectionBaseAdapter
    /**
     * Establishes the connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @throws IOException if an error occurs
     */
    protected void connect(String name, int mode, boolean timeouts)
        throws IOException {}

    /**
     * Checks that the connection is already open.
     * @throws IOException if the connection is closed
     */
    protected void ensureConnected() throws IOException {
        if (!isRoot(fileRoot)) {
            throw new IOException("Root is not accessible");
        }
        if (fileHandler == null) {
            fileHandler = getFileHandler();
	     fileHandler.connect(fileRoot, filePath + fileName);
	     fileHandler.createPrivateDir(fileRoot);
        }
    }

    /**
     * Opens the file connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @param unescape flag to indicate whether URL must be unescaped
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    private Connection openPrimImpl(String name, int mode, boolean timeouts, boolean unescape)
            throws IOException {

        if (!name.startsWith("//")) {
            throw new IllegalArgumentException("Missing protocol separator");
        }

        int rootStart = name.indexOf('/', 2);

        if (rootStart == -1) {
            throw new IllegalArgumentException("Malformed File URL");
        }

        /* The string must be a valid URL path separated by "/" */
		
        if (name.indexOf("/../", rootStart) != -1 ||
            name.indexOf("/./", rootStart) != -1 ||
            name.endsWith("/..") ||
            name.endsWith("/.") ||
            !"/".equals(sep) && name.indexOf(sep, rootStart) != -1 ||
            name.indexOf('\\') != -1) {
                throw new
                    IllegalArgumentException("/. or /.. is not supported "
                    + "or other illegal characters found");
        }

        if (unescape) {
            name = EscapedUtil.getUnescapedString(name);
        }
        String fileURL = "file:" + name;

        // Perform security checks before any object state changes since
        // this method is used not only by Connector.open() but
        // by FileConnection.setFileConnection() too.
        switch (mode) {
        case Connector.READ:
            checkReadPermission(fileURL, mode);
            maxOStreams = 0;
            break;
        case Connector.WRITE:
            checkWritePermission(fileURL, mode);
            maxIStreams = 0;
            break;
        case Connector.READ_WRITE:
            checkReadPermission(fileURL, mode);
            checkWritePermission(fileURL, mode);
            break;
        default:
            throw new IllegalArgumentException("Invalid mode");
        }

        this.fileURL = fileURL;
        this.mode = mode;

        int nameLength = name.length();
        int pathStart = name.indexOf('/', rootStart + 1);

        if (pathStart == -1) {
            throw new IllegalArgumentException("Root is not specified");
        }

        if (pathStart == (nameLength - 1)) {
            fileName = "";
            fileRoot = name.substring(rootStart + 1);
            filePath = name.substring(rootStart);
        } else {
            fileRoot = name.substring(rootStart + 1, pathStart + 1);

            int fileStart = name.lastIndexOf('/', nameLength - 2);

            if (fileStart <= pathStart) {
                fileName = name.substring(pathStart + 1);
                filePath = name.substring(rootStart, pathStart + 1);
            } else {
                filePath = name.substring(rootStart, fileStart + 1);
                fileName = name.substring(fileStart + 1);
            }
        }

        connectionOpen = true;
        return this;
    }

    /**
     * Checks if path is a root path.
     * @param root path to be checked
     * @return <code>true</code> if path is a root,
     *                <code>false</code> otherwise.
     */
    private boolean isRoot(String root) {
        Vector r = listRoots(); // retrieve up-to-date list of mounted roots
        for (int i = 0; i < r.size(); i++) {
        	
            String name = (String)r.elementAt(i);
            if (name.equals(root)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that the connection is already open and connected.
     * @throws ConnectionClosedException if the connection is closed
     * @throws IOException if any error occurs while connecting
     */
    protected void ensureOpenAndConnected() throws IOException {
        if (!isOpen()) {
            throw new ConnectionClosedException("Connection is closed");
        }

        ensureConnected();
    }

    /**
     * Checks that the application has permission to read.
     * @param fileURL complete file URL
     * @param mode access mode
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if read is not allowed
     * @throws IllegalModeException if connection is write only
     */
    private final void checkReadPermission(String fileURL, int mode)
            throws InterruptedIOException {

        if (classSecurityToken == null) { // FC permission
            MIDletSuite suite = Scheduler.getScheduler().getMIDletSuite();

            try {
                suite.checkForPermission(Permissions.FILE_CONNECTION_READ,
                    fileURL);
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        } else { // call from PIM
            classSecurityToken.checkIfPermissionAllowed(
                Permissions.FILE_CONNECTION_READ);
        }

        if (mode == Connector.WRITE) {
            throw new IllegalModeException("Connection is write only");
        }
    }

    /**
     * Checks that the application has permission to read.
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if read is not allowed
     * @throws IllegalModeException if connection is write only
     */
    protected final void checkReadPermission() throws InterruptedIOException {
        checkReadPermission(fileURL, mode);
    }

    /**
     * Checks that the application has permission to read the root path.
     * @throws InterruptedIOException if the permission dialog is
     * terminated before completed
     * @throws SecurityException if read is not allowed
     * @throws IllegalModeException if connection is write only
     */
    protected final void checkRootReadPermission()
            throws InterruptedIOException {

        if (classSecurityToken == null) { // FC permission
            MIDletSuite suite = Scheduler.getScheduler().getMIDletSuite();

            try {
                suite.checkForPermission(Permissions.FILE_CONNECTION_READ,
                                     "file://" + fileRoot);
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        } else { // call from PIM
            classSecurityToken.checkIfPermissionAllowed
                (Permissions.FILE_CONNECTION_READ);
        }

        if (mode == Connector.WRITE) {
            throw new IllegalModeException("Connection is write only");
        }
    }

    /**
     * Checks that the application has permission to write.
     * @param fileURL complete file URL
     * @param mode access mode
     * @throws InterruptedIOException if the permission dialog is
     * terminated before completed
     * @throws SecurityException if write is not allowed
     * @throws IllegalModeException if connection is read only
     */
    private final void checkWritePermission(String fileURL, int mode)
            throws InterruptedIOException {

        if (classSecurityToken == null) { // FC permission
            MIDletSuite suite = Scheduler.getScheduler().getMIDletSuite();

            try {
                suite.checkForPermission(Permissions.FILE_CONNECTION_WRITE,
                                     fileURL);
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        } else { // call from PIM
            classSecurityToken.checkIfPermissionAllowed
                (Permissions.FILE_CONNECTION_WRITE);
        }

        if (mode == Connector.READ) {
            throw new IllegalModeException("Connection is read only");
        }
    }

    /**
     * Checks that the application has permission to write.
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if write is not allowed
     * @throws IllegalModeException if connection is read only
     */
    protected final void checkWritePermission() throws InterruptedIOException {
        checkWritePermission(fileURL, mode);
    }


    /**
     * Gets an array of file system roots.
     * @return up-to-date array of file system roots;
     *         empty array is returned if there are no roots available.
     */
    public static Vector listRoots() {
        BaseFileHandler fh = getFileHandler();
        return fh.listRoots();
    }

    /**
     * Gets a filtered list of files and directories contained in a directory.
     * The directory is the connection's target as specified in
     * <code>Connector.open()</code>.
     *
     * @param   filter String against which all files and directories are
     *          matched for retrieval.  An asterisk ("*") can be used as a
     *          wildcard to represent 0 or more occurrences of any character.
     * @param   includeHidden boolean indicating whether files marked as hidden
     *          should be included or not in the list of files and directories
     *          returned.
     * @return  An Enumeration of strings, denoting the files and directories
     *          in the directory matching the filter. Directories are denoted
     *          with a trailing slash "/" in their returned name.  The
     *          Enumeration has zero length if the directory is empty or no
     *      files and/or directories are found matching the given filter.
     *      Any current directory indication (".") and any parent directory
     *      indication ("..") is not included in the list of files and
     *      directories returned.
     * @exception  SecurityException if the security of the application does
     *          not have read access for the directory.
     * @exception  IllegalModeException if the application does have read
     *          access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @exception  IOException if invoked on a file, the directory does not
     *      exists, the directory is not accessible, or an I/O error occurs.
     * @exception  ConnectionClosedException if the connection is closed.
     * @exception  IllegalArgumentException if filter contains any path
     *      specification or is an invalid filename for the platform
     *      (e.g. contains characters invalid for a filename on the
     *          platform).
     */
    private Enumeration listInternal(String filter, boolean includeHidden)
        throws IOException {
        checkReadPermission();

        ensureOpenAndConnected();

        if (filter != null) {
            if (filter.indexOf('/') != -1) {
                throw new IllegalArgumentException(
                    "Filter contains any path specification");
            }

            String illegalChars = fileHandler.illegalFileNameChars();
            for (int i = 0; i < illegalChars.length(); i++) {
                if (filter.indexOf(illegalChars.charAt(i)) != -1) {
                    throw new
                        IllegalArgumentException("Filter contains characters "
                            + "invalid for a filename");
                }
            }
        }

        return fileHandler.list(filter, includeHidden).elements();
    }

    /**
     * Gets the file handler.
     * @return handle to current file connection
     */
    private static BaseFileHandler getFileHandler() {
        String def = "com.sun.midp.io.j2me.file.DefaultFileHandler";
        String n = null;
        if (hasOtherFileHandler) {
            n = Configuration.getProperty(
                               "com.sun.midp.io.j2me.fileHandlerImpl");
            if (n == null) {
                hasOtherFileHandler = false;
            }
        }
        if (hasOtherFileHandler) {
            try {
                return (BaseFileHandler) (Class.forName(n)).newInstance();
            } catch (ClassNotFoundException e) {
                hasOtherFileHandler = false;
            } catch (Error e) {
                hasOtherFileHandler = false;
            } catch (IllegalAccessException e) {
                hasOtherFileHandler = false;
            } catch (InstantiationException e) {
                hasOtherFileHandler = false;
            }
        }
        try {
            return (BaseFileHandler) (Class.forName(def)).newInstance();
        } catch (ClassNotFoundException e) {
        } catch (Error e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
        throw new Error("Unable to create FileConnection Handler");
    }
}
/**
 * Utility for escaped character handling.
 */
class EscapedUtil {
    /**
     * Gets the escaped string.
     * @param name string to be processed
     * @return escaped string
     * @throws IllegalArgumentException if encoding not supported
     */
    public static String getEscapedString(String name) {
        try {
            if (name == null) {
                return null;
            }
            byte newName[] = new byte[name.length()*12];
            int nextPlace = 0;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (containsReserved(c)) {
                    char data[] = {c};
                    byte[] reservedBytes = new String(data).getBytes("utf-8");
                    for (int j = 0; j < reservedBytes.length; j++) {
                        newName[nextPlace++] = '%';
                        byte upper = (byte) ((reservedBytes[j] >> 4) & 0xF);
                        if (upper <= 9) {
                            newName[nextPlace++] = (byte) ('0' + upper);
                        } else {
                            newName[nextPlace++] = (byte) ('A' + (upper - 10));
                        }
                        byte lower = (byte) (reservedBytes[j] & 0xF);
                        if (lower <= 9) {
                            newName[nextPlace++] = (byte) ('0' + lower);
                        } else {
                            newName[nextPlace++] = (byte) ('A' + (lower - 10));
                        }
                    }
                } else {
                    newName[nextPlace++] = (byte)c;
                }
            }
            return new String(newName, 0, nextPlace);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee.getMessage());
        }
    }


    /**
     * Gets the unescaped string.
     * <pre>
     *   escaped   = "%" hex hex
     *   hex       = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                       "a" | "b" | "c" | "d" | "e" | "f"
     * </pre>
     * @param name string to be processed
     * @return escaped string
     * @throws IllegalArgumentException if encoding not supported
     *
     */
    public static String getUnescapedString(String name) {
        try {
            if (name == null) {
                return null;
            }
            if (name.indexOf("%") == -1) {
                return name;
            } else {
                byte newName[] = new byte[name.length()];
                int nextPlace = 0;
                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (c == '%') {
                        String hexNum = name.substring(i+1, i+3).toUpperCase();
                        if (isHexCharsLegal(hexNum)) {
                            c = hexToChar(hexNum);
                            i = i + 2;
                        } else {
                            throw new IllegalArgumentException("Bad format");
                        }
                    } else if (containsReserved(c)) {
                        throw
                            new IllegalArgumentException("Bad escaped format");
                    }
                    newName[nextPlace++] = (byte)c;
                }
                return new String(newName, 0, nextPlace,  "UTF-8");
            }
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee.getMessage());
        }
    }

    /**
     * Checks if the hexadecimal character is valid.
     * @param hexValue string to be checked
     * @return <code>true</code> if all characters are valid
     */
    private static boolean isHexCharsLegal(String hexValue) {
        if ((isDigit(hexValue.charAt(0)) || isABCDEF(hexValue.charAt(0))) &&
            (isDigit(hexValue.charAt(1)) || isABCDEF(hexValue.charAt(1)))) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Converts one hexadecimal char.
     * @param hexValue string to be processed
     * @return normalized hex value
     */
    private static char hexToChar(String hexValue) {
        char c = 0;
        if (isDigit(hexValue.charAt(0))) {
            c += (hexValue.charAt(0) - '0')*16;
        } else {
            c += (hexValue.charAt(0) - 'A' + 10)*16;
        }

        if (isDigit(hexValue.charAt(1))) {
            c += (hexValue.charAt(01) - '0');
        } else {
            c += (hexValue.charAt(1) - 'A' + 10);
        }
        return c;
    }

    /**
     * Checks if character is decimal digit.
     * @param c character to check
     * @return <code>true</code> if in the range 0..9
     */
    private static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * Checks if character is hexadecimal digit.
     * @param c character to check
     * @return  <code>true</code> if in the range A..F
     */
    private static boolean isABCDEF(char c) {
        return (c >= 'A' && c <= 'F');
    }

    /**
     * Checks if character is from the reserved character set.
     * @param c character to check
     * @return  <code>true</code> if not in the range A..Z,
     * a..z,..9, or punctuation (forward slash, colon, hyphen,
     * under score, period, exclamation, tilde, asterisk, single quote,
     * left paren or right paren).
     */
    private static boolean containsReserved(char c) {
        return !((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                 (c >= '0' && c <= '9') || ("/:-_.!~*'()".indexOf(c) != -1));
    }

} // End  of EscapeUtil

/**
 * Input stream for the connection
 */
class FileConnectionInputStream extends InputStream {

    /** Pointer to the connection. */
    private Protocol parent;

    /** Buffer for single char reads. */
    byte[] buf = new byte[1];

    /**
      * Buffer for mark/reset funtionality support.
      * <code>null</code> value indicates <code>mark</code> was not called or
      * <code>readlimit</code> value of the last <code>mark</code> was exceeded.
      */
    byte[] markBuf = null;

    /** The size of data stored in <code>markBuf</code>. */
    int markSize = 0;

    /** Current position in <code>markBuf</code> to read data from. */
    int markPos = 0;

    /**
     * Indicates whether <code>reset</code> method was called.
     * If so, data is read from <code>markBuf</code> buffer
     * otherwise via <code>parent.readBytes</code> method.
     */
    boolean isReadFromBuffer = false;

    /**
     * Constructs a BaseInputStream for a ConnectionBaseAdapter.
     *
     * @param parent pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    FileConnectionInputStream(Protocol parent) throws IOException {
        this.parent = parent;
    }

    /**
     * Check the stream is open
     *
     * @exception  InterruptedIOException  if it is not.
     */
    private void ensureOpen() throws InterruptedIOException {
        if (parent == null) {
            throw new InterruptedIOException("Stream closed");
        }
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * <p>The <code>available</code> method always returns <code>0</code> if
     * {@link ConnectionBaseAdapter#available()} is
     * not overridden by the subclass.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {

        ensureOpen();

        return parent.available();
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        if (read(buf, 0, 1) > 0) {
            return (buf[0] & 0xFF);
        }

        return -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     *
     * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
     * thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * <p> If the first byte cannot be read for any reason other than end of
     * file, then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len) throws IOException {
        int test;

        ensureOpen();

        if (len == 0) {
            return 0;
        }

        /*
         * test the parameters so the subclass will not have to.
         * this will avoid crashes in the native code
         */
        test = b[off] + b[len - 1] + b[off + len - 1];

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            return parent.readBytes(b, off, len);
        }

        // read data from mark buffer if reset method was called
        if (isReadFromBuffer) {
            int dataSize = markSize - markPos;
            if (dataSize > 0) {
                int copySize = (dataSize > len) ? len : dataSize;
                System.arraycopy(markBuf, markPos, b, off, copySize);
                markPos += copySize;

                // read data directly from the stream
                // if size of data in the buffer is not enough
                int readSize = 0;
                if (copySize < len) {
                    readSize = parent.readBytes(
                        b, off + copySize, len - copySize);

                    // check if eos is reached
                    if (readSize == -1) {
                        readSize = 0;
                    } else {
                        // check the mark buffer overflow
                        if (markSize + readSize > markBuf.length) {
                            markBuf = null;
                        // cache the data in the mark buffer
                        } else {
                            System.arraycopy(
                                b, off + copySize, markBuf, markSize, readSize);
                            markSize += readSize;
                        }
                    }

                    isReadFromBuffer = false;
                }

                return copySize + readSize;
            } else {
                isReadFromBuffer = false;
            }
        }

        int readSize = parent.readBytes(b, off, len);

        // fill mark buffer if exists
        if (markBuf != null) {
            if (readSize > 0) {
                // check the mark buffer overflow
                if (markSize + readSize > markBuf.length) {
                    markBuf = null;
                    // cache the data in the mark buffer
                } else {
                    System.arraycopy(b, off, markBuf, markSize, readSize);
                    markSize += readSize;
                }
            }
        }

        return readSize;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (parent != null) {
            parent.closeInputStream();
            parent = null;
        }
	// free buffer used by mark/reset operations if it was allocated
	markBuf = null;
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods.
     *
     * <p>The <code>markSupported</code> method of
     * <code>BaseInputStream</code> returns <code>true</code>.
     *
     * @return  always <code>true</code>
     *
     * @see     BaseInputStream#mark(int)
     * @see     BaseInputStream#reset()
     * @see     java.io.InputStream#markSupported()
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The <code>readlimit</code> arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> The stream remembers all the bytes read after the call to
     * <code>mark</code> and stands ready to supply those same bytes again
     * if and whenever the method <code>reset</code> is called.
     *  However, the stream is not remember any data at all if more
     * than <code>readlimit</code> bytes are read from the stream before
     * <code>reset</code> is called.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     BaseInputStream#reset()
     * @see     java.io.InputStream#reset()
     * @see     java.io.InputStream#mark(int)
     */
    public synchronized void mark(int readlimit) {
        // check whether the stream is closed
        if (parent == null) {
            return;
        }

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            parent.mark(readlimit);
        } else {
            byte[] oldBuf = markBuf;
            markBuf = new byte[readlimit];

            // copy relevant data from old buffer if any
            if (isReadFromBuffer) {
                int oldDataSize = markSize - markPos;
                int copySize = (readlimit > oldDataSize) ?
                    oldDataSize : readlimit;
                System.arraycopy(oldBuf, markPos, markBuf, 0, copySize);
                markSize = copySize;
            } else {
                markSize = 0;
            }
            markPos = 0;
        }
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * <p> If the method <code>mark</code> has not been called since
     * the stream was created, or the number of bytes read from the stream
     * since <code>mark</code> was last called is larger than the argument
     * to <code>mark</code> at that last call, then an
     * <code>IOException</code> is thrown.
     *
     * <p> If such an <code>IOException</code> is not thrown, then the
     * stream is reset to a state such that all the bytes read since the
     * most recent call to <code>mark</code> will be resupplied
     * to subsequent callers of the <code>read</code> method, followed by
     * any bytes that otherwise would have been the next input data as of
     * the time of the call to <code>reset</code>.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *                          mark has been invalidated;
     *                          or if the stream is closed
     * @see     BaseInputStream#mark(int)
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        ensureOpen();

        // use parent's mark/reset functionality
        // if the parent supports the own one
        if (parent.markSupported()) {
            parent.reset();
        } else {
            if (markBuf == null) {
                throw new IOException("Invalid mark position");
            }
            markPos = 0;
            isReadFromBuffer = true;
        }
    }

    static private byte[] dummy_buffer = new byte[64*1024];
    public long skip(long n) throws IOException
    {
        int readBytesTotal = 0;        
        int readBytes = 0;
        while (n > 0){     
            readBytes = read( dummy_buffer, 0, (int)((n > 64*1024) ? 64*1024 : n) );
            if ( readBytes <= 0 ) {
                break;
            } else {
                readBytesTotal += readBytes;
                n -= readBytes;
            }
        }
        return (long)readBytesTotal;
    }
}

