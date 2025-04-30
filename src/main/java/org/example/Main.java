package org.example;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Enable detailed HTTP client logging
            System.setProperty("jdk.httpclient.HttpClient.log", "all");
            System.setProperty("jdk.httpclient.HttpClient.log.level", "ALL");
            System.setProperty("jdk.httpclient.HttpClient.log.headers", "true");
            System.setProperty("jdk.httpclient.HttpClient.log.body", "true");
            
            logger.debug("Initializing MCP client transport");
            
            // Create HTTP client with logging
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            McpClientTransport transport = HttpClientSseClientTransport.builder("http://localhost:9200/_plugins/_ml/")
            .sseEndpoint("sse")
                    .customizeClient(clientBuilder -> {
                        clientBuilder.connectTimeout(Duration.ofSeconds(10));
                    }).build();
            
            logger.debug("Building MCP sync client");
            McpSyncClient client = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(10))
                    .capabilities(McpSchema.ClientCapabilities.builder()
                            .roots(true)
                            .sampling()
                            .build())
                    .build();
            logger.info("Starting MCP client");

            logger.debug("Initializing client");
            McpSchema.InitializeResult initResult = client.initialize();
            logger.info("Initialized: {}", initResult);

            logger.debug("Listing available tools");
            McpSchema.ListToolsResult tools = client.listTools();
            logger.info("Available tools: {}", tools);

            logger.info("Calling ListIndexTool");
            McpSchema.CallToolResult result = client.callTool(
                    new McpSchema.CallToolRequest("ListIndexTool",
                            Map.of("input", Map.of("index", List.of("test"))))
            );
            logger.info("Tool call result: {}", result);

            logger.debug("Closing client gracefully");
            client.closeGracefully();
        } catch (Exception e) {
            logger.error("Error occurred during execution", e);
        }
    }
}
