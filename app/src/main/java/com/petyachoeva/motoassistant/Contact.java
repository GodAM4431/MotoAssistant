package com.petyachoeva.motoassistant;

public class Contact {
    private String contact_name;
    private String contact_phone;

    public Contact() {
    }

    public Contact(String contactName, String contactPhone) {
        this.contact_name = contact_name;
        this.contact_phone = contactPhone;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }
}
