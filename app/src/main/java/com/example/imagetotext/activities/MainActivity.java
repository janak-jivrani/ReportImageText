package com.example.imagetotext.activities;

import static com.example.imagetotext.core.MyConstants.topSorting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagetotext.R;
import com.example.imagetotext.core.MyConstants;
import com.example.imagetotext.core.MyHelper;
import com.example.imagetotext.core.TextOCR;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    MaterialButton inputImageBtn, getTextBtn;
    ShapeableImageView imageView;
    EditText imageText;
    ImageView menuBtn;
    Uri imageUri = null;
    static final int CAMERA_REQUEST_CODE = 100;
    static final int STORAGE_REQUEST_CODE = 101;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    ProgressDialog progressDialog;

    TextRecognizer textRecognizer;

    //Speaker Activity
    private TextToSpeech mTTS;
    private Button mButtonSpeak;
    ArrayList<TextOCR> textOCRS = new ArrayList<>();
    int columnCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonSpeak = findViewById(R.id.button_speak);
        menuBtn = findViewById(R.id.menuBtn);
        inputImageBtn = findViewById(R.id.inputImageBtn);
        getTextBtn = findViewById(R.id.getTextBtn);
        imageView = findViewById(R.id.imageView);
        imageText = findViewById(R.id.imageText);
        textOCRS.clear();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Build.VERSION.SDK_INT > Build.VERSION_CODES.Q ? Manifest.permission.READ_EXTERNAL_STORAGE : Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Build.VERSION.SDK_INT > Build.VERSION_CODES.Q ? Manifest.permission.READ_EXTERNAL_STORAGE : Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        /*menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });*/

        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialog();
            }
        });
        getTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(MainActivity.this, "Pick image first", Toast.LENGTH_SHORT).show();
                } else {
                    if (textOCRS.isEmpty()) {
                        getImageFromText();
                    } else {
                        openReceiptScreen();
                    }
                }
            }
        });

        //Speaker Code
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageText == null) {
                    Toast.makeText(MainActivity.this,"Pls enter text!",Toast.LENGTH_LONG).show();
                }else {
                    speak();
                }
            }
        });
    }

    private void speak() {
        String text = imageText.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }

    private void getImageFromText() {
        progressDialog.setMessage("Getting image.....");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            progressDialog.setMessage("Getting text....");
            Task<Text> textTaskResult = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    progressDialog.dismiss();
                    String recognizedText=text.getText();
                    imageText.setText(recognizedText);

                    //String resultText = result.getText();
                    textOCRS.clear();
                    columnCount = 1;
                    //ArrayList<TextOCRGroup> textOCRGroup = new ArrayList<>();
                    int minLeft = 0, maxRight = 0;
                    for (Text.TextBlock block : text.getTextBlocks()) {
                        //String blockText = block.getText();
                        //Point[] blockCornerPoints = block.getCornerPoints();
                        //Rect blockFrame = block.getBoundingBox();
                        for (Text.Line line : block.getLines()) {
                            String lineText = line.getText();
                            //Point[] lineCornerPoints = line.getCornerPoints();
                            //Rect lineFrame = line.getBoundingBox();
                            TextOCR textOCR = new TextOCR(lineText);
                            int topMin = 0, topMax = 0, left = 0, right = 0;
                            for (Text.Element element : line.getElements()) {
                                //Point[] elementCornerPoints = element.getCornerPoints();
                                Rect elementFrame = element.getBoundingBox();
                                if (elementFrame != null) {
                                    if (topMin == 0 || topMin > elementFrame.top)
                                        topMin = elementFrame.top;
                                    if (topMax == 0 || topMax < elementFrame.top)
                                        topMax = elementFrame.top;
                                    if (left == 0 || left > elementFrame.left)
                                        left = elementFrame.left;
                                    if (right == 0 || right < elementFrame.right)
                                        right = elementFrame.right;
                                }
                            }
                            //Log.v(TAG, lineText + " topmin " + topMin + " topmax " + topMax + " left " + left + " right " + right);
                            textOCR.setParams(topMin, topMax, left, right);
                            textOCRS.add(textOCR);
                            if (minLeft == 0 || minLeft > left)
                                minLeft = left;
                            if (maxRight < right)
                                maxRight = right;
                        }
                    }
                    Collections.sort(textOCRS, topSorting);
                    //Log.v(TAG,"maxRight "+maxRight);
                    int minDiffBetween2LineTops = 1000, lastTop = 0, joinyLeftMin = 0, joinyLeftMax = 0, joinyRightMin = 0, joinyRightMax = 0;
                    for (int j = 0; j < textOCRS.size(); j++) {
                        boolean isSecondaryContent = false;
                        int topMin = textOCRS.get(j).topMin;
                        int topMax = textOCRS.get(j).topMax;
                        int left = textOCRS.get(j).left;
                        int right = textOCRS.get(j).right;
                        //String lineText = textOCRS.get(j).line;
                        for (int i = 0; i < textOCRS.size(); i++) {
                            if (i != j) {//should not compare to self
                                int tMin = textOCRS.get(i).topMin;
                                int tMax = textOCRS.get(i).topMax;
                                int joinyCount = textOCRS.get(i).joinyCount;
                                int tRight = textOCRS.get(i).right;
                                String groupId = textOCRS.get(i).groupId;
                                if (left > tRight) {
                                    //Log.v(TAG,"loop 2 "+left+":"+right+" ANND "+tMin+":"+topMin+" AND "+tMax+":"+topMax);
                                    if (tMin == topMin || tMax == topMax) {
                                        //Log.v(TAG,"inner");
                                        if (groupId.trim().length() == 0) {
                                            groupId = MyHelper.getRandomString(6);
                                            textOCRS.get(i).setGroupId(groupId);
                                        }
                                        //Log.v(TAG, right+" joining " + i + " " + groupId + " " + textOCRS.get(i).line);
                                        textOCRS.get(j).joinGroup(groupId);
                                        if (joinyCount == 0) {
                                            textOCRS.get(i).incrementJoinyCount(left, right);
                                        } else {
                                            int tLeft = textOCRS.get(i).left;
                                            int leftDiff = (tLeft > left) ? tLeft - left : left - tLeft;
                                            int rightDiff = (tRight > right) ? tRight - right : right - tRight;
                                            if (leftDiff > 20 && rightDiff > 20)
                                                textOCRS.get(i).incrementJoinyCount();
                                        }
                                        if (joinyLeftMin == 0)
                                            joinyLeftMin = left;
                                        else if (left < joinyLeftMin)
                                            joinyLeftMin = left;
                                        if (joinyLeftMax == 0)
                                            joinyLeftMax = left;
                                        else if (left > joinyLeftMax)
                                            joinyLeftMax = left;
                                        if (joinyRightMin == 0)
                                            joinyRightMin = right;
                                        else if (right < joinyRightMin)
                                            joinyRightMin = right;
                                        if (joinyRightMax == 0)
                                            joinyRightMax = right;
                                        else if (right > joinyRightMax)
                                            joinyRightMax = right;
                                        isSecondaryContent = true;
                                        break;
                                    } else {
                                        int top1Diff = (tMin > topMin) ? tMin - topMin : topMin - tMin;
                                        int top2Diff = (tMax > topMax) ? tMax - topMax : topMax - tMax;
                                        if (top1Diff < 20 || top2Diff < 20) {
                                            if (groupId.trim().length() == 0) {
                                                groupId = MyHelper.getRandomString(6);
                                                textOCRS.get(i).setGroupId(groupId);
                                            }
                                            //Log.v(TAG, textOCRS.get(j).line + " joining " + i + " " + groupId + " " + textOCRS.get(i).line);
                                            textOCRS.get(j).joinGroup(groupId);
                                            textOCRS.get(i).incrementJoinyCount();
                                            if (joinyLeftMin == 0)
                                                joinyLeftMin = left;
                                            else if (left < joinyLeftMin)
                                                joinyLeftMin = left;
                                            if (joinyLeftMax == 0)
                                                joinyLeftMax = left;
                                            else if (left > joinyLeftMax)
                                                joinyLeftMax = left;
                                            if (joinyRightMin == 0)
                                                joinyRightMin = right;
                                            else if (right < joinyRightMin)
                                                joinyRightMin = right;
                                            if (joinyRightMax == 0)
                                                joinyRightMax = right;
                                            else if (right > joinyRightMax)
                                                joinyRightMax = right;
                                            isSecondaryContent = true;
                                            break;
                                        }
                                    }
                                }

                                if (textOCRS.get(i).joinyCount+1 > columnCount) {
                                    columnCount = textOCRS.get(i).joinyCount + 1;
                                }
                            }
                        }
                        if (!isSecondaryContent) {
                            int align = MyConstants.AlignLeft;
                            int rDiff = maxRight - right;
                            int lDiff = left - minLeft;
                            int diff = 0;
                            diff = (lDiff > rDiff) ? (lDiff - rDiff) : (rDiff - lDiff);
                            int minLDifference = (int) (maxRight * 0.0714);
                            if (diff < 80 && lDiff > minLDifference)
                                align = MyConstants.AlignCenter;
                            if (align == MyConstants.AlignLeft) {
                                if (lDiff > 50 && rDiff < 30)
                                    align = MyConstants.AlignRight;
                            }
                            //Log.v(TAG, textOCRS.get(j).line + " ALGN " + left + " " + right + " " + align + " " + minLeft + " " + maxRight + " " + diff + " " + lDiff + " " + rDiff);
                            if (align != MyConstants.AlignLeft)
                                textOCRS.get(j).setAlign(align);
                            int top = (textOCRS.get(j).topMin + textOCRS.get(j).topMax) / 2;

                            //Log.v(TAG,textOCRS.get(j).line+" "+textOCRS.get(j).topMin+" "+textOCRS.get(j).topMax+" "+top+" "+lastTop);
                            if (lastTop == 0) {
                                lastTop = top;
                            } else {
                                int topDiff = top - lastTop;
                                if (topDiff < minDiffBetween2LineTops)
                                    minDiffBetween2LineTops = topDiff;
                                textOCRS.get(j).setLastTopDifference(topDiff);
                                lastTop = top;
                            }
                        }
                    }
                    int joinyAlign = -1;
                    if (joinyLeftMin > 0 && joinyLeftMax > 0 && joinyRightMin > 0 && joinyRightMax > 0) {
                        int leftDiff = joinyLeftMax - joinyLeftMin;
                        int rightDiff = joinyRightMax - joinyRightMin;
                        if (rightDiff <= leftDiff && (maxRight - joinyRightMax) < 50)
                            joinyAlign = MyConstants.AlignRight;
                        else if (leftDiff < rightDiff && (joinyLeftMin - minLeft) > 50)
                            joinyAlign = MyConstants.AlignLeft;
                        //Log.v(TAG,joinyAlign+" "+joinyLeftMin+":"+joinyLeftMax+" AND "+joinyRightMin+":"+joinyRightMax+" AAND "+leftDiff+":"+rightDiff);
                    }

                    //TODO: open new activity
                    Log.d("DetectedText","OcrTextSize="+textOCRS.size());
                    openReceiptScreen();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReceiptScreen() {
        Intent intent = new Intent(MainActivity.this,ReceiptActivity.class);
        intent.putExtra("columCount",columnCount);
        intent.putExtra("textOcrList",textOCRS);
        startActivity(intent);
    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                textOCRS.clear();
                if (id == 1) {
                    if (checkCameraPermissions()) {
                        pickImageCamera();
                    } else {
                        requestCameraPermissions();
                    }
                } else if (id == 2) {
                    if (checkStoragePermissions()) {
                        pickImageGallery();
                    } else {
                        requestStoragePermission();
                    }
                }
                return true;
            }
        });
    }

    public void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    public ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    });

    public void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    public ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imageView.setImageURI(imageUri);
                    } else {
                        Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    public void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);

    }

    public boolean checkCameraPermissions() {
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return cameraResult && storageResult;
    }

    public void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickImageGallery();
                    } else {
                        Toast.makeText(this, "Storage Permissions Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this, menuBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Translate");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Speaker");
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "NotePad");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 1) {
                    Intent intent = new Intent(MainActivity.this, Translate.class);
                    startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(MainActivity.this, Speaker.class);
                    startActivity(intent);
                } else if (id == 3) {
                    Intent intent = new Intent(MainActivity.this, NotePad.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }



}