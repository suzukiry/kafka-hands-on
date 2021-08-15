# 演習6. Producer と Consumer の開発


Producer と Consumer のクライアントを開発していきます。
今回の演習で使うための新しいトピックを、最初に作成しておきます。Partitionを4つとした新しいtesttopicを作成し、このトピックに対して開発する Producer と Consumer を使って、メッセージの送受信を行います。

```bash
$ /opt/kafka/bin/kafka-topics.sh --alter --topic mytopic --partitions 4 --bootstrap-server 192.168.33.11:2181
```

## Producer の開発 {#login}

以下のコマンドを実行して Kafka Producer のソースコードを clone します。

```bash
$ git clone https://github.com/k-kosugi/kafka-hands-on
```

### Producer のコード確認 {#code}

ダウンロードしたレポジトリの、/producer/src/main/java/com/redhat/japan/kafka/producer ディレクトリにProducerのコードが保存されています。今回は、IDEから実行していきますので、お使いのIDEからダウンロードしたProducerのファイルを閲覧してみてください。

```java
package com.redhat.japan.kafka.client;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Producer {

    public static void main(String[] args) {

        // Properties の生成
        Properties properties = new Properties();

        // Kafka Broker の設定
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.33.11:9092");

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
                        ProducerRecord<String, String> records = new ProducerRecord<>("testtopic", i);
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

```

### Producer のコード実行

IDE、または.jarファイルを作成してコマンドラインから実行します。実行すると以下のようなログが出力されます。

```
=====================================================
2021-08-13T15:23:55.370
      value : message1
  partition : 1
     offset : 0
=====================================================
2021-08-13T15:23:57.396
      value : message2
  partition : 0
     offset : 0
=====================================================
2021-08-13T15:23:59.423
      value : message3
  partition : 2
     offset : 0
=====================================================
2021-08-13T15:24:01.433
      value : message4
  partition : 1
     offset : 1
=====================================================
2021-08-13T15:24:03.445
      value : messasge5
  partition : 0
     offset : 1
=====================================================
2021-08-13T15:24:05.459
      value : message6
  partition : 2
     offset : 1
=====================================================
2021-08-13T15:24:07.472
      value : messasge7
  partition : 1
     offset : 2
=====================================================
2021-08-13T15:24:09.485
      value : message8
  partition : 0
     offset : 2

Process finished with exit code 0
```
このログからpartition がローテーションされていることがわかります。これはパーティションの割り当てと呼ばれ、KafkaではまずProducerがBrokerにどのようにパーティションを割り当てるかを決定します。 割り当てられた後は、offsetの番号は通常通りインクリメントされていきます。

Producerクライアントによるパーティションの割り当て方法は、デフォルトではラウンドロビンでのメッセージ送信となります。その他、Producer クライアントから、以下のような方法でメッセージを送信することができます。

## Consumer の開発

それではtesttopicへ８つのmessageを送付しましたので、Consumerクライアントを使ってメッセージの受信をしていきます。

### Consumer のコード確認
本演習の初回でダウンロードしたレポジトリの、/consumer/src/main/java/com/redhat/japan/kafka/consumer ディレクトリにConsumerのコードを保存しています。

Kafka　Consumerは、ConsumerとConsumerグループで構成されています。ConsumerはConsumerグループに属していて、各Consumerは購読するトピックのパーティションを担当しています。

また万が一Consumerが障害でシャットダウンなどに止まった場合にも、グループ内のConsumer同士で担当するパーティションのオーナーシップを引き継ぐことができるため、可用性を持つことができます。この仕組みがリバランスと言われています。

![](../../images/exercise-6-consumer1.png)

図１や図２のようにConsumerを増やすことで購読するトピックのパーティションを共有できます。トピックのメッセージの消費をスケールするには、パーティションと同数のConsumerを持つことです。図３は４つのConsumerにそれぞれ一つのパーティションが割り当てられている例です。
そこで、ここでは図３のようなConsumer、Consumerグループを開発していきます。Consumerをマルチスレッドで作るため、複数スレッドとしてKafka Consumerを呼び出すConsumerGroup.javaと、実際にConsumerタスクを生成するConsumerThread.javaの二つを利用します。

