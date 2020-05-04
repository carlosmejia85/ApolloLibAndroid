package pin;

import android.app.Activity;
import android.content.Intent;

import com.spectratech.controllers.ControllerError;
import com.spectratech.controllers.ControllerMessage;
import com.spectratech.controllers.KeyDllController;
import com.spectratech.lib.level1.Ab;
import com.spectratech.lib.level1.HStr;
import com.spectratech.lib.level2.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import card.IApolloInterface;

import static com.spectratech.controllers.KKeyDllController.TKeyBlockConstant.K_ALGORITHM_TDEA;
import static com.spectratech.controllers.KKeyDllController.TKeyBlockConstant.K_USAGE_PIN;
import static com.spectratech.controllers.KKeyDllController.TKeyBlockConstant.K_USAGE_TMK;
import static com.spectratech.controllers.PEDDLL.ACTION_GET_PINBLOCK;
import static com.spectratech.controllers.PEDDLL.ACTIVITY_RESULT_OK;
import static com.spectratech.controllers.PEDDLL.ACTIVITY_RESULT_OPERATION_ERR;
import static com.spectratech.controllers.PEDDLL.EXTRA_KEY_INDEX;
import static com.spectratech.controllers.PEDDLL.EXTRA_PAN;
import static com.spectratech.controllers.PEDDLL.EXTRA_PINBLOCK_FORMAT;
import static com.spectratech.controllers.PEDDLL.VALUE_PINBLOCK_ISO9564_1_FMT0;

public class ApolloPED

{

    private String TAG = "ApolloPED";
    private Activity context;

    private IApolloInterface onData;

    public ApolloPED(Activity activity)
    {
        this.context = activity;
    }

    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    private byte[] masterKey;

    private byte[] encryptedWorkingKey;

    public IApolloInterface getOnData() {
        return onData;
    }

    public void setOnData(IApolloInterface onData) {
        this.onData = onData;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(byte[] masterKey) {
        this.masterKey = masterKey;
    }

    public byte[] getEncryptedWorkingKey() {
        return encryptedWorkingKey;
    }

    public void setEncryptedWorkingKey(byte[] encryptedWorkingKey)
    {
        this.encryptedWorkingKey = encryptedWorkingKey;
    }

    protected KeyDllController mKeyDllController;

    protected void releaseKeyDllController()
    {
        if (mKeyDllController != null)
        {
            mKeyDllController.disconnectController();
            mKeyDllController.releaseControllerInstance();
            mKeyDllController = null;
        }
    }

    public void startRequest ()
    {
        mKeyDllController = KeyDllController.getControllerInstance(getContext().getBaseContext(), new MyDelegate());
        mKeyDllController.enableDebugLog(true);

        mKeyDllController.connectController();

    }

    class MyDelegate implements KeyDllController.KeyDllDelegate
    {

        String TAG = "PEDDelegate";
        @Override
        public void onError(ControllerError.Error paramError, String paramString)
        {
            Log.i(TAG, "[onError]:" + paramError + " " + paramString + "\n");
        }

        @Override
        public void onControllerConnected()
        {
            Log.i(TAG, "[onControllerConnected]" + "\n");

            if (null != onData)
            {
                onData.onMessage( 1, "Connected" );
            }
        }

        @Override
        public void onControllerDisconnected() {
            Log.i(TAG, "[onControllerDisconnected]" + "\n");
        }

        @Override
        public void onDeviceInfoReceived(Hashtable<String, String> hashtable) {
            Log.i(TAG, "[onDeviceInfoReceived]" + "\n");
        }

        @Override
        public void onMessageReceived(ControllerMessage.MessageText paramMessageText) {
            Log.i(TAG, "[onMessageReceived]" + "[" + paramMessageText + "]" + "\n");
        }
    };

    public int clearAllKeys()
    {
        String sFn = "callClearAllKeys";
        int ret = ACTIVITY_RESULT_OPERATION_ERR;
        boolean flagDelete = mKeyDllController.keyDeleteAll(context);

        if (flagDelete)
        {
            ret = ACTIVITY_RESULT_OK;
        }

        return ret;
    }

    private byte[] calculateKvc(byte[] aKey) {
        byte[] kvc = null;
        try {
            Cipher encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aKey, "DESede"));
            kvc = encryptCipher.doFinal(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            Log.d(TAG, "calculateKvc, key " + Ab.toHStr(aKey) +", kvc " + Ab.toHStr(kvc) );
            kvc = Ab.sub(kvc, 0, 4);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return kvc;
    }


    /***
     * Este se usa normal con la MasterKey
     * @return
     */

    public int injectTerminalMasterKey()
    {
        final String sFn = "injectTMK";


        byte[] key      =  masterKey; //FIX_TMK_ARRAY[0];
        short tmkIdx    = -1    ;
        short keyIdx    = 0x00  ;

        byte[] kvc = calculateKvc(key);
        Log.d(TAG, "TmkIdx:"+tmkIdx);
        Log.d(TAG, "KeyIdx:"+keyIdx);
        Log.d(TAG, "TMK:" + Ab.toHStr(key));
        Log.d(TAG, "KVC:" + Ab.toHStr(kvc));


        byte algorithm  = K_ALGORITHM_TDEA;
        byte usage      = K_USAGE_TMK;
        int resultCode  = mKeyDllController.keyInject(context, tmkIdx, keyIdx, key, algorithm, usage, kvc);

        return (resultCode);
    }

    /**
     * Esta varía un poco, ya que normalmente tenemos una EWK, las cual es la WK pero encriptada
     * y se inserta encriptada en la PED, pero en este caso acá debemos de pasarle como parámetro
     * la llave sin encriptar, es decir, la WK y la función internamete
     */
    public int injectEncryptedWorkingKey( )
    {
        //default key
        //537291D2EFF6E9BD9A7AEB1B8880BB81



        Log.d(TAG, "callKeyInjection: Start");
        short tmkIdx = 0x00;
        byte[] ePINKey = null;
        byte[] kvc = null;


        if (tmkIdx != -1)
        {

            Cipher encryptCipher = null;

            Log.i(TAG, "Finding Working Key");
            try
            {

                encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");

                encryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getMasterKey(), "DESede"));

                ePINKey = encryptCipher.doFinal(getEncryptedWorkingKey());
            }
            catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException e)
            {
                e.printStackTrace();

                Log.e(TAG, e.getMessage());
            }


