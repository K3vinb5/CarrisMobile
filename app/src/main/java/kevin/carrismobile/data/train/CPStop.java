package kevin.carrismobile.data.train;

public class CPStop {

    private String arrivalRealDateTime;
    private String departureRealDateTime;
    private String arrivalScheduledDateTime;
    private String departureScheduledDateTime;
    // info: -1 -> Suprimido | null -> Sem Informação
    private String delay;
    private String fromNode;
    private String fromNodeName;
    private String node;
    private String toNode;
    private String toNodeName;
    private String serviceType;
    private String lineNumber;
    private String trainNumber;
    private String nodeName;

    public CPStop(String arrivalRealDateTime, String departureRealDateTime, String arrivalScheduledDateTime, String departureScheduledDateTime, String delay, String fromNodeId, String fromNodeName, String lineNumber, String serviceType, String toNodeId, String toNodeName, String trainNumber, String node) {
        this.arrivalRealDateTime = arrivalRealDateTime;
        this.departureRealDateTime = departureRealDateTime;
        this.arrivalScheduledDateTime = arrivalScheduledDateTime;
        this.departureScheduledDateTime = departureScheduledDateTime;
        this.delay = delay;
        this.fromNode = fromNodeId;
        this.fromNodeName = fromNodeName;
        this.node = node;
        this.toNode = toNodeId;
        this.toNodeName = toNodeName;
        this.lineNumber = lineNumber;
        this.serviceType = serviceType;
        this.trainNumber = trainNumber;
    }

    public void setCurrentName(String name) {
        this.nodeName = name;
    }

    public String getArrivalRealDateTime() {
        return arrivalRealDateTime;
    }

    public void setArrivalRealDateTime(String arrivalRealDateTime) {
        this.arrivalRealDateTime = arrivalRealDateTime;
    }

    public String getDepartureRealDateTime() {
        return departureRealDateTime;
    }

    public void setDepartureRealDateTime(String departureRealDateTime) {
        this.departureRealDateTime = departureRealDateTime;
    }

    public String getArrivalScheduledDateTime() {
        return arrivalScheduledDateTime;
    }

    public void setArrivalScheduledDateTime(String arrivalScheduledDateTime) {
        this.arrivalScheduledDateTime = arrivalScheduledDateTime;
    }

    public String getDepartureScheduledDateTime() {
        return departureScheduledDateTime;
    }

    public void setDepartureScheduledDateTime(String departureScheduledDateTime) {
        this.departureScheduledDateTime = departureScheduledDateTime;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getOriginStationId() {
        return fromNode;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getFromNodeName() {
        return fromNodeName;
    }

    public void setFromNodeName(String fromNodeName) {
        this.fromNodeName = fromNodeName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDestinyStationId() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public String getToNodeName() {
        return toNodeName;
    }

    public void setToNodeName(String toNodeName) {
        this.toNodeName = toNodeName;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getStationId() {
        return node;
    }
    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CPStop){
            return ((CPStop)obj).getStationId().equals(this.getStationId()) &&
                    ((CPStop)obj).getOriginStationId().equals(this.getOriginStationId()) &&
                    ((CPStop)obj).getDestinyStationId().equals(this.getDestinyStationId());
        }
        return false;
    }

    @Override
    public String toString() {
        String delay;
        if (this.getDelay() == null){
            delay = "Sem Informação";
            return this.getArrivalScheduledDateTime() + ": " + this.getFromNodeName() + " -> " + this.getToNodeName() + " L: " + this.getLineNumber();
        }
        else if(this.getDelay().equals("-1")){
            delay = "Suprimido";
            return this.getArrivalScheduledDateTime() + " (" + delay + "): " + this.getFromNodeName() + " -> " + this.getToNodeName() + " L: " + this.getLineNumber();
        }
        else {
            delay = this.getDelay();
            if (!delay.equals("0")){
                return this.getArrivalScheduledDateTime() + " (Atraso :" + delay + "min): " + this.getFromNodeName() + " -> " + this.getToNodeName() + " L: " + this.getLineNumber();
            }
        }
        return this.getArrivalScheduledDateTime() + ": " + this.getFromNodeName() + " -> " + this.getToNodeName() + " L: " + this.getLineNumber();
    }
}
