package com.noan.takemyface.ui.Sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.noan.takemyface.R;
import com.noan.takemyface.data.IfaceTool;
import com.noan.takemyface.data.Result;
import com.noan.takemyface.databinding.FragmentSignBinding;


/**
 * A placeholder fragment containing a simple view.
 */
public class ClassSignFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private final MutableLiveData<Result<String>> ifaceLoginResult = new MutableLiveData<>();
    IfaceTool ifaceLogin=new IfaceTool();

    private void ifaceLogin(String token)
    {
        new Thread(() -> {

            Result<String> result = ifaceLogin.login(token);
            ifaceLoginResult.postValue(result);
        }).start();
    }

    private FragmentSignBinding binding;

    public static ClassSignFragment newInstance(int index) {
        ClassSignFragment fragment = new ClassSignFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentSignBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText sessionToken=binding.sessionToken;
        Button checkToken = binding.checkSessionToken;
        ProgressBar loadingProgressBar=binding.progressBar;
        checkToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                ifaceLogin(sessionToken.getText().toString());
            }
        });
        ifaceLoginResult.observe(getViewLifecycleOwner(), new Observer<Result<String>>() {
            @Override
            public void onChanged(Result<String> ifaceResult) {
                if (ifaceResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (ifaceResult instanceof Result.Success) {
                    checkTokenSucceed(((Result.Success<String>) ifaceResult).getData());
                }
                else{
                    checkTokenFailed(ifaceResult.toString());
                }

            }

        });
    }

    private void checkTokenFailed(String errorMsg){
        Toast.makeText(
                getContext().getApplicationContext(),
                errorMsg,
                Toast.LENGTH_LONG).show();
    }

    private void checkTokenSucceed(String token){
        String welcome = getString(R.string.welcome);
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
        Intent singAct =new Intent();
        singAct.setClass(this.getActivity() , ClassSignActivity.class);
        singAct.putExtra("token",token);
        startActivity(singAct);
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}