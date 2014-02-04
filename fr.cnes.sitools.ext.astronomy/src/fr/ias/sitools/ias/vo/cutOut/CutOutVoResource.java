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

import fr.cnes.sitools.astro.cutout.CutOutException;
import fr.cnes.sitools.astro.cutout.CutOutInterface;
import fr.cnes.sitools.astro.cutout.CutOutSITools2;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.representation.Representation;


// FIN DES IMPORTS

/**
 *
 * @author marc
 */
public class CutOutVoResource {
    
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
     * Url du storage des Cut Fits
     */
    private transient String urlStorageCutFits = "http://idoc-herschel-test.ias.u-psud.fr/sitools/datastorage/user/storageFitsCut/";
    /**
     * path du storage des Cut Fits
     */
    private transient String pathStorageCutFits = "/usr/local/Sitools2_Herschel_Test/data/CutFitsTmp/folderFitsCut/";
    /**
     * Url du du CutOut Services pour un ds
     */
    private final transient String urlServicesCutOut = "http://idoc-herschel-test.ias.u-psud.fr/ds/pub/pacsphotol2/plugin/cutOut?1=1&p[0]=LISTBOXMULTIPLE|";

    private boolean switchCutOut;
    // CONSTRUCTEURS
    public CutOutVoResource(Request request, Context context, DataSetApplication datasetApp, ResourceModel resourceModel) {
        this.request = request;
        this.context = context;
        this.datasetApp = datasetApp;
        this.resourceModel = resourceModel;
        this.switchCutOut = false;
    }
    public CutOutVoResource(Request request, Context context, DataSetApplication datasetApp, ResourceModel resourceModel,boolean switchCutOut) {
        this.request = request;
        this.context = context;
        this.datasetApp = datasetApp;
        this.resourceModel = resourceModel;
        this.switchCutOut = switchCutOut;
    }
    
