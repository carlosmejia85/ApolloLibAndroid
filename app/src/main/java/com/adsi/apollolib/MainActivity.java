package com.adsi.apollolib;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.adsi.apollolibrary.printer.ALINEAMIENTO;
import com.adsi.apollolibrary.printer.ApolloPrinter;
import com.adsi.apollolibrary.printer.TAMANIO_LETRA;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import card.ApolloCard;
import card.ApolloCheckCardMode;
import card.ApolloCode;
import card.IApolloInterface;
import pin.ApolloPED;
import util.ApolloUtil;

public class MainActivity extends AppCompatActivity
{


    Button btnImprimir  ;
    Button btnTarjeta   ;

    static public final int REQ_GET_PINBLOCK = 1005;

    public final static String EXTRA_ENCRYPTED_PINBLOCK = "com.spectratech.mpos.peddll.encryptedpinblock";

    public static final int ACTIVITY_RESULT_KEY_NOT_INJECTED = 1005;
    public static final int ACTIVITY_RESULT_PARAM_ERR = 1002;
    public static final int ACTIVITY_RESULT_INVALID_KEY_USAGE = 1010;
    public static final int ACTIVITY_RESULT_PIN_BYPASS = 1011;
    static public final int INTEX_RESULT_PIN_EXCESS = 104;

