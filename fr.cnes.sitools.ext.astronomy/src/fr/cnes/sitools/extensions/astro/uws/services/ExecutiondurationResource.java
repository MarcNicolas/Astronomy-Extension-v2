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

import fr.cnes.sitools.extensions.astro.uws.UwsApplicationPlugin;
import fr.cnes.sitools.extensions.astro.uws.common.UniversalWorkerException;
import fr.cnes.sitools.extensions.astro.uws.jobmanager.JobTaskManager;
import fr.cnes.sitools.extensions.astro.uws.representation.JobExecutionDurationRepresentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.ivoa.xml.uws.v1.ExecutionPhase;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Resource to handle Execution Duration
 * @author Jean-Christophe Malapert
 */
public class ExecutiondurationResource extends BaseJobResource {

    @Override
    public void doInit() throws ResourceException {
        super.doInit();
        CopyOnWriteArraySet<Method> allowedMethods = new CopyOnWriteArraySet<Method>();
        allowedMethods.add(Method.GET);
        if (((UwsApplicationPlugin) getApplication()).isAllowedExecutionTimePostMethod()) {
            allowedMethods.add(Method.POST);
        }
        setAllowedMethods(allowedMethods);
        setName("Execution duration Resource");
        setDescription("This resource handles execution time");
    }

    /**
     * Get a the expected execution duration as an integer
     * The server returns a HTTP Status 200 when the operation is completed
     * @return Returns the execution duration as an integer
     * @exception ResourceException Returns a HTTP Status 404 when job-id is unknown
     * @exception ResourceException Returns a HTTP Status 500 for an Internal Server Error
     */
    @Get("plain")
    public Representation getExecutionDuration() {
        setStatus(Status.SUCCESS_OK);
        return new JobExecutionDurationRepresentation(getJobTask(), true);
    }

