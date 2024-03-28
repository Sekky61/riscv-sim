/**
 * @file WeakHashMapSerializer.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Serialize WeakHashMap to JSON
 * @date 07 November      2023 21:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gradle.superscalarsim.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gradle.superscalarsim.managers.InstanceManager;
import com.gradle.superscalarsim.models.Identifiable;

import java.io.IOException;

/**
 * @brief Serializes {@link InstanceManager} to JSON object. Key is the id of the instance. Value is the instance itself.
 */
public class ManagerSerializer extends StdSerializer<InstanceManager<?>>
{
  
  public ManagerSerializer()
  {
    super(InstanceManager.class, false);
  }
  
  @Override
  public void serialize(InstanceManager value, JsonGenerator jgen, SerializerProvider provider) throws IOException
  {
    jgen.writeStartObject();
    
    // Loop over all instances and serialize them
    for (Object instance : value.getInstances())
    {
      Identifiable entry = (Identifiable) instance;
      String       key   = entry.getId();
      jgen.writeObjectField(key, entry);
    }
    
    jgen.writeEndObject();
  }
}
