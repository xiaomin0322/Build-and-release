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

package com.sun.tools.internal.xjc.generator.annotation.spec;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import com.sun.codemodel.internal.JAnnotationWriter;

public interface XmlAccessorOrderWriter
    extends JAnnotationWriter<XmlAccessorOrder>
{


    XmlAccessorOrderWriter value(XmlAccessOrder value);

}
