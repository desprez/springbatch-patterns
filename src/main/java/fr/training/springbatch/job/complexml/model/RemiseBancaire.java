package fr.training.springbatch.job.complexml.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RemiseBancaire")
public class RemiseBancaire extends Record {

	private String bicBeneficiaire;

	private String bicEmetteur;

	private String bicDestinataire;

	private String canalTrans;

	private String circuitRglt103;

	private String codeApplication;

	private String dateMessage;

	private String famille;

	private String identifiantInterne;

	private Long nbreOpeAttentes;

	private Long nbreOperejetees;

	private Long nbreOpetraitees;

	private Long nbreOpetotalRC;

	private String origine;

	private String referenceExterne;

	private String sens;

	private String statut;

	private String dateCreation;

	private String dateModification;

	private String dateInscription;

	private Long nombrePA;

	private List<PisteAudit> pisteAudits;

	private List<RemiseHomogene> remiseHomogenes;

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

	@XmlElement(name = "CanalTrans")
	public String getCanalTrans() {
		return canalTrans;
	}

	public void setCanalTrans(final String canalTrans) {
		this.canalTrans = canalTrans;
	}

	@XmlElement(name = "CircuitRglt103")
	public String getCircuitRglt103() {
		return circuitRglt103;
	}

	public void setCircuitRglt103(final String circuitRglt103) {
		this.circuitRglt103 = circuitRglt103;
	}

	@XmlElement(name = "CodeApplication")
	public String getCodeApplication() {
		return codeApplication;
	}

	public void setCodeApplication(final String codeApplication) {
		this.codeApplication = codeApplication;
	}

	@XmlElement(name = "DateMessage")
	public String getDateMessage() {
		return dateMessage;
	}

	public void setDateMessage(final String dateMessage) {
		this.dateMessage = dateMessage;
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

	@XmlElement(name = "NbreOpetotalRC")
	public Long getNbreOpetotalRC() {
		return nbreOpetotalRC;
	}

	public void setNbreOpetotalRC(final Long nbreOpetotalRC) {
		this.nbreOpetotalRC = nbreOpetotalRC;
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

	@XmlElement(name = "RemiseHomogene")
	public List<RemiseHomogene> getRemiseHomogenes() {
		return remiseHomogenes;
	}

	public void setRemiseHomogenes(final List<RemiseHomogene> remiseHomogenes) {
		this.remiseHomogenes = remiseHomogenes;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("RemiseBancaire [bicBeneficiaire=").append(bicBeneficiaire).append(", bicEmetteur=")
		.append(bicEmetteur).append(", bicDestinataire=").append(bicDestinataire).append(", canalTrans=")
		.append(canalTrans).append(", circuitRglt103=").append(circuitRglt103).append(", codeApplication=")
		.append(codeApplication).append(", dateMessage=").append(dateMessage).append(", famille=")
		.append(famille).append(", identifiantInterne=").append(identifiantInterne).append(", nbreOpeAttentes=")
		.append(nbreOpeAttentes).append(", nbreOperejetees=").append(nbreOperejetees)
		.append(", nbreOpetraitees=").append(nbreOpetraitees).append(", nbreOpetotalRC=").append(nbreOpetotalRC)
		.append(", origine=").append(origine).append(", referenceExterne=").append(referenceExterne)
		.append(", sens=").append(sens).append(", statut=").append(statut).append(", dateCreation=")
		.append(dateCreation).append(", dateModification=").append(dateModification)
		.append(", dateInscription=").append(dateInscription).append(", nombrePA=").append(nombrePA)
		.append(", pisteAudits=").append(pisteAudits).append(", remiseHomogenes=").append(remiseHomogenes)
		.append("]");
		return builder.toString();
	}



}
