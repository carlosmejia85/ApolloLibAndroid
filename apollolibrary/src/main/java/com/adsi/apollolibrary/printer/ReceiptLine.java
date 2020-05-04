package com.adsi.apollolibrary.printer;

public class ReceiptLine
{
    String text;
    int textSize;

    public ReceiptLine(String text, int size) {
        this.text = text;
        this.textSize = size;
    }

    public ReceiptLine(String linea, TAMANIO_LETRA tamanio, ALINEAMIENTO alineacion)
    {
        int fontSize    = 0;
        int spacesToPad = 0;
        String lineaFormateada = "";

        switch(tamanio)
        {
            case PEQUENIO:
                fontSize = 0;
                spacesToPad = 48;
                break;

            case GRANDE:
                fontSize = 2;
                spacesToPad = 20;
                break;
            default:
                case MEDIANO:
                fontSize    = 1 ;
                spacesToPad = 35;
        }

        boolean padLeft = true;
        switch(alineacion)
        {


            case CENTRO:
                lineaFormateada = Utilidad.FillStringWith(linea, ' ', (spacesToPad + linea.length()) / 2, padLeft);
                break;
            case DERECHA:
                lineaFormateada = Utilidad.FillStringWith(linea, ' ', spacesToPad, padLeft);
                break;
            default:
            case IZQUIERDA:
                lineaFormateada = linea;
        }

        this.text       = lineaFormateada;
        this.textSize   = fontSize;
    }

}
