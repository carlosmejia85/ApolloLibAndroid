package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Random;

public class ApolloUtil
{
    /**
     * Rellena con el caracter especificado la cantidad de espacios faltantes en
     * digits Ejemplo: Vamos a rellenar de ceros el nÃºmero 15, pero lo queremos
     * de 5 dÃ­gitos 15 -> "00015"
     *
     * cadena = "15" toFillWith = "0" digitis = 5
     *
     * @param cadena
     * @param toFillWith
     * @param digits
     * @return
     */
    public static String FillStringWith(String cadena, char toFillWith, int digits) {

        StringBuffer filledString = new StringBuffer(cadena);

        int longitudCadena = cadena.length();

        int counter = digits - longitudCadena;

        if (counter > 0) {
            for (int i = 0; i < counter; i++) {
                filledString.insert(0, toFillWith);
            }
        }

        return filledString.toString();

    }

    public static String FillStringWith(String cadena, char toFillWith, int digits, boolean padLeft) {

        if (padLeft) {
            return FillStringWith(cadena, toFillWith, digits);
        }else {
            StringBuffer filledString = new StringBuffer(cadena);

            int longitudCadena = cadena.length();

            int counter = digits - longitudCadena;

            if (counter > 0) {
                for (int i = 0; i < counter; i++) {
                    filledString.append(toFillWith);
                }
            }

            return filledString.toString();

        }

    }
    /**
     * *
     * Convierte un caractér ASCII a BCD (Binay Code Decimal)
     *
     * @param asc
     * @return
     */
    private static byte aasc_to_bcd(byte asc) {
        byte bcd;

        if ((asc >= '0') && (asc <= '9')) {
            bcd = (byte) (asc - '0');
        } else if ((asc >= 'A') && (asc <= 'F')) {
            bcd = (byte) (asc - 'A' + 10);
        } else if ((asc >= 'a') && (asc <= 'f')) {
            bcd = (byte) (asc - 'a' + 10);
        } else {
            bcd = (byte) (asc - 48);
        }

        // reparar caso caracter ' ' quedaba como 0xF0
        if (asc == ' ') {
            bcd = 0;
        }
        return bcd;
    }

    /**
     * *
     * Convierte un byte de array de ASCII a BCD (Binay Code Decimal)
     *
     * @param ascii
     * @return
     */
    public static byte[] ASCII_To_BCD(byte[] ascii) {
        byte[] bcd = new byte[ascii.length / 2];

        int j = 0;
        for (int i = 0; i < (ascii.length + 1) / 2; i++) {
            bcd[i] = aasc_to_bcd(ascii[j++]);
            bcd[i] = (byte) (((j >= ascii.length) ? 0x00 : aasc_to_bcd(ascii[j++])) + (bcd[i] << 4));
        }
        return bcd;
    }

    /***
     * Calcula el Longitudinal Redundancy Check (Chequeo de Redundancia Longitudinal )
     * @param data
     * @return
     */
    public static byte[] calculateLRC(byte[] data) {
        byte checksum = 0;

        for (int i = 0; i <= data.length - 1; i++) {
            checksum ^= (byte) (data[i]);
        }

        return new byte[]{checksum};
    }

