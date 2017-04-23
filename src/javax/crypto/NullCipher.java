/*
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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

package javax.crypto;

/**
 * The NullCipher class is a class that provides an
 * "identity cipher" -- one that does not tranform the plaintext.  As
 * a consequence, the ciphertext is identical to the plaintext.  All
 * initialization methods do nothing, while the blocksize is set to 1
 * byte.
 *
 * @author  Li Gong
 * @since 1.4
 */

public class NullCipher extends Cipher {

    public NullCipher() {
        super(new NullCipherSpi(), null);
    }
}
