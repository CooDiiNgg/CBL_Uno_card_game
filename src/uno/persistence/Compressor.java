package uno.persistence;

import java.io.ByteArrayOutputStream;

public class Compressor {
    private static final int MAGIC_FIRST = 'U';
    private static final int MAGIC_SECOND = 'Z';
    private static final int HEADER_SIZE = 6;
    private static final int DISTANCE_BITS = 12;
    private static final int LENGTH_BITS = 4;
    private static final int MAX_DISTANCE = (1 << DISTANCE_BITS) - 1;
    private static final int MIN_MATCH = 3;
    private static final int MAX_MATCH = (1 << LENGTH_BITS) - 1 + MIN_MATCH;

    public static byte[] compress(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(MAGIC_FIRST);
        output.write(MAGIC_SECOND);
        writeInt(output, data.length);

        int position = 0;
        while (position < data.length) {
            byte[] group = new byte[1 + 8 * 2];
            int groupSize = 1;
            int flags = 0;
            for (int bit = 0; bit < 8 && position < data.length; bit++) {
                int[] match = findLongestMatch(data, position);
                int distance = match[0];
                int length = match[1];
                if (length >= MIN_MATCH) {
                    flags |= 1 << bit;
                    int token = (distance << LENGTH_BITS) | (length - MIN_MATCH);
                    group[groupSize++] = (byte) (token >>> 8);
                    group[groupSize++] = (byte) token;
                    position += length;
                } else {
                    group[groupSize++] = data[position];
                    position++;
                }
            }
            group[0] = (byte) flags;
            output.write(group, 0, groupSize);
        }
        return output.toByteArray();
    }

    public static byte[] decompress(byte[] data) throws SaveException {
        if (data == null || data.length < HEADER_SIZE) {
            throw new SaveException("Compressed data is too short");
        }
        if ((data[0] & 0xFF) != MAGIC_FIRST || (data[1] & 0xFF) != MAGIC_SECOND) {
            throw new SaveException("This is not a compressed UNO save");
        }
        int originalLength = readInt(data, 2);
        if (originalLength < 0 || originalLength > (long) (data.length - HEADER_SIZE) * MAX_MATCH) {
            throw new SaveException("Compressed header is corrupt");
        }

        byte[] output = new byte[originalLength];
        int outputPosition = 0;
        int position = HEADER_SIZE;
        while (outputPosition < originalLength) {
            if (position >= data.length) {
                throw new SaveException("Compressed data ended too early");
            }
            int flags = data[position++] & 0xFF;
            for (int bit = 0; bit < 8 && outputPosition < originalLength; bit++) {
                if ((flags & (1 << bit)) == 0) {
                    if (position >= data.length) {
                        throw new SaveException("Compressed data ended too early");
                    }
                    output[outputPosition++] = data[position++];
                } else {
                    if (position + 1 >= data.length) {
                        throw new SaveException("Compressed data ended too early");
                    }
                    int token = ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
                    position += 2;
                    int distance = token >>> LENGTH_BITS;
                    int length = (token & ((1 << LENGTH_BITS) - 1)) + MIN_MATCH;
                    if (distance == 0 || distance > outputPosition || outputPosition + length > originalLength) {
                        throw new SaveException("Compressed data contains an invalid reference");
                    }
                    for (int i = 0; i < length; i++) {
                        output[outputPosition] = output[outputPosition - distance];
                        outputPosition++;
                    }
                }
            }
        }
        return output;
    }

    private static int[] findLongestMatch(byte[] data, int position) {
        int bestDistance = 0;
        int bestLength = 0;
        int start = Math.max(0, position - MAX_DISTANCE);
        for (int candidate = start; candidate < position; candidate++) {
            int length = 0;
            while (length < MAX_MATCH && position + length < data.length
                    && data[candidate + length] == data[position + length]) {
                length++;
            }
            if (length > bestLength) {
                bestLength = length;
                bestDistance = position - candidate;
            }
        }
        return new int[]{bestDistance, bestLength};
    }

    private static void writeInt(ByteArrayOutputStream output, int value) {
        output.write((value >>> 24) & 0xFF);
        output.write((value >>> 16) & 0xFF);
        output.write((value >>> 8) & 0xFF);
        output.write(value & 0xFF);
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }
}
