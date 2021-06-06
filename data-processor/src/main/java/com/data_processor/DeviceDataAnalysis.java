package com.data_processor;

import com.data_processor.sources.MQTTSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Optional;

public class DeviceDataAnalysis {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        final MQTTSource mqttSource = new MQTTSource();
        final DataStreamSource<String> tempratureDataSource = env.addSource(mqttSource);
        final DataStream<String> stream = tempratureDataSource.map((MapFunction<String, String>) data -> data);

        stream.print();
        stream
            .addSink((SinkFunction<String>) value -> {
                final String websocketUrl = Optional
                        .ofNullable(System.getenv("WEBSOCKET_URL"))
                        .orElse("ws://localhost:3000");
                final WebSocketClient client = new EmptyClient(new URI(websocketUrl));
                client.connectBlocking();
                client.send(value);
            });

        env.execute("Device data analysis");
    }
}