    public List<String> execute(){
        List<String> urlCutFitsFiles = new ArrayList();
        LOG.severe("-------------------------------------------- Je suis dans execute!!");
        Representation rep = null;
        final String posInput = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.POS);
        final String sizeInput = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.SIZE);
        final String format = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.FORMAT);
        /*final String intersect = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.INTERSECT);
        final String verbosity = this.request.getResourceRef().getQueryAsForm().getFirstValue(SimpleImageAccessProtocolLibrary.VERB);
        *///----------------------- MODIFICATION MARC POUR LE CUT OUT----------------------------------
    
        final DataSetExplorerUtil dsExplorerUtil = new DataSetExplorerUtil(this.datasetApp, this.request,this.context);
        final SimpleImageAccessInputParameters inputParameters = new SimpleImageAccessInputParameters(datasetApp, request, this.context, this.resourceModel);
        
        // Get query parameters
        final DatabaseRequestParameters dbParams = dsExplorerUtil.getDatabaseParams();
        //TEST 2
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
        //
        // Get dataset records
        DatabaseRequest databaseRequest  = DatabaseRequestFactory.getDatabaseRequest(dbParams);

        // Execute query
        try {
            databaseRequest.createRequest();
           
            String fileName = null;
            String fileNameCut = null;
            
            
            //int nbDatas = databaseRequest.getCount();
            String primaryKeyName = databaseRequest.getPrimaryKeys().get(0);
            int k=0;
            
            String urlCutFitsFileTmp = null;
            while (databaseRequest.nextResult()){
                //if(k==2){
                    int primaryKeyValue = -1;
                    String urlFitsString = null;
                    Fits fitsFile= null;
                    URL urlFits = null;
                    Record rec = databaseRequest.getRecord();
                    List<AttributeValue> listRec = rec.getAttributeValues();
                    for(AttributeValue a : listRec){
                        
                        if(a.getName().equalsIgnoreCase("filename")){
                            fileName = a.getValue().toString();
                            fileNameCut = new StringBuilder(fileName).insert(fileName.lastIndexOf("."), "_cut_"+sizeInput+"deg").toString();
                        }else if(a.getName().equalsIgnoreCase("download")){
                            urlFitsString = a.getValue().toString();
                            urlFits = new URL(urlFitsString);
                            fitsFile = new Fits(urlFits);
                            
                        }else if(a.getName().equalsIgnoreCase(primaryKeyName)){
                            primaryKeyValue = Integer.parseInt(a.getValue().toString());
                        }
                    }

                    urlCutFitsFileTmp = urlServicesCutOut+primaryKeyName+"%7C"+primaryKeyValue+"&fileName="+fileNameCut+"&RA="+posInput.split(",")[0]+"&DEC="+posInput.split(",")[1]
                            +"&Radius="+sizeInput+"&OutputFormat=FITS";
                    //LOG.severe("****************************   urlCutFitsFileTmp : "+urlCutFitsFileTmp);
                    /*String urlTmp2 =  URLEncoder.encode(urlCutFitsFileTmp, "UTF-8");
                    LOG.severe("****************************   ENOCDE(urlCutFitsFileTmp) : "+urlTmp2);*/
                    urlCutFitsFiles.add(urlCutFitsFileTmp);
                    //urlCutFitsFiles.add(urlTmp2);
                    
                    if(this.switchCutOut){
                        LOG.info("----------- JE RENTRE DANS LE if(this.switchCutOut) -------------------");
                        CutOutInterface cutOut = new CutOutSITools2(fitsFile, Double.parseDouble(posInput.split(",")[0]), Double.parseDouble(posInput.split(",")[1]), Double.parseDouble(sizeInput));
    //                  String test = urlFitsString.substring(urlFitsString.lastIndexOf("/"))+"/"+fileNameCut;
    //                  String testURI = URLEncoder.encode(test);
                        //LOG.severe("************************************************************* urlCutFitsFileTmp : "+urlCutFitsFileTmp);
                       /* File f = new File(urlCutFitsFileTmp);
                        //LOG.severe("*********************************************************** testURI : "+testURI);
                        LOG.severe("*********************************************************** f.exists() : "+f.exists());
                        if(!f.exists()){
                            LOG.severe("*********************************************************** AVANT f.createNewFile()");
                            boolean fTest = f.createNewFile();
                            LOG.severe("***********************************************************  APRES f.createNewFile() : "+fTest);
                        }*/
                        File fitsCut = new File(pathStorageCutFits+fileNameCut);
                    
                        if(!fitsCut.exists()){
                            LOG.severe("*********************************************************** AVANT f.createNewFile()");
                            boolean fitsCutTestFile = fitsCut.createNewFile();
                            LOG.severe("***********************************************************  APRES f.createNewFile() : "+fitsCutTestFile);
                        }else{
                            LOG.severe("*********************************************************** AVANT fitsCut.delete();");
                            fitsCut.delete();
                            boolean fitsCutTestFile = fitsCut.createNewFile();
                            LOG.severe("***********************************************************  APRES f.createNewFile() et fitsCut.delete(); : "+fitsCutTestFile);
                        
                        }
                        cutOut.createCutoutFits(new FileOutputStream(fitsCut));
                    }
                    //rep = new CutOutRepresentation(MediaType.ALL, cutOut);
                    //************************** POUR RETOURNER le Fits ****************************************
                    /*rep = new CutOutRepresentation(MediaType.valueOf("image/fits"), cutOut);
                    final Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disp.setFilename(fileNameCut);
                    rep.setDisposition(disp);*/
                    
                    //Fits fitsFile = new Fits()
                    //LOG.severe("---------------------------- urlFinale : "+encodedurlAp4);
                    /*
                    URL obj = new URL(url1);
                
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    
                    //add request header
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    
                    int responseCode = con.getResponseCode();
                    LOG.info("************************* RESPONSE CODE DE LA REQUEST : "+responseCode);
                    */
                //}
                //k++;
                
            }
            
            
            
        }catch (SitoolsException ex) {
            LOG.severe("********************************************   "+ex.getMessage());
            try {
                if (Util.isSet(databaseRequest)) {
                    databaseRequest.close();
                }
            }catch (SitoolsException ex1) {
                LOG.log(Level.FINER, null, ex1);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(CutOutVoResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FitsException ex) {
            Logger.getLogger(CutOutVoResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CutOutException ex) {
            Logger.getLogger(CutOutVoResource.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (FileNotFoundException ex) {
            Logger.getLogger(ClassTestCutOut.class.getName()).log(Level.SEVERE, null, ex);
        } */catch (IOException ex) {
            Logger.getLogger(CutOutVoResource.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (URISyntaxException ex) {
            Logger.getLogger(ClassTestCutOut.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ClassTestCutOut.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClassTestCutOut.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return urlCutFitsFiles;
    }
    
}