            Log.i(TAG, "Calculate KVC");

            // cal kvc 1st
            kvc = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            try
            {
                encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");
                encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ePINKey, "DESede"));
                kvc = encryptCipher.doFinal(kvc);
            } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }



            /*try
            {

                encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");
                //encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(FIX_TMK_ARRAY[mTmkDataIdx], "DESede"));

                encryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, "DESede"));
                ePINKey = encryptCipher.doFinal(ePINKey);
            } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException e) {
                e.printStackTrace();
            }

             */

        }



        short keyIdx = 1; //(short) Str.toInt(mEtKeyIdx.getText().toString().replace(".", ""));
        byte[] keyDat = encryptedWorkingKey;


        byte algorithm  = K_ALGORITHM_TDEA;
        byte usage      = K_USAGE_PIN;


        Log.d(TAG, "EWK:" + Ab.toHStr(keyDat));
        Log.d(TAG, "KVC:" + Ab.toHStr(kvc));
        Log.d(TAG, "TmkIdx:"+tmkIdx);
        Log.d(TAG, "KeyIdx:"+keyIdx);

        int resultCode = mKeyDllController.keyInject(
                context,
                tmkIdx,
                keyIdx,
                keyDat,
                algorithm,
                usage,
                kvc
        );

        return resultCode;
    }

    private static final int REQ_GET_PINBLOCK = 1005;

    public int askForPin (String pan)
    {




        final String sFnName = "callGetPinBlock";

        short keyIndex = 1;
        byte[] abPan = HStr.toAbF( pan );

        if ((keyIndex < 0) || (Ab.nnLen(abPan) <= 0) )
        {
            return -1;
        }

        Intent intent = new Intent(ACTION_GET_PINBLOCK);
        intent.putExtra(EXTRA_KEY_INDEX, keyIndex);
        intent.putExtra(EXTRA_PAN, abPan);

        intent.putExtra(EXTRA_PINBLOCK_FORMAT, VALUE_PINBLOCK_ISO9564_1_FMT0);


        getContext().startActivityForResult(intent, REQ_GET_PINBLOCK);
        return 0;

    }

}
