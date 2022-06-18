package com.example.streamingapplication;

import java.util.ArrayList;

public interface INode {

    static  final Address ZookeeperAddress = new Address("192.168.1.5",5000);

    ArrayList<Address> initClients = new ArrayList<>();
    //void init(Request request) throws IOException;

    //void connect() throws IOException;

    //void disconnect() throws IOException;
    //------------------------------------------------------------------------------------------------------------------

    void connect();
    void disconnect();
    void init(int x);
    void updateNodes(Value value);

}