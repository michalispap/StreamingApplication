package com.example.streamingapplication;

import java.util.List;

public interface IBroker extends INode {



    List<Consumer> registeredUsers = null;
    List<Publisher> registeredPublishers = null;
    Consumer acceptConnection(Consumer consumer);
    Publisher acceptConnection(Publisher publisher);
    void calculateKeys();
    void filterConsumers(String s);
    void notifyBrokersOnChanges();
    void notifyPublisher(String s);
    void pull(String s);

}
