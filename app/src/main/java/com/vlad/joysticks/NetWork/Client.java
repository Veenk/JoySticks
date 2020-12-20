package com.vlad.joysticks.NetWork;

import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.RequiresApi;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Objects;


public class Client{
    private static Client instance;
    private DatagramSocket socket_sender;
    private DatagramSocket socket_receiver;
    private InetAddress address;
    private static OkHttpClient httpClient;
    private int PORT;
    private String HOST;
    private final Socket socket1;
    private ByteArrayInputStream bais;


    private Client(final String HOST, final int PORT) throws IOException {
        this.HOST = HOST;
        this.PORT = PORT;


        address = InetAddress.getByName(HOST);
        socket1 = new Socket(HOST, PORT);
        socket_sender = new DatagramSocket();
        socket_receiver = new DatagramSocket();

    }

    public static Client getInstance(String host, int port) throws IOException {
        if(instance == null){
            System.out.println("return new Client");
            instance = new Client(host,port);
            return instance;
        }
        else {
            System.out.println("port: " + instance.socket1.getPort());
            System.out.println("host: " + instance.socket1.getInetAddress().getHostName());
            System.out.println("is connected:  " + instance.socket1.isConnected());
            return instance;
        }

    }

    //    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendData(byte[] data){
        System.out.println("hi");

        DatagramPacket packet = new DatagramPacket(data, data.length, address, 8001);
        try {
            socket_sender.send(packet);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveImage();



    }

    public void receiveImage()
    {
        byte[] receiveData = new byte[256];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        System.out.println("receive image");
        try {

            System.out.println(socket_receiver.isConnected());
            System.out.println(socket_receiver.getPort());
//            System.out.println(socket_receiver.getInetAddress().getHostName());
            socket_receiver.receive(receivePacket);
            System.out.println(receivePacket.getAddress().getHostName());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = receivePacket.getData();
        System.out.println(receivePacket.getLength());
        bais = new ByteArrayInputStream(data);



    }

    public ByteArrayInputStream getBais(){
        return bais;
    }

    public static String getVersions(){
        httpClient = new OkHttpClient();
        String versions = "";

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8889/info")
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            if(response.isSuccessful()){
                versions = Objects.requireNonNull(response.body()).string();
                System.out.println(versions);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


        return versions;
    }






}