ConsumerGroup.java
```java

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ConsumerGroup {

    public static void main(String[] args) {

        String consumerGroupName = "groupid";
        String topic = "mytopic";
        String[] consumerList = {"Consumer1", "Consumer2", "Consumer3"};
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
```


ConsumerThread.java
```java
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
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
}
```

### Consumer のコード実行 
IDEまたは.jarファイルから実行します。

```
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 26, key: null, value: message1
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 26, key: null, value: message2
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 28, key: null, value: message3
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 27, key: null, value: message4
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 27, key: null, value: message5
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 29, key: null, value: message6
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 28, key: null, value: message7
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 28, key: null, value: message8
```

Consumer1にはpartition: 0、Consumer2にはpartition: 1、Consumer3にはpartition: 2が割り当てられており、トピックの３つのパーティションにそれぞれConsumerが割り当てられているのがわかります。

### パーティション以上のConsumer のケース
それでは、パーティション以上のConsumerが生成された場合も見ておきましょう。

![](../../images/exercise-6-consumer2.png)

Kafka Consumerでは、トピックのパーティション数以上にConsumerを追加してもパーティションはアサインされません。余ったConsumerはアイドル状態になるため、ちゃんとパーティション数を確認の上、Consumer数は設計することが必要です。

ConsumerGroupWithFiveConsumers.java
```java

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ConsumerGroupWithFiveConsumers {

    public static void main(String[] args) {

        String consumerGroupName = "groupid";
        String topic = "mytopic";

        // Consumerの数を5つに増やしました
        String[] consumerList = {"Consumer1", "Consumer2", "Consumer3", "Consumer4", "Consumer5"};
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
```

実際に実行してみましょう。同様に、IDEまたは.jarファイルから実行します。

```
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 26, key: null, value: message1
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 26, key: null, value: message2
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 28, key: null, value: message3
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 27, key: null, value: message4
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 27, key: null, value: message5
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 29, key: null, value: message6
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 28, key: null, value: message7
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 28, key: null, value: message8
```

結果の中には、Consumer5が一度も登場してきていません。このように、パーティション数以上のConsumerを追加しても、パーティションよりもConsumerの方が多い場合には、メッセージを受け取ってくれないConsumerが出てきしまいますので、避けましょう。

### Consumerグループ追加 のケース
最後は別のConsumerグループを追加したケースを試していきます。AMQ Stream(Apache Kafka) では、メッセージを消費するアプリケーションが増えた場合にも柔軟に対応するができます。

![](../../images/exercise-6-consumer3.png)

ここでは異なるConsumerグループを作成する、AnotherConsumerGroup.javaを利用して実際に動かしてみます。


AnotherConsumerGroup.java
```java

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class AnotherConsumerGroup {

    public static void main(String[] args) {

        String consumerGroupName = "anothergroupid";
        String topic = "mytopic";

        // Consumerの数を5つに増やしました
        String[] consumerList = {"AnotherConsumer1", "AnotherConsumer2"};
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
```

こちらも実際に実行してみましょう。同様に、IDEまたは.jarファイルから実行します。


ConsumerGroup.javaの結果
```
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 26, key: null, value: message1
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 26, key: null, value: message2
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 28, key: null, value: message3
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 27, key: null, value: message4
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 27, key: null, value: message5
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 29, key: null, value: message6
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 28, key: null, value: message7
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 28, key: null, value: message8
```

AnotherConsumerGroup.javaの結果
```
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 26, key: null, value: message1
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 26, key: null, value: message2
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 28, key: null, value: message3
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 27, key: null, value: message4
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 27, key: null, value: message5
group: groupid, consumer: Consumer1, partition: 0, topic: mytopic, offset: 29, key: null, value: message6
group: groupid, consumer: Consumer3, partition: 2, topic: mytopic, offset: 28, key: null, value: message7
group: groupid, consumer: Consumer2, partition: 1, topic: mytopic, offset: 28, key: null, value: message8
```

Consumer Group



以上で Producer と Consumer の開発 (基礎編)　は終了です。

