package bugacity.com.bugacity.paypal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPayment;

import java.math.BigDecimal;

import bugacity.com.bugacity.R;


public class PaymentActivity extends ActionBarActivity {

    private static Activity	activity;
    private String			money = "5";
    private LinearLayout	layout;
    private TextView requestView;
    private CheckoutButton	payButton;
    private PayPal			pp;
    private boolean			paypalIsInit	= false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        activity = this;
        initLibrary();
        initUi();
        requestView.setText(getString(R.string.xy_wants_z_dollar_from_you, money, "BugaCity"));
    }

    public void initUi() {
        layout = (LinearLayout) findViewById(R.id.receive_layout);
        requestView = (TextView) findViewById(R.id.receive_request_text);
    }

    public void initLibrary() {
        new Thread() {
            @Override
            public void run() {
                pp = PayPal.getInstance();

                if (pp == null) {
                    pp = PayPal.initWithAppID(activity,
                            "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
                    pp.setLanguage("en_US");
                    pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
                    pp.setShippingEnabled(true);
                    pp.setDynamicAmountCalculationEnabled(false);

                    paypalIsInit = true;

                    payButton = pp.getCheckoutButton(activity,
                            PayPal.BUTTON_194x37, CheckoutButton.TEXT_PAY);
                    payButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            onPayClick(v);
                        }
                    });
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            layout.addView(payButton);
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }


    public PayPalPayment createPayment() {
        final PayPalPayment payment = new PayPalPayment();
        payment.setCurrencyType("EUR");
        payment.setRecipient("info@bugacity.pt");
        payment.setSubtotal(new BigDecimal(5));
        payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
        return payment;
    }

    public static void showToast(final int text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onPayClick(View view) {
        if (paypalIsInit) {
            final PayPalPayment payment = createPayment();
            final Intent checkoutIntent = PayPal.getInstance().checkout(
                    payment, this, new ResultDelegate());
            startActivity(checkoutIntent);
        }
    }
}
