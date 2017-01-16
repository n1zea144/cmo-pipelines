/*
 * Copyright (c) 2016 - 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal CMO-Pipelines.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.cmo.pipelines.cvr.model;

import java.util.*;

/**
 *
 * @author heinsz
 */
public class CVRClinicalRecord {

    private String sampleId;
    private String patientId;
    private String cancerType;
    private String sampleType;
    private String sampleClass;
    private String metastaticSite;
    private String primarySite;
    private String cancerTypeDetailed;
    private String genePanel;
    private String otherSampleId;
    private String otherPatientId;
    private String sex;
    private String soComments;
    private String sampleCoverage;
    private String tumorPurity;
    private String oncotreeCode;
    private String partCConsented;
    private String msiComment;
    private String msiScore;
    private String msiType;
    private String institute;
    private String somaticStatus;
    private String isNew;

    private final String DEFAULT_SAMPLE_CLASS = "Tumor";

    public CVRClinicalRecord(CVRMetaData metaData) {
        this.sampleId = metaData.getDmpSampleId();
        this.patientId = metaData.getDmpPatientId();
        this.cancerType = metaData.getTumorTypeName();
        this.sampleType = this.resolveSampleType(metaData.getIsMetastasis());
        this.sampleClass = DEFAULT_SAMPLE_CLASS;
        this.metastaticSite = metaData.getMetastasisSite();
        this.primarySite = metaData.getPrimarySite();
        this.cancerTypeDetailed = metaData.getTumorTypeName();
        this.genePanel = metaData.getGenePanel();
        this.otherSampleId = metaData.getLegacySampleId();
        this.otherPatientId = metaData.getLegacyPatientId();
        this.sex = this.resolveSex(metaData.getGender());
        this.soComments = metaData.getSoComments();
        this.sampleCoverage = String.valueOf(metaData.getSampleCoverage());
        this.tumorPurity = metaData.getTumorPurity();
        this.oncotreeCode = metaData.getTumorTypeCode();
        this.partCConsented = "NO";
        this.msiComment = metaData.getMsiComment();
        this.msiScore = metaData.getMsiScore();
        this.msiType = metaData.getMsiType();
        this.institute = metaData.getOutsideInstitute();
        this.somaticStatus = metaData.getSomaticStatus();
    }

    public CVRClinicalRecord(GMLMetaData metaData) {
        this.sampleId = metaData.getDmpSampleId();
        this.patientId = metaData.getDmpPatientId();
        this.cancerType = "";
        this.sampleType = "";
        this.sampleClass = DEFAULT_SAMPLE_CLASS;
        this.metastaticSite = "";
        this.primarySite = "";
        this.cancerTypeDetailed = "";
        this.genePanel = metaData.getGenePanel();
        this.otherSampleId = metaData.getLegacySampleId();
        this.otherPatientId = metaData.getLegacyPatientId();
        this.sex = this.resolveSex(metaData.getGender());
        this.soComments = metaData.getSoComments();
        this.sampleCoverage = String.valueOf(metaData.getSampleCoverage());
        this.tumorPurity = "";
        this.oncotreeCode = "";
        this.partCConsented = "NO";
        this.msiComment = "";
        this.msiScore = "";
        this.msiType = "";
        this.institute = "";
    }

    public CVRClinicalRecord() {
    }

    public String getSAMPLE_ID() {
        return this.sampleId != null ? this.sampleId : "";
    }

