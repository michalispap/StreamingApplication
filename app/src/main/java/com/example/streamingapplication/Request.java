package com.example.streamingapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class Request implements Serializable {

    Address Address;
    //SenderType SenderType=null;
    String VideoName;
    ArrayList<String> Topics;
    String text;

    byte[] chunk;

//    public Request(Address address, ArrayList<String> topics,SenderType senderType,String VideoName) {
//        this.Address = address;
//        this.Topics = topics;
//        this.SenderType=senderType;
//    }
//    public Request(Address address, ArrayList<String> topics,SenderType senderType) {
//        this.Address = address;
//        this.Topics = topics;
//        this.SenderType=senderType;
//    }
    public Request (Address address , String text){
        this.Address = address;
        this.text = text;
    }

    public Request(Address address, ArrayList<String> topics) {
        this.Address = address;
        this.Topics = topics;
    }
    public Address getAddress() { return Address; }
    public void setAddress(Address address) { this.Address = address; }
    public ArrayList<String> getTopics() { return Topics; }
    public void setTopics(ArrayList<String> topics) { this.Topics = topics; }
    public String getVideoName(){return this.VideoName;}
}
