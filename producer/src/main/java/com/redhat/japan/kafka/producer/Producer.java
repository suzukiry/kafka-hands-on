package com.redhat.japan.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Producer {

    public static void main(String[] args) {
        // Properties の生成
        Properties properties = new Properties();

        // Kafka Broker の設定
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.33.11:9092");//

        // StringSerializer の設定
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // KafkaProducer の生成
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // 同期送信
        Stream.of("message1", "message2", "message3", "message4", "mesasge5", "message6", "mesasge7", "message8")
                .forEach(i -> {
                    try {
                        // Record の生成(キーなしでレコードを生成)
                        // 第一引数は topic 名
                        // 第二引数は メッセージ
                        ProducerRecord<String, String> records = new ProducerRecord<>("mytopic", i);
                        Future<RecordMetadata> future = producer.send(records);
                        RecordMetadata recordMetadata = future.get();

                        System.out.println("=====================================================");
                        System.out.println(LocalDateTime.now());
                        System.out.println("      value : " + i);
                        System.out.println("  partition : " + recordMetadata.partition());
                        System.out.println("     offset : " + recordMetadata.offset());

                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });

    }

}
