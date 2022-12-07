package fr.training.springbatch.job.complexml.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RemiseHomogene")
public class RemiseHomogene {

    private String bicBeneficiaire;

    private String bicEmetteur;

    private String bicDestinataire;

    private String compteDeb;

    private String dateMessage;

    private String devise;

    private String famille;

    private String identifiantInterne;

    private String idInterneRH;

    private String idInterneRC;

    private String montant;

    private Long nbreOpeAttentes;

    private Long nbreOperejetees;

    private Long nbreOpetraitees;

    private Long nbreOpetotal;

    private String origine;

    private String referenceExterne;

    private String sens;

    private String statut;

    private String typeEntite;

    private String dateCreation;

    private String dateModification;

    private String dateInscription;

    private Long nombrePA;

    private List<PisteAudit> pisteAudits;

    private Long nombreOP;

    private List<Operation> operations;

    @XmlElement(name = "BICBeneficiaire")
    public String getBicBeneficiaire() {
        return bicBeneficiaire;
    }

    public void setBicBeneficiaire(final String bicBeneficiaire) {
        this.bicBeneficiaire = bicBeneficiaire;
    }

    @XmlElement(name = "BICEmetteur")
    public String getBicEmetteur() {
        return bicEmetteur;
    }

    public void setBicEmetteur(final String bicEmetteur) {
        this.bicEmetteur = bicEmetteur;
    }

    @XmlElement(name = "BICDestinataire")
    public String getBicDestinataire() {
        return bicDestinataire;
    }

    public void setBicDestinataire(final String bicDestinataire) {
        this.bicDestinataire = bicDestinataire;
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

    @XmlElement(name = "IdInterneRH")
    public String getIdInterneRH() {
        return idInterneRH;
    }

    public void setIdInterneRH(final String idInterneRH) {
        this.idInterneRH = idInterneRH;
    }

    @XmlElement(name = "IdInterneRH")
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

    @XmlElement(name = "NbreOpeAttentes")
    public Long getNbreOpeAttentes() {
        return nbreOpeAttentes;
    }

    public void setNbreOpeAttentes(final Long nbreOpeAttentes) {
        this.nbreOpeAttentes = nbreOpeAttentes;
    }

    @XmlElement(name = "NbreOperejetees")
    public Long getNbreOperejetees() {
        return nbreOperejetees;
    }

    public void setNbreOperejetees(final Long nbreOperejetees) {
        this.nbreOperejetees = nbreOperejetees;
    }

    @XmlElement(name = "NbreOpetraitees")
    public Long getNbreOpetraitees() {
        return nbreOpetraitees;
    }

    public void setNbreOpetraitees(final Long nbreOpetraitees) {
        this.nbreOpetraitees = nbreOpetraitees;
    }

    @XmlElement(name = "NbreOpetotal")
    public Long getNbreOpetotal() {
        return nbreOpetotal;
    }

    public void setNbreOpetotal(final Long nbreOpetotal) {
        this.nbreOpetotal = nbreOpetotal;
    }

    @XmlElement(name = "Origine")
    public String getOrigine() {
        return origine;
    }

    public void setOrigine(final String origine) {
        this.origine = origine;
    }

    @XmlElement(name = "ReferenceExterne")
    public String getReferenceExterne() {
        return referenceExterne;
    }

    public void setReferenceExterne(final String referenceExterne) {
        this.referenceExterne = referenceExterne;
    }

    @XmlElement(name = "Sens")
    public String getSens() {
        return sens;
    }

    public void setSens(final String sens) {
        this.sens = sens;
    }

    @XmlElement(name = "Statut")
    public String getStatut() {
        return statut;
    }

    public void setStatut(final String statut) {
        this.statut = statut;
    }

    @XmlElement(name = "TypeEntite")
    public String getTypeEntite() {
        return typeEntite;
    }

    public void setTypeEntite(final String typeEntite) {
        this.typeEntite = typeEntite;
    }

    @XmlElement(name = "DateCreation")
    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(final String dateCreation) {
        this.dateCreation = dateCreation;
    }

    @XmlElement(name = "DateModification")
    public String getDateModification() {
        return dateModification;
    }

    public void setDateModification(final String dateModification) {
        this.dateModification = dateModification;
    }

    @XmlElement(name = "DateInscription")
    public String getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(final String dateInscription) {
        this.dateInscription = dateInscription;
    }

    @XmlElement(name = "NombrePA")
    public Long getNombrePA() {
        return nombrePA;
    }

    public void setNombrePA(final Long nombrePA) {
        this.nombrePA = nombrePA;
    }

    @XmlElement(name = "PA")
    public List<PisteAudit> getPisteAudits() {
        return pisteAudits;
    }

    public void setPisteAudits(final List<PisteAudit> pisteAudits) {
        this.pisteAudits = pisteAudits;
    }

    @XmlElement(name = "NombreOP")
    public Long getNombreOP() {
        return nombreOP;
    }

    public void setNombreOP(final Long nombreOP) {
        this.nombreOP = nombreOP;
    }

    @XmlElement(name = "Operation")
    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(final List<Operation> operations) {
        this.operations = operations;
    }

}
