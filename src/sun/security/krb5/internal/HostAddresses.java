/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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
 *
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 */

package sun.security.krb5.internal;

import sun.security.krb5.PrincipalName;
import sun.security.krb5.KrbException;
import sun.security.krb5.Asn1Exception;
import sun.security.util.*;
import java.util.Vector;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.io.IOException;
import sun.security.krb5.internal.ccache.CCacheOutputStream;

/**
 * Implements the ASN.1 HostAddresses type.
 *
 * <xmp>
 * HostAddresses   -- NOTE: subtly different from rfc1510,
 *                 -- but has a value mapping and encodes the same
 *         ::= SEQUENCE OF HostAddress
 *
 * HostAddress     ::= SEQUENCE  {
 *         addr-type       [0] Int32,
 *         address         [1] OCTET STRING
 * }
 * </xmp>
 *
 * <p>
 * This definition reflects the Network Working Group RFC 4120
 * specification available at
 * <a href="http://www.ietf.org/rfc/rfc4120.txt">
 * http://www.ietf.org/rfc/rfc4120.txt</a>.
 */

public class HostAddresses implements Cloneable {
    private static boolean DEBUG = sun.security.krb5.internal.Krb5.DEBUG;
    private HostAddress[] addresses = null;
    private volatile int hashCode = 0;

    public HostAddresses(HostAddress[] new_addresses) throws IOException {
        if (new_addresses != null) {
           addresses = new HostAddress[new_addresses.length];
           for (int i = 0; i < new_addresses.length; i++) {
                if (new_addresses[i] == null) {
                   throw new IOException("Cannot create a HostAddress");
                } else {
                   addresses[i] = (HostAddress)new_addresses[i].clone();
                }
           }
        }
    }

    public HostAddresses() throws UnknownHostException {
        addresses = new HostAddress[1];
        addresses[0] = new HostAddress();
    }

    private HostAddresses(int dummy) {}

    public HostAddresses(PrincipalName serverPrincipal)
        throws UnknownHostException, KrbException {

        String[] components = serverPrincipal.getNameStrings();

        if (serverPrincipal.getNameType() != PrincipalName.KRB_NT_SRV_HST ||
            components.length < 2)
            throw new KrbException(Krb5.KRB_ERR_GENERIC, "Bad name");

        String host = components[1];
        InetAddress addr[] = InetAddress.getAllByName(host);
        HostAddress hAddrs[] = new HostAddress[addr.length];

        for (int i = 0; i < addr.length; i++) {
            hAddrs[i] = new HostAddress(addr[i]);
        }

        addresses = hAddrs;
    }

    public Object clone() {
        HostAddresses new_hostAddresses = new HostAddresses(0);
        if (addresses != null) {
            new_hostAddresses.addresses = new HostAddress[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                new_hostAddresses.addresses[i] =
                        (HostAddress)addresses[i].clone();
            }
        }
        return new_hostAddresses;
    }

