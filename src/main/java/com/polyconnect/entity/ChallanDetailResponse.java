package com.polyconnect.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChallanDetailResponse {
    @JsonProperty("Table")
    private List<ChallanDetail> table;

    @JsonProperty("Table1")
    private List<StudentDetail> table1;

    public static class ChallanDetail {
        private String id;
        private String merchantId;
        private String subscriberid;
        private String txnrefno;
        private String bankrefno;
        private String txnamt;
        private String bankid;
        private String txndate;
        private String authstatus;
        private String errordesc;
        @JsonProperty("addtninfo3")
        private String pinFromAddtn;
        @JsonProperty("addtninfo5")
        private String feeType;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getSubscriberid() {
            return subscriberid;
        }

        public void setSubscriberid(String subscriberid) {
            this.subscriberid = subscriberid;
        }

        public String getTxnrefno() {
            return txnrefno;
        }

        public void setTxnrefno(String txnrefno) {
            this.txnrefno = txnrefno;
        }

        public String getBankrefno() {
            return bankrefno;
        }

        public void setBankrefno(String bankrefno) {
            this.bankrefno = bankrefno;
        }

        public String getTxnamt() {
            return txnamt;
        }

        public void setTxnamt(String txnamt) {
            this.txnamt = txnamt;
        }

        public String getBankid() {
            return bankid;
        }

        public void setBankid(String bankid) {
            this.bankid = bankid;
        }

        public String getTxndate() {
            return txndate;
        }

        public void setTxndate(String txndate) {
            this.txndate = txndate;
        }

        public String getAuthstatus() {
            return authstatus;
        }

        public void setAuthstatus(String authstatus) {
            this.authstatus = authstatus;
        }

        public String getErrordesc() {
            return errordesc;
        }

        public void setErrordesc(String errordesc) {
            this.errordesc = errordesc;
        }

        public String getPinFromAddtn() {
            return pinFromAddtn;
        }

        public void setPinFromAddtn(String pinFromAddtn) {
            this.pinFromAddtn = pinFromAddtn;
        }

        public String getFeeType() {
            return feeType;
        }

        public void setFeeType(String feeType) {
            this.feeType = feeType;
        }
    }

    public static class StudentDetail {
        private String pin;

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }
    }

    public List<ChallanDetail> getTable() {
        return table;
    }

    public void setTable(List<ChallanDetail> table) {
        this.table = table;
    }

    public List<StudentDetail> getTable1() {
        return table1;
    }

    public void setTable1(List<StudentDetail> table1) {
        this.table1 = table1;
    }
}
