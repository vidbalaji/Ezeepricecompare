package com.vidhyalearning.ezeepricecompare;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainScreen extends AppCompatActivity  {
    private static final String LOG_TAG = "Text API";
    private static final int PHOTO_REQUEST = 10;
    private EditText scanResults;
    TextView scannedbarcode;
    private Uri imageUri;
    Button barcodebutton,ocrbutton;
    String mProductTitle;
    private TextRecognizer detector;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final int  REQUEST_WRITE_PERMISSION_FORBARCODE=30;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        scanResults = (EditText)findViewById(R.id.editTextSearch);
        barcodebutton = (Button)findViewById(R.id.barcodebutton);
        ocrbutton = (Button)findViewById(R.id.ocrbutton);
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (!(result == PackageManager.PERMISSION_GRANTED)) {
            ocrbutton.setVisibility(View.INVISIBLE);

        }
        else
        {
            ocrbutton.setVisibility(View.VISIBLE);

        }

        detector = new TextRecognizer.Builder(getApplicationContext()).build();
        scannedbarcode = (TextView)findViewById(R.id.scannedbarcode);
        scannedbarcode.setText("");
    }

    public void scanQRcode(View view) {

        ActivityCompat.requestPermissions(MainScreen.this, new
                String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION_FORBARCODE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    takePicture();
                } else {
                    Toast.makeText(MainScreen.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_PERMISSION_FORBARCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ocrbutton.setVisibility(View.VISIBLE);
                    takeBarcodePicture();

                } else {
                    Toast.makeText(MainScreen.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void takeBarcodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        // integrator.setDesiredBarcodeFormats(IntentIntegrator.
        // );
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
        integrator.setResultDisplayDuration(0);
        integrator.setWide();  // Wide scanning rectangle, may work better for 1D barcodes
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {
                scanResults.setText("");
                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> textBlocks = detector.detect(frame);
                    String blocks = "";
                    String lines = "";
                    String words = "";
                    for (int index = 0; index < textBlocks.size(); index++) {
                        //extract scanned text blocks here
                        TextBlock tBlock = textBlocks.valueAt(index);
                        blocks = blocks + tBlock.getValue() + "\n" + "\n";
                        for (Text line : tBlock.getComponents()) {
                            //extract scanned text lines here
                            lines = lines + line.getValue() + "\n";
                            for (Text element : line.getComponents()) {
                                //extract scanned text words here
                                words = words + element.getValue() + " ";
                            }
                        }
                    }
                    if (textBlocks.size() == 0) {
                        scanResults.setText("Scan Failed: Found nothing to scan");
                    } else {

                        scanResults.setText(scanResults.getText() + words);

                    }
                } else {
                    scanResults.setText("Could not set up the detector!");
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(LOG_TAG, e.toString());
            }
        }
        else if(requestCode==49374){
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scanningResult != null) {
                //we have a result
                String scanContent = scanningResult.getContents();
                if(scanningResult.getContents() == null){
                    Toast.makeText(this, "No Barcode scanned", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                String scanFormat = scanningResult.getFormatName();

                scannedbarcode.setText( scanContent);
                AmazonFragment fragment = AmazonFragment.newInstance("",scanContent);
                mProductTitle="";
                mProductTitle=fragment.searchFn(scanContent,true);
                scanResults.setText(mProductTitle);
                if(mProductTitle ==null ){
                    Toast toast = Toast.makeText(getApplicationContext(),"Could not fetch the Product for this scan", Toast.LENGTH_SHORT);
                    toast.show();
                }



            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");


        imageUri = FileProvider.getUriForFile(MainScreen.this,
                BuildConfig.APPLICATION_ID + ".provider",
                photo);


        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    public void scanOCRcode(View view) {

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if(isSDPresent)
        {
            // yes SD-card is present
            ActivityCompat.requestPermissions(MainScreen.this, new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
        else
        {
          Toast.makeText(this,"SD card not present.Could not capture Image ",Toast.LENGTH_LONG).show();
        }
    }



    public void searchFunction(View view) {
        String productTitle = scanResults.getText().toString();
        if(productTitle.isEmpty()){
            Toast.makeText(this,"Please enter a product to search",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        Toast.makeText(this,"Searching may take a while.Please Wait",Toast.LENGTH_LONG).show();
        intent.setClass(this,MainActivity.class);
        intent.putExtra("SearchItem",productTitle);
        startActivity(intent);

    }

    public void compareProduct(View view) {
        String productTitle = scanResults.getText().toString();
        if(productTitle.isEmpty()){
            Toast.makeText(this,"Please enter a product to search",Toast.LENGTH_SHORT).show();
            return;
        }

       Intent intent = new Intent();
        intent.setClass(this,Main2Activity.class);
        intent.putExtra("SearchProductTitle",productTitle);
        Toast.makeText(this,"Searching may take a while.Please Wait",Toast.LENGTH_LONG).show();
        startActivity(intent);

    }
}
