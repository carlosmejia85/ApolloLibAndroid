package com.adsi.apollolibrary.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Printer;

import com.spectratech.controllers.ControllerError;
import com.spectratech.controllers.ControllerMessage;
import com.spectratech.controllers.PrinterController;
import com.spectratech.lib.level2.DType.DScPrtImage;
import com.spectratech.lib.level2.ULv2;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import card.IApolloInterface;

import static card.ApolloCode.PRINTER_INITIALIZED;

public class ApolloPrinter implements PrinterDevice
{

    private String                  TAG = "ApolloPrinter"     ;
    private Context                 ctx                     ;
    private Bitmap                  headerLogo              ;
    private int                     PRN_OK = 0              ;
    private ArrayList<ReceiptLine>  receiptLineTbl          ;

    private IApolloInterface        onData                  ;

    private PrinterController mPrinterController;

    public ApolloPrinter (Context context)
    {
        this.ctx = context;
    }



    public void                     setContext                  (   Context ctx     )
    {
        this.ctx = ctx;
    }

    public Context                  getContext                  (                   )
    {
        return this.ctx;
    }

    public ArrayList<ReceiptLine>   getReceiptLineArray         (                   )
    {
        return this.receiptLineTbl;
    }

    public void                     setReceiptLineArray         (ArrayList<ReceiptLine> receiptLineTbl) {
        this.receiptLineTbl = receiptLineTbl;
    }

    public IApolloInterface getOnData() {
        return onData;
    }

    public void setOnData(IApolloInterface onData) {
        this.onData = onData;
    }

    public class PrinterDelegate  implements PrinterController.PrinterDelegate
    {


        private Bitmap logoBitmap ;

        public void setLogoBitmap (Bitmap logo)
        {
            this.logoBitmap = logo;
        }

        public Bitmap getLogoBitmap ()
        {
            return this.logoBitmap;
        }


        @Override
        public void onPrintDataRequested()
        {

            byte[] data = null;

            if (getLogoBitmap() != null)
            {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                getLogoBitmap().compress(Bitmap.CompressFormat.JPEG, 70, stream);
                data = stream.toByteArray();
                getLogoBitmap().recycle();

                //DScPrtImage bodyScPrtImage = ULv2.bitmap2PrtImg(getLogoBitmap(), 127);
                //data = getbodyScPrtImage.m_data;

            }

            int iXS = 32;

            Bitmap bmLine   = null;
            Bitmap bm       = null;

            bmLine = ULv2.text2Bitmap("Spectra Technologies", iXS, Paint.Align.LEFT);
            bm = ULv2.addBitmap(bm, bmLine);

            for(int index = 0; index < receiptLineTbl.size(); index++)
            {
                ReceiptLine aLine = receiptLineTbl.get(index);


                switch(aLine.textSize)
                {
                    case 0:
                        iXS = 15;
                        bmLine = ULv2.text2Bitmap(aLine.text, iXS, Paint.Align.LEFT, Typeface.MONOSPACE);
                        break;
                    case 1:
                        iXS = 20;
                        bmLine = ULv2.text2Bitmap(aLine.text, iXS, Paint.Align.LEFT, Typeface.MONOSPACE);

                        break;
                    case 2:
                        iXS = 32;
                        bmLine = ULv2.text2Bitmap(aLine.text, iXS, Paint.Align.LEFT, Typeface.MONOSPACE);
                }

                bm = ULv2.addBitmap(bm, bmLine);

            }



            DScPrtImage bodyScPrtImage = ULv2.bitmap2PrtImg(bm, 127);
            byte[] bodyData = bodyScPrtImage.m_data;

            data = Utilidad.concatVar( data, bodyData);

            mPrinterController.sendPrinterData(data);


        }

        @Override
        public void onPrinterStatus(PrinterController.PrintStatus printStatus) {

        }

        @Override
        public void onPrinterCompleted()
        {
            releaseController();
        }

        @Override
        public void onError(ControllerError.Error error, String s) {

        }

