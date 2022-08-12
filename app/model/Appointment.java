package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public  class Appointment {
    String mrNo;
    String bpjsNo;
    String scheduleCode;
    String noRujukan;
    String providerPerujuk;

    @JsonProperty("mrNo")
    public String getMrNo() {
        return mrNo;
    }

    @JsonProperty("mrNo")
    public void setMrNo(String mrNo) {
        this.mrNo = mrNo;
    }

    @JsonProperty("bpjsNo")
    public String getBpjsNo() {
        return bpjsNo;
    }

    @JsonProperty("bpjsNo")
    public void setBpjsNo(String bpjsNo) {
        this.bpjsNo = bpjsNo;
    }

    @JsonProperty("scheduleCode")
    public String getScheduleCode() {
        return scheduleCode;
    }

    @JsonProperty("scheduleCode")
    public void setScheduleCode(String scheduleCode) {
        this.scheduleCode = scheduleCode;
    }

    @JsonProperty("noRujukan")
    public String getNoRujukan() {
        return noRujukan;
    }

    @JsonProperty("noRujukan")
    public void setNoRujukan(String noRujukan) {
        this.noRujukan = noRujukan;
    }

    @JsonProperty("providerPerujuk")
    public String getProviderPerujuk() {
        return providerPerujuk;
    }

    @JsonProperty("providerPerujuk")
    public void setProviderPerujuk(String providerPerujuk) {
        this.providerPerujuk = providerPerujuk;
    }
}
