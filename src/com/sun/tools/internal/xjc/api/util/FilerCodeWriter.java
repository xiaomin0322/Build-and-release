/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.internal.xjc.api.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.sun.codemodel.internal.CodeWriter;
import com.sun.codemodel.internal.JPackage;
import com.sun.mirror.apt.Filer;

import static com.sun.mirror.apt.Filer.Location.CLASS_TREE;
import static com.sun.mirror.apt.Filer.Location.SOURCE_TREE;

/**
 * {@link CodeWriter} that generates source code to {@link Filer}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class FilerCodeWriter extends CodeWriter {

    private final Filer filer;

    public FilerCodeWriter(Filer filer) {
        this.filer = filer;
    }

    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        Filer.Location loc;
        if(fileName.endsWith(".java")) {
            // APT doesn't do the proper Unicode escaping on Java source files,
            // so we can't rely on Filer.createSourceFile.
            loc = SOURCE_TREE;
        } else {
            // put non-Java files directly to the output folder
            loc = CLASS_TREE;
        }
        return filer.createBinaryFile(loc,pkg.name(),new File(fileName));
    }

    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String name;
        if(pkg.isUnnamed())
            name = fileName;
        else
            name = pkg.name()+'.'+fileName;

        name = name.substring(0,name.length()-5);   // strip ".java"

        return filer.createSourceFile(name);
    }

    public void close() {
        ; // noop
    }
}
