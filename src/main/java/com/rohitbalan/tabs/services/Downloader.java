package com.rohitbalan.tabs.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class Downloader {

    private final Logger logger = LoggerFactory.getLogger(Downloader.class);

    @Value("${com.rohitbalan.tabs.dailyDownloadThreshold}")
    private int dailyDownloadThreshold;

    private int urlsDownloadedToday;



    public String execute(final String url) throws IOException, InterruptedException {
        if(urlsDownloadedToday < dailyDownloadThreshold) {
            urlsDownloadedToday++;
        } else {
            Thread.sleep(1000 * 60 * 60 * 24);
            urlsDownloadedToday = 0;
        }
        final CloseableHttpClient httpClient = HttpClients.createDefault();

        final HttpGet httpGet = new HttpGet(url);

        if(System.getProperty("http.proxyHost")!=null && System.getProperty("http.proxyPort")!=null) {
            final HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), "http");
            final RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpGet.setConfig(config);
        }

        final CloseableHttpResponse response = httpClient.execute(httpGet);
        final HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }
}
