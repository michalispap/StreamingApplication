package com.example.streamingapplication;

public interface IConsumer extends INode {

    void disconnect(String s);
    void register(String s);
    void showConversationData(String s, Value v);

}
