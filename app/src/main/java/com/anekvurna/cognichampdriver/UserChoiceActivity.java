package com.anekvurna.cognichampdriver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.anekvurna.cognichampdriver.SanathUtilities.*;


public class UserChoiceActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_choice);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            takeToProfilesActivity();
            return;
        }
        initializeGoogleAuthService();
        initializeFacebookAuthService();
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.anekvurna.cognichampdriver",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            showToast(this, "Error");

        } catch (NoSuchAlgorithmException e) {
            showToast(this, "Error2");
        }*/
    }

    private void initializeFacebookAuthService() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                // ...
            }
        });
    }

      /*  @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the activity result back to the Facebook SDK

        }*/


    private void handleFacebookAccessToken(AccessToken token) {

        setProgressBar(this,true, "Signing in...");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setProgressBar(UserChoiceActivity.this,false, "dummy");
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(!task.getResult().getAdditionalUserInfo().isNewUser()) {
                                handlePreviousUser();
                            }
                            else
                            {
                                loadActivityAndFinish(UserChoiceActivity.this, MobileVerificationActivity.class);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            showToast(UserChoiceActivity.this, "Authentication failed");
                        }

                        // ...
                    }
                });
    }

    void initializeGoogleAuthService()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    private void takeToProfilesActivity() {
        initializeSharedPrefs(this);
        int profileStatus = preferences.getInt(getString(R.string.profile_status),0);
        if(!preferences.getBoolean("mobileVerified", false))
        {
            loadActivityAndFinish(this, MobileVerificationActivity.class);
            return;
        }
        switch (profileStatus)
        {
            case 0 : loadActivityAndFinish(this, ProfileBasicActivity.class);break;
            case 1 : loadActivityAndFinish(this, ProfileAddressActivity.class);break;
            case 2 : loadActivityAndFinish(this, ProfileOfficialActivity.class);break;
            case 3 : loadActivityAndFinish(this, ProfileCarActivity.class);break;
            default: loadActivityAndFinish(this, ViewTabbedActivity.class);break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                showToast(UserChoiceActivity.this, "Google Sign In Failed");
                // [END_EXCLUDE]
            }
        }
        else
        {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        setProgressBar(this,true, "Signing in...");
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setProgressBar(UserChoiceActivity.this,false, "dummy");
                            // Sign in success, update UI with the signed-inLog.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(!task.getResult().getAdditionalUserInfo().isNewUser()) {
                               handlePreviousUser();
                            }
                            else
                            {
                                loadActivityAndFinish(UserChoiceActivity.this, MobileVerificationActivity.class);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            showToast(UserChoiceActivity.this, "Sign in failed");

                        }
                    }
                });
    }

    void handlePreviousUser()
    {
        initializeSharedPrefs(UserChoiceActivity.this);
        editor.putInt("profileStatus", 4);
        editor.apply();
        /*Intent intent = new Intent(this, MobileVerificationActivity.class);
        intent.putExtra("isNew", true);
        startActivity(intent);*/
        loadActivityAndFinish(this, MobileVerificationActivity.class);
    }

    public void onRegister(View view)
    {
        loadActivity(this, EmailRegistrationActivity.class);
    }

    public void onLogIn(View view)
    {
        loadActivity(this, EmailLogInActivity.class);
    }
}
