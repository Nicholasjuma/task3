package com.example.task3;



public class Comment {
    private String userId;
    private String recipeId;
    private String commentText;
    private long timestamp;

    public Comment() {}

    public Comment(String userId, String recipeId, String commentText, long timestamp) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getCommentText() {
        return commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
