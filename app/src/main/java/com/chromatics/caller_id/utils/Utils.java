package com.chromatics.caller_id.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Utils {

    public static String md5String(String str) {
        return new String(Hex.encodeHex(DigestUtils.md5(str.getBytes())));
    }

    public static String generateAppId() {
        UUID randomUUID = UUID.randomUUID();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(randomUUID.getMostSignificantBits());
        buffer.putLong(randomUUID.getLeastSignificantBits());

        return new String(Base64.encodeBase64(buffer.array()))
                .replace('/', '_')
                .replace('\\', '-')
                .replace("\n", "")
                .replace("=", "");
    }

}
