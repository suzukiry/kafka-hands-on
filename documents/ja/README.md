# OS と Middleware

| 概要 | 説明 | ダウンロード |
|:--|:--|:--|
| OS | Red Hat Enterprise Linux(RHEL) 8.0 | [RHELの入手](https://sso.redhat.com/auth/realms/redhat-external/protocol/saml?SAMLRequest=fVLLasMwEPwV33RSZDvPCttgEgqBtJSk7aGXoshrIpAlVys3ab%2B%2BskPa9BLQRbszs7MjZSga3fKy8wezhY8O0EclIjivrFlag10DbgfuU0l42W5ycvC%2BRc6YkBIQRw6qg%2FAjaRtW2aPRVlTIek1WKyO0%2BgYSrYJouPSKf3xEe00WwQBzIHSD7FymcPLgggZrnfVWWj3okmi9ysl7PBmn4dS0ntU1nSxkSsXkTtLZdHY3j5PFot7vAxSxg7VBL4zPSRqnMY0TOo6f44RP5zxZvJHoFRwOztJRTKJTow3yflBOOme4FaiQG9EAci%2F5rnzY8ADk4pLRNaW9zbnsQYqsR%2FPBnSs6o2oFFf3NL2PX7ez8Qo9Bbr16slrJr6jU2h6XIS4POfGuCyHfW9cIf9tAX1EVrQcob%2FvF0YPxhBXnmf8%2FQvED&RelayState=https%3A%2F%2Faccess.redhat.com%2Fdownloads%2Fcontent%2F479&SigAlg=http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23rsa-sha1&Signature=SfF1BaU99nQ0V%2BDUpkVyiJSRkTqOsIip31xMMk7wHQYyPVio5XyDn4Rg9zbhYYLIyph8Y28eGiOcAcoBJwuMOkk%2BpngBYs%2Bu1IDXutGaeWfwd6RT3yLPYLm4rq6ebUThyR1dCokcQl7wQgVuPvSTUU5UXbGG3MCKWpZAj%2F2H80FMPpaRxoIsrcu6Jt4ObB%2BXskYg9DACuxvlSl%2FF6UxTz4XaCT7o8mRTsoHxEr1TWUTEzQJy1S7aq5G17M8FD8ITD%2B6Ank0y82Jm96eTBMqFgE1UvfJUdiU%2FFvo3%2FFpNGy9WuNhOFUu4jQwCmFz69FGEyeqva7nUfFkil%2FsbsP7KvPCjDN%2BndobuZBbmCPQEUZoD%2BqVFmkKz9owAjzLTp3vrPOD0rM1gmMN1FXnLuRJVpzKYs%2BJ%2BILl992bBRO9WUzyVjdj%2BrwE%2FjYyVpcvLZTdKQyb5aEcju%2FcVs02oI7Q9JHGJHDBA%2Fj9qxvjiqygeQLAffkGOEZ0xTrS668PLv21KgM%2FUwJPbF5pNQfn9Mn7Tj7%2BfEZToucD6y3k5OAX3EvpGtrNI%2Fy2BiM8%2BikRh3O2NAjQEPxGTu2Gl2F7E00Cdq30QkN23GCGIeXgBT2%2BD38z0tragR0RdaQXY6QqMEgLhN4Q9os55rqnicOLzAL92Rds0O4pJv5mjI2SNd4xIzDE%3D)  |
| Java | OpenJDK 11 | yum コマンドでインストール(※[参考](https://developers.redhat.com/blog/2018/12/10/install-java-rhel8/)) | 
| AMQ Streams | AMQ Streams 1.3 | [AMQ Streams を入手](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=jboss.amq.streams) |

## Kafka Broker & Zookeeper の構成

### Kafka Broker 3台を別々のノードで実行する場合

| Vagrant ノード名 | ノード | 内部ネットワーク(eth1) |
|:--|:--|:--|:--|
| ansible | ansible | 192.168.33.31 |
| node1 | Kafka Broker 1 & Zookeeper | 192.168.33.11 |
| node2 | Kafka Broker 2 | 192.168.33.12 |
| node3 | Kafka Broker 3 | 192.168.33.13 |

> [!NOTE]
>  broker2、broker3 は AMQ Streams のインストール、設定作業が既に完了した状態になっています。

### Kafka Broker 3台を同一ノードで実行する場合

| Vagrant ノード名 | ノード | 内部ネットワーク(eth1) |
|:--|:--|:--|:--|
| ansible | ansible | 192.168.33.31 |
| node1 | Kafka Broker 1, Kafka Broker 2, Kafka Broker 3 & Zookeeper | 192.168.33.11 |

### 各マシンへのログイン方法

ホスト(Windows or Red Hat Enterprise Linux) から `vagrant ssh` コマンドを実行して RHEL にログインします。

> [!NOTE]
> broker1 にログインする場合
> ```bash
> $ vagrant ssh broker1
> ```
> - ssh の引数は上記表の <b>Vagrant ノード名</b> になります。
> - vagrant で初期作成された `vagrant` ユーザーでログインします。
> - kafka 関連のコマンドを使用する場合は `kafka` ユーザーにスイッチ。
>   ```bash
>   $ su kafka
>   Password:
>   ```

## 今回のハンズオンで触れていない項目
### firewall
本ハンズオン構成では、ansible を利用して、以下のポートの穴あけを自動で実施済みです。実際は Kafka Brokers で設定する listenrs のポート番号などによって変わってくるため、適宜変更してください。

| ポート番号 | 用途 |
|:--|:--|
| 9092 | Broker が使用する listener が使用 |
| 2181 | Zookeeper が使用 |
| 2888 | Zookeeper が使用 |
| 3888 | Zookeeper が使用 |


例)
```bash
# firewall-cmd --add-port=9092/tcp --zone=public --permanent
# systemctl restart firewalld
```

### ストレージ
Kafka Broker はローカルのストレージを利用するのが最もパフォーマンスを出すことができます。Kafka Broker はシーケンシャルな Write/Read を実施するために HDD でも問題ありませんが、Zookeeper はランダム Write/Read であるため SSD を推奨します。
今回は、VirtualBox で割り当てたローカルストレージをそのまま利用しています。

### 複数 NIC 使用時の構成
NIC が一枚しかない環境では Kafka Brokers の listeners にはホストの設定を省略して記載することができます。

```
listeners=PLAINTEXT://:9092
```
今回の構成では、各 VM は NIC 2枚構成になっています。
1. eth0: ホストと VM 間を結ぶ NIC
    - 主に vagrant ssh でログインする際に使用されます。
1. eth1: VM 間のネットワーク
    - VirtualBox によって kafka 内部ネットワーク(192.168.33.0/24)が割り振られています。 
    - Kafka で使用するネットワークはこちらの eth1 なので明示的に listeners を IP で指定しています。