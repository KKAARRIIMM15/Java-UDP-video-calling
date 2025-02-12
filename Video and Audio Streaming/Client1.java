package org.example.demo;


import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;


public class Client1 extends Application
{
    static Thread UDP_recv_Voice_Thread = null;
    static Thread UDP_send_Voice_Thread = null;
    static Thread send_Frame_Thread = null;

    static Thread recv_Frame_Thread = null;
    static WritableImage recv_Frame_img = null;

    public static void main(String[] args) {  System.out.println( System.getProperty("user.dir"));
        launch(args);
    }


    private WritableImage convertToJavaFXImage(BufferedImage bufferedImage) {
        if (bufferedImage == null)
            return null;

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);
                pixelWriter.setArgb(x, y, argb);
            }
        }

        return writableImage;
    }

    /*********************/

    static int[][] convert_To_Pixels( BufferedImage image )
    {
        try{
            // Get image dimensions
            int width = image.getWidth();
            int height = image.getHeight();

            // Create a 2D array to store the pixel values
            int[][] pixelArray = new int[width][height];

            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    int pixel = image.getRGB(x, y);
                    pixelArray[x][y] = pixel;
                }
            }
            return pixelArray;

        } catch ( Exception ex) {     }

        return null;
    }
    /***************/


    static PixelWriter Convert_To_Image( int[][] pixelArray )
    {
        try{
            int width = pixelArray.length;
            int height = pixelArray[0].length;

            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            // Set the pixel values in the BufferedImage
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    int pixelValue = pixelArray[x][y];
                    pixelWriter.setArgb(x, y, pixelValue);
                }
            }

            return pixelWriter;
        } catch( Exception ex ){ }

        return null;
    }

    /**********************/


    private static byte[] serialize(Serializable obj)
    {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /************************/

    private static Object deserialize(byte[] serializedBytes)
    {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return objectInputStream.readObject();

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        Webcam camera = Webcam.getDefault();
        camera.setViewSize( WebcamResolution.VGA.getSize() );
        camera.open();

        ImageView imageView = new ImageView( convertToJavaFXImage( camera.getImage() ) );  imageView.setFitHeight(500);  imageView.setPreserveRatio(true);

        StackPane root = new StackPane();   root.getChildren().add(imageView);
        Scene scene = new Scene(root , 700 , 600 );  scene.setFill(Color.web("#000036"));

        primaryStage.setTitle("client");
        primaryStage.setScene(scene);
        primaryStage.show();


        UDP_recv_Voice_Thread = new Thread( ()->
        {
            SourceDataLine speaker = null;
            try
            {
                AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, true);
                speaker = AudioSystem.getSourceDataLine(format);
                speaker.open(format);
                speaker.start();

                System.out.println(" Speaker is ON ");

                DatagramSocket UDP_Connection = new DatagramSocket( );
                InetAddress IP = InetAddress.getByName("197.165.163.3");
                int Port = 6666;

                byte[] sig = new byte[1];   sig[0] = 15;
                DatagramPacket sending_test_Packet = new DatagramPacket(sig, sig.length, IP, Port);
                UDP_Connection.send( sending_test_Packet);


                byte[] buffer = new byte[8192];
                DatagramPacket UDP_packet = new DatagramPacket(buffer, buffer.length);

                while (true)
                {
                    // Receive audio data from the client
                    UDP_Connection.receive(UDP_packet);
                    int bytesRead = UDP_packet.getLength();
                    speaker.write(buffer, 0, bytesRead);
                }

            } catch ( Exception ex ){ ex.printStackTrace(); System.out.println(" LOG:  Speaker is not recieving the correct Signal "); speaker.flush();  speaker.close(); }
        });



        UDP_send_Voice_Thread = new Thread( ()->
        {
            try
            {
                AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, true);

                // Get the microphone input
                TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
                microphone.open(format);
                microphone.start();

                DatagramSocket UDP_Connection = new DatagramSocket();
                InetAddress IP = InetAddress.getByName("197.165.163.3");
                int Port = 3333;

                byte[] buffer = new byte[8192];

                System.out.println(" Mic is ON ");
                System.out.println(" Streaming voice... \n ");

                while (true)
                {
                    // Capture audio from the microphone
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    // Send the captured audio to the server
                    DatagramPacket UDP_packet = new DatagramPacket(buffer, bytesRead, IP, Port);
                    UDP_Connection.send(UDP_packet);
                }
            } catch ( Exception ex ) { System.out.println(" LOG:  Mic is Closed ");}
        } );


        Thread UDPcameraThread = new Thread( ()->{
            try
            {
                DatagramSocket UDP_Connection = new DatagramSocket( );
                InetAddress IP = InetAddress.getByName("197.165.163.3");
                int Port = 4444;

                byte[] sig = new byte[1];   sig[0] = 11;
                DatagramPacket sending_test_Packet = new DatagramPacket(sig, sig.length, IP, Port);
                UDP_Connection.send( sending_test_Packet);

                byte[] carry = new byte[60000];
                DatagramPacket UDP_packet = new DatagramPacket(carry, carry.length);

            while (true)
            {
             try
                {
                    /** Recv Frame  **/

                UDP_Connection.receive(UDP_packet);
                int bytesRead = UDP_packet.getLength();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(carry, 0, bytesRead);   baos.flush();
                byte[] actualData = baos.toByteArray();
                System.out.println( actualData.length );

               try{ BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(actualData));
                      WritableImage fxImage = convertToJavaFXImage(bufferedImage);
                      imageView.setImage(fxImage); } catch (Exception ex ){ex.printStackTrace(); System.out.println(" \n LOG: Packet Loss");}


                    /** send Frame  **/
                 BufferedImage image = camera.getImage();

                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ImageIO.write(image, "jpg", byteArrayOutputStream);   byteArrayOutputStream.flush();
                 byte[] imageBytes = byteArrayOutputStream.toByteArray();
                DatagramPacket UDP_packet_snd_img = new DatagramPacket(imageBytes, imageBytes.length, IP, Port);
                UDP_Connection.send(UDP_packet_snd_img);

                } catch (Exception ex ){ System.out.println( " \n LOG : Poor Connection \n ");  }

            }

        } catch (IOException e) { System.out.println(" \n LOG :  Connection Can not be Enstablished \n ");  return; }

        } );


        Thread UDP_recv_image_Thread = new Thread( ()->{
            try
            {
                DatagramSocket UDP_Connection = new DatagramSocket( );
                InetAddress IP = InetAddress.getByName("197.165.163.3");
                int Port = 4444;

                byte[] sig = new byte[1];   sig[0] = 11;
                DatagramPacket sending_test_Packet = new DatagramPacket(sig, sig.length, IP, Port);
                UDP_Connection.send( sending_test_Packet);

                byte[] carry = new byte[60000];
                DatagramPacket UDP_packet = new DatagramPacket(carry, carry.length);

                while (true)
                {
                    try
                    {
                        /** Recv Frame  **/
                        UDP_Connection.receive(UDP_packet);
                        int bytesRead = UDP_packet.getLength();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        baos.write(carry, 0, bytesRead);   baos.flush();
                        byte[] actualData = baos.toByteArray();

                        try{ BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(actualData));
                            WritableImage fxImage = convertToJavaFXImage(bufferedImage);
                            imageView.setImage(fxImage); } catch (Exception ex ){ex.printStackTrace(); System.out.println(" \n LOG: Packet Loss");}

                    } catch (Exception ex ){ System.out.println( " \n LOG : Poor Connection \n ");  }

                }

            } catch (IOException e) { System.out.println(" \n LOG :  Connection Can not be Enstablished \n ");  return; }

        } );


        Thread UDP_send_image_Thread = new Thread( ()->{
            try
            {
                DatagramSocket UDP_Connection = new DatagramSocket( );
                InetAddress IP = InetAddress.getByName("197.165.163.3");
                int Port = 5555;
                while (true)
                {
                    try
                    {
                        /** send Frame  **/
                        BufferedImage image = camera.getImage();

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", byteArrayOutputStream);   byteArrayOutputStream.flush();
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        DatagramPacket UDP_packet_snd_img = new DatagramPacket(imageBytes, imageBytes.length, IP, Port);
                        UDP_Connection.send(UDP_packet_snd_img);

                    } catch (Exception ex ){ System.out.println( " \n LOG : Poor Connection \n ");  }

                }

            } catch (IOException e) { System.out.println(" \n LOG :  Connection Can not be Enstablished \n ");  return; }

        } );

        UDP_recv_image_Thread.start();
        UDP_send_image_Thread.start();
        UDP_recv_Voice_Thread.start();
        UDP_send_Voice_Thread.start();
    }


}