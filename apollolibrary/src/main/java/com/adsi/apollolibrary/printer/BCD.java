package com.adsi.apollolibrary.printer;

public class BCD
{
    public BCD() {
    }

    public static byte[] DecimalToBCD(long num) {
        int digits = 0;

        for(long temp = num; temp != 0L; temp /= 10L) {
            ++digits;
        }

        int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
        byte[] bcd = new byte[byteLen];

        int i;
        byte tmp;
        for(i = 0; i < digits; ++i) {
            tmp = (byte)((int)(num % 10L));
            if (i % 2 == 0) {
                bcd[i / 2] = tmp;
            } else {
                bcd[i / 2] |= (byte)(tmp << 4);
            }

            num /= 10L;
        }

        for(i = 0; i < byteLen / 2; ++i) {
            tmp = bcd[i];
            bcd[i] = bcd[byteLen - i - 1];
            bcd[byteLen - i - 1] = tmp;
        }

        return bcd;
    }

    public static long BCDToDecimal(byte[] bcd) {
        return Long.valueOf(BCDtoString(bcd));
    }

    public static String BCDtoString(byte bcd) {
        StringBuffer sb = new StringBuffer();
        byte high = (byte)(bcd & 240);
        high = (byte)(high >>> 4);
        high = (byte)(high & 15);
        byte low = (byte)(bcd & 15);
        sb.append(high);
        sb.append(low);
        return sb.toString();
    }

    public static String BCDtoString(byte[] bcd) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < bcd.length; ++i) {
            sb.append(BCDtoString(bcd[i]));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("Testing DecimalToBCD:");
        testForValue(1L, "00000001");
        testForValue(11L, "00010001");
        testForValue(111L, "0000000100010001");
        testForValue(1111L, "0001000100010001");
        testForValue(11111L, "000000010001000100010001");
        testForValue(42L, "01000010");
        testForValue(112233L, "000100010010001000110011");
        testForValue(12345L, "000000010010001101000101");
        System.out.println("\nTesting two way conversion using DecimalToBCD and back using BCDToDecimal:");
        testForValue(1L);
        testForValue(11L);
        testForValue(111L);
        testForValue(1111L);
        testForValue(11111L);
        testForValue(12983283L);
        testForValue(9832098349L);
    }

    private static void testForValue(long val, String expected) {
        String binaryString = byteArrayToBinaryString(DecimalToBCD(val));
        System.out.print(String.format("Testing: %10d -> %30s %4s\n", val, binaryString, binaryString.equals(expected) ? "[OK]" : "[FAIL]"));
    }

    private static void testForValue(long val) {
        long newVal = BCDToDecimal(DecimalToBCD(val));
        System.out.print(String.format("Testing: %10d -> %30d %4s\n", val, newVal, newVal == val ? "[OK]" : "[FAIL]"));
    }

    private static String byteArrayToBinaryString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte i = var2[var4];
            String byteInBinary = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
            sb.append(byteInBinary);
        }

        return sb.toString();
    }
}
