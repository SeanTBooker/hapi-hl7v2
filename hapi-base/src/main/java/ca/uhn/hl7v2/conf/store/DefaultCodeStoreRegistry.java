/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "DefaultCodeStoreRegistry.java".  Description:
 * "Default Implementation of a CodeStoreRegistry"
 *
 * The Initial Developer of the Original Code is University Health Network. Copyright (C)
 * 2012.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * GNU General Public License (the "GPL"), in which case the provisions of the GPL are
 * applicable instead of those above.  If you wish to allow use of your version of this
 * file only under the terms of the GPL and not to allow others to use your version
 * of this file under the MPL, indicate your decision by deleting  the provisions above
 * and replace  them with the notice and other provisions required by the GPL License.
 * If you do not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the GPL.
 *
 */
package ca.uhn.hl7v2.conf.store;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Implementation of a {@link CodeStoreRegistry} 
 * 
 * @author Christian Ohr
 */
public class DefaultCodeStoreRegistry implements CodeStoreRegistry {

    private static final List<CodeStoreRegistration> codeStores = new ArrayList<>();

    public void addCodeStore(CodeStore store) {
        addCodeStore(store, ".*");
    }

    public void addCodeStore(CodeStore store, String profileID) {
        codeStores.add(new CodeStoreRegistration(store, profileID));
    }

    public CodeStore getCodeStore(String profileID, String codeSystem) {
        CodeStore store = null;
        for (CodeStoreRegistration reg : codeStores) {
            if ((store = reg.matchingCodeStore(codeSystem, profileID)) != null)
                break;
        }
        return store;
    }

}
