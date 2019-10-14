package headers;

/**
 *
 */
public abstract class Header {

    /**
     * @param data
     * @param start
     * @param end
     * @return
     */
    public static String bytesToBitString(byte[] data, int start, int end) {

        String bitStr = "";

        for (int i = start; i < end; i++) {
            String s = Integer.toBinaryString(data[i] & 0xFF);
            s = String.format("%8s", s).replace(' ', '0');
            bitStr += s;
        }

        return bitStr;
    }

    /**
     * @param data
     * @param start
     * @param end
     * @return
     */
    public static int calculateChecksum(byte[] data, int start, int end) {
        String bitStr = bytesToBitString(data, start, end);

        return calculateChecksum(bitStr);
    }

    /**
     * @param bitStr
     * @return
     */
    public static int calculateChecksum(String bitStr) {
        int header_length = 36;

        String[] octets = new String[header_length];

		/* Adds the IP header to the octets array */
        for (int i = 0; i < header_length; i++) {
            int start = i * 8;
            int end = start + 8;

            octets[i] = bitStr.substring(start, end);
        }

        // Set checksum to 0
        octets[2] = "00000000";
        octets[3] = "00000000";

        String sum = new String(new char[16]).replace("\0", "0");

		/* Loops through every pair of octets and adds them to the sum
		 * as a 16 bit value */
        for (int i = 0; i < octets.length; i += 2) {
            String value = "";

            // Converts two octets into 16 bit value
            value = octets[i] + octets[i + 1];

            // Adds value to the current sum
            int s = Integer.parseInt(sum, 2) + Integer.parseInt(value, 2);
            sum = Integer.toBinaryString(s);
            sum = String.format("%16s", sum).replace(' ', '0');

            // Handles 16-bit carry correction
            if (sum.length() > 16) {
                String carryStr = sum.substring(0, sum.length() - 16);
                int carry = Integer.parseInt(carryStr, 2);

                s = Integer.parseInt(sum.substring(carryStr.length()), 2);
                s += carry;

                sum = Integer.toBinaryString(s);
                sum = String.format("%16s", sum).replace(' ', '0');
            }
        }

        // Flips the bits
        sum = sum.replace('1', '2');
        sum = sum.replace('0', '1');
        sum = sum.replace('2', '0');

        return Integer.parseInt(sum, 2);
    }
}
