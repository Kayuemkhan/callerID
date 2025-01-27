package com.chromatics.caller_id.models;

import com.chromatics.caller_id.utils.NumberCategory;

public class CommunityReview {

    public enum Rating {

        UNKNOWN(0),
        POSITIVE(1),
        NEGATIVE(2),
        NEUTRAL(3);

        private final int id;

        Rating(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Rating getById(int id) {
            for (Rating rating : Rating.values()) {
                if (rating.getId() == id) return rating;
            }
            return null;
        }

    }

    private int id;

    private Rating rating;
    private NumberCategory category;
    private String author;
    private String title;
    private String comment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public NumberCategory getCategory() {
        return category;
    }

    public void setCategory(NumberCategory category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "CommunityReview{" +
                "id=" + id +
                ", rating=" + rating +
                ", category=" + category +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

}