    /***
     * Convierte una cadena a su representación binaria
     * @param dataIn
     * @return
     */
    public static byte[] OptionToBit(String dataIn) {
        // dataIn = NYYYNNNN = 01110000 = hex70 = dec112 =
        byte[] bits = {(byte) 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        byte convbit = 0;

        for (int ndx = 0; ndx <= dataIn.length() - 1; ndx++) {
            if (dataIn.substring(ndx, ndx + 1).compareTo("Y") == 0) {
                convbit |= bits[ndx];
            }
        }

        return new byte[]{convbit};
    }

    /***
     * ejem: Utilidad.padString("1", 16, ' ', 'I')
     * @param s
     * @param n
     * @param c
     * @param padLeft
     * @return
     */
    public static String padString(String s, int n, char c,
                                   char padLeft) {
        if (s == null) {
            return s;
        }
        int add = n - s.length(); // may overflow int size... should not be a problem in real life
        if (add <= 0) {
            return s;
        }
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

    /***
     * Recibe un string calcula su longitud y esta la coloca en un string en bcd
     * ejem:  datain = "0340" -> return=0x03 0x40
     * @param datain
     * @return
     */
    public static byte[] CalcLenHexBCD2(byte[] datain) {
        String str4len = padString(Integer.toHexString(datain.length), 4, '0', 'D');
        //byte[] bcd2len = Utilidad.str2bcd(str4len, true);
        byte[] bcd2len = ASCII_To_BCD(str4len.getBytes());
        return bcd2len;
    }

    /***
     * Recibe un string calcula su longitud y esta la coloca en un string en bcd
     * ejem:  datain = "0340" -> return=0x03 0x40
     * @param datain
     * @return
     */
    public static byte[] CalcLenDecBCD2(byte[] datain) {
        String str4len = padString(Integer.toString(datain.length), 4, '0', 'D');
        //byte[] bcd2len = Utilidad.str2bcd(str4len, true);
        byte[] bcd2len = ASCII_To_BCD(str4len.getBytes());
        return bcd2len;
    }

    /**
     * Convierte una cadena a BCD (Binary Code Decimal)
     *
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @param d The byte array to copy into.
     * @param offset Where to start copying into.
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft, byte[] d, int offset) {
        int len = s.length();
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            d[offset + (i >> 1)] |= (s.charAt(i - start) - '0') << ((i & 1) == 1 ? 0 : 4);
        }
        return d;
    }

    /**
     * Convierte una cadena a BCD (Binary Code Decimal)
     *
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft) {
        int len = s.length();
        byte[] d = new byte[(len + 1) >> 1];
        return str2bcd(s, padLeft, d, 0);
    }

    /**
     * Concatena dos arreglos (array1 and array2)
     *
     * @param array1
     * @param array2
     * @return the concatenated array
     */
    public static byte[] concat(byte[] array1, byte[] array2) {
        byte[] concatArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, concatArray, 0, array1.length);
        System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
        return concatArray;
    }

    /***
     * Concatena los parámetros y los regresa como un solo arreglo
     * @param cntParam
     * @return
     */
    public static byte[] concatVar(byte[][] cntParam) {

        byte[] concatArrayAll = null;

        byte[] array = null;

        for (int i = 0; i < cntParam.length; i++){

            array = cntParam[i];

            if  (array == null) continue;

            if (concatArrayAll == null) {

                concatArrayAll = new byte[array.length];
                System.arraycopy(array, 0, concatArrayAll, 0, array.length);
                continue;
            }

            byte[] concatArrayTmp = new byte[array.length + concatArrayAll.length];
            System.arraycopy(concatArrayAll, 0, concatArrayTmp, 0, concatArrayAll.length);
            System.arraycopy(array, 0, concatArrayTmp, concatArrayAll.length, array.length);

            concatArrayAll = new byte[concatArrayTmp.length];
            System.arraycopy(concatArrayTmp, 0, concatArrayAll, 0, concatArrayTmp.length);
        }

        return concatArrayAll;
    }

    /**
     * Convierte de BCD(Binary Code Decimal) de un número a cadena
     *
     * @param b - BCD representation
     * @param offset - starting offset
     * @param len - BCD field len * 2
     * @param padLeft - was padLeft packed?
     * @return the String representation of the number
     */
    public static String bcd2str(byte[] b, int offset,
                                 int len, boolean padLeft) {
        StringBuffer d = new StringBuffer(len);
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            int shift = ((i & 1) == 1 ? 0 : 4);
            char c = Character.forDigit(
                    ((b[offset + (i >> 1)] >> shift) & 0x0F), 16);
            d.append(Character.toUpperCase(c));
        }
        return d.toString();
    }

    /***
     * Convierte del formato BCD (Binary Code Decimal) a ASCII
     * @param bcdDigits
     * @return
     */
    public static String BCD_TO_ASCII(byte[] bcdDigits) {
        StringBuffer sb = new StringBuffer(bcdDigits.length * 2);
        //for (byte b : bcdDigits) {
        byte b = 0;

        for (int i = 0; i < bcdDigits.length; i++){
            b = bcdDigits[i];

            String hexString = Integer.toHexString(b);

            if (hexString.length() < 2) {
                hexString = "0" + hexString;
            }

            sb.append(hexString);
        }

        return sb.toString();
    }

