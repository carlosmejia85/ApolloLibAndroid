package card;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.spectratech.controllers.BaseCardController;
import com.spectratech.controllers.ControllerError;
import com.spectratech.controllers.ControllerMessage;
import com.spectratech.controllers.PEDDLL;
import com.spectratech.controllers.TransactionFlowController;
import com.spectratech.lib.level1.HStr;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import static com.spectratech.controllers.PEDDLL.ACTION_GET_PINBLOCK;
import static com.spectratech.controllers.PEDDLL.EXTRA_KEY_INDEX;
import static com.spectratech.controllers.PEDDLL.EXTRA_PAN;
import static com.spectratech.controllers.TransactionFlowController.GenericStatus.FALSE;

public class ApolloCard
{

    private Hashtable<String, String> field55   ;

    private Context             context         ;

    private Activity            anActivity      ;

    private String              amount          ;

    private String              cashbackAmount  ;

    private String              moneda          ;

    private IApolloInterface onData          ;

    private String              track2          ;

    //public ApolloCard(Context context)
    //{
    //    this.context = context;
    //}

    public ApolloCard(Activity context)
    {
        this.anActivity = context;
        defaultField55();
    }

    public IApolloInterface getOnData()
    {
        return onData;
    }

    public void setOnData(IApolloInterface onData)
    {
        this.onData = onData;
    }

    public  Context     getContext          (                           )
    {
        return context;
    }

    public  void        setContext          (   Context context         )
    {
        this.context = context;
        defaultField55();
    }

    public  String      getAmount           (                           )
    {
        return amount;
    }

    public  void        setAmount           (   String amount           )
    {
        this.amount = amount;
    }

    public  String      getCashbackAmount   (                           )
    {
        return cashbackAmount;
    }

    public  void        setCashbackAmount   (   String cashbackAmount   )
    {
        this.cashbackAmount = cashbackAmount;
    }

    public  String      getMoneda           (                           )
    {
        return moneda;
    }

    public  void        setMoneda           (   String moneda           )
    {
        this.moneda = moneda;
    }

    public  String      getTrack2           (                           )
    {
        return track2;
    }

    public  void        setTrack2           (   String track2           )
    {
        this.track2 = track2;
    }

    public  Hashtable<String, String>       getField55  (                                       )
    {
        return field55;
    }

    public  void                            setField55  (Hashtable<String, String> field55      )
    {
        this.field55 = field55;
    }

    public void defaultField55 ()
    {
        field55 = new Hashtable<String, String>()
        {

            {
                put( "82"   ,   "");	// APPLICATION INTERCHANGE PROFILE (AIP)
                put( "84"   ,   "");	// AID - Dedicated File (DF) Name
                put( "95"   ,   "");    // TERMINAL VERIFICATION RESULTS (TVR)
                put( "9A"   ,   "");	// TRANSACTION DATE
                put( "9C"   ,   "");	// TRANSACTION TYPE
                put( "5F2A" ,   "");    // TRANSACTION CURRENCY CODE
                put( "5F34" ,   "");    // PAN SEQUENCE NUMBER  --  *** notice it's limited to L=1
                put( "9F02" ,   "");	// Amount, Authorised
                put( "9F03" ,   "");	// Amount, Other
                put( "9F10" ,   "");	// ISSUER APPLICATION DATA /IAD
                put( "9F1A" ,   "");	// TERMINAL COUNTRY CODE
                put( "9F1E" ,   "");	// Interface Device (IFD) Serial
                put( "9F26" ,   "");	// APPLICATION CRYPTOGRAM
                put( "9F27" ,   "");	// CRYPTOGRAM INFORMATION DATA (CID)
                put( "9F33" ,   "");	// Terminal Capabilities
                put( "9F34" ,   "");	// APPLICATION PAN SEQUENCE NUMBER
                put( "9F35" ,   "");	// Terminal Type
                put( "9F36" ,   "");	// APPLICATION TRANSACTION COUNTER (ATC)
                put( "9F37" ,   "");	// UNPREDICTABLE NUMBER
                put( "9F6E" ,   "");    // Form Factor Indicator
                put( "9F7C" ,   "");    // Customer Exclusive Data (CED) ver C-3_Kernel_3_v2.6 pag 131
            }
        };
    }
    public enum ActivityRequestCode
    {
        GET_OFFLINE_PIN ,
        GET_ONLINE_PIN  ,
    }


    private TransactionFlowController mTransactionFlowController;

    TransactionFlowController.CheckCardMode mode = TransactionFlowController.CheckCardMode.SWIPE_OR_INSERT_OR_TAP;


