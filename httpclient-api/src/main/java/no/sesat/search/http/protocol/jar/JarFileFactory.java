/*
 * @(#)JarFileFactory.java	1.37 06/02/27
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package no.sesat.search.http.protocol.jar;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.security.Permission;

/* A factory for cached JAR file. This class is used to both retrieve
 * and cache Jar files.
 *
 *
 * @since JDK1.2
 */
class JarFileFactory implements URLJarFile.URLJarFileCloseController {
    /* the url to file cache */
    private static HashMap fileCache = new HashMap();

    /* the file to url cache */
    private static HashMap urlCache = new HashMap();

    URLConnection getConnection(JarFile jarFile) throws IOException {
        URL u = (URL) urlCache.get(jarFile);
        if (u != null) {
            return u.openConnection();
        }
        return null;
    }

    public JarFile get(URL url) throws IOException {
        return get(url, null);
    }

    JarFile get(URL url, JarURLConnection parentConnection) throws IOException {

        JarFile result = null;
        JarFile local_result = null;

        if (null == parentConnection || parentConnection.getUseCaches()) {
            synchronized (this) {
                result = getCachedJarFile(url);
            }
            if (result == null) {
                local_result = URLJarFile.getJarFile(url, parentConnection);
                synchronized (this) {
                    result = getCachedJarFile(url);
                    if (result == null) {
                        fileCache.put(url, local_result);
                        urlCache.put(local_result, url);
                        result = local_result;

                    }else {
                        if (local_result != null) {
                            local_result.close();
                        }
                    }
                }
            }
        }else {
            result = URLJarFile.getJarFile(url, parentConnection);
        }
        if (result == null) {
            throw new FileNotFoundException(url.toString());
        }
        return result;
    }

    /**
     * Callback method of the URLJarFileCloseController to
     * indicate that the JarFile is close. This way we can
     * remove the JarFile from the cache
     */
    public void close(JarFile jarFile) {
        URL urlRemoved = (URL) urlCache.remove(jarFile);
        if (urlRemoved != null) {
            fileCache.remove(urlRemoved);
        }
    }

    private JarFile getCachedJarFile(URL url) {
        JarFile result = (JarFile) fileCache.get(url);

        /* if the JAR file is cached, the permission will always be there */
        if (result != null) {
            Permission perm = getPermission(result);
            if (perm != null) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    try {
                        sm.checkPermission(perm);
                    }
                    catch (SecurityException se) {
                        // fallback to checkRead/checkConnect for pre 1.2
                        // security managers
                        if ((perm instanceof java.io.FilePermission) && perm.getActions().indexOf("read") != -1) {
                            sm.checkRead(perm.getName());
                        }
                        else if ((perm instanceof java.net.SocketPermission) && perm.getActions().indexOf("connect") != -1) {
                            sm.checkConnect(url.getHost(), url.getPort());
                        }
                        else {
                            throw se;
                        }
                    }
                }
            }
        }
        return result;
    }

    private Permission getPermission(JarFile jarFile) {
        try {
            URLConnection uc = getConnection(jarFile);
            if (uc != null) {
                return uc.getPermission();
            }
        }
        catch (IOException ioe) {
            // gulp
        }

        return null;
    }
}
