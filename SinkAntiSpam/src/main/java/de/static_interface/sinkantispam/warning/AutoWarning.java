/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkantispam.warning;

import de.static_interface.sinkantispam.WarnUtil;
import de.static_interface.sinkantispam.database.row.Warning;
import de.static_interface.sinklibrary.user.IngameUser;

public abstract class AutoWarning extends Warning {

    public AutoWarning(IngameUser user, String reason, int id) {
        this.reason = reason;
        this.userWarningId = id;
        this.isAutoWarning = true;
        this.userId = WarnUtil.getWarnedPlayer(user).id;
    }
}
