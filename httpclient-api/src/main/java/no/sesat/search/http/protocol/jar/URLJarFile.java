/*
 * @(#)URLJarFile.java	1.11 06/02/27
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package no.sesat.search.http.protocol.jar;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.jar.URLJarFileCallBack;

/* URL jar file is a common JarFile subtype used for JarURLConnection */
public class URLJarFile extends JarFile {
    /*
     * Interface to be able to call retrieve() in plugin if
     * this variable is set.
     */
    private static URLJarFileCallBack callback = null;

    /* Controller of the Jar File's closing */
    private URLJarFileCloseController closeController = null;

    private static int BUF_SIZE = 2048;

    private Manifest superMan;

    private Attributes superAttr;

    private Map superEntries;

    static JarFile getJarFile(URL url) throws IOException {
        return getJarFile(url, null);
    }

    static JarFile getJarFile(URL url, JarURLConnection parentConnection) throws IOException {
        if (isFileURL(url)) {
            return new URLJarFile(url, parentConnection.factory);
        }
        else {
            return retrieve(url, parentConnection);
        }
    }

    /*
     * Changed modifier from private to public in order to be able
     * to instantiate URLJarFile from sun.plugin package.
     */
    public URLJarFile(File file) throws IOException {
        this(file, null);
    }

    /*
     * Changed modifier from private to public in order to be able
     * to instantiate URLJarFile from sun.plugin package.
     */
    public URLJarFile(File file, URLJarFileCloseController closeController) throws IOException {
        super(file, true, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
        this.closeController = closeController;
    }

    private URLJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        super(ParseUtil.decode(url.getFile()));
        this.closeController = closeController;
    }

    private static boolean isFileURL(URL url) {
        if (url.getProtocol().equalsIgnoreCase("file")) {
            /*
             * Consider this a 'file' only if it's a LOCAL file, because
             * 'file:' URLs can be accessible through ftp.
             */
            String host = url.getHost();
            if (host == null || host.equals("") || host.equals("~") || host.equalsIgnoreCase("localhost")) {
                return true;
            }
        }
        return false;
    }

    /*
     * close the jar file.
     */
    @Override
    protected void finalize() throws IOException {
        close();
    }

    /**
     * Returns the <code>ZipEntry</code> for the given entry name or
     * <code>null</code> if not found.
     *
     * @param name the JAR file entry name
     * @return the <code>ZipEntry</code> for the given entry name or
     *         <code>null</code> if not found
     * @see java.util.zip.ZipEntry
     */
    @Override
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            if (ze instanceof JarEntry) {
                return new URLJarFileEntry((JarEntry) ze);
            }
            else {
                throw new InternalError(super.getClass() + " returned unexpected entry type " + ze.getClass());
            }
        }
        return null;
    }

    @Override
    public Manifest getManifest() throws IOException {

        if (!isSuperMan()) {
            return null;
        }

        Manifest man = new Manifest();
        Attributes attr = man.getMainAttributes();
        attr.putAll((Map) superAttr.clone());

        // now deep copy the manifest entries
        if (superEntries != null) {
            Map entries = man.getEntries();
            Iterator it = superEntries.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Attributes at = (Attributes) superEntries.get(key);
                entries.put(key, at.clone());
            }
        }

        return man;
    }

    /* If close controller is set the notify the controller about the pending close */
    @Override
    public void close() throws IOException {
        if (closeController != null) {
            closeController.close(this);
        }
        super.close();
    }

    // optimal side-effects
    private synchronized boolean isSuperMan() throws IOException {

        if (superMan == null) {
            superMan = super.getManifest();
        }

        if (superMan != null) {
            superAttr = superMan.getMainAttributes();
            superEntries = superMan.getEntries();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Given a URL, retrieves a JAR file, caches it to disk, and creates a
     * cached JAR file object.
     */
    private static JarFile retrieve(final URL url) throws IOException {
        return retrieve(url, null);
    }

    /**
     * Given a URL, retrieves a JAR file, caches it to disk, and creates a
     * cached JAR file object.
     */
    private static JarFile retrieve(
            final URL url,
            final JarURLConnection parentConnection) throws IOException {
        /*
         * See if interface is set, then call retrieve function of the class
         * that implements URLJarFileCallBack interface (sun.plugin - to
         * handle the cache failure for JARJAR file.)
         */
        if (callback != null) {
            return callback.retrieve(url);

        }else {

            JarFile result = null;

            final URLConnection connection = url.openConnection();
            // propagate request properties downwards. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6270774
            if(null != parentConnection){
                for(Map.Entry<String,List<String>> entry : parentConnection.getRequestProperties().entrySet()){
                    for(String value : entry.getValue()){
                        connection.addRequestProperty(entry.getKey(), value);
                    }
                }
            }

            /* get the stream before asserting privileges */
            final InputStream in = connection.getInputStream();

            try {
                result = (JarFile) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        OutputStream out = null;
                        File tmpFile = null;
                        try {
                            tmpFile = File.createTempFile("jar_cache", null);
                            tmpFile.deleteOnExit();
                            out  = new FileOutputStream(tmpFile);
                            int read = 0;
                            byte[] buf = new byte[BUF_SIZE];
                            while ((read = in.read(buf)) != -1) {
                                out.write(buf, 0, read);
                            }
                            out.close();
                            out = null;
                            return new URLJarFile(tmpFile, null != parentConnection.factory ? parentConnection.factory : null);
                        } catch (IOException e) {
                            if (tmpFile != null) {
                                tmpFile.delete();
                            }
                            throw e;
                        } finally {
                            if (in != null) {
                            in.close();
                            }
                            if (out != null) {
                            out.close();
                            }
                        }
                    }
		    });

            }catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }

            return result;
        }
    }

    /*
     * Set the call back interface to call retrive function in sun.plugin
     * package if plugin is running.
     */
    public static void setCallBack(URLJarFileCallBack cb) {
        callback = cb;
    }

    private class URLJarFileEntry extends JarEntry {
        private JarEntry je;

        URLJarFileEntry(JarEntry je) {
            super(je);
            this.je = je;
        }

        @Override
        public Attributes getAttributes() throws IOException {
            if (URLJarFile.this.isSuperMan()) {
                Map e = URLJarFile.this.superEntries;
                if (e != null) {
                    Attributes a = (Attributes) e.get(getName());
                    if (a != null) {
                        return (Attributes) a.clone();
                    }
                }
            }
            return null;
        }

        @Override
        public java.security.cert.Certificate[] getCertificates() {
            Certificate[] certs = je.getCertificates();
            return certs == null ? null : (Certificate[]) certs.clone();
        }

        @Override
        public CodeSigner[] getCodeSigners() {
            CodeSigner[] csg = je.getCodeSigners();
            return csg == null ? null : (CodeSigner[]) csg.clone();
        }
    }

    public interface URLJarFileCloseController {
        public void close(JarFile jarFile);
    }
}
