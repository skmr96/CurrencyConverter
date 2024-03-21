package com.example.satheeshm.currencyconverter;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnection {

    public static boolean checkConnection(Context context)
    {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        if(connMgr !=null)
        {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo !=null){
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return true;
                }
            }
        }
        return false;
    }
}