    private void resultGetPINBlock(int aResultCode, Intent aData)
    {
        String strMsg = null;
        String strKsn = null;
        if (aResultCode == RESULT_OK)
        {
            byte[] abPb = aData.getByteArrayExtra(EXTRA_ENCRYPTED_PINBLOCK);

            if (abPb.length > 0)
            {
                String pinBlock = ApolloUtil.bcd2str(abPb, 0, abPb.length * 2, false);

                strMsg = String.format("PinBlock = %s", pinBlock);
            }
            else
            {
                strMsg = "NO PinBlock";
            }
        }
        else if (aResultCode == ACTIVITY_RESULT_KEY_NOT_INJECTED)
        {
            strMsg = "R.string.key_not_injected";
        }
        else if (aResultCode == INTEX_RESULT_PIN_EXCESS)
        {
            strMsg = "R.string.entry_lmt_exceed";
        }
        else if (aResultCode == ACTIVITY_RESULT_PARAM_ERR)
        {
            strMsg = "R.string.input_param_err";
        }
        else if (aResultCode == ACTIVITY_RESULT_INVALID_KEY_USAGE)
        {
            strMsg = "R.string.invalid_key_usage";
        }
        else if (aResultCode == ACTIVITY_RESULT_PIN_BYPASS)
        {
            strMsg = "R.string.pin_bypass";
        }
        else
        {
            strMsg = "R.string.nok_or_cancel";
        }

        if (strMsg == null)
        {
            strMsg = "%s: strMsg == null";
            Log.w("PinBlock", strMsg);
        }
        Toast.makeText(this,strMsg, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch (requestCode)
        {
            case REQ_GET_PINBLOCK:
                resultGetPINBlock(resultCode, data);
                break;
        }
    }

    Button btnPIN       ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnImprimir = findViewById( R.id.btnPrinter );

        if (null != btnImprimir)
        {
            btnImprimir.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    printerDemo();
                }
            });
        }

        btnTarjeta = findViewById( R.id.btnTarjeta );

        btnTarjeta.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pedirTarjeta();
            }
        });

        btnPIN = findViewById(R.id.btnPIN);

        if (null != btnPIN)
        {
            btnPIN.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    pedirPIN();
                }
            });
        }
    }


    public void pedirTarjeta ()
    {
        ApolloCard card = new ApolloCard( MainActivity.this );


        card.setAmount          ( "90.7");
        card.setCashbackAmount  ("0.00");
        card.setMoneda          ("0558");

        card.setOnData(new IApolloInterface()
        {
            @Override
            public void onMessage(int code, String mensaje)
            {
                Log.i("onMessage", String.format("Codigo = %d Mensaje %s", code, mensaje));

                if( code ==  ApolloCode.ONLINE_PROCESSING_REQUESTED.ordinal())
                {
                    Log.i("Info", "ApolloCode.ONLINE_PROCESSING_REQUESTED");

                    String campo55 = mensaje;

                }
            }

            @Override
            public void onCardDetected(int cardResult, String mensaje)
            {
                Log.i("onMessage", String.format("cardResult = %d Mensaje %s", cardResult, mensaje));
            }

            @Override
            public int selectedAID(String aidList)
            {
                return 0;
            }

            @Override
            public byte[] onPin(int cardResult, String mensaje)
            {
                return new byte[0];
            }
        });

        card.startRequest(ApolloCheckCardMode.SWIPE_OR_INSERT_OR_TAP);

    }

    public void printerDemo ()
    {

        ApolloPrinter printer = new ApolloPrinter( MainActivity.this);


        printer.setOnData(new IApolloInterface()
        {
            @Override
            public void onMessage(int code, String mensaje)
            {

            }

            @Override
            public void onCardDetected(int cardResult, String mensaje)
            {

            }

            @Override
            public int selectedAID(String aidList)
            {
                return 0;
            }

            @Override
            public byte[] onPin(int cardResult, String mensaje)
            {
                return new byte[0];
            }
        });
        Bitmap bitmap = BitmapFactory.decodeResource(  getResources(),
                R.drawable.android_bitmap);






        //printer.addLogoHeader(  bitmap );
        //------------------
        //-- Pequena
        //------------------

        printer.addLineaImprimir("LINEA 1 \n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.IZQUIERDA  );
        printer.addLineaImprimir("LINEA 2 \n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.CENTRO     );
        printer.addLineaImprimir("LINEA 3\n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.DERECHA    );
        printer.addLineaImprimir("LINEA 3                   012345345\n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.CENTRO    );

        //------------------
        //-- Mediana
        //------------------
        printer.addLineaImprimir("TRANSACCION COMPRA \n"     , TAMANIO_LETRA.PEQUENIO, ALINEAMIENTO.IZQUIERDA  );
        printer.addLineaImprimir("LINEA 2 \n"     , TAMANIO_LETRA.PEQUENIO, ALINEAMIENTO.CENTRO     );
        printer.addLineaImprimir("Transaccion de Anulacion 3\n"     , TAMANIO_LETRA.PEQUENIO, ALINEAMIENTO.DERECHA    );

        //------------------
        //-- GRANDE
        //------------------
        printer.addLineaImprimir("SUB TOTAL $ 1.00\n"     , TAMANIO_LETRA.GRANDE  , ALINEAMIENTO.IZQUIERDA  );
        printer.addLineaImprimir("TAX       $ 0.10\n"     , TAMANIO_LETRA.GRANDE  , ALINEAMIENTO.CENTRO     );
        printer.addLineaImprimir("TOTAL     $ 1.10\n"     , TAMANIO_LETRA.GRANDE  , ALINEAMIENTO.DERECHA    );

        printer.addLineaImprimir("GRACIAS\n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.CENTRO    );
        printer.addLineaImprimir("TOTAL\n"     , TAMANIO_LETRA.MEDIANO, ALINEAMIENTO.DERECHA   );

        printer.imprimirDocumento();


    }



    public void pedirPIN ()
    {
        byte[] masterKey = new byte[] { (byte)0x85, (byte)0xBF, (byte)0xFC, (byte)0x9F, (byte)0xA1, (byte)0x82, (byte)0xD7, (byte)0x1F,
                (byte)0x56, (byte)0xA5, (byte)0x9C, (byte)0x35, (byte)0xB3, (byte)0xB9, (byte)0xBE, (byte)0x99,
                (byte)0x15, (byte)0x21, (byte)0xBB, (byte)0x84, (byte)0xDA, (byte)0x17, (byte)0x94, (byte)0x76
        };

        byte[] ewk = new byte[]
                {
                        0x30, (byte)0xF6, 0x1F, 0x5F, (byte)0x87, 0x34, 0x3F, 0x59, (byte)0x9D, 0x32,(byte) 0x91, 0x78, (byte)0xD3, (byte)0xFC, (byte)0xA9, (byte)0xA9
                };


        final ApolloPED ped = new ApolloPED( MainActivity.this );
        ped.setMasterKey            (   masterKey   );
        ped.setEncryptedWorkingKey  (   ewk         );

        ped.setOnData(new IApolloInterface() {
            @Override
            public void onMessage(int code, String mensaje)
            {
                if (code == 1)
                {
                    ped.clearAllKeys                ();
                    ped.injectTerminalMasterKey     ();
                    ped.injectEncryptedWorkingKey   ();

                    int result = ped.askForPin( "411827115117934" );
                }
            }

            @Override
            public void onCardDetected(int cardResult, String mensaje) {

            }

            @Override
            public int selectedAID(String aidList) {
                return 0;
            }

            @Override
            public byte[] onPin(int cardResult, String mensaje) {
                return new byte[0];
            }
        });
        ped.startRequest                ();




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
