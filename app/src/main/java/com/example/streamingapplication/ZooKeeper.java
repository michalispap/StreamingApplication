package com.example.streamingapplication;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZooKeeper {

    protected static final Map<Address,ArrayList<String>> brokerTopics = new ConcurrentHashMap<Address,ArrayList<String>>();


    public static void main(String args[]){
        new ZooKeeper().startServer();
    }

     public void startServer(){
        final ExecutorService threadPool = Executors.newFixedThreadPool(12);
        Runnable task  = () ->
        {

            try{
                final DatagramSocket socket = new DatagramSocket();
                socket.connect(InetAddress.getByName("8.8.8.8") , 10002);
//                InetAddress addr = InetAddress.getByName("8.8.8.8").;
                System.out.println(socket.getLocalAddress().getHostAddress());
                System.out.println(socket.getLocalAddress());
                ServerSocket serverSocket = new ServerSocket(INode.ZookeeperAddress.getPort(), 20 , socket.getLocalAddress());

                //System.out.println(serverSocket.getInetAddress());

                System.out.println("Waiting for brokers to connect...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(new Task(clientSocket));
                    System.out.println("socket .accept()");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        };
        new Thread(task).start();
     }

    protected void updateBrokers(Value value){
        Address address = value.getAddress();

        if (brokerTopics.containsKey(address))
            brokerTopics.replace(address, value.getTopics());
        else
            brokerTopics.put(address, value.getTopics());
        System.out.println("brokerTopics Updated...");
        brokerTopics.forEach((k,v)
            -> System.out.println("Address: " +k + "  Topics: " +v)
        );
    }


     private class Task implements Runnable{
        private Socket clientSocket;

        //Constructor
         private Task(Socket clientSocket){
             this.clientSocket = clientSocket;
         }

         @Override
         public void run(){
             System.out.println("Incoming Broker connection accepted \n Proceeding with request..");

             try{
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

                 String action = (String)in.readObject();
                 if(action.equalsIgnoreCase("get brokers")){
                     out.writeObject(new HashMap<>(brokerTopics));
                     out.flush();
                     System.out.println("Send Broker List");
                 }else if (action.equalsIgnoreCase("insert or update broker")){
                     Value value = (Value)in.readObject();
                     updateBrokers(value);
                     System.out.println("Update Brokers List ");

                 }

             }catch (Exception e){
                 e.printStackTrace();
             }try {
                 clientSocket.close();
             }catch (Exception e){
                 e.printStackTrace();
             }

         }
     }
}
