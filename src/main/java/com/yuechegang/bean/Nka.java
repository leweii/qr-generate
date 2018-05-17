package com.yuechegang.bean;

import java.io.Serializable;

public class Nka implements Serializable {
    private static final long serialVersionUID = 1L;
    private String customerNumber;
    private String salesOffice;
    private String salesGroup;

    public Nka(final String customerNumber, final String salesOffice, final String salesGroup) {
        this.customerNumber = customerNumber;
        this.salesOffice = salesOffice;
        this.salesGroup = salesGroup;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(final String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getSalesOffice() {
        return salesOffice;
    }

    public void setSalesOffice(final String salesOffice) {
        this.salesOffice = salesOffice;
    }

    public String getSalesGroup() {
        return salesGroup;
    }

    public void setSalesGroup(final String salesGroup) {
        this.salesGroup = salesGroup;
    }
}
