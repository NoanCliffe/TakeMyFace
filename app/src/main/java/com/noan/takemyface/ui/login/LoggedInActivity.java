package com.noan.takemyface.ui.login;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.noan.takemyface.R;
import com.noan.takemyface.databinding.ActivityLoggedInBinding;


public class LoggedInActivity extends AppCompatActivity {

    private ActivityLoggedInBinding binding;
    private String sessionToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoggedInBinding.inflate(getLayoutInflater());
        sessionToken=getIntent().getStringExtra("token");
        setContentView(binding.getRoot());
        final Button copyButton = binding.copyBut;
        final TextView Token = binding.token;
        Token.setSingleLine(false);
        Token.setEllipsize(TextUtils.TruncateAt.END);

        Token.setText(sessionToken);


        copyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("Label",sessionToken));
                Toast.makeText(getApplicationContext(), getString(R.string.copied), Toast.LENGTH_LONG).show();
            }
        });
    }

}