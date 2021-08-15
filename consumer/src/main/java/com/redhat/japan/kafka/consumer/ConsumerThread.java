package com.redhat.japan.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerThread implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private KafkaConsumer<String, String> kafkaConsumer;
    private String consumerGroupName;
    private String consumerName;
    private String topic;

    public ConsumerThread(String consumerName, String consumerGroupName, String topic) {

        this.consumerName = consumerName;
        this.consumerGroupName = consumerGroupName;
        this.topic = topic;

        // プロパティの生成と設定
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.33.11:9092");//
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupName);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // Consumer の生成
        this.kafkaConsumer = new KafkaConsumer<>(properties);
    }

    public void run() {
        // スレッドを トピック に割り当てる
        kafkaConsumer.subscribe(Collections.singletonList(topic));
        try {
            while (!closed.get()) {
                // polling
                ConsumerRecords<String, String> consumerRecord = kafkaConsumer.poll(Duration.ofMillis(1_000));

                consumerRecord.forEach(record -> {
                    System.out.println("group: " + consumerGroupName +
                            ", consumer: " + consumerName +
                            ", partition: " + record.partition() +
                            ", topic: " + record.topic() +
                            ", offset: " + record.offset() +
                            ", key: " + record.key() +
                            ", value: " + record.value()
                    );
                });
            }
        } catch (Exception e) {
            if (!closed.get()) {
                throw e;
            }
        } finally {
            kafkaConsumer.close();
        }

    }

/*    public void shutdown() {
        closed.set(true);
        this.kafkaConsumer.wakeup();
    }*/
}

