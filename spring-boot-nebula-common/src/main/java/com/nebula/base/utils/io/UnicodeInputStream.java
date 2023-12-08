package com.nebula.base.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author : wh
 * @date : 2022/1/14 13:55
 * @description:
 */
public class UnicodeInputStream extends InputStream{

    public static final int MAX_BOM_SIZE = 4;

    private final PushbackInputStream internalInputStream;
    private boolean initialized;
    private int BOMSize = -1;
    private Charset encoding;
    private final Charset targetEncoding;

    /**
     * Creates new unicode stream. It works in two modes: detect mode and read mode.
     * <p>
     * Detect mode is active when target encoding is not specified.
     * In detect mode, it tries to detect encoding from BOM if exist.
     * If BOM doesn't exist, encoding is not detected.
     * <p>
     * Read mode is active when target encoding is set. Then this stream reads
     * optional BOM for given encoding. If BOM doesn't exist, nothing is skipped.
     */
    public UnicodeInputStream(final InputStream in, final Charset targetEncoding) {
        internalInputStream = new PushbackInputStream(in, MAX_BOM_SIZE);
        this.targetEncoding = targetEncoding;
    }

    /**
     * Returns detected UTF encoding or {@code null} if no UTF encoding has been detected (i.e. no BOM).
     * If stream is not read yet, it will be {@link #init() initalized} first.
     */
    public Charset getDetectedEncoding() {
        if (!initialized) {
            try {
                init();
            } catch (final IOException ioex) {
                throw new IllegalStateException(ioex);
            }
        }
        return encoding;
    }

    public static final byte[] BOM_UTF32_BE = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    public static final byte[] BOM_UTF32_LE = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
    public static final byte[] BOM_UTF8 = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    public static final byte[] BOM_UTF16_BE = new byte[]{(byte) 0xFE, (byte) 0xFF};
    public static final byte[] BOM_UTF16_LE = new byte[]{(byte) 0xFF, (byte) 0xFE};

    /**
     * Detects and decodes encoding from BOM character.
     * Reads ahead four bytes and check for BOM marks.
     * Extra bytes are unread back to the stream, so only
     * BOM bytes are skipped.
     */
    protected void init() throws IOException {
        if (initialized) {
            return;
        }

        if (targetEncoding == null) {

            // DETECT MODE

            final byte[] bom = new byte[MAX_BOM_SIZE];
            final int n = internalInputStream.read(bom, 0, bom.length);
            final int unread;

            if ((bom[0] == BOM_UTF32_BE[0]) && (bom[1] == BOM_UTF32_BE[1]) && (bom[2] == BOM_UTF32_BE[2]) && (bom[3] == BOM_UTF32_BE[3])) {
                encoding = Charset.forName("UTF-32BE");
                unread = n - 4;
            } else if ((bom[0] == BOM_UTF32_LE[0]) && (bom[1] == BOM_UTF32_LE[1]) && (bom[2] == BOM_UTF32_LE[2]) && (bom[3] == BOM_UTF32_LE[3])) {
                encoding = Charset.forName("UTF-32LE");
                unread = n - 4;
            } else if ((bom[0] == BOM_UTF8[0]) && (bom[1] == BOM_UTF8[1]) && (bom[2] == BOM_UTF8[2])) {
                encoding = StandardCharsets.UTF_8;
                unread = n - 3;
            } else if ((bom[0] == BOM_UTF16_BE[0]) && (bom[1] == BOM_UTF16_BE[1])) {
                encoding = StandardCharsets.UTF_16BE;
                unread = n - 2;
            } else if ((bom[0] == BOM_UTF16_LE[0]) && (bom[1] == BOM_UTF16_LE[1])) {
                encoding = StandardCharsets.UTF_16LE;
                unread = n - 2;
            } else {
                // BOM not found, unread all bytes
                unread = n;
            }

            BOMSize = MAX_BOM_SIZE - unread;

            if (unread > 0) {
                internalInputStream.unread(bom, (n - unread), unread);
            }
        } else {

            // READ MODE

            byte[] bom = null;

            final String targetEncodingName = targetEncoding.name();

            switch (targetEncodingName) {
                case "UTF-8":
                    bom = BOM_UTF8;
                    break;
                case "UTF-16LE":
                    bom = BOM_UTF16_LE;
                    break;
                case "UTF-16BE":
                case "UTF-16":
                    bom = BOM_UTF16_BE;
                    break;
                case "UTF-32LE":
                    bom = BOM_UTF32_LE;
                    break;
                case "UTF-32BE":
                case "UTF-32":
                    bom = BOM_UTF32_BE;
                    break;
                default:
                    // no UTF encoding, no BOM
                    break;
            }

            if (bom != null) {
                final byte[] fileBom = new byte[bom.length];
                final int n = internalInputStream.read(fileBom, 0, bom.length);

                boolean bomDetected = true;
                for (int i = 0; i < n; i++) {
                    if (fileBom[i] != bom[i]) {
                        bomDetected = false;
                        break;
                    }
                }

                if (!bomDetected) {
                    internalInputStream.unread(fileBom, 0, fileBom.length);
                }
            }
        }

        initialized = true;
    }

    /**
     * Closes input stream. If stream was not used, encoding
     * will be unavailable.
     */
    @Override
    public void close() throws IOException {
        internalInputStream.close();
    }

    /**
     * Reads byte from the stream.
     */
    @Override
    public int read() throws IOException {
        init();
        return internalInputStream.read();
    }

    /**
     * Returns BOM size in bytes.
     * Returns <code>-1</code> if BOM not found.
     */
    public int getBOMSize() {
        return BOMSize;
    }

}
