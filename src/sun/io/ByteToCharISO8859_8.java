/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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

package sun.io;

import sun.nio.cs.ext.ISO_8859_8;

/**
 * A table to convert ISO8859_8 to Unicode
 *
 * @author  ConverterGenerator tool
 */

public class ByteToCharISO8859_8 extends ByteToCharSingleByte {

    private final static ISO_8859_8 nioCoder = new ISO_8859_8();

    public String getCharacterEncoding() {
        return "ISO8859_8";
    }

    public ByteToCharISO8859_8() {
        super.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
    }
}
