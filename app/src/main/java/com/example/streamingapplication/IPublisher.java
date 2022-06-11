package com.example.streamingapplication;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public interface IPublisher extends INode {

    /*void addHashTag(String hashtag);

    void removeHashTag(String hashtag);

    //HashMap<Address, ArrayList<String>> getBrokersList();

    void push(File file, ArrayList<String> topics, ObjectOutputStream outputStream) throws TikaException, IOException, SAXException;

    void notifyFailure(Broker broker);

    void notifyBrokerForHashtags(String string);

    //ArrayList<MultimediaFile> generateChunks(File file) throws TikaException, IOException, SAXException;*/

    //------------------------------------------------------------------------------------------------------------------

    ProfileName profileName = null;
    ArrayList<MultimediaFile> generateChunks(File file) throws TikaException, IOException, SAXException;
    void getBrokerList();
    Address hashTopic(String s) throws NoSuchAlgorithmException;
    void notifyBrokersNewMessage(String s);
    void notifyFailure(Broker broker);
    void push(String s, Value v);

}