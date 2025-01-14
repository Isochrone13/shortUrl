package program;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


// Класс для работы с файлом конфигурации
public class ConfigService {
    private final Map<String, String> configMap = new HashMap<>();

    // Конструктор, который принимает путь к конфигурационному файлу.
    public ConfigService(String configFilePath) {
        loadConfig(configFilePath);
    }

    /*
    Метод который считывает конфигурацию из файла и сохраняет их во внутреннюю переменную configMap
    */
    private void loadConfig(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Файл config.txt не найден. Используем значения по умолчанию.");
            // Если файл не найден, задаём значения по умолчанию
            configMap.put("BASE_URL", "www.example.com");
            configMap.put("DEFAULT_TL_MINUTES", "1440");
            configMap.put("DEFAULT_MAX_CLICKS", "5");
            return;
        }
        // На каждой строке берётся часть до знака = как ключ, а после = как значение.
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("=")) {
                    String[] arr = line.split("=");
                    if (arr.length == 2) {
                        configMap.put(arr[0].trim(), arr[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения config.txt: " + e.getMessage());
        }
    }

    /*
    Методы для получения доступа к данным из конфига,
    Получаем строку из configMap по ключу (например, "BASE_URL").
    Если ключа в конфигурационном файле нет, возвращаем дефолтное значение.
    */
    public long getDefaultTlMinutes() {
        String val = configMap.getOrDefault("DEFAULT_TL_MINUTES", "1440");
        return Long.parseLong(val);
    }

    public long getDefaultMaxClicks() {
        String val = configMap.getOrDefault("DEFAULT_MAX_CLICKS", "5");
        return Long.parseLong(val);
    }

    public String getBaseUrl() {
        return configMap.getOrDefault("BASE_URL", "www.example.com");
    }
}