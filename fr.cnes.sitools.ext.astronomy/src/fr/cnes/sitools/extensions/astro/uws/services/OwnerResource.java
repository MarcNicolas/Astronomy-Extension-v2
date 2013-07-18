 /*******************************************************************************
 * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of SITools2.
 *
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.extensions.astro.uws.services;

import fr.cnes.sitools.extensions.astro.uws.representation.JobOwnerRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.ext.wadl.RepresentationInfo;
import org.restlet.ext.wadl.RequestInfo;
import org.restlet.ext.wadl.ResponseInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;


/**
 * Resource to handle Owner
 * @author Jean-Christophe Malapert
 */
public class OwnerResource extends BaseJobResource {

    @Override
    public void doInit() throws ResourceException {
        super.doInit();
        setName("Owner Resource");
        setDescription("This resource handles job owner");
    }

    /**
     * Get an owner
     * @return Returns the owner Representation
     * @exception ResourceException Returns a HTTP Status 404 when the jobId is not found
     * @exception ResourceException Returns a HTTP Status 500 for an Internal Server Error
     */
    @Get
    public Representation getOwner() {
        return new JobOwnerRepresentation(getJobTask(),true);
    }

    @Override
    protected Representation describe() {
        setName("Owner Resource");
        setDescription("This resource handles job owner");
        return super.describe();
    }

    @Override
    protected void describeGet(MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Getting an ownerId");

        ResponseInfo responseInfo = new ResponseInfo();
        List<RepresentationInfo> repsInfo = new ArrayList<RepresentationInfo>();
        RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setXmlElement("xs:string");
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("Owner");
        docInfo.setTextContent("the owner (creator) of the job - this should be expressed as a string that can be parsed in accordance with IVOA security standards. If there was no authenticated job creator then this should be set to NULL.");
        repInfo.setDocumentation(docInfo);
        repsInfo.add(repInfo);
        responseInfo.setRepresentations(repsInfo);
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        info.getResponses().add(responseInfo);

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        responseInfo.setDocumentation("Job does not exist");
        info.getResponses().add(responseInfo);

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(responseInfo);

        RequestInfo request = new RequestInfo();
        ParameterInfo param = new ParameterInfo();
        param.setStyle(ParameterStyle.TEMPLATE);
        param.setName("job-id");
        param.setDocumentation("job-id value");
        param.setRequired(true);
        param.setType("xs:string");
        request.getParameters().add(param);
        info.setRequest(request);
    }
}
