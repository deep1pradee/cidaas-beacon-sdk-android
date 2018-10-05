package cidaasbeaconsdk.Entity;

public class ErrorEntity {
    public int status = 500;
    boolean isSuccess;

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String message = "";
}
