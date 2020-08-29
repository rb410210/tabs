package com.rohitbalan.tabs.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

@Slf4j
@Service
public class Downloader {

    @Value("${com.rohitbalan.tabs.dailyDownloadThreshold}")
    private int dailyDownloadThreshold;

    private long lastDownloadTimeInMillis;
    private long waitTimeInMillis;

    @PostConstruct
    private void init() {
        waitTimeInMillis = (1000 * 60 * 60 * 24) / dailyDownloadThreshold;
        log.debug("Setting waitTimeInMillis: {}", waitTimeInMillis);
    }


    public String execute(String url) throws IOException, InterruptedException {
        if(url!=null && url.startsWith("/")) {
            url = "https://www.ultimate-guitar.com" + url;
        }
        final long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime < (lastDownloadTimeInMillis + waitTimeInMillis)) {
            final long sleepTime = lastDownloadTimeInMillis + waitTimeInMillis - currentTime;
            log.debug("Sleeping for: {}", sleepTime);
            Thread.sleep(sleepTime);
        }
        lastDownloadTimeInMillis = Calendar.getInstance().getTimeInMillis();

        final CloseableHttpClient httpClient = HttpClients.createDefault();

        final HttpGet httpGet = new HttpGet(url);

        if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
            final HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), "http");
            final RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpGet.setConfig(config);
        }

        final CloseableHttpResponse response = httpClient.execute(httpGet);
        final int statusCode = response.getStatusLine().getStatusCode();
        log.debug("statusCode {}", statusCode);
        final HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }
}
