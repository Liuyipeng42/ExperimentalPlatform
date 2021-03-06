package com.guet.ExperimentalPlatform.Service.impls;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.guet.ExperimentalPlatform.Utils.FileOperation;
import com.guet.ExperimentalPlatform.Entity.PORunCodesRecord;
import com.guet.ExperimentalPlatform.mapper.PORunCodesRecordMapper;
import com.guet.ExperimentalPlatform.pojo.ContainerInfo;
import com.guet.ExperimentalPlatform.Service.PaddingOracleService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PaddingOracleServiceImpl extends ServiceImpl<PORunCodesRecordMapper, PORunCodesRecord>
        implements PaddingOracleService {

    private final DockerClient client;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final ConcurrentHashMap<String, ContainerInfo> userIdContainer = new ConcurrentHashMap<>();

    public PaddingOracleServiceImpl(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://0.0.0.0:2375").build();
        client = DockerClientBuilder.getInstance(config).build();
    }

    public boolean createEnvironment(String userId, String imageName) {

        try {
            CreateContainerResponse container;

            System.out.println(userId);
            // 创建容器
            container = client.createContainerCmd(imageName)
                    .withName("container" + userId).exec();

            String containerId = container.getId();

            // 启动容器
            client.startContainerCmd(containerId).exec();

            String containerIP = client.inspectContainerCmd(container.getId()).exec()
                    .getNetworkSettings()
                    .getNetworks()
                    .get("bridge")
                    .getIpAddress();

            System.out.println(containerIP);

            userIdContainer.put(
                    userId,
                    new ContainerInfo()
                            .setContainerId(containerId)
                            .setContainerIP(containerIP)
            );

            // 复制文件
            copyCodes(userId);

        } catch (ConflictException e) {
            return true;
        }

        return true;
    }

    public void closeEnvironment(String userId){

        redisTemplate.opsForSet().remove("po:" + userId, 1, 2, 3, 4, 5);

        ContainerInfo containerInfo = userIdContainer.get(userId);
        if (containerInfo != null) {

            String containerId = containerInfo.getContainerId();
            // 关闭容器
            client.stopContainerCmd(containerId).exec();
            // 删除容器
            client.removeContainerCmd(containerId).exec();
            // 删除文件
            userIdContainer.remove(userId);
            new File("PaddingOracleFiles/ExperimentDataFile/" + userId + "_manual_attack.py").delete();
            new File("PaddingOracleFiles/ExperimentDataFile/" + userId + "_auto_attack.py").delete();
        }

    }

    public void copyCodes(String userId){
        FileOperation.copyAndReplace("PaddingOracleFiles/OriginalFiles/manual_attack.py",
                "PaddingOracleFiles/ExperimentDataFile/" + userId + "_manual_attack.py",
                "\"containerIP\"", "\"" + userIdContainer.get(userId).getContainerIP() +"\"");

        FileOperation.copyAndReplace("PaddingOracleFiles/OriginalFiles/auto_attack.py",
                "PaddingOracleFiles/ExperimentDataFile/" + userId + "_auto_attack.py",
                "\"containerIP\"", "\"" + userIdContainer.get(userId).getContainerIP() +"\"");
    }

}
