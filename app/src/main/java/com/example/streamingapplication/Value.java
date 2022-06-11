package com.example.streamingapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Value implements Serializable {

    private MultimediaFile multimediaFile;
    private ArrayList<String> hashtags;
    private Address address;
    private String action = "something";
    private String topic;
    private Date dateCreated;
    private String type;
    SenderType sender = null;
    public Boolean initialized = true;
    public Boolean isLast = false;


    public Value (MultimediaFile m, SenderType senderType) {
        this.multimediaFile = m;
        this.sender = senderType;
    }
    public Value (Address address , String topic, Date dateCreated , String type, SenderType sender){
        this.address = address;
        this.topic = topic;
        this.dateCreated = dateCreated;
        this.sender = sender;
        this.type = type;
    }
    /// Broker to Zookeeper
    public Value(Address address , ArrayList<String> topics , SenderType type){
        this.address = address;
        this.hashtags = topics;
        this.sender = type;
    }
    /// USED BY PUBLISHER TO SEND HASHTAG TO BROKER
    public Value(Address address , String topic , String action, SenderType type){
        this.address = address;
        this.topic = topic;
        this.sender = type;
    }
    /// Used by consumer to init()
    public Value (Address address, SenderType senderType , Boolean initialized) {
        this.address = address;
        this.sender = senderType;
        this.initialized = initialized;
    }
    /// Used by publisher.push()
    public Value (MultimediaFile m, Address address, SenderType senderType) {
        this.multimediaFile = m;
        this.address = address;
        this.sender = senderType;
    }
    // getBrokersList()
    public Value(Address address , String action, SenderType senderType){
        this.address = address;
        this.action = action;
        this.sender = senderType;
    }

    public MultimediaFile getMultimediaFile() {return multimediaFile;}

    public Address getAddress() {return address;}

    public ArrayList<String> getTopics(){
        return hashtags;
    }

    public String getAction(){
        return action;
    }

    public String getTopic() {return topic;}
    public Date getDateCreated(){
        return dateCreated;
    }
    public String getType(){
        return type;
    }

}
