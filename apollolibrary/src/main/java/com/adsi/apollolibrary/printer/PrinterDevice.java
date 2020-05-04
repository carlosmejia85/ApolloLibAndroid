package com.adsi.apollolibrary.printer;

import android.graphics.Bitmap;

public interface PrinterDevice
{
    OperationResult     imprimirDocumento               (                                                                   );

    void                addLineaImprimir                (    String linea, TAMANIO_LETRA tamanio, ALINEAMIENTO alineamiento );

    void                addLogoHeader                   (    Bitmap logo                                                    );

    OperationResult     checkPaper                      (                                                                   );
}