package com.example.streamingapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class Broker implements INode {

    static InetAddress inetAddress;
    private static Address address = null;
    private static String Ip;
    private static int port;

    private Socket socket;
    private ServerSocket serverSocket;

    // Registered Publishers and Consumers with topics
    private HashMap<Address,ArrayList<String>>  registeredConsumers;
    // Total of initialized Clients , we dont keep track of topics etc.


    // this list includes both channel names and specific topics
    public static ArrayList<String> topics = new ArrayList<>();

    // handles thread parallelism
    private Map<String, ArrayList<MultimediaFile>> Queue = new ConcurrentHashMap< >();

    // Constructor
    public Broker(String Ip , int port){
        this.Ip = Ip;
        this.port = port;
        //this.address = new Address(this.Ip ,this.port);
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        registeredConsumers = new HashMap<>();
        init(5);
        connect();

    }

    public static void main(String args[]) throws IOException{
        System.out.println("port num:");
        port = new Scanner(System.in).nextInt();
//        inetAddress = InetAddress.getLocalHost();
//        Ip = inetAddress.getHostAddress();
//        System.out.println(Ip);
        final DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"),10002);

        address = new Address(socket.getLocalAddress().getHostAddress(),port);
        System.out.println(address);

        new Broker(address.getIp(),address.getPort());
    }

    // creates server side socket and accepts connections
    // start new thread for each connection
    // serverThread.start()
    public void connect(){
        try{
            System.out.println("Server Socket Up and Running ...\n");
            while(true){

                socket = serverSocket.accept();
                System.out.println("socket.accept()\n");
                System.out.println(socket.getPort());

                Runnable task = () -> {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                        Value value = (Value)in.readObject();
                        System.out.println(value.sender);
                        if(value.sender == SenderType.PUBLISHER){
                            new Thread(new publisherThread(socket , in , out ,value))
                                    .start();
                            System.out.println("pub thread.start()\n");
                        }else{
                            new Thread(new consumerThread(socket , in , out,value))
                                    .start();
                            System.out.println("cons thread.start()\n");
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                };
                new Thread(task).start();
            }
        }catch (IOException  e) { //| ClassNotFoundException
            e.printStackTrace();

        } finally {
            try {
                // close socket connection
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public ArrayList<MultimediaFile> sortByDate(ArrayList<MultimediaFile> mfList) {

        Collections.sort(mfList, MultimediaFile.DateComparator);
        return mfList;
    }

    /// Broker init() is responsible serving the client(either pub or cons), the brokersList {< <Ip,Port>,ArrayList<String>(Topics) >}
    @Override
    public void init(int x){
        updateBrokerInfo();
    }
    @Override
    public void updateNodes(Value value) {
        if(!topics.contains(value.getTopic())) {
            topics.add(value.getTopic());
        }
        topics.stream().forEach( e -> System.out.println(e));
    }
    @Override
    public void disconnect(){}

    public static HashMap<Address,ArrayList<String>> getBrokerList(){
        HashMap<Address,ArrayList<String>> brokers = new HashMap<>();

        try(Socket service = new Socket(INode.ZookeeperAddress.getIp() , INode.ZookeeperAddress.getPort())){
            ObjectOutputStream service_out = new ObjectOutputStream(service.getOutputStream());
            ObjectInputStream service_in = new ObjectInputStream(service.getInputStream());

            service_out.writeObject("get brokers");
            service_out.flush();
            brokers = (HashMap<Address, ArrayList<String>>) service_in.readObject();
        }catch(IOException | ClassNotFoundException | ClassCastException e){
            e.printStackTrace();
            System.out.println("Problem synchronising brokers");
            return null;
        }
        return brokers;
    }

    public void updateBrokerInfo(){
        try{
            Socket serviceSocket = new Socket(INode.ZookeeperAddress.getIp(), INode.ZookeeperAddress.getPort());

            ObjectOutputStream out = new ObjectOutputStream(serviceSocket.getOutputStream());
            out.writeObject("insert or update broker");
            out.flush();

            out.writeObject(new Value(address , topics, SenderType.BROKER));
            out.flush();

            System.out.println("Broker send info to Zookeeper");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void sendFiles(ArrayList<MultimediaFile> files , Address _address , String topic){
        System.out.println(files);
        files.forEach(file -> {
            System.out.println(file);
            try{
                Socket socketToConsumer = new Socket(_address.getIp(), _address.getPort() + 1);
                System.out.println("Sending files to:   "+_address);

                ObjectInputStream in = new ObjectInputStream(socketToConsumer.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socketToConsumer.getOutputStream());

                List<byte[]> chunks = file.getVideoFileChunks();
                out.writeObject(new Value(address , topic, file.DateCreated , file.getType(), SenderType.BROKER));
                for(int i=0;i< chunks.size();i++) {
                    try {
                        if(i==chunks.size()-1) {
                            Value valueToSend = new Value(new MultimediaFile(chunks.get(i)), address , SenderType.PUBLISHER);
                            valueToSend.isLast = true;
                            out.writeObject(valueToSend);
                        }else{
                            out.writeObject(new Value(new MultimediaFile(chunks.get(i)), address, SenderType.BROKER));
                            out.flush();
                        }
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
                System.out.println("FOR exited");
            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("Sent File");
            });
        System.out.println("Sending Files ended...");
    }

///////////////// PUBLISHER THREAD INNER CLASS///////////
    public class publisherThread extends Thread implements Serializable{

        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        private final Socket socket;
        private Value value;

        public publisherThread(Socket _socket, ObjectInputStream in , ObjectOutputStream out, Value value) {
            this.socket = _socket;
            this.value = value;
            this.service_in = in;
            this.service_out = out;
        }

        @Override
        public void run(){
            try{
                System.out.println("Server Thread For Pub Triggered");

                if( value.getAction().equals("get brokers")){
                    init();
                    System.out.println("Get Brokers........");
                }else{
                    updateNodes(value);
                    updateBrokerInfo();
                    insertFileToQueue(value.getTopic());
                    Queue.forEach((k,v)->{
                        System.out.println("Topic: " + k + "   MultimediaFile:  " + v);
                        v.forEach(file-> System.out.println(file.DateCreated));
                        v.forEach(file ->System.out.println(file.type));
                    });
                    registeredConsumers.forEach((consumer,list)->
                    {
                        if(list.contains(value.getTopic())){
                            System.out.println("QUEUE: ");
                            System.out.println(Queue.get(value.getTopic()));
                            sendFiles(Queue.get(value.getTopic()),consumer , value.getTopic());
                        }
                    });
                }

            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        void init(){
            try {

                service_out.writeObject(new HashMap<>(getBrokerList()));
                service_out.flush();

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        void insertFileToQueue(String hashtag){
            ArrayList<byte[]> chunks = new ArrayList<>();
            Date dateCreated;
            String type;
            try {
                while(true) {
                    System.out.println("INSERTING FILE TO QUEUE");
                    Value chunk = (Value) service_in.readObject();
                    chunks.add(chunk.getMultimediaFile().getVideoFileChunk());
                    if (chunk.isLast){
                        dateCreated = chunk.getMultimediaFile().DateCreated;
                        type = chunk.getMultimediaFile().getType();
                        System.out.println(" Received whole file " + dateCreated + "with type:   "+ type);
                        break;
                    }
                }
//                Queue.putIfAbsent(new AtomicReference<String>(hashtag) , new AtomicReference<ArrayList<MultimediaFile>>(file));
                if(Queue.containsKey(hashtag)){
                    Queue.get(hashtag).add(new MultimediaFile(chunks,dateCreated,type));
                }else{
                    ArrayList<MultimediaFile> list = new ArrayList<>();
                    list.add(new MultimediaFile(chunks,dateCreated,type));
                    Queue.put(hashtag,list);
                }
                //System.out.println(Queue);

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }
    }

    ///////////////// CONSUMER THREAD INNER CLASS///////////
    public class consumerThread extends Thread{
        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        public Socket socket;
        private Value value;

        public consumerThread(Socket _socket , ObjectInputStream in , ObjectOutputStream out , Value value){
            this.socket = _socket;
            this.value = value;
            this.service_in = in;
            this.service_out = out;
        }

        @Override
        public void run(){
            try{

                System.out.println("Consumer Thread running ...\n");
                if(!value.initialized){

                    init();
                    INode.initClients.add(value.getAddress());

                }/// Consumer is Initialized
                else{
                    System.out.println(value.getAction());
                    updateConsumers(value);
                    for(String topic : Queue.keySet()){
                        if (topic.equals(value.getTopic())) {
                            System.out.println("before sort");
                            System.out.println(Queue.get(topic));
                            ArrayList<MultimediaFile> sortedFiles = sortByDate(Queue.get(topic));
                            System.out.println("after sort");
                            System.out.println(sortedFiles);
                            sendFiles(sortedFiles, value.getAddress(), value.getTopic());
                        }
                    }
                }
                System.out.println("Consumer thread ended....");
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        void init(){
            try {
                service_out.writeObject(new HashMap<>(getBrokerList()));
                service_out.flush();

            }catch(IOException e){
                e.printStackTrace();
            }
        }


        void updateConsumers(Value value){

            // We already have the consumer registered to Broker
            if(registeredConsumers.containsKey(value.getAddress())){
                registeredConsumers.get(value.getAddress())
                        .add(value.getTopic());
                System.out.println("Con updated ....");
            }else {
                // Publisher not registered to Broker
                ArrayList<String> listWithTopic = new ArrayList<>();
                listWithTopic.add(value.getTopic());
                registeredConsumers.put(value.getAddress(),  listWithTopic);
                System.out.println("Con is now registered...");
            }

            registeredConsumers.forEach((k,v)
                    -> System.out.println("Consumers Address: " + k + "  Topics: " +v)
            );

        }
    }

}
