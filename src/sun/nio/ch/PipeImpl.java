/*
 * Copyright (c) 2002, 2008, Oracle and/or its affiliates. All rights reserved.
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

/*
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.Random;


/**
 * A simple Pipe implementation based on a socket connection.
 */

class PipeImpl
    extends Pipe
{

    // Source and sink channels
    private SourceChannel source;
    private SinkChannel sink;

    // Random object for handshake values
    private static final Random rnd;

    static {
        Util.load();
        byte[] someBytes = new byte[8];
        boolean resultOK = IOUtil.randomBytes(someBytes);
        if (resultOK) {
            rnd = new Random(ByteBuffer.wrap(someBytes).getLong());
        } else {
            rnd = new Random();
        }
    }

    private class Initializer
        implements PrivilegedExceptionAction<Void>
    {

        private final SelectorProvider sp;

        private Initializer(SelectorProvider sp) {
            this.sp = sp;
        }

        public Void run() throws IOException {
            ServerSocketChannel ssc = null;
            SocketChannel sc1 = null;
            SocketChannel sc2 = null;

            try {
                // loopback address
                InetAddress lb = InetAddress.getByName("127.0.0.1");
                assert(lb.isLoopbackAddress());

                // bind ServerSocketChannel to a port on the loopback address
                ssc = ServerSocketChannel.open();
                ssc.socket().bind(new InetSocketAddress(lb, 0));

                // Establish connection (assumes connections are eagerly
                // accepted)
                InetSocketAddress sa
                    = new InetSocketAddress(lb, ssc.socket().getLocalPort());
                sc1 = SocketChannel.open(sa);

                ByteBuffer bb = ByteBuffer.allocate(8);
                long secret = rnd.nextLong();
                bb.putLong(secret).flip();
                sc1.write(bb);

                // Get a connection and verify it is legitimate
                for (;;) {
                    sc2 = ssc.accept();
                    bb.clear();
                    sc2.read(bb);
                    bb.rewind();
                    if (bb.getLong() == secret)
                        break;
                    sc2.close();
                }

                // Create source and sink channels
                source = new SourceChannelImpl(sp, sc1);
                sink = new SinkChannelImpl(sp, sc2);
            } catch (IOException e) {
                try {
                    if (sc1 != null)
                        sc1.close();
                    if (sc2 != null)
                        sc2.close();
                } catch (IOException e2) { }
                IOException x = new IOException("Unable to establish"
                                                + " loopback connection");
                x.initCause(e);
                throw x;
            } finally {
                try {
                    if (ssc != null)
                        ssc.close();
                } catch (IOException e2) { }
            }
            return null;
        }
    }

    PipeImpl(final SelectorProvider sp) throws IOException {
        try {
            AccessController.doPrivileged(new Initializer(sp));
        } catch (PrivilegedActionException x) {
            throw (IOException)x.getCause();
        }
    }


    public SourceChannel source() {
        return source;
    }

    public SinkChannel sink() {
        return sink;
    }

}
