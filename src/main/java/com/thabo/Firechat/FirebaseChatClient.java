package com.thabo.Firechat;

import java.util.Scanner;
import com.thabo.Firechat.FirebaseAuthClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.thabo.Firechat.Listener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;

public class FirebaseChatClient {
    public static final String DATABASE_URL = "";

    public static void sendMessages(String userId,String text,String idToken)throws Exception{
        String url = DATABASE_URL + "/messages.json?auth=" + idToken;
        String payload = String.format("{\"user\":\"%s\",\"text\":\"%s\",\"timestamp\":%d}",userId,text,System.currentTimeMillis());

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try(OutputStream out = connection.getOutputStream())//try  with resources
        {
            out.write(payload.getBytes());
            System.out.println("well done messsage sent");//message sent to Firebase
        }
        try(InputStream in = connection.getInputStream();Scanner scannerIn = new Scanner(in)){
            String reply = scannerIn.useDelimiter("\\A").next();
            System.out.println(reply);
        }

    }

    public FirebaseChatClient (){//throws Exception
        int trials = 3;
        //while(!(trials==0))//3 login/signup trials
        {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Hi Welcome to \uD83D\uDD25 Firechat \uD83D\uDD25");
            System.out.println("1.Signing in\n2.Signing up");
            String option = scanner.nextLine();
            System.out.println("Please provide your email");
            String email = scanner.nextLine();
            System.out.println("And now your password");
            String password =  scanner.nextLine();
            if(option.equals("1")){
                try {
                    System.out.println(FirebaseAuthClient.signIn(email, password));
                    String response = FirebaseAuthClient.signIn(email, password);
                    System.out.println(response);//tracing
                    String idToken ,refreshToken,localId;

                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    if(json.has("idToken") &&(json.has("refreshToken"))){
                        idToken = json.get("idToken").getAsString();
                        refreshToken = json.get("refreshToken").getAsString();
                        localId = json.get("localId").getAsString();
                        System.out.println("Successfully signed in!\n \uD83D\uDD25 feel the burn! \uD83D\uDD25");

                        Listener listener = new Listener(idToken,DATABASE_URL); //spawn new listening thread per client

                        String clientInput = scanner.nextLine(); //send messages while in main thread
                        while(!(clientInput.equals("exit"))){
                            sendMessages(localId,clientInput,idToken);
                            clientInput = scanner.nextLine();
                        }

                    }else{
                        System.out.println("Could not sign you in");
                        System.out.println(response);
                    }
                }
                catch(Exception e){
                    System.out.println("yy"+e);
                }

                //spawn new listening thread
                //while writing constantly maybe in a loop
            }
            else if(option.equals("2")) {
                try {
                    String response = FirebaseAuthClient.signUp(email, password);
                    String idToken ,refreshToken,localId;

                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    if((json.has("idToken")) &&(json.has("refreshToken"))){
                        idToken = json.get("idToken").getAsString();
                        refreshToken = json.get("refreshToken").getAsString();
                        localId = json.get("loacalId").getAsString();
                        System.out.println("You Are In!");
                    }else{
                        System.out.println("We could not sign you up");
                    }
                }catch(Exception e){
                    System.out.println(e);
                }
            }
            else{
                System.out.println("Please enter just 1 or just 2 as your option");
                System.exit(0);
                trials=trials-1;
                //welcome();
            }
        }
        System.out.println("Please try again later\nBYE!");
        System.exit(0);

    }

    public static void main(String[] args){FirebaseChatClient client = new FirebaseChatClient();}

    class Listener extends Thread {
        private String databaseURL;
        private String idToken;

        public Listener(String databaseURL, String idToken){
            this.databaseURL =databaseURL;
            this.idToken= idToken;
        }
        @Override
        public void run(){
            streamMessages(databaseURL,idToken);
        }

        public static void streamMessages(String databaseURL,String idToken){
            OkHttpClient client = new OkHttpClient();
            String url = databaseURL + "/messages.json?auth=" + idToken;

            Request request = new Request.Builder().url(url).build();

            EventSourceListener listener = new EventSourceListener(){
                @Override
                public void onOpen(EventSource eventSource, Response response){
                    System.out.println("Stream opened...");
                }

                //@Override
                public void onEvent(EventSource eventSource, String id, String type,String data){
                    System.out.println("New event: "+ data);
                }

                @Override
                public void onClosed(EventSource eventSource){
                    System.out.println("Stream closed.");
                }

                @Override
                public void onFailure(EventSource eventSource,Throwable t, Response response){
                    System.err.println("Stream error: "+ t.getMessage());
                }
            };

            EventSources.createFactory(client).newEventSource(request,listener);


        }
    }

}

