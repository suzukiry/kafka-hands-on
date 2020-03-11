package com.redhat.japan.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerThread implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private KafkaConsumer<String, String> kafkaConsumer;

    private String consumer;

    /**
     * コンストラクタ
     *
     * @param kafkaConsumer
     */
    public ConsumerThread(KafkaConsumer<String, String> kafkaConsumer, String consumerName) {
        this.kafkaConsumer = kafkaConsumer;
        this.consumer = consumerName;
    }

    public void run() {
        // スレッドを mytopic に割り当てるß
        synchronized (this.consumer) {
            this.kafkaConsumer.subscribe(Arrays.asList("mytopic"));
        }

        while (!closed.get()) {
            // polling
            try {
                ConsumerRecords<String, String> consumerRecord = this.kafkaConsumer.poll(Duration.ofMillis(1_000));

                consumerRecord.forEach(record -> {
                    System.out.println("group: my-consumer" +
                            ", consumer: " + this.consumer +
                            ", partition: " + record.partition() +
                            ", topic: " + record.topic() +
                            ", key: " + record.key() +
                            ", value: " + record.value()
                    );
                });
            } catch (Exception e) {
                if (!closed.get()) {
                    throw e;
                }
            } finally {
                this.kafkaConsumer.close();
            }

        }

    }

    public void shutdown() {
        closed.set(true);
        this.kafkaConsumer.wakeup();
    }

}
