package util.exceptions;

import org.springframework.http.HttpStatus;

public class ExceptionModel {

    private String errorMessage;
    private String recommendation;
    private HttpStatus status;

    public ExceptionModel() {
    }

    public ExceptionModel(String errorMessage, String recommendation, HttpStatus status) {
        this.errorMessage = errorMessage;
        this.recommendation = recommendation;
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}