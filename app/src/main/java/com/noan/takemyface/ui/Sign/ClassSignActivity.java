package com.noan.takemyface.ui.Sign;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import com.noan.takemyface.R;
import com.noan.takemyface.data.IfaceTool;
import com.noan.takemyface.data.Result;
import com.noan.takemyface.databinding.ActivityClassSignBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ClassSignActivity extends AppCompatActivity {
    private String sessionToken;
    private final MutableLiveData<Result<String>> location = new MutableLiveData<>();
    private final MutableLiveData<Result<IfaceTool.SignSuccessRes>> signResult = new MutableLiveData<>();
    private @NonNull ActivityClassSignBinding binding;
    private Button signBut;
    private Button checkLocationBut;
    private Button getFaceBut;
    private ImageView faceImage;
    private Bitmap faceScaled;
    TextView locationText;
    IfaceTool iface;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFaceWithPermission();
            }else {
                Toast.makeText(this, getString(R.string.getPermission), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static String bitmapToBase64(@NonNull Bitmap bitmap) {
        try {

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                byte[] bitmapBytes = outStream.toByteArray();
                String base64Str = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
                outStream.flush();
                outStream.close();
                return  base64Str;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    Log.w("pic", String.valueOf(data.getData()));
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    Matrix matrix = new Matrix();
                    float scale = (float) faceImage.getWidth() /bitmap.getWidth();
                    matrix.postScale(scale, scale);
                    faceScaled = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
                    faceImage.setImageBitmap(faceScaled);
                    signBut.setEnabled(true);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private void getFaceImg() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }else {
            getFaceWithPermission();
        }
    }

    private void getFaceWithPermission()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , "image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iface = new IfaceTool();
        binding = ActivityClassSignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionToken = getIntent().getStringExtra("token");
        iface.setToken(sessionToken);
        signBut = binding.signBut;
        getFaceBut = binding.getFaceImg;
        checkLocationBut = binding.checkLocationBut;
        locationText = binding.location;
        faceImage=binding.faceView;

        checkLocationBut.setOnClickListener(v -> {
            locationText.setText(getString(R.string.locationTextLoading));
            new Thread(() -> location.postValue(iface.getLocation())).start();
        });
        location.observe(this, newLocation -> {
            if (newLocation instanceof Result.Success) {

                locationText.setText(((Result.Success<String>) newLocation).getData());
                getFaceBut.setEnabled(true);
            } else {
                locationText.setText(newLocation.toString());
            }

        });
        signBut.setOnClickListener(v -> {
            String faceBase64 = bitmapToBase64(faceScaled);
            new Thread(() -> signResult.postValue(iface.signWithFace(faceBase64))).start();
        });

        getFaceBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFaceImg();
            }
        });



        signResult.observe(this,signRes -> {
            if (signRes instanceof Result.Success) {
                IfaceTool.SignSuccessRes resData=((Result.Success<IfaceTool.SignSuccessRes>) signRes).getData();
                String message= String.format("姓名：%s\n位置：%s", resData.getSignName(),resData.getLocation());
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("打卡成功!")
                        .setMessage(message)
                        .create()
                        .show();

            }
            else{
                Toast.makeText(
                        this.getApplicationContext(),
                        signRes.toString(),
                        Toast.LENGTH_LONG).show();
                Log.w("sign",signRes.toString());
            }

        });

    }

}