public class UserBook {
    String title;
    String author;
    String originalRating;
    String originalReviews;
    String status;
    String spendTime;
    String startDate;
    String endDate;
    String userRating;
    String userReview;

    public UserBook(String title, String author, String originalRating, String originalReviews, String status, String spendTime, String startDate, String endDate, String userRating, String userReview) {
        this.title = title;
        this.author = author;
        this.originalRating = originalRating;
        this.originalReviews = originalReviews;
        this.status = status;
        this.spendTime = spendTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userRating = userRating;
        this.userReview = userReview;
    }
}