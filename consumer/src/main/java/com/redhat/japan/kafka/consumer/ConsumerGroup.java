package com.redhat.japan.kafka.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ConsumerGroup {

    public static void main(String[] args) {

        String consumerGroupName = "groupid";
        String topic = "mytopic";
        String[] consumerList = {"Consumer1", "Consumer2", "Consumer3", "Consumer4"};
        int numConsumers = consumerList.length;
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        // 同期送信
        Stream.of(consumerList)
                .forEach(consumer -> {
                    ConsumerThread consumerThread = new ConsumerThread(consumer,consumerGroupName,topic);
                    executor.submit(consumerThread);
                });
    }
}
