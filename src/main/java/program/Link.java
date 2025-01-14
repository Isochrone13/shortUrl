package program;

// Класс-модель для короткой ссылки
public class Link {
    // Объявляем типы полей класса:
    private String shortCode;       // уникальный код ссылки без base URL
    private String ownerUuid;       // uuid владельца
    private String originalUrl;     // исходная ссылка
    private long createTimeMs;      // время создания в мс)
    private long tlMinutes;         // время жизни ссылки (в минутах)
    private long maxClicks;         // лимит переходов
    private long currentClicks;     // сколько уже было переходов

    /*
    Объявляем конструктор класса.
    Поля остаются неинициализированными и могут быть установлены позже с помощью сеттеров.
    */

    public Link() {
    }

    // Объявляем геттеры/сеттеры
    public String getShortCode() {
        return shortCode;
    }
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    public String getOwnerUuid() {
        return ownerUuid;
    }
    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }
    public String getOriginalUrl() {
        return originalUrl;
    }
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    public long getCreateTimeMs() {
        return createTimeMs;
    }
    public void setCreateTimeMs(long createTimeMs) {
        this.createTimeMs = createTimeMs;
    }
    public long getTlMinutes() {
        return tlMinutes;
    }
    public void setTlMinutes(long tlMinutes) {
        this.tlMinutes = tlMinutes;
    }
    public long getMaxClicks() {
        return maxClicks;
    }
    public void setMaxClicks(long maxClicks) {
        this.maxClicks = maxClicks;
    }
    public long getCurrentClicks() {
        return currentClicks;
    }
    public void setCurrentClicks(long currentClicks) {
        this.currentClicks = currentClicks;
    }
}
