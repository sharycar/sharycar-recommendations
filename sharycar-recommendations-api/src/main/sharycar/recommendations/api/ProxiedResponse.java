package sharycar.recommendations.api;

public class ProxiedResponse {

    private String response;
    private String proxiedFrom;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getProxiedFrom() {
        return proxiedFrom;
    }

    public void setProxiedFrom(String proxiedFrom) {
        this.proxiedFrom = proxiedFrom;
    }
}