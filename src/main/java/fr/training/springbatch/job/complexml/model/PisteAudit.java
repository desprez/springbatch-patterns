package fr.training.springbatch.job.complexml.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PisteAudit")
public class PisteAudit {

    private String codeEvt;

    private String dateInscr;

    private String idInterne;

    private String libEvt;

    private String codeTypeEnt;

    private String codeApp;

    private String dateCrea;

    @XmlElement(name = "CodeEvt")
    public String getCodeEvt() {
        return codeEvt;
    }

    public void setCodeEvt(final String codeEvt) {
        this.codeEvt = codeEvt;
    }

    @XmlElement(name = "DateInscr")
    public String getDateInscr() {
        return dateInscr;
    }

    public void setDateInscr(final String dateInscr) {
        this.dateInscr = dateInscr;
    }

    @XmlElement(name = "IdInterne")
    public String getIdInterne() {
        return idInterne;
    }

    public void setIdInterne(final String idInterne) {
        this.idInterne = idInterne;
    }

    @XmlElement(name = "LibEvt")
    public String getLibEvt() {
        return libEvt;
    }

    public void setLibEvt(final String libEvt) {
        this.libEvt = libEvt;
    }

    @XmlElement(name = "CodeTypeEnt")
    public String getCodeTypeEnt() {
        return codeTypeEnt;
    }

    public void setCodeTypeEnt(final String codeTypeEnt) {
        this.codeTypeEnt = codeTypeEnt;
    }

    @XmlElement(name = "CodeApp")
    public String getCodeApp() {
        return codeApp;
    }

    public void setCodeApp(final String codeApp) {
        this.codeApp = codeApp;
    }

    @XmlElement(name = "DateCrea")
    public String getDateCrea() {
        return dateCrea;
    }

    public void setDateCrea(final String dateCrea) {
        this.dateCrea = dateCrea;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PisteAudit [codeEvt=").append(codeEvt).append(", dateInscr=").append(dateInscr).append(", idInterne=").append(idInterne)
                .append(", libEvt=").append(libEvt).append(", codeTypeEnt=").append(codeTypeEnt).append(", codeApp=").append(codeApp).append(", dateCrea=")
                .append(dateCrea).append("]");
        return builder.toString();
    }

}
