package com.example.streamingapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class AppNode {

    protected static String Ip;
    protected static int port;
    private static int type;

    // localhost addr
    static InetAddress inetAddress;
    protected static Address address = null;

    private int action ;

    /// USERS BROKER LIST
    public static HashMap<Address, ArrayList<String>> brokersList;

    public static void main (String args[]) throws IOException {


        // get localhost IP and port num;
        try{
            System.out.println("port num:");
            port = new Scanner(System.in).nextInt();
            System.out.println("Your port num is:  "+ port);
//            inetAddress = InetAddress.getLocalHost();
//            Ip = inetAddress.getHostAddress();
//            System.out.println("Your Ip is:   "+Ip);
//            address = new Address(Ip,port);

            final DatagramSocket dSocket = new DatagramSocket();
            dSocket.connect(InetAddress.getByName("8.8.8.8"),10002);
            address = new Address(dSocket.getLocalAddress().getHostAddress() , port);


        }catch(Exception e){
            e.getStackTrace();
        }

        System.out.println("Enter Publisher Channel Name:  ");
        String channelName =  new BufferedReader(new InputStreamReader(System.in)).readLine();
        Publisher pub = new Publisher(address, channelName);
        Consumer con = new Consumer(address);
        //Publisher pub = new Publisher(address, channelName);

        System.out.print( "Welcome , select user type , 0 to exit , 1 for pub , 2 for consumer  , 3 for Updating Broker Info" );
        int type= new Scanner(System.in).nextInt();
        while( type != 0){
            // Publisher logic
            if(type == 1) {
                try {
                    System.out.println("Type 1 to send text / Type 2 to send Photo / Type 3 to send Video ....\n");
                    int a = new Scanner(System.in).nextInt();
                    String content = null;
                    // text
                    if(a == 1){
                        System.out.println("Enter text to share: \n");
                         content = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    }// photo
                    else if(a==2){
                        /// content must now be path of photo...
//                        Path path = Paths.get("test.jpg");
                        System.out.println("Here is a list of the available images:\n");
                        File directory = new File("data");
                        String[] fileList = directory.list();
                        int flag = 0;
                        for (String s : fileList) {
                            if (s.endsWith("jpg")) {
                                System.out.println(s);
                            }
                        }
                        System.out.println("\nChoose one of the above images:\n");
                        while (flag == 0) {
                            Scanner sc = new Scanner(System.in);
                            content = sc.nextLine();
                            for (String s : fileList) {
                                if (s.equalsIgnoreCase(content)) {
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                System.out.println("Wrong file name, try again.");
                            }
                        }
                    }// video
                    else if(a==3){
                        //content = "C:\\Users\\alex\\source\\repos\\distributed_sys_streamer\\data\\sample5.mp4";
                        System.out.println("Here is a list of the available videos:\n");
                        File directory = new File("data");
                        String[] fileList = directory.list();
                        int flag = 0;
                        for (String s : fileList) {
                            if (s.endsWith("mp4")) {
                                System.out.println(s);
                            }
                        }
                        System.out.println("\nChoose one of the above videos:\n");
                        while (flag == 0) {
                            Scanner sc = new Scanner(System.in);
                            content = sc.nextLine();
                            for (String s : fileList) {
                                if (s.equalsIgnoreCase(content)) {
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                System.out.println("Wrong file name, try again.");
                            }
                        }
                    }else{
                        System.out.println("Try again...");
                        break;
                    }

                    System.out.println("Enter HashTag ...  type end to Stop");
                    ArrayList<String> hashTags = new ArrayList();
                    BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
                    String hashtag;

                    while(!(hashtag = br.readLine()).equals("end")){

                        hashTags.add(hashtag);
                        System.out.println(hashtag + " added to hashtags\n");
                    }
                    Date dateCreated = new Date();
                    /// video case --> text should be the path location selected after the switch case
                    pub.setFileCollection(content,hashTags);
                    System.out.println("FileCollection:\n");
                    System.out.println(pub.getFileCollection());

                    pub.sendFile(content,hashTags,dateCreated);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Consumer Logic
            if(type == 2) {
                try {
                    System.out.println("Type 1 to register / Type 2 to view conversation data ....\n");
                    int a = new Scanner(System.in).nextInt();
                    BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
                    String topic;
                    if(a == 1){
                        System.out.println("Enter topics of interest: \n");
                        while (!(topic = br.readLine()).equals("end")) {
                            con.register(topic);
                            System.out.println("Registered to: "+topic );
                            System.out.println("Type end to Stop");
                        }
                    }else if(a ==2){
                        System.out.println("Enter topic to show history: \n");
                        topic = br.readLine();
                        con.showConversationData(topic);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(type == 3){
                pub.getBrokerList();

            }
            System.out.println(" select user type , 0 to exit , 1 for pub , 2 for consumer  , 3 for Updating Broker Info");
            type = new Scanner(System.in).nextInt();
        }

        System.out.println("APP NODE EXITING");


    }
}
