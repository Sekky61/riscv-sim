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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gradle.superscalarsim.managers.IInstanceManager;
import com.gradle.superscalarsim.models.Identifiable;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class ManagerSerializer extends StdSerializer<IInstanceManager>
{
  
  public ManagerSerializer()
  {
    this(null);
  }
  
  public ManagerSerializer(Class<IInstanceManager> t)
  {
    super(t);
  }
  
  @Override
  public void serialize(IInstanceManager value,
                        JsonGenerator jgen,
                        SerializerProvider provider) throws IOException, JsonProcessingException
  {
    
    jgen.writeStartObject();
    
    // Loop over all instances and serialize them
    // Key is the instance id, value is the instance
    WeakHashMap                               instances = value.getInstances();
    Iterator<Map.Entry<Identifiable, Object>> itr       = instances.entrySet().iterator();
    while (itr.hasNext())
    {
      Map.Entry<Identifiable, Object> entry = itr.next();
      String                          key   = entry.getKey().getId();
      jgen.writeObjectField(key, entry.getKey());
    }
    
    jgen.writeEndObject();
  }
}
