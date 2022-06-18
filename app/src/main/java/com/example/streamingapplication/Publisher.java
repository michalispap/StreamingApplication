package com.example.streamingapplication;

import android.os.Environment;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Publisher {

    private Socket socket;
    public Address addr;
    public String channelName;
    private HashMap<String,ArrayList<String>> FileCollection = new HashMap<>();
    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.1.5", 6000)
    ));

    /// CONSTRUCTORS
    public Publisher(){}

    public Publisher(Address _addr , String _channelName){
        this.addr = _addr;
        System.out.println(this.addr);
        this.channelName = _channelName;
    }

    public void setFileCollection(String text , ArrayList<String> topics){
        this.FileCollection.put(text , topics);
    }
    public HashMap<String,ArrayList<String>> getFileCollection(){
        return this.FileCollection;
    }

    public static void main (String args[]){}

    public HashMap<String, String> getMetadata(String file){
        HashMap<String, String> data = new HashMap<>();

        try  {
            FileInputStream f = new FileInputStream(new File(file));
            File _file = new File(file);
            long lengthInKb = (_file.length()/1024);
            String _name = _file.getAbsolutePath().substring(57);
            System.out.println(_name +"\n"+  Long.toString(lengthInKb));
            data.put("LengthInKb" , Long.toString(lengthInKb));
            data.put("name" , _name);
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext pcontext = new ParseContext();
            MP4Parser MP4Parser = new MP4Parser();
            MP4Parser.parse(f, handler, metadata, pcontext);
            String[] metadataNames = metadata.names();

            for (String name : metadataNames) {
                //System.out.println(name);
                switch (name) {
                    case "Creation-Date":
                        data.put("Creation-Date", metadata.get(name));
                        break;
                    case "xmpDM:duration":
                        data.put("xmpDM:duration", metadata.get(name));
                        break;
                    case "tiff:ImageWidth":
                        data.put("tiff:ImageWidth", metadata.get(name));
                        break;
                    case "tiff:ImageLength":
                        data.put("tiff:ImageLength", metadata.get(name));
                        break;
                }
            }
        }
        catch (Exception e ) {
            System.out.println("Exception in Publisher" + e.getStackTrace());
        }
        //System.out.println(data);

        return data;
    }

    public ArrayList<byte[]> generateChunks(File file ) throws TikaException, IOException, SAXException {
        ArrayList<byte[]> chunks = new ArrayList<>();
        try {
            byte[] fileInBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            for(int i=0 ; i < fileInBytes.length;){
                byte [] chunk = new byte[Math.min(1024*1024/2 , fileInBytes.length -i)];

                for(int j=0 ; j<chunk.length ; j++,i++){
                    chunk[j] = fileInBytes[i];
                }
                chunks.add(chunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chunks;
    }

    public void getBrokerList() {
        Runnable task = () -> {
            try{
                System.out.println("Updating Brokers List...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                service_out.writeObject(new Value(this.addr,"get brokers", SenderType.PUBLISHER));
                service_out.flush();
                AppNode.brokersList = (HashMap) service_in.readObject();
                //System.out.println("HashMap Read:\n");
                AppNode.brokersList.forEach((k, v)
                        -> System.out.println("Address: " + k + "   Topics:" +  v)
                );

            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        };
        new Thread(task).start();
    }

    public Address hashTopic(String topic) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(topic.getBytes(), 0, topic.length());
        String md5 = new BigInteger(1, digest.digest()).toString(16);
        BigInteger decimal = new BigInteger(md5, 16);
        BigInteger result = decimal.mod(BigInteger.valueOf(3));
        int mod = result.intValue();
        switch (mod) {
            case 0: {
                System.out.println("Broker 1 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[0]);
                return (Address) Broker.getBrokerList().keySet().toArray()[0];
            }
            case 1: {
                System.out.println("Broker 2 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[1]);
                return (Address) Broker.getBrokerList().keySet().toArray()[1];
            }
            case 2: {
                System.out.println("Broker 3 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[2]);
                return (Address) Broker.getBrokerList().keySet().toArray()[2];
            }
        }

        return null;
    }

    public void sendFile(String text,ArrayList<String> hashtags , Date dateCreated) {
        Runnable task = () -> {
            try {
                hashtags.forEach(hashtag
                        -> {
                    try {
                        System.out.println("Thread sending file started...");
                        notifyBroker(text,hashtag,dateCreated);
                        System.out.println("Thread sending text ended ....");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.getStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }
    public void notifyBroker(String content,String hashtag, Date dateCreated){
        try{
            String type = null;
            Address address = hashTopic(hashtag);
            Socket socketBroker = new Socket(address.getIp(), address.getPort());
            System.out.println("Notifying Broker: " + address  + " for:  "+ hashtag);

            ObjectOutputStream serv_out = new ObjectOutputStream(socketBroker.getOutputStream());

            ArrayList<String> listOfHashtag = new ArrayList<>();
            listOfHashtag.add(hashtag);

            /// send file to broker ///
            ArrayList<byte[]> chunks = new ArrayList<>();
            HashMap<String, String> metaMap = new HashMap<String,String>();

            if(content.endsWith(".mp4") || content.endsWith(".jpg")) {
                System.out.println("GenerateChunks for video or photo");
                type = content.substring(content.length()-4);
                System.out.println(type);
                File file = new File(content); // content is the absolute path of the file

                chunks = generateChunks(file);
            }
            else {
                System.out.println("GenerateChunks for text");
                try {
                    type = ".txt";
                    File file = new File("/storage/emulated/0/Download/Testing" + content+ ".txt");
                    if(!file.exists()){
                        try{
                            file.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    FileOutputStream writer = new FileOutputStream(file);
                    writer.write(content.getBytes());
                    writer.flush();
                    writer.close();
                    chunks = generateChunks(file);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            serv_out.writeObject(new Value(this.addr,hashtag , "something" , SenderType.PUBLISHER));
            serv_out.flush();
            for(int i=0;i<chunks.size();i++){
                push(i, chunks ,dateCreated,type,serv_out);
            }

        } catch (NoSuchAlgorithmException | IOException | TikaException | SAXException e) {
            e.printStackTrace();
        }
    }
    public void push(int i , ArrayList<byte[]> chunks, Date dateCreated, String type,ObjectOutputStream serv_out) throws IOException {

        if(i==chunks.size()-1){
            Value valueToSend = new Value(new MultimediaFile(chunks.get(i), dateCreated , type), this.addr , SenderType.PUBLISHER);
            valueToSend.isLast = true;
            serv_out.writeObject(valueToSend);
            System.out.println("IS LAST TRUE");

        }else {
            serv_out.writeObject(new Value(new MultimediaFile(chunks.get(i), dateCreated), this.addr, SenderType.PUBLISHER));
        }
        serv_out.flush();
    }
}
