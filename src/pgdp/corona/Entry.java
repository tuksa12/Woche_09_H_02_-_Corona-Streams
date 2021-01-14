package pgdp.corona;

import java.time.LocalDate;

//An Entry object represents one line of entry in the Dataset
public class Entry {
    private final int districtId;
    private final String ageGroup; //A00-A04, A05-A14, ..., unbekannt
    private final char sex; //[M]an / [W]oman / [u]nknown
    private final LocalDate reportingDate;
    private final CaseReport infection;
    private final CaseReport death;
    private final CaseReport recovery;

    public Entry(int districtId, String ageGroup, char sex, LocalDate reportingDate,
                 CaseReport infection, CaseReport death, CaseReport recovery) {
        this.districtId = districtId;
        this.ageGroup = ageGroup;
        this.sex = sex;
        this.reportingDate = reportingDate;
        this.infection = infection;
        this.death = death;
        this.recovery = recovery;
    }

    //returns the unique ID of the federal state of this entry (IdBundesland)
    public int getStateId() {
        return Dataset.stateOfDistrict(districtId);
    }

    //returns the name of the federal state of this entry (Bundesland)
    public String getState() {
        return Dataset.nameOfState(Dataset.stateOfDistrict(districtId));
    }

    //returns the unique ID of the district of this entry (IdLandkreis)
    public int getDistrictId() {
        return districtId;
    }

    //returns the name of the district of this entry (Landkreis)
    public String getDistrict() {
        return Dataset.nameOfDistrict(districtId);
    }

    //returns the age group of this entry (Altersgruppe)
    public String getAgeGroup() {
        return ageGroup;
    }

    //returns the sex of this entry, either 'M'an, 'W'oman or 'u'nknown (Geschlecht)
    public char getSex() {
        return sex;
    }

    //returns the reporting date (to the government health officials) of this entry (Meldedatum)
    public LocalDate getReportingDate() {
        return reportingDate;
    }

    //returns the infection case report of this entry (AnzahlFall/NeuerFall)
    public CaseReport getInfection() {
        return infection;
    }

    //returns the death case report of this entry (AnzahlTodesfall/NeuerTodesfall)
    public CaseReport getDeath() {
        return death;
    }

    //returns the recovery case report of this entry (AnzahlGenesen/NeuGenesen)
    public CaseReport getRecovery() {
        return recovery;
    }


    @Override
    public String toString() {
        return "{state=" + getState() + ", district=" + getDistrict() + ", ageGroup=" + ageGroup + ", sex=" + sex +
                ", reportingDate=" + reportingDate + ", infection=" + infection + ", death=" + death +
                ", recovery=" + recovery + "}";
    }
}
