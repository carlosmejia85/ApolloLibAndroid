package com.adsi.apollolibrary.printer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class Utilidad
{
    public Utilidad() {
    }

    public static String FillStringWith(String cadena, char toFillWith, int digits) {
        return FillStringWith(cadena, toFillWith, digits, true);
    }

    public static String FillStringWith(String cadena, char toFillWith, int digits, boolean padLeft)
    {
        StringBuilder filledString = new StringBuilder(cadena);
        //int longitudCadena = cadena.length();
        int counter = digits; // - longitudCadena;

        if (counter > 0)
        {
            for(int i = 0; i < counter; ++i) {
                if (padLeft) {
                    filledString.insert(0, toFillWith);
                } else {
                    filledString.append(toFillWith);
                }
            }
        }

        return filledString.toString();
    }

    private static byte aasc_to_bcd(byte asc) {
        byte bcd;
        if (asc >= 48 && asc <= 57) {
            bcd = (byte)(asc - 48);
        } else if (asc >= 65 && asc <= 70) {
            bcd = (byte)(asc - 65 + 10);
        } else if (asc >= 97 && asc <= 102) {
            bcd = (byte)(asc - 97 + 10);
        } else {
            bcd = (byte)(asc - 48);
        }

        if (asc == 32) {
            bcd = 0;
        }

        return bcd;
    }

    public static byte[] ASCII_To_BCD(byte[] ascii) {
        byte[] bcd = new byte[ascii.length / 2];
        int j = 0;

        for(int i = 0; i < (ascii.length + 1) / 2; ++i) {
            bcd[i] = aasc_to_bcd(ascii[j++]);
            bcd[i] = (byte)((j >= ascii.length ? 0 : aasc_to_bcd(ascii[j++])) + (bcd[i] << 4));
        }

        return bcd;
    }

    public static byte[] calculateLRC(byte[] data) {
        byte checksum = 0;

        for(int i = 0; i <= data.length - 1; ++i) {
            checksum ^= data[i];
        }

        return new byte[]{checksum};
    }

    public static byte[] OptionToBit(String dataIn) {
        byte[] bits = new byte[]{-128, 64, 32, 16, 8, 4, 2, 1};
        byte convbit = 0;

        for(int ndx = 0; ndx <= dataIn.length() - 1; ++ndx) {
            if (dataIn.substring(ndx, ndx + 1).compareTo("Y") == 0) {
                convbit |= bits[ndx];
            }
        }

        return new byte[]{convbit};
    }

    public static String padString(String s, int n, char c, char padLeft) {
        if (s == null) {
            return s;
        } else {
            int add = n - s.length();
            if (add <= 0) {
                return s;
            } else {
                StringBuffer str = new StringBuffer(s);
                char[] ch = new char[add];
                Arrays.fill(ch, c);
                if (padLeft == 'D') {
                    str.insert(0, ch);
                } else {
                    str.append(ch);
                }

                return str.toString();
            }
        }
    }

    public static byte[] CalcLenHexBCD2(byte[] datain) {
        String str4len = padString(Integer.toHexString(datain.length), 4, '0', 'D');
        byte[] bcd2len = ASCII_To_BCD(str4len.getBytes());
        return bcd2len;
    }

    public static byte[] CalcLenDecBCD2(byte[] datain) {
        String str4len = padString(Integer.toString(datain.length), 4, '0', 'D');
        byte[] bcd2len = ASCII_To_BCD(str4len.getBytes());
        return bcd2len;
    }

    public static byte[] str2bcd(String s, boolean padLeft, byte[] d, int offset) {
        int len = s.length();
        int start = (len & 1) == 1 && padLeft ? 1 : 0;

        for(int i = start; i < len + start; ++i) {
            d[offset + (i >> 1)] = (byte)(d[offset + (i >> 1)] | s.charAt(i - start) - 48 << ((i & 1) == 1 ? 0 : 4));
        }

        return d;
    }

    public static byte[] str2bcd(String s, boolean padLeft) {
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        return str2bcd(s, padLeft, d, 0);
    }

    public static byte[] concat(byte[] array1, byte[] array2) {
        byte[] concatArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, concatArray, 0, array1.length);
        System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
        return concatArray;
    }

    public static byte[] concatVar(byte[]... cntParam) {
        byte[] concatArrayAll = null;
        byte[][] var2 = cntParam;
        int var3 = cntParam.length;

        for(int var4 = 0; var4 < var3; ++var4)
        {
            byte[] array = var2[var4];

            if (array == null)
            {
                continue;
            }

            if (concatArrayAll == null)
            {
                concatArrayAll = new byte[array.length];
                System.arraycopy(array, 0, concatArrayAll, 0, array.length);
            }
            else
            {
                byte[] concatArrayTmp = new byte[array.length + concatArrayAll.length];
                System.arraycopy(concatArrayAll, 0, concatArrayTmp, 0, concatArrayAll.length);
                System.arraycopy(array, 0, concatArrayTmp, concatArrayAll.length, array.length);
                concatArrayAll = new byte[concatArrayTmp.length];
                System.arraycopy(concatArrayTmp, 0, concatArrayAll, 0, concatArrayTmp.length);
            }
        }

        return concatArrayAll;
    }

    public static String bcd2str(byte[] b, int offset, int len, boolean padLeft) {
        StringBuffer d = new StringBuffer(len);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;

        for(int i = start; i < len + start; ++i) {
            int shift = (i & 1) == 1 ? 0 : 4;
            char c = Character.forDigit(b[offset + (i >> 1)] >> shift & 15, 16);
            d.append(Character.toUpperCase(c));
        }

        return d.toString();
    }

    public static String BCD_TO_ASCII(byte[] bcdDigits) {
        StringBuilder sb = new StringBuilder(bcdDigits.length * 2);
        byte[] var2 = bcdDigits;
        int var3 = bcdDigits.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static String hexdump(byte[] b, int offset, int len) {
        StringBuffer sb = new StringBuffer();
        StringBuffer hex = new StringBuffer();
        StringBuffer ascii = new StringBuffer();
        String sep = "  ";
        String lineSep = System.getProperty("line.separator");

        for(int i = offset; i < len; ++i) {
            char hi = Character.forDigit(b[i] >> 4 & 15, 16);
            char lo = Character.forDigit(b[i] & 15, 16);
            hex.append(Character.toUpperCase(hi));
            hex.append(Character.toUpperCase(lo));
            hex.append(' ');
            char c = (char)b[i];
            ascii.append(c >= ' ' && c < 127 ? c : '.');
            int j = i % 16;
            switch(j) {
                case 7:
                    hex.append(' ');
                    break;
                case 15:
                    sb.append(hexOffset(i));
                    sb.append(sep);
                    sb.append(hex.toString());
                    sb.append(' ');
                    sb.append(ascii.toString());
                    sb.append(lineSep);
                    hex = new StringBuffer();
                    ascii = new StringBuffer();
            }
        }

        if (hex.length() > 0) {
            while(hex.length() < 49) {
                hex.append(' ');
            }

            sb.append(hexOffset(len));
            sb.append(sep);
            sb.append(hex.toString());
            sb.append(' ');
            sb.append(ascii.toString());
            sb.append(lineSep);
        }

        return sb.toString();
    }

    private static String hexOffset(int i) {
        i = i >> 4 << 4;
        int w = i > 65535 ? 8 : 4;
        return zeropad(Integer.toString(i, 16), w);
    }

    public static String zeropad(String s, int len) {
        return padleft(s, len, '0');
    }

    public static String padleft(String s, int len, char c) {
        s = s.trim();
        if (s.length() > len) {
            return "";
        } else {
            StringBuffer d = new StringBuffer(len);
            int var4 = len - s.length();

            while(var4-- > 0) {
                d.append(c);
            }

            d.append(s);
            return d.toString();
        }
    }

    public static ArrayList<String> lstFileDir(String rutaDir, String SelExt) {
        ArrayList<String> nameFile = new ArrayList();
        File directorio = new File(rutaDir);
        String[] listaDirectorio = directorio.list();
        if (listaDirectorio == null) {
            System.out.println("No hay ficheros en el directorio especificado");
        } else {
            for(int x = 0; x < listaDirectorio.length; ++x) {
                if (listaDirectorio[x].indexOf(SelExt) > -1) {
                    nameFile.add(listaDirectorio[x]);
                }
            }
        }

        return nameFile;
    }

    public static void Delay(long timer) {
        if (timer != 0L) {
            DelayMseg(timer * 1000L);
        }
    }

    public static void DelayMseg(long time) {
        try {
            if (time != 0L) {
                Thread.sleep(time);
            }
        } catch (InterruptedException var4) {
            GregorianCalendar now = new GregorianCalendar();

            while(now.getTimeInMillis() + time >= System.currentTimeMillis()) {
            }

        }
    }

    public static boolean isNum(String strNum) {
        boolean ret = true;

        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException var3) {
            ret = false;
        }

        return ret;
    }

    public static int Hex2Int(byte[] source, int sourceLength) {
        int val = 0;

        for(int i = 0; i < sourceLength; ++i) {
            if (source[i] <= 57) {
                val += (source[i] - 48) * (1 << 4 * (sourceLength - 1 - i));
            } else {
                val += (source[i] - 55) * (1 << 4 * (sourceLength - 1 - i));
            }
        }

        return val;
    }

    public static byte[] numeroToByteArray(long numero, int lenByte) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        byte[] datos = new byte[lenByte];
        byte[] datosAux = new byte[8];
        bb.putLong(numero);
        datosAux = bb.array();
        System.arraycopy(datosAux, 8 - lenByte, datos, 0, lenByte);
        return datos;
    }
}
