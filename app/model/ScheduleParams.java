package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public  class ScheduleParams {
    String startDate;
    String endDate;
    String polyCode;
    String doctorCode;
    String startTime;
    String endTime;

    public ScheduleParams(String startDate, String endDate, String polyCode, String doctorCode, String startTime, String endTime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.polyCode = polyCode;
        this.doctorCode = doctorCode;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("polyCode")
    public String getPolyCode() {
        return polyCode;
    }

    @JsonProperty("polyCode")
    public void setPolyCode(String polyCode) {
        this.polyCode = polyCode;
    }

    @JsonProperty("doctorCode")
    public String getDoctorCode() {
        return doctorCode;
    }

    @JsonProperty("doctorCode")
    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    @JsonProperty("startTime")
    public String getStartTime() {
        return startTime;
    }

    @JsonProperty("startTime")
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @JsonProperty("endTime")
    public String getEndTime() {
        return endTime;
    }

    @JsonProperty("endTime")
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

