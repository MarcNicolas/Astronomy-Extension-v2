/*
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
 */

package fr.ias.sitools.ias.vo.cutOut;

// DEBUT DES IMPORTS
import fr.cnes.sitools.astro.vo.sia.AbstractSqlGeometryConstraint;
import fr.cnes.sitools.astro.vo.sia.SimpleImageAccessInputParameters;
import fr.cnes.sitools.astro.vo.sia.SimpleImageAccessProtocolLibrary;
import fr.cnes.sitools.astro.vo.sia.SqlGeometryFactory;
import fr.cnes.sitools.common.exception.SitoolsException;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.database.DatabaseRequest;
import fr.cnes.sitools.dataset.database.DatabaseRequestFactory;
import fr.cnes.sitools.dataset.database.DatabaseRequestParameters;
import fr.cnes.sitools.dataset.database.common.DataSetExplorerUtil;
import fr.cnes.sitools.dataset.model.Predicat;
import fr.cnes.sitools.datasource.jdbc.model.AttributeValue;
import fr.cnes.sitools.datasource.jdbc.model.Record;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.util.Util;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.ias.sitools.server.Constante.Const;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Context;
import org.restlet.Request;
// FIN DES IMPORTS

/**
 *
 * @author Mnicolas@IAS
 */
public class CutOutVoResource extends SitoolsParameterizedResource {
    
    /**
   * Logger.
   */
    private static final Logger LOG = Logger.getLogger(CutOutVoResource.class.getName());
   
    /**
   * Data model that stores the metadata response of the service.
   */
    private final transient Map dataModel = new HashMap();
    
    /**
     * Request.
    */
    private final transient Request request;
    
    /**
    * Context.
    */
    private final transient Context context;
    
    /**
    * Application where this resources is linked.
    */
    private final transient DataSetApplication datasetApp;
    
    /**
    * Configuration parameters of this resource.
    */
    private final transient ResourceModel resourceModel;
    
    /**
     * The url of the Sitools2 Instance
     */
    private final transient String urlHostDomain = getSitoolsSetting(Const.APP_HOST_DOMAIN);
    
    /**
     * Url du du CutOut Services pour un ds
     */
    private final transient String apiStringListBox = "?1=1&amp;p[0]=LISTBOXMULTIPLE|";
        
    /**
     * The Url od the DataSet
     */
    private final String urlDs;
    
    /**
     * The Complete Url of the cut fits
     */
    private final String urlServicesCutOut;
    
    /**
     * The Url Of the PlugIn Cut Out
     */
    private final String urlPlugInCutOut;
    
    /**
     * @param request
     * @param context
     * @param datasetApp
     * @param resourceModel 
     */
    // CONSTRUCTEURS
    public CutOutVoResource(Request request, Context context, DataSetApplication datasetApp, ResourceModel resourceModel) {
        this.request = request;
        this.context = context;
        this.datasetApp = datasetApp;
        this.resourceModel = resourceModel;
        this.urlDs = this.datasetApp.getDataSet().getSitoolsAttachementForUsers();
        this.urlPlugInCutOut = this.resourceModel.getParameterByName("urlCutOutService").getValue();
        this.urlServicesCutOut = this.urlHostDomain+this.urlDs+this.urlPlugInCutOut+this.apiStringListBox;
    }

    public HashMap execute(){
        // Init de la HashMap qui contiendra les primarykey et les urls des fits coupés correspondant
        HashMap<String,String> urlCutFitsFiles = new HashMap<String,String>();
        // On récupère les paramètres de la requetes SIA
        final String posInput = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.POS);
        final String sizeInput = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.SIZE);
        
        // On prépare la requete sur la base
        final DataSetExplorerUtil dsExplorerUtil = new DataSetExplorerUtil(this.datasetApp, this.request,this.context);
        final SimpleImageAccessInputParameters inputParameters = new SimpleImageAccessInputParameters(datasetApp, request, this.context, this.resourceModel);
        
        // Get query parameters
        final DatabaseRequestParameters dbParams = dsExplorerUtil.getDatabaseParams();
        final AbstractSqlGeometryConstraint sql = SqlGeometryFactory.create(String.valueOf(this.resourceModel.getParameterByName(SimpleImageAccessProtocolLibrary.INTERSECT).getValue()));
        sql.setInputParameters(inputParameters);
        final Object geometry = (Util.isNotEmpty(this.resourceModel.getParameterByName(SimpleImageAccessProtocolLibrary.GEO_ATTRIBUT).getValue()))
                      ? this.resourceModel.getParameterByName(SimpleImageAccessProtocolLibrary.GEO_ATTRIBUT).getValue()
                      : Arrays.asList("crval1", "crval2");
        sql.setGeometry(geometry);

        if (sql.getSqlPredicat() != null) {
            final Predicat predicat = new Predicat();
            predicat.setStringDefinition(sql.getSqlPredicat());
            final List<Predicat> predicatList = dbParams.getPredicats();
            predicatList.add(predicat);
            dbParams.setPredicats(predicatList);
        }
        
        // Get dataset records
        DatabaseRequest databaseRequest  = DatabaseRequestFactory.getDatabaseRequest(dbParams);
        try {
            // Execute query
        
            databaseRequest.createRequest();

            String primaryKeyName = databaseRequest.getPrimaryKeys().get(0);
            String urlCutFitsFile = null;
            String fileNameCut = null;
            
            while (databaseRequest.nextResult()){
                String primaryKeyValue = null;
                Record recs = databaseRequest.getRecord();
                List<AttributeValue> listRec = recs.getAttributeValues();
                for(AttributeValue rec : listRec){
                  if(rec.getName().equalsIgnoreCase("filename")){
                      //fileName = rec.getValue().toString();
                      fileNameCut = new StringBuilder(rec.getValue().toString()).insert(rec.getValue().toString().lastIndexOf("."), "_cut_"+sizeInput+"deg").toString();
                  }else if(rec.getName().equalsIgnoreCase(primaryKeyName)){
                      primaryKeyValue = rec.getValue().toString();
                  }
                }
                urlCutFitsFile = this.urlServicesCutOut+primaryKeyName+"%7C"+primaryKeyValue+"&amp;fileName="+fileNameCut+"&amp;RA="+posInput.split(",")[0]+"&amp;DEC="+posInput.split(",")[1]
                    +"&amp;Radius="+sizeInput+"&amp;OutputFormat=FITS";
                urlCutFitsFiles.put(primaryKeyValue, urlCutFitsFile);   
            }
        } catch (SitoolsException ex) {
            Logger.getLogger(CutOutVoResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return urlCutFitsFiles;
    }

    
    /**
     * Getter of the Url services Cut Out
     * @return url of the services Cut Out
     */
    public String getUrlServicesCutOut() {
        return this.urlServicesCutOut;
    }

    /**
     * Getter of the Url Cut Out Plug In
     * @return url of the plugin Cut Out
     */
    public String getUrlPlugInCutOut() {
        return this.urlPlugInCutOut;
    }

}