    /**
     * Realiza el dumping o la representación de la trama o arreglo b
     * en formato legible
     * @param b a byte[] buffer
     * @param offset starting offset
     * @param len the Length
     * @return hexdump
     */
    public static String hexdump(byte[] b, int offset, int len) {
        StringBuffer sb = new StringBuffer();
        StringBuffer hex = new StringBuffer();
        StringBuffer ascii = new StringBuffer();
        String sep = "  ";
        String lineSep = System.getProperty("line.separator");

        for (int i = offset; i < len; i++) {
            char hi = Character.forDigit((b[i] >> 4) & 0x0F, 16);
            char lo = Character.forDigit(b[i] & 0x0F, 16);
            hex.append(Character.toUpperCase(hi));
            hex.append(Character.toUpperCase(lo));
            hex.append(' ');
            char c = (char) b[i];
            ascii.append((c >= 32 && c < 127) ? c : '.');

            int j = i % 16;
            switch (j) {
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
                    break;
            }
        }
        if (hex.length() > 0) {
            while (hex.length() < 49) {
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

    /**
     * Realiza el desplazamiento para formatos hexadecimales
     * @param i
     * @return
     */
    private static String hexOffset(int i) {
        i = (i >> 4) << 4;
        int w = i > 0xFFFF ? 8 : 4;
        return zeropad(Integer.toString(i, 16), w);
    }

    /**
     * Rellena hacia la izquierda con ceros
     *
     * @param s - original string
     * @param len - desired len
     * @return zero padded string
     */
    public static String zeropad(String s, int len) {
        return padleft(s, len, '0');
    }

    /**
     * Rellena hacia la izquiera
     *
     * @param s - original string
     * @param len - desired len
     * @param c - padding char
     * @return padded string
     */
    public static String padleft(String s, int len, char c) {
        s = s.trim();
        if (s.length() > len) {
            return "";
        }
        StringBuffer d = new StringBuffer(len);
        int fill = len - s.length();
        while (fill-- > 0) {
            d.append(c);
        }
        d.append(s);
        return d.toString();
    }

    /***
     * Lista contenido de un directorio (sin el contenido de sub-dire) y selec segun string
     * en el nombre.
     * @param rutaDir
     * @param SelExt
     * @return
     */
    public static String[] lstFileDir(String rutaDir, String SelExt) {
        ArrayList nameFile = new ArrayList();
        File directorio = new File(rutaDir);
        String[] listaDirectorio = directorio.list();
        if (listaDirectorio == null) {
            System.out.println("No hay ficheros en el directorio especificado");
        } else {
            for (int x = 0; x < listaDirectorio.length; x++) {
                if (listaDirectorio[x].indexOf(SelExt) > -1) {
                    nameFile.add(listaDirectorio[x]);
                }
            }
        }

        String[] filenameArray = new String[nameFile.size()];

        int i = 0;
        for(Iterator rowIterator = nameFile.iterator(); rowIterator.hasNext(); ) {
            String filename = (String) rowIterator.next();
            filenameArray[i] = filename;
            i++;
        }

        return filenameArray;
    }

    /***
     * Realiza una espera en el hilo principal
     * @param timer
     */
    public static void Delay(long timer) {
        if (timer == 0) {
            return;
        }
        DelayMseg(timer * 1000L);
    }

    /***
     * Demora el hilo por la cantidad especificada
     * @param time
     */
    public static void DelayMseg(long time) {
        try {
            if (time == 0) {
                return;
            }
            Thread.sleep(time);
        } catch (InterruptedException e) {
            for (Calendar now = new GregorianCalendar(); now.getTimeInMillis() + time >= System.currentTimeMillis(););
            return;
        }
    }

    /***
     *  This is for emulate the funcionality of Arrays.copyOfRange, in SDK 4.0 doesn't exist
     * @param source
     * @param indexStart
     * @param indexEnd
     * @return
     */
    public static byte[] CopyRange (byte[] source, int indexStart, int indexEnd){

        int length = indexEnd - indexStart;

        byte[] destiny = new byte[length];

        for (int i = 0; i < length; i++){
            //destiny[i] = source[i];
            destiny[i] = source [indexStart + i];
        }

        return destiny;
    }

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }


    public static int getRandomNumber (int minimum, int maximum)
    {

        Random rand = new Random();
        return rand.nextInt(maximum) + minimum;

    }
}
