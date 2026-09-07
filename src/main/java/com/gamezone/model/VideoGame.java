package com.gamezone.model;

/**
 * Represents a video game product sold at GameZone Unicesar.
 * A video game is a specific type of Product characterized by
 * its platform, genre, and recommended age rating.
 */
public class VideoGame extends Product {

    private String platform;
    private String genre;
    private String ageRating;

    /**
     * Creates a new VideoGame with the given common and particular attributes.
     *
     * @param id        unique identifier of the product
     * @param title     display title of the product
     * @param price     unit price of the product
     * @param stock     initial quantity available in inventory
     * @param platform  platform the game was developed for (e.g. PS5, PC)
     * @param genre     genre of the game (e.g. Action, RPG)
     * @param ageRating recommended age classification (e.g. E, T, M)
     */
    public VideoGame(String id, String title, double price, int stock,
                     String platform, String genre, String ageRating) {
        super(id, title, price, stock);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    @Override
    public String getDescription() {
        return getTitle() + " - Platform: " + platform +
                ", Genre: " + genre +
                ", Age rating: " + ageRating +
                ", Price: $" + getPrice() +
                ", Stock: " + getStock();
    }

    @Override
    public String toString() {
        return "VideoGame{" + getDescription() + "}";
    }

}