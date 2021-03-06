/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/**
 * @test
 * @compile HelloPathWorld.java
 * @run main CompileTest
 */

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.*;

import com.sun.tools.javac.nio.*;
import com.sun.tools.javac.util.Context;
import java.nio.file.spi.FileSystemProvider;


public class CompileTest {
    public static void main(String[] args) throws Exception {
        new CompileTest().run();
    }

    public void run() throws Exception {
        File rtDir = new File("rt.dir");
        File javaHome = new File(System.getProperty("java.home"));
        if (javaHome.getName().equals("jre"))
            javaHome = javaHome.getParentFile();
        File rtJar = new File(new File(new File(javaHome, "jre"), "lib"), "rt.jar");
        expand(rtJar, rtDir);

        String[] rtDir_opts = {
            "-bootclasspath", rtDir.toString(),
            "-classpath", "",
            "-sourcepath", "",
            "-extdirs", ""
        };
        test(rtDir_opts, "HelloPathWorld");

        if (isJarFileSystemAvailable()) {
            String[] rtJar_opts = {
                "-bootclasspath", rtJar.toString(),
                "-classpath", "",
                "-sourcepath", "",
                "-extdirs", ""
            };
            test(rtJar_opts, "HelloPathWorld");

            String[] default_opts = { };
            test(default_opts, "HelloPathWorld");

            // finally, a non-trivial program
            test(default_opts, "CompileTest");
        } else
            System.err.println("jar file system not available: test skipped");
    }

    void test(String[] opts, String className) throws Exception {
        count++;
        System.err.println("Test " + count + " " + Arrays.asList(opts) + " " + className);
        Path testSrcDir = Paths.get(System.getProperty("test.src"));
        Path testClassesDir = Paths.get(System.getProperty("test.classes"));
        Path classes = Paths.get("classes." + count);
        classes.createDirectory();

        Context ctx = new Context();
        PathFileManager fm = new JavacPathFileManager(ctx, true, null);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList(opts));
        options.addAll(Arrays.asList(
                "-verbose", "-XDverboseCompilePolicy",
                "-d", classes.toString()
        ));
        Iterable<? extends JavaFileObject> compilationUnits =
                fm.getJavaFileObjects(testSrcDir.resolve(className + ".java"));
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        JavaCompiler.CompilationTask t =
                compiler.getTask(out, fm, null, options, null, compilationUnits);
        boolean ok = t.call();
        System.err.println(sw.toString());
        if (!ok) {
            throw new Exception("compilation failed");
        }

        File expect = new File("classes." + count + "/" + className + ".class");
        if (!expect.exists())
            throw new Exception("expected file not found: " + expect);
        long expectedSize = new File(testClassesDir.toString(), className + ".class").length();
        long actualSize = expect.length();
        if (expectedSize != actualSize)
            throw new Exception("wrong size found: " + actualSize + "; expected: " + expectedSize);
    }

    boolean isJarFileSystemAvailable() {
        boolean result = false;
        for (FileSystemProvider fsp: FileSystemProvider.installedProviders()) {
            String scheme = fsp.getScheme();
            System.err.println("Provider: " + scheme + " " + fsp);
            if (scheme.equalsIgnoreCase("jar") || scheme.equalsIgnoreCase("zip"))
                result = true;
        }
        return result;
    }

    void expand(File jar, File dir) throws IOException {
        JarFile jarFile = new JarFile(jar);
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (!je.isDirectory()) {
                    copy(jarFile.getInputStream(je), new File(dir, je.getName()));
                }
            }
        } finally {
            jarFile.close();
        }
    }

    void copy(InputStream in, File dest) throws IOException {
        dest.getParentFile().mkdirs();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        try {
            byte[] data = new byte[8192];
            int n;
            while ((n = in.read(data, 0, data.length)) > 0)
                out.write(data, 0, n);
        } finally {
            out.close();
            in.close();
        }
    }

    void error(String message) {
        System.err.println("Error: " + message);
        errors++;
    }

    int errors;
    int count;
}
