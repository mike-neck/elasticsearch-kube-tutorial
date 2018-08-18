elasticsearch 入門のメモ

elasticsearch のインストール
---

elasticsearch の docker イメージをインストールする。

```
$ docker pull docker.elastic.co/elasticsearch/elasticsearch:6.3.2
```

これを起動する際は次のコマンドで起動する(ローカルで使うだけ)。

```
docker run \
  --rm -d \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  docker.elastic.co/elasticsearch/elasticsearch:6.3.2
```

参照 : https://www.elastic.co/guide/en/elasticsearch/reference/6.3/docker.html

起動したかテストを行う

```
$ -i curl https://localhost:9200
HTTP/1.1 200 OK
content-type: application/json; charset=UTF-8
content-length: 494

{
  "name" : "dECbAFj",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "dIiqDdfiSDK-s3NZ-1AIZw",
  "version" : {
    "number" : "6.3.2",
    "build_flavor" : "default",
    "build_type" : "tar",
    "build_hash" : "053779d",
    "build_date" : "2018-07-20T05:20:23.451332Z",
    "build_snapshot" : false,
    "lucene_version" : "7.3.1",
    "minimum_wire_compatibility_version" : "5.6.0",
    "minimum_index_compatibility_version" : "5.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

確認できたら、一度 elasticsearch を落としておく。

---

logstash のインストール
---

elasticsearch にデータを流し込むのに logstash を使う。
こちらもバイナリーを持ちたくないので docker を使う。

```
$ docker pull docker.elastic.co/logstash/logstash:6.3.2
```

一旦こちらを起動してみる。

```
$ docker run -it --rm \
  docker.elastic.co/logstash/logstash:6.3.2 \
  bin/logstash -e 'input { stdin { } } output { stdout {} }'
```

起動に少々時間がかかるが、次のログが出力されていれば起動完了している。

```
[2018-08-11T13:21:48,356][INFO ][logstash.agent           ] Successfully started Logstash API endpoint {:port=>9600}
```

なお、 docker イメージから起動した場合、 elasticsearch を `elasticsearch` という名前で
ネットワークから探そうとして失敗するため、えらーのログが大量に出てくる。

とりあえず、ここでは気にせずに 適当に文字列を入力してみると、 elasticsearch に送り込むと思しき json が出力される

```
foo
{
       "message" => "foo",
          "host" => "2febc9a88d9a",
    "@timestamp" => 2018-08-11T13:22:10.747Z,
      "@version" => "1"
}
```

`Ctrl + C` を押して一旦 logstash を止めておく。

---

filebeat のインストール
---

logstash の getting started を読んでいると、途中から filebeat が出てくるので、これもインストールする。
これもまたローカルにバイナリーを持ちたくないので docker を使う。

```
$ docker pull docker.elastic.co/beats/filebeat:6.3.2
```

filebeat を docker で立ち上げるのは少しむずかしい。というのも、 logstash の存在を前提にしているためである。
サンプルとなる設定ファイルを見ると、 `output.logstash` というフィールドがあって、 docker での起動を考えさせられる。

```yaml
filebeat.prospectors:
  - type: log
    paths:
      - /path/to/file/logstash-tutorial.log
output.logstash:
  hosts: ["localhost:5044"]
```

引用: https://www.elastic.co/guide/en/logstash/current/advanced-pipeline.html

docker で立ち上げつつ、 logstash のサービス解決も可能にするため、 minikube で立ち上げてみることにする

#### logstash の設定

まず logstash の設定。 beats から読み込むので次のような設定になる模様(https://www.elastic.co/guide/en/logstash/current/plugins-inputs-beats.html)

```
input {
  beats {
    port => 5044
  }
}
output {
  stdout { codec => rubydebug }
}
```

これを `logstash.config` という名前のファイルに保存してから、 `logstash-config` という名前の ConfigMap を作る

```
$ kubectl create cm logstash-config --from-file=logstash.config
```

##### 確認

```
$ kubectl get cm
NAME              DATA      AGE
logstash-config   1         1m
```

#### filebeats の設定

次に filebeats の設定を作る。

先程の filebeat の設定を少し書き換えて `filebeat-config.yml` ファイルに保存する。

```yaml
filebeat.prospectors:
  - type: log
    paths:
      - /logfile/apache.log
output.logstash:
  hosts: ["logstash:5044"]
```

そして、 `filebeat-config` という ConfigMap を作る

```
$ kubectl create cm filebeat-config --from-file=filebeat-config.yml
```

`apache.log` ファイルについては、 Volume で filebeat コンテナに載せる

### コンテナの立ち上げ

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: logstash
spec:
  replicas: 1
  selector:
    matchLabels:
      app: logstash
  template:
    metadata:
      labels:
        app: logstash
    spec:
      containers:
      - image: docker.elastic.co/logstash/logstash:6.3.2
        name: logstash
        ports:
        - containerPort: 5044
          name: lumberjack
          protocol: TCP
        volumeMounts:
        - name: logstash-config-volume
          mountPath: /config
        command: [/usr/share/logstash/bin/logstash, -f, /config/logstash.config]
      volumes:
      - name: logstash-config-volume
        configMap:
          name: logstash-config
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: filebeat
spec:
  replicas: 1
  selector:
    matchLabels:
      app: filebeat
  template:
    metadata:
      labels:
        app: filebeat
    spec:
      containers:
      - name: filebeat
        image: docker.elastic.co/beats/filebeat:6.3.2
        volumeMounts:
        - name: filebeat-config-volume
          mountPath: /config
        - name: log-files
          mountPath: /logfile
        command: [/usr/share/filebeat/filebeat, -c, /config/filebeat-config.yml, -d, "publish"]
      volumes:
      - name: filebeat-config-volume
        configMap:
          name: filebeat-config
      - name: log-files
        hostPath:
          path: /path/to/log
          type: Directory
---
apiVersion: v1
kind: Service
metadata:
  name: logstash
spec:
  ports:
  - port: 5044
  selector:
    app: logstash
```

このファイルを `filebeat-logstash.yml` としておき、次のコマンドでこのディレクトリーで使えるようにする

```shellsession
$ sed -e "s+/path/to/log+`pwd`/log-files+g" filebeat-logstash.yml > filebeat-logstash-service.yml
```