    public boolean inList(HostAddress addr) {
        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++)
                if (addresses[i].equals(addr))
                    return true;
        }
        return false;
    }

    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            if (addresses != null) {
                for (int i=0; i < addresses.length; i++)  {
                    result = 37*result + addresses[i].hashCode();
                }
            }
            hashCode = result;
        }
        return hashCode;

    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof HostAddresses)) {
            return false;
        }

        HostAddresses addrs = (HostAddresses)obj;
        if ((addresses == null && addrs.addresses != null) ||
            (addresses != null && addrs.addresses == null))
            return false;
        if (addresses != null && addrs.addresses != null) {
            if (addresses.length != addrs.addresses.length)
                return false;
            for (int i = 0; i < addresses.length; i++)
                if (!addresses[i].equals(addrs.addresses[i]))
                    return false;
        }
        return true;
    }

   /**
    * Constructs a new <code>HostAddresses</code> object.
    * @param encoding a single DER-encoded value.
    * @exception Asn1Exception if an error occurs while decoding an
    * ASN1 encoded data.
    * @exception IOException if an I/O error occurs while reading
    * encoded data.
    */
    public HostAddresses(DerValue encoding)
        throws  Asn1Exception, IOException {
        Vector<HostAddress> tempAddresses = new Vector<>();
        DerValue der = null;
        while (encoding.getData().available() > 0) {
            der = encoding.getData().getDerValue();
            tempAddresses.addElement(new HostAddress(der));
        }
        if (tempAddresses.size() > 0) {
            addresses = new HostAddress[tempAddresses.size()];
            tempAddresses.copyInto(addresses);
        }
    }


   /**
    * Encodes a <code>HostAddresses</code> object.
    * @return byte array of encoded <code>HostAddresses</code> object.
    * @exception Asn1Exception if an error occurs while decoding an
    * ASN1 encoded data.
    * @exception IOException if an I/O error occurs while reading
    * encoded data.
    */
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();

        if (addresses != null && addresses.length > 0) {
            for (int i = 0; i < addresses.length; i++)
                bytes.write(addresses[i].asn1Encode());
        }
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }

    /**
     * Parse (unmarshal) a <code>HostAddresses</code> from a DER input stream.
     * This form
     * parsing might be used when expanding a value which is part of
     * a constructed sequence and uses explicitly tagged type.
     *
     * @exception Asn1Exception if an Asn1Exception occurs.
     * @param data the Der input stream value, which contains one or more
     * marshaled value.
     * @param explicitTag tag number.
     * @param optional indicates if this data field is optional.
     * @return an instance of <code>HostAddresses</code>.
     */
    public static HostAddresses parse(DerInputStream data,
                                      byte explicitTag, boolean optional)
        throws Asn1Exception, IOException {
        if ((optional) &&
            (((byte)data.peekByte() & (byte)0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            return new HostAddresses(subDer);
        }
    }

    /**
         * Writes data field values in <code>HostAddresses</code> in FCC
         * format to a <code>CCacheOutputStream</code>.
         *
         * @param cos a <code>CCacheOutputStream</code> to be written to.
         * @exception IOException if an I/O exception occurs.
         * @see sun.security.krb5.internal.ccache.CCacheOutputStream
         */

    public void writeAddrs(CCacheOutputStream cos) throws IOException {
        cos.write32(addresses.length);
        for (int i = 0; i < addresses.length; i++) {
            cos.write16(addresses[i].addrType);
            cos.write32(addresses[i].address.length);
            cos.write(addresses[i].address, 0,
                      addresses[i].address.length);
        }
    }


    public InetAddress[] getInetAddresses() {

        if (addresses == null || addresses.length == 0)
            return null;

        ArrayList<InetAddress> ipAddrs = new ArrayList<>(addresses.length);

        for (int i = 0; i < addresses.length; i++) {
            try {
                if ((addresses[i].addrType == Krb5.ADDRTYPE_INET) ||
                    (addresses[i].addrType == Krb5.ADDRTYPE_INET6)) {
                    ipAddrs.add(addresses[i].getInetAddress());
                }
            } catch (java.net.UnknownHostException e) {
                // Should not happen since IP address given
                return null;
            }
        }

        InetAddress[] retVal = new InetAddress[ipAddrs.size()];
        return ipAddrs.toArray(retVal);

    }

    /**
     * Returns all the IP addresses of the local host.
     */
    public static HostAddresses getLocalAddresses() throws IOException
    {
        String hostname = null;
        InetAddress[] inetAddresses = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();
            inetAddresses = InetAddress.getAllByName(hostname);
            HostAddress[] hAddresses = new HostAddress[inetAddresses.length];
            for (int i = 0; i < inetAddresses.length; i++)
                {
                    hAddresses[i] = new HostAddress(inetAddresses[i]);
                }
            if (DEBUG) {
                System.out.println(">>> KrbKdcReq local addresses for "
                                   + hostname + " are: ");

                for (int i = 0; i < inetAddresses.length; i++) {
                    System.out.println("\n\t" + inetAddresses[i]);
                    if (inetAddresses[i] instanceof Inet4Address)
                        System.out.println("IPv4 address");
                    if (inetAddresses[i] instanceof Inet6Address)
                        System.out.println("IPv6 address");
                }
            }
            return (new HostAddresses(hAddresses));
        } catch (Exception exc) {
            throw new IOException(exc.toString());
        }

    }

    /**
     * Creates a new HostAddresses instance from the supplied list
     * of InetAddresses.
     */
    public HostAddresses(InetAddress[] inetAddresses)
    {
        if (inetAddresses == null)
            {
                addresses = null;
                return;
            }

        addresses = new HostAddress[inetAddresses.length];
        for (int i = 0; i < inetAddresses.length; i++)
            addresses[i] = new HostAddress(inetAddresses[i]);
    }
}
