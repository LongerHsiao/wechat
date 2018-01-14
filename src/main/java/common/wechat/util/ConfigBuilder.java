package common.wechat.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author LongerHsiao
 * @date 2017-12-14
 */
public final class ConfigBuilder {
    private ConfigBuilder() {
    }

    private static class ConfigBuilderHolder {
        public static final ConfigBuilder INSTANCE = new ConfigBuilder();
    }

    public static ConfigBuilder getInstance() {
        return ConfigBuilderHolder.INSTANCE;
    }

    public Properties init(String fileName) {
        return this.init(this.getClass().getClassLoader(), fileName);
    }

    private Properties init(ClassLoader classLoader, String fileName) {
        Properties config = new Properties();
        InputStream input = classLoader.getResourceAsStream(fileName);
        if (input != null) {
            try {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading classpath property '" + fileName + "', see nested exceptions", e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return config;
    }
}
