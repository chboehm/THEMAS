/* 
 * Copyright 2015 Institute of Computer Science,
 *                Foundation for Research and Technology - Hellas.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 * 
 * =============================================================================
 * Contact: 
 * =============================================================================
 * Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
 *     Tel: +30-2810-391632
 *     Fax: +30-2810-391638
 *  E-mail: isl@ics.forth.gr
 * WebSite: http://www.ics.forth.gr/isl/cci.html
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 * This file is part of the THEMAS system.
 */
package DB_Classes;


import Utils.ConstantParameters;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
import java.util.*;

/**
 *
 * @author tzortzak
 */
public class DBCreate_Modify_Facet {

    /*
    QClass Q = new QClass();
    DBGeneral dbGen;
    HttpServlet ServletCaller; 
    
    DBConnect_Facet dbCon;
    DBRemove_Facet dbRemF;
    String errorMsg;
    IntegerObject sis_session;
    IntegerObject tms_session;
    boolean Facet_CreationOrModificationSucceded;
     */
    // constats used by this class methods
    public static final int DB_CREATE_Facet = 0;
    public static final int DB_MODIFY_Facet = 1;

    /*----------------------------------------------------------------------
    Constructor of DBCreate_Modify_Facet
    -----------------------------------------------------------------------*/
    public DBCreate_Modify_Facet(/*HttpSession session, HttpServlet caller, IntegerObject sisSession, IntegerObject tmsSession*/) {
        /*ServletCaller = caller;
        dbCon = new DBConnect_Facet(sessionInstance,ServletCaller, sisSession, tmsSession);
        dbGen = new DBGeneral();
        dbRemF = new DBRemove_Facet(sessionInstance,ServletCaller, sisSession, tmsSession);
        errorMsg = new String("");
        sis_session = sisSession;
        tms_session = tmsSession;
        
        Facet_CreationOrModificationSucceded = true;*/
    }

    /*---------------------------------------------------------------------
    Create_Or_ModifyFacet()
    -----------------------------------------------------------------------
    INPUT: - String targetFacet: the Facet to be created / modified
    - String createORmodify: "create" or "modify"
    determines if the targetFacet is going to be created or modified
    - String deletionOperator: "delete" or null
    in case of modification, determines if the targetFacet is going to be deleted/(undo)abandoned
    OUTPUT: a String with the result of the creation / modification
    FUNCTION: creates / modifies the given Facet
    CALLED BY: Create_Modify_Facet servlet
    ----------------------------------------------------------------------*/
    public boolean Create_Or_ModifyFacet(String selectedThesaurus, QClass Q, TMSAPIClass TA, IntegerObject sis_session, IntegerObject tms_session,  DBGeneral dbGen, String targetFacet,/* Vector targetFacetLetterCodes, */ String createORmodify, String deletionOperator, StringObject errorMsg, boolean errorIfExists, String pathToMessagesXml) {

        
        DBConnect_Facet dbCon = new DBConnect_Facet();

        StringObject errorMsgPrefix = new StringObject();
        if (createORmodify.equals("create")) {
            dbGen.Translate(errorMsgPrefix, "root/EditFacet/Creation/ErrorPrefix", null, pathToMessagesXml);
        } else {
            dbGen.Translate(errorMsgPrefix, "root/EditFacet/Edit/ErrorPrefix", null, pathToMessagesXml);
        }

        Q.reset_name_scope();
        // looking for Facet prefix (EKTClass`)
        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        //Utils.StaticClass.webAppSystemOut("DEBUG: Before first Query");
        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus, Q, sis_session.getValue());
        //Utils.StaticClass.webAppSystemOut("DEBUG: After first Query. prefix="+prefix);
        // convert target Facet to DB encoding with prefix
        StringObject targetFacetObj = new StringObject(prefix.concat(targetFacet));

        /*
        try {
            byte[] byteArray = targetFacet.getBytes("UTF-8");

            int maxFacetChars =  dbtr.getMaxBytesForFacet(selectedThesaurus, Q, sis_session);
            if(byteArray.length > maxFacetChars){
                Vector<String> errorArgs = new Vector<String>();
                errorArgs.add(""+maxFacetChars);
                errorArgs.add(""+byteArray.length);

                dbGen.Translate(errorMsg, "root/EditFacet/Edit/LongName", errorArgs, pathToMessagesXml);                
                return false;
            }
        } catch (UnsupportedEncodingException ex) {
            Utils.StaticClass.webAppSystemOut(ex.getMessage());
            Utils.StaticClass.handleException(ex);
        }
        */
        // in case of empty Facet
        if (targetFacetObj.getValue().trim().equals(prefix) == true) {

            dbGen.Translate(errorMsg, "root/EditFacet/Edit/NoTargetSpecified", null, pathToMessagesXml);

            errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
            
            return false;
        }
        
