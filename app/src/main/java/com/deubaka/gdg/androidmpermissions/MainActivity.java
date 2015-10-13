package com.deubaka.gdg.androidmpermissions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_SMS_PERMISSION = 7;

    private View mRootView;
    private TextInputLayout mRecipientWrapper;
    private TextInputLayout mMessageWrapper;
    private EditText mRecipientEditText;
    private EditText mMessageEditText;
    private Button mSendButton;
    private Button mDelegateSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        mRootView = findViewById(R.id.root_view);

        mRecipientEditText = (EditText) findViewById(R.id.et_recipient);
        mMessageEditText = (EditText) findViewById(R.id.et_message);
        mSendButton = (Button) findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(this);
        mDelegateSendButton = (Button) findViewById(R.id.btn_delegate_send);
        mDelegateSendButton.setOnClickListener(this);

        mRecipientWrapper = (TextInputLayout) findViewById(R.id.til_recipient_wrapper);
        mRecipientWrapper.setHint(getString(R.string.recipient_hint));

        mMessageWrapper = (TextInputLayout) findViewById(R.id.til_message_wrapper);
        mMessageWrapper.setHint(getString(R.string.your_message_hint));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    sendMessage();
                } else {
                    startRequestForPermission();
                }

                break;
            case R.id.btn_delegate_send:
                sendMessageViewIntent();

                break;

        }
    }

    private void sendMessage() {
        if (areFieldsValid()) {
            String recipientAddress = mRecipientEditText.getText().toString();
            String message = mMessageEditText.getText().toString();

            SmsManager.getDefault().sendTextMessage(recipientAddress, null, message, null, null);
        }
    }

    private void sendMessageViewIntent() {
        if (areFieldsValid()) {
            String recipientAddress = mRecipientEditText.getText().toString();
            String message = mMessageEditText.getText().toString();

            Uri uri = Uri.parse("smsto:" + recipientAddress);
            Intent sendSmsIntent = new Intent(Intent.ACTION_SENDTO, uri);
            sendSmsIntent.putExtra("sms_body", message);
            startActivity(sendSmsIntent);
        }
    }

    private boolean areFieldsValid() {
        String recipientAddress = mRecipientEditText.getText().toString();
        if (TextUtils.isEmpty(recipientAddress) || !android.util.Patterns.PHONE.matcher(recipientAddress).matches()) {
            mRecipientWrapper.setError(getString(R.string.invalid_recipient_error));
            return false;
        }

        String message = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(message)) {
            mRecipientWrapper.setError(getString(R.string.message_empty_error));
            return false;
        }

        return true;
    }

    private void startRequestForPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            Snackbar.make(mRootView, R.string.sms_permission_needed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermission();
                        }
                    })
                    .show();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.SEND_SMS }, REQUEST_CODE_SMS_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_SMS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_granted_sending, Toast.LENGTH_SHORT).show();
                    sendMessage();
                } else {
                    Snackbar.make(mRootView, R.string.unable_to_send, Snackbar.LENGTH_SHORT).show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
