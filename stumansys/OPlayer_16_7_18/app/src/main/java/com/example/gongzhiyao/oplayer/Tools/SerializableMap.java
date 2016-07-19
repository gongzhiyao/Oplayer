package com.example.gongzhiyao.oplayer.Tools;

import java.io.Serializable;
import java.util.Map;

public class SerializableMap implements Serializable {
 
    private Map<String,String> map;
 
    public Map<String, String> getMap() {
        return map;
    }
 
    public SerializableMap setMap(Map<String, String> map) {
        this.map = map;
        return null;
    }
}