        int KindOfFacet = dbGen.GetKindOfFacet(selectedThesaurus, targetFacetObj, Q, sis_session);
        if (createORmodify.equals("create")) {
            errorMsg.setValue(errorMsg.getValue().concat(dbCon.ConnectFacet(selectedThesaurus, Q, TA, sis_session, tms_session, targetFacetObj, errorIfExists,pathToMessagesXml)));

        } else // modify
        {
            if (deletionOperator != null) { // delete / (undo) abandon facet


                DBRemove_Facet dbRemF = new DBRemove_Facet();
                if (KindOfFacet == ConstantParameters.FACET_OF_KIND_NEW) { // new facet => delete 
                    errorMsg.setValue(errorMsg.getValue().concat(dbRemF.DeleteFacet(Q, TA, sis_session, tms_session, dbGen, targetFacetObj)));
                } else {

                    //Facet_CreationOrModificationSucceded = true;
                    if (KindOfFacet == ConstantParameters.FACET_OF_KIND_OBSOLETE) { // obsolete facet => undo abandon
                        // convert BT_for_undo_abandon to DB encoding with prefix ?????

                        errorMsg.setValue(errorMsg.getValue().concat(dbRemF.UndoAbandonFacet(TA, tms_session, dbGen, targetFacetObj)));
                    }
                    if (KindOfFacet == ConstantParameters.FACET_OF_KIND_RELEASED) { // released facet => abandon
                        errorMsg.setValue(errorMsg.getValue().concat(dbRemF.AbandonFacet(TA, tms_session, dbGen, targetFacetObj)));
                    }


                }


            } /*else {
        
        //letter code modification and parent facets modifications should be handled 
        modifyLetterCodes(targetFacet, targetFacetLetterCodes);
        }*/
        }