        @Override
        public void onControllerConnected()
        {

            if (null != mPrinterController && onData != null)
            {
                mPrinterController.initPrinter();


                try
                {
                    Thread.sleep(1500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                onData.onMessage( PRINTER_INITIALIZED.ordinal(),  "Printer initialized");
            }


        }

        @Override
        public void onControllerDisconnected()
        {

        }

        @Override
        public void onDeviceInfoReceived(Hashtable<String, String> hashtable)
        {

        }

        @Override
        public void onMessageReceived(ControllerMessage.MessageText messageText)
        {

        }
    }

    private void releaseController()
    {
        if (mPrinterController != null)
        {
            mPrinterController.disconnectController();
            mPrinterController.releaseControllerInstance();
            mPrinterController = null;
        }

    }

    @Override
    public OperationResult imprimirDocumento()
    {
        OperationResult result = new OperationResult();

        result.setSuccess(true);

        Log.d(this.TAG, "Ejecutando funcion imprimirDocumento");


        if (null == this.ctx)
        {
            Log.d(this.TAG, "imprimirDocumento Context is NULL");
            result.setSuccess(false);
            result.addMessage("imprimirDocumento Context is NULL", MessageType.Error);
            return result;
        }
        else if (this.receiptLineTbl == null)
        {
            Log.d(this.TAG, "receiptLineTbl is NULL");
            result.setSuccess(false);
            result.addMessage("receiptLineTbl is NULL", MessageType.Error);
            return result;
        }
        else if (this.receiptLineTbl.size() <= 0)
        {
            Log.d(this.TAG, "No hay lineas que imprimir");
            result.setSuccess(false);
            result.addMessage("No hay lineas que imprimir. receiptLineTbl.size() = 0", MessageType.Error);
            return result;
        }
        else
        {
            try
            {

                PrinterDelegate delegate = new PrinterDelegate();
                delegate.setLogoBitmap( headerLogo );

                mPrinterController = PrinterController.getControllerInstance(getContext(), delegate);

                if (null != mPrinterController)
                {
                    mPrinterController.connectController();
                    result.addMessage("Printer Started", MessageType.Info);
                    result.setSuccess(true);
                }
                else
                {
                    result.addMessage("Printer is null", MessageType.Error);
                    result.setSuccess(false);
                }

            }
            catch (Exception ex)
            {
                if (ex.getMessage() != null)
                {
                    Log.d(this.TAG, ex.getMessage());
                    result.setSuccess(false);
                    result.addMessage(ex.getMessage(), MessageType.Error);
                }
            }

            return result;
        }

    }

    @Override
    public void addLineaImprimir(String linea, TAMANIO_LETRA tamanio, ALINEAMIENTO alineamiento)
    {
        int fontSize        = 0;
        int spacesToPad     = 0;
        String lineaFormateada = "";

        if (null == this.receiptLineTbl)
        {
            this.receiptLineTbl = new ArrayList();
        }


        switch(tamanio)
        {
            case PEQUENIO:
                fontSize = 0;
                spacesToPad = 45;
                break;

            case GRANDE:
                fontSize = 2;
                spacesToPad = 22;
                break;
            default:
            case MEDIANO:
                fontSize = 1;
                spacesToPad = 34;
        }

        boolean padLeft = true;

        switch(alineamiento)
        {

            case CENTRO:

                int spacesToAdd = spacesToPad       / 2;
                int middleString = linea.length()   / 2;

                int theSpaces = spacesToAdd - middleString;

                lineaFormateada = Utilidad.FillStringWith(linea, ' ', theSpaces , padLeft);
                break;
            case DERECHA:
                lineaFormateada = Utilidad.FillStringWith(linea, ' ', spacesToPad - linea.length() - 1, padLeft);
                break;
            default:
            case IZQUIERDA:
                lineaFormateada = linea;
        }

        this.receiptLineTbl.add(new ReceiptLine(lineaFormateada, fontSize));
    }

    @Override
    public void addLogoHeader(Bitmap logo)
    {
        this.headerLogo = logo;
    }

    @Override
    public OperationResult checkPaper() {
        return null;
    }
}
