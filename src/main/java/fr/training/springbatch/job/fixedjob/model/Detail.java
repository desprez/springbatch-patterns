package fr.training.springbatch.job.fixedjob.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Detail extends AbstractLine {

	private String boaId;
	private String contractType;
	private String headquarter;
	private String account;
	private String key;
	private String iban;
	private String currency;
	private String fluxId;
	private String docId;
	private String productionDate;
	private String docType;
	private String sendState;
	private String sendDate;
	private String groupDate;
	private String groupCode;
	private String anomalyCode;
	private String anomalyLabel;
	private String anomalyDate;
	private String anomalyCount;
	private String recyclingDate1;
	private String recyclingDate2;
	private String recyclingDate3;
	private String recyclingDate4;
	private String recyclingDate5;
	private String bicCode;

	public Detail() {

	}

	public Detail(final String recordType) {
		super("01");
	}

	public String getBoaId() {
		return boaId;
	}

	public void setBoaId(final String boaId) {
		this.boaId = boaId;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(final String contractType) {
		this.contractType = contractType;
	}

	public String getHeadquarter() {
		return headquarter;
	}

	public void setHeadquarter(final String headquarter) {
		this.headquarter = headquarter;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(final String account) {
		this.account = account;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(final String iban) {
		this.iban = iban;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(final String currency) {
		this.currency = currency;
	}

	public String getFluxId() {
		return fluxId;
	}

	public void setFluxId(final String fluxId) {
		this.fluxId = fluxId;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(final String docId) {
		this.docId = docId;
	}

	public String getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(final String productionDate) {
		this.productionDate = productionDate;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(final String docType) {
		this.docType = docType;
	}

	public String getSendState() {
		return sendState;
	}

	public void setSendState(final String sendState) {
		this.sendState = sendState;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(final String sendDate) {
		this.sendDate = sendDate;
	}

	public String getGroupDate() {
		return groupDate;
	}

	public void setGroupDate(final String groupDate) {
		this.groupDate = groupDate;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(final String groupCode) {
		this.groupCode = groupCode;
	}

	public String getAnomalyCode() {
		return anomalyCode;
	}

	public void setAnomalyCode(final String anomalyCode) {
		this.anomalyCode = anomalyCode;
	}

	public String getAnomalyLabel() {
		return anomalyLabel;
	}

	public void setAnomalyLabel(final String anomalyLabel) {
		this.anomalyLabel = anomalyLabel;
	}

	public String getAnomalyDate() {
		return anomalyDate;
	}

	public void setAnomalyDate(final String anomalyDate) {
		this.anomalyDate = anomalyDate;
	}

	public String getAnomalyCount() {
		return anomalyCount;
	}

	public void setAnomalyCount(final String anomalyCount) {
		this.anomalyCount = anomalyCount;
	}

	public String getRecyclingDate1() {
		return recyclingDate1;
	}

	public void setRecyclingDate1(final String recyclingDate1) {
		this.recyclingDate1 = recyclingDate1;
	}

	public String getRecyclingDate2() {
		return recyclingDate2;
	}

	public void setRecyclingDate2(final String recyclingDate2) {
		this.recyclingDate2 = recyclingDate2;
	}

	public String getRecyclingDate3() {
		return recyclingDate3;
	}

	public void setRecyclingDate3(final String recyclingDate3) {
		this.recyclingDate3 = recyclingDate3;
	}

	public String getRecyclingDate4() {
		return recyclingDate4;
	}

	public void setRecyclingDate4(final String recyclingDate4) {
		this.recyclingDate4 = recyclingDate4;
	}

	public String getRecyclingDate5() {
		return recyclingDate5;
	}

	public void setRecyclingDate5(final String recyclingDate5) {
		this.recyclingDate5 = recyclingDate5;
	}

	public String getBicCode() {
		return bicCode;
	}

	public void setBicCode(final String bicCode) {
		this.bicCode = bicCode;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("boaId", getBoaId()) //
				.append("contractType", getContractType()) //
				.append("headquarter", getHeadquarter()) //
				.append("account", getAccount()) //
				.append("key", getKey()) //
				.append("iban", getIban()) //
				.append("currency", getCurrency()) //
				.append("fluxId", getFluxId()) //
				.append("docId", getDocId()) //
				.append("productionDate", getProductionDate()) //
				.append("docType", getDocType()) //
				.append("sendState", getSendState()) //
				.append("sendDate", getSendDate()) //
				.append("groupDate", getGroupDate()) //
				.append("groupCode", getGroupCode()) //
				.append("anomalyCode", getAnomalyCode()) //
				.append("anomalyLabel", getAnomalyLabel()) //
				.append("anomalyDate", getAnomalyDate()) //
				.append("anomalyCount", getAnomalyCount()) //
				.append("recyclingDate1", getRecyclingDate1()) //
				.append("recyclingDate2", getRecyclingDate2()) //
				.append("recyclingDate3", getRecyclingDate3()) //
				.append("recyclingDate4", getRecyclingDate4()) //
				.append("recyclingDate5", getRecyclingDate5()) //
				.append("bicCode", getBicCode()) //
				.toString();
	}

}
