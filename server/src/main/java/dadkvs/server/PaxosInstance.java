package dadkvs.server;

public class PaxosInstance {
    private int highestTimestamp;
    private int acceptedValue;
    private int acceptedTimestamp;
    private int decidedValue;
    private int decidedTimestamp;

    public PaxosInstance() {
        this.highestTimestamp = -1;
        this.acceptedValue = -1;
        this.acceptedTimestamp = -1;
        this.decidedValue = -1;
        this.decidedTimestamp = -1;
    }

    public int getHighestTimestamp() {
        return highestTimestamp;
    }

    public void setHighestTimestamp(int highestTimestamp) {
        this.highestTimestamp = highestTimestamp;
    }

    public int getAcceptedValue() {
        return acceptedValue;
    }

    public void setAcceptedValue(int acceptedValue) {
        this.acceptedValue = acceptedValue;
    }

    public int getAcceptedTimestamp() {
        return acceptedTimestamp;
    }

    public void setAcceptedTimestamp(int acceptedTimestamp) {
        this.acceptedTimestamp = acceptedTimestamp;
    }

    public int getDecidedValue() {
        return decidedValue;
    }

    public void setDecidedValue(int decidedValue) {
        this.decidedValue = decidedValue;
    }

    public int getDecidedTimestamp() {
        return decidedTimestamp;
    }

    public void setDecidedTimestamp(int decidedTimestamp) {
        this.decidedTimestamp = decidedTimestamp;
    }

    public boolean isDecided() {
        return decidedValue != -1;
    }
}