package com.example.uberv.harvester;

public class Contact {
    private String clientName;
    private String primaryOg;
    private String mcClassification;
    private String geoArea;
    private String role;
    private String person;
    private String personLink;
    private String geoUnit;
    private String countryForRole;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPrimaryOg() {
        return primaryOg;
    }

    public void setPrimaryOg(String primaryOg) {
        this.primaryOg = primaryOg;
    }

    public String getMcClassification() {
        return mcClassification;
    }

    public void setMcClassification(String mcClassification) {
        this.mcClassification = mcClassification;
    }

    public String getGeoArea() {
        return geoArea;
    }

    public void setGeoArea(String geoArea) {
        this.geoArea = geoArea;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPersonLink() {
        return personLink;
    }

    public void setPersonLink(String personLink) {
        this.personLink = personLink;
    }

    public String getGeoUnit() {
        return geoUnit;
    }

    public void setGeoUnit(String geoUnit) {
        this.geoUnit = geoUnit;
    }

    public String getCountryForRole() {
        return countryForRole;
    }

    public void setCountryForRole(String countryForRole) {
        this.countryForRole = countryForRole;
    }

    public Contact(String clientName, String primaryOg, String mcClassification, String geoArea, String role, String person, String personLink, String geoUnit, String countryForRole) {

        this.clientName = clientName;
        this.primaryOg = primaryOg;
        this.mcClassification = mcClassification;
        this.geoArea = geoArea;
        this.role = role;
        this.person = person;
        this.personLink = personLink;
        this.geoUnit = geoUnit;
        this.countryForRole = countryForRole;
    }

    public Contact() {

    }

    @Override
    public String toString() {
        return "Contact{" +
                "clientName='" + clientName + '\'' +
                ", primaryOg='" + primaryOg + '\'' +
                ", mcClassification='" + mcClassification + '\'' +
                ", geoArea='" + geoArea + '\'' +
                ", role='" + role + '\'' +
                ", person='" + person + '\'' +
                ", personLink='" + personLink + '\'' +
                ", geoUnit='" + geoUnit + '\'' +
                ", countryForRole='" + countryForRole + '\'' +
                '}';
    }

    public String toExcelString(){
        return ";"+clientName+";"+primaryOg+";"+mcClassification+";"+geoArea+";"+role+";"+person+";"+geoUnit+";"+countryForRole;
    }
}
