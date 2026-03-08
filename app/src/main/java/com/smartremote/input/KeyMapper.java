package com.smartremote.input;

import java.util.HashMap;

public class KeyMapper {

    static HashMap<String,Byte> map=new HashMap<>();

    static{

        map.put("A",(byte)0x04);
        map.put("B",(byte)0x05);
        map.put("C",(byte)0x06);

    }

    public static byte getKeyCode(String key){

        return map.get(key);

    }

}
