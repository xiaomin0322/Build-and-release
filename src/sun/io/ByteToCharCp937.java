/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
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

import sun.nio.cs.ext.*;

public class ByteToCharCp937 extends ByteToCharDBCS_EBCDIC {

    // Return the character set id
    public String getCharacterEncoding() {
        return "Cp937";
    }

    public ByteToCharCp937() {
        super((DoubleByte.Decoder)new IBM937().newDecoder());
    }
}
