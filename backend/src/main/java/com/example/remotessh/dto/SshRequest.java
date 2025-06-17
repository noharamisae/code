package com.example.remotessh.dto;

public class SshRequest {
    private String ip;
    private String storeCode;       // "7105"
    private String allStreCodeFile;   //  "TestStreCode.txt"
    private String scriptName;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getScriptName() {
        return scriptName;
    }
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
        }
    public String getStoreCode() {
        return storeCode;
    }
    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }


    public String getAllStreCodeFile() {
        return allStreCodeFile;
    }
    public void setAllStreCodeFile(String allStreCodeFile) {
        this.allStreCodeFile = allStreCodeFile;
        }
    
}
