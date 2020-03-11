# 演習1

> [!NOTE]
> 本 Hands-On では予め AMQ Streams のアーカイブを配置済みです。
> AMQ Streams をご自身の環境にダウンロードするには [Red Hat Customer Portal](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=jboss.amq.streams) のサイトへログインする必要があります。


## インストール先へログイン {#login}

ホストから broker1 へ ログインします。この時、ユーザーは `vagrant` ユーザーとなります。
`Vagrantfile` が格納されているディレクトリに移動してください。

```bash
$ cd kafka-hands-on/vagrant
$ vagrant ssh broker1
```

> [!NOTE]
> ホストから broker1 へログインし、その後 kafka ユーザーへスイッチします。


## ユーザー、グループ、パスワードの作成 {#user}

Kafka Broker / Zookeeper を起動するための `user`、`group`、`password` を作成します。

| ユーザー | グループ | パスワード |
|:--|:--|:--|
|kafka|kafka|kafka|

```bash
$ sudo groupadd kafka
$ sudo useradd -g kafka kafka
$ echo kafka | sudo passwd --stdin kafka
```

```bashs
Changing password for user kafka.
passwd: all authentication tokens updated successfully.
```

## インストール用ディレクトリの作成 {#directory}

AMQ Streams をインストール先のディレクトリを作成します。

```bash
$ sudo mkdir -p /opt/kafka
```

## アーカイブの解凍 {#unzip}

一時ディレクトリ `/tmp/kafka` を作成し、そのディレクトリに `unzip` コマンドで解凍した結果を格納します。

```bash
$ sudo mkdir -p /tmp/kafka
$ sudo unzip /root/amq-streams-1.3.0-bin.zip -d /tmp/kafka
```

> [!NOTE]
> AMQ Streams は `/root/amq-streams-1.3.0-bin.zip` へ配置済みです。

## AMQ Streams のインストール {#install}

AMQ Streams インストール用ディレクトリに `mv` コマンドで解凍結果を格納します。

```bash
$ sudo mv -n /tmp/kafka/kafka_2.12-2.3.0.redhat-00003/* /opt/kafka
$ sudo rm -rf /tmp/kafka
```

## 所有者の変更 {#chown}

`/opt/kafka` ディレクトリを `kafka:kafka` へ所有者を変更します。

```bash
$ sudo chown -R kafka:kafka /opt/kafka
```

## zookeeper 用ディレクトリの作成 {#zookeeper}

Zookeeper が起動する際にデータを格納するためのディレクトリ `/var/lib/zookeeper` を作成し、所有者を `kafka:kafka` に変更します。

```bash
$ sudo mkdir -p /var/lib/zookeeper
$ sudo chown -R kafka:kafka /var/lib/zookeeper
```

> [!WARNING] 
> この作業は Zookeeper を構築する Node でのみ必要な作業です。Hands-On では Broker1 と Zookeeper が共存するため実施する必要がありますが、Kafka Broker と Zookeeper が異なる Node で動作し、かつ Kafka Broker の設定時には本作業の実施は必要ありません。

## Kafka Broker 用ディレクトリの作成 {#broker}

```bash
sudo mkdir -p /var/lib/kafka
sudo chown -R kafka:kafka /var/lib/kafka
```

> [!WARNING]
> この作業は Kafka Broker を構築する Node でのみ必要な作業です。Hands-On では Broker1 と Zookeeper が共存するため実施する必要があります。

これでインストールは完了です。

