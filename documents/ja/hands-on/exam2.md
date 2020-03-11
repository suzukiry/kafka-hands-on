# 演習2


## broker1 へのログイン {#login}

Zookeeper を起動する VM broker1 へログインします。

```bash
$ vagrant ssh broker1
```

## kafka ユーザーへのスイッチ {#switch}

Zookeeper を起動するためのユーザー `kafka:kafka` へスイッチします。

```bash
$ su - kafka
Password:
```

> [!NOTE]
> 以降、kafka の操作は `kafka` ユーザーで実施します。
> パスワードは `kafka` です。

## myid の設定 {#myid}

演習1 で作成した `/var/lib/zookeeper` ディレクトリに `myid` ファイルを作成し、起動する Zookeeper の id を設定します。

```bash
$ echo "1" > /var/lib/zookeeper/myid
```

> [!TIP]
> `/var/lib/zookeeper/myid` へ `1` を設定します。Zookeeper を Multi Node 設定にする場合は、異なる値をそれぞれの `myid` に設定します。
>
> 今回 Zookeeper は Single Node 構成なので、1回しか設定しません。

## zookeeper.properties の設定 {#zookeeper}

`/opt/kafka/config/zookeeper.properties` を編集して値を変更します。

```bash
$ vi /opt/kafka/config/zookeeper.properties
```
```bash
dataDir=/var/lib/zookeeper
timeTick=2000
initLimit=5
syncLimit=2
server.1=192.168.33.11:2888:3888
```

> [!WARNING] 
> 本番環境では可用性の観点から複数台(奇数台)の Zookeeper を起動してください。下記設定項目を参考にしてください。

| 設定値 | 意味 |
|:--|:--|
| initLimit | Zookeeper サーバーは起動後リーダーに接続するまでに initLimit で指定した時間内に接続する必要があります。timeTick × initLimit が実際の時間になります。今回の設定では 2000ms × 5 tick = 10 秒です。|
|syncLimit|Zookeeper サーバーのデータがリーダーのデータよりどれくらい古くても良いかを設定します。timeTick * syncLimit が実際の時間になります。|
| tickTime | initLimit/syncLimit の基準値。ミリ秒単位で指定します。 |
| server.x | Zookeeper が複数台存在する場合は、server.2 や server.3 といった具合に設定を増やします。|


## Zookeeper の起動 {#exec}

Zookeeper を起動します。

```bash
$ /opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties 
```

> [!TIP]
> `-daemon` オプションを付与すると、バックグラウンドで zookeeper が起動します。

Zookeeper が起動したかどうかを確認します。

```bash
$ jcmd | grep zookeeper
```
```bash
7116 org.apache.zookeeper.server.quorum.QuorumPeerMain /opt/kafka/config/zookeeper.properties
```

以上で Zookeeper の設定と起動は完了です。