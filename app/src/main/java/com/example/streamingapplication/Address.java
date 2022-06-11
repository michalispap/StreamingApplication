package com.example.streamingapplication;

import java.io.Serializable;
import java.util.Objects;


public class Address implements Serializable {

    private String ip;
    private int port;

    public Address(String ip, int port) {
        this.ip = ip; this.port = port;
    }
    public Address(Address address) {
        this.ip = address.getIp(); this.port = address.getPort();
    }
    public String getIp() { return ip; }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

    public void setIp(String ip) { this.ip = ip; }

    @Override
    public int hashCode() {
        return  String.valueOf(this.port).hashCode() + ip.hashCode();
    }

    @Override
    public String toString() {
        return this.ip +" , "+this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Address address = (Address)o;
        return port == address.port && Objects.equals(ip, address.ip);
    }
}