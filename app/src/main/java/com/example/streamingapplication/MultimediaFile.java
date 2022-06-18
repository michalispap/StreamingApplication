package com.example.streamingapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Comparator;

public class MultimediaFile implements Serializable {

    String AbsolutePath;
    String FileName;
    String ChannelName;
    Date DateCreated;
    String type;
    ArrayList<String> Hashtags = new ArrayList<>();
    List<byte[]> File = new ArrayList<>();

    public long Count = 0;
    private static final long serialVersionUID = 2007333342745327391L;

    String text;

    public static Comparator <MultimediaFile> DateComparator = (m1, m2) -> {
        Date Date1 = m1.DateCreated;
        Date Date2 = m2.DateCreated;

        //ascending order
        return Date1.compareTo(Date2);

        //descending order
        //return Date2.compareTo(Date1);
    };

    public MultimediaFile(String absolutePath){
        this.AbsolutePath = absolutePath;
    }

    //Text constructor
    public MultimediaFile(byte[] FileChunk , Date dateCreated){
        this.File.add(FileChunk);
        this.DateCreated = dateCreated;
    }
    public MultimediaFile(byte[] FileChunk , Date dateCreated, String type){
        this.File.add(FileChunk);
        this.DateCreated = dateCreated;
        this.type =type;
    }
    public MultimediaFile(byte[] FileChunk){
        this.File.add(FileChunk);
    }
    public MultimediaFile(List<byte[]> File,Date dateCreated){
        this.File = File;
        this.DateCreated = dateCreated;
    }
    public MultimediaFile(List<byte[]> File,Date dateCreated ,String type){
        this.File = File;
        this.DateCreated = dateCreated;
        this.type = type;
    }
    public MultimediaFile( String channelName , String text){
        //this.FileChunk = FileChunk;
        this.ChannelName = channelName;
        this.text = text;
    }

    public String getVideoName() {
        return FileName;
    }
    public void setHashtags(ArrayList<String> hashtags) {
        this.Hashtags= hashtags;
    }
    public void setHashtag(String topic){this.Hashtags.add(topic);}
    public ArrayList<String> getHashtags() {
        return Hashtags;
    }
    public void addHashtag(String hashtag){
        if(!Hashtags.contains(hashtag))
            Hashtags.add(hashtag);
    }

    public String getAbsolutePath() {return AbsolutePath;}
    public List<byte[]> getVideoFileChunks() {
        return File;
    }
    public byte[] getVideoFileChunk(){return File.get(0);}
    public String getType(){
        return type;
    }
}
