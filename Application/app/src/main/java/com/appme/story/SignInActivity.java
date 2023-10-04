package com.appme.story;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.appme.story.databinding.ActivitySigninBinding;

public class SignInActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_CREDENTIAL = "key_credential";
    private static final String KEY_CREDENTIAL_TO_SAVE = "key_credential_to_save";

    private static final int RC_SIGN_IN = 1;
    private static final int RC_CREDENTIALS_READ = 2;
    private static final int RC_CREDENTIALS_SAVE = 3;
    private ActivitySigninBinding binding;

    private CredentialsClient mCredentialsClient;
    private GoogleSignInClient mSignInClient;
    private ProgressDialog mProgressDialog;
    private boolean mIsResolving = false;
    private Credential mCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING, false);
            mCredential = savedInstanceState.getParcelable(KEY_CREDENTIAL);
        }

        // Build CredentialsClient and GoogleSignInClient, don't set account name
        buildClients(null);
        
        // Sign in button
        SignInButton signInButton = (SignInButton) findViewById(R.id.button_google_sign_in);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);

        // Other buttons
        binding.buttonEmailSignIn.setOnClickListener(this);
        binding.buttonGoogleRevoke.setOnClickListener(this);
        binding.buttonGoogleSignOut.setOnClickListener(this);
        binding.buttonEmailSignIn.setOnClickListener(this);
        binding.buttonEmailSave.setOnClickListener(this);
    }

    private void buildClients(String accountName) {
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        if (accountName != null) {
            gsoBuilder.setAccountName(accountName);
        }

        mCredentialsClient = Credentials.getClient(this);
        mSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putParcelable(KEY_CREDENTIAL, mCredential);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mIsResolving) {
            requestCredentials(true /* shouldResolve */, false /* onlyPasswords */);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        } else if (requestCode == RC_CREDENTIALS_READ) {
            mIsResolving = false;
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                handleCredential(credential);
            }
        } else if (requestCode == RC_CREDENTIALS_SAVE) {
            mIsResolving = false;
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Credential save failed.");
            }
        }
    }

    private void googleSilentSignIn() {
        // Try silent sign-in with Google Sign In API
        Task<GoogleSignInAccount> silentSignIn = mSignInClient.silentSignIn();

        if (silentSignIn.isComplete() && silentSignIn.isSuccessful()) {
            handleGoogleSignIn(silentSignIn);
            return;
        }

        showProgress();
        silentSignIn.addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                hideProgress();
                handleGoogleSignIn(task);
            }
        });
    }

    private void handleCredential(Credential credential) {
        mCredential = credential;

        Log.d(TAG, "handleCredential:" + credential.getAccountType() + ":" + credential.getId());
        if (IdentityProviders.GOOGLE.equals(credential.getAccountType())) {
            // Google account, rebuild GoogleApiClient to set account name and then try
            buildClients(credential.getId());
            googleSilentSignIn();
        } else {
            // Email/password account
            String status = String.format("Signed in as %s", credential.getId());
            ((TextView) findViewById(R.id.text_email_status)).setText(status);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleGoogleSignIn:" + completedTask);

        boolean isSignedIn = (completedTask != null) && completedTask.isSuccessful();
        if (isSignedIn) {
            // Display signed-in UI
            GoogleSignInAccount gsa = completedTask.getResult();
            String status = String.format("Signed in as %s (%s)", gsa.getDisplayName(),
                    gsa.getEmail());
            binding.textGoogleStatus.setText(status);

            // Save Google Sign In to SmartLock
            Credential credential = new Credential.Builder(gsa.getEmail())
                    .setAccountType(IdentityProviders.GOOGLE)
                    .setName(gsa.getDisplayName())
                    .setProfilePictureUri(gsa.getPhotoUrl())
                    .build();

            saveCredential(credential);
        } else {
            // Display signed-out UI
            binding.textGoogleStatus.setText(R.string.signed_out);
        }

        binding.buttonGoogleSignIn.setEnabled(!isSignedIn);
        binding.buttonGoogleSignOut.setEnabled(isSignedIn);
        binding.buttonGoogleRevoke.setEnabled(isSignedIn);
    }

    private void requestCredentials(final boolean shouldResolve, boolean onlyPasswords) {
        CredentialRequest.Builder crBuilder = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true);

        if (!onlyPasswords) {
            crBuilder.setAccountTypes(IdentityProviders.GOOGLE);
        }

        showProgress();
        mCredentialsClient.request(crBuilder.build()).addOnCompleteListener(
                new OnCompleteListener<CredentialRequestResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
                        hideProgress();

                        if (task.isSuccessful()) {
                            // Auto sign-in success
                            handleCredential(task.getResult().getCredential());
                            return;
                        }

                        Exception e = task.getException();
                        if (e instanceof ResolvableApiException && shouldResolve) {
                            // Getting credential needs to show some UI, start resolution
                            ResolvableApiException rae = (ResolvableApiException) e;
                            resolveResult(rae, RC_CREDENTIALS_READ);
                        } else {
                            Log.w(TAG, "request: not handling exception", e);
                        }
                    }
                });
    }

    private void resolveResult(ResolvableApiException rae, int requestCode) {
        if (!mIsResolving) {
            try {
                rae.startResolutionForResult(SignInActivity.this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Failed to send Credentials intent.", e);
                mIsResolving = false;
            }
        }
    }

    private void saveCredential(Credential credential) {
        if (credential == null) {
            Log.w(TAG, "Ignoring null credential.");
            return;
        }

        mCredentialsClient.save(credential).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "save:SUCCESS");
                            return;
                        }

                        Exception e = task.getException();
                        if (e instanceof ResolvableApiException) {
                            // Saving the credential can sometimes require showing some UI
                            // to the user, which means we need to fire this resolution.
                            ResolvableApiException rae = (ResolvableApiException) e;
                            resolveResult(rae, RC_CREDENTIALS_SAVE);
                        } else {
                            Log.w(TAG, "save:FAILURE", e);
                            Toast.makeText(SignInActivity.this,
                                    "Unexpected error, see logs for detals",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onGoogleSignInClicked() {
        Intent intent = mSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void onGoogleRevokeClicked() {
        if (mCredential != null) {
            mCredentialsClient.delete(mCredential);
        }
        mSignInClient.revokeAccess().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        handleGoogleSignIn(null);
                    }
                });
    }

    private void onGoogleSignOutClicked() {
        mCredentialsClient.disableAutoSignIn();
        mSignInClient.signOut().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        handleGoogleSignIn(null);
                    }
                });
    }

    private void onEmailSignInClicked() {
        requestCredentials(true, true);
    }

    private void onEmailSaveClicked() {
        String email = ((EditText) findViewById(R.id.edit_text_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.edit_text_password)).getText().toString();

        if (email.length() == 0|| password.length() == 0) {
            Log.w(TAG, "Blank email or password, can't save Credential.");
            Toast.makeText(this, "Email/Password must not be blank.", Toast.LENGTH_SHORT).show();
            return;
        }

        Credential credential = new Credential.Builder(email)
                .setPassword(password)
                .build();

        saveCredential(credential);
    }

    @Override
    public void onClick(View v) {
       if(v.getId() == R.id.button_google_sign_in) {
               onGoogleSignInClicked();
        }else if(v.getId() == R.id.button_google_revoke) {
               onGoogleRevokeClicked();
        }else if(v.getId() == R.id.button_google_sign_out) {
               onGoogleSignOutClicked();
        }else if(v.getId() == R.id.button_email_sign_in) {
               onEmailSignInClicked();
        }else if(v.getId() == R.id.button_email_save) {
               onEmailSaveClicked();
        }
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    private void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
