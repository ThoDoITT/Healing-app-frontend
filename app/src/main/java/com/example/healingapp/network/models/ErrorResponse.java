package com.example.healingapp.network.models;


import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    // Sử dụng value và alternate để thử nhiều key JSON phổ biến cho lỗi
    @SerializedName(value = "message", alternate = {"msg", "detail"})
    private String detailedMessage;

    @SerializedName(value = "error", alternate = {"error_description"})
    private String errorSummary;

    // transient: Gson sẽ bỏ qua trường này khi serialize/deserialize JSON
    // Chúng ta sẽ set giá trị này từ HTTP status code
    private transient int statusCode;

    public ErrorResponse() {
    }

    // Constructor để tạo thủ công hoặc khi parse lỗi
    public ErrorResponse(String errorSummary, String detailedMessage, int statusCode) {
        this.errorSummary = errorSummary;
        this.detailedMessage = detailedMessage;
        this.statusCode = statusCode;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public String getDetailedMessage() {
        // Ưu tiên detailedMessage, nếu không có thì dùng errorSummary
        return detailedMessage != null ? detailedMessage : (errorSummary != null ? errorSummary : "Lỗi không xác định");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    // Phương thức tĩnh để tạo ErrorResponse từ Exception phía client
    public static ErrorResponse fromException(Throwable t, int defaultStatusCode) {
        return new ErrorResponse("Lỗi phía client", t.getMessage(), defaultStatusCode);
    }
}