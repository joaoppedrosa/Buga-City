package bugacity.com.bugacity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import bugacity.com.bugacity.MainActivity;
import bugacity.com.bugacity.R;


public class LoginActivity extends Activity implements GooglePlusLoginUtils.GPlusLoginStatus {

    private String TAG = "LoginActivity";
    private GooglePlusLoginUtils gLogin;
    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        gLogin = new GooglePlusLoginUtils(this, R.id.activity_login_gplus);
        gLogin.setLoginStatus(this);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        if (sharedpreferences.contains(GooglePlusLoginUtils.NAME) && sharedpreferences.contains(GooglePlusLoginUtils.PHOTO)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        gLogin.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gLogin.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        gLogin.onActivityResult(requestCode, responseCode, intent);

    }

    @Override
    public void OnSuccessGPlusLogin(Bundle profile) {
        Log.i(TAG,profile.getString(GooglePlusLoginUtils.NAME));
        Log.i(TAG,profile.getString(GooglePlusLoginUtils.EMAIL));
        Log.i(TAG,profile.getString(GooglePlusLoginUtils.PHOTO));
        Log.i(TAG,profile.getString(GooglePlusLoginUtils.PROFILE));

        editor = sharedpreferences.edit();
        editor.putString("name", profile.getString(GooglePlusLoginUtils.NAME));
        editor.putString("mail", profile.getString(GooglePlusLoginUtils.EMAIL));
        editor.putString("photo", profile.getString(GooglePlusLoginUtils.PHOTO));
        editor.putString("logout", "false");
        editor.commit();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
