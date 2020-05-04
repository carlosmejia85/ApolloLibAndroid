package card;

public interface IApolloInterface
{
    void onMessage       (  int code           , String mensaje );

    void onCardDetected  (  int cardResult     , String mensaje );

    int  selectedAID     (  String aidList                      );


    byte[] onPin         (  int cardResult     , String mensaje );
}
