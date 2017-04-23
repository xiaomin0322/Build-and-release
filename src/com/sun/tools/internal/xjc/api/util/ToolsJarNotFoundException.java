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

/**
 * Signals an error when tools.jar was not found.
 *
 * Simply print out the message obtained by {@link #getMessage()}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ToolsJarNotFoundException extends Exception {
    /**
     * Location where we expected to find tools.jar
     */
    public final File toolsJar;

    public ToolsJarNotFoundException(File toolsJar) {
        super(calcMessage(toolsJar));
        this.toolsJar = toolsJar;
    }

    private static String calcMessage(File toolsJar) {
        return Messages.TOOLS_JAR_NOT_FOUND.format(toolsJar.getPath());
    }
}
