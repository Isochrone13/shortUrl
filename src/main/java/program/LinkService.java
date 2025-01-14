package program;

import java.io.*;
import java.util.Random;

// Класс для работы с короткими ссылками
public class LinkService {
    // Объявляем тип поля класса, который Хранит путь к папке, где будут находиться файлы ссылок
    private final String linksFolderPath;

    /*
    Объявляем конструктор класса.
    */
    public LinkService(String linksFolderPath) {
        this.linksFolderPath = linksFolderPath;
    }

    /*
    Метод создания новой короткой ссылки.
    */
    public Link createLink(String ownerUuid, String originalUrl,
                           long requestedTl, long defaultTl,
                           long requestedClicks, long defaultClicks) {

        // Рассчитать фактическое время жизни actual time limit (min)
        long actualTl = Math.min(requestedTl, defaultTl);
        // Рассчитать фактический лимит переходов actual max clicks (max)
        long actualMc = Math.max(requestedClicks, defaultClicks);

        // Создаём объект Link
        Link link = new Link();
        link.setShortCode(generateShortCode());
        link.setOwnerUuid(ownerUuid);
        link.setOriginalUrl(originalUrl);
        link.setCreateTimeMs(System.currentTimeMillis());
        link.setTlMinutes(actualTl);
        link.setMaxClicks(actualMc);
        link.setCurrentClicks(0);

        // Сохранить в файл
        saveLinkToFile(link);
        return link;
    }

    /*
    Метод для редактирования лимита переходов.
    */
    public void editLinkLimit(Link link, long newLimit, long defaultClicks) {
        long finalLimit = Math.max(newLimit, defaultClicks);
        link.setMaxClicks(finalLimit);
        saveLinkToFile(link);
    }

    /*
    Метод для удаления ссылки (фактически удаляем файл).
    */
    public void deleteLink(String shortCode) {
        String path = linksFolderPath + File.separator + shortCode + ".txt";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /*
    Метод загрузки ссылки из файла.
    */
    public Link loadLink(String shortCode) {
        String path = linksFolderPath + File.separator + shortCode + ".txt";
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Link link = new Link();
            link.setShortCode(br.readLine());       // shortURL
            link.setOwnerUuid(br.readLine());       // ownerUuid
            link.setOriginalUrl(br.readLine());     // originalUrl
            link.setCreateTimeMs(Long.parseLong(br.readLine()));
            link.setTlMinutes(Long.parseLong(br.readLine()));
            link.setMaxClicks(Long.parseLong(br.readLine()));
            link.setCurrentClicks(Long.parseLong(br.readLine()));
            return link;
        } catch (IOException e) {
            System.out.println("Ошибка чтения ссылки: " + e.getMessage());
            return null;
        }
    }

    /*
    Метод для сохранения ссылки в файл.
    */
    public void saveLinkToFile(Link link) {
        String path = linksFolderPath + File.separator + link.getShortCode() + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, false))) {
            // Запишем поля по строкам
            bw.write(link.getShortCode());
            bw.newLine();
            bw.write(link.getOwnerUuid());
            bw.newLine();
            bw.write(link.getOriginalUrl());
            bw.newLine();
            bw.write(String.valueOf(link.getCreateTimeMs()));
            bw.newLine();
            bw.write(String.valueOf(link.getTlMinutes()));
            bw.newLine();
            bw.write(String.valueOf(link.getMaxClicks()));
            bw.newLine();
            bw.write(String.valueOf(link.getCurrentClicks()));
        } catch (IOException e) {
            System.out.println("Ошибка записи ссылки: " + e.getMessage());
        }
    }

    /*
    Метод для удаления ссылки, если срок жизни истёк — удаляем.
    */
    public void cleanupExpiredLinks() {
        File folder = new File(linksFolderPath);
        File[] files = folder.listFiles();
        if (files == null) return;

        long now = System.currentTimeMillis();
        for (File f : files) {
            if (!f.isFile() || !f.getName().endsWith(".txt")) {
                continue;
            }
            // Попробуем загрузить ссылку
            Link link = loadLink(f.getName().replace(".txt", ""));
            if (link == null) {
                continue;
            }
            // Проверяем время
            long ageMs = now - link.getCreateTimeMs();
            long tlMs = link.getTlMinutes() * 60_000;
            // если фактическое время меньше лимита, то удаляем ссылку
            if (ageMs >= tlMs) {
                deleteLink(link.getShortCode());
            }
        }
    }

    /*
    Метод для генерации кода ссылки (8 символов: A-Z, a-z, 0-9).
    */
    private String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int idx = random.nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
