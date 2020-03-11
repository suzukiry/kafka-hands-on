package com.redhat.japan.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;
import java.util.stream.Stream;

public class Consumer {

    public static void main(String[] args) {

        // プロパティの生成と設定
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.33.11:9092");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // Consumer の生成
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

        Stream.of("Consumer1", "Consumer2", "Consumer3", "Consumer4", "Consumer5")
                .forEach(consumer -> {
                    Thread thread = new Thread(new ConsumerThread(kafkaConsumer, consumer));
                    thread.start();
                });

    }

}
