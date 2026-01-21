package com.thabo.Firechat;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Scanner;



public class FirebaseAuthClient {
    public static final String API_KEY=" ";//cleaned api key

    public static String signUp(String email, String password)throws Exception{

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
        String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",email,password);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/jason");
        connection.setDoOutput(true);

        try(OutputStream outputStream = connection.getOutputStream()){
            outputStream.write(payload.getBytes());
        }

        Scanner scanner = new Scanner(connection.getInputStream());
        String response = scanner.useDelimiter("\\A").next();

        return response;//
    }
    public static String signIn(String email,String password)throws Exception{
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

        String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",email, password);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/jason");
        connection.setDoOutput(true);

        try(OutputStream outputStream =  connection.getOutputStream())//try with resources block
        {
            outputStream.write(payload.getBytes()); //writing to connection stream
        }


        Scanner scanner = new Scanner(connection.getInputStream());//getting back the response
        return scanner.useDelimiter("\\A").next(); //response
        //return response;
    }
}

