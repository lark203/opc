package com.atlantafx.util;

import com.atlantafx.core.config.ConfigStore;
import com.atlantafx.features.model.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(15))
            .executor(EXECUTOR)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final HttpClient HTTP_CLIENT_ASYNC = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(15))
            .executor(EXECUTOR)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static HttpRequest.Builder addAuthHeader(HttpRequest.Builder builder) {
        String token = ConfigStore.get("api_token");
        if (StringUtils.isNotBlank(token)) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder.header("Accept", "application/json");
    }

    private static <T> T parseResponse(HttpResponse<InputStream> response, JavaType targetType) throws IOException {
        int statusCode = response.statusCode();
        
        if (statusCode >= 500) {
            throw new IOException("服务器内部错误 (HTTP " + statusCode + ")");
        }
        
        if (statusCode >= 400) {
            throw new IOException("客户端请求错误 (HTTP " + statusCode + ")");
        }

        try (InputStream is = response.body()) {
            if (is == null) throw new IOException("响应体为空");
            
            Result<T> result = MAPPER.readValue(is, targetType);
            
            if (result == null) throw new IOException("JSON 解析失败");
            
            if (!result.isSuccess()) {
                throw new RuntimeException(result.msg());
            }
            return result.data();
        }
    }

    public static <T> T get(String url, TypeReference<T> typeRef) throws IOException, InterruptedException {
        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .GET()
                .build();

        JavaType type = MAPPER.getTypeFactory().constructParametricType(
                Result.class,
                MAPPER.getTypeFactory().constructType(typeRef)
        );

        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return parseResponse(response, type);
    }

    public static <T> T get(String url, Class<T> clazz) throws IOException, InterruptedException {
        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .GET()
                .build();

        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);

        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return parseResponse(response, type);
    }

    public static <T> CompletableFuture<T> getAsync(String url, Class<T> clazz) {
        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .GET()
                .build();

        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);

        return HTTP_CLIENT_ASYNC.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    try {
                        return parseResponse(response, type);
                    } catch (IOException e) {
                        throw new RuntimeException("异步请求失败", e);
                    }
                });
    }

    public static <T> T post(String url, Object body, Class<T> clazz) throws IOException, InterruptedException {
        String jsonBody = MAPPER.writeValueAsString(body);

        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);

        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return parseResponse(response, type);
    }

    public static <T> CompletableFuture<T> postAsync(String url, Object body, Class<T> clazz) {
        try {
            String jsonBody = MAPPER.writeValueAsString(body);

            HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);

            return HTTP_CLIENT_ASYNC.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(response -> {
                        try {
                            return parseResponse(response, type);
                        } catch (IOException e) {
                            throw new RuntimeException("异步请求失败", e);
                        }
                    });
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public static <T> T put(String url, Object body, Class<T> clazz) throws IOException, InterruptedException {
        String jsonBody = MAPPER.writeValueAsString(body);

        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        JavaType type = MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);

        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return parseResponse(response, type);
    }

    public static int delete(String url) throws IOException, InterruptedException {
        HttpRequest request = addAuthHeader(HttpRequest.newBuilder())
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode();
    }

    public static String getPlainText(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static byte[] download(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }

    public static HttpResponse<String> sendFormPost(String url, Map<String, String> formData) throws IOException, InterruptedException {
        String formBody = formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}