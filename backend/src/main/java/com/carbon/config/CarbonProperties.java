package com.carbon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "carbon")
public class CarbonProperties {
    private Ai ai = new Ai();

    public Ai getAi() {
        return ai;
    }

    public void setAi(Ai ai) {
        this.ai = ai;
    }

    public static class Ai {
        private double confidenceThreshold = 0.80;
        private Baidu baidu = new Baidu();

        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }

        public void setConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }

        public Baidu getBaidu() {
            return baidu;
        }

        public void setBaidu(Baidu baidu) {
            this.baidu = baidu;
        }
    }

    public static class Baidu {
        private String endpoint;
        private String apiKey;
        private String secretKey;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
