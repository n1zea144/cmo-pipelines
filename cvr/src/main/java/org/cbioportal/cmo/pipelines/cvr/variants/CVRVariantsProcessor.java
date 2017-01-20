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

package org.cbioportal.cmo.pipelines.cvr.variants;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.cbioportal.cmo.pipelines.cvr.CVRUtilities;
import org.cbioportal.cmo.pipelines.cvr.model.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author heinsz
 */
public class CVRVariantsProcessor implements ItemProcessor<CVRVariants, String> {

    @Value("${dmp.server_name}")
    private String dmpServerName;
    
    @Value("${dmp.tokens.retrieve_segment_data}")
    private String dmpRetrieveSegmentData;
    
    @Value("${dmp.tokens.consume_sample}")
    private String dmpConsumeSample;
    
    @Value("#{jobParameters[sessionId]}")
    private String sessionId;
    
    @Value("#{jobParameters[skipSeg]}")
    private boolean skipSeg;
    
    @Value("#{jobParameters[testingMode]}")
    private boolean testingMode;

    Logger log = Logger.getLogger(CVRVariantsProcessor.class);

    @Autowired
    public CVRUtilities cvrUtilities;

    private String dmpSegmentUrl;
    private String dmpConsumeUrl;

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = getRequestEntity();

    // Need to call get_seg_data against the CVR webservice for every sample, then merge the results together (CVRMergedResult)
    // All of these get put into the cvrData object, which contains everything and is what get sent to the writer
    @Override
    public String process(CVRVariants i) throws Exception {
        dmpSegmentUrl = dmpServerName + dmpRetrieveSegmentData + "/" + sessionId + "/";
        dmpConsumeUrl = dmpServerName + dmpConsumeSample + "/" + sessionId + "/";
        HashMap<String, Object> results = i.getResults();
        CVRData cvrData = new CVRData(i.getSampleCount(), i.getDisclaimer(), new ArrayList<CVRMergedResult>());
        ObjectMapper mapper = new ObjectMapper();
        Iterator it = results.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String sampleId = (String)pair.getKey();
            cvrUtilities.addNewId(sampleId);
            cvrUtilities.addAllIds(sampleId);
            CVRResult result = (CVRResult)pair.getValue();
            CVRSegData segData = new CVRSegData();
            if (!skipSeg) {
                segData = getSegmentData(sampleId);
            }
            if (segData != null) {
                if (!testingMode) {
                    consumeSample(sampleId);
                }
                CVRMergedResult mergedResult = new CVRMergedResult(result, segData);
                cvrData.addResult(mergedResult);
            }
        }
        // All the merged data is sent as a json string to the writer at this point
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cvrData);
    }

    private HttpEntity getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<Object>(headers);
    }

    private void consumeSample(String sampleId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CVRConsumeSample> responseEntity;
        try {
            responseEntity = restTemplate.exchange(dmpConsumeUrl + sampleId, HttpMethod.GET, requestEntity, CVRConsumeSample.class);
            if (responseEntity.getBody().getaffectedRows()==0) {
                String message = "No consumption for sample " + sampleId;
                log.warn(message);
            }
            if (responseEntity.getBody().getaffectedRows()>1) {
                String message = "Multple samples consumed (" + responseEntity.getBody().getaffectedRows() + ") for sample " + sampleId;
                log.warn(message);
            }
            if (responseEntity.getBody().getaffectedRows()==1) {
                String message = "Sample "+ sampleId +" consumed succesfully";
                log.info(message);
            }
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Error consuming sample " + sampleId + "(" + e + ")");
        }

    }

    private CVRSegData getSegmentData(String sampleId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CVRSegData> responseEntity;
        try {
            responseEntity = restTemplate.exchange(dmpSegmentUrl + sampleId, HttpMethod.GET, requestEntity, CVRSegData.class);
        } catch (org.springframework.web.client.RestClientException e) {
            String message = "Error getting seg data for sample: " + sampleId;
            log.warn(message);
            return null;
        }
        return responseEntity.getBody();
    }
}
