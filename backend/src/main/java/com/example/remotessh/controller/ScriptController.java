package com.example.remotessh.controller;

import com.example.remotessh.dto.SshRequest;
import com.example.remotessh.service.SshSercive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ScriptController {

    @Autowired
    private SshSercive sshService;

    @PostMapping("/ssps-image-send")
    public ResponseEntity<Map<String, String>> runSspsImageSend(@RequestBody SshRequest requestSspsImageSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestSspsImageSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.executeSspsImageSend("10.168.106.102",
                requestSspsImageSend.getStoreCode(), requestSspsImageSend.getAllStreCodeFile());
        return ResponseEntity.ok(logContent);
    }

    @PostMapping("/ssps-script-send")
    public ResponseEntity<Map<String, String>> runSspsScriptSend(@RequestBody SshRequest requestSspsScriptSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestSspsScriptSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.executeSspsScriptSend("10.168.106.102",
                requestSspsScriptSend.getScriptName(), requestSspsScriptSend.getStoreCode(),
                requestSspsScriptSend.getAllStreCodeFile());
        return ResponseEntity.ok(logContent);
    }

    @PostMapping("/ssps-check-send")
    public ResponseEntity<Map<String, String>> runSspsCheckSend(@RequestBody SshRequest requestSspsCheckSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestSspsCheckSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.runSspsCheckSend("10.168.106.102",
                requestSspsCheckSend.getStoreCode(), requestSspsCheckSend.getAllStreCodeFile());
        return ResponseEntity.ok(logContent);
    }

    @PostMapping("/rpm-send")
    public ResponseEntity<Map<String, String>> executeRpmSend(@RequestBody SshRequest requestRpmSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestRpmSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.executeRpmSend("10.168.106.102",
                requestRpmSend.getStoreCode(), requestRpmSend.getAllStreCodeFile());

                System.out.println("Log content:2222222222222222222222222222222 " + logContent);
        return ResponseEntity.ok(logContent);
    }

    @PostMapping("/rpm-script-send")
    public ResponseEntity<Map<String, String>> executeRpmScriptSend(@RequestBody SshRequest requestRpmScriptSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestRpmScriptSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.executeRpmScriptSend("10.168.106.102",
                requestRpmScriptSend.getScriptName(), requestRpmScriptSend.getStoreCode(),
                requestRpmScriptSend.getAllStreCodeFile());
        return ResponseEntity.ok(logContent);
    }

    @PostMapping("/rpm-check-send")
    public ResponseEntity<Map<String, String>> runRpmCheckSend(@RequestBody SshRequest requestRpmCheckSend) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestRpmCheckSend);
            System.out.println("request JSON = " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> logContent = sshService.runRpmCheckSend("10.168.106.102",
                requestRpmCheckSend.getStoreCode(), requestRpmCheckSend.getAllStreCodeFile());
        return ResponseEntity.ok(logContent);
    }

    @GetMapping("/browse-files")
    public Map<String, Object> browseFiles() {
        List<String> files = sshService.getRemoteFileList("10.168.106.102", "/ts2100/share/SA3000/retail_tool/");
        Map<String, Object> result = new HashMap<>();
        result.put("files", files);
        return result;
    }
}
