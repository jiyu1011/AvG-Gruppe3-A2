package com.acme;

import org.eclipse.paho.client.mqttv3.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Locale;
import java.util.Random;

@SpringBootApplication
public class TemperatureSensor implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(TemperatureSensor.class);

    @Value("${roomId:}")
    private String roomId;

    public static void main(String[] args) {
        SpringApplication.run(TemperatureSensor.class, args);
    }

    @Override
    public void run(String... args) {
        String topic = "home/temperature_control";  // 公共主题
        String broker = "tcp://localhost:1883";
        String clientId = "TemperatureSensor-" + roomId;
        Random random = new Random();

        try {
            MqttClient client = new MqttClient(broker, clientId);
            client.connect();
            logger.info("Temperature sensor for " + roomId + " connected to MQTT broker");

            while (true) {
                // 生成 15 到 30 之间的随机温度值
                double temperature = 15 + (30 - 15) * random.nextDouble();

                // 创建包含 roomId 和 temperature 的 JSON 格式消息
                String content = String.format(Locale.US, "{\"roomId\":\"%s\", \"temperature\":%.2f}", roomId, temperature);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(1);

                // 发布消息到公共主题
                client.publish(topic, message);
                logger.info("Published temperature for room " + roomId + ": " + content);

                // 每 5 秒发送一次消息
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            logger.error("Temperature sensor for room " + roomId + " encountered an error", e);
        }
    }
}
