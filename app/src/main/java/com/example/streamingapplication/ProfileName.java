package com.example.streamingapplication;

import java.util.HashMap;
import java.util.ArrayList;

public class ProfileName {

    //Fields
    private String profileName;
    private HashMap<String, ArrayList<Value>> userVideoFilesMap = new HashMap<>();
    private HashMap<String, Integer> subscribedConversations = new HashMap<>();

    //profileName get-set
    public String getProfileName() {
        return profileName;
    }
    public void setProfileName(String profileName) {this.profileName = profileName;}

    //userVideoFilesMap get-set
    public HashMap getUserVideoFilesMap() {return userVideoFilesMap;}
    public void setUserVideoFilesMap(HashMap<String, ArrayList<Value>> userVideoFilesMap) {
        this.userVideoFilesMap= userVideoFilesMap;
    }

    ////subscribedConversations get-set
    public HashMap subscribedConversations() {return subscribedConversations;}
    public void setSubscribedConversations(HashMap<String, Integer> subscribedConversations) {
        this.subscribedConversations = subscribedConversations;
    }

}