    /**
     * Accept an execution duration
     * @param form form that contains only EXECUDTIONDURATION parameter
     * Redirects to /{jobId}
     * @exception ResourceException Returns a HTTP Status 400 when the Form is not valid
     * @exception ResourceException Returns a HTTP Status 403 when the job is not PENDING or operation is undefined
     * @exception ResourceException Returns a HTTP Status 404 when job-id is unknown
     * @exception ResourceException Returns a HTTP Status 500 for an Internal Server Error
     */
    @Post("form")
    public void acceptExecutionDuration(Form form) throws ResourceException {
        try {
            Set<Method> methods = getAllowedMethods();
            if (!methods.contains(Method.POST)) {
                throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            }
            if (isValidAction()) {
                if (isValidAction(form)) {
                    Parameter parameter = form.get(0);
                    JobTaskManager.getInstance().setExecutionTime(getJobTask(), Integer.valueOf(parameter.getValue()));
                    this.redirectToJobID();
                } else {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Parameters are not valid");
                }
            } else {
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Destruction time can only be set during PENDING phase");
            }
        } catch (UniversalWorkerException ex) {
            throw new ResourceException(ex.getStatus(), ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Check whether the POST action is allowed.
     * The POST action is only allowed while the job is being PENDING
     * @return Returns True when the POST action is allowed otherwise False
     * @exception UniversalWorkerException
     */
    protected final boolean isValidAction() throws UniversalWorkerException {
        ExecutionPhase phase = JobTaskManager.getInstance().getStatus(getJobTask());
        return (phase.equals(phase.PENDING)) ? true : false;
    }

    /**
     * Check whether the POST action is allowed.
     * The POST action is not allowed :
     *  - while the job is not being PENDING
     *  - when DESTRUCTIONTIME >= 0
     * @param form Form send by a user
     * @return Returns True when the Form object is valid otherwhise False
     */
    protected boolean isValidAction(Form form) {
        boolean isValid = false;

        if (form == null || form.size() > 1) {
            isValid = false;
        } else {
            Parameter param = form.get(0);
            isValid = (param.getName().equals(fr.cnes.sitools.extensions.astro.uws.common.Constants.EXECUTIONDURATION)
                    && isParsableToInt(param.getValue())
                    && Integer.valueOf(param.getValue()) >= 0) ? true : false;
        }

        return isValid;
    }

    protected final boolean isParsableToInt(String i) {
        try {
            Integer.parseInt(i);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
    protected Representation describe() {
        setName("Execution duration Resource");
        setDescription("This resource handles execution time");
        return super.describe();
    }

    @Override
    protected void describeGet(MethodInfo info) {
        info.setName(Method.GET);
        info.setDocumentation("Get the execution duration");

        ResponseInfo responseInfo = new ResponseInfo();
        List<RepresentationInfo> repsInfo = new ArrayList<RepresentationInfo>();
        RepresentationInfo repInfo = new RepresentationInfo();
        repInfo.setXmlElement("xs:int");
        repInfo.setMediaType(MediaType.TEXT_PLAIN);
        DocumentationInfo docInfo = new DocumentationInfo();
        docInfo.setTitle("ExecutionDuration");
        docInfo.setTextContent("The duration (in seconds) for which the job should be allowed to run - a value of 0 is intended to mean unlimited - returned at /(jobs)/(jobid)/executionduration");
        repInfo.setDocumentation(docInfo);
        repsInfo.add(repInfo);
        responseInfo.setRepresentations(repsInfo);
        responseInfo.getStatuses().add(Status.SUCCESS_OK);
        info.getResponses().add(responseInfo);

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.SERVER_ERROR_INTERNAL);
        info.getResponses().add(responseInfo);

        responseInfo = new ResponseInfo();
        responseInfo.getStatuses().add(Status.CLIENT_ERROR_NOT_FOUND);
        responseInfo.setDocumentation("Job does not exist");
        info.getResponses().add(responseInfo);

        RequestInfo request = new RequestInfo();
        ParameterInfo param = new ParameterInfo();
        param.setStyle(ParameterStyle.TEMPLATE);
        param.setName("job-id");
        param.setDocumentation("job-id value");
        param.setRequired(true);
        param.setType("xs:int");
        request.getParameters().add(param);
        info.setRequest(request);
    }

    @Override
    protected void describePost(MethodInfo info) {
        info.setName(Method.POST);
        info.setDocumentation("Changing the Execution Duration");

        ResponseInfo responseInfo = null;
        if (((UwsApplicationPlugin) getApplication()).isAllowedExecutionTimePostMethod()) {
            responseInfo = new ResponseInfo();
            responseInfo.getStatuses().add(Status.REDIRECTION_SEE_OTHER);
            responseInfo.setDocumentation("Redirects to /{job-id}");
            info.getResponses().add(responseInfo);

            responseInfo = new ResponseInfo();
            responseInfo.getStatuses().add(Status.CLIENT_ERROR_BAD_REQUEST);
            responseInfo.setDocumentation("executionTime parameter is not valid");
            info.getResponses().add(responseInfo);

            responseInfo = new ResponseInfo();
            responseInfo.getStatuses().add(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            responseInfo.setDocumentation("Execution time cannot be modified when the job is not PENDING");
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
            param.setName("EXECUTIONTIME");
            param.setStyle(ParameterStyle.QUERY);
            param.setDocumentation("Set the execution Time in seconds");
            param.setRequired(true);
            param.setType("xs:string");
            request.getParameters().add(param);
            param = new ParameterInfo();
            param.setStyle(ParameterStyle.TEMPLATE);
            param.setName("job-id");
            param.setDocumentation("job-id value");
            param.setRequired(true);
            param.setType("xs:string");
            request.getParameters().add(param);
            info.setRequest(request);
        } else {
            responseInfo = new ResponseInfo();
            responseInfo.getStatuses().add(Status.CLIENT_ERROR_FORBIDDEN);
            responseInfo.setDocumentation("The client is not allowed to change the executionTime value");
            info.getResponses().add(responseInfo);
        }
        super.describePost(info);
    }
}