    public void startRequest (int checkCardMode)
    {

        switch (checkCardMode)
        {
            case 0:
                mode = TransactionFlowController.CheckCardMode.NONE;
                break;
            case 1:
                mode = TransactionFlowController.CheckCardMode.SWIPE;
                break;
            case 2:
                mode = TransactionFlowController.CheckCardMode.INSERT;
                break;
            case 3:
                mode = TransactionFlowController.CheckCardMode.TAP;
                break;
            case 4:
                mode = TransactionFlowController.CheckCardMode.SWIPE_OR_INSERT;
                break;
            case 5:
                mode = TransactionFlowController.CheckCardMode.SWIPE_OR_TAP;
                break;
            case 6:
                mode = TransactionFlowController.CheckCardMode.INSERT_OR_TAP;
                break;
            case 7:
                mode = TransactionFlowController.CheckCardMode.SWIPE_OR_INSERT_OR_TAP;
                break;
        }


        mTransactionFlowController = TransactionFlowController.getControllerInstance(this.anActivity, new MyDelegate());

        mTransactionFlowController.enableDebugLog(true);

        String content = "";
        content += "API Version: " + mTransactionFlowController.getApiVersion() + "\n";

        mTransactionFlowController.connectController();


    }


    boolean validateServiceCode( String track2Data )
    {

        String[] buffer = null;

        String secondPart = "";
        if(null == track2Data)
        {
            return false;
        }

        if (track2Data.length() == 0)
        {
            return false;
        }

        buffer = track2Data.split("=");

        if (buffer == null)
        {
            return false;
        }

        if (buffer.length <= 1)
        {
            return false;
        }

        secondPart = buffer[1];

        char indicator = secondPart.charAt( 5 );

        if( ( ( indicator == '2' ) || ( indicator == '6' ) ) )
        {
            return true;
        }

        return false;
    }


    /**
     * release Controller
     */
    protected void releaseController()
    {
        if (mTransactionFlowController != null)
        {
            mTransactionFlowController.abortDetection           (   );
            mTransactionFlowController.disconnectController     (   );
            mTransactionFlowController.releaseControllerInstance(   );
            mTransactionFlowController = null;
        }

    }

    class MyDelegate implements TransactionFlowController.TransactionFlowDelegate {
        @Override
        public void onError(ControllerError.Error paramError, String paramString)
        {
            String error = String.format("[%s] -> [%s]", paramString, paramError.name());
            if (null != onData)
            {
                onData.onMessage( ApolloCode.ERROR.ordinal(), error );
            }
        }

        @Override
        public void onControllerConnected( )
        {

            Hashtable<String, Object> data = new Hashtable<String, Object>();
            data.put(TransactionFlowController.EMV_OPTION                       , TransactionFlowController.EmvOption.START                     );
            data.put(TransactionFlowController.CHKCRD_MODE                      , mode                                                          );
            data.put(TransactionFlowController.AMOUNT                           , getAmount             ()                                      );
            data.put(TransactionFlowController.CASHBACKAMOUNT                   , getCashbackAmount     ()                                      );
            data.put(TransactionFlowController.TRANSACTIONTYPE                  , TransactionFlowController.TransactionType.GOODS               );
            data.put(TransactionFlowController.CURRENCYCODE                     , getMoneda             ()                                      );
            data.put(TransactionFlowController.EMV_TXNNO                        , "000001"                                                      );        // must pass to lib
            data.put(TransactionFlowController.EMV_ISCLFINALCONFIRMATIONENABLE  , FALSE                                                         );

            mTransactionFlowController.startTransactionFlow(data);
        }

        @Override
        public void onControllerDisconnected()
        {
            if (null != onData)
            {
                onData.onMessage(ApolloCode.CONTROLLER_CONNECTED.ordinal(), "onControllerDisconnected");
            }
        }

        @Override
        public void onDeviceInfoReceived(Hashtable<String, String> hashtable)
        {

            if (null != onData)
            {
                onData.onMessage(ApolloCode.DATA_RECEIVED.ordinal(), hashtable != null ? hashtable.toString() : "");
            }

        }

        @Override
        public void onMessageReceived(ControllerMessage.MessageText messageText)
        {
            if (null != onData)
            {
                onData.onMessage(ApolloCode.MESSAGE_RECEIVED.ordinal(), messageText.toString());
            }

        }

        @Override
        public void onCardInteractionDetecting(BaseCardController.CheckCardMode checkCardMode)
        {
            if (null != onData)
            {
                onData.onMessage(ApolloCode.CARD_DETECTING.ordinal(), checkCardMode.toString());
            }

        }

        @Override
        public void onDetectCardInteractionAborted(boolean b)
        {

            if (null != onData)
            {
                onData.onMessage(ApolloCode.CARD_INTERACTION_ABORTED.ordinal(), b == true ? "true" : "false");
            }
        }


        public void getTrack2FromTLV(Hashtable<String, String> hashtable)
        {
            String tag = "track2";

            if (hashtable == null)
            {
                track2 = "";
                return;
            }


            if (hashtable.contains(tag))
            {
                track2 = hashtable.get(tag);
            }
        }

