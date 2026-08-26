package com.polyconnect.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChallanNumberResponse {
    @JsonProperty("Table")
    private List<ChallanData> table;

    public static class ChallanData {
        @JsonProperty("ChalanaNumber")
        private String chalanaNumber;
        @JsonProperty("txndate")
        private String txnDate;

        public String getChalanaNumber() {
            return chalanaNumber;
        }

        public void setChalanaNumber(String chalanaNumber) {
            this.chalanaNumber = chalanaNumber;
        }

        public String getTxnDate() {
            return txnDate;
        }

        public void setTxnDate(String txnDate) {
            this.txnDate = txnDate;
        }
    }

    public List<ChallanData> getTable() {
        return table;
    }

    public void setTable(List<ChallanData> table) {
        this.table = table;
    }
}