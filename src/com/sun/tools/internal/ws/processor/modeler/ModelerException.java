/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.internal.ws.processor.modeler;

import com.sun.tools.internal.ws.processor.ProcessorException;
import com.sun.xml.internal.ws.util.localization.Localizable;

/**
 * ModelerException represents an exception that occurred while
 * visiting service model.
 *
 * @see ProcessorException
 *
 * @author WS Development Team
*/
public class ModelerException extends ProcessorException {

    public ModelerException(String key, Object... args) {
        super(key, args);
    }

    public ModelerException(Throwable throwable) {
        super(throwable);
    }

    public ModelerException(Localizable arg) {
        super("modeler.nestedModelError", arg);
    }

    public String getDefaultResourceBundleName() {
        return "com.sun.tools.internal.ws.resources.modeler";
    }

}
