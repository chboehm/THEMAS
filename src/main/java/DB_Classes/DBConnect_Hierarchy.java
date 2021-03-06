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



import javax.servlet.http.*;
import neo4j_sisapi.*;
import neo4j_sisapi.tmsapi.TMSAPIClass;
/**
 *
 * @author tzortzak
 */

/*---------------------------------------------------------------------
                            DBConnect_Hierarchy
-----------------------------------------------------------------------
class with methods updating Hierarchies (basically for creation) of the SIS data base
used by DBCreate_Modify_Hierarchy
----------------------------------------------------------------------*/ 
public class DBConnect_Hierarchy {

    /*
    QClass Q = new QClass();
    DBGeneral g;
    HttpServlet ServletCaller;
    
    IntegerObject sis_session;
    IntegerObject tms_session;
*/
    public DBConnect_Hierarchy() {
        /*ServletCaller = caller;
        g = new DBGeneral();
        sis_session = sisSession;
        tms_session = tmsSession;*/
        
    }
 
    public String ConnectHierarchy(String selectedThesaurus,QClass Q, TMSAPIClass TA,IntegerObject sis_session, IntegerObject tms_session, StringObject targetHierarchyObj, StringObject targetHierarchyFacetObj, String pathToErrorsXML) {

        StringObject errorMsgObj = new StringObject("");

        DBThesaurusReferences dbtr = new DBThesaurusReferences();
        DBGeneral dbGen = new DBGeneral();
        String prefix = dbtr.getThesaurusPrefix_Class(selectedThesaurus,Q,sis_session.getValue());

        if (targetHierarchyObj.getValue().trim().equals(prefix)) {
            dbGen.Translate(errorMsgObj, "root/EditHierarchy/Creation/EmptyName", null, pathToErrorsXML);
            
            //errorMSG = errorMSG.concat("δεν είναι εφικτή η δημιουργία κενής ιεραρχίας");
            return errorMsgObj.getValue();
        }

        if (targetHierarchyFacetObj.getValue().trim().equals(prefix)) {
            dbGen.Translate(errorMsgObj, "root/EditHierarchy/Creation/NoFacetName", null, pathToErrorsXML);
            //errorMSG = errorMSG.concat("δεν είναι εφικτή η ταξινόμιση σε κενό μικροθησαυρό");
            return errorMsgObj.getValue();
        }

        int ret = TA.CHECK_CreateHierarchy(targetHierarchyObj, targetHierarchyFacetObj);

        if (ret == TMSAPIClass.TMS_APIFail) {

            errorMsgObj.setValue(dbGen.check_success(ret, TA, null,tms_session));
            //errorMSG = errorMSG.concat(/*"<tr><td>" + */dbGen.check_success(ret, null,tms_session) /*+ "</td></tr>"*/);
            return errorMsgObj.getValue();
        }

        return errorMsgObj.getValue();

    }
}
