package br.com.poc.ingestion.domain;

public class Config {

    private Integer amountMessages;

    private Boolean tst;

    private Boolean esf;

    private Boolean wpc;

    public Boolean getTst() {
        return tst;
    }

    public void setTst(Boolean tst) {
        this.tst = tst;
    }

    public Boolean getEsf() {
        return esf;
    }

    public void setEsf(Boolean esf) {
        this.esf = esf;
    }

    public Boolean getWpc() {
        return wpc;
    }

    public void setWpc(Boolean wpc) {
        this.wpc = wpc;
    }

    public Integer getAmountMessages() {
        return amountMessages;
    }

    public void setAmountMessages(Integer amountMessages) {
        this.amountMessages = amountMessages;
    }
}
