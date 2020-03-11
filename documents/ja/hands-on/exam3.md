# 演習3. Kafka Broker の設定と起動

以下のドキュメントを参考にしてください。

- [Red Hat 公式ドキュメント](https://access.redhat.com/documentation/en-us/red_hat_amq/7.5/html/using_amq_streams_on_rhel/assembly-getting-started-str#file_systems)
- [Apache Kafka 公式ドキュメント]((https://access.redhat.com/documentation/en-us/red_hat_amq/7.5/html/using_amq_streams_on_rhel/configuring_kafka#proc-running-multinode-kafka-cluster-str)

## broker1 へのログイン {#login}

Kafka Broker を起動する VM broker1 へログインします。

```bash
$ vagrant ssh broker1
```

## ファイルシステム の確認 {#file}

AMQ Streams が使用するファイルシステムは xfs を推奨します。ファイルシステムを確認します。
`df` コマンドを使用して、使用しているファイルシステムが `xfs` であることを確認します。

```bash
$ df -T
```
```bash
Filesystem                  Type     1K-blocks    Used Available Use% Mounted on
devtmpfs                    devtmpfs    404184       0    404184   0% /dev
tmpfs                       tmpfs       420552       0    420552   0% /dev/shm
tmpfs                       tmpfs       420552   10920    409632   3% /run
tmpfs                       tmpfs       420552       0    420552   0% /sys/fs/cgroup
/dev/mapper/rhel_rhel8-root xfs       30320164 2533056  27787108   9% /
/dev/sda1                   xfs        1038336  173608    864728  17% /boot
tmpfs                       tmpfs        84108       0     84108   0% /run/user/1000
```

あるいは `mount` コマンドを使用することでも `xfs` であることを確認できます。

```bash
$ mount
```
```bash
sysfs on /sys type sysfs (rw,nosuid,nodev,noexec,relatime,seclabel)
proc on /proc type proc (rw,nosuid,nodev,noexec,relatime)
devtmpfs on /dev type devtmpfs (rw,nosuid,seclabel,size=404184k,nr_inodes=101046,mode=755)
securityfs on /sys/kernel/security type securityfs (rw,nosuid,nodev,noexec,relatime)
tmpfs on /dev/shm type tmpfs (rw,nosuid,nodev,seclabel)
devpts on /dev/pts type devpts (rw,nosuid,noexec,relatime,seclabel,gid=5,mode=620,ptmxmode=000)
tmpfs on /run type tmpfs (rw,nosuid,nodev,seclabel,mode=755)
tmpfs on /sys/fs/cgroup type tmpfs (ro,nosuid,nodev,noexec,seclabel,mode=755)
cgroup on /sys/fs/cgroup/systemd type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,xattr,release_agent=/usr/lib/systemd/systemd-cgroups-agent,name=systemd)
pstore on /sys/fs/pstore type pstore (rw,nosuid,nodev,noexec,relatime,seclabel)
bpf on /sys/fs/bpf type bpf (rw,nosuid,nodev,noexec,relatime,mode=700)
cgroup on /sys/fs/cgroup/cpuset type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,cpuset)
cgroup on /sys/fs/cgroup/net_cls,net_prio type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,net_cls,net_prio)
cgroup on /sys/fs/cgroup/perf_event type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,perf_event)
cgroup on /sys/fs/cgroup/cpu,cpuacct type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,cpu,cpuacct)
cgroup on /sys/fs/cgroup/pids type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,pids)
cgroup on /sys/fs/cgroup/blkio type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,blkio)
cgroup on /sys/fs/cgroup/freezer type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,freezer)
cgroup on /sys/fs/cgroup/rdma type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,rdma)
cgroup on /sys/fs/cgroup/memory type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,memory)
cgroup on /sys/fs/cgroup/hugetlb type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,hugetlb)
cgroup on /sys/fs/cgroup/devices type cgroup (rw,nosuid,nodev,noexec,relatime,seclabel,devices)
configfs on /sys/kernel/config type configfs (rw,relatime)
/dev/mapper/rhel_rhel8-root on / type xfs (rw,relatime,seclabel,attr2,inode64,noquota)
selinuxfs on /sys/fs/selinux type selinuxfs (rw,relatime)
systemd-1 on /proc/sys/fs/binfmt_misc type autofs (rw,relatime,fd=38,pgrp=1,timeout=0,minproto=5,maxproto=5,direct,pipe_ino=17765)
debugfs on /sys/kernel/debug type debugfs (rw,relatime,seclabel)
mqueue on /dev/mqueue type mqueue (rw,relatime,seclabel)
hugetlbfs on /dev/hugepages type hugetlbfs (rw,relatime,seclabel,pagesize=2M)
/dev/sda1 on /boot type xfs (rw,relatime,seclabel,attr2,inode64,noquota)
tmpfs on /run/user/1000 type tmpfs (rw,nosuid,nodev,relatime,seclabel,size=84108k,mode=700,uid=1000,gid=1000)
```

## kafka ユーザーへのスイッチ {#switch}

Zookeeper を起動するためのユーザー `kafka:kafka` へスイッチします。

```bash
$ su - kafka
Password:
```

> [!NOTE]
> パスワードは `kafka` です。


## Kafka Broker の設定 {#config}
`/opt/kafka/config/server.properties` ファイルを編集して Broker1 の設定を行います。

```bash
$ vi /opt/kafka/config/server.properties
```

```bash
broker.id=0
listeners=PLAINTEXT://192.168.33.11:9092
log.dirs=/var/lib/kafka
zookeeper.connect=192.168.33.11:2181
delete.topic.enable=true
```


> [!NOTE]
> 今回のハンズオンでは Zookeeper と Kafka Broker1 が同居しているため、演習1で既にインストール作業は完了しています。
> Kafka Broker をインストールする場合は Zookeeper と同一の手順でインストールすることができます。

Hands-on で設定した項目の詳細は以下の通りです。

|値|型|説明|デフォルト|Mode|
|:--|:--:|:--|:--:|:--|
|broker.id| int | このサーバーの broker id。 設定されていない場合、一意の broker id が生成されます。zookeeper で生成された broker id とユーザーが設定した broker id との競合を避けるため、生成された broker id は reserved.broker.max.id + 1 から始まります。| -1 | read-only |
|listeners| string | listen する URI と listenr 名のコンマ区切りリスト。 listener 名がセキュリティプロトコルでない場合は、listener.security.protocol.map も設定する必要があります。ホスト名を0.0.0.0に指定して、すべてのインターフェイスにバインドします。ホスト名を空のままにして、デフォルトのインターフェースにバインドします。正当なリスナーリストの例：PLAINTEXT://myhost:9092、 SSL://:9091、 CLIENT://0.0.0.0:9092、REPLICATION://localhost:9093 | null | per-broker|
|log.dirs| string | ログデータが保持されるディレクトリ。設定されていない場合、log.dir の値が使用されます。| null | read-only |
|zookeeper.connect|string|ZooKeeper接続文字列を hostname:port の形式で指定します。host と port は、Zookeeper サーバーのホストとポートです。 Zookeeper マシンがダウンしているときに他の Zookeeper ノードを介して接続できるようにするには、hostname1:port1、hostname2:port2、hostname3:port3 の形式で複数のホストを指定することもできます。 サーバーは、Zookeeper 接続文字列の一部として Zookeeper chroot パスを持つこともできます。Zookeeper 接続文字列は、グローバルな Zookeeper 名前空間の何らかの path の下にデータを配置します。 たとえば、chroot path を /chroot/pathにするには、接続文字列を hostname1:port1,hostname2:port2,hostname3:port3/chroot/pathとして指定します。| | read-only|
|delete.topic.enable|boolean|トピックの削除を有効にします。この設定がオフになっている場合、管理ツールを使用してトピックを削除しても効果はありません。| true | read-only |


## Kafka Broker の起動 {#exec}

Terminal を他に二つ起動し、broker2、broker3 へもログインしておきます。

- Terminal2
    ``` bash
    $ vagrant ssh broker2
    $ su - kafka
    Password:
    ```
- Terminal3
    ```bash
    $ vagrant ssh broker3
    $ su - kafka
    Password:
    ```

> [!NOTE]
> broker2、broker3 の `kafka` ユーザーのパスワードは `kafka` です。


Broker1 ~ Broker3 までを順番に起動します。
```bash
$ /opt/kafka/bin/kafka-server/start.sh -daemon /opt/kafka/config/server.properties
```

Broker1 ~ ３ で Kafka Broker が起動したか確認します。
```bash
$ jcmd | grep kafka
```
```
9819 kafka.Kafka /opt/kafka/config/server.properties
```

> [!TIP]
> Kafka Broker の Heap Size を変更したい場合は、`KAFKA_HEAP_OPTS` 環境変数を設定する必要があります。
> ```bash
> export KAFKA_HEAP_OPTS="-Xms512m -Xmx512m"
> ```

上記で Kafka Broker の設定と起動は完了です。