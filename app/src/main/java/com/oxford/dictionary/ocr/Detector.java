package com.oxford.dictionary.ocr;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;



public class Detector extends AppCompatActivity implements View.OnClickListener {
    private ImageView img;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    StringBuilder strBuilder = new StringBuilder();
    ImageView imageview;
    Button btnProcess;
    TextView txtView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar,10);
        ViewCompat.setElevation((LinearLayout) findViewById(R.id.extension),10);
        createNetErrorDialog();


        fab = (FloatingActionButton) findViewById(R.id.nextStep);
        fab.setOnClickListener(this);


        // To get bitmap from resource folder of the application.
        img = (ImageView) findViewById(R.id.croppedImage);
        btnProcess = (Button) findViewById(R.id.btnProcess);
        txtView = (TextView) findViewById(R.id.textView);
        bitmap = CropAndRotate.croppedImage;
        img.setImageBitmap(bitmap);
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!txtRecognizer.isOperational()) {
                    txtView.setText(R.string.error_prompt);
                } else {
                    Frame frame =new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray items = txtRecognizer.detect(frame);
                    for (int i = 0; i < items.size(); i++)
                    {
                        TextBlock item = (TextBlock)items.valueAt(i);
                        strBuilder.append(item.getValue());
                    }
                    txtView.setText(strBuilder.toString());
                }
            }
        });
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Connectivity Prompt")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Proceed",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View view) {
        EditText etButton = (EditText) findViewById(R.id.editDetect);
        TextView txtView = (TextView) findViewById(R.id.textView);
        String textText = txtView.getText().toString();
        String etText = etButton.getText().toString();
        if((TextUtils.isEmpty(textText)) && (TextUtils.isEmpty(etText))) {
            etButton.setError("Ah! A Glitch I guess.");
            return;
        }
        else if((TextUtils.isEmpty(textText)) && (!(TextUtils.isEmpty(etText)))) {
            etButton.setError("Oops! Got It");
            Intent intent = new Intent(Detector.this, LemmatronActivity.class);
            intent.putExtra("Term",etText);
            startActivity(intent);
        }
        else if (!((TextUtils.isEmpty(textText))) && (!(TextUtils.isEmpty(etText)))) {
            etButton.setError("Oops! Got It");
            Intent intent = new Intent(Detector.this, LemmatronActivity.class);
            intent.putExtra("Term",etText);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(Detector.this, LemmatronActivity.class);
            intent.putExtra("Term",textText);
            startActivity(intent);
        }
    }
}