    public void setSAMPLE_ID(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPATIENT_ID() {
        return this.patientId != null ? this.patientId : "";
    }

    public void setPATIENT_ID(String patientId) {
        this.patientId = patientId;
    }

    public String getCANCER_TYPE() {
        return this.cancerType != null ? this.cancerType : "";
    }

    public void setCANCER_TYPE(String cancerType) {
        this.cancerType = cancerType;
    }

    public String getSAMPLE_TYPE() {
        return this.sampleType != null ? this.sampleType : "";
    }

    public void setSAMPLE_TYPE(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getSAMPLE_CLASS() {
        return this.sampleClass != null ? this.sampleClass : "";
    }

    public void setSAMPLE_CLASS(String sampleClass) {
        this.sampleClass = sampleClass;
    }

    public String getMETASTATIC_SITE() {
        return this.metastaticSite != null ? this.metastaticSite : "";
    }

    public void setMETASTATIC_SITE(String metastaticSite) {
        this.metastaticSite = metastaticSite;
    }

    public String getPRIMARY_SITE() {
        return this.primarySite != null ? this.primarySite : "";
    }

    public void setPRIMARY_SITE(String primarySite) {
        this.primarySite = primarySite;
    }

    public String getCANCER_TYPE_DETAILED() {
        return this.cancerTypeDetailed != null ? this.cancerTypeDetailed : "";
    }

    public void setCANCER_TYPE_DETAILED(String cancerTypeDetailed) {
        this.cancerTypeDetailed = cancerTypeDetailed;
    }

    public String getGENE_PANEL() {
        return this.genePanel != null ? this.genePanel : "";
    }

    public void setGENE_PANEL(String genePanel) {
        this.genePanel = genePanel;
    }

    public String getOTHER_SAMPLE_ID() {
        return this.otherSampleId != null ? this.otherSampleId : "";
    }

    public void setOTHER_SAMPLE_ID(String otherSampleId) {
        this.otherSampleId = otherSampleId;
    }

    public String getOTHER_PATIENT_ID() {
        return this.otherPatientId != null ? this.otherPatientId : "";
    }

    public void setOTHER_PATIENT_ID(String otherPatientId) {
        this.otherPatientId = otherPatientId;
    }

    public String getSEX() {
        return this.sex != null ? this.sex : "";
    }

    public void setSEX(String sex) {
        this.sex = sex;
    }

    public String getSO_COMMENTS() {
        return this.soComments != null ? this.soComments : "";
    }

    public void setSO_COMMENTS(String soComments) {
        this.soComments = soComments;
    }

    public String getSAMPLE_COVERAGE() {
        return this.sampleCoverage != null ? this.sampleCoverage : "";
    }

    public void setSAMPLE_COVERAGE(String sampleCoverage) {
        this.sampleCoverage = sampleCoverage;
    }

    public String getTUMOR_PURITY() {
        return this.tumorPurity != null ? this.tumorPurity : "";
    }

    public void setTUMOR_PURITY(String tumorPurity) {
        this.tumorPurity = tumorPurity;
    }

    public String getONCOTREE_CODE() {
        return this.oncotreeCode != null ? this.oncotreeCode : "";
    }

    public void setONCOTREE_CODE(String oncotreeCode) {
        this.oncotreeCode = oncotreeCode;
    }

    public String get12_245_PARTC_CONSENTED() {
        return this.partCConsented != null ? this.partCConsented : "";
    }

    public void set12_245_PARTC_CONSENTED(String partCConsented) {
        this.partCConsented = partCConsented;
    }

    public String getMSI_COMMENT() {
        return this.msiComment != null ? this.msiComment : "";
    }

    public void setMSI_COMMENT(String msiComment) {
        this.msiComment = msiComment;
    }

    public String getMSI_SCORE() {
        return this.msiScore != null ? this.msiScore : "";
    }

    public void setMSI_SCORE(String msiScore) {
        this.msiScore = msiScore;
    }

    public String getMSI_TYPE() {
        return this.msiType != null ? this.msiType : "";
    }

    public void setMSI_TYPE(String msiType) {
        this.msiType = msiType;
    }

    public String getINSTITUTE() {
        // Assume MSKCC if not specified or -, the field from CVR is 'outside_institute'
        if (this.institute != null) {
            return this.institute.equals("-") ? "MSKCC" : this.institute;
        }
        return "MSKCC";
    }

    public void setINSTITUTE(String institute) {
        this.institute = institute;
    }

    public String getSOMATIC_STATUS() {
        return somaticStatus != null ? somaticStatus : "";
    }

    public void setSOMATIC_STATUS(String somaticStatus) {
        this.somaticStatus = somaticStatus;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public String getIsNew() {
        return this.isNew != null ? this.isNew : "";
    }

    private String resolveSampleType(Integer isMetastasis) {
        if (isMetastasis != null)
            return isMetastasis == 0 ? "Primary" : "Metastasis";
        return "";

    }

    private String resolveSex(Integer sex) {
        if (sex != null) {
            return sex == 0 ? "Female" : "Male";
        }
        return "NA";
    }

    public static List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("SAMPLE_ID");
        fieldNames.add("PATIENT_ID");
        fieldNames.add("CANCER_TYPE");
        fieldNames.add("SAMPLE_TYPE");
        fieldNames.add("SAMPLE_CLASS");
        fieldNames.add("METASTATIC_SITE");
        fieldNames.add("PRIMARY_SITE");
        fieldNames.add("CANCER_TYPE_DETAILED");
        fieldNames.add("GENE_PANEL");
        fieldNames.add("OTHER_SAMPLE_ID");
        fieldNames.add("OTHER_PATIENT_ID");
        fieldNames.add("SEX");
        fieldNames.add("SO_COMMENTS");
        fieldNames.add("SAMPLE_COVERAGE");
        fieldNames.add("TUMOR_PURITY");
        fieldNames.add("ONCOTREE_CODE");
        fieldNames.add("12_245_PARTC_CONSENTED");
        fieldNames.add("MSI_COMMENT");
        fieldNames.add("MSI_SCORE");
        fieldNames.add("MSI_TYPE");
        fieldNames.add("INSTITUTE");
        fieldNames.add("SOMATIC_STATUS");
        return fieldNames;
    }
}
