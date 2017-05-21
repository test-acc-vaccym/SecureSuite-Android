/*
 * Copyright (c) 2017. Nuvolect LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Contact legal@nuvolect.com for a less restrictive commercial license if you would like to use the
 * software without the GPLv3 restrictions.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package com.nuvolect.securesuite.webserver.connector;//

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nuvolect.securesuite.util.LogUtil;
import com.nuvolect.securesuite.util.Omni;
import com.nuvolect.securesuite.util.OmniFile;
import com.nuvolect.securesuite.util.OmniUtil;
import com.nuvolect.securesuite.webserver.connector.base.ConnectorJsonCommand;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

//TODO create class description
//
public class CmdRename extends ConnectorJsonCommand {

    @Override
    public InputStream go(@NonNull Map<String, String> params) {
        //Target is a hashed volume and path
        String target = params.containsKey("target") ? params.get("target") : "" ;
        String url = params.get("url");

        OmniFile targetFile = OmniUtil.getFileFromHash(target);
        LogUtil.log(LogUtil.LogType.CMD_RENAME, "Target " + targetFile.getPath());

        String name = params.containsKey("name") ? params.get("name") : "";

        String volumeId = Omni.getVolumeId(target);
        String newPath = targetFile.getParentFile().getPath() + File.separator + name;
        OmniFile newFile = new OmniFile(volumeId, newPath);
        JsonArray added = new JsonArray();
        JsonArray removed = new JsonArray();
        JsonObject wrapper = new JsonObject();

        /**
         * Need to return actual file urls, not the objects
         */
        if (targetFile.renameFile(newFile)) {
            added.add(newFile.getFileObject(url));
            removed.add(targetFile.getFileObject(url));
        }
        wrapper.add("added", added);
        wrapper.add("removed", removed);
        return getInputStream(wrapper);
    }
}
