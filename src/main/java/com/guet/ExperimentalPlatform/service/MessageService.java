package com.guet.ExperimentalPlatform.service;


import com.guet.ExperimentalPlatform.pojo.TransmissionInfo;

import java.util.concurrent.ConcurrentHashMap;

public interface MessageService {

    void saveMessage(String messageText, long userId, long toUserId,
                     ConcurrentHashMap<String, TransmissionInfo> transmissionId);

}
