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

package de.static_interface.sinklibrary.api.configuration.option;

import javax.annotation.Nullable;

public class YamlLongOption extends YamlOption<Long> {

    public YamlLongOption(String path, Long defaultValue) {
        super(path, defaultValue);
    }

    public YamlLongOption(String path, Long defaultValue, String comment) {
        super(path, defaultValue, comment);
    }

    public YamlLongOption(@Nullable YamlParentOption parent, String path, Long defaultValue) {
        super(parent, path, defaultValue);
    }

    public YamlLongOption(@Nullable YamlParentOption parent, String path, Long defaultValue, @Nullable String comment) {
        super(parent, path, defaultValue, comment);
    }

    @Override
    public Long getValue() {
        return getConfig().getYamlConfiguration().getLong(getPath(), getDefaultValue());
    }
}
