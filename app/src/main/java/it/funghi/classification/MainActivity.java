package it.funghi.classification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.funghi.classification.classificatore.ImageClassifier;

/**
 * The Main Activity Class
 * <p>
 * Created by A Anand on 11-05-2020
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Requests Codes to identify camera and permission requests
     */
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQEUST_CODE = 10001;
    private static final int RESULT_LOAD_IMAGE = 1;

    /**
     * UI Elements
     */
    private ImageView imageView;
    private ListView listView;
    private ImageClassifier imageClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initalizing ui elements
        initializeUIElements();

        Button buttonLoadImage =  findViewById(R.id.buttonLoadPicture);

        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }

    /**
     * Method to initalize UI Elements. this method adds the on click
     */
    private void initializeUIElements() {
        imageView = findViewById(R.id.iv_capture);
        listView = findViewById(R.id.lv_probabilities);
        Button takepicture = findViewById(R.id.bt_take_picture);

        /*
         * Creating an instance of our tensor image classifier
         */
        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e("Image Classifier Error", "ERROR: " + e);
        }

        // adding on click listener to button
        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking whether camera permissions are available.
                // if permission is avaialble then we open camera intent to get picture
                // otherwise reqeusts for permissions
                if (hasPermission()) {
                    openCamera();
                } else {
                    requestPermission();
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // if this is the result of our camera image request
        if (requestCode == CAMERA_REQEUST_CODE) {
            // getting bitmap of the image
            Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            // displaying this bitmap in imageview
            imageView.setImageBitmap(photo);

            // pass this bitmap to classifier to make prediction
            List<ImageClassifier.Recognition> predicitons = imageClassifier.recognizeImage(
                    photo, 0);

            // creating a list of string to display in list view
            final List<String> predicitonsList = new ArrayList<>();
            for (ImageClassifier.Recognition recog : predicitons) {
                /*String[] recog_split = recog.getName().split("_");
                String part1 = recog_split[0];
                String part2 = recog_split[1];*/

                String url = null;



                predicitonsList.add("+-----------------------------------------------+" );
                predicitonsList.add("|        MUSHROOM DETECTION        |");
                predicitonsList.add("+-----------------------------------------------+");


                predicitonsList.add("Probable Mushroom family : " + recog.getName());

                predicitonsList.add("Please visit the link below !");


                url = "https://en.wikipedia.org/wiki/" + recog.getName();



                TextView t2 = (TextView) findViewById(R.id.textView3);


                t2.setText(Html.fromHtml(url));
                t2.setMovementMethod(LinkMovementMethod.getInstance());
                t2.setHighlightColor(android.R.color.holo_red_dark);
                

            }

            // creating an array adapter to display the classification result in list view
            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    this, R.layout.support_simple_spinner_dropdown_item, predicitonsList);
            listView.setAdapter(predictionsAdapter);

        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            // displaying this bitmap in imageview

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                // displaying this bitmap in imageview
                imageView.setImageBitmap(image);
                // pass this bitmap to classifier to make prediction
                List<ImageClassifier.Recognition> predicitons = imageClassifier.recognizeImage(
                  image    , 0);

                // creating a list of string to display in list view
                final List<String> predicitonsList = new ArrayList<>();
                for (ImageClassifier.Recognition recog : predicitons) {
                   /* String[] recog_split = recog.getName().split("_");
                    String part1 = recog_split[0];
                    String part2 = recog_split[1];*/

                    String url = null;


                    predicitonsList.add("+-----------------------------------------------+" );
                    predicitonsList.add("|        MUSHROOM DETECTION        |");
                    predicitonsList.add("+-----------------------------------------------+");


                    predicitonsList.add("Probable Mushroom family : " + recog.getName());

                    predicitonsList.add("Please visit the link below !");


                    url = "https://en.wikipedia.org/wiki/" + recog.getName();



                    TextView t2 = (TextView) findViewById(R.id.textView3);


                    t2.setText(Html.fromHtml(url));
                    t2.setMovementMethod(LinkMovementMethod.getInstance());
                    t2.setHighlightColor(android.R.color.holo_red_dark);




                }

                // creating an array adapter to display the classification result in list view
                ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                        this, R.layout.support_simple_spinner_dropdown_item, predicitonsList);
                listView.setAdapter(predictionsAdapter);



            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if this is the result of our camera permission request
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (hasAllPermissions(grantResults)) {
                openCamera();
            } else {
                requestPermission();
            }
        }
    }

    /**
     * checks whether all the needed permissions have been granted or not
     *
     * @param grantResults the permission grant results
     * @return true if all the reqested permission has been granted,
     * otherwise returns false
     */
    private boolean hasAllPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    /**
     * Method requests for permission if the android version is marshmallow or above
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // whether permission can be requested or on not
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
            // request the camera permission permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * creates and starts an intent to get a picture from camera
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQEUST_CODE);
    }

    /**
     * checks whether camera permission is available or not
     *
     * @return true if android version is less than marshmallo,
     * otherwise returns whether camera permission has been granted or not
     */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
