package com.example.streamingapplication;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Consumer implements Parcelable {

    private Socket socket;
    private ServerSocket serverSocket;
    private Socket socketToReceive;
    public Address addr;
    private Value value;
    ArrayList<String> topics = new ArrayList();

    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.1.7", 6000)
    ));

    public Consumer(Address _addr){
        this.addr = _addr;
        init();
        pull();
    }

    protected Consumer(Parcel in) {
        topics = in.createStringArrayList();
    }

    public static final Creator<Consumer> CREATOR = new Creator<Consumer>() {
        @Override
        public Consumer createFromParcel(Parcel in) {
            return new Consumer(in);
        }

        @Override
        public Consumer[] newArray(int size) {
            return new Consumer[size];
        }
    };

    public void init() {
        Runnable task = () ->{
            try {
                System.out.println("\n Thread for init running...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());
                service_out.writeObject(new Value(this.addr, SenderType.CONSUMER,false));

                AppNode.brokersList = (HashMap) service_in.readObject();
                AppNode.brokersList.forEach((k, v)
                        -> System.out.println("Address: " + k + "   Topics:" +  v));

            }catch(Exception e){
                e.printStackTrace();
            }try{
                socket.close();
                System.out.println("Thread for init closed...");
            }catch (IOException e){
                e.printStackTrace();
            }

        };
        Thread initThread = new Thread(task);
        initThread.start();
    }

    public void register(String hashtag){
        this.topics.add(hashtag);

        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {
                System.out.println("Thread register started ...");
                //AppNode.brokersList
                Broker.getBrokerList()
                        .forEach((k, t) ->
                        {
                            System.out.println(k + " " + t + "");
                            if (t.contains(hashtag)) {
                                Socket socketToBroker = null;
                                try {
                                    socketToBroker = new Socket(k.getIp(), k.getPort());
                                    System.out.println("Connected to " + k.getIp() + ":" + k.getPort());

                                    ObjectOutputStream service_out = new ObjectOutputStream(socketToBroker.getOutputStream());

                                    service_out.writeObject(new Value(this.addr,hashtag , "something", SenderType.CONSUMER));
                                    service_out.flush();
                                    Log.d("insidePull", "register");

                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
//                                try{
//                                    socketToBroker.close();
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
                            }
                        });
            } catch (Exception e) {
                e.getStackTrace();
            }
            System.out.println("Thread register ended...");

        };
        Thread thread = new Thread(task);
        thread.start();
    }
    public void showConversationData(String hashtag) {
        Runnable task = () ->{
          try{
              System.out.println("Thread Show Conversation Data started...");
              //AppNode.brokersList.forEach((broker, topics)->{
              Broker.getBrokerList().forEach((broker, topics)-> {
                  if(topics.contains(hashtag)){
                      Socket socketToBroker = null;
                      try{
                          socketToBroker = new Socket(broker.getIp() , broker.getPort());
                          ObjectOutputStream out = new ObjectOutputStream(socketToBroker.getOutputStream());
                          //ObjectInputStream in = new ObjectInputStream(socketToBroker.getInputStream());

                          out.writeObject(new Value(this.addr,hashtag , "history", SenderType.CONSUMER));
                          out.flush();
                      }catch (Exception e){
                          e.printStackTrace();
                      }
//                      try{
//                          socketToBroker.close();
//                      }catch (Exception e){
//                          e.printStackTrace();
//                      }

                  }
              });
          }catch (Exception e){
              e.printStackTrace();
          }
          System.out.println("Thread Show Conversation Data ended...");
        };
        new Thread(task).start();
    }

    public void pull(){

        Runnable task =() ->{
            try{

                Log.d("insidePull", "inside pull()");
                serverSocket = new ServerSocket(addr.getPort()+1);
                // server socket at ip 127.0.0.1
                System.out.println("\nServer Socket Open...");

                while(true){

                    socketToReceive = serverSocket.accept();
                    System.out.println("consumer socket.accept()\n");
                    Log.d("insidePull", "accept()");
                    Runnable _task = () ->{
                        try{

                            ArrayList<Date> datesToInsert = new ArrayList<>();
                            ObjectOutputStream out = new ObjectOutputStream(socketToReceive.getOutputStream());
                            ObjectInputStream in = new ObjectInputStream(socketToReceive.getInputStream());

                            Value hashAndDateInValue = (Value)in.readObject();
                            String topic = hashAndDateInValue.getTopic();
                            Log.d("insidePull", topic);
                            Date dateCreated = hashAndDateInValue.getDateCreated();
                            String typeOfFile = hashAndDateInValue.getType();
                            System.out.println("Receiving topic:  "+topic);
                            //String home = System.getProperty("user.home");
                            String home = "/storage/emulated/0/Download/";
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH-mm-ss");
                            Log.d("insidePull", home);

                            if(Files.notExists(Paths.get(home + topic + "withDate" + dateFormat.format(dateCreated) + typeOfFile))){

                                File file = new File(home + topic + "withDate" + dateFormat.format(dateCreated) + typeOfFile);
                                Files.createFile(file.getAbsoluteFile().toPath());

                                while(true){

                                    Value chunkInValue = (Value)in.readObject();
                                    System.out.println("GOT CHUNK");
                                    MultimediaFile chunk = chunkInValue.getMultimediaFile();
                                    saveChunk(chunk,file);

                                    if(chunkInValue.isLast){
                                        System.out.println("Received whole File...");
                                        break;
                                    }
                                }
                            }
                            else{
                                System.out.println("Already Have File...");
                            }

                        }catch (IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    };
                    new Thread(_task).start();
                }
            }catch(IOException e){
                e.printStackTrace();
            }finally {
                try {
                    socketToReceive.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task).start();
    }

    public void saveChunk(MultimediaFile chunk , File file) throws IOException {
        Files.write(file.toPath() , chunk.getVideoFileChunk() , StandardOpenOption.APPEND);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(topics);
    }
}
