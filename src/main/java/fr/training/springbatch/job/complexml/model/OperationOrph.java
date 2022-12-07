package fr.training.springbatch.job.complexml.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OperationOrph")
public class OperationOrph extends Record {

    private String bicBeneficiaire;

    private String bicEmetteur;

    private String bicDestinataire;

    private String canalTrans;

    private String cricuitRegl;

    private String compteDeb;

    private String dateMessage;

    private String devise;

    private String famille;

    private String identifiantInterne;

    private String idInterneRH;

    private String idInterneRC;

    private String montant;

    @XmlElement(name = "BICBeneficiaire")
    public String getBicBeneficiaire() {
        return bicBeneficiaire;
    }

    public void setBicBeneficiaire(final String bicBeneficiaire) {
        this.bicBeneficiaire = bicBeneficiaire;
    }

    @XmlElement(name = "BicEmetteur")
    public String getBicEmetteur() {
        return bicEmetteur;
    }

    public void setBicEmetteur(final String bicEmetteur) {
        this.bicEmetteur = bicEmetteur;
    }

    @XmlElement(name = "BicDestinataire")
    public String getBicDestinataire() {
        return bicDestinataire;
    }

    public void setBicDestinataire(final String bicDestinataire) {
        this.bicDestinataire = bicDestinataire;
    }

    @XmlElement(name = "CanalTrans")
    public String getCanalTrans() {
        return canalTrans;
    }

    public void setCanalTrans(final String canalTrans) {
        this.canalTrans = canalTrans;
    }

    @XmlElement(name = "CricuitRegl")
    public String getCricuitRegl() {
        return cricuitRegl;
    }

    public void setCricuitRegl(final String cricuitRegl) {
        this.cricuitRegl = cricuitRegl;
    }

    @XmlElement(name = "CompteDeb")
    public String getCompteDeb() {
        return compteDeb;
    }

    public void setCompteDeb(final String compteDeb) {
        this.compteDeb = compteDeb;
    }

    @XmlElement(name = "DateMessage")
    public String getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(final String dateMessage) {
        this.dateMessage = dateMessage;
    }

    @XmlElement(name = "Devise")
    public String getDevise() {
        return devise;
    }

    public void setDevise(final String devise) {
        this.devise = devise;
    }

    @XmlElement(name = "Famille")
    public String getFamille() {
        return famille;
    }

    public void setFamille(final String famille) {
        this.famille = famille;
    }

    @XmlElement(name = "IdentifiantInterne")
    public String getIdentifiantInterne() {
        return identifiantInterne;
    }

    public void setIdentifiantInterne(final String identifiantInterne) {
        this.identifiantInterne = identifiantInterne;
    }

    @XmlElement(name = "InterneRH")
    public String getIdInterneRH() {
        return idInterneRH;
    }

    public void setIdInterneRH(final String idInterneRH) {
        this.idInterneRH = idInterneRH;
    }

    @XmlElement(name = "IdInterneRC")
    public String getIdInterneRC() {
        return idInterneRC;
    }

    public void setIdInterneRC(final String idInterneRC) {
        this.idInterneRC = idInterneRC;
    }

    @XmlElement(name = "Montant")
    public String getMontant() {
        return montant;
    }

    public void setMontant(final String montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("OperationOrph [bicBeneficiaire=").append(bicBeneficiaire).append(", bicEmetteur=").append(bicEmetteur).append(", bicDestinataire=")
                .append(bicDestinataire).append(", canalTrans=").append(canalTrans).append(", cricuitRegl=").append(cricuitRegl).append(", compteDeb=")
                .append(compteDeb).append(", dateMessage=").append(dateMessage).append(", devise=").append(devise).append(", famille=").append(famille)
                .append(", identifiantInterne=").append(identifiantInterne).append(", idInterneRH=").append(idInterneRH).append(", idInterneRC=")
                .append(idInterneRC).append(", montant=").append(montant).append("]");
        return builder.toString();
    }

}
