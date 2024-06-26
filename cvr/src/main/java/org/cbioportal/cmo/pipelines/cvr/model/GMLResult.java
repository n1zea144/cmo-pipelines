/*
 * Copyright (c) 2016, 2017, 2024 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan Kettering Cancer
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

/**
 *
 * @author jake
 */

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "cnv-intragenic-variants-gml",
    "cnv-variants-gml",
    "meta-data",
    "snp-indel-gml"
})
public class GMLResult {
    @JsonProperty("cnv-intragenic-variants-gml")
    private List<GMLCnvIntragenicVariant> cnvIntragenicVariantsGml;
    @JsonProperty("cnv-variants-gml")
    private List<CVRCnvVariant> cnvVariantsGml;
    @JsonProperty("meta-data")
    private GMLMetaData metaData;
    @JsonProperty("snp-indel-gml")
    private List<GMLSnp> snpIndelGml;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public GMLResult() {
    }

    public GMLResult(List<GMLCnvIntragenicVariant> cnvIntragenicVariantsGml,
            List<CVRCnvVariant> cnvVariantsGml,
            GMLMetaData metaData,
            List<GMLSnp> snpIndelGml) {
        this.cnvIntragenicVariantsGml = cnvIntragenicVariantsGml;
        this.cnvVariantsGml = cnvVariantsGml;
        this.metaData = metaData;
        this.snpIndelGml = snpIndelGml;
    }

    @JsonProperty("cnv-intragenic-variants-gml")
    public List<GMLCnvIntragenicVariant> getCnvIntragenicVariantsGml() {
        return cnvIntragenicVariantsGml != null ? cnvIntragenicVariantsGml : new ArrayList();
    }

    @JsonProperty("cnv-intragenic-variants-gml")
    public void setCnvIntragenicVariantsGml(List<GMLCnvIntragenicVariant> cnvIntragenicVariantsGml) {
        this.cnvIntragenicVariantsGml = cnvIntragenicVariantsGml;
    }

    @JsonProperty("cnv-variants-gml")
    public List<CVRCnvVariant> getCnvVariantsGml() {
        return cnvVariantsGml != null ? cnvVariantsGml : new ArrayList();
    }

    @JsonProperty("cnv-variants-gml")
    public void setCnvVariantsGml(List<CVRCnvVariant> cnvVariantsGml) {
        this.cnvVariantsGml = cnvVariantsGml;
    }

    @JsonProperty("meta-data")
    public GMLMetaData getMetaData() {
        return metaData;
    }

    @JsonProperty("meta-data")
    public void setMetaData(GMLMetaData metaData) {
        this.metaData = metaData;
    }

    @JsonProperty("snp-indel-gml")
    public List<GMLSnp> getSnpIndelGml() {
        return snpIndelGml != null ? snpIndelGml : new ArrayList();
    }

    @JsonProperty("snp-indel-gml")
    public void setSnpIndelGml(List<GMLSnp> snpIndelGml) {
        this.snpIndelGml = snpIndelGml;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public GMLResult withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    public List<GMLSnp> getAllSignedoutGmlSnps() {
        List<GMLSnp> signedoutSnps = new ArrayList<>();
        for (GMLSnp snp : snpIndelGml) {
            if (snp.getClinicalSignedOut().equals("1")) {
                signedoutSnps.add(snp);
            }
        }
        return signedoutSnps;
    }
}
