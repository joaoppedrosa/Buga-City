package bugacity.com.bugacity.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import bugacity.com.bugacity.paypal.PaymentActivity;

/**
 * Created by Joao on 13/11/2014.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String state = extras.getString("message");

            if(state.startsWith("A reserva terminou")){
                String [] split = state.split(" ");
                String price = split[6].trim();
                Intent intentPayment = new Intent(context, PaymentActivity.class);
                intent.putExtra("price",price);
                intentPayment.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentPayment);
            }
        }
        ComponentName com = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context,(intent.setComponent(com)));
        setResultCode(Activity.RESULT_OK);
    }


}
