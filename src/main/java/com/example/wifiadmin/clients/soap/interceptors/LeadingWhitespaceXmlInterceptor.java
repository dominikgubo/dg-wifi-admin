package com.example.wifiadmin.clients.soap.interceptors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public final class LeadingWhitespaceXmlInterceptor
        extends AbstractPhaseInterceptor<Message> {

    private static final byte[] XML_DECLARATION = "<?xml"
            .getBytes(StandardCharsets.US_ASCII);

    public LeadingWhitespaceXmlInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        InputStream input = message.getContent(InputStream.class);
        if (input == null) {
            return;
        }

        try {
            byte[] body = input.readAllBytes();
            message.setContent(InputStream.class,
                    new ByteArrayInputStream(normalize(body)));
        } catch (IOException exception) {
            throw new Fault(exception);
        }
    }

    private static byte[] normalize(byte[] body) {
        int contentStart = hasUtf8Bom(body) ? 3 : 0;
        int whitespaceStart = contentStart;

        while (contentStart < body.length && isXmlWhitespace(body[contentStart])) {
            contentStart++;
        }

        if (contentStart == whitespaceStart
                || !startsWith(body, contentStart, XML_DECLARATION)) {
            return body;
        }

        int removed = contentStart - whitespaceStart;
        byte[] normalized = new byte[body.length - removed];
        System.arraycopy(body, 0, normalized, 0, whitespaceStart);
        System.arraycopy(body, contentStart, normalized, whitespaceStart,
                body.length - contentStart);
        return normalized;
    }

    private static boolean hasUtf8Bom(byte[] body) {
        return body.length >= 3
                && (body[0] & 0xFF) == 0xEF
                && (body[1] & 0xFF) == 0xBB
                && (body[2] & 0xFF) == 0xBF;
    }

    private static boolean isXmlWhitespace(byte value) {
        return value == ' ' || value == '\t' || value == '\n' || value == '\r';
    }

    private static boolean startsWith(byte[] body, int offset, byte[] prefix) {
        if (body.length - offset < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (body[offset + index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }
}