        @Override
        public void onCardInteractionDetected(BaseCardController.CheckCardResult checkCardResult, Hashtable<String, String> hashtable)
        {

            getTrack2FromTLV (hashtable);

            if (null != onData)
            {
                onData.onCardDetected(ApolloCode.CARD_DETECTED.ordinal(), hashtable != null ? hashtable.toString() : "");
            }

            if (checkCardResult == BaseCardController.CheckCardResult.MSR)
            {

                if (track2 != null && !track2.equals(""))
                {

                    boolean validated = validateServiceCode( track2 );

                    if (onData != null)
                    {
                        onData.onMessage(  ApolloCode.CARD_SERVICE_CODE_VALIDATED.ordinal(),  validated == true ? "true" : "false" );
                    }

                    if (validated )
                    {
                        releaseController();

                        startRequest( ApolloCheckCardMode.INSERT_OR_TAP );

                    }

                }
            }
        }

        @Override
        public void onCTLAudioToneReceived(BaseCardController.ContactlessStatusTone contactlessStatusTone)
        {

        }

        @Override
        public void onCTLLightReceived(BaseCardController.ContactlessStatusLed contactlessStatusLed) {

        }

        @Override
        public void onSelectAIDRequested(ArrayList<String> arrayList)
        {
            if (null != onData)
            {
                String aids = "";

                for (String aid : arrayList)
                {
                    aids += aid + ",";
                }
                int selected = onData.selectedAID       (  aids         );

                mTransactionFlowController.selectAID    (   selected    );

            }
            else
            {
                mTransactionFlowController.selectAID(0);
            }
        }

        @Override
        public void onConfirmationRequested(String s)
        {



            //String sPan = data.containsKey("5A") ? data.get("5A") : "Tag 5A not found";

            mTransactionFlowController.sendConfirmation(true);
        }

        @Override
        public void onOnlineProcessRequested(String s)
        {

            Hashtable<String, String> data = BaseCardController.decodeTlv(s);


            // getting keySet() into Set
            Set<String> setOfTags = field55.keySet();


            for (String tag : setOfTags)
            {
                if (data.containsKey(tag))
                {
                    field55.put(tag,  data.get(tag) );
                }
            }

            if (null != onData)
            {
                onData.onMessage(  ApolloCode.ONLINE_PROCESSING_REQUESTED.ordinal(), field55.toString());
            }

            //mTextView.append("[" + "onOnlineProcessRequested" + "]" + "\n");
            final String host_resp = "8A023030";
            mTransactionFlowController.sendOnlineProcessingData(host_resp);
        }

        @Override
        public void onBatchDataReceived(String s)
        {

        }

        @Override
        public void onReversalDataReceived(String s)
        {

        }

        @Override
        public void onTransactionStatusReceived(TransactionFlowController.TransactionResult transactionResult)
        {
            if (null != onData)
            {
                onData.onMessage(ApolloCode.TRANSACTION_STATUS.ordinal(), transactionResult.toString());
            }

            releaseController();
        }

        @Override
        public void onPinEntryRequested(TransactionFlowController.PinEntrySource pinEntrySource, String s)
        {

            final boolean isOffline = (pinEntrySource == TransactionFlowController.PinEntrySource.PEDDLL_OFFLINE) ? true : false;


            if (pinEntrySource == TransactionFlowController.PinEntrySource.PEDDLL_OFFLINE)
            {
                final Intent intent = new Intent(ACTION_GET_PINBLOCK);
                intent.putExtra(PEDDLL.EXTRA_OFFLINE_ONLY, true);
                if (anActivity != null)
                    anActivity.startActivityForResult(intent, ActivityRequestCode.GET_OFFLINE_PIN.ordinal());

                return;
            }

            if (pinEntrySource == TransactionFlowController.PinEntrySource.PEDDLL_ONLINE)
            {
                final Intent intent = new Intent(ACTION_GET_PINBLOCK);
                intent.putExtra(EXTRA_KEY_INDEX, (short) 1);
                intent.putExtra(EXTRA_PAN, HStr.toAb("123456789012"));
                //intent.putExtra(EXTRA_KSN, HStr.toAb("00112233445566778899"));
                if (anActivity != null)
                    anActivity.startActivityForResult(intent, ActivityRequestCode.GET_ONLINE_PIN.ordinal());
                return;
            }
        }

        @Override
        public void onEmvCardDataReceived(boolean b, String s)
        {

        }

        @Override
        public void onEmvCardNumberReceived(boolean b, String s)
        {

        }

        @Override
        public void onSetAmountRequest(String s)
        {


            Hashtable<String, Object> data = new Hashtable<String, Object>();
            data.put(TransactionFlowController.AMOUNT           , getAmount         ()  );
            data.put(TransactionFlowController.CASHBACKAMOUNT   , getCashbackAmount ()  );
            data.put(TransactionFlowController.CURRENCYCODE     , getMoneda         ()  );

            mTransactionFlowController.setAmount(data);
        }
    }



}