        if (errorMsg != null && errorMsg.getValue() != null && errorMsg.getValue().length() > 0) { // case of error
            //Facet_CreationOrModificationSucceded = false;
            // abort transaction
            //Q.abort_transaction();
            //Q.end_transaction();
            errorMsg.setValue(errorMsgPrefix.getValue() + errorMsg.getValue());
            return false;

        } else { // case of NO error
            //Facet_CreationOrModificationSucceded = true;
            // end transaction
            //Q.end_transaction();
            Vector<String> errorArgs = new Vector<String>();
            errorArgs.add(targetFacet);
            
            if (createORmodify.equals("create")) {

                dbGen.Translate(errorMsg, "root/EditFacet/Creation/SuccessMsg", errorArgs, pathToMessagesXml);
                //errorMsg.setValue("Ο μικροθησαυρός : '" + targetFacet + "' δημιουργήθηκε με επιτυχία.");

            } else { // modify
                if (deletionOperator != null) { // delete / (undo) abandon descriptor
                    //String message = "";
                    if (KindOfFacet == ConstantParameters.FACET_OF_KIND_NEW) {
                        dbGen.Translate(errorMsg, "root/EditFacet/Deletion/SuccessMsg", errorArgs, pathToMessagesXml);
                        //message = "Ο μικροθησαυρός : '" + targetFacet + "' διαγράφηκε με επιτυχία.";
                    }
                    if (KindOfFacet == ConstantParameters.FACET_OF_KIND_OBSOLETE) {

                        dbGen.Translate(errorMsg, "root/EditFacet/Deletion/SuccessObsoleteMsg", errorArgs, pathToMessagesXml);
                        //message = "Ο μικροθησαυρός : '" + targetFacet + "' ολοκληρώθηκε η αναίρεση της κατάργησής του.";
                    }
                    if (KindOfFacet == ConstantParameters.FACET_OF_KIND_RELEASED) {
                        dbGen.Translate(errorMsg, "root/EditFacet/Deletion/SuccessReleasedMsg", errorArgs, pathToMessagesXml);
                        //message = "Ο μικροθησαυρός : '" + targetFacet + "' καταργήθηκε με επιτυχία.";
                    }
                     
                    //errorMsg.setValue(message);
                    //return message;
                } else {
                    dbGen.Translate(errorMsg, "root/EditFacet/Edit/SuccessMsg", errorArgs, pathToMessagesXml);
                    //errorMsg.setValue("Ο μικροθησαυρός : '" + targetFacet + "' τροποποιήθηκε με επιτυχία.");                
                }
            }

            return true;
        }

    }

    
    /*
    private void modifyLetterCodes(String selectedThesaurus,TMSAPIClass TA, String targetFacet, Vector targetFacetLetterCodes, Locale targetLocale) {
    
    //Get existing letter codes.
    Vector currentLC = new Vecto();
    Vector addLC = new Vecto();
    Vector removeLC = new Vecto();
    
    currentLC.addAll(dbGen.returnResults_Facet(sessionInstance, targetFacet, "letter_code",Q,sis_session,targetLocale));
    
    if (!currentLC.isEmpty()) {
    
    for (int k = 0; k < currentLC.size(); k++) {
    
    //if one current letter code does not exist in targetFacetLetterCodes then it should be deleted
    if (!targetFacetLetterCodes.contains(currentLC.get(k).toString())) {
    removeLC.addElement(currentLC.get(k).toString());
    }
    }
    }
    
    //Now find out which are the new letter codes need that must be added
    for (int l = 0; l < targetFacetLetterCodes.size(); l++) {
    
    if (!currentLC.contains(targetFacetLetterCodes.get(l).toString())) {
    
    addLC.addElement(targetFacetLetterCodes.get(l).toString());
    }
    }
    
    //Preparing Deletion And Addition
    //looking for Facet prefix (EKT`)
    DBThesaurusReferences dbtr = new DBThesaurusReferences();
    String prefix = dbtr.getThesaurusPrefix_Class(sessionInstance,Q,sis_session.getValue());
    
    // convert target Facet to DB encoding with prefix
    StringObject targetFacetObj = new StringObject(prefix.concat(targetFacet));
    
    int sisSessionId = sis_session.getValue();
    // get instances of EKTFacet
    Q.reset_name_scope();
    Q.set_current_node(sisSessionId, targetFacetObj);
    
    //Get letter_code links originating from current node
    int linkFromSet = Q.get_link_from_by_category(sisSessionId, 0, new StringObject("Facet"), new StringObject("letter_code"));
    
    Q.reset_set(sisSessionId, linkFromSet);
    
    StringObject fromcls = new StringObject();
    StringObject label = new StringObject();
    StringObject categ = new StringObject();
    StringObject cls = new StringObject();
    IntegerObject uniq_categ = new IntegerObject();
    
    IntegerObject clsID = new IntegerObject();
    IntegerObject linkID = new IntegerObject();
    IntegerObject categID = new IntegerObject();
    CMValue cmv = new CMValue();
    int ret;
    
    //Delete Letter codes
    //traverse all letter code links originating from current facet and check if deletion is needed
    while (Q.retur_full_link_id(sisSessionId, linkFromSet, cls, clsID, label, linkID, categ, fromcls, categID, cmv, uniq_categ) != QClass.APIFail) {
    
    //If letter_code value exists in removeLC Vector then perform delete and get possible error message
    String temp = cmv.getString();
    if (removeLC.contains(temp)) {
    
    ret = TA.DeleteFacetAttribute(this.tms_session.getValue(), linkID.getValue(), targetFacetObj);
    
    if (ret == TMSAPIClass.TMS_APIFail) {
    errorMsg = errorMsg.concat("<tr><td>" + dbGen.check_success(ret, null,tms_session) + "</td></tr>");
    return;
    }
    }
    
    }
    //We might want here to check if every removeLC elemnt has been deleted and inform user.
    
    
    //Add Letter Codes
    //Commit Facet Additions
    for (int m = 0; m < addLC.size(); m++) {
    
    StringObject nullLinkName = new StringObject();
    cmv.assign_string(addLC.get(m).toString());
    int catSet = Q.set_get_new();
    Q.reset_name_scope();
    Q.set_current_node(new StringObject("Facet"));
    Q.set_current_node(new StringObject("letter_code"));
    Q.set_put(catSet);
    
    ret = TA.CreateFacetAttribute(tms_session.getValue(), nullLinkName, targetFacetObj, cmv, catSet);
    
    if (ret == TMSAPIClass.TMS_APIFail) {
    errorMsg = errorMsg.concat("<tr><td>" + dbGen.check_success(ret, null,tms_session) + "</td></tr>");
    return;
    }
    
    }
    
    Q.free_all_sets();
    
    }*/
}
