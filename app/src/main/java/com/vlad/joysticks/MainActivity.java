package com.vlad.joysticks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.vlad.joysticks.NetWork.Client;
import com.vlad.joysticks.listeners.JoystickListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainActivity extends AppCompatActivity {

    private JoyStickHandler rightJoystick;
    private JoyStickHandler leftJoystick;
    private Button getVersionsButton;
    private String response;
    private short yaw;
    private short pitch;
    private short throttle;
    private short roll;
    private ImageView propellerImageView;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.INTERNET,
    };
    private List<String> missingPermission = new ArrayList<>();
    private static final int REQUEST_PERMISSION_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();
        rightJoystick = findViewById(R.id.right_stick);
        leftJoystick = findViewById(R.id.left_stick);
        propellerImageView = findViewById(R.id.propellerImageView);
        getVersionsButton = findViewById(R.id.getVersionsButton);

        runOnUiThread(()-> propellerImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.propeller)));

        getVersionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Callable task = Client::getVersions;
                FutureTask<String> future = new FutureTask<>(task);
                new Thread(future).start();
                try {
                    Toast.makeText(getApplicationContext(), future.get(), Toast.LENGTH_SHORT).show();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        rightJoystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onTouch(JoyStickHandler joystick, short pX, short pY) {
                roll = pX;
                pitch = pY;
                System.out.println(roll);
                System.out.println(pitch);
            }
        });

        leftJoystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onTouch(JoyStickHandler joystick, short pX, short pY) {
                yaw = pX;
                throttle = pY;
            }
        });

//        [255,yaw,throttle,roll,pitch, mode]
        Thread t1;
        t1 = new Thread(() -> {
            boolean b = true;
            Client clientSender;
            while(b) {
                try {
                    clientSender = Client.getInstance("10.0.2.2", 8888);
                    byte[] data = {(byte) 255, (byte) yaw, (byte) throttle, (byte) roll, (byte) pitch, (byte) 2};
                    clientSender.sendData(data);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                b = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                b= true;
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b = true;
                Client clientSender;
                ByteArrayInputStream byteArrayInputStream;
                while(b) {
                    System.out.println("IM HERE");
                    try {
                        clientSender = Client.getInstance("10.0.2.2", 8888);
//                        System.out.println("IM HERE");
                        clientSender.receiveImage();
//                        System.out.println("IM HERE 1 ");
                        byteArrayInputStream = clientSender.getBais();
                        System.out.println("IM HERE 2 ");
                        ByteArrayInputStream finalByteArrayInputStream = byteArrayInputStream;
                        runOnUiThread(()-> propellerImageView.setImageBitmap(BitmapFactory.decodeStream(finalByteArrayInputStream)));
                        byteArrayInputStream.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    b = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    b= true;
                }
            }
        });
        t2.start();




    }

    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            //
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(MainActivity.this, "Need to grant the permissions!", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
        } else {
            Toast.makeText(MainActivity.this, "Need to grant the permissions!", Toast.LENGTH_SHORT).show();

        }
    }

}